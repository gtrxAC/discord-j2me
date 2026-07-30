package fi.gtrxac.bluewap;

import javax.microedition.io.*;
import javax.microedition.lcdui.Font;
import javax.microedition.rms.*;
import java.io.*;
import java.util.*;

public class Util {
	public static byte[] stringToBytes(String str) {
		return stringToBytes(str, null);
	}

	public static byte[] stringToBytes(String str, String charset) {
		if (charset == null || charset.length() == 0) {
			charset = "UTF-8";
		}
		try {
			return str.getBytes(charset);
		}
		catch (Exception e) {
			return str.getBytes();
		}
	}

	public static String bytesToString(byte[] bytes) {
		return bytesToString(bytes, null);
	}

	public static String bytesToString(byte[] bytes, String charset) {
		if (charset == null || charset.length() == 0) {
			charset = "UTF-8";
		}
		try {
			return new String(bytes, charset);
		}
		catch (Exception e) {
			return new String(bytes);
		}
	}

	public static String getCharsetFromContentType(String contentType) {
		if (contentType == null) return null;

		int charsetIndex = contentType.toLowerCase().indexOf("charset=");
		if (charsetIndex == -1) return null;

		int start = charsetIndex + "charset=".length();
		int end = contentType.length();
		int semicolonIndex = contentType.indexOf(';', start);
		if (semicolonIndex != -1) end = semicolonIndex;

		String charset = contentType.substring(start, end).trim();
		if (charset.length() > 1 && charset.startsWith("\"") && charset.endsWith("\"")) {
			charset = charset.substring(1, charset.length() - 1);
		}
		return charset;
	}

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

	public static String[] split(String str, String delimiter) {
		if (str == null || str.length() == 0) {
			return new String[0];
		}
		Vector parts = _split(str, delimiter);
		String[] result = new String[parts.size()];
		parts.copyInto(result);
		return result;
	}

	public static Vector splitVec(String str, String delimiter) {
		if (str == null || str.length() == 0) {
			return new Vector();
		}
		return _split(str, delimiter);
	}

	private static Vector _split(String str, String delimiter) {
		Vector parts = new Vector();
		int start = 0;
		int index;
		
		while ((index = str.indexOf(delimiter, start)) != -1) {
			parts.addElement(str.substring(start, index));
			start = index + delimiter.length();
		}
		
		// Add the last part
		parts.addElement(str.substring(start));

		return parts;
	}

    public static String sanitizeWml(String text) {
        text = Util.replace(text, "&", "&amp;");
        text = Util.replace(text, "'", "&apos;");
        text = Util.replace(text, "\"", "&quot;");
        text = Util.replace(text, "<", "&lt;");
        return Util.replace(text, ">", "&gt;");
    }

    private static Font cachedFont;
    private static int cachedMinWidth;
    private static int cachedSpaceWidth;

    /**
     * Get array of text lines to draw (word wrap)
     * Input string should not begin or end with a space or line break (trim the string if needed)
     */
    public static String[] wordWrap(String text, int maxWidth, Font font) {
        if (text == null || text.length() == 0 || text.equals(" ")) {
            return new String[0];
        }
        if (cachedFont != font) {
            cachedFont = font;
            cachedMinWidth = font.charWidth('W') + 2;
            cachedSpaceWidth = font.charWidth(' ');
        }
        if (maxWidth < cachedMinWidth) {
            return new String[0];
        }
        
		// text = replace(text, "\t", "  ");  // comment out if your string will never have tabs
        // text = replace(text, "\r", "");  // comment out if your string will never have carriage returns
        Vector lines = new Vector();

        int lineEnd = text.indexOf('\n');
        if (lineEnd != -1) {
            int lineBegin = 0;
            do {
                lines.addElement(text.substring(lineBegin, lineEnd));
                lineBegin = lineEnd + 1;
                lineEnd = text.indexOf('\n', lineBegin);
            }
            while (lineEnd != -1);

            lines.addElement(text.substring(lineBegin));
        } else {
            lines.addElement(text);
        }

        Vector out = new Vector();
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            int lineLength = line.length();

            if (font.stringWidth(line) > maxWidth) {
                // this line is too long for one screen line, so split it into multiple lines based on word boundaries
                int pos = 0;
                int availableWidth = maxWidth;
                StringBuffer outLine = new StringBuffer();

                loop: while (true) {
                    // get the next word (from cursor position to the next space character, or to the end of the line)
                    int nextSpace = line.indexOf(' ', pos);
                    if (nextSpace == -1) {
                        nextSpace = lineLength;
                    }
                    String thisWord = line.substring(pos, nextSpace);
                    int thisWordWidth = font.stringWidth(thisWord);

                    if (thisWordWidth < availableWidth) {
                        // word fits on the current line
                        outLine.append(thisWord);
                        availableWidth -= thisWordWidth;
                    } else {
                        // word doesn't fit on current line -> finish this line
                        if (outLine.length() != 0) {
                            out.addElement(outLine.toString());
                            outLine.setLength(0);
                        }

                        if (thisWordWidth < maxWidth) {
                            // word fits on one line -> add the word to the next line
                            outLine.append(thisWord);
                            availableWidth = maxWidth - thisWordWidth;
                        } else {
                            // word is too long to fit on one line -> split the word
                            for (int c = thisWord.length() - 1; c >= 0; c--) {
                                String splitWord = thisWord.substring(0, c);
    
                                if (font.stringWidth(splitWord) < maxWidth) {
                                    out.addElement(splitWord);
                                    pos += c;
                                    break;
                                }
                            }
                            availableWidth = maxWidth;
                            continue;
                        }
                    }

                    // skip past this word
                    pos += thisWord.length();

                    while (true) {
                        if (pos >= lineLength) break loop;

                        // add space(s) to the end of the current line (a line will never begin with a space)
                        if (line.charAt(pos) != ' ') break;
                        outLine.append(' ');
                        availableWidth -= cachedSpaceWidth;
                        pos++;
                    }
                }
                // add the last remaining line to the output if needed
                if (outLine.length() != 0) {
                    out.addElement(outLine.toString());
                }
            } else {
                // this whole line fits on one screen line, so add it as-is
                out.addElement(line);
            }
        }
        String[] arr = new String[out.size()];
        out.copyInto(arr);
        return arr;
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

