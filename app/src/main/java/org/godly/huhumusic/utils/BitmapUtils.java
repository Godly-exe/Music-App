/*
 * 
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.godly.huhumusic.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * {@link Bitmap} specific helpers.
 *

 */
public final class BitmapUtils {

	/* Initial blur radius. */
	private static final int DEFAULT_BLUR_RADIUS = 8;

	/**
	 * This class is never instantiated
	 */
	private BitmapUtils() {
	}

	/**
	 * Takes a bitmap and creates a new slightly blurry version of it.
	 *
	 * @param sentBitmap The {@link Bitmap} to blur.
	 * @return A blurred version of the given {@link Bitmap}.
	 */
	public static Bitmap createBlurredBitmap(Bitmap sentBitmap) {
		if (sentBitmap == null) {
			return null;
		}

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap mBitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		int w = mBitmap.getWidth();
		int h = mBitmap.getHeight();

		int[] pix = new int[w * h];
		mBitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = DEFAULT_BLUR_RADIUS + DEFAULT_BLUR_RADIUS + 1;

		int[] r = new int[wh];
		int[] g = new int[wh];
		int[] b = new int[wh];
		int[] vmin = new int[Math.max(w, h)];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;

		int divsum = div + 1 >> 1;
		divsum *= divsum;
		int[] dv = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = i / divsum;
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = DEFAULT_BLUR_RADIUS + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -DEFAULT_BLUR_RADIUS; i <= DEFAULT_BLUR_RADIUS; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + DEFAULT_BLUR_RADIUS];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = p & 0x0000ff;
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = DEFAULT_BLUR_RADIUS;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - DEFAULT_BLUR_RADIUS + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + DEFAULT_BLUR_RADIUS + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = p & 0x0000ff;

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -DEFAULT_BLUR_RADIUS * w;
			for (i = -DEFAULT_BLUR_RADIUS; i <= DEFAULT_BLUR_RADIUS; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + DEFAULT_BLUR_RADIUS];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = DEFAULT_BLUR_RADIUS;
			for (y = 0; y < h; y++) {
				pix[yi] = 0xff000000 | dv[rsum] << 16 | dv[gsum] << 8 | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - DEFAULT_BLUR_RADIUS + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		mBitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return mBitmap;
	}

	/**
	 * This is only used when the launcher shortcut is created.
	 *
	 * @param bitmap The artist, album, genre, or playlist image that's going to
	 *               be cropped.
	 * @param size   The new size.
	 * @return A {@link Bitmap} that has been resized and cropped for a launcher
	 * shortcut.
	 */
	public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (w == size && h == size) {
			return bitmap;
		}

		float mScale = (float) size / Math.min(w, h);

		Bitmap mTarget = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		int mWidth = Math.round(mScale * bitmap.getWidth());
		int mHeight = Math.round(mScale * bitmap.getHeight());
		Canvas mCanvas = new Canvas(mTarget);
		mCanvas.translate((size - mWidth) / 2f, (size - mHeight) / 2f);
		mCanvas.scale(mScale, mScale);
		Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		mCanvas.drawBitmap(bitmap, 0, 0, paint);
		return mTarget;
	}
}
