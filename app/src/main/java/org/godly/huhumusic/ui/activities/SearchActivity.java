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

import android.app.SearchManager;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import org.godly.huhumusic.R;
import org.godly.huhumusic.loaders.MusicSearchLoader;
import org.godly.huhumusic.model.Album;
import org.godly.huhumusic.model.Artist;
import org.godly.huhumusic.model.Music;
import org.godly.huhumusic.model.Song;
import org.godly.huhumusic.ui.adapters.listview.SearchAdapter;
import org.godly.huhumusic.ui.adapters.listview.holder.RecycleHolder;
import org.godly.huhumusic.ui.dialogs.PlaylistCreateDialog;
import org.godly.huhumusic.utils.ApolloUtils;
import org.godly.huhumusic.utils.ContextMenuItems;
import org.godly.huhumusic.utils.MusicUtils;
import org.godly.huhumusic.utils.MusicUtils.ServiceToken;
import org.godly.huhumusic.utils.NavUtils;
import org.godly.huhumusic.utils.ThemeUtils;

import java.util.List;

/**
 * Provides the search interface for Apollo.
 *

 */
public class SearchActivity extends ActivityBase implements LoaderCallbacks<List<Music>>, OnScrollListener, OnQueryTextListener, OnItemClickListener {

	/**
	 * ID of the loader
	 */
	private static final int LOADER_ID = 0xF97E2FD6;

	/**
	 * Grid view column count. ONE - list, TWO - normal grid
	 */
	private static final int ONE = 1, TWO = 2;

	/**
	 * context menu group ID
	 */
	private static final int GROUP_ID = 0xC1A35EE4;

	/**
	 * The service token
	 */
	private ServiceToken mToken;

	/**
	 * The query
	 */
	private String mFilterString;

	/**
	 * List view adapter
	 */
	private SearchAdapter mAdapter;

