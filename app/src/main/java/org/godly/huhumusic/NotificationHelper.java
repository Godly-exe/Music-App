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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import org.godly.huhumusic.service.MusicPlaybackService;
import org.godly.huhumusic.utils.PreferenceUtils;

/**
 * Builds the notification for Apollo's service. Jelly Bean and higher uses the
 * expanded notification by default.
 *

 */
public class NotificationHelper {

	/**
	 * Notification ID
	 * use different notification IDs for each build to avoid conflics
	 */
	private static final int APOLLO_MUSIC_SERVICE = BuildConfig.DEBUG ? 0x5D74E856 : 0x28E61796;

	/**
	 *
	 */
	private static final String INTENT_AUDIO_PLAYER = BuildConfig.APPLICATION_ID + ".AUDIO_PLAYER";

	/**
	 * Notification name
	 */
	private static final String NOTFICIATION_NAME = "HuHuMusic Controlpanel";

	/**
	 * Service context
	 */
	private MusicPlaybackService mService;

	/**
	 * manage and update notification
	 */
	private NotificationManagerCompat notificationManager;

	/**
	 * Builder used to construct a notification
	 */
	private NotificationCompat.Builder notificationBuilder;

	private PreferenceUtils mPreferences;

	/**
	 * notification views
	 */
	private RemoteViews mSmallContent, mExpandedView;

	/**
	 * callbacks to the service
	 */
	private PendingIntent callbackPlayPause, callbackNext, callbackPrevious, callbackStop;

