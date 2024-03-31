package org.godly.huhumusic.ui.adapters.viewpager;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.godly.huhumusic.ui.fragments.QueueFragment;

/**
 * @author nuclearfog
 */
public class QueueAdapter extends FragmentStatePagerAdapter {

	/**
	 *
	 */
	public QueueAdapter(FragmentManager fm) {
		super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
	}


	@NonNull
	@Override
	public Fragment getItem(int position) {
		return new QueueFragment();
	}


	@Override
	public int getCount() {
		return 1;
	}
}