package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.RutException;
import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.rut.Word;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TermSec {
	private SPARQLWriter sparql;
	private Word word = new Word();

    public TermSec(SPARQLWriter sparql) {
		this.sparql = sparql;
	}
	
	private void parseAdministrativeStatus(Element termSec, Word word) {
		final Map<String, String> statuses = Stream.of(new String[][] {
			{ "admittedTerm-admn-sts", "lexinfo:admittedTerm" },
			{ "deprecatedTerm-admn-sts", "lexinfo:deprecatedTerm" },
			{ "supersededTerm-admn-sts", "lexinfo:supersededTerm" },
			{ "preferredTerm-admn-sts", "lexinfo:preferredTerm" },
			//{ "proposedTerm-admn-sts", "lexinfo:proposedTerm" },	// FIXME: does it exist?
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		String status = Nodes.getTextOfTagOrAlternateTagWithAttribute(termSec, "administrativeStatus", "termNote", "type");
		if (status != null) {
			String translatedStatus = statuses.get(status);
			if (translatedStatus == null) {
				final String ANSI_YELLOW = "\u001B[33m";
				final String ANSI_RESET = "\u001B[0m";

				System.err.println(ANSI_YELLOW+"Warning: unknown administrative status "+ status + ANSI_RESET);
				//throw new RutException(String.format("Unknown administrative status: %s", status));
			}
			else
				sparql.insertTriple(word.FQName, "lexinfo:normativeAuthorization", translatedStatus);
		}
	}

	private void parseTermType(Element termSec, Word word) {
		final Map<String, String> termTypes = Stream.of(new String[][] {
			{ "acronym", "lexinfo:acronym" },
			{ "fullForm", "lexinfo:fullForm" },
			{ "shortForm", "lexinfo:abbreviatedForm" },
			{ "abbreviation", "lexinfo:abbreviation" },
			{ "phrase", "lexinfo:compound" },
			{ "variant", "lexinfo:shortForm" },
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		String termType = Nodes.getTextOfTagOrAlternateTagWithAttribute(termSec, "termType", "termNote", "type");
		if (termType != null) {
			String translatedType = termTypes.get(termType);
			if (translatedType == null) 
				throw new RutException(String.format("Unknown term type: %s", translatedType));
			sparql.insertTriple(word.FQName, "lexinfo:termType", translatedType);
		}
	}

	private void parseDescripGrp(Element termSec, Word word, String language) {
		//Element descripGrp = (Element) termSec.getElementsByTagNameNS("*", "descripGrp").item(0);
		Element descripGrp = termSec;
		
		String externalCrossReference = Nodes.getTextOfTag(descripGrp, "externalCrossReference");
		String crossReference = Nodes.getTextOfTag(descripGrp, "crossReference");
		String source = Nodes.getTextOfTag(descripGrp, "source");
		String context = Nodes.getTextOfTag(descripGrp, "context");

		if (context != null) {
			Map<String, String> object = new HashMap<>();
			object.put("rdf:value", sparql.formatObjectWithLanguage(context, language));

			if (source != null) 
				object.put("dct:source", sparql.formatObjectWithUrlIfPossible(source));
			
			if (externalCrossReference != null) 
				object.put("dct:identifier", sparql.formatObjectWithUrlIfPossible(externalCrossReference));

			if (crossReference != null) 
				object.put("rdf:seeAlso", sparql.formatObjectWithUrlIfPossible(crossReference));

			sparql.insertTriple(word.FQName+"_sense", "ontolex:usage", object);
		} else {
			if (source != null)
				sparql.insertTripleWithUrlIfPossible(word.FQName, "dct:source", source);

			if (externalCrossReference != null)
				sparql.insertTripleWithUrlIfPossible(word.FQName, "rdf:seeAlso", externalCrossReference);

			if (crossReference != null)
				sparql.insertTripleWithUrlIfPossible(word.FQName, "rdf:seeAlso", crossReference);
		}
	}

	public void parseTermSec(Element termSec, String lexiconFQN, String language, String conceptFQN) {

		String lemma = Nodes.getTextOfTag(termSec, "term");
		String grammaticalGender = Nodes.getTextOfTag(termSec, "grammaticalGender");

		if (lemma == null) return;

		String partOfSpeech = Nodes.getTextOfTagOrAlternateTagWithAttribute(termSec, "partOfSpeech", "termNote", "type");
		String partOfSpeechFQN = partOfSpeech != null? String.format("lexinfo:%s", partOfSpeech) : null;
		
		String grammaticalGenderFQN = String.format("lexinfo:%s", grammaticalGender);

		word.reuse(lemma, partOfSpeechFQN, language);
		word.conceptFQN = conceptFQN;
		if (grammaticalGender != null)
			word.canonicalForm.features.put(grammaticalGenderFQN, "lexinfo:gender");

		sparql.addWord(word, lexiconFQN, "ontolex:LexicalEntry");
		parseTermType(termSec, word);
		parseAdministrativeStatus(termSec, word);
		parseDescripGrp(termSec, word, language);

		String note = Nodes.getTextOfTag(termSec, "note");
		if (note != null) {
			sparql.insertTripleWithString(word.FQName, "skos:note", note);
		}

	}

}
