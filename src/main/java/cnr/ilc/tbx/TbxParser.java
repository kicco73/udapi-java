package cnr.ilc.tbx;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.rut.RutException;
import cnr.ilc.rut.utils.CountingInputStream;
import cnr.ilc.rut.utils.Logger;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TbxParser implements ParserInterface, ResourceInterface {
	private Document document;
	private ConceptEntry conceptEntryParser = new ConceptEntry();
	private Collection<GlobalInterface> globals = new ArrayList<>();
	private Collection<ConceptInterface> concepts = new ArrayList<>();
	private Collection<TermInterface> terms = new ArrayList<>();
	private Map<String, String> subjectFields = new LinkedHashMap<>();
	private Map<String, String> lexicons = new LinkedHashMap<>();
	
    public TbxParser(InputStream inputStream) throws Exception {
		CountingInputStream countingInputStream = new CountingInputStream(inputStream);

		document = parseTbx(countingInputStream);
		document.getDocumentElement().normalize();
		Element tbx = (Element) document.getElementsByTagName("tbx").item(0);

		Global global = new Global();
		global.metadata.putInMap("*", countingInputStream.getCount(), "summary", "fileSize");
		global.metadata.putInMap("*", "tbx", "summary", "fileType");
		global.metadata.putInMap("*", tbx.getAttribute("type"), "summary", "variant");
		globals.add(global);
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

	private void parseConceptEntries() {
		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");

		int prevPercentage = 0;
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			int newPrecentage = 100 * (i+1) / conceptEntries.getLength();
			if (newPrecentage > prevPercentage) {
				Logger.progress(newPrecentage, "Progress");
				prevPercentage = newPrecentage;
			}

			Element conceptEntry = (Element) conceptEntries.item(i);
			ConceptInterface concept = conceptEntryParser.parseConceptEntry(conceptEntry, terms);
			concepts.add(concept);

			lexicons.putAll(conceptEntryParser.getLexicons());

			String subjectField = concept.getSubjectField();
			if (subjectField != null)
				subjectFields.put(subjectField, concept.getSubjectFieldFQN());
		}
		Logger.progress(100, "Done");
	}

	private void finaliseSubjectfields() {
		for (Entry<String,String> entry: subjectFields.entrySet()) {
			Global global = new Global();
			global.subjectField = entry.getKey();
			String subjectFieldFQN = entry.getValue();
			global.metadata.addToList("*", global.subjectField, "summary", "subjectFields");
			global.triples.add(subjectFieldFQN, "rdf:type", "skos:ConceptScheme");
			global.triples.addString(subjectFieldFQN, "skos:prefLabel", global.subjectField);
			globals.add(global);
		}
	}

	private void finaliseLexicons() {
		for (Entry<String,String> entry: lexicons.entrySet()) {
			Global global = new Global();
			global.language = entry.getKey();
			global.metadata.addToList(global.language, global.language, "summary", "languages");
			global.triples.addLexicon(global.language);
			globals.add(global);
		}
	}

	@Override
	public ResourceInterface parse() throws Exception {
		parseConceptEntries();
		finaliseSubjectfields();
		finaliseLexicons();
		return this;
	}

	@Override
	public Collection<String> getLanguages() {
		return lexicons.keySet();
	}

	@Override
	public Collection<GlobalInterface> getGlobals() {
		return globals;
	}

	@Override
	public Collection<ConceptInterface> getConcepts() {
		return concepts;
	}

	@Override
	public Collection<TermInterface> getTerms() {
		return terms;
	}

}
