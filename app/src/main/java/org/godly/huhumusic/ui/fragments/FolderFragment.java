package org.godly.huhumusic.ui.fragments;

import static org.godly.huhumusic.Config.FOLDER;
import static org.godly.huhumusic.Config.ID;
import static org.godly.huhumusic.Config.MIME_TYPE;
import static org.godly.huhumusic.Config.NAME;
import static org.godly.huhumusic.ui.activities.ProfileActivity.PAGE_FOLDERS;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import org.godly.huhumusic.R;
import org.godly.huhumusic.loaders.FolderLoader;
import org.godly.huhumusic.model.Folder;
import org.godly.huhumusic.ui.activities.ProfileActivity;
import org.godly.huhumusic.ui.adapters.listview.FolderAdapter;
import org.godly.huhumusic.ui.adapters.listview.holder.RecycleHolder;
import org.godly.huhumusic.ui.fragments.phone.MusicBrowserPhoneFragment;
import org.godly.huhumusic.utils.ContextMenuItems;
import org.godly.huhumusic.utils.FragmentViewModel;
import org.godly.huhumusic.utils.MusicUtils;
import org.godly.huhumusic.utils.PreferenceUtils;

import java.util.List;

/**
 * decompiled from Apollo 1.6 APK
 */
public class FolderFragment extends Fragment implements LoaderCallbacks<List<Folder>>, OnItemClickListener, Observer<String> {

	/**
	 * context menu group ID
	 */
	private static final int GROUP_ID = 0x1E42C9C7;

	/**
	 * ID of the loader
	 */
	private static final int LOADER_ID = 0xE1E246AA;

	/**
	 * listview adapter for music folder view
	 */
	private FolderAdapter mAdapter;

	/**
	 * viewmodel used for communication with hosting activity
	 */
	private FragmentViewModel viewModel;

	/**
	 * app settings
	 */
	private PreferenceUtils preference;

	/**
	 * context menu selection
	 */
	@Nullable
	private Folder selectedFolder;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(@Nullable Bundle extras) {
		super.onCreate(extras);
		// init preferences
		preference = PreferenceUtils.getInstance(requireContext());
		mAdapter = new FolderAdapter(requireContext());
		viewModel = new ViewModelProvider(requireActivity()).get(FragmentViewModel.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle extras) {
		// Init views
		View mRootView = inflater.inflate(R.layout.list_base, container, false);
		ListView mList = mRootView.findViewById(R.id.list_base);
		TextView emptyholder = mRootView.findViewById(R.id.list_base_empty_info);
		// set listview
		mList.setAdapter(mAdapter);
		mList.setEmptyView(emptyholder);
		mList.setRecyclerListener(new RecycleHolder());
		mList.setOnCreateContextMenuListener(this);
		mList.setOnItemClickListener(this);
		return mRootView;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel.getSelectedItem().observe(getViewLifecycleOwner(), this);
		setHasOptionsMenu(true);
		LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		viewModel.getSelectedItem().removeObserver(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenuInfo info) {
		super.onCreateContextMenu(menu, v, info);
		if (info instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) info;
			selectedFolder = mAdapter.getItem(adapterContextMenuInfo.position);
			if (selectedFolder != null) {
				menu.add(GROUP_ID, ContextMenuItems.PLAY_FOLDER, Menu.NONE, R.string.context_menu_play_selection);
				menu.add(GROUP_ID, ContextMenuItems.ADD_FOLDER_QUEUE, Menu.NONE, R.string.add_to_queue);
				// hide artist from list
				if (selectedFolder.isVisible()) {
					menu.add(GROUP_ID, ContextMenuItems.HIDE_FOLDER, Menu.NONE, R.string.context_menu_hide_folder);
				} else {
					menu.add(GROUP_ID, ContextMenuItems.HIDE_FOLDER, Menu.NONE, R.string.context_menu_unhide_folder);
				}
			}
		} else {
			// remove selection
			selectedFolder = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onContextItemSelected(@NonNull MenuItem item) {
		if (item.getGroupId() == GROUP_ID && selectedFolder != null) {
			switch (item.getItemId()) {
				case ContextMenuItems.PLAY_FOLDER:
					long[] selectedFolderSongs = MusicUtils.getSongListForFolder(requireContext(), selectedFolder.getName());
					MusicUtils.playAll(requireContext(), selectedFolderSongs, 0, false);
					return true;

				case ContextMenuItems.ADD_FOLDER_QUEUE:
					selectedFolderSongs = MusicUtils.getSongListForFolder(requireContext(), selectedFolder.getName());
					MusicUtils.addToQueue(requireActivity(), selectedFolderSongs);
					return true;

				case ContextMenuItems.HIDE_FOLDER:
					MusicUtils.excludeFolder(requireContext(), selectedFolder);
					MusicUtils.refresh();
					return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Folder mFolder = mAdapter.getItem(position);
		Bundle bundle = new Bundle();
		bundle.putLong(ID, -1L);
		bundle.putString(NAME, mFolder.getName());
		bundle.putString(MIME_TYPE, PAGE_FOLDERS);
		bundle.putString(FOLDER, mFolder.getPath());
		Intent intent = new Intent(requireActivity(), ProfileActivity.class);
		intent.putExtras(bundle);
		requireActivity().startActivity(intent);
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull
	@Override
	public Loader<List<Folder>> onCreateLoader(int id, Bundle args) {
		return new FolderLoader(requireContext());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoadFinished(@NonNull Loader<List<Folder>> loader, @NonNull List<Folder> data) {
		// stop loader
		LoaderManager.getInstance(this).destroyLoader(LOADER_ID);
		// Clear list
		mAdapter.clear();
		// add data to the adapter
		for (Folder folder : data) {
			if (preference.showExcludedTracks() || folder.isVisible()) {
				mAdapter.add(folder);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoaderReset(@NonNull Loader<List<Folder>> loader) {
		// Clear the data in the adapter
		mAdapter.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onChanged(String action) {
		if (action.equals(MusicBrowserPhoneFragment.REFRESH)) {
			LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
		}
	}
}