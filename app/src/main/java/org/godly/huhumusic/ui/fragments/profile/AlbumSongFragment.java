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

package org.godly.huhumusic.ui.fragments.profile;


import static org.godly.huhumusic.ui.adapters.listview.ProfileSongAdapter.DISPLAY_ALBUM_SETTING;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import org.godly.huhumusic.Config;
import org.godly.huhumusic.R;
import org.godly.huhumusic.loaders.AlbumSongLoader;
import org.godly.huhumusic.model.Song;
import org.godly.huhumusic.provider.FavoritesStore;
import org.godly.huhumusic.ui.adapters.listview.ProfileSongAdapter;
import org.godly.huhumusic.ui.dialogs.PlaylistCreateDialog;
import org.godly.huhumusic.utils.ContextMenuItems;
import org.godly.huhumusic.utils.MusicUtils;

import java.util.List;

/**
 * This class is used to display all of the songs from a particular album.
 *

 */
public class AlbumSongFragment extends ProfileFragment implements LoaderCallbacks<List<Song>> {

	/**
	 * Used to keep context menu items from bleeding into other fragments
	 */
	private static final int GROUP_ID = 0x169012DB;

	/**
	 * LoaderCallbacks identifier
	 */
	private static final int LOADER_ID = 0x77D144AE;

	/**
	 * The adapter for the list
	 */
	private ProfileSongAdapter mAdapter;

	/**
	 * context menu selection
	 */
	@Nullable
	private Song mSong;


	@Override
	protected void init() {
		// Enable the options menu
		setHasOptionsMenu(true);
		// Start the loader
		// init adapter
		mAdapter = new ProfileSongAdapter(requireContext(), DISPLAY_ALBUM_SETTING, false);
		setAdapter(mAdapter);
		//
		LoaderManager.getInstance(this).initLoader(LOADER_ID, getArguments(), this);
	}


	@Override
	protected void onItemClick(View v, int pos, long id) {
		MusicUtils.playAllFromUserItemClick(requireContext(), mAdapter, pos);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (menuInfo instanceof AdapterContextMenuInfo) {
			// Get the position of the selected item
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			// Creat a new song
			mSong = mAdapter.getItem(info.position);
			// Play the song
			menu.add(GROUP_ID, ContextMenuItems.PLAY_SELECTION, Menu.NONE, R.string.context_menu_play_selection);
			// Play the song
			menu.add(GROUP_ID, ContextMenuItems.PLAY_NEXT, Menu.NONE, R.string.context_menu_play_next);
			// Add the song to the queue
			menu.add(GROUP_ID, ContextMenuItems.ADD_TO_QUEUE, Menu.NONE, R.string.add_to_queue);
			// Add the song to a playlist
			SubMenu subMenu = menu.addSubMenu(GROUP_ID, ContextMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
			MusicUtils.makePlaylistMenu(requireContext(), GROUP_ID, subMenu, true);
			// Make the song a ringtone
			menu.add(GROUP_ID, ContextMenuItems.USE_AS_RINGTONE, Menu.NONE, R.string.context_menu_use_as_ringtone);
			// Delete the song
			menu.add(GROUP_ID, ContextMenuItems.DELETE, Menu.NONE, R.string.context_menu_delete);
		} else {
			// remove selected track
			mSong = null;
		}
	}


	@Override
	public boolean onContextItemSelected(@NonNull MenuItem item) {
		if (item.getGroupId() == GROUP_ID && mSong != null) {
			long[] trackId = {mSong.getId()};

			switch (item.getItemId()) {
				case ContextMenuItems.PLAY_SELECTION:
					MusicUtils.playAll(requireContext(), trackId, 0, false);
					return true;

				case ContextMenuItems.PLAY_NEXT:
					MusicUtils.playNext(trackId);
					return true;

				case ContextMenuItems.ADD_TO_QUEUE:
					MusicUtils.addToQueue(requireActivity(), trackId);
					return true;

				case ContextMenuItems.ADD_TO_FAVORITES:
					FavoritesStore.getInstance(requireContext()).addSongId(mSong);
					return true;

				case ContextMenuItems.NEW_PLAYLIST:
					PlaylistCreateDialog.getInstance(trackId).show(getParentFragmentManager(), PlaylistCreateDialog.NAME);
					return true;

				case ContextMenuItems.PLAYLIST_SELECTED:
					long mPlaylistId = item.getIntent().getLongExtra("playlist", -1L);
					if (mPlaylistId != -1L) {
						MusicUtils.addToPlaylist(requireActivity(), trackId, mPlaylistId);
					}
					return true;

				case ContextMenuItems.USE_AS_RINGTONE:
					MusicUtils.setRingtone(requireActivity(), mSong.getId());
					return true;

				case ContextMenuItems.DELETE:
					MusicUtils.openDeleteDialog(requireActivity(), mSong.getName(), trackId);
					return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull
	@Override
	public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
		long albumId = args != null ? args.getLong(Config.ID) : -1L;
		return new AlbumSongLoader(requireContext(), albumId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoadFinished(@NonNull Loader<List<Song>> loader, @NonNull List<Song> data) {
		// disable loader
		LoaderManager.getInstance(this).destroyLoader(LOADER_ID);
		// Start fresh
		mAdapter.clear();
		// Add the data to the adpater
		for (Song song : data) {
			mAdapter.add(song);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoaderReset(@NonNull Loader<List<Song>> loader) {
		// Clear the data in the adapter
		mAdapter.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void moveToCurrent() {
		long trackId = MusicUtils.getCurrentAudioId();
		for (int pos = 0; pos < mAdapter.getCount(); pos++) {
			if (mAdapter.getItemId(pos) == trackId) {
				scrollTo(pos);
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void refresh() {
		LoaderManager.getInstance(this).initLoader(LOADER_ID, getArguments(), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drop(int from, int to) {
		// not used
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(int which) {
		// not used
	}
}