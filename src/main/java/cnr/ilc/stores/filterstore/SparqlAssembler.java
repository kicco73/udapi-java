package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.sparql.WordSerialiser;
import cnr.ilc.stores.filterstore.processors.NoSensesProcessor;
import cnr.ilc.stores.filterstore.processors.PolysemicProcessor;
import cnr.ilc.stores.filterstore.processors.ProcessorInterface;
import cnr.ilc.stores.filterstore.processors.SynonymsProcessor;
import cnr.ilc.stores.filterstore.processors.TranslateSenseProcessor;
import cnr.ilc.stores.filterstore.processors.TranslateTermProcessor;

public class SparqlAssembler {
	protected SPARQLWriter output;
	final private SqliteConnector db;
	final private NoSensesProcessor noSenseProcessor = new NoSensesProcessor();
	final private TranslateTermProcessor translateTermProcessor = new TranslateTermProcessor();
	final private PolysemicProcessor polysemicProcessor;
	final private TranslateSenseProcessor translateSenseProcessor = new TranslateSenseProcessor();
	final private SynonymsProcessor synonymsProcessor = new SynonymsProcessor();

	public SparqlAssembler(SqliteConnector sql, SPARQLWriter writer) {
		db = sql;
		output = writer;
		polysemicProcessor = new PolysemicProcessor(db);
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

	private List<ProcessorInterface> buildPipeline(Filter filter) {
		List<ProcessorInterface> processors = new ArrayList<>();
		if (filter.isNoSenses()) {
			processors.add(noSenseProcessor);
			if (filter.isTranslateTerms())
				processors.add(translateTermProcessor);
		} else {
			processors.add(polysemicProcessor);
			if (filter.isTranslateSenses())
				processors.add(translateSenseProcessor);
			if (filter.isSynonyms())
				processors.add(synonymsProcessor);
		}
		return processors;
	}

	private Collection<WordInterface> processWords(Filter filter, TripleSerialiser triples) throws Exception {
		List<ProcessorInterface> processors = buildPipeline(filter);
		Collection<WordInterface> words = getWords(filter);

		for (ProcessorInterface processor: processors)
			words = processor.filter(words, triples);

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
		Collection<WordInterface> words = processWords(filter, triples);
		return coalesce(words, triples);
	}
}	
