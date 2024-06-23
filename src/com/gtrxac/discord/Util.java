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

    static private int[] circleData;
    static private int cachedCircleSize;

    public static void drawCircleCutout(Graphics g, int x, int y, int size) {
        if (circleData == null || cachedCircleSize != size) {
            Image circleImage = Image.createImage(size, size);
            Graphics cg = circleImage.getGraphics();
            cg.setColor(0);
            cg.fillArc(0, 0, size, size, 0, 360);

            circleData = new int[size*size];
            circleImage.getRGB(circleData, 0, size, 0, 0, size, size);
        }

        for (int sy = 0; sy < size; sy++) {
            for (int sx = 0; sx < size; sx++) {
                if (circleData[sy*size + sx] == 0xFF000000) continue;
                g.fillRect(x + sx, y + sy, 1, 1);
            }
        }
    }
}