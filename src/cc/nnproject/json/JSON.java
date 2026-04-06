/*
Copyright (c) 2021-2026 Arman Jussupgaliyev

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
package cc.nnproject.json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * JSON Library compatible with CLDC 1.1 & JDK 1.1<br>
 * Usage:<p><code>JSONObject obj = JSON.getObject(str);</code></p>
 * <b>When using proguard, add</b>: <p><code>-optimizations !code/simplification/object</code>
 * @author Shinovon
 * @version 2.5
 */
public final class JSON {

	// parse all nested elements once
	static final boolean parse_members = false;
	
	// identation for formatting
	static final String FORMAT_TAB = "  ";
	
	// used internally for storing nulls, get methods must return real null
	public static final Object json_null = new Object();
	
	public static final Boolean TRUE = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);
	
	public static AbstractJSON get(String text) throws JSONException {
		if (text == null || text.length() <= 1)
			throw new JSONException("Empty text");
		char c = text.charAt(0);
		if (c != '{' && c != '[')
			throw new JSONException("Not JSON object or array");
		return (AbstractJSON) parseJSON(text, 0, text.length());
	}

	public static JSONObject getObject(String text) throws JSONException {
		if (text == null || text.length() <= 1)
			throw new JSONException("Empty text");
		if (text.charAt(0) != '{')
			throw new JSONException("Not JSON object");
		return (JSONObject) parseJSON(text, 0, text.length());
	}

	public static JSONArray getArray(String text) throws JSONException {
		if (text == null || text.length() <= 1)
			throw new JSONException("Empty text");
		if (text.charAt(0) != '[')
			throw new JSONException("Not JSON array");
		return (JSONArray) parseJSON(text, 0, text.length());
	}

	static Object getJSON(Object obj) throws JSONException {
		if (obj instanceof Hashtable) {
			return new JSONObject((Hashtable) obj);
		}
		if (obj instanceof Vector) {
			return new JSONArray((Vector) obj);
		}
		if (obj == null) {
			return json_null;
		}
		return obj;
	}

	static Object parseJSON(String str, int start, int end) throws JSONException {
		char first = str.charAt(start);
		while (first <= ' ') {
			first = str.charAt(++start);
		}
		
		char last = str.charAt(end - 1);
		while (last <= ' ') {
			last = str.charAt((--end) - 1);
		}
		switch (first) {
		case '"': { // string
			if (last != '"')
				throw new JSONException("Unexpected end of text");
			if (str.indexOf('\\', start) < end) {
				StringBuffer sb = new StringBuffer();
				int i = start + 1;
				end--;
				// parse escaped chars in string
				loop: {
					while (i < end) {
						char c = str.charAt(i);
						switch (c) {
						case '\\': {
							next: {
								replace: {
									if (end < i + 1) {
										sb.append(c);
										break loop;
									}
									char c1 = str.charAt(i + 1);
									switch (c1) {
									case 'u':
										i+=2;
										sb.append((char) Integer.parseInt(
												new String(new char[] {str.charAt(i++), str.charAt(i++), str.charAt(i++), str.charAt(i++)}),
												16));
										break replace;
									case 'x':
										i+=2;
										sb.append((char) Integer.parseInt(
												new String(new char[] {str.charAt(i++), str.charAt(i++)}),
												16));
										break replace;
									case 'n':
										sb.append('\n');
										i+=2;
										break replace;
									case 'r':
										sb.append('\r');
										i+=2;
										break replace;
									case 't':
										sb.append('\t');
										i+=2;
										break replace;
									case 'f':
										sb.append('\f');
										i+=2;
										break replace;
									case 'b':
										sb.append('\b');
										i+=2;
										break replace;
									case '\"':
									case '\'':
									case '\\':
									case '/':
										i+=2;
										sb.append((char) c1);
										break replace;
									default:
										break next;
									}
								}
								break;
							}
							sb.append(c);
							i++;
							break;
						}
						default:
							sb.append(c);
							i++;
						}
					}
				}
				str = sb.toString();
				sb = null;
				return str;
			}
			return str.substring(1 + start, end - 1);
		}
		case '{': // JSON object or array
		case '[': {
			boolean object = first == '{';
			if (object ? last != '}' : last != ']')
				throw new JSONException("Unexpected end of text");
			int brackets = 0;
			int i = start + 1;
			char nextDelimiter = object ? ':' : ',';
			boolean escape = false;
			String key = null;
			Object res = object ? (Object) new JSONObject() : (Object) new JSONArray();
			
			for (int splIndex; i < end - 1; i = splIndex + 1) {
				// skip all spaces
				for (; i < end - 1 && str.charAt(i) <= ' '; i++);

				splIndex = i;
				boolean quote = false;
				for (; splIndex < end - 1 && (quote || brackets > 0 || str.charAt(splIndex) != nextDelimiter); splIndex++) {
					char c = str.charAt(splIndex);
					if (!escape) {
						if (c == '\\') {
							escape = true;
						} else if (c == '"') {
							quote = !quote;
						}
					} else escape = false;
	
					if (!quote) {
						if (c == '{' || c == '[') {
							brackets++;
						} else if (c == '}' || c == ']') {
							brackets--;
						}
					}
				}

				// fail if unclosed quotes or brackets left
				if (quote || brackets > 0) {
					throw new JSONException("Corrupted JSON");
				}

				if (object && key == null) {
					key = str.substring(i + 1, str.lastIndexOf('"', splIndex));
					nextDelimiter = ',';
				} else if (i == splIndex) {
					throw new JSONException("Empty value");
				} else {
					char c = str.charAt(i);
					Object value;
					if (parse_members || (c != '{' && c != '[')) {
						value = parseJSON(str, i, splIndex);
					} else {
						// leave value as JSONString to parse it later
						value = new String[] {(String) str.substring(i, splIndex)};
					}
					if (object) {
						((JSONObject) res)._put(key, value);
						key = null;
						nextDelimiter = ':';
					} else if (splIndex > i) {
						((JSONArray) res).addElement(value);
					}
				}
			}
			return res;
		}
		case 'n': // null
			return json_null;
		case 't': // true
			return TRUE;
		case 'f': // false
			return FALSE;
		default: // number
			int l = end - start;
			if ((first >= '0' && first <= '9') || first == '-') {
				try {
					// hex
					if (l > 1 && first == '0' && str.charAt(start + 1) == 'x') {
						if (l > 9) // str.length() > 10
							return new Long(Long.parseLong(str.substring(start + 2, end), 16));
						return new Integer(Integer.parseInt(str.substring(start + 2, end), 16));
					}
					str = str.substring(start, end);
					// decimal
					if (str.indexOf('.') != -1 || str.indexOf('E') != -1 || str.indexOf('e') != -1 || "-0".equals(str))
						return new Integer(0);
					if (first == '-') l--;
					if (l > 8) // (str.length() - (str.charAt(0) == '-' ? 1 : 0)) >= 10
						return new Long(Long.parseLong(str));
					return new Integer(Integer.parseInt(str));
				} catch (Exception e) {}
			}
			throw new JSONException("Couldn't be parsed: " + str);
//			return new String[]{str};
		}
	}
	
	// compatibility
	static Object parseJSON(String str) throws JSONException {
		return parseJSON(str, 0, str.length());
	}
	
	public static boolean isNull(Object obj) {
		return obj == json_null || obj == null;
	}

	// transforms string for exporting
	static String escape_utf8(String s) {
		int len = s.length();
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < len) {
			char c = s.charAt(i);
			switch (c) {
			case '"':
			case '\\':
				sb.append("\\").append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				if (c < 32 || c > 1103 || (c >= '\u0080' && c < '\u00a0')) {
					String u = Integer.toHexString(c);
					sb.append("\\u");
					for (int z = u.length(); z < 4; z++) {
						sb.append('0');
					}
					sb.append(u);
				} else {
					sb.append(c);
				}
			}
			i++;
		}
		return sb.toString();
	}

	static int getInt(Object o) throws JSONException {
		try {
			if (o instanceof String[])
				return Integer.parseInt(((String[]) o)[0]);
			if (o instanceof Integer)
				return ((Integer) o).intValue();
			if (o instanceof Long)
				return (int) ((Long) o).longValue();
		} catch (Throwable e) {}
		throw new JSONException("Cast to int failed: " + o);
	}

	static long getLong(Object o) throws JSONException {
		try {
			if (o instanceof String[])
				return Long.parseLong(((String[]) o)[0]);
			if (o instanceof Integer)
				return ((Integer) o).longValue();
			if (o instanceof Long)
				return ((Long) o).longValue();
		} catch (Throwable e) {}
		throw new JSONException("Cast to long failed: " + o);
	}

	public static void writeString(OutputStream out, String s) throws IOException {
		int len = s.length();
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			switch (c) {
			case '"':
			case '\\':
				out.write((byte) '\\');
				out.write((byte) c);
				break;
			case '\b':
				out.write((byte) '\\');
				out.write((byte) 'b');
				break;
			case '\f':
				out.write((byte) '\\');
				out.write((byte) 'f');
				break;
			case '\n':
				out.write((byte) '\\');
				out.write((byte) 'n');
				break;
			case '\r':
				out.write((byte) '\\');
				out.write((byte) 'r');
				break;
			case '\t':
				out.write((byte) '\\');
				out.write((byte) 't');
				break;
			default:
				if (c < 32 || c > 255) {
					out.write((byte) '\\');
					out.write((byte) 'u');
					String u = Integer.toHexString(c);
					for (int z = u.length(); z < 4; z++) {
						out.write((byte) '0');
					}
					out.write(u.getBytes());
				} else {
					out.write((byte) c);
				}
			}
		}
	}

}
