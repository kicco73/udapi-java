package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.RutException;
import cnr.ilc.rut.Word;
import cnr.ilc.sparql.SPARQLFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TermSec {
	private Word word;
	
	private void parseAdministrativeStatus(Element termSec) {
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
				return;
			}
			
			word.triples.add(word.FQName, "lexinfo:normativeAuthorization", translatedStatus);
		}
	}

	private void parseTermType(Element termSec) {
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
				word.triples.add(word.FQName, "lexinfo:termType", translatedType);
		}
	}

	private void parseDescripGrp(Element termSec, String language) {
		//Element descripGrp = (Element) termSec.getElementsByTagNameNS("*", "descripGrp").item(0);
		Element descripGrp = termSec;
		
		String externalCrossReference = Nodes.getTextOfTag(descripGrp, "externalCrossReference");
		String crossReference = Nodes.getTextOfTag(descripGrp, "crossReference");
		String source = Nodes.getTextOfTag(descripGrp, "source");
		String context = Nodes.getTextOfTag(descripGrp, "context");

		if (context != null) {
			Map<String, String> object = new HashMap<>();
			object.put("rdf:value", SPARQLFormatter.formatObjectWithLanguage(context, language));
		
			if (source != null) 
				object.put("dct:source", SPARQLFormatter.formatObjectWithUrlIfPossible(source));
			
			if (externalCrossReference != null) 
				object.put("dct:identifier", SPARQLFormatter.formatObjectWithUrlIfPossible(externalCrossReference));
	
			if (crossReference != null) 
				object.put("rdf:seeAlso", SPARQLFormatter.formatObjectWithUrlIfPossible(crossReference));
	
			word.triples.add(word.FQName+"_sense", "ontolex:usage", object, language);
			
		} else {
			if (source != null)
				word.triples.addUrlOrString(word.FQName, "dct:source", source);

			if (externalCrossReference != null)
				word.triples.addUrlOrString(word.FQName, "rdf:seeAlso", externalCrossReference);

			if (crossReference != null)
				word.triples.addUrlOrString(word.FQName, "rdf:seeAlso", crossReference);
		}
	}

	private void parseNote(Element termSec, String language) {
		String note = Nodes.getTextOfTag(termSec, "note");
		if (note != null) {
			word.triples.addStringWithLanguage(word.FQName, "skos:note", note, language);
		}
	}

	public Word parseTermSec(Element termSec, String lexiconFQN, String language, Concept concept, String creator) {

		String lemma = Nodes.getTextOfTag(termSec, "term");
		String grammaticalGender = Nodes.getTextOfTag(termSec, "grammaticalGender");

		String partOfSpeech = Nodes.getTextOfTagOrAlternateTagWithAttribute(termSec, "partOfSpeech", "termNote", "type");
		String partOfSpeechFQN = partOfSpeech != null? String.format("lexinfo:%s", partOfSpeech) : null;
		
		String grammaticalGenderFQN = String.format("lexinfo:%s", grammaticalGender);

		word = concept.newWord(lemma, partOfSpeechFQN, language, lexiconFQN, creator);

		if (grammaticalGender != null)
			word.canonicalForm.features.put(grammaticalGenderFQN, "lexinfo:gender");

		parseTermType(termSec);
		parseAdministrativeStatus(termSec);
		parseDescripGrp(termSec, language);
		parseNote(termSec, language);

		return word;
	}

}
