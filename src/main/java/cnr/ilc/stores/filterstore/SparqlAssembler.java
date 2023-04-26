package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import cnr.ilc.lemon.PojoWord;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.utils.Logger;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.sparql.WordSerialiser;

public class SparqlAssembler {
	protected SPARQLWriter output;
	private SqliteConnector db;
	private PolysemicSupport ps;

	public SparqlAssembler(SqliteConnector sql, SPARQLWriter writer) {
		output = writer;
		db = sql;
		ps = new PolysemicSupport(db);
	}

	private void assembleConcept(Filter filter) throws SQLException {
		Filter includeNullSubjectField = new Filter(filter);
		Collection<String> subjectFields = includeNullSubjectField.getSubjectFields();
		if (subjectFields.size() > 0)
			subjectFields.add(null);

		for (String serialised: db.selectConcept("serialised", includeNullSubjectField))
			output.append(serialised);
	}

	private void assembleEntity(String entityName, Filter filter, String ...columnNames) throws SQLException {
		filter = new Filter(filter);
		Collection<String> languages = filter.getLanguages();
		if (languages.size() > 0) languages.add("*");
		ResultSet rs = db.selectEntity(entityName, filter);
		int total = rs.getFetchSize();
		int current = 0;
		while (rs.next()) {
			Logger.progress(++current * 100 / total, "Assembling %s entity", entityName);
			for (String columnName: columnNames) {
				String serialised = rs.getString(columnName);
				output.append(serialised);
			}
		}
		Logger.progress(100,  "Done");
	}

	private void assembleGlobal(Filter filter) throws Exception {
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

		assembleEntity("global", globalFilter, "serialised");
	}

	private void assembleWordReplacements(Collection<WordInterface> words) {
		for (WordInterface word: words) {
			PojoWord p = (PojoWord) word;
			output.addComment("Synthesised polysemic term `%s`@%s with %d senses", p.getLemma(), p.getLanguage(), p.senses.size());
			output.append(word.getSerialised());
			output.append(WordSerialiser.serialiseLexicalSenses(word));
		}
	}

	private void assembleWord(Filter filter) throws Exception {
		Collection<WordInterface> replacingEntries = ps.findAndResolvePolysemicEntries(filter);

		Filter filterOutPolysemicGroups = new Filter(filter);
		filterOutPolysemicGroups.setNoPolysemicGroups(true);

		assembleEntity("word", filterOutPolysemicGroups, "serialised", "serialisedSenses");
		assembleWordReplacements(replacingEntries);
	}

	public String getSparql(Filter filter) throws Exception {

		assembleGlobal(filter);

		if (!filter.isNoConcepts())
			assembleConcept(filter);		

		assembleWord(filter);
		return output.getSparql();
	}
}	
