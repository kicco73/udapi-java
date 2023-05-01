package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.lemon.resource.Concept;
import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.rut.utils.IdGenerator;
import cnr.ilc.rut.utils.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ConceptEntry {
	private LangSec langSecParser;
	private static IdGenerator idGenerator = new IdGenerator();

    public ConceptEntry() throws Exception {
		langSecParser = new LangSec();
	}
	
	private void parseSubjectField(Element conceptEntry, Concept concept, String conceptId) {
		String subjectField = Nodes.getTextOfTagOrAlternateTagWithAttribute(conceptEntry, "subjectField", "descrip", "type");
		if (subjectField == null) return;

		String subjectFieldFQN = String.format(":sf_%s", idGenerator.getId(subjectField));		
		concept.setSubjectField(subjectField, subjectFieldFQN);
	}

	private void parseConceptEntryChildren(Element root, Concept concept) {
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
				concept.triples.addUrlOrString(concept.getFQName(), link, content);
				if (key.equals("definition"))
					concept.setDefinition(content, "*");
			}
		}
	}

	private void parseLangSecs(Element conceptEntry, Concept concept) {

		NodeList langSecs = conceptEntry.getElementsByTagNameNS("*", "langSec");
		
		for (int j = 0; j < langSecs.getLength(); ++j)  {
			Element langSec = (Element) langSecs.item(j);
			langSecParser.parseLangSec(langSec, concept);
		}
		
		Nodes.removeNodesFromParsingTree(langSecs);
	}

	private void parseDate(Element conceptEntry, Concept concept) {

		NodeList dates = conceptEntry.getElementsByTagNameNS("*", "date");
		if (dates.getLength() == 0) return;
		if (dates.getLength() > 1) 
			Logger.warn("Concept %s has more than one date, picking the first one", concept.getId()); 
		
		Element dateElement = (Element)dates.item(0);
		String date = dateElement.getTextContent();
		concept.metadata.putInMap("*", date, "concepts", concept.getId(), "date");
		concept.date = date;
	}

	public ConceptInterface parseConceptEntry(Element conceptEntry, Collection<TermInterface> terms) {
		String id = conceptEntry.getAttribute("id");

		if (id == null) {
			Random random = new Random();
			id = Long.toString(random.nextLong());
		}

		Concept concept = new Concept(id);

		parseLangSecs(conceptEntry, concept);
		parseConceptEntryChildren(conceptEntry, concept);	
		parseSubjectField(conceptEntry, concept, id);
		parseDate(conceptEntry, concept);

		terms.addAll(concept.getTerms());
		return concept;
	}

	public Set<String> getLanguages() {
		return langSecParser.lexicons.keySet();
	}

	public Map<String, String> getLexicons() {
		return langSecParser.lexicons;
	}
}