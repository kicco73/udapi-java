package cnr.ilc.sparql;

import java.util.Map;

import cnr.ilc.rut.ResourceInterface;

public interface TripleStoreInterface {
	public void store(ResourceInterface resource);
	public Map<String,Object> getMetadata();
	public String getSparql();
}
