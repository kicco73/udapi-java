package cnr.ilc.stores.filterstore;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.sparql.WordSerialiser;
import cnr.ilc.stores.TripleStoreInterface;

public class ResourceWriter implements TripleStoreInterface {
	private SqliteConnector db;

	public ResourceWriter(SqliteConnector db) {
		this.db = db;
	}

	private void storeGlobal(GlobalInterface global) throws SQLException {
		String serialised = global.getSerialised();
		String metadata = global.getJson();
		db.executeUpdate("insert into global (language, subjectField, metadata, serialised) values ('%s', %s, %s, %s)",
				global.getLanguage(), db.quote(global.getSubjectField()), db.quote(metadata), db.quote(serialised));
	}

	private void storeConcept(ConceptInterface concept, Collection<String> languages) throws SQLException {
		Collection<String> langs = new HashSet<>();
		langs.add("*");
		langs.addAll(languages);
		for (String language : langs) {
			String serialised = concept.getSerialised(language);
			String metadata = concept.getJson();
			String subjectField = concept.getSubjectField();

			if (serialised.length() > 0 || !metadata.equals("null")) {
				db.executeUpdate(
					"insert into concept (conceptId, language, date, subjectField, metadata, serialised) values ('%s', '%s', %s, %s, %s, %s)",
					concept.getId(), language, db.quote(concept.getDate()), db.quote(subjectField),
					db.quote(metadata), db.quote(serialised));
			}
		}
	}

	private void storeTerm(TermInterface word) throws SQLException {
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
		String senseFQN = word.getSenses().iterator().next().getFQName(); // TODO: handle multiple senses

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
				db.quote(senseFQN), db.quote(serialisedSenses));
	}

	public void store(ResourceInterface input) throws Exception {

		db.clear();

		for (GlobalInterface global : input.getGlobals())
			storeGlobal(global);

		for (ConceptInterface concept : input.getConcepts())
			storeConcept(concept, input.getLanguages());

		for (TermInterface word : input.getTerms())
			storeTerm(word);

		db.markPolysemicGroups();
	}

}
