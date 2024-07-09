package com.gtrxac.discord;

import java.util.Vector;
import javax.microedition.lcdui.*;
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
        int imgAspect = imgW*100 / imgH;
        int maxAspect = maxW*100 / maxH;
        int width, height;

        if (imgW <= maxW && imgH <= maxH) {
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

    public static String fileSizeToString(int size) {
        if (size >= 1000000) return "" + size/1000000 + " MB";
        if (size >= 1000) return "" + size/1000 + " kB";
        return "" + size + " bytes";
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
}