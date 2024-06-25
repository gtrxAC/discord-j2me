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

    // https://github.com/shinovon/JTube/blob/master/src/tube42/lib/imagelib/ImageUtils.java#L87
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
}