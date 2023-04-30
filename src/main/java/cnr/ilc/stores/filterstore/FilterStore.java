package cnr.ilc.stores.filterstore;

import java.util.Collection;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.rut.Filter;
import cnr.ilc.stores.TripleStoreInterface;

public class FilterStore implements TripleStoreInterface, ResourceInterface {
	private ResourceReader reader;
	private ResourceWriter writer;

	public FilterStore(SqliteConnector db) {
		reader = new ResourceReader(db);
		writer = new ResourceWriter(db);
	}

	public void store(ResourceInterface input) throws Exception {
		writer.store(input);
	}

	@Override
	public Collection<String> getLanguages() throws Exception {
		return reader.getLanguages();
	}

	@Override
	public Collection<GlobalInterface> getGlobals() throws Exception {
		return reader.getGlobals();
	}

	@Override
	public Collection<ConceptInterface> getConcepts() throws Exception {
		return reader.getConcepts();
	}

	@Override
	public Collection<TermInterface> getTerms() throws Exception {
		return reader.getTerms();
	}

	public void setFilter(Filter filter) {
		reader.setFilter(filter);
	}

}	
