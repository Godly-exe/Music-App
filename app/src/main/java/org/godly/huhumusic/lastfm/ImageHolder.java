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

import androidx.annotation.NonNull;

import org.godly.huhumusic.BuildConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Abstract superclass for all items that may contain images (such as
 * {@link Artist}s, {@link Album}s
 *
 * @author Janni Kovacs
 */
public abstract class ImageHolder {

	protected Map<ImageSize, String> imageUrls = new HashMap<>();

	/**
	 *
	 */
	protected static void loadImages(ImageHolder holder, DomElement element) {
		Collection<DomElement> images = element.getChildren("image");
		for (DomElement image : images) {
			String attribute = image.getAttribute("size");
			ImageSize size = null;
			if (attribute == null || attribute.isEmpty()) {
				size = ImageSize.UNKNOWN;
			} else {
				try {
					size = ImageSize.valueOf(attribute.toUpperCase(Locale.ENGLISH));
				} catch (IllegalArgumentException e) {
					if (BuildConfig.DEBUG) {
						e.printStackTrace();
					}
					// if they suddenly again introduce a new image size
				}
			}
			if (size != null) {
				holder.imageUrls.put(size, image.getText());
			}
		}
	}

	/**
	 * Returns the URL of the image in the specified size, or <code>null</code>
	 * if not available.
	 *
	 * @param size The preferred size
	 * @return an image URL
	 */
	public String getImageURL(ImageSize size) {
		return imageUrls.get(size);
	}


	@NonNull
	@Override
	public String toString() {
		return imageUrls.toString();
	}
}