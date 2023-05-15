package cnr.ilc.sparql;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import cnr.ilc.rut.utils.DateProvider;

public class TripleSerialiser {
	private Map<String, String> features = new LinkedHashMap<>();

	public TripleSerialiser() {
		features.put("*", "");
	}

	private void add(String subject, String link, String object, String language) {
		object = object.replaceAll("[\n\t ]+", " ");
		String languageSpecific = features.getOrDefault(language, "");
		languageSpecific += SPARQLFormatter.formatStatement(subject, link, object);
		features.put(language, languageSpecific);
	} 

	public void add(String subject, String link, String object) {
		add(subject, link, object, "*");
	}

	public void addString(String subject, String link, String object) {
		String objectString = SPARQLFormatter.formatObjectAsString(object);
		add(subject, link, objectString);
	}

	public void addMultiple(String subject, Map<String,String> links) {
		String language = "*";
		String languageSpecific = features.getOrDefault(language, "");
		languageSpecific += SPARQLFormatter.formatMultipleStatement(subject, links);
		features.put(language, languageSpecific);
	}

	public void addUrlOrString(String subject, String link, String possibleUrl) {
		String object = SPARQLFormatter.formatObjectWithUrlIfPossible(possibleUrl);
		add(subject, link, object);
	}

	public void addStringWithLanguage(String subject, String link, String description, String language) {
		String object = SPARQLFormatter.formatObjectWithLanguage(description, language);
		add(subject, link, object, language);
	}

	public void add(String subject, String link, Map<String,String> anonObject, String language) {
		String languageSpecific = features.getOrDefault(language, "");
		languageSpecific += SPARQLFormatter.formatAnonStatement(subject, link, anonObject);
		features.put(language, languageSpecific);
	}

	public void addMetaData(String entryFQN, String creator) {
		Date now = DateProvider.getInstance().getDate();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"); // Quoted "Z" to indicate UTC, no timezone offset
		String date = String.format("\"%s:00\"",  df.format(now));
		Map<String,String> links = Map.of("dct:creator", "\""+creator+"\"", "dct:created", date, "dct:modified", date);
		addMultiple(entryFQN, links);
	}

	static public String getLexiconFQN(String language) {
		return String.format(":lexicon_%s", language);
	}

	public String addLexicon(String language) {	
		String lexiconFQN = getLexiconFQN(language);
		Map<String,String> links = Map.of("rdf:type", "lime:Lexicon", "lime:language", "\""+language+"\"");
		addMultiple(lexiconFQN, links);
		return lexiconFQN;
	}

	public void addComment(String template, Object... args) {
		String comment = String.format("\n\t# "+template+"\n\n", args);
		String languageSpecific = features.getOrDefault("*", "");
		languageSpecific += comment;
		features.put("*", languageSpecific);
	}

	public String serialise(String language) {
		return features.getOrDefault(language, "");
	}

	public Collection<String> getLanguages() {
		return features.keySet();
	}

	public String serialise() {
		String serialised = "";
		for (String languageSpecific: features.values()) {
			serialised += languageSpecific;
		}
		return serialised;
	}

}
