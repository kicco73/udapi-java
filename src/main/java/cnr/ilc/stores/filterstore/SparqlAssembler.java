package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.PojoResource;
import cnr.ilc.lemon.resource.ResourceInterface;
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

	private Collection<ConceptInterface> getConcepts(Filter filter) throws SQLException {
		if (filter.isNoConcepts()) return new ArrayList<ConceptInterface>();

		Filter includeNullSubjectField = new Filter(filter);
		Collection<String> subjectFields = includeNullSubjectField.getSubjectFields();
		if (subjectFields.size() > 0)
			subjectFields.add(null);
		return db.selectConcepts(includeNullSubjectField);
	}

	private void processConcepts(Filter filter) throws SQLException {
		for (ConceptInterface concept: getConcepts(filter)) {
			output.append(concept.getSerialised());
		}
	}

	private Collection<String> getLanguages(Filter filter) {
		return null; // FIXME: TODO:
	}

	private Collection<GlobalInterface> getGlobals(Filter filter) throws Exception {
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

	private void processGlobals(Filter filter) throws Exception {
		Collection<GlobalInterface> globals = getGlobals(filter);
		for (GlobalInterface global: globals) {
			output.append(global.getSerialised());
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

	public ResourceInterface getResource(Filter filter) throws Exception {
		return new PojoResource(
			getLanguages(filter),
			getGlobals(filter), 
			getConcepts(filter),
			getWords(filter)
		);
	}
}	
