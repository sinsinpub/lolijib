/**
 * optimized thumbnail code is written by Kamanashis Roy.
 *
 * http://miniim.blogspot.com/2008/05/image-thumbnail-in-optimized-way-for.html
 */
package com.sugree.twitter.views;

import java.io.IOException;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
//#ifdef polish.api.mmapi
import javax.microedition.media.control.GUIControl;
import javax.microedition.media.control.VideoControl;
//#endif
import javax.microedition.media.MediaException;

import com.substanceofcode.twitter.Settings;
import com.substanceofcode.utils.Log;
import com.substanceofcode.infrastructure.Device;

import com.sugree.twitter.TwitterController;

// use fully qualified classname, make sure it use native GUI, and not Polish GUI
public class SnapshotScreen extends javax.microedition.lcdui.Form implements CommandListener, PlayerListener {
//#ifdef polish.api.mmapi
	private TwitterController controller;

	private Player player;
	private VideoControl videoControl;
	private javax.microedition.lcdui.Item videoItem;
	private String status;
	private byte[] snapshotRaw;
	private javax.microedition.lcdui.Image snapshotImage;
	private boolean visible;

	private Command snapCommand;
	private Command okCommand;
	private Command cancelCommand;
	private Command retryCommand;
	private Command visibleCommand;
//#endif

	public SnapshotScreen(TwitterController controller, String status) throws Exception {
		super("Take a picture");
//#ifdef polish.api.mmapi
		this.controller = controller;
		this.status = status;

		init();

		snapCommand = new Command("Capture", Command.OK, 1);
		addCommand(snapCommand);
		okCommand = new Command("OK", Command.OK, 2);
		cancelCommand = new Command("Cancel", Command.BACK, 3);
		addCommand(cancelCommand);
		retryCommand = new Command("Retry", Command.SCREEN, 4);
		visibleCommand = new Command("Visible/Hide", Command.SCREEN, 5);
		addCommand(visibleCommand);

		setCommandListener(this);
//#endif
	}

	public void playerUpdate(Player player, String event, Object eventData) {
		Log.info("playerUpdate "+event);
	}

//#ifdef polish.api.mmapi
	private void init() throws Exception {
		String locator = controller.getSettings().getStringProperty(Settings.CAPTURE_DEVICE, Device.getSnapshotLocator());
		boolean fullscreen = controller.getSettings().getBooleanProperty(Settings.SNAPSHOT_FULLSCREEN, false);
		String hack = controller.getSettings().getStringProperty(Settings.HACK, "");
		String uri;

		synchronized (this) {
			player = null;
			try {
				Log.info("opening "+locator);
				player = Manager.createPlayer(locator);
				player.addPlayerListener(this);
			} catch (Error e) {
				throw new Exception("createPlayer(\""+locator+"\") "+e.toString());
			}
		}

		player.realize();
		Log.info("realize() "+player.getState());

		videoControl = (VideoControl)player.getControl("VideoControl");
		Log.info("getControl() "+videoControl);
		videoItem = (javax.microedition.lcdui.Item)videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
		Log.info("initDisplayMode() "+videoItem);
//#if polish.midp2 || polish.midp3
		if (!fullscreen) {
			videoControl.setDisplaySize(getHeight()*videoControl.getSourceWidth()/videoControl.getSourceHeight(), getHeight());
			videoItem.setLayout(javax.microedition.lcdui.Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);
			videoItem.setPreferredSize(getHeight()*videoControl.getSourceWidth()/videoControl.getSourceHeight(), getHeight());
		}
//#endif
		if (fullscreen) {
			try {
				videoControl.setDisplayFullScreen(true);
			} catch (MediaException e) {
				Log.error(e.toString());
			}
		}
	}

//#ifdef polish.midp1
	public void deleteAll() {
		for(int i=size()-1; i>=0; i--) {
			delete(i);
		}
	}
//#endif

	public void start(boolean visibleFlag) {
		deleteAll();
		append(videoItem);

		visible = visibleFlag;
		videoControl.setVisible(visible);
		try {
			player.start();
		} catch (Exception e) {
			Log.error("start() "+e.toString());
		}
	}

