package cnr.ilc.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.Word;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.sparql.TripleSerialiser;

public class SqliteStore extends SPARQLWriter {
	private Connection connection;
	private Statement statement;

	private void executeUpdate(String query, Object ...args) {
		try {
			query = String.format(query, args);
			statement.executeUpdate(query);
		} 
		catch(SQLException e) {
			System.err.println(e.getMessage() + "\nQuery: " + query);
			System.err.println(e.getMessage());
		}
	}

	private ResultSet executeQuery(String query, Object ...args) {
		try {
			ResultSet rs = statement.executeQuery(String.format(query, args));
			return rs;
		} 
		catch(SQLException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	private void createSchema() throws SQLException {
		executeUpdate("drop table if exists lexicon");
		executeUpdate("create table lexicon (language string, FQName string, serialised string)");
		executeUpdate("drop table if exists word");
		executeUpdate("create table word (lemma string, partOfSpeech string, language string, concept string, serialised string)");
		executeUpdate("drop table if exists concept");
		executeUpdate("create table concept (id string, serialised string)");
	}

	private void assembleEntity(String entityName) {
		try {
			ResultSet rs = executeQuery("select * from " + entityName);
			while (rs.next()) {
				String serialised = rs.getString("serialised");
				append(serialised);
			}
		}
		catch(SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Override
	protected void appendLexicon(String lexiconFQN, String language) {
		TripleSerialiser triples = new TripleSerialiser();
		triples.addLexicon(lexiconFQN, language, creator);
		String serialised = triples.serialise();
		serialised = serialised.replaceAll("'", "''");
		executeUpdate("insert into lexicon (language, FQName, serialised) values ('%s', '%s', '%s')", 
			language, lexiconFQN, serialised);
	}

	@Override
	protected void appendConcept(Concept concept) {
		String serialised = concept.triples.serialise();
		serialised = serialised.replaceAll("'", "''");
		executeUpdate("insert into concept (id, serialised) values ('%s', '%s')", 
			concept.id, serialised);
	}

	@Override
	protected void appendWord(Word word) {
		String conceptId = word.concept == null? "null" : "'" + word.concept.get().id + "'";
		String serialised = word.triples.serialise();
		serialised = serialised.replaceAll("'", "''");
		executeUpdate("insert into word (lemma, partOfSpeech, language, concept, serialised) values ('%s', '%s', '%s', %s, '%s')", 
			word.canonicalForm.text, word.partOfSpeech, word.language, conceptId,  serialised);
	}

	public SqliteStore(String namespace, String creator, int chunkSize, String fileName) {
		super(namespace, creator, chunkSize);
		try {
			String url = String.format("jdbc:sqlite:%s", fileName);
			connection = DriverManager.getConnection(url);
			statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			createSchema();
		}
		catch(SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
	}

	@Override
	public String serialised() {
		assembleEntity("lexicon");
		assembleEntity("concept");
		assembleEntity("word");
		return super.serialised();
	}

}	
