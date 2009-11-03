package com.sugree.twitter;

import javax.microedition.midlet.MIDlet;

import com.substanceofcode.utils.HttpUtil;

public class TwitterMIDlet extends MIDlet {
	public static String NAME;
	public static String VERSION;
	public static String URL;

	private TwitterController controller;
	private boolean first;

	public TwitterMIDlet() {
		TwitterMIDlet.NAME = getAppProperty("MIDlet-Name");
		TwitterMIDlet.VERSION = getAppProperty("MIDlet-Version");
		TwitterMIDlet.URL = "http://jibjib.googlecode.com/files/jibjib-"+VERSION+".xml";
		first = true;
		try {
			controller = new TwitterController(this);
			HttpUtil.setTwitterController(controller);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startApp() {
		if (first) {
			controller.showStart();
			first = false;
		} else {
			controller.showTimeline();
		}
	}

	public void pauseApp() {
		notifyPaused();
	}

	public void destroyApp(boolean unconditional) {
		try {
		} catch (Exception ex) {
		}
		notifyDestroyed();
	}
}
