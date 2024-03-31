package org.godly.huhumusic.model;

import androidx.annotation.NonNull;

/**
 * item super class for music information
 */
public abstract class Music {

	/**
	 * ID of the element
	 */
	private long id;

	/**
	 * name of the element
	 */
	private String name;

	/**
	 * visibility of the item
	 */
	private boolean visible;

	/**
	 *
	 */
	protected Music(long id, String name, boolean visible) {
		if (name != null)
			this.name = name;
		else
			this.name = "";
		this.id = id;
		this.visible = visible;
	}

	/**
	 * get name of the item like artist name or track name
	 *
	 * @return name of the item
	 */
	public String getName() {
		return name;
	}

	/**
	 * get ID of the item like artist, album or song ID
	 *
	 * @return ID of the item
	 */
	public final long getId() {
		return id;
	}

	/**
	 * get visibility of the item
	 *
	 * @return true if item is visible
	 */
	public boolean isVisible() {
		return visible;
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + name + "\" visible=" + isVisible();
	}
}