package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cnr.ilc.rut.Filter;
import cnr.ilc.rut.utils.Metadata;

@SuppressWarnings("unchecked")
public class MetadataMerger {
	private SqliteConnector db;
	private JSONParser jsonParser = new JSONParser();

	public MetadataMerger(SqliteConnector db) {
		this.db = db;
	}

	private int mergeJson(Metadata metadata, String columnName, String entityName, Filter filter) throws Exception {
		int results = 0;
		filter = new Filter(filter);
		HashSet<String> langs = (HashSet<String>) filter.getLanguages();
		if (langs.size() > 0) langs.add("*");
		String where = filter.buildWhere(entityName);
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

	private int mergeJsonConcept(Metadata metadata, String columnName, Filter filter) throws SQLException, ParseException {
		Collection<String> jsonStrings = db.selectConcept(columnName, filter);
		for (String jsonString: jsonStrings) {			
			if (jsonString == null) continue;
			Map<String, Object> json = (Map<String, Object>) jsonParser.parse(jsonString);
			metadata.merge("*", json);
		}
		Map<String, Object> concepts =  metadata.getMap("*", "concepts");
		return concepts == null? 0 : concepts.size();
	}

	public Map<String,Object> getMetadata(Filter filter) throws Exception {
		Metadata metadata = new Metadata();
		Filter noDatesFilter = new Filter(filter);
		noDatesFilter.setDates(null);

		mergeJson(metadata, "metadata", "global", noDatesFilter);
		int numberOfConcepts = mergeJsonConcept(metadata, "metadata", filter);
		int numberOfTerms = mergeJson(metadata, "metadata", "word", filter);

		metadata.putInMap("*", numberOfConcepts, "summary", "numberOfConcepts");
		metadata.putInMap("*", numberOfTerms, "summary", "numberOfTerms");
		metadata.putInMap("*", db.selectTermStats(filter), "summary", "languages");
		metadata.putInMap("*", db.selectConceptDates(filter), "summary", "dates");
		metadata.putInMap("*", db.selectPolysemicEntries(filter), "summary", "polysemic");

		return metadata.getMap("*");
	}

	public String getJson(Filter filter) throws Exception {
		return JSONObject.toJSONString(getMetadata(filter));
	}
}	
