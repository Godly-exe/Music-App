package org.godly.huhumusic.model;

import androidx.annotation.NonNull;

/**
 * represents a music folder
 *
 * @author nuclearfog
 */
public class Folder extends Music implements Comparable<Folder> {

	private String name = "";
	private String path = "";

	/**
	 * @param path (absolute folder path)
	 */
	public Folder(String path, boolean visible) {
		super(0L, "", visible);
		int end = path.lastIndexOf("/");
		if (end > 1) {
			this.path = path.substring(0, end);
			int start = path.lastIndexOf("/", end - 1);
			if (start >= 0) {
				name = path.substring(start + 1, end);
			} else {
				name = path.substring(end);
			}
		}
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public int compareTo(Folder folder) {
		if (folder.getName().equals(getName()))
			return folder.getPath().compareToIgnoreCase(getPath());
		return folder.getName().compareToIgnoreCase(getName());
	}

	/**
	 * get absolute path of this folder
	 *
	 * @return absolute path
	 */
	public String getPath() {
		return path;
	}


	@NonNull
	@Override
	public String toString() {
		return "path=\"" + getPath() + "\" file=\"" + getName() + "\" visible=" + isVisible();
	}
}