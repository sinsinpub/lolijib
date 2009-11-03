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
public class UpdateStatusScreen extends javax.microedition.lcdui.TextBox implements CommandListener {
	private TwitterController controller;

	private Command sendCommand;
	private Command cancelCommand;
	private Command insertCommand;
//#ifdef polish.api.mmapi
	private Command snapshotCommand;
	private Command quickSnapshotCommand;
//#endif
//#ifdef polish.api.locationapi
	private Command gpsCommand;
//#endif
	private Command reverseGeocoderCommand;
	private Command cellIdCommand;
	private Command statCommand;
	private Command squeezeCommand;
	
	public UpdateStatusScreen(TwitterController controller, String text) {
		super("What are you doing?", text, controller.getStatusMaxLength(), TextField.ANY);
		this.controller = controller;

		Settings settings = controller.getSettings();

		sendCommand = new Command("Send", Command.OK, 1);
		addCommand(sendCommand);
		cancelCommand = new Command("Cancel", Command.CANCEL, 2);
		addCommand(cancelCommand);
		insertCommand = new Command("Insert@#", Command.SCREEN, 3);
		addCommand(insertCommand);
//#ifdef polish.api.mmapi
		snapshotCommand = new Command("Picture", Command.SCREEN, 4);
		addCommand(snapshotCommand);
		quickSnapshotCommand = new Command("Quick Picture", Command.SCREEN, 5);
		addCommand(quickSnapshotCommand);
//#endif
//#ifdef polish.api.locationapi
		if (System.getProperty("microedition.location.version") != null && settings.getBooleanProperty(Settings.ENABLE_GPS, true)) {
			gpsCommand = new Command("GPS Location", Command.SCREEN, 6);
			addCommand(gpsCommand);
		}
//#endif
		if (settings.getBooleanProperty(Settings.ENABLE_REVERSE_GEOCODER, true)) {
			reverseGeocoderCommand = new Command("Rev Geo Loc", Command.SCREEN, 7);
			addCommand(reverseGeocoderCommand);
		}
		if (settings.getBooleanProperty(Settings.ENABLE_CELL_ID, true)) {
			cellIdCommand = new Command("Cell ID Loc", Command.SCREEN, 8);
			addCommand(cellIdCommand);
		}
		statCommand = new Command("Statistics", Command.SCREEN, 9);
		addCommand(statCommand);
		if (settings.getBooleanProperty(Settings.ENABLE_SQUEEZE, true)) {
			squeezeCommand = new Command("Squeeze", Command.SCREEN, 10);
			addCommand(squeezeCommand);
		}

		setCommandListener(this);
	}

	public void insert(String text) {
		insert(text, getCaretPosition());
	}

	public void commandAction(Command cmd, Displayable display) {
		if (cmd == sendCommand) {
			controller.updateStatus(getString());
		} else if (cmd == cancelCommand) {
			controller.showTimeline();
		} else if (cmd == insertCommand) {
			controller.showInsert();
//#ifdef polish.api.mmapi
		} else if (cmd == snapshotCommand) {
			controller.showSnapshot();
		} else if (cmd == quickSnapshotCommand) {
			controller.quickSnapshot();
//#endif
//#ifdef polish.api.locationapi
		} else if (cmd == gpsCommand) {
			controller.insertLocation(Location.MODE_GPS);
//#endif
//#if polish.midp2 || polish.midp3
		} else if (cmd == reverseGeocoderCommand) {
			controller.insertLocation(Location.MODE_REVERSE_GEOCODER);
		} else if (cmd == cellIdCommand) {
			controller.insertLocation(Location.MODE_CELL_ID);
//#endif
		} else if (cmd == statCommand) {
			controller.showStat(getString());
		} else if (cmd == squeezeCommand) {
			setString(controller.squeezeText(getString()));
		}
	}
}
