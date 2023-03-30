package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.rut.Word;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TermSec {
	private SPARQLWriter sparql;
	private Element termSec;

    public TermSec(Element termSec, SPARQLWriter sparql) {
		this.sparql = sparql;
		this.termSec = termSec;
	}
	
	private void parseAdministrativeStatus(Word word) {
		final Map<String, String> statuses = Stream.of(new String[][] {
			{ "admittedTerm-admn-sts", "lexinfo:admittedTerm" },
			{ "deprecatedTerm-admn-sts", "lexinfo:deprecatedTerm" },
			{ "supersededTerm-admn-sts", "lexinfo:supersededTerm" },
			{ "preferredTerm-admn-sts", "lexinfo:preferredTerm" },
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		String status = statuses.get(Nodes.getTextOfTag(termSec, "administrativeStatus"));
		if (status != null) {
			sparql.insertTriple(word.FQName+"_sense", "lexinfo:normativeAuthorization", status);
		}
	}

	private void parseTermType(Word word) {
		final Map<String, String> termTypes = Stream.of(new String[][] {
			{ "acronym", "lexinfo:acronym" },
			{ "fullForm", "lexinfo:fullForm" },
			{ "shortForm", "lexinfo:abbreviatedForm" },
			{ "abbreviation", "lexinfo:abbreviation" },
			{ "phrase", "lexinfo:compound" },
			{ "variant", "lexinfo:shortForm" },
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		String termType = termTypes.get(Nodes.getTextOfTag(termSec, "termType"));
		if (termType != null) {
			sparql.insertTriple(word.FQName, "lexinfo:termType", termType);
		}
	}

	private void parseDescriptGrp(Word word, String language) {
		Element descriptGrp = (Element) termSec.getElementsByTagNameNS("*", "descriptGrp").item(0);
		String externalCrossReference = Nodes.getTextOfTag(descriptGrp, "externalCrossReference");
		String crossReference = Nodes.getTextOfTag(descriptGrp, "crossReference");
		String source = Nodes.getTextOfTag(descriptGrp, "source");
		String context = Nodes.getTextOfTag(descriptGrp, "context");

		if (context != null) {
			Map<String, String> object = new HashMap<>();
			object.put("rdf:value", String.format("\"%s\"@%s", context, language));

			if (source != null) 
				object.put("dct:source", String.format("\"%s\"", source));
			
			if (externalCrossReference != null) 
				object.put("dct:identifier", String.format("\"%s\"", externalCrossReference));

			if (crossReference != null) 
				object.put("rdf:seeAlso", String.format("\"%s\"", crossReference));

			sparql.insertTriple(word.FQName+"_sense", "ontolex:usage", object);
		} else {
			if (source != null)
				sparql.insertTriple(word.FQName, "dct:source", String.format("\"%s\"", source));

			if (externalCrossReference != null)
				sparql.insertTriple(word.FQName, "rdf:seeAlso", String.format("\"%s\"", externalCrossReference));

			if (crossReference != null)
				sparql.insertTriple(word.FQName, "rdf:seeAlso", String.format("\"%s\"", crossReference));
		}
	}

	public void parseTermSec(String lexiconFQN, String language, String conceptFQN) {

		String lemma = Nodes.getTextOfTag(termSec, "term");
		String partOfSpeech = Nodes.getTextOfTag(termSec, "partOfSpeech");
		String grammaticalGender = Nodes.getTextOfTag(termSec, "grammaticalGender");

		if (lemma == null || partOfSpeech == null) {
			return;
		}

		String partOfSpeechFQN = String.format("lexinfo:%s", partOfSpeech);
		String grammaticalGenderFQN = String.format("lexinfo:%s", grammaticalGender);

		Word word = new Word(lemma, partOfSpeechFQN, language);
		word.conceptFQN = conceptFQN;
		if (grammaticalGender != null)
			word.canonicalForm.features.put(grammaticalGenderFQN, "lexinfo:gender");

		sparql.addWord(word, lexiconFQN, "ontolex:LexicalEntry");
		parseTermType(word);
		parseAdministrativeStatus(word);
		parseDescriptGrp(word, language);

		String note = Nodes.getTextOfTag(termSec, "note");
		if (note != null)
			sparql.insertTriple(word.FQName, "skos:note", String.format("\"%s\"", note));

	}

}
