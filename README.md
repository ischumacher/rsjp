# RSJP (Ridiculously simple JSON parser)

- Single file solution
- Allows unquoted keys and values
- Key/Value maps and Arrays are both maps. Arrays are maps with incremented number keys.
- Everything else is a String (i.e. integers, reals)
- Ignores preamble (until finds a '{' or '[' character)
- Ignores postamble (everything after last closing '}' or ']')
- Easily extract data from deep hierachies

Example:

    public static void main(String[] args) throws IOException, ScriptException {
		String str = "var a = {\n"
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
		Map<String, Object> jso = Json.parseJSON(Json.getIterator(str));
		System.out.println(jso);
		System.out.println(Json.get(jso, "glossary", "GlossDiv", "GlossList", "GlossEntry", "GlossDef", "GlossSeeAlso", "1"));
		System.out.println(Json.get(jso, "glossary", "GlossDiv", "GlossList"));
    }

Ouput:

    {glossary: {title: "example glossary", GlossDiv: {title: S, GlossList: {GlossEntry: {ID: SGML, SortAs: SGML, GlossTerm: "Standard Generalized Markup Language", Acronym: SGML, Abbrev: "ISO 8879:1986", GlossDef: {para: "A meta-markup language, used to create markup languages such as DocBook.", GlossSeeAlso: {0: GML, 1: XML}}, GlossSee: markup}}}}}
    XML
    {GlossEntry: {ID: SGML, SortAs: SGML, GlossTerm: "Standard Generalized Markup Language", Acronym: SGML, Abbrev: "ISO 8879:1986", GlossDef: {para: "A meta-markup language, used to create markup languages such as DocBook.", GlossSeeAlso: {0: GML, 1: XML}}, GlossSee: markup}}
