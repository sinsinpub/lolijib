/*
 * UserEntry.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.substanceofcode.twitter.model;

import java.util.Date;
import com.substanceofcode.utils.StringUtil;

/**
 * UserEntry
 * 
 * @author sin_sin
 */
public class User {

	private long id;
	private String name;
	private String screenName;
	private String location;
	private String description;
	private String imageUrl;
	private String url;
	private boolean protect;
	private int statusCount;
	private int followerCount;
	private int friendCount;
	private boolean following;
	private boolean notification;

	public User(long id) {
		this.id = id;
	}

	public User(long id, String name, String screenName, String location,
			String description, String imageUrl, String url, boolean protect,
			int statusCount, int followerCount, int friendCount,
			boolean following, boolean notification) {
		this.id = id;
		this.name = name;
		this.screenName = screenName;
		this.location = location;
		this.description = description;
		this.imageUrl = imageUrl;
		this.url = url;
		this.protect = protect;
		this.statusCount = statusCount;
		this.followerCount = followerCount;
		this.friendCount = friendCount;
		this.following = following;
		this.notification = notification;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isProtect() {
		return protect;
	}

	public void setProtect(boolean protect) {
		this.protect = protect;
	}

	public int getStatusCount() {
		return statusCount;
	}

	public void setStatusCount(int statusCount) {
		this.statusCount = statusCount;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	public int getFriendCount() {
		return friendCount;
	}

	public void setFriendCount(int friendCount) {
		this.friendCount = friendCount;
	}

	public boolean isFollowing() {
		return following;
	}

	public void setFollowing(boolean following) {
		this.following = following;
	}

	public boolean isNotification() {
		return notification;
	}

	public void setNotification(boolean notification) {
		this.notification = notification;
	}

}
