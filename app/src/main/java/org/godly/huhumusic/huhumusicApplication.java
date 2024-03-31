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

package org.godly.huhumusic;

import android.app.Application;

import androidx.core.app.NotificationManagerCompat;

import org.godly.huhumusic.cache.ImageCache;
import org.godly.huhumusic.player.AudioEffects;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to turn off logging for jaudiotagger and free up memory when
 * {@code #onLowMemory()} is called on pre-ICS devices. On post-ICS memory is
 * released within {@link ImageCache}.
 *

 */
public class huhumusicApplication extends Application {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// Turn off logging for jaudiotagger.
		Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLowMemory() {
		// clear image cache
		ImageCache.getInstance(this).evictAll();
		super.onLowMemory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTerminate() {
		try {
			// remove notification
			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
			notificationManager.cancelAll();
			// release audioeffects
			AudioEffects.release();
		} catch (SecurityException exception) {
			// ignore
		}
		super.onTerminate();
	}
}