package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.sparql.WordSerialiser;
import cnr.ilc.processors.ProcessorInterface;

public class SparqlAssembler {
	protected SPARQLWriter output;
	final private SqliteConnector db;
	final private ProcessorInterface processor;

	public SparqlAssembler(SqliteConnector sql, ProcessorInterface postProcessor, SPARQLWriter writer) {
		db = sql;
		processor = postProcessor;
		output = writer;
	}

	private void processConcepts(Filter filter) throws SQLException {
		if (filter.isNoConcepts()) return;
		Filter includeNullSubjectField = new Filter(filter);
		Collection<String> subjectFields = includeNullSubjectField.getSubjectFields();
		if (subjectFields.size() > 0)
			subjectFields.add(null);

		for (String serialised: db.selectConcept("serialised", includeNullSubjectField)) {
			output.append(serialised);
		}
	}

	private void processGlobals(Filter filter) throws Exception {
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
		while (rs.next()) {
			output.append(rs.getString("serialised"));
		}
	}

	private Collection<WordInterface> getWords(Filter filter) throws Exception {
		Collection<WordInterface> words = new ArrayList<>();
		ResultSet rs = db.selectEntity("word", filter);
		while (rs.next()) {
			WordInterface word = db.hydrateWord(rs);
			words.add(word);
		}
		return words;
	}

	private String coalesce(Collection<WordInterface> words, TripleSerialiser triples) throws Exception {
		for (WordInterface word: words) {
			output.append(word.getSerialised());
			output.append(WordSerialiser.serialiseLexicalSenses(word));
		}
		output.append(triples.serialise());
		return output.getSparql();
	}

	public String getSparql(Filter filter) throws Exception {
		TripleSerialiser triples = new TripleSerialiser();
		processGlobals(filter);
		processConcepts(filter);		
		Collection<WordInterface> words = getWords(filter);
		processor.filter(words, triples);
		return coalesce(words, triples);
	}
}	
