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

import java.util.Enumeration;
import java.util.Hashtable;

public class JSONObject extends AbstractJSON {

	protected Hashtable table;

	public JSONObject() {
		table = new Hashtable();
	}

	/**
	 * @deprecated Doesn't adapt nested elements
	 */
	public JSONObject(Hashtable table) {
		this.table = table;
	}

	/**
	 * @deprecated Compatibility with org.json
	 */
	public JSONObject(String str) {
		table = JSON.getObject(str).table; // FIXME
	}
	
	public Object get(String name) throws JSONException {
		try {
			if (has(name)) {
				Object o = table.get(name);
				if (o instanceof JSONString)
					table.put(name, o = JSON.parseJSON(((JSONString) o).str));
				if (o == JSON.json_null)
					return null;
				return o;
			}
		} catch (JSONException e) {
			throw e;
		} catch (Exception e) {
		}
		throw new JSONException("No value for name: " + name);
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
	
	public String getString(String name) throws JSONException {
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
	
	public JSONObject getObject(String name) throws JSONException {
		try {
			return (JSONObject) get(name);
		} catch (ClassCastException e) {
			throw new JSONException("Not object: " + name);
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
	
	public JSONArray getArray(String name) throws JSONException {
		try {
			return (JSONArray) get(name);
		} catch (ClassCastException e) {
			throw new JSONException("Not array: " + name);
		}
	}
	
	public JSONArray getArray(String name, JSONArray def) throws JSONException {
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
	
	public int getInt(String name) throws JSONException {
		return JSON.getInt(get(name));
	}
	
	public int getInt(String name, int def) {
		if (!has(name)) return def;
		try {
			return getInt(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(String name) throws JSONException {
		return JSON.getLong(get(name));
	}

	public long getLong(String name, long def) {
		if (!has(name)) return def;
		try {
			return getLong(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public double getDouble(String name) throws JSONException {
		return JSON.getDouble(get(name));
	}

	public double getDouble(String name, double def) {
		if (!has(name)) return def;
		try {
			return getDouble(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(String name) throws JSONException {
		Object o = get(name);
		if (o == JSON.TRUE) return true;
		if (o == JSON.FALSE) return false;
		if (o instanceof Boolean) return ((Boolean) o).booleanValue();
		if (o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if (s.equals("true")) return true;
			if (s.equals("false")) return false;
		}
		throw new JSONException("Not boolean: " + o);
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
			throw new JSONException("No value for name: " + name);
		return table.get(name) == JSON.json_null;
	}
	
	/**
	 * @deprecated
	 */
	public void put(String name, Object obj) {
		table.put(name, JSON.getJSON(obj));
	}
	
	public void put(String name, AbstractJSON json) {
		table.put(name, json);
	}
	
	public void put(String name, String s) {
		table.put(name, s);
	}

	public void put(String name, int i) {
		table.put(name, new Integer(i));
	}

	public void put(String name, long l) {
		table.put(name, new Long(l));
	}

	public void put(String name, double d) {
		table.put(name, new Double(d));
	}

	public void put(String name, boolean b) {
		table.put(name, new Boolean(b));
	}
	
	public boolean hasValue(Object object) {
		return table.contains(JSON.getJSON(object));
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
			if (a instanceof AbstractJSON) {
				if (!((AbstractJSON)a).similar(b)) {
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
			if (v instanceof AbstractJSON) {
				s.append(((AbstractJSON) v).build());
			} else if (v instanceof String) {
				s.append("\"").append(JSON.escape_utf8((String) v)).append("\"");
			} else if (v == JSON.json_null) {
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

	protected String format(int l) {
		int size = size();
		if (size == 0)
			return "{}";
		String t = "";
		for (int i = 0; i < l; i++) {
			t = t.concat(JSON.FORMAT_TAB);
		}
		String t2 = t.concat(JSON.FORMAT_TAB);
		StringBuffer s = new StringBuffer("{\n");
		s.append(t2);
		Enumeration keys = table.keys();
		int i = 0;
		while (keys.hasMoreElements()) {
			String k = (String) keys.nextElement();
			s.append("\"").append(k).append("\": ");
			Object v = get(k);
			if (v instanceof JSONString)
				table.put(k, v = JSON.parseJSON(((JSONString) v).str));
			if (v instanceof AbstractJSON) {
				s.append(((AbstractJSON) v).format(l + 1));
			} else if (v instanceof String) {
				s.append("\"").append(JSON.escape_utf8((String) v)).append("\"");
			} else if (v == JSON.json_null) {
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
			if (v instanceof JSONString)
				table.put(k, v = JSON.parseJSON(((JSONString) v).str));
			if (v instanceof JSONObject) {
				v = ((JSONObject) v).toTable();
			} else if (v instanceof JSONArray) {
				v = ((JSONArray) v).toVector();
			}
			copy.put(k, v);
		}
		return copy;
	}
	
	void _put(String name, Object obj) {
		table.put(name, obj);
	}
	
	// TODO: Enumeration elements()
	// TODO: String keyOf(Object)

}
