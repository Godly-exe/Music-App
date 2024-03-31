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

package org.godly.huhumusic.service;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.godly.huhumusic.BuildConfig;
import org.godly.huhumusic.Config;
import org.godly.huhumusic.R;
import org.godly.huhumusic.cache.ImageCache;
import org.godly.huhumusic.cache.ImageFetcher;
import org.godly.huhumusic.provider.RecentStore.RecentStoreColumns;
import org.godly.huhumusic.ui.widgets.RecentWidgetProvider;
import org.godly.huhumusic.utils.CursorFactory;

/**
 * This class is used to build the recently listened list for the
 * {@link RecentWidgetProvider}.
 *

 */
public class RecentWidgetService extends RemoteViewsService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetRemoteViewsFactory(getApplicationContext());
	}

	/**
	 * This is the factory that will provide data to the collection widget.
	 */
	private static class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
		/**
		 * Number of views (ImageView and TextView)
		 */
		private static final int VIEW_TYPE_COUNT = 1;

		/**
		 * max recent item number
		 */
		private static final int RECENT_LIMIT = 20;

		/**
		 * Image cache
		 */
		private ImageFetcher mFetcher;

		/**
		 * Cursor to use
		 */
		private Cursor mCursor;

		/**
		 * application context
		 */
		private Context mContext;

		/**
		 * Constructor of <code>WidgetRemoteViewsFactory</code>
		 *
		 * @param context The {@link Context} to use.
		 */
		public WidgetRemoteViewsFactory(Context context) {
			// Initialize the image cache
			mFetcher = ImageFetcher.getInstance(context);
			mFetcher.setImageCache(ImageCache.getInstance(context));
			this.mContext = context.getApplicationContext();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getCount() {
			// Check for errors
			if (mCursor == null || mCursor.isClosed() || mCursor.getCount() <= 0) {
				return 0;
			}
			return Math.min(RECENT_LIMIT, mCursor.getCount());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public RemoteViews getViewAt(int position) {
			mCursor.moveToPosition(position);
			// Create the remote views
			RemoteViews mViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.app_widget_recents_items);
			// Copy the album id
			long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ID));
			// Copy the album name
			String albumName = mCursor.getString(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ALBUMNAME));
			// Copy the artist name
			String artist = mCursor.getString(mCursor.getColumnIndexOrThrow(RecentStoreColumns.ARTISTNAME));
			// Set the album names
			mViews.setTextViewText(R.id.app_widget_recents_line_one, albumName);
			// Set the artist names
			mViews.setTextViewText(R.id.app_widget_recents_line_two, artist);
			// Set the album art
			Bitmap bitmap = mFetcher.getCachedArtwork(albumName, artist, id);
			if (bitmap != null) {
				mViews.setImageViewBitmap(R.id.app_widget_recents_base_image, bitmap);
			} else {
				mViews.setImageViewResource(R.id.app_widget_recents_base_image, R.drawable.default_artwork);
			}
			// Open the profile of the touched album
			Intent profileIntent = new Intent();
			profileIntent.putExtra(Config.ID, id);
			profileIntent.putExtra(Config.NAME, albumName);
			profileIntent.putExtra(Config.ARTIST_NAME, artist);
			profileIntent.putExtra(RecentWidgetProvider.SET_ACTION, RecentWidgetProvider.OPEN_PROFILE);
			mViews.setOnClickFillInIntent(R.id.app_widget_recents_items, profileIntent);
			// Play the album when the artwork is touched
			Intent playAlbumIntent = new Intent();
			playAlbumIntent.putExtra(Config.ID, id);
			playAlbumIntent.putExtra(RecentWidgetProvider.SET_ACTION, RecentWidgetProvider.PLAY_ALBUM);
			mViews.setOnClickFillInIntent(R.id.app_widget_recents_base_image, playAlbumIntent);
			return mViews;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasStableIds() {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onDataSetChanged() {
			if (mCursor != null && !mCursor.isClosed()) {
				mCursor.close();
			}
			mCursor = CursorFactory.makeRecentCursor(mContext);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onDestroy() {
			closeCursor();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public RemoteViews getLoadingView() {
			// Nothing to do
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onCreate() {
			// Nothing to do
		}

		private void closeCursor() {
			if (mCursor != null && !mCursor.isClosed()) {
				mCursor.close();
				mCursor = null;
			}
		}
	}
}