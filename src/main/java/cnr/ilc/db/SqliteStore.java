package cnr.ilc.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.rut.SPARQLEntity;
import cnr.ilc.rut.SPARQLFormatter;
import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.rut.TripleStoreInterface;
import cnr.ilc.rut.Word;

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
		executeUpdate("create table lexicon (id string, FQName string, serialised string)");
		executeUpdate("drop table if exists word");
		executeUpdate("create table word (lemma string, partOfSpeech string, language string, concept string, serialised string)");
		executeUpdate("drop table if exists concept");
		executeUpdate("create table concept (id string, serialised string)");
	}

	private void writeLexicons(Map<String, String> lexicons) {
		for (Entry<String, String> lexicon: lexicons.entrySet()) {
			SPARQLEntity formatter = new SPARQLEntity();
			formatter.addLexicon(lexicon.getValue(), lexicon.getKey(), creator);
			executeUpdate("insert into lexicon (id, FQName, serialised) values ('%s', '%s', '%s')", 
				lexicon.getKey(), lexicon.getValue(), formatter.serialise());
		}
	}

	private void writeWords(Collection<Word> words) {
		if (words == null) return;
		for (Word word: words) {
			String conceptId = word.concept == null? "null" : "'" + word.concept.get().id + "'";
			String serialised = word.serialise();
			serialised = serialised.replaceAll("'", "''");
			executeUpdate("insert into word (lemma, partOfSpeech, language, concept, serialised) values ('%s', '%s', '%s', %s, '%s')", 
				word.canonicalForm.text, word.partOfSpeech, word.language, conceptId,  serialised);
		}
	}

	private void writeConcepts(Collection<Concept> concepts) {
		if (concepts == null) return;
		for (Concept concept: concepts) {
			String serialised = concept.serialise();
			serialised = serialised.replaceAll("'", "''");
			executeUpdate("insert into concept (id, serialised) values ('%s', '%s')", 
				concept.id, serialised);
			writeWords(concept.words);
		}
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
	public void serialise(ResourceInterface resource) {
		writeLexicons(resource.getLexicons());
		writeConcepts(resource.getConcepts());
		writeWords(resource.getWords());

	}

	@Override
	public String serialised() {
		append("INSERT DATA {\n");
		assembleEntity("lexicon");
		assembleEntity("concept");
		assembleEntity("word");
		append("}");
		return super.serialised();
	}

}	
