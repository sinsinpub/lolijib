package com.sugree.twitter.views;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.rms.RecordStoreException;

import org.json.me.JSONException;

import com.substanceofcode.infrastructure.Device;
import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.twitter.Settings;
import com.substanceofcode.utils.Log;
import com.sugree.twitter.TwitterController;

// use fully qualified classname, make sure it use native GUI, and not Polish GUI
public class SetupScreen extends javax.microedition.lcdui.Form implements CommandListener, javax.microedition.lcdui.ItemStateListener {
	private final String[] gatewaysLabel = {
		"Custom",
		"Twitter",
		"Birdnest appspot",
		"Birdnest onedd",
	};
	private final String[] gatewaysValue = {
		null,
		"http://twitter.com/",
		"http://nest.appspot.com/text/",
		"http://nest.onedd.net/text/",
	};

	private final String[] pictureGatewaysLabel = {
		"Custom",
		"TwitPic",
		"TwitGoo",
		"yfrog",
		"upic.me",
		"Birdnest onedd TwitPic",
		"Birdnest onedd TwitGoo",
		"Birdnest onedd yfrog",
		"Birdnest onedd upic.me",
	};
	private final String[] pictureGatewaysValue = {
		null,
		"http://twitpic.com/",
		"http://twitgoo.com/",
		"http://yfrog.com/",
		"http://upic.me/",
		"http://nest.onedd.net/text/twitpic/",
		"http://nest.onedd.net/text/twitgoo/",
		"http://nest.onedd.net/text/yfrog/",
		"http://nest.onedd.net/text/upicme/",
	};

	private final String[] startsLabel = {
		"Empty",
		"Tweet",
		"Friends",
		"@Replies",
	};
	private final int[] startsValue = {
		TwitterController.START_EMPTY_TIMELINE,
		TwitterController.START_UPDATE,
		TwitterController.START_FRIENDS_TIMELINE,
		TwitterController.START_REPLIES_TIMELINE,
	};

	private final String[] flagsLabel = {
		"Optimize bandwidth",
		"Alternate authentication",
		"Fullscreen picture",
		"160-characters tweet (non-standard)",
		"Resize thumbnail",
		"Wrap timeline",
		"Enable squeeze",
		"Enable GPS",
		"Enable reverse geocoder",
		"Enable cell ID",
		"Enable refresh",
		"Enable refresh alert",
		"Enable refresh vibrate",
		"Enable refresh counter",
		"Swap minimize and refresh",
		"Enable auto tweet",
		"Enable auto tweet picture",
		"Force no Host",
	};

//#ifdef polish.api.mmapi
	private final String[] captureDevicesLabel = {
		"Custom",
		"capture://video",
		"capture://image",
		"capture://devcam0",
		"capture://devcam1",
	};
	private final String[] captureDevicesValue = {
		null,
		"capture://video",
		"capture://image",
		"capture://devcam0",
		"capture://devcam1",
	};

	private String[] snapshotEncodingsLabel;
//#endif

	private TwitterController controller;

	private javax.microedition.lcdui.TextField usernameField;
	private javax.microedition.lcdui.TextField passwordField;
	private javax.microedition.lcdui.TextField timelineLengthField;
	private javax.microedition.lcdui.TextField suffixTextField;
	private javax.microedition.lcdui.TextField refreshIntervalField;
	private javax.microedition.lcdui.TextField autoUpdateTextField;
	private javax.microedition.lcdui.TextField gatewayField;
	private javax.microedition.lcdui.ChoiceGroup gatewaysField;
//#ifdef polish.api.mmapi
	private javax.microedition.lcdui.TextField pictureGatewayField;
	private javax.microedition.lcdui.ChoiceGroup pictureGatewaysField;
	private javax.microedition.lcdui.TextField captureDeviceField;
	private javax.microedition.lcdui.ChoiceGroup captureDevicesField;
	private javax.microedition.lcdui.TextField snapshotEncodingField;
	private javax.microedition.lcdui.ChoiceGroup snapshotEncodingsField;
//#endif
	private javax.microedition.lcdui.TextField customWordsField;
	private javax.microedition.lcdui.TextField locationFormatField;
	private javax.microedition.lcdui.TextField cellIdFormatField;
	private javax.microedition.lcdui.ChoiceGroup startsField;
	private javax.microedition.lcdui.TextField timeOffsetField;
	private javax.microedition.lcdui.ChoiceGroup flagsField;
	private javax.microedition.lcdui.TextField hackField;
	private Command saveCommand;
	private Command cancelCommand;
	private Command togglePasswordCommand;

