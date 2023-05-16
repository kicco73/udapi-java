package cnr.ilc.sparql;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import cnr.ilc.rut.utils.DateProvider;

public class TripleSerialiser {
	static private Pattern removeBlanks = Pattern.compile("[\n\t ]+");
	private Map<String, StringBuilder> features = new LinkedHashMap<>();

	public TripleSerialiser() {
		features.put("*", new StringBuilder(""));
	}

	private void addFeature(String language, String content) {
		StringBuilder languageSpecific = features.containsKey(language)? features.get(language) : new StringBuilder("");
		languageSpecific.append(content);
		features.put(language, languageSpecific);
	}

	private void add(String subject, String link, String object, String language) {
		object = removeBlanks.matcher(object).replaceAll(" ");
		String content = SPARQLFormatter.formatStatement(subject, link, object);
		addFeature(language, content);
	} 

	public void add(String subject, String link, String object) {
		add(subject, link, object, "*");
	}

	public void addString(String subject, String link, String object) {
		String objectString = SPARQLFormatter.formatObjectAsString(object);
		add(subject, link, objectString);
	}

	public void addMultiple(String subject, String... links) {
		String content = SPARQLFormatter.formatMultipleStatement(subject, links);
		addFeature("*", content);
	}

	public void addUrlOrString(String subject, String link, String possibleUrl) {
		String object = SPARQLFormatter.formatObjectWithUrlIfPossible(possibleUrl);
		add(subject, link, object);
	}

	public void addStringWithLanguage(String subject, String link, String description, String language) {
		String object = SPARQLFormatter.formatObjectAsStringWithLanguage(description, language);
		add(subject, link, object, language);
	}

	public void addObject(String subject, String link, Map<String,String> anonObject, String language) {

		int count = 0;
		String[] anonLinks = new String[anonObject.size()*2];
		for (Entry<String,String> entry: anonObject.entrySet()) {
			anonLinks[count++] = entry.getKey();
			anonLinks[count++] = entry.getValue();
		}

		String content = SPARQLFormatter.formatAnonStatement(subject, link, anonLinks);
		addFeature(language, content);
	}

	public void addMetaData(String entryFQN, String creator) {
		Date now = DateProvider.getInstance().getDate();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"); // Quoted "Z" to indicate UTC, no timezone offset
		String date = String.format("\"%s:00\"",  df.format(now));
		addMultiple(entryFQN, "dct:creator", "\""+creator+"\"", "dct:created", date, "dct:modified", date);
	}

	static public String getLexiconFQN(String language) {
		return String.format(":lexicon_%s", language);
	}

	public String addLexicon(String language) {	
		String lexiconFQN = getLexiconFQN(language);
		addMultiple(lexiconFQN, "rdf:type", "lime:Lexicon", "lime:language", "\""+language+"\"");
		return lexiconFQN;
	}

	public void addComment(String template, Object... args) {
		String comment = String.format("\n\t# "+template+"\n\n", args);
		addFeature("*", comment);
	}

	public String serialise(String language) {
		return features.containsKey(language)? features.get(language).toString() : "";
	}

	public Collection<String> getLanguages() {
		return features.keySet();
	}

	public String serialise() {
		StringBuilder builder = new StringBuilder("");
		for (StringBuilder languageSpecific: features.values()) {
			builder.append(languageSpecific);
		}
		return builder.toString();
	}

}
