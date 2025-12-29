package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.lang.Math;

public class Util {
	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	/**
	 * Get array of text lines to draw (word wrap)
	 * https://github.com/shinovon/JTube/blob/2.6.1/src/jtube/Util.java
	 */
	public static String[] wordWrap(String text, int maxWidth, Font font) {
		if (text == null || text.length() == 0 || text.equals(" ") || maxWidth < font.charWidth('W') + 2) {
			return new String[0];
		}
		text = replace(text, "\r", "");
		Vector v = new Vector(3);
		char[] chars = text.toCharArray();
		if (text.indexOf('\n') > -1) {
			int j = 0;
			for (int i = 0; i < text.length(); i++) {
				if (chars[i] == '\n') {
					v.addElement(text.substring(j, i));
					j = i + 1;
				}
			}
			v.addElement(text.substring(j, text.length()));
		} else {
			v.addElement(text);
		}
		for (int i = 0; i < v.size(); i++) {
			String s = (String) v.elementAt(i);
			if(font.stringWidth(s) >= maxWidth) {
				int i1 = 0;
				for (int i2 = 0; i2 < s.length(); i2++) {
					if (font.stringWidth(s.substring(i1, i2+1)) >= maxWidth) {
						boolean space = false;
						for (int j = i2; j > i1; j--) {
							char c = s.charAt(j);
							if (c == ' ' || (c >= ',' && c <= '/')) {
								space = true;
								v.setElementAt(s.substring(i1, j + 1), i);
								v.insertElementAt(s.substring(j + 1), i + 1);
								i += 1;
								i2 = i1 = j + 1;
								break;
							}
						}
						if (!space) {
							i2 = i2 - 2;
							v.setElementAt(s.substring(i1, i2), i);
							v.insertElementAt(s.substring(i2), i + 1);
							i2 = i1 = i2 + 1;
							i += 1;
						}
					}
				}
			}
		}
		String[] arr = new String[v.size()];
		v.copyInto(arr);
		return arr;
	}

	public static int[] resizeFit(int imgW, int imgH, int maxW, int maxH) {
		return resizeFit(imgW, imgH, maxW, maxH, false);
	}

	public static int[] resizeFit(int imgW, int imgH, int maxW, int maxH, boolean mustUpscale) {
		int imgAspect = imgW*100 / imgH;
		int maxAspect = maxW*100 / maxH;
		int width, height;

		if (!mustUpscale && imgW <= maxW && imgH <= maxH) {
			width = imgW;
			height = imgH;
		}
		else if (imgAspect > maxAspect) {
			width = maxW;
			height = (maxW*100)/imgAspect;
		} else {
			height = maxH;
			width = (maxH*imgAspect)/100;
		}

		return new int[]{width, height};
	}

	public static int indexOfAny(String haystack, String[] needles, int startIndex) {
		int result = -1;

		for (int i = 0; i < needles.length; i++) {
			int current = haystack.indexOf(needles[i], startIndex);
			if (current != -1 && (current < result || result == -1)) {
				result = current;
			}
		}
		return result;
	}

