package com.sugree.twitter.tasks;

import java.util.Vector;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.TwitterApi;
import com.sugree.twitter.TwitterController;
import com.sugree.twitter.TwitterException;

public class RequestTimelineTask extends AbstractTask {
	private TwitterController controller;
	private TwitterApi api;
	private int feedType;
	private boolean nonBlock;

	public final static int FEED_FRIENDS = 0;
	public final static int FEED_REPLIES = 1;
	public final static int FEED_USER = 2;
	public final static int FEED_PUBLIC = 3;
	public final static int FEED_DIRECT = 4;
	public final static int FEED_FAVORITES = 5;

	public RequestTimelineTask(TwitterController controller, TwitterApi api, int feedType, boolean nonBlock) {
		this.controller = controller;
		this.api = api;
		this.feedType = feedType;
		this.nonBlock = nonBlock;
	}

	public void doTask() {
		Vector timeline = new Vector();

		try {
			if (feedType == FEED_FRIENDS) {
				timeline = api.requestFriendsTimeline(controller.getLastId());
			} else if (feedType == FEED_REPLIES) {
				timeline = api.requestMentionsTimeline(controller.getLastId());
			} else if (feedType == FEED_USER) {
				timeline = api.requestUserTimeline(controller.getLastId(), controller.getUserName());
			} else if (feedType == FEED_PUBLIC) {
				timeline = api.requestPublicTimeline(controller.getLastId());
			} else if (feedType == FEED_DIRECT) {
				timeline = api.requestDirectMessagesTimeline(controller.getLastId());
			} else if (feedType == FEED_FAVORITES) {
				timeline = api.requestFavoritesTimeline();
			}
			controller.addTimeline(timeline, nonBlock);
			controller.showTimeline();
		} catch (TwitterException e) {
			e.printStackTrace();
			controller.showError(e, controller.SCREEN_TIMELINE, nonBlock);
		}
	}
}
