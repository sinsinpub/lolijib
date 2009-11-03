package com.sugree.twitter.views;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Canvas;

import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.Log;
import com.substanceofcode.twitter.Settings;

import com.sugree.twitter.tasks.RequestTimelineTask;
import com.sugree.twitter.TwitterController;
import com.sugree.utils.DateUtil;

public class TimelineScreen extends List implements CommandListener {
	private final int TIMER_INTERVAL = 10;

	private TwitterController controller;
	private Command readCommand;
	private Command replyCommand;
	private Command directCommand;
	private Command updateCommand;
	private Command refreshCommand;
	private Command deleteCommand;
	private Command friendsTimelineCommand;
	private Command publicTimelineCommand;
	private Command userTimelineCommand;
	private Command myTimelineCommand;
	private Command repliesTimelineCommand;
	private Command directTimelineCommand;
	private Command favoritesTimelineCommand;
	private Command searchCommand;
	private Command setupCommand;
	private Command logCommand;
	private Command aboutCommand;
	private Command minimizeCommand;
	private Command exitCommand;

	private int refreshInterval;
	private int timeLeft;
	private Timer refreshTimer;
	private TimerTask refreshTask;
	private boolean autoUpdate;

	private Vector statuses;
	private long selectedStatus;
	private int length;
	private String title;
	private String currentUser;
	private boolean showCounter;

	public TimelineScreen(TwitterController controller) {
		super("jibjib", Choice.IMPLICIT);
		this.controller = controller;

		Settings settings = controller.getSettings();
		boolean swapMinimizeRefresh = settings.getBooleanProperty(Settings.SWAP_MINIMIZE_REFRESH, false);

		selectedStatus = 0;
		statuses = new Vector();
		title = getTitle();
		showCounter = false;

		refreshInterval = 0;

		readCommand = new Command("Read", Command.ITEM, 1);
//#if polish.midp2 || polish.midp3
		setSelectCommand(readCommand);
//#else
		addCommand(readCommand);
//#endif
		replyCommand = new Command("Reply", Command.ITEM, 2);
		addCommand(replyCommand);
		directCommand = new Command("Direct Message", Command.ITEM, 2);

		updateCommand = new Command("Tweet", Command.SCREEN, 6);
		addCommand(updateCommand);
		if (swapMinimizeRefresh) {
			refreshCommand = new Command("Refresh", Command.EXIT, 20);
		} else {
			refreshCommand = new Command("Refresh", Command.SCREEN, 7);
		}
		addCommand(refreshCommand);
		friendsTimelineCommand = new Command("Friends", Command.SCREEN, 8);
		addCommand(friendsTimelineCommand);
		repliesTimelineCommand = new Command("@Mentions", Command.SCREEN, 9);
		addCommand(repliesTimelineCommand);
		directTimelineCommand = new Command("d Messages", Command.SCREEN, 10);
		addCommand(directTimelineCommand);
		favoritesTimelineCommand = new Command("Favorites", Command.SCREEN, 11);
		addCommand(favoritesTimelineCommand);
		myTimelineCommand = new Command("Me", Command.SCREEN, 12);
		addCommand(myTimelineCommand);
		userTimelineCommand = new Command("Archive", Command.ITEM, 13);
		addCommand(userTimelineCommand);
		publicTimelineCommand = new Command("Everyone", Command.SCREEN, 14);
		addCommand(publicTimelineCommand);
		searchCommand = new Command("Search", Command.SCREEN, 15);
		addCommand(searchCommand);
		deleteCommand = new Command("Delete", Command.ITEM, 15);

		setupCommand = new Command("Setup", Command.SCREEN, 16);
		addCommand(setupCommand);
		logCommand = new Command("Log", Command.SCREEN, 17);
		addCommand(logCommand);
		aboutCommand = new Command("About", Command.SCREEN, 18);
		addCommand(aboutCommand);
		exitCommand = new Command("Exit", Command.SCREEN, 19);
		addCommand(exitCommand);
		if (!swapMinimizeRefresh) {
			minimizeCommand = new Command("Minimize", Command.EXIT, 20);
			addCommand(minimizeCommand);
		}

		setCommandListener(this);
	}

	private void saveSelected() {
		int index = getSelectedIndex();

		if (index >= 0 && index < statuses.size()) {
			selectedStatus = ((Status)statuses.elementAt(index)).getId();
		}
	}

