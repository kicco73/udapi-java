package cnr.ilc.stores;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.Metadata;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.rut.Word;
import cnr.ilc.sparql.TripleSerialiser;

public class FilterStore extends MemoryStore {
	private SqliteConnector db = new SqliteConnector();
	private MetadataManager metadataManager = new MetadataManager(db);
	private Collection<String> languages = new HashSet<>();

	private void assembleConcept(String columnName) throws SQLException {
		for (String serialised: db.selectConcept(columnName, this.languages))
			append(serialised);
	}

	private void assembleEntity(String columnName, String entityName) throws SQLException {
		Collection<String> languages = new HashSet<String>(this.languages);
		if (languages.size() > 0) languages.add("*");
		String where = db.buildWhere(entityName, languages);
		ResultSet rs = db.executeQuery("select %s from %s where %s order by language", columnName, entityName, where);
		while (rs.next()) {
			String serialised = rs.getString(columnName);
			append(serialised);
		}
	}

	@Override 
	public void store(ResourceInterface resource) throws Exception {
		super.store(resource);
		Metadata metadata = new Metadata();
		metadata.putInMap("*", resource.getSummary(), "summary");
		String json = metadata.toJson("*").replaceAll("'", "''");
		db.executeUpdate("insert into summary (language, metadata) values ('*', '%s')", json);	
	}

	@Override
	protected void appendLexicon(String lexiconFQN, String language) throws SQLException {
		TripleSerialiser triples = new TripleSerialiser();
		triples.addLexicon(lexiconFQN, language, creator);
		String serialised = triples.serialise();
		serialised = serialised.replaceAll("'", "''");
		String metadata = String.format("{\"summary\": {\"languages\": [\"%s\"]}}", language);
		db.executeUpdate("insert into lexicon (language, metadata, serialised) values ('%s', '%s', '%s')", 
			language, metadata, serialised);
	}

	@Override
	protected void appendConcept(Concept concept, Collection<String> languages) throws SQLException {
		Collection<String> langs = new HashSet<>();
		langs.add("*");
		langs.addAll(languages);
		for (String language: langs) {
			String serialised = concept.triples.serialise(language);
			serialised = serialised.replaceAll("'", "''");
			String metadata = concept.metadata.toJson(language);
			metadata = metadata.replaceAll("'", "''");
			if (serialised.length() > 0 || !metadata.equals("null"))
				db.executeUpdate("insert into concept (conceptId, language, metadata, serialised) values ('%s', '%s', '%s', '%s')", 
					concept.id, language, metadata, serialised);	
		}
	}

	@Override
	protected void appendWord(Word word) throws SQLException {
		String conceptId = word.concept == null? "null" : "'" + word.concept.get().id + "'";
		String serialised = word.triples.serialise();
		serialised = serialised.replaceAll("'", "''");
		String lemma = word.canonicalForm.text.replaceAll("'", "''");
		String metadata = word.metadata.toJson(word.language);
		metadata = metadata.replaceAll("'", "''");
		db.executeUpdate("insert into word (lemma, language, conceptId, metadata, serialised) values ('%s', '%s', %s, '%s', '%s')", 
			lemma, word.language, conceptId,  metadata, serialised);
	}

	public FilterStore(String namespace, String creator, int chunkSize, String fileName) throws Exception {
		super(namespace, creator, chunkSize);
		db.connect(fileName);
	}

	@Override
	public String getSparql() throws Exception {
		assembleEntity("serialised", "lexicon");
		assembleConcept("serialised");
		assembleEntity("serialised", "word");
		return super.getSparql();
	}

	@Override
	public Map<String,Object> getMetadata() throws Exception {
		return metadataManager.getMetadata(this.languages);
	}

	public void setLanguages(Collection<String> languages) {
		this.languages = languages;
	}

}	
