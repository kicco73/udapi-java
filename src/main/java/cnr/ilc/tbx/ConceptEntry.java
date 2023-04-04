package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.common.RutException;
import cnr.ilc.rut.SPARQLWriter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ConceptEntry {
	private SPARQLWriter sparql;
	private LangSec langSecParser;
	private Set<String> subjectFields = new HashSet<>();
	protected int numberOfTerms = 0;

    public ConceptEntry(SPARQLWriter sparql) throws Exception {
		this.sparql = sparql;
		langSecParser = new LangSec(sparql);
	}
	
	private void parseSubjectField(Element conceptEntry, String conceptFQN, String conceptId) {
		Node subjectFieldNode = conceptEntry.getElementsByTagNameNS("*", "subjectField").item(0);

		if (subjectFieldNode == null) {
			subjectFieldNode = conceptEntry.getElementsByTagNameNS("*", "descrip").item(0);
			if (subjectFieldNode == null) return;
			Node typeNode = subjectFieldNode.getAttributes().getNamedItem("type");
			if (typeNode == null || !typeNode.getTextContent().equals("subjectField")) return;
		};

		String subjectField = subjectFieldNode.getTextContent();
		String subjectFieldFQN = String.format("%s_%s", conceptFQN, subjectField.replaceAll(" ", "_").replaceAll(";", "-"));
		
		if (!subjectFields.contains(subjectFieldFQN)) {
			subjectFields.add(subjectFieldFQN);
			sparql.insertTriple(subjectFieldFQN, "rdf:type", "skos:ConceptScheme");
			sparql.insertTriple(subjectFieldFQN, "skos:prefLabel", String.format("\"%s\"", subjectField));
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
	}

	public String parseConceptEntry(Element conceptEntry) {
		String id = conceptEntry.getAttribute("id");
		String conceptFQN = String.format(":concept_%s", id);

		if (id == null) {
			// TODO:
			throw new RutException("TODO -- ConceptEntry must have an id");
		}

		sparql.insertTriple(conceptFQN, "rdf:type", "skos:Concept");
		sparql.insertTriple(conceptFQN, "skos:prefLabel", String.format("\"%s\"", id));

		NodeList langSecs = conceptEntry.getElementsByTagNameNS("*", "langSec");

		for (int j = 0; j < langSecs.getLength(); ++j)  {
			Element langSec = (Element) langSecs.item(j);
			numberOfTerms += langSecParser.parseLangSec(langSec, conceptFQN);
		}

		Nodes.removeNodesFromParsingTree(langSecs);
		parseConceptEntryChildren(conceptEntry, conceptFQN);	
		parseSubjectField(conceptEntry, conceptFQN, id);

		return id;
	}

	public int getNumberOfLanguages() {
		return langSecParser.lexicons.size();
	}

}