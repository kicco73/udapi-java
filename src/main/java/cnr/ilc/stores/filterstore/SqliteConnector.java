package cnr.ilc.stores.filterstore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cnr.ilc.rut.utils.Metadata;


public class SqliteConnector {
	private Connection connection;
	private Statement statement;

	private void createIndices(String tableName, String... columnNames) throws SQLException {
		for (String columnName: columnNames) {
			executeUpdate("create index %s_%s_idx on %s (%s)", 
				tableName, columnName, tableName, columnName
			);
		}
	}

	private void createTable(String tableName, String ...fields) throws SQLException {
		List<String> defaultFields = new ArrayList<String>(
			Arrays.asList("language string", "date string", "subjectField string", "metadata json", "serialised string default ''"))
		;
		defaultFields.addAll(Arrays.asList(fields));
		String fieldsString = String.join(", ", defaultFields);
		
		executeUpdate("drop table if exists %s", tableName);
		executeUpdate("create table %s (%s)", tableName, fieldsString);
		createIndices(tableName, "language", "date", "subjectField");
	}

	private void createSchema() throws SQLException {
		createTable("global");
		createTable("concept", "conceptId string");
		createIndices("concept", "conceptId");
		createTable("word", "conceptId string", "lemma string");
		createIndices("word", "conceptId");
	}

	public void connect(String fileName) throws SQLException {
		boolean exists = new File(fileName).exists();
		String url = String.format("jdbc:sqlite:%s", fileName);
		connection = DriverManager.getConnection(url);
		statement = connection.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec
		if(!exists) createSchema();
	}

	public void executeUpdate(String query, Object ...args) throws SQLException {
		query = String.format(query, args);
		statement.executeUpdate(query);
	}

	public ResultSet executeQuery(String query, Object ...args) throws SQLException {
		query = String.format(query, args);
		//System.err.println(query);
		ResultSet rs = statement.executeQuery(query);
		return rs;
	}

	public String quote(String s) {
		if (s == null) return null;
		s = "'" + s.replaceAll("'", "''") + "'";
		return s;
	}

	private String whereValueInList(String entityName, String columnName, Collection<String> values) {
		String where = "";
		String op = "AND";
		if (values == null) return where;

		if (values.contains(null)) {
			where += String.format(" %s %s.%s IS null", op, entityName, columnName);
			values.remove(null);
			op = "OR";
		}

		if (values.size() > 0) {
			String listString = values.stream().collect(Collectors.joining("', '"));
			where += String.format(" %s %s.%s in ('%s')", op, entityName, columnName, listString);	
		}
		return where;
	}

	public String buildWhere(String entityName, Filter filter) {
		String where = "true";
		for (Entry<String, Collection<String>> clause: filter.getMap().entrySet()) {
			where += whereValueInList(entityName, clause.getKey(), clause.getValue());			
		}
		return where;
	}

	public Collection<String> selectConcept(String columnName, Filter filter) throws SQLException {
		Collection<String> result = new ArrayList<String>();
		filter = new Filter(filter);
		Collection<String> langs = filter.getLanguages();
		if (langs.size() > 0) langs.add("*");

		String query = """
			select distinct %s from concept 
			where %s and conceptId in (
				select distinct concept.conceptId as conceptId
				from concept
				inner join word 
				on concept.conceptId = word.conceptId
				where concept.language = '*' and %s
			) order by conceptId, language	
		""";

		String whereConcept = buildWhere("concept", filter);
		String whereWord = buildWhere("word", filter);
		ResultSet rs = executeQuery(query, columnName, whereConcept, whereWord);
		while (rs.next()) {
			result.add(rs.getString(columnName));
		}
		return result;
	}

	public Object selectPolysemicEntries(Filter filter) throws SQLException {
		Metadata result = new Metadata();

		String query = """
			select lemma, conceptId, language from word where lemma || ":" || language in (
				select lemma || ":" || language from word where %s 
				group by lemma, language having count(*) > 1
			) order by lemma, language
		""";
		String where = buildWhere("word", filter);
		ResultSet rs = executeQuery(query, where);
		while (rs.next()) {
			Map<String,String> term = new HashMap<>();
			term.put("t", rs.getString("lemma"));
			term.put("c", rs.getString("conceptId"));
			term.put("l", rs.getString("language"));
			result.addToList("*", term);
		}
		return result.getObject("*");
	}

	public Map<String, Long> selectTermStats(Filter filter) throws SQLException {
		Map<String, Long> results = new LinkedHashMap<>();

		String where = buildWhere("word", filter);
		String query = "select language, count(*) as n from word where %s group by language";
		ResultSet rs = executeQuery(query, where);

		while (rs.next()) {
			String language = rs.getString("language");
			long count = rs.getLong("n");
			results.put(language, count);
		}

		return results;
	}

	public Collection<String> selectConceptDates(Filter filter) throws SQLException {
		Collection<String> results = new ArrayList<>();

		String where = buildWhere("concept", filter);
		String query = "select distinct date from concept where date is not null and %s";
		ResultSet rs = executeQuery(query, where);

		while (rs.next()) {
			String date = rs.getString("date");
			results.add(date);
		}

		return results;
	}
}	
