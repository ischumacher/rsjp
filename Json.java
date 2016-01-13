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
	public static void main(String[] args) {
		// EXAMPLE JSON
		String str = "{\n"
				  + "    \"glossary\": {\n"
				  + "        \"title\": \"example glossary\",\n"
				  + "		\"GlossDiv\": {\n"
				  + "            \"title\": \"S\",\n"
				  + "			\"GlossList\": {\n"
				  + "                \"GlossEntry\": {\n"
				  + "                    \"ID\": \"SGML\",\n"
				  + "					\"SortAs\": \"SGML\",\n"
				  + "					\"GlossTerm\": \"Standard Generalized Markup Language\",\n"
				  + "					\"Acronym\": \"SGML\",\n"
				  + "					\"Abbrev\": \"ISO 8879:1986\",\n"
				  + "					\"GlossDef\": {\n"
				  + "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n"
				  + "						\"GlossSeeAlso\": [\"GML\", \"XML\"]\n"
				  + "                    },\n"
				  + "					\"GlossSee\": \"markup\"\n"
				  + "                }\n"
				  + "            }\n"
				  + "        }\n"
				  + "    }\n"
				  + "}";
		Map<String, Object> jso = Json.parseJSON(str);
		System.out.println(jso);
		System.out.println(Json.get(jso, "glossary", "GlossDiv", "GlossList", "GlossEntry", "Acronym"));
	}
	public static Map<String, Object> parseJSON(Reader r) throws IOException {
		return parseJSON(new ReaderCharacterIterator(r));
	}
	public static Map<String, Object> parseJSON(String str) {
		return parseJSON(new StringCharacterIterator(str));
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
			Object obj = m.get(keys[i]);
			if (obj instanceof Map) {
				m = (Map<String, Object>) m.get(keys[i]);
			} else {
				return null;
			}
		}
		return m.get(keys[keys.length - 1]);
	}
	static Map<String, Object> _parseJSON(Iterator<Character> iter) {
		Map<String, Object> map = new LinkedHashMap<String, Object>() {
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
	public static String toJSONString(Map<String, Object> map) {
		return _toJSONString(map, 0);
	}
	private static final String ONETAB = "   ";
	private static String _toJSONString(Map<String, Object> map, int level) {
		StringBuilder sb = new StringBuilder();
		StringBuilder tab = new StringBuilder();
		for (int i = 0; i < level; ++i) {
			tab.append(ONETAB);
		}
		if (level > 0) {
			sb.append('\n').append(tab);
		}
		sb.append('{');
		String sep = "";
		for (Map.Entry<String, Object> me : map.entrySet()) {
			sb.append(sep);
			sb.append('\n');
			sb.append(tab);
			sb.append(ONETAB);
			sb.append(me.getKey());
			sb.append(": ");
			Object v = me.getValue();
			if (v instanceof Map) {
				sb.append(_toJSONString((Map<String, Object>) v, ++level));
			} else {
				sb.append(safeValue((String) me.getValue()));
			}
			sep = ", ";
		}
		sb.append('\n');
		sb.append(tab);
		sb.append("}");
		return sb.toString();
	}
	private static String safeValue(String value) {
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
}