	public SetupScreen(TwitterController controller) {
		super("Setup");
		this.controller = controller;

		Settings settings = controller.getSettings();

		String username = settings.getStringProperty(Settings.USERNAME, "");
		usernameField = new javax.microedition.lcdui.TextField("Username", username, 32, TextField.ANY);
		append(usernameField);

		String password = settings.getStringProperty(Settings.PASSWORD, "");
		passwordField = new javax.microedition.lcdui.TextField("Password", password, 32, TextField.PASSWORD);
		append(passwordField);

		int timelineLength = settings.getIntProperty(Settings.TIMELINE_LENGTH, 20);
		timelineLengthField = new javax.microedition.lcdui.TextField("Items in Timeline", String.valueOf(timelineLength), 32, TextField.NUMERIC);
		append(timelineLengthField);

		String suffixText = settings.getStringProperty(Settings.SUFFIX_TEXT, "");
		suffixTextField = new javax.microedition.lcdui.TextField("Suffix", suffixText, 140, TextField.ANY);
		append(suffixTextField);

		int refreshInterval = settings.getIntProperty(Settings.REFRESH_INTERVAL, 120);
		refreshIntervalField = new javax.microedition.lcdui.TextField("Refresh Interval", String.valueOf(refreshInterval), 32, TextField.NUMERIC);
		append(refreshIntervalField);

		String autoUpdateText = settings.getStringProperty(Settings.AUTO_UPDATE_TEXT, "%H:%M");
		autoUpdateTextField = new javax.microedition.lcdui.TextField("Auto tweet text", autoUpdateText, 140, TextField.ANY);
		append(autoUpdateTextField);

		String gateway = settings.getStringProperty(Settings.GATEWAY, "http://nest.onedd.net/text/");
		gatewayField = new javax.microedition.lcdui.TextField("Gateway", gateway, 128, TextField.URL);
		append(gatewayField);

		gatewaysField = new javax.microedition.lcdui.ChoiceGroup("Preset Gateways", Choice.EXCLUSIVE, gatewaysLabel, null);
		append(gatewaysField);

//#ifdef polish.api.mmapi
		String pictureGateway = settings.getStringProperty(Settings.PICTURE_GATEWAY, "http://nest.onedd.net/text/twitpic/");
		pictureGatewayField = new javax.microedition.lcdui.TextField("Picture Gateway", pictureGateway, 128, TextField.URL);
		append(pictureGatewayField);

		pictureGatewaysField = new javax.microedition.lcdui.ChoiceGroup("Preset Picture Gateways", Choice.EXCLUSIVE, pictureGatewaysLabel, null);
		append(pictureGatewaysField);

		String captureDevice = settings.getStringProperty(Settings.CAPTURE_DEVICE, Device.getSnapshotLocator());
		captureDeviceField = new javax.microedition.lcdui.TextField("Capture Device", captureDevice, 128, TextField.ANY);
		append(captureDeviceField);

		captureDevicesField = new javax.microedition.lcdui.ChoiceGroup("Preset Capture Devices", Choice.EXCLUSIVE, captureDevicesLabel, null);
		captureDevicesField.setSelectedIndex(0, true);
		append(captureDevicesField);

		String snapshotEncoding = settings.getStringProperty(Settings.SNAPSHOT_ENCODING, "");
		snapshotEncodingField = new javax.microedition.lcdui.TextField("Picture Options", snapshotEncoding, 128, TextField.ANY);
		append(snapshotEncodingField);

		snapshotEncodingsField = new javax.microedition.lcdui.ChoiceGroup("Preset Picture Options", Choice.EXCLUSIVE);
		snapshotEncodingsField.append("Custom", null);
		snapshotEncodingsLabel = Device.getSnapshotEncodings();
		if (snapshotEncodingsLabel != null) {
			for(int i=0; i<snapshotEncodingsLabel.length; i++) {
				snapshotEncodingsField.append(snapshotEncodingsLabel[i], null);
			}
		}
		snapshotEncodingsField.setSelectedIndex(0, true);
		append(snapshotEncodingsField);
//#endif

		String customWords = settings.getStringProperty(Settings.CUSTOM_WORDS, "#jibjib,@having");
		customWordsField = new javax.microedition.lcdui.TextField("Custom Words", customWords, 1000, TextField.ANY);
		append(customWordsField);

		String locationFormat = settings.getStringProperty(Settings.LOCATION_FORMAT, "l:%lat,%lon http://maps.google.com/maps?q=%lat%2c%lon");
		locationFormatField = new javax.microedition.lcdui.TextField("Location Format", locationFormat, 1000, TextField.ANY);
		append(locationFormatField);

		String cellIdFormat = settings.getStringProperty(Settings.CELLID_FORMAT, "c2l:%cid,%lac");
		cellIdFormatField = new javax.microedition.lcdui.TextField("Cell ID Format", cellIdFormat, 1000, TextField.ANY);
		append(cellIdFormatField);

		int startScreen = settings.getIntProperty(Settings.START_SCREEN, 0);
		startsField = new javax.microedition.lcdui.ChoiceGroup("Start Screen", Choice.EXCLUSIVE, startsLabel, null);
		startsField.setSelectedIndex(startScreen, true);
		append(startsField);

		String timeOffset = settings.getStringProperty(Settings.TIME_OFFSET, "0000");
		timeOffsetField = new javax.microedition.lcdui.TextField("Time Offset", timeOffset, 5, TextField.NUMERIC);
		append(timeOffsetField);

		boolean[] flags = {
			settings.getBooleanProperty(Settings.OPTIMIZE_BANDWIDTH, true),
			settings.getBooleanProperty(Settings.ALTERNATE_AUTHEN, false),
			settings.getBooleanProperty(Settings.SNAPSHOT_FULLSCREEN, false),
			settings.getBooleanProperty(Settings.STATUS_LENGTH_MAX, false),
			settings.getBooleanProperty(Settings.RESIZE_THUMBNAIL, true),
			settings.getBooleanProperty(Settings.WRAP_TIMELINE, false),
			settings.getBooleanProperty(Settings.ENABLE_SQUEEZE, true),
			settings.getBooleanProperty(Settings.ENABLE_GPS, true),
			settings.getBooleanProperty(Settings.ENABLE_REVERSE_GEOCODER, true),
			settings.getBooleanProperty(Settings.ENABLE_CELL_ID, true),
			settings.getBooleanProperty(Settings.ENABLE_REFRESH, true),
			settings.getBooleanProperty(Settings.ENABLE_REFRESH_ALERT, true),
			settings.getBooleanProperty(Settings.ENABLE_REFRESH_VIBRATE, true),
			settings.getBooleanProperty(Settings.ENABLE_REFRESH_COUNTER, false),
			settings.getBooleanProperty(Settings.SWAP_MINIMIZE_REFRESH, false),
			settings.getBooleanProperty(Settings.ENABLE_AUTO_UPDATE, false),
			settings.getBooleanProperty(Settings.ENABLE_AUTO_UPDATE_PICTURE, false),
			settings.getBooleanProperty(Settings.FORCE_NO_HOST, false),
		};
		flagsField = new javax.microedition.lcdui.ChoiceGroup("Advanced Options", Choice.MULTIPLE, flagsLabel, null);
		flagsField.setSelectedFlags(flags);
		append(flagsField);

		String hack = settings.getStringProperty(Settings.HACK, "");
		hackField = new javax.microedition.lcdui.TextField("Hack", hack, 1024, TextField.ANY);
		append(hackField);

		saveCommand = new Command("Save", Command.OK, 1);
		addCommand(saveCommand);
		cancelCommand = new Command("Cancel", Command.CANCEL, 2);
		addCommand(cancelCommand);
		togglePasswordCommand = new Command("Toggle Password", Command.SCREEN, 3);
		addCommand(togglePasswordCommand);

		setCommandListener(this);
		setItemStateListener(this);
	}

