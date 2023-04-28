package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.utils.Logger;
import cnr.ilc.sparql.SPARQLFormatter;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.sparql.WordSerialiser;
import cnr.ilc.stores.filterstore.processors.NoSensesProcessor;
import cnr.ilc.stores.filterstore.processors.PolysemicProcessor;
import cnr.ilc.stores.filterstore.processors.ProcessorInterface;
import cnr.ilc.stores.filterstore.processors.SynonymsProcessor;

public class SparqlAssembler {
	protected SPARQLWriter output;
	final private SqliteConnector db;
	final private PolysemicProcessor polysemicProcessor;
	final private SynonymsProcessor synonymsProcessor = new SynonymsProcessor();
	final private NoSensesProcessor noSenseProcessor = new NoSensesProcessor();

	public SparqlAssembler(SqliteConnector sql, SPARQLWriter writer) {
		db = sql;
		output = writer;
		polysemicProcessor = new PolysemicProcessor(db);
	}

	private void assembleConcepts(Filter filter) throws SQLException {
		if (filter.isNoConcepts()) return;
		Filter includeNullSubjectField = new Filter(filter);
		Collection<String> subjectFields = includeNullSubjectField.getSubjectFields();
		if (subjectFields.size() > 0)
			subjectFields.add(null);

		for (String serialised: db.selectConcept("serialised", includeNullSubjectField)) {
			output.append(serialised);
		}
	}

	private Filter assembleEntity(String entityName, Filter filter, String ...columnNames) throws SQLException {
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
		return filter;
	}

	private void assembleGlobals(Filter filter) throws Exception {
		Filter globalFilter = new Filter(filter);
		Collection<String> filterSubjectFields = globalFilter.getSubjectFields();

		if (globalFilter.isNoConcepts()) {
			filterSubjectFields.clear();
			filterSubjectFields.add(null);
		} else {
			if (filterSubjectFields.size() > 0)
				filterSubjectFields.add(null);

			System.err.println("GLOB");
			Collection<String> usedSubjectFields = db.selectConcept("subjectField", globalFilter);
			filterSubjectFields.clear();
			filterSubjectFields.addAll(usedSubjectFields);		
			if (filterSubjectFields.size() > 0)
				filterSubjectFields.add(null);
		}

		assembleEntity("global", globalFilter, "serialised");
	}

	private void assembleWords(Collection<WordInterface> words) {
		for (WordInterface word: words) {
			output.append(word.getSerialised());
			output.append(WordSerialiser.serialiseLexicalSenses(word));
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

	private List<ProcessorInterface> buildPipeline(Filter filter) {
		List<ProcessorInterface> processors = new ArrayList<>();
		if (filter.isNoSenses()) {
			processors.add(noSenseProcessor);
		} else {
			processors.add(polysemicProcessor);
			if (filter.isSynonyms())
				processors.add(synonymsProcessor);
		}
		return processors;
	}

	private void assembleWords(Filter filter) throws Exception {
		List<ProcessorInterface> processors = buildPipeline(filter);
		TripleSerialiser triples = new TripleSerialiser();
		Collection<WordInterface> words = getWords(filter);

		for (ProcessorInterface processor: processors)
			words = processor.filter(words, triples);

		assembleWords(words);
		output.append(triples.serialise());
	}

	public String getSparql(Filter filter) throws Exception {
		assembleGlobals(filter);
		assembleConcepts(filter);		
		assembleWords(filter);
		return output.getSparql();
	}
}	
