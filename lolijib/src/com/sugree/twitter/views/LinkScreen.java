package com.sugree.twitter.views;

import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import com.substanceofcode.utils.Log;

import com.sugree.twitter.TwitterController;

// use fully qualified classname, make sure it use native GUI, and not Polish GUI
public class LinkScreen extends javax.microedition.lcdui.List implements CommandListener {
	private TwitterController controller;

	private static final String[] linkLabels = {
		"Official Home",
		"Project",
		"Stable JAD",
		"Stable JAR",
		"Beta JAD",
		"Beta JAR",
		"Mobile Twitter",
	};

	private final String[] linkValues = {
		"http://jibjib.org/",
		"http://code.google.com/p/jibjib/",
//#ifdef polish.name:defined
//#=	"http://jibjib.googlecode.com/svn/d/jibjib-${ polish.name }.jad",
//#=	"http://jibjib.googlecode.com/svn/d/jibjib-${ polish.name }.jar",
//#=	"http://jibjib.googlecode.com/svn/d/jibjib-beta-${ polish.vendor }_${ polish.name }.jad",
//#=	"http://jibjib.googlecode.com/svn/d/jibjib-beta-${ polish.vendor }_${ polish.name }.jar",
//#else
		"http://jibjib.googlecode.com/svn/d/jibjib.jad",
		"http://jibjib.googlecode.com/svn/d/jibjib.jar",
		"http://jibjib.googlecode.com/svn/d/jibjib-beta.jad",
		"http://jibjib.googlecode.com/svn/d/jibjib-beta.jar",
//#endif
		"http://m.twitter.com/",
	};

	private Command goCommand;
	private Command cancelCommand;

	public LinkScreen(TwitterController controller) {
		super("Links", Choice.IMPLICIT, linkLabels, null);
		this.controller = controller;

		goCommand = new Command("Go", Command.ITEM, 1);
		addCommand(goCommand);
		cancelCommand = new Command("Cancel", Command.CANCEL, 2);
		addCommand(cancelCommand);

		setCommandListener(this);
	}

	public void commandAction(Command cmd, Displayable display) {
		int index = getSelectedIndex();
		if ((cmd == goCommand || cmd == List.SELECT_COMMAND) && index >= 0) {
			controller.openUrl(linkValues[index]);
			if (index > 1) {
				controller.exit();
			} else {
				controller.showTimeline();
			}
		} if (cmd == cancelCommand) {
			controller.showTimeline();
		}
	}
}