	private void destroy() {
		videoControl = null;
		try {
			player.stop();
			player.deallocate();
			player.close();
		} catch (Exception e) {
			Log.error("stop() "+e.toString());
		}
		player = null;
	}

	public void quickSnapshot() throws Exception {
		quickSnapshot(false);
	}

	public void quickSnapshot(boolean background) throws Exception {
		String encoding = controller.getSettings().getStringProperty(Settings.SNAPSHOT_ENCODING, null);
		if (encoding != null && encoding.length() == 0) {
			encoding = null;
		}

		try {
			snapshotRaw = videoControl.getSnapshot(encoding);
			player.stop();
			deleteAll();
			destroy();
			controller.setSnapshot(snapshotRaw);
			snapshotRaw = null;
		} catch (Exception e) {
			Log.error("getSnapshot(\""+encoding+"\") "+e.toString());
			destroy();
			throw e;
		}
		if (!background) {
			controller.showUpdate(status);
		}
	}

	private void getSnapshot() {
		String encoding = controller.getSettings().getStringProperty(Settings.SNAPSHOT_ENCODING, null);
		boolean resize = controller.getSettings().getBooleanProperty(Settings.RESIZE_THUMBNAIL, true);
		if (encoding != null && encoding.length() == 0) {
			encoding = null;
		}
		try {
			snapshotRaw = videoControl.getSnapshot(encoding);
			player.stop();
			deleteAll();
			if (resize) {
				snapshotImage = createThumbnail(javax.microedition.lcdui.Image.createImage(snapshotRaw, 0, snapshotRaw.length));
			} else {
				snapshotImage = javax.microedition.lcdui.Image.createImage(snapshotRaw, 0, snapshotRaw.length);
			}
//#if polish.midp2 || polish.midp3
			javax.microedition.lcdui.ImageItem item = new javax.microedition.lcdui.ImageItem("", snapshotImage, Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER, null);
//#else
//			javax.microedition.lcdui.ImageItem item = new javax.microedition.lcdui.ImageItem("", snapshotImage, ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_NEWLINE_AFTER, null);
//#endif
			append(item);
		} catch (Exception e) {
			Log.error("getSnapshot(\""+encoding+"\") "+e.toString());
			destroy();
			controller.showError(e, TwitterController.SCREEN_UPDATE);
		}
	}
//#endif

	public void commandAction(Command cmd, Displayable display) {
//#ifdef polish.api.mmapi
		int state = player.getState();

		if (cmd == okCommand && state != Player.STARTED) {
			destroy();
			controller.setSnapshot(snapshotRaw);
			snapshotRaw = null;
			controller.showUpdate(status);
		} else if (cmd == snapCommand && state == Player.STARTED) {
			getSnapshot();
			removeCommand(snapCommand);
			removeCommand(visibleCommand);
			addCommand(okCommand);
			addCommand(retryCommand);
			setTitle("Confirm?");
		} else if (cmd == cancelCommand) {
			destroy();
			controller.showUpdate(status);
		} else if (cmd == retryCommand && state != Player.STARTED) {
			snapshotImage = null;
			snapshotRaw = null;
			start(visible);
			addCommand(snapCommand);
			addCommand(visibleCommand);
			removeCommand(okCommand);
			removeCommand(retryCommand);
			setTitle("Take a picture");
		} else if (cmd == visibleCommand) {
			visible = !visible;
			videoControl.setVisible(visible);
		}
//#endif
	}

//#if polish.midp2 || polish.midp3
	public javax.microedition.lcdui.Image createThumbnail(javax.microedition.lcdui.Image image) {
		int sw = image.getWidth();
		int sh = image.getHeight();

		int ph = getHeight();
		int pw = ph * sw / sh;

		return getThumbnailWrapper(image, pw, ph, 0);
	}