	private void restoreSelected() {
		int lastIndex = findStatus(selectedStatus);
		if (lastIndex < 0) {
			lastIndex = statuses.size()-1;
		}
		if (lastIndex >= 0 && lastIndex < statuses.size()) {
			setSelectedIndex(lastIndex, true);
		}
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setShowCounter(boolean showCounter) {
		this.showCounter = showCounter;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.timeLeft = this.refreshInterval = refreshInterval;
	}

	public void setRefresh(boolean enable) {
		if (refreshTimer != null) {
			refreshTimer.cancel();
		}
		if (enable) {
			refreshTimer = new Timer();
			refreshTask = new RefreshTask();
			refreshTimer.scheduleAtFixedRate(refreshTask, TIMER_INTERVAL*1000, TIMER_INTERVAL*1000);
		} else {
			refreshTask = null;
			refreshTimer = null;
			setTitle(title);
		}
	}

	public void setAutoUpdate(boolean enable) {
		autoUpdate = enable;
	}

	public void clearTimeline() {
		statuses.removeAllElements();
		removeAll();
	}

	public void addTimeline(Vector timeline) {
		saveSelected();
		for(int i=0; i<timeline.size(); i++) {
			if (findStatus(((Status)timeline.elementAt(i)).getId()) < 0) {
				if (i < statuses.size()) {
					statuses.insertElementAt(timeline.elementAt(i), i);
				} else {
					statuses.addElement(timeline.elementAt(i));
				}
			}
		}
		if (statuses.size() > length) {
			statuses.setSize(length);
			statuses.trimToSize();
		}
		updateTimeline();
		restoreSelected();
	}

	public void update(Status status) {
		int index = findStatus(status.getId());
		if (index >= 0) {
			statuses.setElementAt(status, index);
			saveSelected();
			updateTimeline();
			restoreSelected();
		}
	}

	public String getLastDate() {
		if (statuses != null && statuses.size() > 0) {
			Status status = (Status)statuses.elementAt(0);
			return DateUtil.formatHTTPDate(status.getDate());
		}
		return "";
	}

	public String getLastId() {
		if (statuses != null && statuses.size() > 0) {
			Status status = (Status)statuses.elementAt(0);
			return String.valueOf(status.getId());
		}
		return "";
	}

	public Vector getTimeline() {
		return statuses;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	private int findStatus(long status) {
		for(int i=0; i<statuses.size(); i++) {
			if (((Status)statuses.elementAt(i)).getId() == status) {
				return i;
			}
		}
		return -1;
	}

	public void removeAll() {
		while (size() > 0) {
			delete(0);
		}
	}

	private void updateTimeline() {
		removeAll();

		Enumeration statusEnum = statuses.elements();
		while(statusEnum.hasMoreElements()) {
			Status status = (Status)statusEnum.nextElement();
			append(status.getScreenName()+": "+status.getText(), null);
		}
		timeLeft = refreshInterval;
	}

	public void commandAction(Command cmd, Displayable display) {
		int index = getSelectedIndex();
		if (cmd == readCommand && index >= 0) {
			Status status = (Status)statuses.elementAt(index);
			controller.showStatus(status);
		} if (cmd == replyCommand && index >= 0) {
			Status status = (Status)statuses.elementAt(index);
			controller.showUpdate("@"+status.getScreenName()+" ");
			controller.setReplyTo(status.getId());
		} if (cmd == directCommand && index >= 0) {
			Status status = (Status)statuses.elementAt(index);
			controller.showUpdate("d "+status.getScreenName()+" ");
		} else if (cmd == updateCommand) {
			controller.showUpdate();
		} else if (cmd == refreshCommand) {
			controller.fetchTimeline(controller.getCurrentFeedType());
		} else if (cmd == friendsTimelineCommand) {
			setTitleName("Friends");
			currentUser = null;
			addCommand(userTimelineCommand);
			removeCommand(directCommand);
			addCommand(replyCommand);
			controller.fetchTimeline(RequestTimelineTask.FEED_FRIENDS);
		} else if (cmd == publicTimelineCommand) {
			setTitleName("Everyone");
			currentUser = null;
			addCommand(userTimelineCommand);
			removeCommand(directCommand);
			addCommand(replyCommand);
			controller.fetchTimeline(RequestTimelineTask.FEED_PUBLIC);
		} else if (cmd == myTimelineCommand) {
			Settings settings = controller.getSettings();
			String username = settings.getStringProperty(Settings.USERNAME, "Me");
			setTitleName(username);
			currentUser = username;
			removeCommand(userTimelineCommand);
			removeCommand(directCommand);
			removeCommand(replyCommand);
			addCommand(deleteCommand);
			controller.fetchTimeline(RequestTimelineTask.FEED_USER);
		} else if (cmd == userTimelineCommand && index >= 0) {
			Status status = (Status)statuses.elementAt(index);
			String username = status.getScreenName();
			setTitleName(username);
			currentUser = username;
			removeCommand(userTimelineCommand);
			addCommand(directCommand);
			addCommand(replyCommand);
			controller.fetchTimeline(RequestTimelineTask.FEED_USER);
		} else if (cmd == repliesTimelineCommand) {
			setTitleName("@Mentions");
			currentUser = null;
			addCommand(userTimelineCommand);
			removeCommand(directCommand);
			addCommand(replyCommand);
			controller.fetchTimeline(RequestTimelineTask.FEED_REPLIES);
		} else if (cmd == directTimelineCommand) {
			setTitleName("Direct");
			currentUser = null;
			addCommand(userTimelineCommand);
			addCommand(directCommand);
			removeCommand(replyCommand);
			controller.fetchTimeline(RequestTimelineTask.FEED_DIRECT);
		} else if (cmd == favoritesTimelineCommand) {
			setTitleName("Favorites");
			currentUser = null;
			addCommand(userTimelineCommand);
			addCommand(directCommand);
			addCommand(replyCommand);
			controller.fetchTimeline(RequestTimelineTask.FEED_FAVORITES);
		} else if (cmd == searchCommand) {
			controller.showSearch("");
		} else if (cmd == setupCommand) {
			controller.showSetup();
		} else if (cmd == logCommand) {
			controller.showLog();
		} else if (cmd == aboutCommand) {
			controller.showAbout();
		} else if (cmd == minimizeCommand) {
			controller.minimize();
		} else if (cmd == exitCommand) {
			controller.exit();
		}
	}

	public void setTitleName(String s) {
		setTitle(s);
		title = s;
	}

	// only implement this method when using the J2ME Polish GUI:
	//#if polish.usePolishGui
	public void keyPressed(int code) {
		int index = getSelectedIndex();
		switch (code) {
		case Canvas.KEY_NUM1: // search (not implemented yet)
			break;
		case Canvas.KEY_NUM2: // tweet, as Gmail mobile (compose)
			controller.showUpdate();
			break;
		case Canvas.KEY_NUM3: // read
			if (index >= 0) {
				Status status = (Status)statuses.elementAt(index);
				controller.showStatus(status);
			}
			break;
		case Canvas.KEY_NUM4: // reply
			if (index >= 0) {
				Status status = (Status)statuses.elementAt(index);
				controller.showUpdate("@"+status.getScreenName()+" ");
				controller.setReplyTo(status.getId());
			}
			break;
		case Canvas.KEY_NUM5: // retweet
			if (index >= 0) {
				Status status = (Status)statuses.elementAt(index);
				controller.showUpdate(status.getRetweet(controller.getStatusMaxLength()));
			}
			break;
		case Canvas.KEY_NUM6: // nothing
			break;
		case Canvas.KEY_NUM7: // delete, as Gmail mobile (not implemented yet)
			break;
		case Canvas.KEY_NUM8: // nothing
			break;
		case Canvas.KEY_NUM9: // nothing
			break;
		case Canvas.KEY_NUM0: // refresh
			controller.fetchTimeline(controller.getCurrentFeedType());
			break;
		case Canvas.KEY_STAR: // favorite, as Gmail mobile
			if (index >= 0) {
				Status status = (Status)statuses.elementAt(index);				
				controller.toggleFavorited(status);
			}
			break;
		default:
			//# super.keyPressed(code);
			break;			
		}
	}
	//#endif

	private class RefreshTask extends TimerTask {
		public final void run() {
			if (refreshInterval > 0 &&
					controller.getScreen(controller.SCREEN_CURRENT) == controller.getScreen(controller.SCREEN_TIMELINE)) {
				timeLeft -= TIMER_INTERVAL;
				if (timeLeft <= 0) {
					timeLeft = refreshInterval;
					if (autoUpdate) {
						controller.updateStatus();
					} else {
						controller.fetchTimeline(controller.getCurrentFeedType(), true);
					}
				} else if (showCounter && timeLeft % 10 == 0) {
					setTitle(title+" "+timeLeft);
				}
			}
		}
	}
}
