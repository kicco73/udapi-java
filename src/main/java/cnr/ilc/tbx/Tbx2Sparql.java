package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.common.RutException;
import cnr.ilc.rut.SPARQLWriter;

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
	public final String tbxType;
	public final long fileSize;

	private Document document;
	private SPARQLWriter sparql;
	private LangSec langSecParser;
	private Set<String> concepts = new HashSet<>();
	private int numberOfTerms = 0;
	private Set<String> subjectFields = new HashSet<>();

    public Tbx2Sparql(String fileName, SPARQLWriter sparql) throws Exception {
		this.sparql = sparql;
		langSecParser = new LangSec(sparql);
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

	private void parseSubjectField(Element conceptEntry, String conceptFQN) {
		Node subjectFieldNode = conceptEntry.getElementsByTagNameNS("*", "subjectField").item(0);
		if (subjectFieldNode == null) return;

		String subjectFieldFQN = String.format("%s_%s", conceptFQN, subjectFieldNode.getTextContent());
		
		if (!subjectFields.contains(subjectFieldFQN)) {
			subjectFields.add(subjectFieldFQN);
			sparql.insertTriple(subjectFieldFQN, "rdf:type", "skos:ConceptScheme");
			sparql.insertTriple(conceptFQN, "skos:prefLabel", subjectFieldFQN);
		}

		sparql.insertTriple(conceptFQN, "skos:inScheme", subjectFieldFQN);
	}

	private void parseConceptEntryChildren(Element root, String FQN) {
		final Map<String, String> links = Stream.of(new String[][] {
			{ "definition", "skos:definition" },
			{ "note", "skos:note" },
			{ "source", "dct:source" },
			{ "externalCrossReference", "rdf:seeAlso" },
			{ "crossReference", "rdf:seeAlso" },
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

		for (Entry<String,String> entry: links.entrySet()) {
			String key = entry.getKey();
			String link = entry.getValue();
			String content = Nodes.getTextOfTag(root, key);
			if (content != null) {
				sparql.insertTriple(FQN, link,  "\"" + content + "\"");
			}
		}

		parseSubjectField(root, FQN);
	}

	private String parseConceptEntry(Element conceptEntry) {
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
			numberOfTerms += langSecParser.parseLangSec(langSec, conceptFQN);
		}

		Nodes.removeNodesFromParsingTree(langSecs);
		parseConceptEntryChildren(conceptEntry, conceptFQN);	
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
		return langSecParser.lexicons.size();
	}

	public int getNumberOfTerms() {
		return numberOfTerms;
	}
}
