package ian.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Json {
	private static final String ONETAB = "   ";
	static Map<String, Object> _parseJSON(Iterator<Character> iter) {
		final Map<String, Object> map = new LinkedHashMap<String, Object>() {
			private static final long serialVersionUID = -3192438622581031914L;
			@Override
			public String toString() {
				return toJSONString(this);
			}
		};
		char ch;
		int count = 0;
		KeyValue kv = new KeyValue(count);
		StringBuilder sb = new StringBuilder();
		while (iter.hasNext()) {
			ch = iter.next();
			switch (ch) {
			case '{':
			case '[':
				kv.value = _parseJSON(iter);
				map.put(kv.key, kv.value);
				kv = new KeyValue(++count);
				break;
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				// ignore
				break;
			case '"':
			case '\'':
				sb.append(parseQuotedString(ch, iter));
				break;
			case '\\':
				sb.append(iter.next());
				break;
			case ':':
				kv.key = sb.toString().trim();
				sb = new StringBuilder();
				break;
			case ',':
				if (sb.toString().length() > 0) {
					kv.value = sb.toString();
					sb = new StringBuilder();
					map.put(kv.key, kv.value);
					kv = new KeyValue(++count);
				}
				break;
			case '}':
			case ']':
				if (sb.toString().length() > 0) {
					kv.value = sb.toString();
					map.put(kv.key, kv.value);
				}
				return map;
			default:
				sb.append(ch);
			}
		}
		return map;
	}
	@SuppressWarnings("unchecked")
	private static String _toJSONString(Map<String, Object> map, int level, boolean compliant) {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder tab = new StringBuilder();
		for (int i = 0; i < level; ++i) {
			tab.append(ONETAB);
		}
		if (level > 0) {
			sb.append('\n').append(tab);
		}
		sb.append('{');
		String sep = "";
		for (final Map.Entry<String, Object> me : map.entrySet()) {
			sb.append(sep);
			sb.append('\n');
			sb.append(tab);
			sb.append(ONETAB);
			sb.append(safeValue(me.getKey(), compliant));
			sb.append(": ");
			final Object v = me.getValue();
			if (v instanceof Map) {
				sb.append(_toJSONString((Map<String, Object>) v, ++level, compliant));
			} else {
				if (v == null) {
					sb.append("null");
				} else {
					sb.append(safeValue(v.toString(), compliant));
				}
			}
			sep = ", ";
		}
		sb.append('\n');
		sb.append(tab);
		sb.append("}");
		return sb.toString();
	}
	public static boolean getBoolean(Map<String, Object> map, String... keys) {
		return Boolean.parseBoolean(getString(map, keys));
	}
	public static double getDouble(Map<String, Object> map, String... keys) {
		return Double.parseDouble(getString(map, keys));
	}
	public static int getInteger(Map<String, Object> map, String... keys) {
		return Integer.parseInt(getString(map, keys));
	}
	public static long getLong(Map<String, Object> map, String... keys) {
		return Long.parseLong(getString(map, keys));
	}
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getMap(Map<String, Object> map, String... keys) {
		Map<String, Object> m = map;
		for (int i = 0; i < keys.length - 1; ++i) {
			final Object obj = m.get(keys[i]);
			if (obj instanceof Map) {
				m = (Map<String, Object>) m.get(keys[i]);
			} else {
				return null;
			}
		}
		return (Map<String, Object>) m.get(keys[keys.length - 1]);
	}
	@SuppressWarnings("unchecked")
	public static Object getMapOrString(Map<String, Object> map, String... keys) {
		Map<String, Object> m = map;
		for (int i = 0; i < keys.length - 1; ++i) {
			final Object obj = m.get(keys[i]);
			if (obj instanceof Map) {
				m = (Map<String, Object>) m.get(keys[i]);
			} else {
				return null;
			}
		}
		return m.get(keys[keys.length - 1]);
	}
	@SuppressWarnings("unchecked")
	public static String getString(Map<String, Object> map, String... keys) {
		Map<String, Object> m = map;
		for (int i = 0; i < keys.length - 1; ++i) {
			final Object obj = m.get(keys[i]);
			if (obj instanceof Map) {
				m = (Map<String, Object>) m.get(keys[i]);
			} else {
				return null;
			}
		}
		return (String) m.get(keys[keys.length - 1]);
	}
	public static void main(String[] args) {
		// EXAMPLE JSON
		final String str = "{basic:{0:{index:0, license:'to be determined', 'a b c': 'a b c', image:\"img_left\","
				+ "description:'black square hard smooth heavy', category:alien}}";
		final Map<String, Object> jso = Json.parseJSON(str);
		System.out.println("**Compact JSON**");
		System.out.println(jso);
		System.out.println();
		System.out.println("**Compliant JSON**");
		System.out.println(Json.toJSONString(jso, true));
		System.out.println();
		System.out.println("json.basic.0.description = " + Json.getString(jso, "basic", "0", "description"));
	}
	public static Map<String, Object> parseJSON(Iterator<Character> iter) {
		while (iter.hasNext()) {
			final char ch = iter.next();
			if (ch == '{' || ch == '[') {
				break;
			}
		}
		return _parseJSON(iter);
	}
	public static Map<String, Object> parseJSON(Reader r) throws IOException {
		return parseJSON(new ReaderCharacterIterator(r));
	}
	public static Map<String, Object> parseJSON(String str) {
		return parseJSON(new StringCharacterIterator(str));
	}
	static String parseQuotedString(char endChar, Iterator<Character> iter) {
		final StringBuilder sb = new StringBuilder();
		char ch = iter.next();
		while (ch != endChar) {
			if (ch == '\\') {
				ch = iter.next();
			}
			sb.append(ch);
			ch = iter.next();
		}
		return sb.toString();
	}
	private static String safeValue(String value, boolean compliant) {
		final char[] ch = value.toCharArray();
		boolean quote = false;
		if (compliant) {
			try {
				if (!value.equals(null)) {
					Double.parseDouble(value);
				}
			} catch (final NumberFormatException e) {
				quote = true;
			}
		}
		for (int i = 0; i < ch.length; ++i) {
			switch (ch[i]) {
			case ':':
			case '\'':
			case ' ':
			case '\n':
			case '\t':
			case '\r':
			case ',':
			case '\"':
			case '{':
			case '[':
			case '}':
			case ']':
				quote = true;
				break;
			}
		}
		if (!quote) {
			return value;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append('\"');
		for (int i = 0; i < ch.length; ++i) {
			if (ch[i] == '"') {
				sb.append('\'');
			}
			sb.append(ch[i]);
		}
		sb.append('\"');
		return sb.toString();
	}
	public static String toJSONString(Map<String, Object> map) {
		return toJSONString(map, false);
	}
	public static String toJSONString(Map<String, Object> map, boolean compliant) {
		return _toJSONString(map, 0, compliant);
	}
}

class KeyValue {
	public String key;
	public Object value;
	public KeyValue(int count) {
		key = Integer.toString(count);
	}
	public KeyValue(String key, Object value) {
		this.key = key;
		this.value = value;
	}
}

class ReaderCharacterIterator implements Iterator<Character> {
	private int ch;
	private final Reader r;
	public ReaderCharacterIterator(Reader r) throws IOException {
		this.r = r;
		this.ch = r.read();
	}
	@Override
	public boolean hasNext() {
		return ch > -1;
	}
	@Override
	public Character next() {
		try {
			final char c = (char) ch;
			ch = r.read();
			return c;
		} catch (final IOException ex) {
			Logger.getLogger(ReaderCharacterIterator.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}

class StringCharacterIterator implements Iterator<Character> {
	private final char[] ch;
	int pos = -1;
	public StringCharacterIterator(String str) {
		this.ch = str.toCharArray();
	}
	@Override
	public boolean hasNext() {
		return pos < ch.length - 1;
	}
	@Override
	public Character next() {
		++pos;
		return ch[pos];
	}
}