	public void itemStateChanged(javax.microedition.lcdui.Item item) {
		if (item == gatewaysField) {
			String url = gatewaysValue[gatewaysField.getSelectedIndex()];
			if (url != null) {
				gatewayField.setString(url);
			}
//#ifdef polish.api.mmapi
		} else if (item == pictureGatewaysField) {
			String url = pictureGatewaysValue[pictureGatewaysField.getSelectedIndex()];
			if (url != null) {
				pictureGatewayField.setString(url);
			}
		} else if (item == captureDevicesField) {
			String device = captureDevicesValue[captureDevicesField.getSelectedIndex()];
			if (device != null) {
				captureDeviceField.setString(device);
			}
		} else if (item == snapshotEncodingsField) {
			snapshotEncodingsLabel = Device.getSnapshotEncodings();
			int index = snapshotEncodingsField.getSelectedIndex();
			if (index > 0) {
				snapshotEncodingField.setString(snapshotEncodingsLabel[index-1]);
			}
//#endif
		}
	}

	public void commandAction(Command cmd, Displayable display) {
		if (cmd == saveCommand) {
			try {
				Log.verbose("userField");
				String username = usernameField.getString();
				Log.verbose("passwordField");
				String password = passwordField.getString();
				Log.verbose("gatewayField");
				String gateway = gatewayField.getString();
				Log.verbose("timelineLengthField");
				int timelineLength = Integer.parseInt(timelineLengthField.getString());
				Log.verbose("suffixTextField");
				String suffixText = suffixTextField.getString();
				Log.verbose("refreshIntervalField");
				int refreshInterval = Integer.parseInt(refreshIntervalField.getString());
				Log.verbose("autoUpdateTextField");
				String autoUpdateText = autoUpdateTextField.getString();
				Log.verbose("customWordsField");
				String customWords = customWordsField.getString();
				Log.verbose("locationFormatField");
				String locationFormat = locationFormatField.getString();
				Log.verbose("cellIdFormatField");
				String cellIdFormat = cellIdFormatField.getString();
				Log.verbose("startsField");
				int startScreen = startsField.getSelectedIndex();
				Log.verbose("timeOffsetField");
				String timeOffset = timeOffsetField.getString();

//#ifdef polish.api.mmapi
				Log.verbose("pictureGatewayField");
				String pictureGateway = pictureGatewayField.getString();
				Log.verbose("captureDeviceField");
				String captureDevice = captureDeviceField.getString().trim();
				Log.verbose("snapshotEncodingField");
				String snapshotEncoding = snapshotEncodingField.getString().trim();
//#endif

				Log.verbose("flagsField");
				boolean[] flags = new boolean[flagsField.size()];
				flagsField.getSelectedFlags(flags);
				Log.verbose("hackField");
				String hack = hackField.getString();

				if (!gateway.endsWith("/")) {
					gateway += "/";
				}

				Log.verbose("getSettings");
				Settings settings = controller.getSettings();

				settings.setStringProperty(Settings.USERNAME, username);
				settings.setStringProperty(Settings.PASSWORD, password);
				settings.setStringProperty(Settings.GATEWAY, gateway);
				settings.setIntProperty(Settings.TIMELINE_LENGTH, timelineLength);
				settings.setStringProperty(Settings.SUFFIX_TEXT, suffixText);
				settings.setIntProperty(Settings.REFRESH_INTERVAL, refreshInterval);
				settings.setStringProperty(Settings.AUTO_UPDATE_TEXT, autoUpdateText);
				settings.setStringProperty(Settings.CUSTOM_WORDS, customWords);
				settings.setStringProperty(Settings.LOCATION_FORMAT, locationFormat);
				settings.setStringProperty(Settings.CELLID_FORMAT, cellIdFormat);
				settings.setStringProperty(Settings.TIME_OFFSET, timeOffset);
				settings.setIntProperty(Settings.START_SCREEN, startScreen);
				settings.setStringProperty(Settings.TIME_OFFSET, timeOffset);
				settings.setBooleanProperty(Settings.OPTIMIZE_BANDWIDTH, flags[0]);
				settings.setBooleanProperty(Settings.ALTERNATE_AUTHEN, flags[1]);
				settings.setBooleanProperty(Settings.SNAPSHOT_FULLSCREEN, flags[2]);
				settings.setBooleanProperty(Settings.STATUS_LENGTH_MAX, flags[3]);
				settings.setBooleanProperty(Settings.RESIZE_THUMBNAIL, flags[4]);
				settings.setBooleanProperty(Settings.WRAP_TIMELINE, flags[5]);
				settings.setBooleanProperty(Settings.ENABLE_SQUEEZE, flags[6]);
				settings.setBooleanProperty(Settings.ENABLE_GPS, flags[7]);
				settings.setBooleanProperty(Settings.ENABLE_REVERSE_GEOCODER, flags[8]);
				settings.setBooleanProperty(Settings.ENABLE_CELL_ID, flags[9]);
				settings.setBooleanProperty(Settings.ENABLE_REFRESH, flags[10]);
				settings.setBooleanProperty(Settings.ENABLE_REFRESH_ALERT, flags[11]);
				settings.setBooleanProperty(Settings.ENABLE_REFRESH_VIBRATE, flags[12]);
				settings.setBooleanProperty(Settings.ENABLE_REFRESH_COUNTER, flags[13]);
				settings.setBooleanProperty(Settings.SWAP_MINIMIZE_REFRESH, flags[14]);
				settings.setBooleanProperty(Settings.ENABLE_AUTO_UPDATE, flags[15]);
				settings.setBooleanProperty(Settings.ENABLE_AUTO_UPDATE_PICTURE, flags[16]);
				settings.setBooleanProperty(Settings.FORCE_NO_HOST, flags[17]);
				settings.setStringProperty(Settings.HACK, hack);
//#ifdef polish.api.mmapi
				settings.setStringProperty(Settings.PICTURE_GATEWAY, pictureGateway);
				settings.setStringProperty(Settings.CAPTURE_DEVICE, captureDevice);
				settings.setStringProperty(Settings.SNAPSHOT_ENCODING, snapshotEncoding);
//#endif

				controller.loadSettings();

				Log.verbose("save");
				settings.save(true);
			} catch (JSONException e) {
				Log.error(e.toString());
				controller.showError(e);
			} catch (IOException e) {
				Log.error(e.toString());
				controller.showError(e);
			} catch (RecordStoreException e) {
				Log.error(e.toString());
				controller.showError(e);
			} catch (Exception e) {
				Log.error(e.toString());
				controller.showError(e);
			}
			controller.showTimeline();
		} else if (cmd == cancelCommand) {
			controller.showTimeline();
		} else if (cmd == togglePasswordCommand) {
			passwordField.setConstraints(passwordField.getConstraints()^TextField.PASSWORD);
		}
	}
}
