/*
Copyright (c) 2021-2025 Arman Jussupgaliyev

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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * JSON Library compatible with CLDC 1.1 & JDK 1.1<br>
 * Usage:<p><code>JSONObject obj = getObject(str);</code></p>
 * <b>Use with proguard argument</b>: <p><code>-optimizations !code/simplification/object</code>
 * @author Shinovon
 * @version 2.4 (Shrinked)
 */
public class JSONObject {

	protected Hashtable table;

	public JSONObject() {
		table = new Hashtable();
	}

	public JSONObject(Hashtable table) {
		this.table = table;
	}

	/**
	 * @deprecated Compatibility with org.json
	 */
	public JSONObject(String str) {
		table = parseObject(str).table; // FIXME
	}
	
	public Object get(String name) {
		try {
			if (has(name)) {
				Object o = table.get(name);
				if (o instanceof String[])
					table.put(name, o = parseJSON(((String[]) o)[0]));
				if (o == json_null)
					return null;
				return o;
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {}
		throw new RuntimeException("JSON: No value for name: " + name);
	}
	
	// unused methods should be removed by proguard shrinking
	
	public Object get(String name, Object def) {
		if (!has(name)) return def;
		try {
			return get(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public Object getNullable(String name) {
		return get(name, null);
	}
	
	public String getString(String name) {
		Object o = get(name);
		if (o == null || o instanceof String)
			return (String) o;
		return String.valueOf(o);
	}
	
	public String getString(String name, String def) {
		try {
			Object o = get(name, def);
			if (o == null || o instanceof String)
				return (String) o;
			return String.valueOf(o);
		} catch (Exception e) {
			return def;
		}
	}
	
	public String getNullableString(String name) {
		return getString(name, null);
	}
	
	public JSONObject getObject(String name) {
		try {
			return (JSONObject) get(name);
		} catch (ClassCastException e) {
			throw new RuntimeException("JSON: Not object: " + name);
		}
	}
	public JSONObject getObject(String name, JSONObject def) {
		if (has(name)) {
			try {
				return (JSONObject) get(name);
			} catch (Exception e) {
			}
		}
		return def;
	}
	
	public JSONObject getNullableObject(String name) {
		return getObject(name, null);
	}
	
	public JSONArray getArray(String name) {
		try {
			return (JSONArray) get(name);
		} catch (ClassCastException e) {
			throw new RuntimeException("JSON: Not array: " + name);
		}
	}
	
	public JSONArray getArray(String name, JSONArray def) {
		if (has(name)) {
			try {
				return (JSONArray) get(name);
			} catch (Exception e) {
			}
		}
		return def;
	}
	
	
	public JSONArray getNullableArray(String name) {
		return getArray(name, null);
	}
	
	public int getInt(String name) {
		return getInt(get(name));
	}
	
	public int getInt(String name, int def) {
		if (!has(name)) return def;
		try {
			return getInt(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(String name) {
		return getLong(get(name));
	}

	public long getLong(String name, long def) {
		if (!has(name)) return def;
		try {
			return getLong(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(String name) {
		Object o = get(name);
		if (o == TRUE) return true;
		if (o == FALSE) return false;
		if (o instanceof Boolean) return ((Boolean) o).booleanValue();
		if (o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if (s.equals("true")) return true;
			if (s.equals("false")) return false;
		}
		throw new RuntimeException("JSON: Not boolean: " + o);
	}

	public boolean getBoolean(String name, boolean def) {
		if (!has(name)) return def;
		try {
			return getBoolean(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean isNull(String name) {
		if (!has(name))
			throw new RuntimeException("JSON: No value for name: " + name);
		return table.get(name) == json_null;
	}
	
	public void put(String name, Object obj) {
		table.put(name, getJSON(obj));
	}
	
	public void put(String name, JSONObject json) {
		table.put(name, json);
	}
	
	public void put(String name, String s) {
		table.put(name, s == null ? json_null : s);
	}

	public void put(String name, int i) {
		table.put(name, new Integer(i));
	}

	public void put(String name, long l) {
		table.put(name, new Long(l));
	}

	public void put(String name, boolean b) {
		table.put(name, new Boolean(b));
	}
	
	public boolean hasValue(Object object) {
		return table.contains(getJSON(object));
	}
	
	// hasKey
	public boolean has(String name) {
		return table.containsKey(name);
	}
	
	public void clear() {
		table.clear();
	}
	
	public void remove(String name) {
		table.remove(name);
	}
	
	public int size() {
		return table.size();
	}
	
	public boolean isEmpty() {
		return table.isEmpty();
	}
	
	public String toString() {
		return build();
	}
	
	public boolean equals(Object obj) {
		return this == obj || super.equals(obj) || similar(obj);
	}
	
	public boolean similar(Object obj) {
		if (!(obj instanceof JSONObject)) {
			return false;
		}
		if (table.equals(((JSONObject) obj).table)) {
			return true;
		}
		int size = size();
		if (size != ((JSONObject)obj).size()) {
			return false;
		}
		Enumeration keys = table.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Object a = get(key);
			Object b = ((JSONObject)obj).get(key);
			if (a == b) {
				continue;
			}
			if (a == null) {
				return false;
			}
			if (a instanceof JSONObject) {
				if (!((JSONObject)a).similar(b)) {
					return false;
				}
			} else if (!a.equals(b)) {
				return false;
			}
		}
		return true;
	}

	public String build() {
		if (size() == 0)
			return "{}";
		StringBuffer s = new StringBuffer("{");
		Enumeration keys = table.keys();
		while (true) {
			String k = (String) keys.nextElement();
			s.append("\"").append(k).append("\":");
			Object v = table.get(k);
			if (v instanceof JSONObject) {
				s.append(((JSONObject) v).build());
			} else if (v instanceof JSONArray) {
				s.append(((JSONArray) v).build());
			} else if (v instanceof String) {
				s.append("\"").append(escape_utf8((String) v)).append("\"");
			} else if (v instanceof String[]) {
				s.append(((String[]) v)[0]);
			} else if (v == json_null) {
				s.append((String) null);
			} else {
				s.append(v);
			}
			if (!keys.hasMoreElements()) {
				break;
			}
			s.append(",");
		}
		s.append("}");
		return s.toString();
	}
	
	public void write(OutputStream out) throws IOException {
		out.write((byte) '{');
		if (size() == 0) {
			out.write((byte) '}');
			return;
		}
		Enumeration keys = table.keys();
		while (true) {
			String k = (String) keys.nextElement();
			out.write((byte) '"');
			writeString(out, k.toString());
			out.write((byte) '"');
			out.write((byte) ':');
			Object v = table.get(k);
			if (v instanceof JSONObject) {
				((JSONObject) v).write(out);
			} else if (v instanceof JSONArray) {
				((JSONArray) v).write(out);
			} else if (v instanceof String) {
				out.write((byte) '"');
				writeString(out, (String) v);
				out.write((byte) '"');
			} else if (v instanceof String[]) {
				out.write((((String[]) v)[0]).getBytes("UTF-8"));
			} else if (v == json_null) {
				out.write((byte) 'n');
				out.write((byte) 'u');
				out.write((byte) 'l');
				out.write((byte) 'l');
			} else {
				out.write(String.valueOf(v).getBytes("UTF-8"));
			}
			if (!keys.hasMoreElements()) {
				break;
			}
			out.write((byte) ',');
		}
		out.write((byte) '}');
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
					String u = Integer.toHexString(c);
					out.write((byte) '\\');
					out.write((byte) 'u');
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

	public Enumeration keys() {
		return table.keys();
	}

	public JSONArray keysAsArray() {
		JSONArray array = new JSONArray(table.size());
		Enumeration keys = table.keys();
		while (keys.hasMoreElements()) {
			array.addElement(keys.nextElement());
		}
		return array;
	}
	
	/**
	 * @deprecated Use {@link JSONObject#toTable()} instead
	 */
	public Hashtable getTable() {
		return table;
	}

	public Hashtable toTable() {
		Hashtable copy = new Hashtable(table.size());
		Enumeration keys = table.keys();
		while (keys.hasMoreElements()) {
			String k = (String) keys.nextElement();
			Object v = table.get(k);
			if (v instanceof String[])
				table.put(k, v = parseJSON(((String[]) v)[0]));
			if (v instanceof JSONObject) {
				v = ((JSONObject) v).toTable();
			} else if (v instanceof JSONArray) {
				v = ((JSONArray) v).toVector();
			}
			copy.put(k, v);
		}
		return copy;
	}

	// JSON

	// parse all nested elements once
	static final boolean parse_members = false;
	
	// identation for formatting
	static final String FORMAT_TAB = "  ";
	
	// used for storing nulls, get methods must return real null
	public static final Object json_null = new Object();
	
	public static final Boolean TRUE = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);

	public static JSONObject parseObject(String text) {
		if (text == null || text.length() <= 1)
			throw new RuntimeException("JSON: Empty text");
		if (text.charAt(0) != '{')
			throw new RuntimeException("JSON: Not JSON object: " + text);
		return (JSONObject) parseJSON(text);
	}

	public static JSONArray parseArray(String text) {
		if (text == null || text.length() <= 1)
			throw new RuntimeException("JSON: Empty text");
		if (text.charAt(0) != '[')
			throw new RuntimeException("JSON: Not JSON array");
		return (JSONArray) parseJSON(text);
	}

	static Object getJSON(Object obj) {
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

	public static Object parseJSON(String str) {
		char first = str.charAt(0);
		int length;
		char last = str.charAt(length = str.length() - 1);
		if (last <= ' ')
			last = (str = str.trim()).charAt(length = str.length() - 1);
		switch(first) {
		case '"': { // string
			if (last != '"')
				throw new RuntimeException("JSON: Unexpected end of text");
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
				throw new RuntimeException("JSON: Unexpected end of text");
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
					throw new RuntimeException("JSON: Corrupted JSON");
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
							parseJSON((String) value) : new String[] {(String) value};
					if (object) {
						((JSONObject) res).table.put(key, value);
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
						return str;
					if (first == '-') length--;
					if (length > 8) // (str.length() - (str.charAt(0) == '-' ? 1 : 0)) >= 10
						return new Long(Long.parseLong(str));
					return new Integer(Integer.parseInt(str));
				} catch (Exception e) {}
			}
			throw new RuntimeException("JSON: Couldn't be parsed: " + str);
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

	static int getInt(Object o) {
		try {
			if (o instanceof String[])
				return Integer.parseInt(((String[]) o)[0]);
			if (o instanceof Integer)
				return ((Integer) o).intValue();
			if (o instanceof Long)
				return (int) ((Long) o).longValue();
		} catch (Throwable e) {}
		throw new RuntimeException("JSON: Cast to int failed: " + o);
	}

	public static long getLong(Object o) {
		try {
			if (o instanceof String[])
				return Long.parseLong(((String[]) o)[0]);
			if (o instanceof Integer)
				return ((Integer) o).longValue();
			if (o instanceof Long)
				return ((Long) o).longValue();
		} catch (Throwable e) {}
		throw new RuntimeException("JSON: Cast to long failed: " + o);
	}

	public String format(int l) {
		int size = size();
		if (size == 0)
			return "{}";
		String t = "";
		for (int i = 0; i < l; i++) {
			t = t.concat("  ");
		}
		String t2 = t.concat("  ");
		StringBuffer s = new StringBuffer("{\n");
		s.append(t2);
		Enumeration keys = table.keys();
		int i = 0;
		while (keys.hasMoreElements()) {
			String k = (String) keys.nextElement();
			s.append("\"").append(k).append("\": ");
			Object v = get(k);
			if (v instanceof String[])
				table.put(k, v = parseJSON(((String[]) v)[0]));
			if (v instanceof JSONObject) {
				s.append(((JSONObject) v).format(l + 1));
			} else if (v instanceof JSONArray) {
				s.append(((JSONArray) v).format(l + 1));
			} else if (v instanceof String) {
				s.append("\"").append(escape_utf8((String) v)).append("\"");
			} else if (v == json_null) {
				s.append((String) null);
			} else {
				s.append(v);
			}
			i++;
			if (i < size) {
				s.append(",\n").append(t2);
			}
		}
		if (l > 0) {
			s.append("\n").append(t).append("}");
		} else {
			s.append("\n}");
		}
		return s.toString();
	}
}