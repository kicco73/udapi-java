package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.Word;
import cnr.ilc.sparql.SPARQLFormatter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LangSec {
	protected final Map<String, String> lexicons = new HashMap<>();
	TermSec termSecParser = new TermSec();

	private void parseLangSecChildren(Element langSec, Concept concept, String language) {

		String note = Nodes.getTextOfTag(langSec, "note");
		String definition = Nodes.getTextOfTag(langSec, "definition");
		String source = Nodes.getTextOfTag(langSec, "source");
		String externalCrossReference = Nodes.getTextOfTag(langSec, "externalCrossReference");

		if (note != null) {
			concept.triples.addStringWithLanguage(concept.FQName, "skos:note", note, language);		
		}
		
		if (definition == null) return;
		concept.definition.put(language, definition);

		if (source == null && externalCrossReference == null) {
			concept.triples.addStringWithLanguage(concept.FQName, "skos:definition", definition, language);
		} else {
			Map<String, String> object = new HashMap<>();
			object.put("rdf:value", SPARQLFormatter.formatObjectWithLanguage(definition, language));
		
			if (source != null) 
				object.put("dct:source", SPARQLFormatter.formatObjectWithUrlIfPossible(source));
			
			if (externalCrossReference != null) 
				object.put("dct:identifier", SPARQLFormatter.formatObjectWithUrlIfPossible(externalCrossReference));
	
			concept.triples.add(concept.FQName, "skos:definition", object);
		}
	}

	public Collection<Word> parseLangSec(Element langSec, Concept concept, String creator) {
		String lang = langSec.getAttribute("xml:lang");
		String lexiconFQN = String.format(":tbx_%s", lang);
		lexicons.put(lang, lexiconFQN);

		Collection<Word> terms = new HashSet<>();
		NodeList termSecs = langSec.getElementsByTagNameNS("*", "termSec");
		for (int k = 0; k < termSecs.getLength(); ++k)  {
			Element termSec = (Element) termSecs.item(k);
			Word word = termSecParser.parseTermSec(termSec, lexiconFQN, lang, concept, creator);
			terms.add(word);
		}

		Nodes.removeNodesFromParsingTree(termSecs);
		parseLangSecChildren(langSec, concept, lang);
		return terms;
	}
}
