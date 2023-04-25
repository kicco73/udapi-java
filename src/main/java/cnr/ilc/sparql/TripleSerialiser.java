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
		languageSpecific += String.format("\t%s %s %s .\n", subject, link, object);
		features.put(language, languageSpecific);
	} 

	public void add(String subject, String link, String object) {
		add(subject, link, object, "*");
	}

	public void addString(String subject, String link, String object) {
		String objectString = SPARQLFormatter.formatObjectAsString(object);
		add(subject, link, objectString);
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
		String description = SPARQLFormatter.formatObject(anonObject);
		add(subject, link, description, language);
	}

	public void addMetaData(String entryFQN, String creator) {
		Date now = DateProvider.getInstance().getDate();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"); // Quoted "Z" to indicate UTC, no timezone offset
		String date = df.format(now);

		addString(entryFQN, "dct:creator", creator);
		addString(entryFQN, "dct:created", date + ":00");
		addString(entryFQN, "dct:modified", date + ":00");
	}

	public void addLexicon(String lexiconFQN, String language, String creator) {	
		add(lexiconFQN, "rdf:type", "lime:Lexicon");
        addString(lexiconFQN, "lime:language", language);   
		addMetaData(lexiconFQN, creator);     
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
