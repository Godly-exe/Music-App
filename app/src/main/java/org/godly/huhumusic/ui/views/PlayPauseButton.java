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

package org.godly.huhumusic.ui.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

import androidx.appcompat.widget.AppCompatImageButton;

import org.godly.huhumusic.R;
import org.godly.huhumusic.ui.views.theme.HoloSelector;
import org.godly.huhumusic.utils.ApolloUtils;
import org.godly.huhumusic.utils.MusicUtils;

/**
 * A custom {@link ImageButton} that represents the "play and pause" button.
 *

 */
public class PlayPauseButton extends AppCompatImageButton implements OnClickListener, OnLongClickListener {

	/**
	 * @param context The {@link Context} to use
	 * @param attrs   The attributes of the XML tag that is inflating the view.
	 */
	public PlayPauseButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Theme the selector
		setBackground(new HoloSelector(context));
		// Control playback (play/pause)
		setOnClickListener(this);
		// Show the cheat sheet
		setOnLongClickListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClick(View v) {
		if (MusicUtils.isPlaying())
			MusicUtils.pause();
		else
			MusicUtils.play(getContext());
		updateState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onLongClick(View view) {
		if (TextUtils.isEmpty(view.getContentDescription()))
			return false;
		ApolloUtils.showCheatSheet(view);
		return true;
	}

	/**
	 * Sets the correct drawable for playback.
	 */
	public void updateState() {
		if (MusicUtils.isPlaying()) {
			setContentDescription(getResources().getString(R.string.accessibility_pause));
			setImageResource(R.drawable.btn_playback_pause);
		} else {
			setContentDescription(getResources().getString(R.string.accessibility_play));
			setImageResource(R.drawable.btn_playback_play);
		}
	}
}