package ian.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class KeyValue {
	public KeyValue(int count) {
		key = Integer.toString(count);
	}
	public KeyValue(String key, Object value) {
		this.key = key;
		this.value = value;
	}
	public String key;
	public Object value;
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
			char c = (char) ch;
			ch = r.read();
			return c;
		} catch (IOException ex) {
			Logger.getLogger(ReaderCharacterIterator.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
public class Json {
	public static Iterator<Character> getIterator(String str) {
		return new StringCharacterIterator(str);
	}
	public static Iterator<Character> getIterator(Reader r) throws IOException {
		return new ReaderCharacterIterator(r);
	}
	public static Map<String, Object> parseJSON(Iterator<Character> iter) {
		while (iter.hasNext()) {
			char ch = iter.next();
			if (ch == '{' || ch == '[') {
				break;
			}
		}
		return _parseJSON(iter);
	}
	public static Object get(Map<String, Object> map, String... keys) {
		Map<String, Object> m = map;
		for (int i = 0; i < keys.length - 1; ++i) {
			m = (Map<String, Object>) m.get(keys[i]);
		}
		return m.get(keys[keys.length - 1]);
	}
	static Map<String, Object> _parseJSON(Iterator<Character> iter) {
		Map<String, Object> map = new LinkedHashMap<String, Object>() {
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				sb.append('{');
				String sep = "";
				for (Map.Entry<String, Object> me : entrySet()) {
					sb.append(sep);
					sb.append(me.getKey());
					sb.append(": ");
					Object v = me.getValue();
					if (v instanceof String) {
						sb.append(safeValue((String) me.getValue()));
					} else {
						sb.append(me.getValue());
					}
					sep = ", ";
				}
				sb.append("}");
				return sb.toString();
			}
			private String safeValue(String value) {
				char[] ch = value.toCharArray();
				boolean quote = false;
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
				StringBuilder sb = new StringBuilder();
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
					//ignore
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
	static String parseQuotedString(char endChar, Iterator<Character> iter) {
		StringBuilder sb = new StringBuilder();
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
}
