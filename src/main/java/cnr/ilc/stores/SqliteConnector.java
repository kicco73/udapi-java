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
import java.util.stream.Collectors;

public class SqliteConnector {
	private Connection connection;
	private Statement statement;

	private void createSchema() throws SQLException {
		executeUpdate("drop table if exists summary");
		executeUpdate("create table summary (language string, metadata json)");
		executeUpdate("drop table if exists lexicon");
		executeUpdate("create table lexicon (language string, metadata json, serialised string)");
		executeUpdate("drop table if exists word");
		executeUpdate("create table word (lemma string, language string, conceptId string, metadata json, serialised string)");
		executeUpdate("create index word_lemma_idx on word (lemma)");
		executeUpdate("create index word_language_idx on word (language)");
		executeUpdate("drop table if exists concept");
		executeUpdate("create table concept (conceptId string, language string, metadata json, serialised string)");
		executeUpdate("create index concept_language_idx on concept (language)");
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

	protected String buildWhere(String entityName, Collection<String> languages) {
		String where = "true";
		String languagesClause = languages.stream().collect(Collectors.joining("', '"));
		if (languages.size() > 0) 
			where += String.format(" AND %s.language in ('%s')", entityName, languagesClause);	
		return where;
	}

	public Collection<String> selectConcept(String columnName, Collection<String> languages) throws SQLException {
		Collection<String> result = new ArrayList<String>();
		Collection<String> langs = new HashSet<String>(languages);
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

		String whereConcept = buildWhere("concept", langs);
		String whereWord = buildWhere("word", langs);
		ResultSet rs = executeQuery(query, columnName, whereConcept, whereWord);
		while (rs.next()) {
			result.add(rs.getString(columnName));
		}
		return result;
	}
}	
