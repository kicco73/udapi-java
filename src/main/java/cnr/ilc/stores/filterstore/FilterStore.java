package cnr.ilc.stores.filterstore;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.WordSerialiser;
import cnr.ilc.stores.MemoryStore;

public class FilterStore extends MemoryStore {
	private SqliteConnector db = new SqliteConnector();
	private SparqlAssembler sparqlAssembler;
	private MetadataMerger metadataManager = new MetadataMerger(db);
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
		String conceptFQN = null;
		if (word.getConcept() != null) {
			ConceptInterface concept = word.getConcept();
			date = concept.getDate();
			conceptId = concept.getId();
			conceptFQN = concept.getFQName();
			subjectField = concept.getSubjectField();
		}

		String serialised = word.getSerialised();
		String lemma = word.getLemma();
		String language = word.getLanguage();
		String metadata = word.getMetadata().toJson(language);
		String fqName = word.getFQName();
		String serialisedSenses = WordSerialiser.serialiseLexicalSenses(word);
		String senseFQN = word.getSenses().iterator().next().getFQName();	// TODO: handle multiple senses
		
		String query = """
				insert into word (
					lemma, language, date, conceptId, conceptFQN, subjectField, 
					FQName, metadata, serialised, senseFQN, serialisedSenses
				) values ('%s', '%s', %s, %s, %s, %s, %s, %s, %s, %s, %s)
		""";

		db.executeUpdate(query, 
			lemma, language, 
			db.quote(date), db.quote(conceptId), db.quote(conceptFQN), db.quote(subjectField), 
			db.quote(fqName), db.quote(metadata), db.quote(serialised), 
			db.quote(senseFQN), db.quote(serialisedSenses)
		);
	}

	public FilterStore(String namespace, String creator, int chunkSize, Filter filter, String fileName) throws Exception {
		super(namespace, creator, chunkSize, filter);
		db.connect(fileName);
		sparqlAssembler = new SparqlAssembler(db, processor, output);
	}

	@Override
	protected void finaliseStore() throws Exception {
		db.markPolysemicGroups();
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