    /**
     * Gets the thumbnail that fit with given screen width, height and padding.
     *
     * @param image The source image
     * @param padding padding to the screen
     * @return scaled image
     */
	private final javax.microedition.lcdui.Image getThumbnailWrapper(javax.microedition.lcdui.Image image, int expectedWidth, int expectedHeight, int padding) {
		final int sourceWidth = image.getWidth();
		final int sourceHeight = image.getHeight();
		int thumbWidth = -1;
		int thumbHeight = -1;

		// big width
		if(sourceWidth >= sourceHeight) {
			thumbWidth = expectedWidth - padding;
			thumbHeight = thumbWidth * sourceHeight / sourceWidth;
			// fits to height ?
			if(thumbHeight > (expectedHeight - padding)) {
				thumbHeight = expectedHeight - padding;
				thumbWidth = thumbHeight * sourceWidth / sourceHeight;
			}
		} else {
			// big height
			thumbHeight = expectedHeight - padding;
			thumbWidth = thumbHeight * sourceWidth / sourceHeight;
			// fits to width ?
			if(thumbWidth > (expectedWidth - padding)) {
				thumbWidth = expectedWidth - padding;
				thumbHeight = thumbWidth * sourceHeight / sourceWidth;
			}
		}

		// XXX As we do not have floating point, sometimes the thumbnail resolution gets bigger ...
		// we are trying hard to avoid that ..
		thumbHeight = (sourceHeight < thumbHeight) ? sourceHeight : thumbHeight;
		thumbWidth = (sourceWidth < thumbWidth) ? sourceWidth : thumbWidth;

		return getThumbnail(image, thumbWidth, thumbHeight);
	}

    /**
     * Gets thumbnail with a height and width specified ..
     * @param image
     * @param thumbWidth
     * @param thumbHeight
     * @return scaled image
     */
	private final javax.microedition.lcdui.Image getThumbnail(javax.microedition.lcdui.Image image, int thumbWidth, int thumbHeight) {
		int x, y, pos, tmp, z = 0;
		final int sourceWidth = image.getWidth();
		final int sourceHeight = image.getHeight();

		// integer ratio ..
		final int ratio = sourceWidth / thumbWidth;

		// buffer where we read in data from image source
		final int[] in = new int[sourceWidth];

		// buffer of output thumbnail image
		final int[] out = new int[thumbWidth*thumbHeight];

		final int[] cols = new int[thumbWidth];

		// pre-calculate columns we need to access from source image
		for (x = 0,pos = 0; x < thumbWidth; x++) {
			cols[x] = pos;

			// increase the value without fraction calculation
			pos += ratio;
			tmp = pos + (thumbWidth - x) * ratio;
			if(tmp > sourceWidth) {
				pos--;
			} else if(tmp < sourceWidth - ratio) {
				pos++;
			}
		}

		// read through the rows ..
		for (y = 0, pos = 0, z = 0; y < thumbHeight; y++) {

			// read a single row ..
			image.getRGB(in, 0, sourceWidth, 0, pos, sourceWidth, 1);

			for (x = 0; x < thumbWidth; x++, z++) {
				// write this row to thumbnail
				out[z] = in[cols[x]];
			}

			pos += ratio;
			tmp = pos + (thumbHeight - y) * ratio;
			if(tmp > sourceHeight) {
				pos--;
			} else if(tmp < sourceHeight - ratio) {
				pos++;
			}
		}
		return javax.microedition.lcdui.Image.createRGBImage(out, thumbWidth, thumbHeight, false);
	}
//#else
//	private final javax.microedition.lcdui.Image createThumbnail(javax.microedition.lcdui.Image image) {
//		int sourceWidth = image.getWidth();
//		int sourceHeight = image.getHeight();
//
//		int thumbWidth = 64;
//		int thumbHeight = -1;
//
//		if (thumbHeight == -1)
//			thumbHeight = thumbWidth * sourceHeight / sourceWidth;
//
//		javax.microedition.lcdui.Image thumb = javax.microedition.lcdui.Image.createImage(thumbWidth, thumbHeight);
//		Graphics g = thumb.getGraphics();
//
//		for (int y = 0; y < thumbHeight; y++) {
//			for (int x = 0; x < thumbWidth; x++) {
//				g.setClip(x, y, 1, 1);
//				int dx = x * sourceWidth / thumbWidth;
//				int dy = y * sourceHeight / thumbHeight;
//				g.drawImage(image, x - dx, y - dy,
//						Graphics.LEFT | Graphics.TOP);
//			}
//		}
//
//		javax.microedition.lcdui.Image immutableThumb = javax.microedition.lcdui.Image.createImage(thumb);
//
//		return immutableThumb;
//	}
//#endif

}
