package cnr.ilc.tbx;
import org.w3c.dom.*;

import cnr.ilc.rut.SPARQLWriter;
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

	private void parseLangSecChildren(Element langSec, String conceptFQN, String language) {

		String note = Nodes.getTextOfTag(langSec, "note");
		String definition = Nodes.getTextOfTag(langSec, "definition");
		String source = Nodes.getTextOfTag(langSec, "source");
		String externalCrossReference = Nodes.getTextOfTag(langSec, "externalCrossReference");

		if (note != null) {
			sparql.insertTripleWithLanguage(conceptFQN, "skos:note", note, language);
		}
		if (definition == null) return;

		String value = sparql.formatObjectWithLanguage(definition, language);

		if (source == null && externalCrossReference == null) {
			sparql.insertTriple(conceptFQN, "skos:definition", value);
		} else {
			Map<String, String> content = new HashMap<>();
			content.put("rdf:value", value);
			
			if (source != null)
				content.put("dct:source", sparql.formatObjectWithUrlIfPossible(source));
			if (externalCrossReference != null)
				content.put("dct:identifier", sparql.formatObjectWithUrlIfPossible(externalCrossReference));
	
			sparql.insertTriple(conceptFQN, "skos:definition", content);	
		}
	}

	public int parseLangSec(Element langSec, String conceptFQN) {
		String lang = langSec.getAttribute("xml:lang");
		String lexiconFQN = String.format(":tbx_%s", lang);

		if (!lexicons.contains(lang)) {
			lexicons.add(lang);
			sparql.createLexicon(lexiconFQN, lang);
		}

		NodeList termSecs = langSec.getElementsByTagNameNS("*", "termSec");
		int numberOfTerms = termSecs.getLength();
		for (int k = 0; k < termSecs.getLength(); ++k)  {
			Element termSec = (Element) termSecs.item(k);
			termSecParser.parseTermSec(termSec, lexiconFQN, lang, conceptFQN);
		}

		Nodes.removeNodesFromParsingTree(termSecs);
		parseLangSecChildren(langSec, conceptFQN, lang);
		return numberOfTerms;
	}
}
