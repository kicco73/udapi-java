package cnr.ilc.stores.filterstore;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.stores.MemoryStore;

public class FilterStore extends MemoryStore {
	private SqliteConnector db = new SqliteConnector();
	private MetadataManager metadataManager = new MetadataManager(db);
	private SparqlAssembler sparqlAssembler;
	private Filter filter = new Filter();

	@Override
	protected void appendGlobal(Global global) throws SQLException {
		String serialised = global.triples.serialise();
		String metadata = global.metadata.toJson(global.language);
		db.executeUpdate("insert into global (language, subjectField, metadata, serialised) values ('%s', %s, %s, %s)", 
			global.language, db.quote(global.subjectField), db.quote(metadata), db.quote(serialised));
	}

	@Override
	protected void appendConcept(ConceptInterface concept, Collection<String> languages) throws SQLException {
		Collection<String> langs = new HashSet<>();
		langs.add("*");
		langs.addAll(languages);
		for (String language: langs) {
			String serialised = concept.getSerialised(language);
			String metadata = concept.getMetadata().toJson(language);
			String subjectField = concept.getSubjectField();

			if (serialised.length() > 0 || !metadata.equals("null"))
				db.executeUpdate("insert into concept (conceptId, language, date, subjectField, metadata, serialised) values ('%s', '%s', %s, %s, %s, %s)", 
					concept.getId(), language, db.quote(concept.getDate()), db.quote(subjectField), db.quote(metadata), db.quote(serialised));	
		}
	}

	@Override
	protected void appendWord(WordInterface word) throws SQLException {
		String conceptId = null;
		String subjectField = null;
		String date = null;
		if (word.getConcept() != null) {
			ConceptInterface concept = word.getConcept();
			date = concept.getDate();
			conceptId = concept.getId();
			subjectField = concept.getSubjectField();
		}

		String serialised = word.getSerialised();
		String lemma = word.getLemma();
		String language = word.getLanguage();
		String metadata = word.getMetadata().toJson(language);
		String fqName = word.getFQName();

		db.executeUpdate("insert into word (lemma, language, date, conceptId, subjectField, FQName, metadata, serialised) values ('%s', '%s', %s, %s, %s, %s, %s, %s)", 
			lemma, language, db.quote(date), db.quote(conceptId), db.quote(subjectField), db.quote(fqName), db.quote(metadata), db.quote(serialised));
	}

	public FilterStore(String namespace, String creator, int chunkSize, String fileName) throws Exception {
		super(namespace, creator, chunkSize);
		sparqlAssembler = new SparqlAssembler(db, output);
		db.connect(fileName);
	}

	@Override
	public String getSparql() throws Exception {
		return sparqlAssembler.getSparql(filter);
	}

	@Override
	public Map<String,Object> getMetadata() throws Exception {
		return metadataManager.getMetadata(filter);
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

}	
