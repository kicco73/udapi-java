package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.WordInterface;

public class ResourceReader implements ResourceInterface {
	final private SqliteConnector db;
	private Filter filter = new Filter();

	public ResourceReader(SqliteConnector db) {
		this.db = db;
	}

	@Override
	public Collection<String> getLanguages() {
		return null; // FIXME: TODO:
	}

	public Collection<GlobalInterface> getGlobals() throws SQLException {
		Filter globalFilter = new Filter(filter);
		Collection<String> filterSubjectFields = globalFilter.getSubjectFields();

		if (globalFilter.isNoConcepts()) {
			filterSubjectFields.clear();
			filterSubjectFields.add(null);
		} else {
			if (filterSubjectFields.size() > 0)
				filterSubjectFields.add(null);

			Collection<String> usedSubjectFields = db.selectConcept("subjectField", globalFilter);
			filterSubjectFields.clear();
			filterSubjectFields.addAll(usedSubjectFields);		
			if (filterSubjectFields.size() > 0)
				filterSubjectFields.add(null);
		}

		ResultSet rs = db.selectEntity("global", globalFilter);

		Collection<GlobalInterface> globals = new ArrayList<>();
		while (rs.next()) {
			GlobalInterface global = db.hydrateGlobal(rs);
			globals.add(global);
		}
		return globals;
	}

	@Override
	public Collection<ConceptInterface> getConcepts() throws SQLException {
		if (filter.isNoConcepts()) return new ArrayList<ConceptInterface>();

		Filter includeNullSubjectField = new Filter(filter);
		Collection<String> subjectFields = includeNullSubjectField.getSubjectFields();
		if (subjectFields.size() > 0)
			subjectFields.add(null);
		return db.selectConcepts(includeNullSubjectField);
	}

	@Override
	public Collection<WordInterface> getWords() throws Exception {
		Collection<WordInterface> words = new ArrayList<>();
		ResultSet rs = db.selectEntity("word", filter);
		while (rs.next()) {
			WordInterface word = db.hydrateWord(rs);
			words.add(word);
		}
		return words;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

}	