	@Nullable
	private Music selection;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid_search);
		// init view
		GridView mGridView = findViewById(R.id.grid_search);
		TextView emptyText = findViewById(R.id.grid_search_empty_info);
		Toolbar toolbar = findViewById(R.id.grid_search_toolbar);
		// Initialize the theme resources
		ThemeUtils mResources = new ThemeUtils(this);
		// Set the overflow style
		mResources.setOverflowStyle(this);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			mResources.themeActionBar(getSupportActionBar(), R.string.app_name);
		}
		// Control the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Bind Apollo's service
		mToken = MusicUtils.bindToService(this, this);
		// Get the query
		String query = getIntent().getStringExtra(SearchManager.QUERY);
		mFilterString = !TextUtils.isEmpty(query) ? query : null;
		// Action bar subtitle
		mResources.setSubtitle("\"" + mFilterString + "\"");
		// Initialize the adapter
		mAdapter = new SearchAdapter(this);
		// Set the prefix
		mAdapter.setPrefix(mFilterString);
		// Bind the data
		mGridView.setAdapter(mAdapter);
		// Recycle the data
		mGridView.setRecyclerListener(new RecycleHolder());
		// set emty message
		mGridView.setEmptyView(emptyText);
		// Speed up scrolling
		mGridView.setOnScrollListener(this);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnCreateContextMenuListener(this);
		if (ApolloUtils.isLandscape(this)) {
			mGridView.setNumColumns(TWO);
		} else {
			mGridView.setNumColumns(ONE);
		}
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String query = intent.getStringExtra(SearchManager.QUERY);
		mFilterString = !TextUtils.isEmpty(query) ? query : null;
		// Set the prefix
		mAdapter.setPrefix(mFilterString);
		LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStart() {
		super.onStart();
		MusicUtils.notifyForegroundStateChanged(this, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStop() {
		super.onStop();
		MusicUtils.notifyForegroundStateChanged(this, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unbind from the service
		if (MusicUtils.isConnected()) {
			MusicUtils.unbindFromService(mToken);
			mToken = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			selection = mAdapter.getItem(info.position);
			if (selection instanceof Album) {
				menu.add(GROUP_ID, ContextMenuItems.MORE_BY_ARTIST, Menu.NONE, R.string.context_menu_more_by_artist);
			} else if (selection instanceof Song) {
				menu.add(GROUP_ID, ContextMenuItems.PLAY_NEXT, Menu.NONE, R.string.context_menu_play_next);
				menu.add(GROUP_ID, ContextMenuItems.MORE_BY_ARTIST, Menu.NONE, R.string.context_menu_more_by_artist);
				menu.add(GROUP_ID, ContextMenuItems.USE_AS_RINGTONE, Menu.NONE, R.string.context_menu_use_as_ringtone);
			}
			menu.add(GROUP_ID, ContextMenuItems.PLAY_SELECTION, Menu.NONE, R.string.context_menu_play_selection);
			menu.add(GROUP_ID, ContextMenuItems.ADD_TO_QUEUE, Menu.NONE, R.string.add_to_queue);
			menu.add(GROUP_ID, ContextMenuItems.DELETE, Menu.NONE, R.string.context_menu_delete);
			SubMenu subMenu = menu.addSubMenu(GROUP_ID, ContextMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
			MusicUtils.makePlaylistMenu(getApplicationContext(), GROUP_ID, subMenu, true);
		} else {
			selection = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onContextItemSelected(@NonNull MenuItem item) {
		if (item.getGroupId() == GROUP_ID && selection != null) {
			switch (item.getItemId()) {
				case ContextMenuItems.PLAY_SELECTION:
					if (selection instanceof Album) {
						long[] ids = MusicUtils.getSongListForAlbum(this, selection.getId());
						MusicUtils.playAll(getApplicationContext(), ids, 0, false);
					} else if (selection instanceof Artist) {
						long[] ids = MusicUtils.getSongListForArtist(this, selection.getId());
						MusicUtils.playAll(getApplicationContext(), ids, 0, false);
					} else if (selection instanceof Song) {
						long[] ids = new long[]{selection.getId()};
						MusicUtils.playAll(getApplicationContext(), ids, 0, false);
					}
					return true;

				case ContextMenuItems.ADD_TO_QUEUE:
					if (selection instanceof Album) {
						long[] ids = MusicUtils.getSongListForAlbum(this, selection.getId());
						MusicUtils.addToQueue(this, ids);
					} else if (selection instanceof Artist) {
						long[] ids = MusicUtils.getSongListForArtist(this, selection.getId());
						MusicUtils.addToQueue(this, ids);
					} else if (selection instanceof Song) {
						long[] ids = new long[]{selection.getId()};
						MusicUtils.addToQueue(this, ids);
					}
					return true;

				case ContextMenuItems.DELETE:
					String artist = selection.getName();
					if (selection instanceof Album) {
						long[] ids = MusicUtils.getSongListForAlbum(this, selection.getId());
						MusicUtils.openDeleteDialog(this, artist, ids);
					} else if (selection instanceof Artist) {
						long[] ids = MusicUtils.getSongListForArtist(this, selection.getId());
						MusicUtils.openDeleteDialog(this, artist, ids);
					} else if (selection instanceof Song) {
						long[] ids = new long[]{selection.getId()};
						MusicUtils.openDeleteDialog(this, artist, ids);
					}
					return true;

				case ContextMenuItems.MORE_BY_ARTIST:
					if (selection instanceof Album)
						NavUtils.openArtistProfile(this, ((Album) selection).getArtist());
					else if (selection instanceof Song)
						NavUtils.openArtistProfile(this, ((Song) selection).getArtist());
					return true;

				case ContextMenuItems.PLAY_NEXT:
					if (selection instanceof Song) {
						long[] ids = new long[]{selection.getId()};
						MusicUtils.playNext(ids);
					}
					return true;

				case ContextMenuItems.NEW_PLAYLIST:
					if (selection instanceof Album) {
						long[] albumSongIds = MusicUtils.getSongListForAlbum(getApplicationContext(), selection.getId());
						PlaylistCreateDialog.getInstance(albumSongIds).show(getSupportFragmentManager(), PlaylistCreateDialog.NAME);
					} else if (selection instanceof Artist) {
						long[] artistSongIds = MusicUtils.getSongListForArtist(getApplicationContext(), selection.getId());
						PlaylistCreateDialog.getInstance(artistSongIds).show(getSupportFragmentManager(), PlaylistCreateDialog.NAME);
					} else if (selection instanceof Song) {
						long[] ids = new long[]{selection.getId()};
						PlaylistCreateDialog.getInstance(ids).show(getSupportFragmentManager(), PlaylistCreateDialog.NAME);
					}
					return true;

				case ContextMenuItems.PLAYLIST_SELECTED:
					long mPlaylistId = item.getIntent().getLongExtra("playlist", -1L);
					if (mPlaylistId != -1L) {
						if (selection instanceof Album) {
							long[] albumSongIds = MusicUtils.getSongListForAlbum(getApplicationContext(), selection.getId());
							MusicUtils.addToPlaylist(this, albumSongIds, mPlaylistId);
						} else if (selection instanceof Artist) {
							long[] artistSongIds = MusicUtils.getSongListForArtist(getApplicationContext(), selection.getId());
							MusicUtils.addToPlaylist(this, artistSongIds, mPlaylistId);
						} else if (selection instanceof Song) {
							long[] ids = new long[]{selection.getId()};
							MusicUtils.addToPlaylist(this, ids, mPlaylistId);
						}
					}
					return true;

				case ContextMenuItems.USE_AS_RINGTONE:
					if (selection instanceof Song)
						MusicUtils.setRingtone(this, selection.getId());
					return true;
			}
		}
		return false;
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
	@NonNull
	@Override
	public Loader<List<Music>> onCreateLoader(int id, Bundle args) {
		return new MusicSearchLoader(this, mFilterString);
	}


	@Override
	public void onLoadFinished(@NonNull Loader<List<Music>> loader, List<Music> data) {
		// disable loader until user interaction
		LoaderManager.getInstance(this).destroyLoader(LOADER_ID);
		// set data
		mAdapter.clear();
		for (Music music : data) {
			mAdapter.add(music);
		}
	}


	@Override
	public void onLoaderReset(@NonNull androidx.loader.content.Loader<List<Music>> loader) {
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Pause disk cache access to ensure smoother scrolling
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING
				|| scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			mAdapter.setPauseDiskCache(true);
		} else {
			mAdapter.setPauseDiskCache(false);
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
		if (TextUtils.isEmpty(newText)) {
			return false;
		}
		// Called when the action bar search text has changed. Update
		// the search filter, and restart the loader to do a new query
		// with this filter.
		mFilterString = !TextUtils.isEmpty(newText) ? newText : null;
		// Set the prefix
		mAdapter.setPrefix(mFilterString);
		LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Music music = mAdapter.getItem(position);
		// If it's an artist, open the artist profile
		if (music instanceof Artist) {
			Artist artist = (Artist) music;
			NavUtils.openArtistProfile(this, artist.getName());
		}
		// If it's an album, open the album profile
		else if (music instanceof Album) {
			Album album = (Album) music;
			NavUtils.openAlbumProfile(this, album.getName(), album.getArtist(), album.getId());
		}
		// If it's a song, play it and leave
		else if (music instanceof Song) {
			Song song = (Song) music;
			long[] list = new long[]{song.getId()};
			MusicUtils.playAll(getApplicationContext(), list, 0, false);
		}
		// All done
		finish();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Nothing to do
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