	public static Image resizeImage(Image sourceImage, int newWidth, int newHeight) {
		int sourceWidth = sourceImage.getWidth();
		int sourceHeight = sourceImage.getHeight();
		int[] srcBuffer = new int[sourceWidth * sourceHeight];
		sourceImage.getRGB(srcBuffer, 0, sourceWidth, 0, 0, sourceWidth, sourceHeight);
		
		int[] newBuffer = new int[newWidth * newHeight];
		
		// Calculate the scaling ratio
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				// Find the corresponding pixel in the source image
				int srcX = x * sourceWidth / newWidth;
				int srcY = y * sourceHeight / newHeight;
				newBuffer[y * newWidth + x] = srcBuffer[srcY * sourceWidth + srcX];
			}
		}
		
		return Image.createRGBImage(newBuffer, newWidth, newHeight, true);
	}

	/*
	 * Part of the TUBE42 imagelib, released under the LGPL license.
	 *
	 * Development page: https://github.com/tube42/imagelib
	 * License:          http://www.gnu.org/copyleft/lesser.html
	 */
	public static final Image resizeImageBilinear(Image src_i, int w1, int h1) {
		int w0 = src_i.getWidth();
		int h0 = src_i.getHeight();
		int[] dst = new int[w1*h1];

		int[] buffer1 = new int[w0];
		int[] buffer2 = new int[w0];

		// UNOPTIMIZED bilinear filtering:			   
		//		 
		// The pixel position is defined by y_a and y_b,
		// which are 24.8 fixed point numbers
		// 
		// for bilinear interpolation, we use y_a1 <= y_a <= y_b1
		// and x_a1 <= x_a <= x_b1, with y_d and x_d defining how long
		// from x/y_b1 we are.
		//
		// since we are resizing one line at a time, we will at most 
		// need two lines from the source image (y_a1 and y_b1).
		// this will save us some memory but will make the algorithm 
		// noticeably slower

		for (int index1 = 0, y = 0; y < h1; y++) {
			final int y_a = ((y * h0) << 8) / h1;
			final int y_a1 = y_a >> 8;
			int y_d = y_a & 0xFF;

			int y_b1 = y_a1 + 1;
			if (y_b1 >= h0) {
				y_b1 = h0 - 1;
				y_d = 0;
			}

			// get the two affected lines:
			src_i.getRGB(buffer1, 0, w0, 0, y_a1, w0, 1);
			if (y_d != 0)
				src_i.getRGB(buffer2, 0, w0, 0, y_b1, w0, 1);

			for (int x = 0; x < w1; x++) {
				// get this and the next point
				int x_a = ((x * w0) << 8) / w1;
				int x_a1 = x_a >> 8;
				int x_d = x_a & 0xFF;

				int x_b1 = x_a1 + 1;
				if (x_b1 >= w0) {
					x_b1 = w0 - 1;
					x_d = 0;
				}

				// interpolate in x
				int c12, c34;
				int c1 = buffer1[x_a1];
				int c3 = buffer1[x_b1];

				// interpolate in y:
				if (y_d == 0) {
					c12 = c1;
					c34 = c3;
				} else {
					int c2 = buffer2[x_a1];
					int c4 = buffer2[x_b1];

					final int v1 = y_d & 0xFF;
					final int a_c2_RB = c1 & 0x00FF00FF;
					final int a_c2_AG_org = c1 & 0xFF00FF00;

					final int b_c2_RB = c3 & 0x00FF00FF;
					final int b_c2_AG_org = c3 & 0xFF00FF00;

					c12 = (a_c2_AG_org + ((((c2 >>> 8) & 0x00FF00FF) - (a_c2_AG_org >>> 8)) * v1)) & 0xFF00FF00
							| (a_c2_RB + ((((c2 & 0x00FF00FF) - a_c2_RB) * v1) >> 8)) & 0x00FF00FF;
					c34 = (b_c2_AG_org + ((((c4 >>> 8) & 0x00FF00FF) - (b_c2_AG_org >>> 8)) * v1)) & 0xFF00FF00
							| (b_c2_RB + ((((c4 & 0x00FF00FF) - b_c2_RB) * v1) >> 8)) & 0x00FF00FF;
				}

				// final result
				final int v1 = x_d & 0xFF;
				final int c2_RB = c12 & 0x00FF00FF;

				final int c2_AG_org = c12 & 0xFF00FF00;
				dst[index1++] = (c2_AG_org + ((((c34 >>> 8) & 0x00FF00FF) - (c2_AG_org >>> 8)) * v1)) & 0xFF00FF00
						| (c2_RB + ((((c34 & 0x00FF00FF) - c2_RB) * v1) >> 8)) & 0x00FF00FF;
			}
		}
		return Image.createRGBImage(dst, w1, h1, true);
	}

	public static int hsvToRgb(int h, int s, int v) {
		int r, g, b;

		// Ensure hue is between 0 and 359
		h = h % 360;
		if (h < 0) h += 360;

		// Normalize s and v to be between 0 and 255
		s = Math.min(Math.max(s, 0), 255);
		v = Math.min(Math.max(v, 0), 255);

		int region = h / 60;
		int remainder = (h % 60) * 255 / 60;

		int p = (v * (255 - s)) / 255;
		int q = (v * (255 - (s * remainder) / 255)) / 255;
		int t = (v * (255 - (s * (255 - remainder)) / 255)) / 255;

		switch (region) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			default:
				r = v;
				g = p;
				b = q;
				break;
		}
		return (r << 16) | (g << 8) | b;
	}

	public static void hashtablePutWithLimit(Hashtable ht, Vector keys, Object key, Object value, int limit) {
		if (!ht.containsKey(key) && ht.size() >= limit) {
			Object firstKey = keys.elementAt(0);
			ht.remove(firstKey);
			keys.removeElementAt(0);
		}
		ht.put(key, value);
		keys.addElement(key);
	}

	public static String stringToLength(String str, int length) {
		return (str.length() >= length && length > 3) ? str.substring(0, length - 3) + "..." : str;
	}

	public static String stringToWidth(String str, Font font, int area) {
        if (font.stringWidth(str) < area) return str;

        area -= font.stringWidth("...");
        // Reduce string length until it fits in the area
        while (font.stringWidth(str) >= area && str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str + "...";
	}

	public static byte[] stringToBytes(String str) {
		try {
			return str.getBytes("UTF-8");
		}
		catch (Exception e) {
			return str.getBytes();
		}
	}

	public static String bytesToString(byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		}
		catch (Exception e) {
			return new String(bytes);
		}
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (Exception e) {}
	}

	/**
	 * Reads a file's contents from the JAR into a string.
	 * @param name File name
	 * @return String representation of the file's entire contents (UTF-8)
	 * @throws Exception Failed to open file, e.g. it doesn't exist
	 */
	public static String readFile(String name) throws Exception {
		InputStream is = new Object().getClass().getResourceAsStream(name);
		DataInputStream dis = new DataInputStream(is);
		StringBuffer buf = new StringBuffer();

		int ch;
		while ((ch = dis.read()) != -1) {
			buf.append((char) ch);
		}

		String result = buf.toString();
		try {
			return new String(result.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return result;
		}
	}

	public static void setOrAddRecord(RecordStore rms, int index, String data) throws Exception {
		setOrAddRecord(rms, index, stringToBytes(data));
	}

	public static void setOrAddRecord(RecordStore rms, int index, byte[] data) throws Exception {
		if (rms.getNumRecords() >= index) {
			rms.setRecord(index, data, 0, data.length);
		} else {
			rms.addRecord(data, 0, data.length);
		}
	}

	public static void closeRecordStore(RecordStore rms) {
		try {
			rms.closeRecordStore();
		}
		catch (Exception e) {}
	}

	// https://github.com/gtrxAC/discord-j2me/pull/5/commits/193c63f6a00b8e24da7a3582e9d1a92522f9940e
	public static byte[] readBytes(InputStream inputStream, int initialSize, int bufferSize, int expandSize) throws IOException {
		if (initialSize <= 0) initialSize = bufferSize;
		byte[] buf = new byte[initialSize];
		int count = 0;
		byte[] readBuf = new byte[bufferSize];
		int readLen;
		while ((readLen = inputStream.read(readBuf)) != -1) {
			if(count + readLen > buf.length) {
				byte[] newbuf = new byte[count + expandSize];
				System.arraycopy(buf, 0, newbuf, 0, count);
				buf = newbuf;
			}
			System.arraycopy(readBuf, 0, buf, count, readLen);
			count += readLen;
		}
		if(buf.length == count) {
			return buf;
		}
		byte[] res = new byte[count];
		System.arraycopy(buf, 0, res, 0, count);
		return res;
	}

	/**
	 * Split RGB color to its components.
	 * @param color RGB color value
	 * @return An array of three integers where the first is the red value of the given color, second green, and third blue.
	 */
	public static int[] splitRGB(int color) {
		return new int[] {
			(color & 0x00FF0000) >> 16,
			(color & 0x0000FF00) >> 8,
			(color & 0x000000FF)
		};
	}

	public static int contrast(int color, int compare) {
		int[] colorSplit = splitRGB(color);
		int[] compareSplit = splitRGB(compare);

		return
			Math.abs(colorSplit[0] - compareSplit[0]) +
			Math.abs(colorSplit[1] - compareSplit[1]) +
			Math.abs(colorSplit[2] - compareSplit[2]);
	}

	/**
	 * Get which of the colors (A or B) has a higher contrast against the 'compare' color. Alpha is disregarded.
	 */
	public static int higherContrast(int a, int b, int compare) {
		if (contrast(b, compare) > contrast(a, compare)) return b;
		return a;
	}
	
	/**
	 * Blend colors A and B. Alpha is disregarded.
	 * @param a First RGB color value to be blended
	 * @param b Second RGB color value to be blended
	 * @param aRatio The ratio of A to B in increments of 10%, for example, with aRatio = 7, the resulting color will be a blend of 70% A and 30% B.
	 * @return The blended RGB color value
	 */
	public static int blend(int a, int b, int aRatio) {
		int[] as = splitRGB(a);
		int[] bs = splitRGB(b);

		int bRatio = 10 - aRatio;
		int cR = (as[0]*aRatio/10 + bs[0]*bRatio/10) & 0xFF;
		int cG = (as[1]*aRatio/10 + bs[1]*bRatio/10) & 0xFF;
		int cB = (as[2]*aRatio/10 + bs[2]*bRatio/10) & 0xFF;

		return (cR << 16) | (cG << 8) | cB;
	}

//#ifdef SYMBIAN
	public static final boolean isSymbian93;
//#endif

//#ifdef NOKIA_UI_SUPPORT
	public static final boolean supportsNokiaUINotifs;
//#endif

//#ifdef SAMSUNG_FULL
	// List of Samsung phones which have a 480p screen resolution and have the Java runtime used by Bada and the Samsung feature phone OS
	// These phones have a bug where canvases have a tiny font size until they are reopened (see MainMenu)
	// This bug is confirmed on the Jet S8000 and Wave S8500
	private static final String[] SAMSUNG_FONT_BUG_LIST = {
		"S8000", "S8003", "S8500", "S8530", "S8600", "M210S", "M8910"
	};
	public static final boolean hasSamsungFontBug;
	public static final boolean noPointerEventsBug;
//#endif

//#ifdef SYMBIAN
	// List of Symbian phones that have a touchscreen (i.e. no physical softkeys) but have a d-pad
	// Source: lpcwiki
	private static final String[] SYMBIAN_TOUCH_WITH_KEYS_LIST = {
		"NokiaC6-00", "NokiaE6-", "NokiaE7-", "Nokia702T", "NokiaN97", "SonyEricssonU8"
	};

	public static final boolean isTouch;
	public static final boolean isFullTouch;
//#endif

//#ifdef PIGLER_SUPPORT
	public static final boolean supportsPigler;
//#endif

	public static final boolean supportsFileConn;

	public static final boolean isS40;

	public static int fontSize;

	static {
		String platform = System.getProperty("microedition.platform");
		if (platform == null) platform = "";

//#ifdef SYMBIAN
		isSymbian93 = platform.indexOf("sw_platform_version=3.2") != -1;

		isTouch = platform.indexOf("sw_platform_version=5.") != -1;
		isFullTouch = isTouch && indexOfAny(platform, SYMBIAN_TOUCH_WITH_KEYS_LIST, 0) == -1;
//#endif

//#ifdef NOKIA_UI_SUPPORT
		supportsNokiaUINotifs = (System.getProperty("com.nokia.mid.ui.softnotification") != null);
//#endif

//#ifdef SAMSUNG_FULL
		hasSamsungFontBug = indexOfAny(platform, SAMSUNG_FONT_BUG_LIST, 0) != -1;
		noPointerEventsBug = platform.indexOf("S7350") == -1;
//#endif

//#ifdef PIGLER_SUPPORT
		supportsPigler = System.getProperty("org.pigler.api.version") != null;
//#endif

		supportsFileConn = System.getProperty("microedition.io.file.FileConnection.version") != null;

		isS40 =
//#ifdef S40
			true;
//#else
//#ifdef NOT_S40
			false;
//#else
			checkClass("javax.microedition.midlet.MIDletProxy") || checkClass("com.nokia.mid.impl.isa.jam.Jam");
//#endif
//#endif


		fontSize = Font.getDefaultFont().getHeight();
//#ifdef SAMSUNG_FULL
		if (hasSamsungFontBug) fontSize *= 2;
//#endif
	}
	
	// https://github.com/shinovon/JTube/blob/master/src/jtube/PlatformUtils.java
	public static boolean checkClass(String s) {
		try {
			Class.forName(s);
			return true;
		}
		catch (Exception e) {}

		return false;
	}
}
