// ifdef PIGLER_SUPPORT
package org.pigler.tester;

import javax.microedition.lcdui.Image;

import org.pigler.api.IPiglerTapHandler;
import org.pigler.api.PiglerAPI;

public class PiglerAPILayer {
	
	private PiglerAPI api;

	public PiglerAPILayer() {
		api = new PiglerAPI();
	}

	public int init(String appName) throws Exception {
		return api.init(appName);
	}

	public void close() {
		api.close();
	}

	public int createNotification(String title, String text, Image icon, boolean removeOnTap) throws Exception {
		return api.createNotification(title, text, icon, removeOnTap);
	}

	public void removeNotification(int uid) throws Exception {
		api.removeNotification(uid);
	}

	public int removeAllNotifications() throws Exception {
		return api.removeAllNotifications();
	}

	public void updateNotification(int uid, String title, String text) throws Exception {
		api.updateNotification(uid, title, text);
	}
	
	public void setListener(final PiglerAPIHandlerLayer handler) {
		api.setListener(new IPiglerTapHandler() {
			public void handleNotificationTap(int uid) {
				handler.handleNotificationTap(uid);
			}
		});
	}

	public void showGlobalPopup(String title, String text, int flags) throws Exception {
		api.showGlobalPopup(title, text, flags);  // If you get a compiler error here, make sure you have the correct javapiglerapi.jar version from https://nnp.nnchan.ru/pna/lib/javapiglerapi.jar
	}

	public int getAPIVersion() throws Exception {
		return api.getAPIVersion();
	}

}
// endif