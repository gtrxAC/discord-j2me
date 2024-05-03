/*
Copyright (c) 2021-2024 Arman Jussupgaliyev

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

import java.util.Hashtable;
import java.util.Vector;

/**
 * JSON Library compatible with CLDC 1.1 & JDK 1.1<br>
 * Usage:<p><code>JSONObject obj = JSON.getObject(str);</code></p>
 * <b>Use with proguard argument</b>: <p><code>-optimizations !code/simplification/object</code>
 * @author Shinovon
 * @version 2.2
 */
public final class JSON {

	// parse all nested elements once
	static final boolean parse_members = false;
	
	// identation for formatting
	static final String FORMAT_TAB = "  ";
	
	// used for storing nulls, get methods must return real null
	public static final Object json_null = new Object();
	
	public static final Boolean TRUE = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);
	
	public static AbstractJSON get(String text) throws JSONException {
		if (text == null || text.length() <= 1)
			throw new JSONException("Empty text");
		char c = text.charAt(0);
		if (c != '{' && c != '[')
			throw new JSONException("Not JSON object or array");
		return (AbstractJSON) parseJSON(text.trim());
	}

	public static JSONObject getObject(String text) throws JSONException {
		if (text == null || text.length() <= 1)
			throw new JSONException("Empty text");
		if (text.charAt(0) != '{')
			throw new JSONException("Not JSON object");
		return (JSONObject) parseJSON(text.trim());
	}

	public static JSONArray getArray(String text) throws JSONException {
		if (text == null || text.length() <= 1)
			throw new JSONException("Empty text");
		if (text.charAt(0) != '[')
			throw new JSONException("Not JSON array");
		return (JSONArray) parseJSON(text.trim());
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

	static Object parseJSON(String str) throws JSONException {
		char first = str.charAt(0);
		int length = str.length() - 1;
		char last = str.charAt(length);
		switch(first) {
		case '"': { // string
			if (last != '"')
				throw new JSONException("Unexpected end of text");
			if(str.indexOf('\\') != -1) {
				char[] chars = str.substring(1, length).toCharArray();
				str = null;
				int l = chars.length;
				StringBuffer sb = new StringBuffer();
				int i = 0;
				// parse escaped chars in string
				loop: {
					while (i < l) {
						char c = chars[i];
						switch (c) {
						case '\\': {
							next: {
								replace: {
									if (l < i + 1) {
										sb.append(c);
										break loop;
									}
									char c1 = chars[i + 1];
									switch (c1) {
									case 'u':
										i+=2;
										sb.append((char) Integer.parseInt(
												new String(new char[] {chars[i++], chars[i++], chars[i++], chars[i++]}),
												16));
										break replace;
									case 'x':
										i+=2;
										sb.append((char) Integer.parseInt(
												new String(new char[] {chars[i++], chars[i++]}),
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
			return str.substring(1, length);
		}
		case '{': // JSON object or array
		case '[': {
			boolean object = first == '{';
			if (object ? last != '}' : last != ']')
				throw new JSONException("Unexpected end of text");
			int brackets = 0;
			int i = 1;
			char nextDelimiter = object ? ':' : ',';
			boolean escape = false;
			String key = null;
			Object res = object ? (Object) new JSONObject() : (Object) new JSONArray();
			
			for (int splIndex; i < length; i = splIndex + 1) {
				// skip all spaces
				for (; i < length - 1 && str.charAt(i) <= ' '; i++);

				splIndex = i;
				boolean quote = false;
				for (; splIndex < length && (quote || brackets > 0 || str.charAt(splIndex) != nextDelimiter); splIndex++) {
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
					key = str.substring(i, splIndex);
					key = key.substring(1, key.length() - 1);
					nextDelimiter = ',';
				} else {
					Object value = str.substring(i, splIndex).trim();
					// don't check length because if value is empty, then exception is going to be thrown anyway
					char c = ((String) value).charAt(0);
					// leave JSONString as value to parse it later, if its object or array and nested parsing is disabled
					value = parse_members || (c != '{' && c != '[') ?
							parseJSON((String) value) : new JSONString((String) value);
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
			if ((first >= '0' && first <= '9') || first == '-') {
				try {
					// hex
					if (length > 1 && first == '0' && str.charAt(1) == 'x') {
						if (length > 9) // str.length() > 10
							return new Long(Long.parseLong(str.substring(2), 16));
						return new Integer(Integer.parseInt(str.substring(2), 16));
					}
					// decimal
					if (str.indexOf('.') != -1 || str.indexOf('E') != -1 || "-0".equals(str))
						return new Double(Double.parseDouble(str));
					if (first == '-') length--;
					if (length > 8) // (str.length() - (str.charAt(0) == '-' ? 1 : 0)) >= 10
						return new Long(Long.parseLong(str));
					return new Integer(Integer.parseInt(str));
				} catch (Exception e) {}
			}
			throw new JSONException("Couldn't be parsed: " + str);
//			return new JSONString(str);
		}
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

	static double getDouble(Object o) throws JSONException {
		try {
			if (o instanceof JSONString)
				return Double.parseDouble(((JSONString) o).str);
			if (o instanceof Integer)
				return ((Integer) o).intValue();
			if (o instanceof Long)
				return ((Long) o).longValue();
			if (o instanceof Double)
				return ((Double) o).doubleValue();
		} catch (Throwable e) {}
		throw new JSONException("Cast to double failed: " + o);
	}

	static int getInt(Object o) throws JSONException {
		try {
			if (o instanceof JSONString)
				return Integer.parseInt(((JSONString) o).str);
			if (o instanceof Integer)
				return ((Integer) o).intValue();
			if (o instanceof Long)
				return (int) ((Long) o).longValue();
			if (o instanceof Double)
				return ((Double) o).intValue();
		} catch (Throwable e) {}
		throw new JSONException("Cast to int failed: " + o);
	}

	static long getLong(Object o) throws JSONException {
		try {
			if (o instanceof JSONString)
				return Long.parseLong(((JSONString) o).str);
			if (o instanceof Integer)
				return ((Integer) o).longValue();
			if (o instanceof Long)
				return ((Long) o).longValue();
			if (o instanceof Double)
				return ((Double) o).longValue();
		} catch (Throwable e) {}
		throw new JSONException("Cast to long failed: " + o);
	}

}
