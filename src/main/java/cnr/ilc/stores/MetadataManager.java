package cnr.ilc.stores;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cnr.ilc.rut.Metadata;

@SuppressWarnings("unchecked")
public class MetadataManager {
	private SqliteConnector db;
	private JSONParser jsonParser = new JSONParser();

	private String buildWhere(String entityName, Collection<String> languages) {
		String where = "true";
		String languagesClause = languages.stream().collect(Collectors.joining("', '"));
		if (languages.size() > 0) 
			where += String.format(" AND %s.language in ('%s')", entityName, languagesClause);	
		return where;
	}

	private int mergeJson(Metadata metadata, String columnName, String entityName, Collection<String> languages) throws Exception {
		int results = 0;
		Collection<String> langs = new ArrayList<String>(languages);
		if (langs.size() > 0) langs.add("*");
		String where = buildWhere(entityName, langs);
		ResultSet rs = db.executeQuery("select %s from %s where %s", columnName, entityName, where);
		while (rs.next()) {
			results++;
			String jsonString = rs.getString(columnName);
			if (jsonString == null) continue;
			Map<String, Object> json = (Map<String, Object>) jsonParser.parse(jsonString);
			metadata.merge("*", json);
		}
		return results;
	}

	private int mergeJsonConcept(Metadata metadata, String columnName, Collection<String> languages) throws SQLException, ParseException {
		Collection<String> jsonStrings = db.selectConcept(columnName, languages);
		for (String jsonString: jsonStrings) {			
			if (jsonString == null) continue;
			Map<String, Object> json = (Map<String, Object>) jsonParser.parse(jsonString);
			metadata.merge("*", json);
		}
		return metadata.getMap("*", "concepts").size();
	}

	public MetadataManager(SqliteConnector db) {
		this.db = db;
	}

	public Map<String,Object> getMetadata(Collection<String> languages) throws Exception {
		Metadata metadata = new Metadata();

		mergeJson(metadata, "metadata", "lexicon", languages);
		mergeJson(metadata, "metadata", "summary", languages);
		int numberOfConcepts = mergeJsonConcept(metadata, "metadata", languages);
		int numberOfTerms = mergeJson(metadata, "metadata", "word", languages);

		metadata.putInMap("*", numberOfConcepts, "summary", "numberOfConcepts");
		metadata.putInMap("*", numberOfTerms, "summary", "numberOfTerms");
		metadata.putInMap("*", db.termsByLanguage(languages), "summary", "languages");

		return metadata.getMap("*");
	}
}	
