package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.IdGenerator;
import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.rut.Word;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ConceptEntry {
	static private IdGenerator idGenerator = new IdGenerator();
	private SPARQLWriter sparql;
	private LangSec langSecParser;
	private Set<String> subjectFields = new HashSet<>();
	protected int numberOfTerms = 0;

    public ConceptEntry(SPARQLWriter sparql) throws Exception {
		this.sparql = sparql;
		langSecParser = new LangSec(sparql);
	}
	
	private void parseSubjectField(Element conceptEntry, Concept concept, String conceptId) {
		String subjectField = Nodes.getTextOfTagOrAlternateTagWithAttribute(conceptEntry, "subjectField", "descrip", "type");
		if (subjectField == null) return;
		
		String subjectFieldFQN = String.format("%s_%s", concept.FQName, idGenerator.getId(subjectField));
		
		if (!subjectFields.contains(subjectFieldFQN)) {
			subjectFields.add(subjectFieldFQN);
			sparql.insertTriple(subjectFieldFQN, "rdf:type", "skos:ConceptScheme");
			sparql.insertTripleWithString(subjectFieldFQN, "skos:prefLabel", subjectField);
		}

		sparql.insertTriple(concept.FQName, "skos:inScheme", subjectFieldFQN);
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
				sparql.insertTripleWithUrlIfPossible(concept.FQName, link, content);
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
		sparql.insertTriple(concept.FQName, "rdf:type", "skos:Concept");
		sparql.insertTripleWithString(concept.FQName, "skos:prefLabel", id);

		parseLangSecs(conceptEntry, concept);
		parseConceptEntryChildren(conceptEntry, concept);	
		parseSubjectField(conceptEntry, concept, id);

		return concept;
	}

	public Set<String> getLanguages() {
		return langSecParser.lexicons;
	}

}