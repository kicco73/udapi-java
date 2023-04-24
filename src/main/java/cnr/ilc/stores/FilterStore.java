package cnr.ilc.stores;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.Logger;
import cnr.ilc.rut.Metadata;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.rut.Word;
import cnr.ilc.sparql.TripleSerialiser;

public class FilterStore extends MemoryStore {
	private SqliteConnector db = new SqliteConnector();
	private MetadataManager metadataManager = new MetadataManager(db);
	private Filter filter = new Filter();

	private void assembleConcept(String columnName) throws SQLException {
		for (String serialised: db.selectConcept(columnName, filter))
			append(serialised);
	}

	private void assembleEntity(String columnName, String entityName) throws SQLException {
		Filter filter = new Filter(this.filter);
		Collection<String> languages = filter.getLanguages();
		if (languages.size() > 0) languages.add("*");
		String where = db.buildWhere(entityName, filter);
		ResultSet rs = db.executeQuery("select count(*) as n from %s where %s", entityName, where);
		int total = rs.getInt("n");
		rs = db.executeQuery("select %s from %s where %s order by language", columnName, entityName, where);
		int current = 0;
		while (rs.next()) {
			Logger.progress(++current * 100 / total, "Assembling %s entity", entityName);
			String serialised = rs.getString(columnName);
			append(serialised);
		}
		Logger.progress(100,  "Done");
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
		db.executeUpdate("insert into lexicon (language, serialised) values ('%s', '%s')", 
			language, serialised);
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
			String date = (String) concept.metadata.getObject("*", "concepts", concept.id, "date");
			if (date != null) date = "'"+date+"'";
			metadata = metadata.replaceAll("'", "''");
			if (serialised.length() > 0 || !metadata.equals("null"))
				db.executeUpdate("insert into concept (conceptId, language, date, metadata, serialised) values ('%s', '%s', %s, '%s', '%s')", 
					concept.id, language, date, metadata, serialised);	
		}
	}

	@Override
	protected void appendWord(Word word) throws SQLException {
		String conceptId = null;
		String date = null;
		if (word.concept != null) {
			Concept concept = word.concept.get();
			conceptId = String.format("'%s'", concept.id);
			date = (String) concept.metadata.getObject("*", "concepts", concept.id, "date");
			date = date == null? null : String.format("'%s'", date);
		}

		String serialised = word.triples.serialise();
		serialised = serialised.replaceAll("'", "''");
		String lemma = word.canonicalForm.text.replaceAll("'", "''");
		String metadata = word.metadata.toJson(word.language);
		metadata = metadata.replaceAll("'", "''");
		db.executeUpdate("insert into word (lemma, language, date, conceptId, metadata, serialised) values ('%s', '%s', %s, %s, '%s', '%s')", 
			lemma, word.language, date, conceptId,  metadata, serialised);
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
		return metadataManager.getMetadata(filter);
	}

	public void setLanguages(Collection<String> languages) {
		filter.setLanguages(languages);
	}

	public void setDates(Collection<String> dates) {
		filter.setDates(dates);
	}

}	
