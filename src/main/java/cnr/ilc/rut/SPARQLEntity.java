package cnr.ilc.rut;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

public class SPARQLEntity {
	private String features = "";

	public void addFeature(String subject, String link, String object) {
		object = object.replaceAll("[\n\t ]+", " ");
		features += String.format("\t%s %s %s .\n", subject, link, object);
	}

	public void addFeatureAsString(String subject, String link, String object) {
		String objectString = SPARQLFormatter.formatObjectAsString(object);
		addFeature(subject, link, objectString);
	}

	public void addFeatureAsUrlOrString(String subject, String link, String possibleUrl) {
		String object = SPARQLFormatter.formatObjectWithUrlIfPossible(possibleUrl);
		addFeature(subject, link, object);
	}

	public void addFeatureAsStringWithLanguage(String subject, String link, String description, String language) {
		String object = SPARQLFormatter.formatObjectWithLanguage(description, language);
		addFeature(subject, link, object);
	}

	public void addFeature(String subject, String link, Map<String,String> anonObject) {
		String description = SPARQLFormatter.formatObject(anonObject);
		addFeature(subject, link, description);
	}

	private void addMetaData(String entryFQN, String creator) {
		Date now = DateProvider.getInstance().getDate();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"); // Quoted "Z" to indicate UTC, no timezone offset
		String date = df.format(now);

		addFeatureAsString(entryFQN, "dct:creator", creator);
		addFeatureAsString(entryFQN, "dct:created", date + ":00");
		addFeatureAsString(entryFQN, "dct:modified", date + ":00");
	}

	public void addLexicon(String lexiconFQN, String language, String creator) {		
		addFeature(lexiconFQN, "rdf:type", "lime:Lexicon");
        addFeatureAsString(lexiconFQN, "lime:language", language);   
		addMetaData(lexiconFQN, creator);     
	}

	public String serialise() {
		return features;
	}

}
