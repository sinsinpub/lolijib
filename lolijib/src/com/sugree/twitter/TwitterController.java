package com.sugree.twitter;

import java.util.Vector;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Choice;
//#if project.HyperlinkList
import javax.microedition.lcdui.List;
//#endif
import javax.microedition.rms.RecordStoreException;
import javax.microedition.io.ConnectionNotFoundException;

import org.json.me.JSONException;

import com.substanceofcode.twitter.Settings;
import com.substanceofcode.twitter.TwitterApi;
import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.HttpUtil;
import com.substanceofcode.utils.StringUtil;
import com.substanceofcode.utils.Log;
import com.sugree.utils.DateUtil;

import com.sugree.twitter.tasks.RequestTimelineTask;
import com.sugree.twitter.tasks.RequestObjectTask;
import com.sugree.twitter.tasks.UpdateStatusTask;
import com.sugree.twitter.tasks.QuickSnapshotTask;
import com.sugree.twitter.views.SearchScreen;
import com.sugree.twitter.views.TimelineScreen;
import com.sugree.twitter.views.SetupScreen;
import com.sugree.twitter.views.StatusScreen;
import com.sugree.twitter.views.UpdateStatusScreen;
import com.sugree.twitter.views.InsertScreen;
//#ifdef polish.api.mmapi
import com.sugree.twitter.views.SnapshotScreen;
//#endif
import com.sugree.twitter.views.WaitScreen;
import com.sugree.twitter.views.AlertScreen;
import com.sugree.twitter.views.LogScreen;
import com.sugree.twitter.views.LinkScreen;

public class TwitterController {
	public static final int SCREEN_CURRENT = 0;
	public static final int SCREEN_TIMELINE = 1;
	public static final int SCREEN_UPDATE = 2;
	public static final int SCREEN_STATUS = 3;

	public static final int START_EMPTY_TIMELINE = 0;
	public static final int START_UPDATE = 1;
	public static final int START_FRIENDS_TIMELINE = 2;
	public static final int START_REPLIES_TIMELINE = 3;

	private TwitterMIDlet midlet;
	private TwitterApi api;
	private Display display;
	private Settings settings;
	private byte[] snapshot;
	private long replyTo;

	private TimelineScreen timeline;
	private UpdateStatusScreen update;
	private StatusScreen status;
	private InsertScreen insert;
	private SearchScreen search;

	private int currentFeedType;
	private long serverTimeOffset;

	// or should we let the user personalized the list as well ?
	private final String[] squeezeFrom = {
		"http://www.",
		"  ",
		". ",
		" .",
		", ",
		" ,",
		"; ",
		" ;",
		": ",
		" :",
		" !",
		" ?",
		" (",
		"( ",
		") ",
		" )",
		"- ",
		" -",
		"+ ",
		" +",
		" going to ",
		" want to ",
		" at ",
		" and ",
		" one ",
		" to ",
		" two ",
		" three ",
		" four ",
		" for ",
		" with ",
		" without ",
		" are you ",
		" see you ",
		" you ",
		" are ",
		" have ",
		" before ",
		" thanks ",
		" thank you ",
		" message ",
		" tonight ",
		" love ",
		" retweeting ",
		" retweet ",
		" download ",
		" upload ",
		" bangkok ",
		" Bangkok ",
		" people ",
	};

	private final String[] squeezeTo = {
		"www.",		
		" ",
		".",
		".",
		",",
		",",
		";",
		";",
		":",
		":",
		"!",
		"?",
		"(",
		"(",
		")",
		")",
		"-",
		"-",
		"+",
		"+",
		" gonna ",
		" wanna ",
		"@",
		"&",
		" 1 ",
		"2 ",
		" 2 ",
		" 3 ",
		" 4 ",
		"4 ",
		" w ",
		" w/o ",
		" ru ",
		" cu ",
		" u ",
		" r ",
		" hv ",
		" b4 ",
		" thx ",
		" thx ",
		" msg ",
		" 2nite ",
		" luv ",
		" rt ",
		" rt ",
		" d/l ",
		" u/l ",
		" BK ",
		" BK ",
		" ppl ",
	};


