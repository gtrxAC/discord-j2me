/*
Copyright (c) 2022 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package jtube;

import com.gtrxac.discord.Util;

//#ifdef CHECK_DIRECTFONT
public class PlatformUtils {
	public static final String platform;

	public static boolean isJ9;
	public static boolean isAsha;
	public static boolean isS40;

    static {
		String p = System.getProperty("microedition.platform");
		if(p == null) p = "";
		platform = p;

		// s40 check
		isS40 = checkClass("javax.microedition.midlet.MIDletProxy") || checkClass("com.nokia.mid.impl.isa.jam.Jam");

		// asha check
		String s40v = getS40_version();
		isAsha = isNokia() && s40v != null && (s40v.startsWith("7") || s40v.startsWith("8") || s40v.startsWith("9"));

		isJ9 = platform.indexOf("sw_platform=S60") != -1;
    }
	
	private static boolean isJ9S60Version(String v) {
		return isJ9 && platform.indexOf("sw_platform_version=" + v) != -1;
	}
	
	public static boolean isS60v5() {
		return isJ9 && isJ9S60Version("5");
	}
	
	public static boolean isAshaFullTouch() {
		if(!isAsha) return false;
		if(System.getProperty("com.nokia.mid.ui.version") == null) return false;
		String s = platform.substring(5);
		if(s.startsWith("Asha5")) return true;
		char c1 = s.charAt(0);
		char c2 = s.charAt(1);
		char c3 = s.charAt(2);
		if((c1 != '2' && c1 != '3' && c1 != '5') || (c2 != '0' && c2 != '1' && c2 != '3')) return false;
		if(c1 == '5') return c3 != '0';
		if(c1 == '2') return c2 == '3';
		if(c1 == '3') return c2 == '0' ? c3 == '5' || c3 == '6' || c3 == '8' || c3 == '9' : c2 == '1' && (c3 == '0' || c3 == '1');
		return false;
	}

	public static boolean isNokia() {
		return platform.startsWith("Nokia");
	}

	public static String getS40_version() { // not real version
		if(!isS40) {
			return null;
		}
		if(platform.startsWith("Nokia300/") || platform.startsWith("NokiaC3-01") || platform.startsWith("NokiaX3-02")) {
			return "6.1"; // 6th Edition FP1
		}
		isAsha = true;
		if(isAshaFullTouch()) {
			if(platform.startsWith("Nokia230") || platform.startsWith("Nokia5")) {
				return "9.0"; // Asha Platform
			}
		}
		if(checkClass("javax.microedition.sensor.SensorManager")) { // has jsr 256
			return "8.0"; // Java Runtime 2.0
		}
		if(isAshaTouchAndType() || isAshaNoTouch()) {
			if(checkClass("com.nokia.mid.payment.IAPClientPaymentManager")) {
				return "7.1"; // Java Runtime 1.1
			}
			return "7.0"; // Java Runtime 1.0
		}
		isAsha = false;
		if(checkClass("com.arm.cldc.mas.GlobalLock")) {
			return "6.0"; // 6th Edition SDK
		}
		if(checkClass("javax.microedition.location.LocationProvider")) { // has jsr 179
			return "6.0"; // 6th Edition SDK
		}
		if(checkClass("javax.microedition.content.ContentHandler")) { // has chapi
			if(System.getProperty("microedition.jtwi.version") != null) { // has jtwi
				return "6.0 Lite"; // 6th Edition Lite
			}
			return "5.1"; // 5th Edition FP1 SDK
		}
		if(System.getProperty("microedition.jtwi.version") != null) {
			return "5.1 Lite"; // 6th Edition FP1 Lite
		}
		if(checkClass("javax.microedition.amms.GlobalManager")) { // has amms
			return "5.0"; // 5th Edition SDK
		}
		if(checkClass("javax.crypto.Cipher")) { // has crypto api
			return "3.2"; // 3rd Edition FP2
		}
		if(checkClass("javax.xml.parsers")) { // has jsr 172 xml
			return "3.1"; // 3rd Edition FP1
		}
		if(checkClass("javax.microedition.xml.rpc.Element")) { // has jsr 172 rpc
			return "3.1"; // 3rd Edition FP1
		}
		if(checkClass("javax.microedition.m2g.ScalableGraphics")) { // has m2g
			return "3.0.1"; // 3rd Edition
		}
		if(checkClass("com.nokia.mid.pri.PriAccess")) {
			return "3.0"; // 3rd Edition SDK
		}
		if(checkClass("javax.microedition.io.file.FileConnection")) { // has jsr 75
			return "2.2"; // DP 2.0 SDK 6230i
		}
		if(checkClass("com.nokia.mid.impl.isa.io.GeneralSharedIO")) {
			return "2.1"; // DP 2.0 SDK 1.1
		}
		if(checkClass("javax.microedition.lcdui.game.GameCanvas")) { // has midp 2.0
			return "2.0"; // DP 2.0 SDK 1.0
		}
		if(checkClass("com.sun.midp.Main")) {
			return null; // 3410 / not s40
		}
		if(checkClass("javax.microedition.media.Manager")) { // has jsr 135
			return "1.2"; // 3300
		}
		if(checkClass("javax.wireless.messaging.MessageConnection")) { // has wma
			return "1.1";
		}
		return "1.0";
	}
	
	public static boolean checkClass(String s) {
		// try {
		// 	Class.forName(s);
		// 	return true;
		// } catch (Exception e) {
		// }
		// return false;
		return Util.checkClass(s);
	}

	public static boolean isAshaTouchAndType() {
		if(!isAsha) return false;
		String s = platform.substring(5);
		char c1 = s.charAt(0);
		char c2 = s.charAt(1);
		char c3 = s.charAt(2);
		if((c1 != '2' && c1 != '3') || c2 != '0') return false;
		if(c1 == '2') return c2 == '0' && (c3 == '2' || c3 == '3');
		if(c1 == '3') return c2 == '0' && (c3 == '0' || c3 == '3');
		return false;
	}

	public static boolean isAshaNoTouch() {
		if(!isAsha) return false;
		String s = platform.substring(5);
		char c1 = s.charAt(0);
		char c2 = s.charAt(1);
		char c3 = s.charAt(2);
		if((c1 != '2' && c1 != '3') || (c2 != '0' && c2 != '1')) return false;
		if(c1 == '2') return c2 == '0' ? c3 == '0' && c3 == '1' && c3 == '5' : c2 == '1' && c3 == '0';
		if(c1 == '3') return c2 == '0' && c3 == '2';
		return false;
	}
}
//#endif