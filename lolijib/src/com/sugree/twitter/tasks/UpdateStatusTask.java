package com.sugree.twitter.tasks;

import java.util.Vector;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.TwitterApi;
import com.sugree.twitter.TwitterController;
import com.sugree.twitter.TwitterException;
import com.sugree.twitter.views.SnapshotScreen;

public class UpdateStatusTask extends AbstractTask {
	private TwitterController controller;
	private TwitterApi api;
	private String status;
	private long replyTo;
	private byte[] snapshot;
	private SnapshotScreen snapshotScreen;
	private String mimeType;
	private boolean nonBlock;

	public UpdateStatusTask(TwitterController controller, TwitterApi api, String status, long replyTo, byte[] snapshot, String mimeType) {
		this.controller = controller;
		this.api = api;
		this.status = status;
		this.replyTo = replyTo;
		this.snapshot = snapshot;
		this.mimeType = mimeType;
		this.nonBlock = false;
	}

	public UpdateStatusTask(TwitterController controller, TwitterApi api, String status, long replyTo, SnapshotScreen snapshotScreen) {
		this.controller = controller;
		this.api = api;
		this.status = status;
		this.replyTo = replyTo;
		this.snapshotScreen = snapshotScreen;
		this.nonBlock = true;
	}

	public void doTask() {
		try {
			if (snapshotScreen != null) {
				try {
//#ifdef polish.api.mmapi
					snapshotScreen.start(false);
					snapshotScreen.quickSnapshot(true);
//#endif
					snapshot = controller.getSnapshot();
					mimeType = controller.getSnapshotMimeType();
				} catch (Exception e) {
					snapshot = null;
				}
			}
			if (snapshot == null) {
				api.updateStatus(status, replyTo);
			} else {
				api.postPicture(status, snapshot, mimeType);
			}
			controller.showTimeline();
		} catch (TwitterException e) {
			if (nonBlock) {
				controller.showError(e, TwitterController.SCREEN_TIMELINE, nonBlock);
			} else {
				controller.showError(e, TwitterController.SCREEN_UPDATE, nonBlock);
			}
		}
		if (nonBlock) {
			controller.fetchTimeline(controller.getCurrentFeedType(), true);
		}
	}
}
