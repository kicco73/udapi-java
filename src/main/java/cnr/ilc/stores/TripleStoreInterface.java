package cnr.ilc.stores;

import cnr.ilc.lemon.resource.ResourceInterface;

public interface TripleStoreInterface {
	public void store(ResourceInterface resource) throws Exception;
}
