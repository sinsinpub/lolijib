package com.sugree.twitter.views;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
//#if polish.midp2 || polish.midp3
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
//#if project.HyperlinkList
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.List;
//#endif
//#endif
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Canvas;

//#if polish.midp2 || polish.midp3
import com.substanceofcode.utils.StringUtil;
//#if project.HyperlinkList
import com.substanceofcode.utils.HttpUtil;
//#endif
//#endif
import com.substanceofcode.utils.TimeUtil;
import com.substanceofcode.utils.Log;
import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.twitter.Settings;
import com.sugree.twitter.TwitterController;

////#if polish.midp2 || polish.midp3
public class StatusScreen extends Form implements CommandListener, ItemCommandListener {
////#else
//public class StatusScreen extends Form implements CommandListener {
////#endif
	private TwitterController controller;

	private Status status;
	private int timeOffset;

//#if !(polish.midp2 || polish.midp3) || project.HyperlinkList
	private StringItem textField;
//#endif
	private Command replyCommand;
	private Command directCommand;
	private Command retweetCommand;
	private Command favoriteCommand;
	private Command backCommand;
	private Command nextCommand;
	private Command prevCommand;
	private Command profileCommand;
//#if polish.midp2 || polish.midp3
	private Command linkCommand;
//#if project.HyperlinkList
	private Command linkOpenCommand;
	private Command linkBackCommand;
	private List linkList;
//#endif
//#endif

	public StatusScreen(TwitterController controller) {
		super("");
		this.controller = controller;

//#if !(polish.midp2 || polish.midp3) || project.HyperlinkList
		textField = new StringItem("", "");
		append(textField);
//#endif

		backCommand = new Command("Back", Command.BACK, 1);
		addCommand(backCommand);
		prevCommand = new Command("Next", Command.SCREEN, 2);
		addCommand(prevCommand);
		replyCommand = new Command("Reply", Command.SCREEN, 3);
		addCommand(replyCommand);
		retweetCommand = new Command("Retweet", Command.SCREEN, 4);
		addCommand(retweetCommand);
		favoriteCommand = new Command("Toggle Favorite", Command.SCREEN, 5);
		addCommand(favoriteCommand);
		directCommand = new Command("Direct Message", Command.SCREEN, 7);
		addCommand(directCommand);
		nextCommand = new Command("Previous", Command.SCREEN, 9);
		addCommand(nextCommand);
		profileCommand = new Command("Profile", Command.SCREEN, 8);
		addCommand(profileCommand);

//#if polish.midp2 || polish.midp3
		linkCommand = new Command("Link", Command.SCREEN, 6);
//#if project.HyperlinkList
		linkOpenCommand = new Command("Open", Command.ITEM, 1);
		linkBackCommand = new Command("Cancel", Command.BACK, 2);
		linkCommand = new Command("Link", Command.SCREEN, 6);
		linkList = new List("Links", Choice.IMPLICIT);
		linkList.setSelectCommand(linkOpenCommand);
		linkList.addCommand(linkBackCommand);
		linkList.setCommandListener(this);
//#endif
//#endif

		setCommandListener(this);
	}

	public void setTimeOffset(String offset) {
		try {
			int i = Integer.parseInt(offset);
			timeOffset = (i/100*3600+i%100*60)*1000;
		} catch (NumberFormatException e) {
			Log.error(e.toString());
		}
	}

