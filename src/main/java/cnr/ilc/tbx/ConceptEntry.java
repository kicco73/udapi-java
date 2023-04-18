package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.Word;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ConceptEntry {
	private LangSec langSecParser;
	protected int numberOfTerms = 0;

    public ConceptEntry() throws Exception {
		langSecParser = new LangSec();
	}
	
	private void parseSubjectField(Element conceptEntry, Concept concept, String conceptId) {
		String subjectField = Nodes.getTextOfTagOrAlternateTagWithAttribute(conceptEntry, "subjectField", "descrip", "type");
		if (subjectField == null) return;
		concept.addSubjectField(subjectField);
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
				concept.addFeatureAsUrlOrString(concept.FQName, link, content);
			}
		}
	}

	private void parseLangSecs(Element conceptEntry, Concept concept) {

		NodeList langSecs = conceptEntry.getElementsByTagNameNS("*", "langSec");
		
		for (int j = 0; j < langSecs.getLength(); ++j)  {
			Element langSec = (Element) langSecs.item(j);
			Collection<Word> terms = langSecParser.parseLangSec(langSec, concept);
			numberOfTerms += terms.size();
		}
		
		Nodes.removeNodesFromParsingTree(langSecs);
	}

	public Concept parseConceptEntry(Element conceptEntry) {
		String id = conceptEntry.getAttribute("id");

		if (id == null) {
			Random random = new Random();
			id = Long.toString(random.nextLong());
		}

		Concept concept = new Concept(id);

		parseLangSecs(conceptEntry, concept);
		parseConceptEntryChildren(conceptEntry, concept);	
		parseSubjectField(conceptEntry, concept, id);

		return concept;
	}

	public Set<String> getLanguages() {
		return langSecParser.lexicons.keySet();
	}

	public Map<String, String> getLexicons() {
		return langSecParser.lexicons;
	}
}