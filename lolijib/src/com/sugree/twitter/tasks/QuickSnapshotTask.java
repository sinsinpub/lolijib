package com.sugree.twitter.tasks;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.TwitterApi;
import com.sugree.twitter.views.SnapshotScreen;
import com.sugree.twitter.TwitterController;
import com.sugree.twitter.TwitterException;

public class QuickSnapshotTask extends AbstractTask {
	private TwitterController controller;
	private SnapshotScreen snapshot;

	public QuickSnapshotTask(TwitterController controller, SnapshotScreen snapshot) {
		this.controller = controller;
		this.snapshot = snapshot;
	}

	public void doTask() {
		try {
//#ifdef polish.api.mmapi
			snapshot.start(false);
			snapshot.quickSnapshot();
//#endif
		} catch (Exception e) {
			controller.showError(e, TwitterController.SCREEN_UPDATE);
		}
		snapshot = null;
	}
}
