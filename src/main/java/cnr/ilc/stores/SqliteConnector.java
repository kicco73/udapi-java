package cnr.ilc.stores;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cnr.ilc.rut.Logger;

public class SqliteConnector {
	private Connection connection;
	private Statement statement;

	private void createSchema() throws SQLException {
		executeUpdate("drop table if exists summary");
		executeUpdate("create table summary (language string, date string, metadata json)");
		executeUpdate("drop table if exists lexicon");
		executeUpdate("create table lexicon (language string, date string, metadata json, serialised string)");
		executeUpdate("drop table if exists word");
		executeUpdate("create table word (lemma string, language string, date string, conceptId string, metadata json, serialised string)");
		executeUpdate("create index word_lemma_idx on word (lemma)");
		executeUpdate("create index word_language_idx on word (language)");
		executeUpdate("create index word_date_idx on word (date)");
		executeUpdate("create index word_concept_idx on word (conceptId)");
		executeUpdate("drop table if exists concept");
		executeUpdate("create table concept (conceptId string, language string, date string, metadata json, serialised string)");
		executeUpdate("create index concept_id_idx on concept (conceptId)");
		executeUpdate("create index concept_language_idx on concept (language)");
		executeUpdate("create index concept_date_idx on concept (date)");
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
		String fullQuery = String.format(query, args);
		ResultSet rs = statement.executeQuery(fullQuery);
		return rs;
	}

	private String whereValueInList(String entityName, String columnName, Collection<String> values) {
		String where = "";
		if (values != null && values.size() > 0) {
			String listString = values.stream().collect(Collectors.joining("', '"));
			where += String.format(" AND %s.%s in ('%s')", entityName, columnName, listString);	
		}
		return where;
	}

	public String buildWhere(String entityName, Filter filter) {
		String where = "true";
		for (Entry<String, Collection<String>> clause: filter.get().entrySet()) {
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
			select %s from concept 
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
