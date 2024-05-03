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
import java.util.Vector;

public class JSONArray extends AbstractJSON {
	
	protected Object[] elements;
	protected int count;
	
	public JSONArray() {
		elements = new Object[10];
	}
	
	public JSONArray(int size) {
		elements = new Object[size];
	}

	/**
	 * @deprecated Doesn't adapt nested elements
	 */
	public JSONArray(Vector vector) {
		elements = new Object[count = vector.size()];
		vector.copyInto(elements);
	}

	/**
	 * @deprecated Compatibility with org.json
	 */
	public JSONArray(String str) {
		JSONArray tmp = JSON.getArray(str); // FIXME
		elements = tmp.elements;
		count = tmp.count;
	}

	public Object get(int index) throws JSONException {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		try {
			Object o = elements[index];
			if (o instanceof JSONString)
				o = elements[index] = JSON.parseJSON(((JSONString) o).str);
			if (o == JSON.json_null)
				return null;
			return o;
		} catch (Exception e) {
		}
		throw new JSONException("No value at " + index);
	}
	
	// unused methods should be removed by proguard shrinking
	
	public Object get(int index, Object def) {
		try {
			return get(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public Object getNullable(int index) {
		return get(index, null);
	}
	
	public String getString(int index) throws JSONException {
		Object o = get(index);
		if (o == null || o instanceof String)
			return (String) o;
		return String.valueOf(o);
	}
	
	public String getString(int index, String def) {
		try {
			Object o = get(index);
			if (o == null || o instanceof String)
				return (String) o;
			return String.valueOf(o);
		} catch (Exception e) {
			return def;
		}
	}
	
	public String getNullableString(int index) {
		return getString(index, null);
	}
	
	public JSONObject getObject(int index) throws JSONException {
		try {
			return (JSONObject) get(index);
		} catch (ClassCastException e) {
			throw new JSONException("Not object at " + index);
		}
	}
	
	public JSONObject getObject(int index, JSONObject def) {
		try {
			return getObject(index);
		} catch (Exception e) {
		}
		return def;
	}
	
	public JSONObject getNullableObject(int index) {
		return getObject(index, null);
	}
	
	public JSONArray getArray(int index) throws JSONException {
		try {
			return (JSONArray) get(index);
		} catch (ClassCastException e) {
			throw new JSONException("Not array at " + index);
		}
	}
	
	public JSONArray getArray(int index, JSONArray def) {
		try {
			return getArray(index);
		} catch (Exception e) {
		}
		return def;
	}
	
	public JSONArray getNullableArray(int index) {
		return getArray(index, null);
	}
	
	public int getInt(int index) throws JSONException {
		return JSON.getInt(get(index));
	}
	
	public int getInt(int index, int def) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(int index) throws JSONException {
		return JSON.getLong(get(index));
	}

	public long getLong(int index, long def) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public double getDouble(int index) throws JSONException {
		return JSON.getDouble(get(index));
	}

	public double getDouble(int index, double def) {
		try {
			return getDouble(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(int index) throws JSONException {
		Object o = get(index);
		if (o == JSON.TRUE) return true;
		if (o == JSON.FALSE) return false;
		if (o instanceof Boolean) return ((Boolean) o).booleanValue();
		if (o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if (s.equals("true")) return true;
			if (s.equals("false")) return false;
		}
		throw new JSONException("Not boolean: " + o + " (" + index + ")");
	}

	public boolean getBoolean(int index, boolean def) {
		try {
			return getBoolean(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean isNull(int index) {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		return elements[index] == JSON.json_null;
	}
	
	/**
	 * @deprecated
	 */
	public void add(Object object) {
		if (object == this) throw new JSONException();
		addElement(JSON.getJSON(object));
	}
	
	public void add(AbstractJSON json) {
		if (json == this) throw new JSONException();
		addElement(json);
	}
	
	public void add(String s) {
		addElement(s);
	}
	
	public void add(int i) {
		addElement(new Integer(i));
	}

	public void add(long l) {
		addElement(new Long(l));
	}

	public void add(double d) {
		addElement(new Double(d));
	}
	
	public void add(boolean b) {
		addElement(new Boolean(b));
	}

	/**
	 * @deprecated
	 */
	public void set(int index, Object object) {
		if (object == this) throw new JSONException();
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		elements[index] = JSON.getJSON(object);
	}
	
	public void set(int index, AbstractJSON json) {
		if (json == this) throw new JSONException();
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		elements[index] = json;
	}
	
	public void set(int index, String s) {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		elements[index] = s;
	}
	
	public void set(int index, int i) {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		elements[index] = new Integer(i);
	}

	public void set(int index, long l) {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		elements[index] = new Long(l);
	}

	public void set(int index, double d) {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		elements[index] = new Double(d);
	}
	
	public void set(int index, boolean b) {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		elements[index] = new Boolean(b);
	}
	
	/**
	 * @deprecated
	 */
	public void put(int index, Object object) {
		if (object == this) throw new JSONException();
		insertElementAt(JSON.getJSON(object), index);
	}
	
	public void put(int index, AbstractJSON json) {
		if (json == this) throw new JSONException();
		insertElementAt(json, index);
	}
	
	public void put(int index, String s) {
		insertElementAt(s, index);
	}
	
	public void put(int index, int i) {
		insertElementAt(new Integer(i), index);
	}

	public void put(int index, long l) {
		insertElementAt(new Long(l), index);
	}

	public void put(int index, double d) {
		insertElementAt(new Double(d), index);
	}

	public void put(int index, boolean b) {
		insertElementAt(new Boolean(b), index);
	}
	
	public boolean has(Object object) {
		return _indexOf(JSON.getJSON(object), 0) != -1;
	}
	
	public boolean has(int i) {
		return _indexOf(new Integer(i), 0) != -1;
	}

	public boolean has(long l) {
		return _indexOf(new Long(l), 0) != -1;
	}

	public boolean has(double d) {
		return _indexOf(new Double(d), 0) != -1;
	}
	
	public boolean has(boolean b) {
		return _indexOf(new Boolean(b), 0) != -1;
	}
	
	public int indexOf(Object object) {
		return _indexOf(JSON.getJSON(object), 0);
	}

	public int indexOf(Object object, int index) {
		return _indexOf(JSON.getJSON(object), index);
	}
	
	public void clear() {
		for (int i = 0; i < count; i++) elements[i] = null;
		count = 0;
	}
	
	public boolean remove(Object object) {
		int i = _indexOf(JSON.getJSON(object), 0);
		if (i == -1) return false;
		remove(i);
		return true;
	}
	
	public void remove(int index) {
		if (index < 0 || index >= count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		count--;
		int size = count - index;
		if (size > 0)
			System.arraycopy(elements, index + 1, elements, index, size);
		elements[count] = null;
	}
	
	public int size() {
		return count;
	}
	
	public boolean isEmpty() {
		return count == 0;
	}
	
	public String toString() {
		return build();
	}
	
	public boolean equals(Object obj) {
		return this == obj || super.equals(obj) || similar(obj);
	}
	
	public boolean similar(Object obj) {
		if (!(obj instanceof JSONArray)) {
			return false;
		}
		int size = count;
		if (size != ((JSONArray)obj).count) {
			return false;
		}
		for (int i = 0; i < size; i++) {
			Object a = get(i);
			Object b = ((JSONArray)obj).get(i);
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
		int size = count;
		if (size == 0)
			return "[]";
		StringBuffer s = new StringBuffer("[");
		int i = 0;
		while (i < size) {
			Object v = elements[i];
			if (v instanceof AbstractJSON) {
				s.append(((AbstractJSON) v).build());
			} else if (v instanceof String) {
				s.append("\"").append(JSON.escape_utf8((String) v)).append("\"");
			} else if (v == JSON.json_null) {
				s.append((String) null);
			} else {
				s.append(String.valueOf(v));
			}
			i++;
			if (i < size) {
				s.append(",");
			}
		}
		s.append("]");
		return s.toString();
	}

	protected String format(int l) {
		int size = count;
		if (size == 0)
			return "[]";
		String t = "";
		for (int i = 0; i < l; i++) {
			t = t.concat(JSON.FORMAT_TAB);
		}
		String t2 = t.concat(JSON.FORMAT_TAB);
		StringBuffer s = new StringBuffer("[\n");
		s.append(t2);
		int i = 0;
		while (i < size) {
			Object v = elements[i];
			if (v instanceof JSONString) {
				v = elements[i] = JSON.parseJSON(((JSONString) v).str);
			}
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
			s.append("\n").append(t).append("]");
		} else {
			s.append("\n]");
		}
		return s.toString();
	}

	public Enumeration elements() {
		return new Enumeration() {
			int i = 0;
			
			public boolean hasMoreElements() {
				return i < count;
			}
			
			public Object nextElement() {
				Object o = elements[i];
				if (o instanceof JSONString)
					o = elements[i] = JSON.parseJSON(((JSONString) o).str);
				i++;
				return o == JSON.json_null ? null : o;
			}
		};
	}
	
	public void copyInto(Object[] arr) {
		copyInto(arr, 0, arr.length);
	}

	public void copyInto(Object[] arr, int offset, int length) {
		int i = offset;
		int j = 0;
		while(i < arr.length && j < length && j < size()) {
			arr[i++] = get(j++);
		}
	}

	public Vector toVector() {
		int size = count;
		Vector copy = new Vector(size);
		for (int i = 0; i < size; i++) {
			Object o = elements[i];
			if (o instanceof JSONString)
				o = elements[i] = JSON.parseJSON(((JSONString) o).str);
			if (o instanceof JSONObject) {
				o = ((JSONObject) o).toTable();
			} else if (o instanceof JSONArray) {
				o = ((JSONArray) o).toVector();
			}
			copy.addElement(o);
		}
		return copy;
	}

	void addElement(Object object) {
		if (count == elements.length) grow();
		elements[count++] = object;
	}
	
	private void insertElementAt(Object object, int index) {
		if (index < 0 || index > count) {
			throw new JSONException("Index out of bounds: " + index);
		}
		if (count == elements.length) grow();
		int size = count - index;
		if (size > 0)
			System.arraycopy(elements, index, elements, index + 1, size);
		elements[index] = object;
		count++;
	}
	
	private int _indexOf(Object object, int start) {
		for (int i = start; i < count; i++) {
			if (elements[i] instanceof JSONString)
				elements[i] = JSON.parseJSON(((JSONString) elements[i]).str);
			if (object.equals(elements[i])) return i;
		}
		return -1;
	}
	
	private void grow() {
		Object[] tmp = new Object[elements.length * 2];
		System.arraycopy(elements, 0, tmp, 0, count);
		elements = tmp;
	}

}
