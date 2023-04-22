package cnr.ilc.sparql;

import java.util.Map;

import cnr.ilc.rut.ResourceInterface;

public interface TripleStoreInterface {
	public void serialise(ResourceInterface resource);
	public Map<String,Object> getMetadata(ResourceInterface resource);
	public String serialised();
}
