package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.common.RutException;
import cnr.ilc.conllu.main.SPARQLWriter;
import cnr.ilc.conllu.main.Word;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Tbx2Sparql {
	private Document document;
	public final String tbxType;
	private SPARQLWriter sparql;
	private Set<String> lexicons = new HashSet<>();
	private Set<String> concepts = new HashSet<>();
	private int numberOfTerms = 0;
	public final long fileSize;

    public Tbx2Sparql(String fileName, SPARQLWriter sparql) throws Exception {
		this.sparql = sparql;
		Path path = Paths.get(fileName);
		fileSize = Files.size(path);

		document = parseTbx(fileName);
		document.getDocumentElement().normalize();
		Element tbx = (Element) document.getElementsByTagName("tbx").item(0);
		tbxType = tbx.getAttribute("type");
	}
	
	static private Document parseTbx(String fileName) throws RutException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(fileName));
			return document;
		}
		catch(Exception e) {
			throw new RutException(e);
		}
	}

	static private String getTextOfTag(Element root, String tagName) {
		Element element = (Element) root.getElementsByTagNameNS("*", tagName).item(0);
		return element == null? null : element.getTextContent();
	}

	private void parseTermSec(Element termSec, String lexiconFQN, String language) {
		final Map<String, String> parts = Stream.of(new String[][] {
			{ "ADV", "lexinfo:adverb" },
			{ "VERB", "lexinfo:verb" },
			{ "ADJ", "lexinfo:adjective" },
			{ "noun", "lexinfo:noun" },
			{ "PROPN", "lexinfo:properNoun" },
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

		Element term = (Element) termSec.getElementsByTagName("term").item(0);
		Element posElement = (Element) termSec.getElementsByTagName("min:partOfSpeech").item(0);

		if (term == null || posElement == null) {
			return;
		}

		String lemma = term.getTextContent();
		String origPartOfSpeech = posElement.getTextContent();
		String partOfSpeech = parts.get(origPartOfSpeech);
		if (partOfSpeech == null) {
			throw new RutException(String.format("Unknown part of speech: %s", origPartOfSpeech));
		}

		Word word = new Word(lemma, partOfSpeech, language);
		sparql.addWord(word, lexiconFQN, "ontolex:LexicalEntry");
	}

	private void parseLangSec(Element langSec, String conceptFQN) {
		final Map<String, String> links = Stream.of(new String[][] {
			{ "definition", "skos:definition" },
			{ "source", "dct:source" },
			{ "externalCrossReference", "dct:identifier" },
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

		String lang = langSec.getAttribute("xml:lang");
		String lexiconFQN = String.format(":tbx_%s", lang);

		if (!lexicons.contains(lang)) {
			lexicons.add(lang);
			sparql.createLexicon(lexiconFQN, lang);
		}

		parseChildren(langSec, conceptFQN, links);
		// TODO: se c'e' una definizione la si associa al concetto?
		//sparql.insertTriple(conceptFQN, "skos:definition", "definition?");		

		NodeList termSecs = langSec.getElementsByTagNameNS("*", "termSec");
		numberOfTerms += termSecs.getLength();
		for (int k = 0; k < termSecs.getLength(); ++k)  {
			Element termSec = (Element) termSecs.item(k);
			parseTermSec(termSec, lexiconFQN, lang);
		}
	}

	private void parseChildrenShallow(Element root, String FQN, Map<String,String> links) {
		for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			String key = node.getLocalName();
			String link = links.get(key);
			if (link != null) {
				String content = node.getTextContent();
				sparql.insertTriple(FQN, link,  "\"" + content + "\"");
			}
		}
	}

	private void parseChildren(Element root, String FQN, Map<String,String> links) {
		for (Entry<String,String> entry: links.entrySet()) {
			String key = entry.getKey();
			String link = links.get(key);
			Node contentNode = root.getElementsByTagNameNS("*", key).item(0);
			if (contentNode != null) {
				String content = contentNode.getTextContent();
				sparql.insertTriple(FQN, link,  "\"" + content + "\"");
			}
		}
	}

	private String parseConceptEntry(Element conceptEntry) {
		final Map<String, String> links = Stream.of(new String[][] {
			{ "definition", "skos:definition" },
			{ "note", "skos:note" },
			{ "source", "dct:source" },
			{ "externalCrossReference", "rdf:seeAlso" },
			{ "crossReference", "rdf:seeAlso" },
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

		String id = conceptEntry.getAttribute("id");
		String conceptFQN = String.format(":concept_%s", id);

		if (id == null) {
			throw new RutException("ConceptEntry must have an id");
		}

		sparql.insertTriple(conceptFQN, "rdf:type", "skos:Concept");
		sparql.insertTriple(conceptFQN,"skos:prefLabel", String.format("\"%s\"", id));

		NodeList langSecs = conceptEntry.getElementsByTagNameNS("*", "langSec");

		for (int j = 0; j < langSecs.getLength(); ++j)  {
			Element langSec = (Element) langSecs.item(j);
			langSec.getParentNode().removeChild(langSec);
			parseLangSec(langSec, conceptFQN);
		}

		parseChildren(conceptEntry, conceptFQN, links);	
		return id;
}

	public String createSPARQL() throws Exception {
		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");
		
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			Element conceptEntry = (Element) conceptEntries.item(i);
			String conceptId = parseConceptEntry(conceptEntry);
			concepts.add(conceptId);
		}

		return sparql.toString();
	}

	public int getNumberOfConcepts() {
		return concepts.size();
	}

	public int getNumberOfLanguages() {
		return lexicons.size();
	}

	public int getNumberOfTerms() {
		return numberOfTerms;
	}
}
