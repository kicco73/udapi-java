package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.rut.Word;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LangSec {
	private SPARQLWriter sparql;
	protected final Set<String> lexicons = new HashSet<>();
	TermSec termSecParser;

	public LangSec(SPARQLWriter sparql) {
		this.sparql = sparql;
		termSecParser = new TermSec(sparql);
	}

	private void parseLangSecChildren(Element langSec, Concept concept, String language) {

		String note = Nodes.getTextOfTag(langSec, "note");
		String definition = Nodes.getTextOfTag(langSec, "definition");
		String source = Nodes.getTextOfTag(langSec, "source");
		String externalCrossReference = Nodes.getTextOfTag(langSec, "externalCrossReference");

		if (note != null) {
			sparql.insertTripleWithLanguage(concept.FQName, "skos:note", note, language);
		}
		if (definition == null) return;

		String value = sparql.formatObjectWithLanguage(definition, language);

		if (source == null && externalCrossReference == null) {
			sparql.insertTriple(concept.FQName, "skos:definition", value);
		} else {
			Map<String, String> content = new HashMap<>();
			content.put("rdf:value", value);
			
			if (source != null)
				content.put("dct:source", sparql.formatObjectWithUrlIfPossible(source));
			if (externalCrossReference != null)
				content.put("dct:identifier", sparql.formatObjectWithUrlIfPossible(externalCrossReference));
	
			sparql.insertTriple(concept.FQName, "skos:definition", content);	
		}
	}

	public Collection<Word> parseLangSec(Element langSec, Concept concept) {
		String lang = langSec.getAttribute("xml:lang");
		String lexiconFQN = String.format(":tbx_%s", lang);

		if (!lexicons.contains(lang)) {
			lexicons.add(lang);
			lexiconFQN = sparql.createLexicon(lexiconFQN, lang);
		}

		Collection<Word> terms = new HashSet<>();
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
