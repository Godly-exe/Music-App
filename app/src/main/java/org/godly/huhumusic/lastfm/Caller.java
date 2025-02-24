/*
 * 
 * reserved. Redistribution and use of this software in source and binary forms,
 * with or without modification, are permitted provided that the following
 * conditions are met: - Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following disclaimer. -
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. THIS SOFTWARE IS
 * PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.godly.huhumusic.lastfm;

import android.annotation.SuppressLint;
import android.content.Context;

import org.godly.huhumusic.BuildConfig;
import org.godly.huhumusic.lastfm.Result.Status;
import org.godly.huhumusic.utils.PreferenceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The <code>Caller</code> class handles the low-level communication between the
 * client and last.fm.<br/>
 * Direct usage of this class should be unnecessary since all method calls are
 * available via the methods in the <code>Artist</code>, <code>Album</code>,
 * <code>User</code>, etc. classes. If specialized calls which are not covered
 * by the Java API are necessary this class may be used directly.<br/>
 * Supports the setting of a custom {@link Proxy} and a custom
 * <code>User-Agent</code> HTTP header.
 *
 * @author Janni Kovacs
 */

public class Caller {

	/**
	 *
	 */
	private static final String PARAM_API_KEY = "api_key";

	/**
	 * user aent for Last FM
	 */
	private static final String USER_AGENT = "Apollo";

	/**
	 *
	 */
	private static final String DEFAULT_API_ROOT = "https://ws.audioscrobbler.com/2.0/";

	/**
	 * LAST FM Key used to download track informations
	 */
	private static final String LASTFM_API_KEY = "";

	/**
	 * singleton instance
	 */
	private static final Caller mInstance = new Caller();

	/**
	 * app preferences used to get API-key
	 */
	private PreferenceUtils mPrefs;


	private Caller() {
	}

	/**
	 * @return A new instance of this class
	 */
	public static synchronized Caller getInstance(Context context) {
		mInstance.mPrefs = PreferenceUtils.getInstance(context);
		return mInstance;
	}


	public Result call(String method, String... params) {
		return call(method, StringUtilities.map(params));
	}

	/**
	 * Performs the web-service call. If the <code>session</code> parameter is
	 * <code>non-null</code> then an authenticated call is made. If it's
	 * <code>null</code> then an unauthenticated call is made.<br/>
	 * The <code>apiKey</code> parameter is always required, even when a valid
	 * session is passed to this method.
	 *
	 * @param method The method to call
	 * @param params Parameters
	 * @return the result of the operation
	 */
	public Result call(String method, Map<String, String> params) {
		params = new WeakHashMap<>(params);
		InputStream inputStream;
		Result lastResult;

		String apiKey = mPrefs.getApiKey();
		if (!apiKey.isEmpty()) {
			params.put(PARAM_API_KEY, apiKey);
		} else {
			params.put(PARAM_API_KEY, LASTFM_API_KEY);
		}
		try {
			HttpsURLConnection urlConnection = openPostConnection(method, params);
			inputStream = getInputStreamFromConnection(urlConnection);
			if (inputStream == null) {
				lastResult = Result.createHttpErrorResult(urlConnection.getResponseCode(), urlConnection.getResponseMessage());
				return lastResult;
			}
		} catch (IOException ioEx) {
			lastResult = Result.createHttpErrorResult(503, ioEx.getLocalizedMessage());
			return lastResult;
		}
		try {
			lastResult = createResultFromInputStream(inputStream);
		} catch (IOException ioEx) {
			if (BuildConfig.DEBUG) {
				ioEx.printStackTrace();
			}
			lastResult = new Result(ioEx.getLocalizedMessage());
		} catch (SAXException saxEx) {
			if (BuildConfig.DEBUG) {
				saxEx.printStackTrace();
			}
			lastResult = new Result(saxEx.getLocalizedMessage());
		}
		return lastResult;
	}

	/**
	 * Creates a new {@link HttpsURLConnection}, sets the proxy, if available,
	 * and sets the User-Agent property.
	 *
	 * @param url URL to connect to
	 * @return a new connection.
	 * @throws IOException if an I/O exception occurs.
	 */
	public HttpsURLConnection openConnection(String url) throws IOException {
		URL u = new URL(url);
		HttpsURLConnection urlConnection;
		urlConnection = (HttpsURLConnection) u.openConnection();
		urlConnection.setRequestProperty("User-Agent", USER_AGENT);
		urlConnection.setUseCaches(true);
		return urlConnection;
	}


	private HttpsURLConnection openPostConnection(String method, Map<String, String> params) throws IOException {
		HttpsURLConnection urlConnection = openConnection(DEFAULT_API_ROOT);
		urlConnection.setRequestMethod("POST");
		urlConnection.setDoOutput(true);
		urlConnection.setUseCaches(true);
		OutputStream outputStream = urlConnection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		String post = buildPostBody(method, params);
		writer.write(post);
		writer.close();
		return urlConnection;
	}


	private InputStream getInputStreamFromConnection(HttpsURLConnection connection) throws IOException {
		int responseCode = connection.getResponseCode();
		if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN || responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
			return connection.getErrorStream();
		} else if (responseCode == HttpsURLConnection.HTTP_OK) {
			return connection.getInputStream();
		}
		return null;
	}


	@SuppressLint("NewApi")
	private Result createResultFromInputStream(InputStream inputStream) throws SAXException, IOException {
		InputSource input = new InputSource(new InputStreamReader(inputStream));
		Document document = newDocumentBuilder().parse(input);
		Element root = document.getDocumentElement(); // lfm element
		String statusString = root.getAttribute("status");
		Status status = "ok".equals(statusString) ? Status.OK : Status.FAILED;
		if (status == Status.FAILED) {
			Element errorElement = (Element) root.getElementsByTagName("error").item(0);
			int errorCode = Integer.parseInt(errorElement.getAttribute("code"));
			String message = errorElement.getTextContent();
			return Result.createRestErrorResult(errorCode, message);
		} else {
			return Result.createOkResult(document);
		}
	}


	private DocumentBuilder newDocumentBuilder() {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			return builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			if (BuildConfig.DEBUG) {
				e.printStackTrace();
			}
			// better never happens
			throw new RuntimeException(e);
		}
	}


	private String buildPostBody(String method, Map<String, String> params, String... strings) {
		StringBuilder builder = new StringBuilder(100);
		builder.append("method=");
		builder.append(method);
		builder.append('&');
		for (Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, String> entry = it.next();
			builder.append(entry.getKey());
			builder.append('=');
			builder.append(StringUtilities.encode(entry.getValue()));
			if (it.hasNext() || strings.length > 0) {
				builder.append('&');
			}
		}
		int count = 0;
		for (String string : strings) {
			builder.append(count % 2 == 0 ? string : StringUtilities.encode(string));
			count++;
			if (count != strings.length) {
				if (count % 2 == 0) {
					builder.append('&');
				} else {
					builder.append('=');
				}
			}
		}
		return builder.toString();
	}
}