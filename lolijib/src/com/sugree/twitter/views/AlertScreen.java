package com.sugree.twitter.views;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import com.substanceofcode.utils.TimeUtil;
import com.substanceofcode.utils.Log;
import com.substanceofcode.twitter.model.Status;
import com.sugree.twitter.TwitterController;

// use fully qualified classname, make sure it use native GUI, and not Polish GUI
public class AlertScreen extends javax.microedition.lcdui.Form implements CommandListener {
	private TwitterController controller;
	private Displayable nextDisplay;

	private javax.microedition.lcdui.StringItem textField;
	private Command backCommand;
	private Command testCommand;
	private Command scheduleDowntimeCommand;
	private Command linkCommand;

	public AlertScreen(TwitterController controller, String title, String text, Displayable nextDisplay) {
		super(title);
		this.controller = controller;
		this.nextDisplay = nextDisplay;

//#ifdef polish.cldc1.0
//#		if (title.toLowerCase().equals("exception")) {
//#else
		if (title.equalsIgnoreCase("exception")) {
//#endif
			text += "\n\n"+Log.getText();
		}
		textField = new javax.microedition.lcdui.StringItem("", text);
		append(textField);

		backCommand = new Command("OK", Command.OK, 1);
		addCommand(backCommand);
		testCommand = new Command("Test", Command.SCREEN, 2);
		addCommand(testCommand);
		scheduleDowntimeCommand = new Command("Schedule Downtime", Command.SCREEN, 3);
		addCommand(scheduleDowntimeCommand);
		linkCommand = new Command("Link", Command.SCREEN, 4);
		addCommand(linkCommand);

		setCommandListener(this);
	}

	public void commandAction(Command cmd, Displayable display) {
		if (cmd == backCommand) {
			controller.setCurrent(nextDisplay);
		} else if (cmd == testCommand) {
			controller.fetchTest();
		} else if (cmd == scheduleDowntimeCommand) {
			controller.fetchScheduleDowntime();
		} else if (cmd == linkCommand) {
			controller.showLink();
		}
	}
}