	public void setStatus(Status status) {
		this.status = status;
		setTitle(status.getScreenName() + "(" + status.getUser().getName() + ")");

		String favorited;
		if (status.getFavorited()) {
			favorited = " *";
		} else {
			favorited = "";
		}

		String interval;
		Date now = new Date();
		now.setTime(now.getTime()-controller.getServerTimeOffset());
		Date date = new Date(status.getDate().getTime()+timeOffset);
		if (now.getTime() > date.getTime()) {
			interval = TimeUtil.getTimeInterval(date, now)+" ago";
		} else {
			interval = TimeUtil.getTimeInterval(now, date)+" in the future";
		}

		String replyTo = "";
		if (StringUtil.isNotEmpty(status.getReplyToScreenName()))
			replyTo = " in reply to " + status.getReplyToScreenName();

//#if polish.midp2 || polish.midp3
//#if project.HyperlinkList
		String text = status.getText()+"\n\nabout "+interval+" via "+status.getSource()+favorited+replyTo;
		textField.setText(text);

		removeCommand(linkCommand);
		linkList.deleteAll();

		String chunks[] = StringUtil.split(status.getText(), " ");
		String chunk;
		for(int i=0; i<chunks.length; i++) {
			chunk = chunks[i];
			if (chunk.startsWith("http://") ||
				chunk.startsWith("https://") ||
				chunk.startsWith("www.")) {
				if (!chunk.startsWith("http")) {
					chunk = "http://"+chunk;
				}
				linkList.append(chunk, null);
				handleTwitPic(chunk);
			}
		}
		if (linkList.size() > 0) {
			addCommand(linkCommand);
		}
//#else
//		deleteAll();
//
//		javax.microedition.lcdui.StringItem si = null;
//		String chunks[] = StringUtil.split(status.getText(), " ");
//		String chunk;
//		for(int i=0; i<chunks.length; i++) {
//			chunk = chunks[i];
//			if (chunk.startsWith("http://") ||
//				chunk.startsWith("https://") ||
//				chunk.startsWith("www.")) {
//				si = new javax.microedition.lcdui.StringItem("", chunk,
//					javax.microedition.lcdui.Item.HYPERLINK);
//				si.setDefaultCommand(linkCommand);
//				si.setItemCommandListener(this);
//				append(si);
//			} else {
//				if (si == null || si.getAppearanceMode() == javax.microedition.lcdui.Item.HYPERLINK) {
//					si = new javax.microedition.lcdui.StringItem("", chunk);
//					append(si);
//				} else {
//					si.setText(si.getText()+" "+chunk);
//				}
//			}
//		}
//		si = new javax.microedition.lcdui.StringItem("", "\n");
//		si.setLayout(javax.microedition.lcdui.Item.LAYOUT_NEWLINE_BEFORE);
//		append(si);
//		si = new javax.microedition.lcdui.StringItem("",
//			"about "+interval+" via "+status.getSource()+favorited);
//		si.setLayout(javax.microedition.lcdui.Item.LAYOUT_NEWLINE_BEFORE);
//		append(si);
//#endif
//#else
//		String text = status.getText()+"\n\nabout "+interval+" via "+status.getSource()+favorited;
//		textField.setText(text);
//#endif
	}

//#if polish.midp2 || polish.midp3
	public void commandAction(Command cmd, javax.microedition.lcdui.Item item) {
		String url = ((javax.microedition.lcdui.StringItem)item).getText();
		if (!url.startsWith("http")) {
			url = "http://"+url;
		}
		controller.openUrl(url);
		Log.info("open "+url);
	}
//#endif

	public void commandAction(Command cmd, Displayable display) {
		if (cmd == backCommand) {
			status = null;
			controller.showTimeline();
		} else if (cmd == replyCommand) {
			controller.showUpdate("@"+status.getScreenName()+" ");
			controller.setReplyTo(status.getId());
		} else if (cmd == directCommand) {
			controller.showUpdate("d "+status.getScreenName()+" ");
		} else if (cmd == retweetCommand) {
			controller.showUpdate(status.getRetweet(controller.getStatusMaxLength()));
		} else if (cmd == favoriteCommand) {
			controller.toggleFavorited(status);
		} else if (cmd == prevCommand) {
			controller.showPrevStatus();
		} else if (cmd == nextCommand) {
			controller.showNextStatus();
		} else if (cmd == profileCommand) {
			// TODO not implemented
//#if (polish.midp2 || polish.midp3) && project.HyperlinkList
		} else if (cmd == linkCommand) {
			controller.showList(linkList);
		} else if (cmd == linkOpenCommand) {
			String url = linkList.getString(linkList.getSelectedIndex());
			controller.showStatus(status);
			/*
			Log.info("open "+url);
			if (url.startsWith("http://twitpic.com/") && url.endsWith(".jpg")) {
				try {
					url = HttpUtil.getLocation(url, "");
				} catch (Exception e) {
					Log.error("TwitPic "+e.toString());
				}
			}
			*/
			Log.info("open "+url);
			controller.openUrl(url);
		} else if (cmd == linkBackCommand) {
			controller.showStatus(status);
//#endif
		}
	}

//#ifdef polish.usePolishGui
	public void keyPressed(int code) {
		switch (code) {
		case Canvas.KEY_NUM4: // reply
			controller.showUpdate("@"+status.getScreenName()+" ");
			controller.setReplyTo(status.getId());
			break;
		case Canvas.KEY_NUM6: // direct
			controller.showUpdate("d "+status.getScreenName()+" ");
			break;
		case Canvas.KEY_NUM5: // retweet
			controller.showUpdate(status.getRetweet(controller.getStatusMaxLength()));
			break;
		case Canvas.KEY_NUM7: // delete, as Gmail mobile
			break;
		case Canvas.KEY_STAR: // favorite, as Gmail mobile
			controller.toggleFavorited(status);
			break;
		case Canvas.LEFT: // previous status
			controller.showPrevStatus();
			break;
		case Canvas.RIGHT: // next status
			controller.showNextStatus();
			break;
		default:
			//# super.keyPressed(code);
			break;
		}
	}
//#endif

//#if (polish.midp2 || polish.midp3) && project.HyperlinkList
	public void handleTwitPic(String url) {
		if (url.startsWith("https://")) {
			url = url.substring(8);
		}
		if (url.startsWith("http://")) {
			url = url.substring(7);
		}
		if (url.startsWith("www.")) {
			url = url.substring(4);
		}
		int slash = url.indexOf('/');
		if (slash >= 0) {
			String host = url.substring(0, slash);
			String path = url.substring(slash+1, url.length());
			if (host.equals("twitpic.com") && path.indexOf('/') == -1) {
				linkList.append("http://twitpic.com/show/thumb/"+path+".jpg", null);
			}
		}
	}
//#endif
}
