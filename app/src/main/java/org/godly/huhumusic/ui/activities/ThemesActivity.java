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

package org.godly.huhumusic.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import org.godly.huhumusic.R;
import org.godly.huhumusic.ui.fragments.ThemeFragment;
import org.godly.huhumusic.utils.ThemeUtils;

/**
 * A class the displays the {@link ThemeFragment}.
 *

 */
public class ThemesActivity extends ActivityBase {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
		Toolbar toolbar = findViewById(R.id.activity_base_toolbar);
		// Initialize the theme resources
		ThemeUtils mResources = new ThemeUtils(this);
		// Set the overflow style
		mResources.setOverflowStyle(this);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			mResources.themeActionBar(getSupportActionBar(), R.string.settings_theme_chooser_title);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		// Transact the theme fragment
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.activity_base_content, ThemeFragment.class, null).commit();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMetaChanged() {
		// not used
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onRefresh() {
		// not used
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() {
		// not used
	}
}