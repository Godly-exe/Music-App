package org.godly.huhumusic.player;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.godly.huhumusic.BuildConfig;
import org.godly.huhumusic.service.MusicPlaybackService;

import java.lang.ref.WeakReference;

/**
 * custom MediaPlayer implementation containing two MediaPlayer to switch fast tracks
 */
public class MultiPlayer implements OnErrorListener, OnCompletionListener {

	private final WeakReference<MusicPlaybackService> mService;

	private Handler mHandler;

	private MediaPlayer mCurrentMediaPlayer;

	@Nullable
	private MediaPlayer mNextMediaPlayer;

	private boolean mIsInitialized = false;

	/**
	 * Constructor of <code>MultiPlayer</code>
	 */
	public MultiPlayer(MusicPlaybackService service) {
		mService = new WeakReference<>(service);
		mCurrentMediaPlayer = createPlayer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			mIsInitialized = false;
			mCurrentMediaPlayer.reset();
			mHandler.sendMessageDelayed(mHandler.obtainMessage(MusicPlaybackService.MESSAGE_SERVER_DIED), 2000);
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
			// switch to next player
			mCurrentMediaPlayer.release();
			mCurrentMediaPlayer = mNextMediaPlayer;
			mNextMediaPlayer = null;
			//
			mHandler.sendEmptyMessage(MusicPlaybackService.MESSAGE_TRACK_WENT_TO_NEXT);
		} else {
			mHandler.sendEmptyMessage(MusicPlaybackService.MESSAGE_TRACK_ENDED);
		}
	}

	/**
	 * @param uri The path of the file, or the http/rtsp URL of the stream
	 *             you want to play
	 */
	public void setDataSource(Uri uri) {
		mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, uri);
		if (mIsInitialized) {
			resetNextPlayer();
		}
	}

	/**
	 * Set the MediaPlayer to start when this MediaPlayer finishes playback.
	 *
	 * @param uri The path of the file, or the http/rtsp URL of the stream
	 *             you want to play
	 */
	public void setNextDataSource(@NonNull Uri uri) {
		try {
			mNextMediaPlayer = createPlayer();
			mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
			if (setDataSourceImpl(mNextMediaPlayer, uri)) {
				// prepare next player
				mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
			} else {
				// an error occured, reset next player
				resetNextPlayer();
			}
		} catch (Exception err) {
			if (BuildConfig.DEBUG) {
				err.printStackTrace();
			}
		}
	}

	/**
	 * remove next player
	 */
	public void resetNextPlayer() {
		try {
			mCurrentMediaPlayer.setNextMediaPlayer(null);
			if (mNextMediaPlayer != null) {
				mNextMediaPlayer.release();
				mNextMediaPlayer = null;
			}
		} catch (Exception err) {
			if (BuildConfig.DEBUG) {
				err.printStackTrace();
			}
		}
	}

	/**
	 * Sets the handler
	 *
	 * @param handler The handler to use
	 */
	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	/**
	 * @return True if the player is ready to go, false otherwise
	 */
	public boolean isInitialized() {
		return mIsInitialized;
	}

	/**
	 * Starts or resumes playback.
	 */
	public void start() {
		mCurrentMediaPlayer.start();
	}

	/**
	 * Resets the MediaPlayer to its uninitialized state.
	 */
	public void stop() {
		mCurrentMediaPlayer.reset();
		mIsInitialized = false;
	}

	/**
	 * Releases resources associated with this MediaPlayer object.
	 */
	public void release() {
		stop();
		mCurrentMediaPlayer.release();
	}

	/**
	 * Pauses playback. Call start() to resume.
	 */
	public void pause() {
		mCurrentMediaPlayer.pause();
	}

	/**
	 * Gets the duration of the file.
	 *
	 * @return The duration in milliseconds
	 */
	public long duration() {
		return mCurrentMediaPlayer.getDuration();
	}

	/**
	 * Gets the current playback position.
	 *
	 * @return The current position in milliseconds
	 */
	public long position() {
		return mCurrentMediaPlayer.getCurrentPosition();
	}

	/**
	 * Gets the current playback position.
	 *
	 * @param whereto The offset in milliseconds from the start to seek to
	 */
	public void seek(long whereto) {
		mCurrentMediaPlayer.seekTo((int) whereto);
	}

	/**
	 * Sets the volume on this player.
	 *
	 * @param vol Left and right volume scalar
	 */
	public void setVolume(float vol) {
		mCurrentMediaPlayer.setVolume(vol, vol);
	}

	/**
	 * Returns the audio session ID.
	 *
	 * @return The current audio session ID.
	 */
	public int getAudioSessionId() {
		return mCurrentMediaPlayer.getAudioSessionId();
	}

	/**
	 * create and configure MediaPlayer instance
	 *
	 * @return player
	 */
	private MediaPlayer createPlayer() {
		MediaPlayer player = new MediaPlayer();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			AudioAttributes attr = new AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.setUsage(AudioAttributes.USAGE_MEDIA).build();
			player.setAudioAttributes(attr);
		}
		return player;
	}

	/**
	 * @param player The {@link MediaPlayer} to use
	 * @param uri   The path of the file, or the http/rtsp URL of the stream
	 *               you want to play
	 * @return True if the <code>player</code> has been prepared and is
	 * ready to play, false otherwise
	 */
	private boolean setDataSourceImpl(MediaPlayer player, @NonNull Uri uri) {
		MusicPlaybackService musicService = mService.get();
		if (musicService != null) {
			try {
				player.reset();
				player.setOnPreparedListener(null);
				player.setDataSource(musicService.getApplicationContext(), uri);
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				player.prepare();
			} catch (Exception err) {
				if (BuildConfig.DEBUG) {
					err.printStackTrace();
				}
				return false;
			}
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
			intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
			intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, MusicPlaybackService.APOLLO_PACKAGE_NAME);
			musicService.sendBroadcast(intent);
			return true;
		}
		return false;
	}
}