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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.res.ResourcesCompat;

import org.godly.huhumusic.R;

/**
 * In order to implement the theme chooser for Apollo, this class returns a
 * {@link Resources} object that can be used like normal. In other words, when
 * {@code getDrawable()} or {@code getColor()} is called, the object returned is
 * from the current theme package name and because all of the theme resource
 * identifiers are the same as all of Apollo's resources a little less code is
 * used to implement the theme chooser.
 *

 */
public class ThemeUtils {

	/**
	 * Current theme package name.
	 */
	public static final String PACKAGE_INDEX = "theme_index";
	/**
	 * Used to get and set the theme package name.
	 */
	private final SharedPreferences mPreferences;

	/**
	 * Custom action bar layout
	 */
	private final View mActionBarLayout;

	/**
	 * The theme resources.
	 */
	private Resources resources;

	/**
	 * Constructor for <code>ThemeUtils</code>
	 *
	 * @param context The {@link Context} to use.
	 */
	public ThemeUtils(Context context) {
		// Get the preferences
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		resources = context.getResources();
		// todo implement theme setup here
		// Inflate the custom layout
		mActionBarLayout = View.inflate(context, R.layout.action_bar, null);
	}

	/**
	 * Return the index of the selected theme
	 *
	 * @return selection index
	 */
	public final int getThemeSelectionIndex() {
		return mPreferences.getInt(PACKAGE_INDEX, 0);
	}

	/**
	 * Set the index of the theme selection
	 *
	 * @param position selection index
	 */
	public void setThemeSelectionIndex(int position) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(PACKAGE_INDEX, position);
		editor.apply();
	}

	/**
	 * Sets the corret overflow icon in the action bar depending on whether or
	 * not the current action bar color is dark or light.
	 *
	 * @param app The {@link Context} used to set the theme.
	 */
	public void setOverflowStyle(Context app) {
		app.setTheme(R.style.Apollo_Theme_Dark);
	}

	/**
	 * Sets the {@link MenuItem} icon for the favorites action.
	 *
	 * @param favorite The favorites action.
	 */
	public void setFavoriteIcon(MenuItem favorite) {
		Drawable favIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_favorite, null);
		if (favIcon != null) {
			if (MusicUtils.isFavorite()) {
				favIcon.mutate().setColorFilter(resources.getColor(R.color.favorite_selected), PorterDuff.Mode.SRC_IN);
			}
			favorite.setIcon(favIcon);
		}
	}

	/**
	 * Builds a custom layout and applies it to the action bar, then themes the
	 * background, title, and subtitle.
	 *
	 * @param actionBar The {@link ActionBar} to use.
	 * @param titleID   The title for the action bar
	 */
	public void themeActionBar(ActionBar actionBar, @StringRes int titleID) {
		String title = resources.getString(titleID);
		// Set the custom layout
		actionBar.setCustomView(mActionBarLayout);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		// Theme the action bar background
		int color = ResourcesCompat.getColor(resources, R.color.action_bar, null);
		Drawable background = new ColorDrawable(color);
		actionBar.setBackgroundDrawable(background);
		// Theme the title
		setTitle(title);
	}

	/**
	 * Themes the action bar subtitle
	 */
	public void setTitle(String title) {
		if (!TextUtils.isEmpty(title)) {
			// Get the title text view
			TextView actionBarTitle = mActionBarLayout.findViewById(R.id.action_bar_title);
			// Theme the title
			int textColor = ResourcesCompat.getColor(resources, R.color.action_bar_title, null);
			actionBarTitle.setTextColor(textColor);
			// Set the title
			actionBarTitle.setText(title);
		}
	}

	/**
	 * Themes the action bar subtitle
	 *
	 * @param subtitle The subtitle to use
	 */
	public void setSubtitle(String subtitle) {
		if (!TextUtils.isEmpty(subtitle)) {
			TextView actionBarSubtitle = mActionBarLayout.findViewById(R.id.action_bar_subtitle);
			actionBarSubtitle.setVisibility(View.VISIBLE);
			// Theme the subtitle
			int color = ResourcesCompat.getColor(resources, R.color.action_bar_subtitle, null);
			actionBarSubtitle.setTextColor(color);
			// Set the subtitle
			actionBarSubtitle.setText(subtitle);
		}
	}
}