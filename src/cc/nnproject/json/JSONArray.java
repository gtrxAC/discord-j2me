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
import java.util.Vector;

public class JSONArray {
	
	protected Object[] elements;
	protected int count;
	
	public JSONArray() {
		elements = new Object[10];
	}
	
	public JSONArray(int size) {
		elements = new Object[size];
	}


	public JSONArray(Vector vector) {
		elements = new Object[count = vector.size()];
		vector.copyInto(elements);
	}

	/**
	 * @deprecated Compatibility with org.json
	 */
	public JSONArray(String str) {
		JSONArray tmp = JSONObject.parseArray(str);
		elements = tmp.elements;
		count = tmp.count;
	}

	public Object get(int index) {
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		try {
			Object o = elements[index];
			if (o instanceof String[])
				o = elements[index] = JSONObject.parseJSON(((String[]) o)[0]);
			if (o == JSONObject.json_null)
				return null;
			return o;
		} catch (Exception e) {
		}
		throw new RuntimeException("JSON: No value at " + index);
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
	
	public String getString(int index) {
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
	
	public JSONObject getObject(int index) {
		try {
			return (JSONObject) get(index);
		} catch (ClassCastException e) {
			throw new RuntimeException("JSON: Not object at " + index);
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
	
	public JSONArray getArray(int index) {
		try {
			return (JSONArray) get(index);
		} catch (ClassCastException e) {
			throw new RuntimeException("JSON: Not array at " + index);
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
	
	public int getInt(int index) {
		return JSONObject.getInt(get(index));
	}
	
	public int getInt(int index, int def) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(int index) {
		return JSONObject.getLong(get(index));
	}

	public long getLong(int index, long def) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(int index) {
		Object o = get(index);
		if (o == JSONObject.TRUE) return true;
		if (o == JSONObject.FALSE) return false;
		if (o instanceof Boolean) return ((Boolean) o).booleanValue();
		if (o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if (s.equals("true")) return true;
			if (s.equals("false")) return false;
		}
		throw new RuntimeException("JSON: Not boolean: " + o + " (" + index + ")");
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
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		return elements[index] == JSONObject.json_null;
	}
	
	public void add(Object object) {
		if (object == this) throw new RuntimeException();
		addElement(JSONObject.getJSON(object));
	}
	
	public void add(JSONObject json) {
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
	
	public void add(boolean b) {
		addElement(new Boolean(b));
	}

	/**
	 * @deprecated
	 */
	public void set(int index, Object object) {
		if (object == this) throw new RuntimeException();
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		elements[index] = JSONObject.getJSON(object);
	}
	
	public void set(int index, JSONObject json) {
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		elements[index] = json;
	}
	
	public void set(int index, String s) {
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		elements[index] = s;
	}
	
	public void set(int index, int i) {
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		elements[index] = new Integer(i);
	}

	public void set(int index, long l) {
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		elements[index] = new Long(l);
	}
	
	public void set(int index, boolean b) {
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
		}
		elements[index] = new Boolean(b);
	}
	
	/**
	 * @deprecated
	 */
	public void put(int index, Object object) {
		if (object == this) throw new RuntimeException();
		insertElementAt(JSONObject.getJSON(object), index);
	}
	
	public void put(int index, JSONObject json) {
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

	public void put(int index, boolean b) {
		insertElementAt(new Boolean(b), index);
	}
	
	public boolean has(Object object) {
		return _indexOf(JSONObject.getJSON(object), 0) != -1;
	}
	
	public boolean has(int i) {
		return _indexOf(new Integer(i), 0) != -1;
	}

	public boolean has(long l) {
		return _indexOf(new Long(l), 0) != -1;
	}
	
	public boolean has(boolean b) {
		return _indexOf(new Boolean(b), 0) != -1;
	}
	
	public int indexOf(Object object) {
		return _indexOf(JSONObject.getJSON(object), 0);
	}

	public int indexOf(Object object, int index) {
		return _indexOf(JSONObject.getJSON(object), index);
	}
	
	public void clear() {
		for (int i = 0; i < count; i++) elements[i] = null;
		count = 0;
	}
	
	public boolean remove(Object object) {
		int i = _indexOf(JSONObject.getJSON(object), 0);
		if (i == -1) return false;
		remove(i);
		return true;
	}
	
	public void remove(int index) {
		if (index < 0 || index >= count) {
			throw new RuntimeException("JSON: Index out of bounds: " + index);
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
			if (a instanceof JSONArray) {
				if (!((JSONArray)a).similar(b)) {
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
			if (v instanceof JSONObject) {
				s.append(((JSONObject) v).build());
			} else if (v instanceof JSONArray) {
				s.append(((JSONArray) v).build());
			} else if (v instanceof String) {
				s.append("\"").append(JSONObject.escape_utf8((String) v)).append("\"");
			} else if (v instanceof String[]){
				s.append(((String[]) v)[0]);
			} else if (v == JSONObject.json_null) {
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

	public String format(int l) {
		int size = count;
		if (size == 0)
			return "[]";
		String t = "";
		for (int i = 0; i < l; i++) {
			t = t.concat("  ");
		}
		String t2 = t.concat("  ");
		StringBuffer s = new StringBuffer("[\n");
		s.append(t2);
		int i = 0;
		while (i < size) {
			Object v = elements[i];
			if (v instanceof String[])
				v = elements[i] = JSONObject.parseJSON(((String[]) v)[0]);
			if (v instanceof JSONObject) {
				s.append(((JSONObject) v).format(l + 1));
			} else if (v instanceof JSONArray) {
				s.append(((JSONArray) v).format(l + 1));
			} else if (v instanceof String) {
				s.append("\"").append(JSONObject.escape_utf8((String) v)).append("\"");
			} else if (v == JSONObject.json_null) {
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
	
	public void write(OutputStream out) throws IOException {
		int size = count;
		out.write((byte) '[');
		if (size == 0) {
			out.write((byte) ']');
			return;
		}
		int i = 0;
		while (i < size) {
			Object v = elements[i];
			if (v instanceof JSONObject) {
				((JSONObject) v).write(out);
			} else if (v instanceof JSONArray) {
				((JSONArray) v).write(out);
			} else if (v instanceof String) {
				out.write((byte) '"');
				JSONObject.writeString(out, (String) v);
				out.write((byte) '"');
			} else if (v instanceof String[]) {
				out.write((((String[]) v)[0]).getBytes("UTF-8"));
			} else if (v == JSONObject.json_null) {
				out.write((byte) 'n');
				out.write((byte) 'u');
				out.write((byte) 'l');
				out.write((byte) 'l');
			} else {
				out.write(String.valueOf(v).getBytes("UTF-8"));
			}
			i++;
			if (i < size) {
				out.write((byte) ',');
			}
		}
		out.write((byte) '}');
	}

	public Enumeration elements() {
		return new Enumeration() {
			int i = 0;
			
			public boolean hasMoreElements() {
				return i < count;
			}
			
			public Object nextElement() {
				Object o = elements[i];
				if (o instanceof String[])
					o = elements[i] = JSONObject.parseJSON(((String[]) o)[0]);
				i++;
				return o == JSONObject.json_null ? null : o;
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
			if (o instanceof String[])
				o = elements[i] = JSONObject.parseJSON(((String[]) o)[0]);
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
			throw new RuntimeException("JSON: Index out of bounds: " + index);
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
			if (elements[i] instanceof String[])
				elements[i] = JSONObject.parseJSON(((String[]) elements[i])[0]);
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