package org.godly.huhumusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.godly.huhumusic.service.MusicPlaybackService;

import java.lang.ref.WeakReference;

/**
 * this class updates the current play status from {@link MusicPlaybackService} to an activity
 */
public class PlaybackStatus extends BroadcastReceiver {

	/**
	 * callback reference
	 */
	private WeakReference<PlayStatusListener> mReference;


	/**
	 * @param callback callback listener to update information
	 */
	public PlaybackStatus(PlayStatusListener callback) {
		mReference = new WeakReference<>(callback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		PlayStatusListener callback = mReference.get();

		if (action != null && callback != null) {
			switch (action) {
				case MusicPlaybackService.CHANGED_META:
					callback.onMetaChange();
					break;

				case MusicPlaybackService.CHANGED_PLAYSTATE:
					callback.onStateChange();
					break;

				case MusicPlaybackService.CHANGED_REPEATMODE:
				case MusicPlaybackService.CHANGED_SHUFFLEMODE:
					callback.onModeChange();
					break;

				case MusicPlaybackService.ACTION_REFRESH:
					callback.refresh();
					break;
			}
		}
	}

	/**
	 * callback listener for status change
	 */
	public interface PlayStatusListener {

		/**
		 * called when meta information changes
		 */
		void onMetaChange();

		/**
		 * called when playstate changes
		 */
		void onStateChange();

		/**
		 * called when mode changes between repeat and shuffle
		 */
		void onModeChange();

		/**
		 *
		 */
		void refresh();
	}
}