	public TwitterController(TwitterMIDlet midlet) {
		try {
			this.midlet = midlet;

			HttpUtil.setUserAgent(
				TwitterMIDlet.NAME + "/" + TwitterMIDlet.VERSION +
				" (" + System.getProperty("microedition.platform") + ")" +
				" Profile/" +
				System.getProperty("microedition.profiles") +
				" Configuration/" +
				System.getProperty("microedition.configuration"));
			//HttpUtil.setUserAgent("Profile/" + System.getProperty("microedition.profiles") + " Configuration/" + System.getProperty("microedition.configuration"));
			display = Display.getDisplay(midlet);
			api = new TwitterApi(TwitterMIDlet.NAME);
			settings = Settings.getInstance(midlet);

			timeline = new TimelineScreen(this);
			update = new UpdateStatusScreen(this, "");
			insert = new InsertScreen(this);
			status = new StatusScreen(this);
			search = new SearchScreen(this, "");
			loadSettings();

			currentFeedType = -1;
		} catch (JSONException e) {
			Log.error(e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.error(e.toString());
			e.printStackTrace();
		} catch (RecordStoreException e) {
			Log.error(e.toString());
			e.printStackTrace();
		} catch (Error e) {
			Log.error(e.toString());
		}
	}

	public void loadSettings() {
		api.setUsername(settings.getStringProperty(Settings.USERNAME, ""));
		api.setPassword(settings.getStringProperty(Settings.PASSWORD, ""));
		api.setGateway(settings.getStringProperty(Settings.GATEWAY, TwitterApi.DEFAULT_GATEWAY));
		api.setPictureGateway(settings.getStringProperty(Settings.PICTURE_GATEWAY, TwitterApi.DEFAULT_PICTURE_GATEWAY));
		api.setOptimizeBandwidth(settings.getBooleanProperty(Settings.OPTIMIZE_BANDWIDTH, true));
		api.setAlternateAuthentication(settings.getBooleanProperty(Settings.ALTERNATE_AUTHEN, false));
		api.setForceNoHost(settings.getBooleanProperty(Settings.FORCE_NO_HOST, false));

		insert.setCustom(StringUtil.split(settings.getStringProperty(Settings.CUSTOM_WORDS, "#jibjib,@having"), ","));
		insert.setWords(null);
		timeline.setLength(settings.getIntProperty(Settings.TIMELINE_LENGTH, 20));
		timeline.setShowCounter(settings.getBooleanProperty(Settings.ENABLE_REFRESH_COUNTER, false));
		timeline.setRefreshInterval(settings.getIntProperty(Settings.REFRESH_INTERVAL, 120));
		timeline.setRefresh(settings.getBooleanProperty(Settings.ENABLE_REFRESH, true));
		timeline.setAutoUpdate(settings.getBooleanProperty(Settings.ENABLE_AUTO_UPDATE, false));
//#if polish.midp2 || polish.midp3
		if (settings.getBooleanProperty(Settings.WRAP_TIMELINE, false)) {
			timeline.setFitPolicy(Choice.TEXT_WRAP_ON);
		} else {
			timeline.setFitPolicy(Choice.TEXT_WRAP_OFF);
		}
//#endif
		update.setMaxSize(getStatusMaxLength());
		status.setTimeOffset(settings.getStringProperty(Settings.TIME_OFFSET, "0000"));
		processHack();
	}

	public Settings getSettings() {
		return settings;
	}

//	private boolean isStatusLengthMax() {
//		return settings.getBooleanProperty(Settings.STATUS_LENGTH_MAX, false);
//	}

	public int getStatusMaxLength() {
		// statusMaxLength : 160 (hard limit, non-standard)
		//                 : 140 (per spec, default)
		return settings.getBooleanProperty(Settings.STATUS_LENGTH_MAX, false) ? 160 : 140;
	}

	public void processHack() {
		String[] hacks = StringUtil.split(settings.getStringProperty(Settings.HACK, "").toLowerCase(), " ");
		for(int i=0; i<hacks.length; i++) {
			if (hacks[i].equals("alterauth")) {
				api.setAlternateAuthentication(true);
			} else if (hacks[i].equals("noalterauth")) {
				api.setAlternateAuthentication(false);
			}
		}
	}

	public void minimize() {
		try {
			display.setCurrent(null);
			midlet.pauseApp();
		} catch (Exception e) {
			Log.error("Minimize: "+e.toString());
			showTimeline();
		}
	}

	public void openUrl(String url) {
//#if polish.midp2 || polish.midp3
		try {
			midlet.platformRequest(url);
		} catch (ConnectionNotFoundException e) {
			Log.error(e.toString());
		}
//#endif
	}

	public void exit() {
		try {
			midlet.destroyApp(true);
		} catch (Exception e) {
			Log.error("Exit: "+e.toString());
		}
	}

	private Vector extractWords(Vector statuses) {
		Vector words = new Vector();

		for(int i=0; i<statuses.size(); i++) {
			Status status = (Status)statuses.elementAt(i);
			if (!words.contains("@"+status.getScreenName())) {
				words.addElement("@"+status.getScreenName());
			}
			String[] splited = StringUtil.split(status.getText(), " ");
			for(int j=0; j<splited.length; j++) {
				if (splited[j].length() > 1 &&
					(splited[j].charAt(0) == '@' || splited[j].charAt(0) == '#')) {
					if (!words.contains(splited[j])) {
						words.addElement(splited[j]);
					}
				}
			}
		}
		return words;
	}

	public String squeezeText(String text) {
		text = text.trim();
		for (int i = 0; i < squeezeFrom.length; i++) {
			text = StringUtil.replace(text, squeezeFrom[i], squeezeTo[i]);
		}
		return text;
	}

	public void setSnapshot(byte[] raw) {
		snapshot = raw;
	}

	public byte[] getSnapshot() {
		return snapshot;
	}

	public void setReplyTo(long id) {
		replyTo = id;
	}

	public void setServerTimeOffset(long offset) {
		serverTimeOffset = offset;
	}

	public long getServerTimeOffset() {
		return serverTimeOffset;
	}

	public int getCurrentFeedType() {
		return currentFeedType;
	}

	public void updateTimeline(Status status) {
		timeline.update(status);
	}

	public void addTimeline(Vector statuses, boolean alert) {
		timeline.addTimeline(statuses);
		Vector words = extractWords(timeline.getTimeline());
		insert.setWords(words);
		if (statuses.size() > 0 && alert) {
			if (settings.getBooleanProperty(Settings.ENABLE_REFRESH_ALERT, true)) {
				AlertType.ALARM.playSound(display);
			}
			if (settings.getBooleanProperty(Settings.ENABLE_REFRESH_VIBRATE, true)) {
//#if polish.midp2 || polish.midp3
				display.vibrate(1000);
//#endif
			}
		}
	}

	public String getLastStatus() {
		return timeline.getLastDate();
	}

	public String getLastId() {
		return timeline.getLastId();
	}

	public String getUserName() {
		return timeline.getCurrentUser();
	}

	public void refresh() {
		display.setCurrent(display.getCurrent());
	}

	public void setCurrent(Displayable display) {
		this.display.setCurrent(display);
	}

	public void setCurrent(int id) {
		this.display.setCurrent(getScreen(id));
	}

	public String getSnapshotMimeType() {
		String encoding = settings.getStringProperty(Settings.SNAPSHOT_ENCODING, "").toLowerCase();
		String mimeType = "image/jpeg";
		if (encoding.indexOf("jpeg") >= 0 || encoding.indexOf("jpeg") >= 0) {
			mimeType = "image/jpeg";
		} else if (encoding.indexOf("png") >= 0) {
			mimeType = "image/png";
		} else if (encoding.indexOf("gif") >= 0) {
			mimeType = "image/gif";
		}
		return mimeType;
	}

	public void toggleFavorited(Status status) {
		int objectType = 0;

		if (status.getFavorited()) {
			objectType = RequestObjectTask.FAVORITE_DESTROY;
		} else {
			objectType = RequestObjectTask.FAVORITE_CREATE;
		}
		RequestObjectTask task = new RequestObjectTask(this, api, objectType, String.valueOf(status.getId()));
		WaitScreen wait = new WaitScreen(this, task, SCREEN_STATUS);
		wait.println("updating...");
		wait.start();
		display.setCurrent(wait);
	}

	public void updateStatus() {
		String text = settings.getStringProperty(Settings.AUTO_UPDATE_TEXT, "%H:%M");
		text = DateUtil.strftime(text, null);
//#ifdef polish.api.mmapi
		SnapshotScreen snapshot = null;
//#endif
		if (settings.getBooleanProperty(Settings.ENABLE_AUTO_UPDATE_PICTURE, false)) {
			try {
//#ifdef polish.api.mmapi
				snapshot = new SnapshotScreen(this, "");
//#endif
			} catch (Exception e) {
			}
		}
////#ifdef polish.api.mmapi
		UpdateStatusTask task = new UpdateStatusTask(this, api, text, replyTo, snapshot);
////#else
//		UpdateStatusTask task = new UpdateStatusTask(this, api, text, replyTo, null);
////#endif
		WaitScreen wait = new WaitScreen(this, task, SCREEN_TIMELINE);
		wait.println("updating...");
		wait.start();
		display.setCurrent(wait);
	}

	public void updateStatus(String text) {
		String mimeType = getSnapshotMimeType();
		String suffix = settings.getStringProperty(Settings.SUFFIX_TEXT, "");

		UpdateStatusTask task = new UpdateStatusTask(this, api, text+suffix, replyTo, snapshot, mimeType);
		WaitScreen wait = new WaitScreen(this, task, SCREEN_UPDATE);
		wait.println("updating...");
		wait.start();
		display.setCurrent(wait);
	}

	public void fetchTimeline(int feedType) {
		fetchTimeline(feedType, false);
	}

	public void fetchTimeline(int feedType, boolean nonBlock) {
		if (feedType == -1) {
			feedType = RequestTimelineTask.FEED_FRIENDS;
		}
		RequestTimelineTask task = new RequestTimelineTask(this, api, feedType, nonBlock);
		WaitScreen wait = new WaitScreen(this, task, SCREEN_TIMELINE);
		wait.println("fetching...");
		display.setCurrent(wait);
		if (feedType != currentFeedType) {
			timeline.clearTimeline();
		}
		currentFeedType = feedType;
		wait.start();
	}

	public void fetchTest() {
		RequestObjectTask task = new RequestObjectTask(this, api, RequestObjectTask.TEST, "");
		WaitScreen wait = new WaitScreen(this, task, SCREEN_TIMELINE);
		wait.println("fetching...");
		wait.start();
		display.setCurrent(wait);
	}

	public void fetchScheduleDowntime() {
		RequestObjectTask task = new RequestObjectTask(this, api, RequestObjectTask.SCHEDULE_DOWNTIME, "");
		WaitScreen wait = new WaitScreen(this, task, SCREEN_TIMELINE);
		wait.println("fetching...");
		wait.start();
		display.setCurrent(wait);
	}

	public void searchText(String keyword) {
		// TODO not implemented
	}

	public void showStart() {
		int id = settings.getIntProperty(Settings.START_SCREEN, 0);
		switch (id) {
			case START_EMPTY_TIMELINE:
				showTimeline();
				break;
			case START_UPDATE:
				showUpdate();
				break;
			case START_FRIENDS_TIMELINE:
				timeline.setTitleName("Friends");
				fetchTimeline(RequestTimelineTask.FEED_FRIENDS);
				break;
			case START_REPLIES_TIMELINE:
				timeline.setTitleName("@Replies");
				fetchTimeline(RequestTimelineTask.FEED_REPLIES);
				break;
		}
	}

	public void showTimeline() {
		display.setCurrent(timeline);
		snapshot = null;
	}

	public void showSetup() {
		SetupScreen setup = new SetupScreen(this);
		display.setCurrent(setup);
	}

	public void showLog() {
		LogScreen log = new LogScreen(this);
		display.setCurrent(log);
	}

	public void showStatus(Status status) {
		this.status.setStatus(status);
		display.setCurrent(this.status);
	}

	public void showPrevStatus() {
		int index = timeline.getSelectedIndex();
		if (index > 0) {
			timeline.setSelectedIndex(index, false);
			index -= 1;
			timeline.setSelectedIndex(index, true);
			Status status = (Status) timeline.getTimeline().elementAt(index);
			showStatus(status);
		}
	}

	public void showNextStatus() {
		int index = timeline.getSelectedIndex();
		if (index < timeline.size() - 1) {
			timeline.setSelectedIndex(index, false);
			index += 1;
			timeline.setSelectedIndex(index, true);
			Status status = (Status) timeline.getTimeline().elementAt(index);
			showStatus(status);
		}
	}

	public void showUpdate() {
		showUpdate("");
	}

	public void showSearch(String keyword) {
		display.setCurrent(this.search);
	}

	public void showUpdate(String text) {
		setReplyTo(0);
		update.setString(text);
		display.setCurrent(update);
	}

	public void showInsert() {
		display.setCurrent(insert);
	}

	public void showList(List list) {
		display.setCurrent(list);
	}

	public void insertUpdate(String text) {
		update.insert(text);
		display.setCurrent(update);
	}

//#ifdef polish.api.mmapi
	public void showSnapshot() {
		try {
			SnapshotScreen snapshot = new SnapshotScreen(this, update.getString());
			snapshot.start(true);
			display.setCurrent(snapshot);
		} catch (Exception e) {
			showError(e, SCREEN_UPDATE);
		}
	}

	public void quickSnapshot() {
		try {
			SnapshotScreen snapshot = new SnapshotScreen(this, update.getString());
			QuickSnapshotTask task = new QuickSnapshotTask(this, snapshot);
			WaitScreen wait = new WaitScreen(this, task, SCREEN_UPDATE);
			wait.println("taking snapshot...");
			wait.start();
			display.setCurrent(wait);
		} catch (Exception e) {
			showError(e, SCREEN_UPDATE);
		}
	}
//#endif

//#if polish.midp2 || polish.midp3
	public void insertLocation(int mode) {
		try {
			com.sugree.utils.Location loc = new com.sugree.utils.Location(settings);
			update.insert(loc.refresh(api, mode));
			display.setCurrent(update);
		} catch (Exception e) {
			showError(e, SCREEN_UPDATE);
		}
	}
//#endif

	public void showAbout() {
		String text = TwitterMIDlet.NAME+" "+TwitterMIDlet.VERSION+
			" by @sinsinpub based on jibjib by @sugree and LoliTwitter by @pruet "+
			"and Twim by @tlaukkanen.\n\n";
		text += "Platform: "+System.getProperty("microedition.platform")+"\n";
		text += "CLDC: "+System.getProperty("microedition.configuration")+"\n";
		text += "MIDP: "+System.getProperty("microedition.profiles")+"\n";
		text += "LAPI: "+System.getProperty("microedition.location.version")+"\n";
		text += "AMMS: "+System.getProperty("microedition.amms.version")+"\n";
		text += "MMAPI: "+System.getProperty("microedition.media.version")+"\n";
		text += "mixing: "+System.getProperty("supports.mixing")+"\n";
		text += "audio.capture: "+System.getProperty("supports.audio.capture")+"\n";
		text += "video.capture: "+System.getProperty("supports.video.capture")+"\n";
		text += "recording: "+System.getProperty("supports.recording")+"\n";
		text += "audio.encodings: "+System.getProperty("audio.encodings")+"\n";
		text += "video.encodings: "+System.getProperty("video.encodings")+"\n";
		text += "video.snapshot.encodings: "+System.getProperty("video.snapshot.encodings")+"\n";
		text += "streamable.contents: "+System.getProperty("streamable.contents")+"\n";
		String[] properties = {
			"com.sonyericsson.net.mcc",
			"com.sonyericsson.net.mnc",
			"com.sonyericsson.net.cmcc",
			"com.sonyericsson.net.cmnc",
			"com.sonyericsson.net.isonhomeplmn",
			"com.sonyericsson.net.rat",
			"com.sonyericsson.net.cellid",
			"com.sonyericsson.net.lac",
			"com.sonyericsson.net.status",
			"Cell-ID",
			"com.nokia.mid.cellid",
			"CellID",
			"LocAreaCode",
			"IMSI",
		};
		for(int i=0; i<properties.length; i++) {
			text += properties[i]+": "+System.getProperty(properties[i])+"\n";
		}
		showAlert("About", text);
	}

	public void showLink() {
		LinkScreen link = new LinkScreen(this);
		display.setCurrent(link);
	}

	public void showAlert(String title, String text) {
		AlertScreen about = new AlertScreen(this, title, text, timeline);
		display.setCurrent(about);
	}

	public void showStat(String text) {
		int maxChars = getStatusMaxLength();
		int lenChars = text.length();
		int lenBytes = lenChars;		
		try {
			lenBytes = new String(text.getBytes("UTF-8"), "ISO-8859-1").length();
		} catch (UnsupportedEncodingException e) {
		}

		String statText = lenChars + " characters\n" +
			maxChars + " max; " + (maxChars-lenChars) + " left\n" +
			lenBytes + " bytes";
		javax.microedition.lcdui.Alert stat = new javax.microedition.lcdui.Alert("Statistics", statText, null, AlertType.INFO);
		stat.setTimeout(javax.microedition.lcdui.Alert.FOREVER);
		display.setCurrent(stat);
	}

	public void showError(Error e) {
		showError("Error", e.toString(), AlertType.ERROR, SCREEN_CURRENT);
	}

	public void showError(Exception e) {
		showError("Exception", e.getMessage(), AlertType.ERROR, SCREEN_CURRENT);
	}

	public void showError(Exception e, int nextDisplay) {
		showError("Exception", e.getMessage(), AlertType.ERROR, nextDisplay);
	}

	public void showError(Exception e, int nextDisplay, boolean nonblock) {
		if (nonblock) {
			showError("Exception", e.getMessage(), null, nextDisplay);
		} else {
			showError("Exception", e.getMessage(), AlertType.ERROR, nextDisplay);
		}
	}

	public void showError(String title, String text, AlertType type, int nextDisplay) {
		Displayable screen = getScreen(nextDisplay);
		Log.error(text);
		if (type == null) {
			display.setCurrent(screen);
		} else {
			AlertScreen alert = new AlertScreen(this, title, text, screen);
			display.setCurrent(alert);
		}
	}

	public Displayable getScreen(int id) {
		Displayable screen = display.getCurrent();
		switch (id) {
			case SCREEN_CURRENT:
				break;
			case SCREEN_TIMELINE:
				screen = timeline;
				break;
			case SCREEN_UPDATE:
				screen = update;
				break;
			case SCREEN_STATUS:
				screen = status;
				break;
		}
		return screen;
	}
}