	public static byte[] readBytes(InputStream inputStream) throws IOException {
		return readBytes(inputStream, 0, 1024, 2048);
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

	// https://github.com/phd051199/MIDPlay/blob/main/src/Utils.java#L125

  	private static final String HEX_DIGITS = "0123456789ABCDEF";

	public static String urlEncode(String text) {
		if (text == null) {
		return "";
		}

		try {
		byte[] bytes = text.getBytes("UTF-8");
		StringBuffer result = new StringBuffer(bytes.length + (bytes.length >> 1));

		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i] & 0xFF;
			char c = (char) b;

			if (c == ' ') {
			result.append('+');
			} else if (isUrlSafeCharacter(c)) {
			result.append(c);
			} else {
			result
				.append('%')
				.append(HEX_DIGITS.charAt((b >> 4) & 0xF))
				.append(HEX_DIGITS.charAt(b & 0xF));
			}
		}
		return result.toString();
		} catch (UnsupportedEncodingException e) {
		return "";
		}
	}

	private static boolean isUrlSafeCharacter(char c) {
		return (c >= 'A' && c <= 'Z')
			|| (c >= 'a' && c <= 'z')
			|| (c >= '0' && c <= '9')
			|| c == '-'
			|| c == '_'
			|| c == '.'
			|| c == '~';
	}

    public static String urlDecode(String value) {
        if (value == null) {
            return "";
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '+') {
                bytes.write(32);
            }
			else if (c == '%' && i + 2 < value.length()) {
                int hi = hexDigit(value.charAt(i + 1));
                int lo = hexDigit(value.charAt(i + 2));
                if (hi >= 0 && lo >= 0) {
                    bytes.write((hi << 4) | lo);
                    i += 2;
                } else {
                    bytes.write((byte) c);
                }
            } else {
                bytes.write((byte) c);
            }
        }

        try {
            return new String(bytes.toByteArray(), "UTF-8");
        } catch (Exception e) {
            return new String(bytes.toByteArray());
        }
    }

    private static int hexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        return -1;
    }
	
	public static boolean checkClass(String s) {
		try {
			Class.forName(s);
			return true;
		}
		catch (Throwable e) {}

		return false;
	}

    public static String removeDuplicateWhitespace(String text) {
        boolean atWhitespace = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
                if (atWhitespace) {
                    text = text.substring(0, i) + text.substring(i + 1);
                } else {
                    text = text.substring(0, i) + " " + text.substring(i + 1);
                    atWhitespace = true;
                }
            } else {
                atWhitespace = false;
            }
        }
        return text;
    }

    public static String trimLeft(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
                continue;
            }
            return text.substring(i);
        }
        return "";
    }

    public static String trimRight(String text) {
        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
                continue;
            }
            return text.substring(0, i + 1);
        }
        return "";
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

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (Exception e) {}
	}

	public static final boolean isJ2MELoader =
		"The Android Project".equals(System.getProperty("java.vendor"));
}