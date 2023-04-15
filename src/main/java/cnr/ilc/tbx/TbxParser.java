package cnr.ilc.tbx;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import cnr.ilc.rut.CountingInputStream;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.rut.Concept;
import cnr.ilc.rut.RutException;
import cnr.ilc.rut.Word;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TbxParser implements ParserInterface {
	private Document document;
	private ConceptEntry conceptEntryParser = new ConceptEntry();
	private Map<String, String> lexicons = new HashMap<>();
	private Map<String,Object> metadata = new HashMap<>();
	private Collection<Concept> concepts = new ArrayList<>();

    public TbxParser(InputStream inputStream) throws Exception {
		CountingInputStream countingInputStream = new CountingInputStream(inputStream);

		document = parseTbx(countingInputStream);
		document.getDocumentElement().normalize();
		Element tbx = (Element) document.getElementsByTagName("tbx").item(0);

		metadata.put("fileSize", countingInputStream.getCount());
		metadata.put("fileType", "tbx");
		metadata.put("variant", tbx.getAttribute("type"));
	}
	
	static private Document parseTbx(InputStream inputStream) throws RutException {
		InputSource inputSource = new InputSource(inputStream);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputSource);
			return document;
		}
		catch(Exception e) {
			throw new RutException(e);
		}
	}

	private void parseConcepts() {
		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			Element conceptEntry = (Element) conceptEntries.item(i);
			Concept concept = conceptEntryParser.parseConceptEntry(conceptEntry);
			concepts.add(concept);
			lexicons.putAll(conceptEntryParser.getLexicons());
		}		
	}

	private JSONObject convertConceptsToJSONObject(Collection<Concept> concepts) {
		JSONObject jsonConcepts = new JSONObject();


		for(Concept concept: concepts) {
			Map<String, JSONArray> languages = new HashMap<>();
			
			for (Word word: concept.words) {
				JSONArray wordsByLanguage = languages.get(word.language);
				if (wordsByLanguage == null) {
					wordsByLanguage = new JSONArray();
					languages.put(word.language, wordsByLanguage);
				}
				wordsByLanguage.add(word.canonicalForm.text);
			}
			jsonConcepts.put(concept.id, languages);
		}

		return jsonConcepts;
	}

	@Override
	public void parse() throws Exception {
		parseConcepts();

		JSONArray jsonLanguages = new JSONArray();
		Set<String> languages = conceptEntryParser.getLanguages();
		jsonLanguages.addAll(languages);

		metadata.put("languages", jsonLanguages);
		metadata.put("concepts", convertConceptsToJSONObject(concepts));
		metadata.put("numberOfTerms", conceptEntryParser.numberOfTerms);
		metadata.put("numberOfConcepts", concepts.size());
	}

	@Override
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public Map<String, String> getLexicons() {
		return lexicons;
	}

	@Override
	public Collection<Concept> getConcepts() {
		return concepts;
	}

	@Override
	public Collection<Word> getWords() {
		return new ArrayList<Word>();
	}

	@Override
	public String getRdfType() {
		return "ontolex:LexicalEntry";
	}
}
