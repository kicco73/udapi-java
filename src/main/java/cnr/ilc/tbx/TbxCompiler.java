package cnr.ilc.tbx;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import cnr.ilc.rut.CountingInputStream;
import cnr.ilc.rut.IdGenerator;
import cnr.ilc.rut.Pair;
import cnr.ilc.rut.BaseCompiler;
import cnr.ilc.rut.Concept;
import cnr.ilc.rut.RutException;
import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.rut.Word;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TbxCompiler extends BaseCompiler {
	private Document document;
	private ConceptEntry conceptEntryParser;
	private Map<String, String> lexicons = new HashMap<>();
	private IdGenerator idGenerator = new IdGenerator();

    public TbxCompiler(InputStream inputStream, SPARQLWriter sparql) throws Exception {
		super(inputStream, sparql);
		conceptEntryParser = new ConceptEntry();

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

	private Collection<Concept> parseConcepts() {
		Collection<Concept> concepts = new ArrayList<>();

		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			Element conceptEntry = (Element) conceptEntries.item(i);
			Concept concept = conceptEntryParser.parseConceptEntry(conceptEntry);
			concepts.add(concept);
			lexicons.putAll(conceptEntryParser.getLexicons());
		}		

		return concepts;
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

	private void addLexicons() {
		for (Entry<String, String> lexicon: lexicons.entrySet()) {
			String language = lexicon.getKey();
			String lexiconFQN = lexicon.getValue();
			lexiconFQN = sparql.createLexicon(lexiconFQN, language);
		}
	}

	private void writeConcepts(Collection<Concept> concepts) {
		addLexicons();
		for (Concept concept: concepts) {
			sparql.addConcept(concept);
			for (Word word: concept.words) {
				sparql.addWord(word, "ontolex:LexicalEntry");
			}
		}
	}

	@Override
	public String toSPARQL() throws Exception {
		Collection<Concept> concepts = parseConcepts();

		writeConcepts(concepts);

		JSONArray jsonLanguages = new JSONArray();
		Set<String> languages = conceptEntryParser.getLanguages();
		jsonLanguages.addAll(languages);

		metadata.put("languages", jsonLanguages);
		metadata.put("concepts", convertConceptsToJSONObject(concepts));
		metadata.put("numberOfTerms", conceptEntryParser.numberOfTerms);
		metadata.put("numberOfConcepts", concepts.size());

		return sparql.toString();
	}
}