	/**
	 * Constructor of <code>NotificationHelper</code>
	 *
	 * @param service callback to the service
	 */
	public NotificationHelper(MusicPlaybackService service, MediaSessionCompat mSession) {
		mService = service;
		mPreferences = PreferenceUtils.getInstance(service.getApplicationContext());
		// init notification manager & channel
		NotificationChannelCompat notificationChannel = new NotificationChannelCompat.Builder(MusicPlaybackService.NOTIFICAITON_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
				.setName(NOTFICIATION_NAME).setLightsEnabled(false).setVibrationEnabled(false).setSound(null, null).build();
		notificationManager = NotificationManagerCompat.from(service);
		notificationManager.createNotificationChannel(notificationChannel);

		// initialize player activity callback
		Intent intent = new Intent(INTENT_AUDIO_PLAYER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// initialize callbacks
		ComponentName serviceName = new ComponentName(mService, MusicPlaybackService.class);
		callbackPlayPause = PendingIntent.getService(mService, 1, new Intent(MusicPlaybackService.ACTION_TOGGLEPAUSE).setComponent(serviceName), PendingIntent.FLAG_MUTABLE);
		callbackNext = PendingIntent.getService(mService, 2, new Intent(MusicPlaybackService.ACTION_NEXT).setComponent(serviceName), PendingIntent.FLAG_MUTABLE);
		callbackPrevious = PendingIntent.getService(mService, 3, new Intent(MusicPlaybackService.ACTION_PREVIOUS).setComponent(serviceName), PendingIntent.FLAG_MUTABLE);
		callbackStop = PendingIntent.getService(mService, 4, new Intent(MusicPlaybackService.ACTION_STOP).setComponent(serviceName), PendingIntent.FLAG_MUTABLE);
		PendingIntent contentIntent = PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_IMMUTABLE);

		// create notification builder
		notificationBuilder = new NotificationCompat.Builder(mService, MusicPlaybackService.NOTIFICAITON_CHANNEL_ID)
				.setSmallIcon(R.drawable.stat_notify_music)
				.setContentIntent(contentIntent)
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

		// use embedded media control notification of Android
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !mPreferences.oldNotificationLayoutEnabled()) {
			MediaStyle mediaStyle = new MediaStyle();
			mediaStyle.setMediaSession(mSession.getSessionToken());
			notificationBuilder.setStyle(mediaStyle);
		}
		// use custom notification layout for old Android version
		else {
			// initialize small notification view
			mSmallContent = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.notification_template_base);
			mSmallContent.setOnClickPendingIntent(R.id.notification_base_play, callbackPlayPause);
			mSmallContent.setOnClickPendingIntent(R.id.notification_base_next, callbackNext);
			mSmallContent.setOnClickPendingIntent(R.id.notification_base_previous, callbackPrevious);
			mSmallContent.setOnClickPendingIntent(R.id.notification_base_collapse, callbackStop);
			// initialize expanded notification view
			mExpandedView = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.notification_template_expanded_base);
			mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_play, callbackPlayPause);
			mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_next, callbackNext);
			mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, callbackPrevious);
			mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, callbackStop);
			notificationBuilder.setCustomBigContentView(mExpandedView).setCustomContentView(mSmallContent)
					.setPriority(NotificationCompat.PRIORITY_LOW).setOnlyAlertOnce(true).setOngoing(true).setAutoCancel(false).setSilent(true);
		}
	}

	/**
	 * Call this to build the {@link Notification}.
	 */
	public void createNotification() {
		Notification notification = buildNotification();
		postNotification(notification);
		mService.startForeground(APOLLO_MUSIC_SERVICE, notification);
	}

	/**
	 * update existing notification
	 */
	public void updateNotification() {
		postNotification(buildNotification());
	}

	/**
	 * cancel notification when app is in foreground
	 */
	public void cancelNotification() {
		postNotification(null);
	}

	/**
	 * Changes the playback controls in and out of a paused state
	 */
	private Notification buildNotification() {
		// build integrated media control
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !mPreferences.oldNotificationLayoutEnabled()) {
			// set track information to notification directly
			notificationBuilder.setContentTitle(mService.getTrackName());
			notificationBuilder.setContentText(mService.getArtistName());
			notificationBuilder.setLargeIcon(mService.getAlbumArt());
			// init media control (fallback if not supported by MediaStyle)
			notificationBuilder.clearActions();
			notificationBuilder.addAction(R.drawable.btn_playback_previous, "Previous", callbackPrevious);
			if (mService.isPlaying()) {
				notificationBuilder.addAction(R.drawable.btn_playback_pause, "Pause", callbackPlayPause);
			} else {
				notificationBuilder.addAction(R.drawable.btn_playback_play, "Play", callbackPlayPause);
			}
			notificationBuilder.addAction(R.drawable.btn_playback_next, "Next", callbackNext);
			notificationBuilder.addAction(R.drawable.btn_playback_stop, "Stop", callbackStop);
		}
		// build legacy notification
		else {
			int iconRes = mService.isPlaying() ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play;
			// update small notification view
			mSmallContent.setTextViewText(R.id.notification_base_line_one, mService.getTrackName());
			mSmallContent.setTextViewText(R.id.notification_base_line_two, mService.getArtistName());
			mSmallContent.setImageViewBitmap(R.id.notification_base_image, mService.getAlbumArt());
			mSmallContent.setImageViewResource(R.id.notification_base_play, iconRes);
			// update expanded notification view
			mExpandedView.setTextViewText(R.id.notification_expanded_base_line_one, mService.getTrackName());
			mExpandedView.setTextViewText(R.id.notification_expanded_base_line_two, mService.getAlbumName());
			mExpandedView.setTextViewText(R.id.notification_expanded_base_line_three, mService.getArtistName());
			mExpandedView.setImageViewBitmap(R.id.notification_expanded_base_image, mService.getAlbumArt());
			mExpandedView.setImageViewResource(R.id.notification_expanded_base_play, iconRes);
			// add view to notification
			notificationBuilder.setCustomBigContentView(mExpandedView).setCustomContentView(mSmallContent);
		}
		return notificationBuilder.build();
	}

	/**
	 * post/cancel notification
	 *
	 * @param notification notification to post or null to remove existing notification
	 */
	private void postNotification(@Nullable Notification notification) {
		try {
			if (notification != null) {
				notificationManager.notify(APOLLO_MUSIC_SERVICE, notification);
			} else {
				notificationManager.cancel(APOLLO_MUSIC_SERVICE);
			}
		} catch (SecurityException exception) {
			// caught on missing permission. Normally app requires permission to post notification
			// so this scenario can't happen
		}
	}
}