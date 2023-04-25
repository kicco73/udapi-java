package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import cnr.ilc.rut.resource.Concept;
import cnr.ilc.rut.resource.Global;
import cnr.ilc.rut.resource.Word;
import cnr.ilc.rut.utils.Logger;
import cnr.ilc.stores.MemoryStore;

public class FilterStore extends MemoryStore {
	private SqliteConnector db = new SqliteConnector();
	private MetadataManager metadataManager = new MetadataManager(db);
	private Filter filter = new Filter();

	private void assembleConcept() throws SQLException {
		for (String serialised: db.selectConcept("serialised", filter))
			append(serialised);
	}

	private void assembleEntity(String entityName, Filter filter) throws SQLException {
		filter = new Filter(filter);
		Collection<String> languages = filter.getLanguages();
		if (languages.size() > 0) languages.add("*");
		String where = db.buildWhere(entityName, filter);
		ResultSet rs = db.executeQuery("select count(*) as n from %s where %s", entityName, where);
		int total = rs.getInt("n");
		rs = db.executeQuery("select serialised from %s where %s order by language", entityName, where);
		int current = 0;
		while (rs.next()) {
			Logger.progress(++current * 100 / total, "Assembling %s entity", entityName);
			String serialised = rs.getString("serialised");
			append(serialised);
		}
		Logger.progress(100,  "Done");
	}

	@Override
	protected void appendGlobal(Global global) throws SQLException {
		String serialised = global.triples.serialise();
		String metadata = global.metadata.toJson(global.language);
		db.executeUpdate("insert into global (language, subjectField, metadata, serialised) values ('%s', %s, %s, %s)", 
			global.language, db.quote(global.subjectField), db.quote(metadata), db.quote(serialised));
	}

	@Override
	protected void appendConcept(Concept concept, Collection<String> languages) throws SQLException {
		Collection<String> langs = new HashSet<>();
		langs.add("*");
		langs.addAll(languages);
		for (String language: langs) {
			String serialised = concept.triples.serialise(language);
			String metadata = concept.metadata.toJson(language);
			String subjectField = concept.getSubjectField();

			if (serialised.length() > 0 || !metadata.equals("null"))
				db.executeUpdate("insert into concept (conceptId, language, date, subjectField, metadata, serialised) values ('%s', '%s', %s, %s, %s, %s)", 
					concept.id, language, db.quote(concept.date), db.quote(subjectField), db.quote(metadata), db.quote(serialised));	
		}
	}

	@Override
	protected void appendWord(Word word) throws SQLException {
		String conceptId = null;
		String subjectField = null;
		String date = null;
		if (word.concept != null) {
			Concept concept = word.concept.get();
			date = concept.date;
			conceptId = concept.id;
			subjectField = concept.getSubjectField();
		}

		String serialised = word.triples.serialise();
		String lemma = word.canonicalForm.text;
		String metadata = word.metadata.toJson(word.language);

		db.executeUpdate("insert into word (lemma, language, date, conceptId, subjectField, metadata, serialised) values ('%s', '%s', %s, %s, %s, %s, %s)", 
			lemma, word.language, db.quote(date), db.quote(conceptId), db.quote(subjectField), db.quote(metadata), db.quote(serialised));
	}

	public FilterStore(String namespace, String creator, int chunkSize, String fileName) throws Exception {
		super(namespace, creator, chunkSize);
		db.connect(fileName);
	}

	@Override
	public String getSparql() throws Exception {
		Filter includeNullSubjectField = new Filter(filter);
		Collection<String> subjectFields = includeNullSubjectField.getSubjectFields();
		if (subjectFields.size() > 0)
			subjectFields.add(null);

		assembleEntity("global", includeNullSubjectField);

		if (!filter.isNoConcepts())
			assembleConcept();

		assembleEntity("word", filter);
		return super.getSparql();
	}

	@Override
	public Map<String,Object> getMetadata() throws Exception {
		return metadataManager.getMetadata(filter);
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

}	
