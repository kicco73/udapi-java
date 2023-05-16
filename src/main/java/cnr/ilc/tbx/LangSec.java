package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.lemon.resource.Concept;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.lemon.resource.Word;
import cnr.ilc.sparql.SPARQLFormatter;
import cnr.ilc.sparql.TripleSerialiser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class LangSec {
	protected final Map<String, String> lexicons = new HashMap<>();
	TermSec termSecParser = new TermSec();

	private void parseLangSecChildren(Element langSec, Concept concept, String language) {

		String note = Nodes.getTextOfTag(langSec, "note");
		String definition = Nodes.getTextOfTag(langSec, "definition");
		String source = Nodes.getTextOfTag(langSec, "source");
		String externalCrossReference = Nodes.getTextOfTag(langSec, "externalCrossReference");
		String FQName = concept.getFQName();

		if (note != null) {
			concept.triples.addStringWithLanguage(FQName, "skos:note", note, language);		
		}
		
		if (definition == null) return;
		concept.setDefinition(definition, language);

		if (source == null && externalCrossReference == null) {
			concept.triples.addStringWithLanguage(FQName, "skos:definition", definition, language);
		} else {
			Map<String, String> object = new LinkedHashMap<>();
			object.put("rdf:value", SPARQLFormatter.formatObjectAsStringWithLanguage(definition, language));
		
			if (source != null) 
				object.put("dct:source", SPARQLFormatter.formatObjectWithUrlIfPossible(source));
			
			if (externalCrossReference != null) 
				object.put("dct:identifier", SPARQLFormatter.formatObjectWithUrlIfPossible(externalCrossReference));
	
			concept.triples.addObject(FQName, "skos:definition", object, language);
		}
	}

	public Collection<TermInterface> parseLangSec(Element langSec, Concept concept) {
		String lang = langSec.getAttribute("xml:lang");
		String lexiconFQN = TripleSerialiser.getLexiconFQN(lang);
		lexicons.put(lang, lexiconFQN);

		Collection<TermInterface> terms = new HashSet<>();
		NodeList termSecs = langSec.getElementsByTagNameNS("*", "termSec");
		for (int k = 0; k < termSecs.getLength(); ++k)  {
			Element termSec = (Element) termSecs.item(k);
			Word word = termSecParser.parseTermSec(termSec, lexiconFQN, lang, concept);
			terms.add(word);
		}

		Nodes.removeNodesFromParsingTree(termSecs);
		parseLangSecChildren(langSec, concept, lang);
		return terms;
	}
}
