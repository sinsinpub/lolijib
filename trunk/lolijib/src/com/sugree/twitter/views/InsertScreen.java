package com.sugree.twitter.views;

import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.Log;

import com.sugree.twitter.TwitterController;
import com.sugree.utils.DateUtil;

// use fully qualified classname, make sure it use native GUI, and not Polish GUI
public class InsertScreen extends javax.microedition.lcdui.List implements CommandListener {
	private TwitterController controller;
	private Command insertCommand;
	private Command cancelCommand;

	private Vector custom;
	private Vector words;

	public InsertScreen(TwitterController controller) {
		super("Insert", Choice.IMPLICIT);
		this.controller = controller;

		custom = new Vector();
		words = new Vector();

		insertCommand = new Command("Insert", Command.ITEM, 1);
		addCommand(insertCommand);
		cancelCommand = new Command("Cancel", Command.CANCEL, 2);
		addCommand(cancelCommand);

		setCommandListener(this);
	}

	public void removeAll() {
		while (size() > 0) {
			delete(0);
		}
	}

	public void setCustom(String[] words) {
		custom.removeAllElements();
		for(int i=0; i<words.length; i++) {
			custom.addElement(words[i]);
		}
	}

	public void setWords(Vector words) {
		if (words != null) {
			this.words = words;
		}
		updateInsert();
	}

	private void updateInsert() {
		Enumeration wordEnum;

		removeAll();

		wordEnum = custom.elements();
		while(wordEnum.hasMoreElements()) {
			append((String)wordEnum.nextElement(), null);
		}
		wordEnum = words.elements();
		while(wordEnum.hasMoreElements()) {
			append((String)wordEnum.nextElement(), null);
		}
	}

	public void commandAction(Command cmd, Displayable display) {
		int index = getSelectedIndex();
		if ((cmd == insertCommand || cmd == List.SELECT_COMMAND) && index >= 0) {
			controller.insertUpdate(getString(index));
		} if (cmd == cancelCommand) {
			controller.insertUpdate("");
		}
	}
}
