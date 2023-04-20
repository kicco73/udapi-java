package cnr.ilc.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.Metadata;
import cnr.ilc.rut.Word;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.sparql.TripleSerialiser;

@SuppressWarnings("unchecked")
public class SqliteStore extends SPARQLWriter {
	private Connection connection;
	private Statement statement;
	private Collection<String> languages = new ArrayList<>();
	private JSONParser jsonParser = new JSONParser();

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
		executeUpdate("create table lexicon (language string, metadata json, serialised string)");
		executeUpdate("drop table if exists word");
		executeUpdate("create table word (lemma string, partOfSpeech string, language string, conceptId string, metadata json, serialised string)");
		executeUpdate("create index word_lemma_idx on word (lemma)");
		executeUpdate("create index word_language_idx on word (language)");
		executeUpdate("drop table if exists concept");
		executeUpdate("create table concept (id string, language string, metadata json, serialised string)");
	}

	private String buildWhere(String entityName) {
		String where = "true";
		String languagesClause = languages.stream().collect(Collectors.joining("', '"));
		if (languages.size() > 0) where += " AND language in ('*', '" + languagesClause + "')";	
		return where;
	}

	private void assembleEntity(String columnName, String entityName) {
		try {
			String where = buildWhere(entityName);
			String query = String.format("select %s from %s where %s", columnName, entityName, where);
			ResultSet rs = executeQuery(query);
			while (rs.next()) {
				String serialised = rs.getString(columnName);
				append(serialised);
			}
		}
		catch(SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	private void mergeJson(Metadata metadata, String columnName, String entityName) throws Exception {
		try {
			String where = buildWhere(entityName);
			String query = String.format("select %s from %s where %s", columnName, entityName, where);
			ResultSet rs = executeQuery(query);
			while (rs.next()) {
				String jsonString = rs.getString(columnName);
				Map<String, Object> json = (Map<String, Object>) jsonParser.parse(jsonString);
				metadata.merge(json);
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
		String metadata = String.format("{\"languages\": [\"%s\"]}", language);
		executeUpdate("insert into lexicon (language, metadata, serialised) values ('%s', '%s', '%s')", 
			language, metadata, serialised);
	}

	@Override
	protected void appendConcept(Concept concept) {
		for (String language: concept.triples.getLanguages()) {
			String serialised = concept.triples.serialise(language);
			serialised = serialised.replaceAll("'", "''");
			String metadata = concept.metadata.serialise();
			metadata = metadata.replaceAll("'", "''");
			executeUpdate("insert into concept (id, language, metadata, serialised) values ('%s', '%s', '%s', '%s')", 
				concept.id, language, metadata, serialised);	
		}
	}

	@Override
	protected void appendWord(Word word) {
		String conceptId = word.concept == null? "null" : "'" + word.concept.get().id + "'";
		String serialised = word.triples.serialise();
		serialised = serialised.replaceAll("'", "''");
		String lemma = word.canonicalForm.text.replaceAll("'", "''");
		String metadata = word.metadata.serialise();
		metadata = metadata.replaceAll("'", "''");
		executeUpdate("insert into word (lemma, partOfSpeech, language, conceptId, metadata, serialised) values ('%s', '%s', '%s', %s, '%s', '%s')", 
			lemma, word.partOfSpeech, word.language, conceptId,  metadata, serialised);
	}

	public SqliteStore(String namespace, String creator, int chunkSize, String fileName) {
		super(namespace, creator, chunkSize);
		try {
			boolean mustCreateSchema = !new File(fileName).exists();
			String url = String.format("jdbc:sqlite:%s", fileName);
			connection = DriverManager.getConnection(url);
			statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec
			if (mustCreateSchema) createSchema();
		}
		catch(SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
	}

	@Override
	public String serialised() {
		assembleEntity("serialised", "lexicon");
		assembleEntity("serialised", "concept");
		assembleEntity("serialised", "word");
		return super.serialised();
	}

	@Override
	public String getMetadata() {
		Metadata metadata = new Metadata();
		try {
			mergeJson(metadata, "metadata", "lexicon");
			mergeJson(metadata, "metadata", "concept");
			mergeJson(metadata, "metadata", "word");	
		}
		catch(Exception e) {
			System.err.println("Error: cannot merge metadata");
			return "{}";
		}
		return metadata.serialise();
	}

	public void setLanguages(Collection<String> languages) {
		this.languages = languages;
	}

}	
