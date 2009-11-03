package com.sugree.twitter.views;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import com.sugree.twitter.TwitterController;
import com.sugree.utils.Location;
import com.substanceofcode.twitter.Settings;

// use fully qualified classname, make sure it use native GUI, and not Polish GUI
public class SearchScreen extends javax.microedition.lcdui.TextBox implements CommandListener {
	private TwitterController controller;

	private Command sendCommand;
	private Command cancelCommand;
	private Command insertCommand;
	
	public SearchScreen(TwitterController controller, String text) {
		super("What to search?", text, controller.getStatusMaxLength(), TextField.ANY);
		this.controller = controller;

		Settings settings = controller.getSettings();

		sendCommand = new Command("Search", Command.OK, 1);
		addCommand(sendCommand);
		cancelCommand = new Command("Cancel", Command.CANCEL, 2);
		addCommand(cancelCommand);
		insertCommand = new Command("Insert@#", Command.SCREEN, 3);
		addCommand(insertCommand);

		setCommandListener(this);
	}

	public void insert(String text) {
		insert(text, getCaretPosition());
	}

	public void commandAction(Command cmd, Displayable display) {
		if (cmd == sendCommand) {
			controller.searchText(getString());
		} else if (cmd == cancelCommand) {
			controller.showTimeline();
		} else if (cmd == insertCommand) {
			controller.showInsert();
		}
	}
}
