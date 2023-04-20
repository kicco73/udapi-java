package cnr.ilc.sparql;

import cnr.ilc.rut.ResourceInterface;

public interface TripleStoreInterface {
	public void serialise(ResourceInterface resource);
	public String getMetadata();
	public String serialised();
}
