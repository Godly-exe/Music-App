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

package org.godly.huhumusic.model;


/**
 * A class that represents a song.
 *

 */
public class Song extends Music implements Comparable<Song> {

	/**
	 * The song artist
	 */
	private String mArtistName = "";

	/**
	 * The song album
	 */
	private String mAlbumName = "";

	/**
	 * The song duration in seconds
	 */
	private int mDuration = -1;

	/**
	 * playlist position of the track
	 */
	private int playlistPos = -1;

	/**
	 * @param playlistPos playlist position of the track
	 */
	public Song(long songId, String songName, String artistName, String albumName, long length, int playlistPos) {
		this(songId, songName, artistName, albumName, length);
		this.playlistPos = playlistPos;
	}

	/**
	 *
	 */
	public Song(long songId, String songName, String artistName, String albumName, long length) {
		this(songId, songName, artistName, albumName, length, true);
	}

	/**
	 * Constructor of <code>Song</code>
	 *
	 * @param songId     The Id of the song
	 * @param songName   The name of the song
	 * @param artistName The song artist
	 * @param albumName  The song album
	 * @param length     The duration of a song in milliseconds
	 * @param visibility Visibility of the track
	 */
	public Song(long songId, String songName, String artistName, String albumName, long length, boolean visibility) {
		super(songId, songName, visibility);
		if (artistName != null) {
			mArtistName = artistName;
		}
		if (albumName != null) {
			mAlbumName = albumName;
		}
		if (length > 0) {
			mDuration = (int) length / 1000;
		}
	}

    /**
	 * @inheritDoc
	 */
	@Override
	public int compareTo(Song song) {
		return song.getName().compareToIgnoreCase(getName());
	}

	/**
	 * get artist of this song
	 *
	 * @return artist name
	 */
	public String getArtist() {
		return mArtistName;
	}

	/**
	 * album name of the track
	 *
	 * @return album name
	 */
	public String getAlbum() {
		return mAlbumName;
	}

	/**
	 * track duration in seconds
	 *
	 * @return duration in seconds
	 */
	public int duration() {
		return mDuration;
	}

	/**
	 * track duration in milliseconds
	 *
	 * @return duration in milliseconds
	 */
	public long durationMillis() {
		if (mDuration > 0)
			return (long) mDuration * 1000;
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + mAlbumName.hashCode();
		result = prime * result + mArtistName.hashCode();
		result = prime * result + mDuration;
		result = prime * result + Long.hashCode(getId());
		result = prime * result + playlistPos;
		result = prime * result + getName().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Song) {
			Song other = (Song) obj;
			return mAlbumName.equals(other.mAlbumName) && mArtistName.equals(other.mArtistName) &&
					getName().equals(other.getName()) && mDuration == other.mDuration && getId() == other.getId() && other.playlistPos == playlistPos;
		}
		return false;
	}
}