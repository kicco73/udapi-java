package cnr.ilc.lemon.resource;

import java.util.Collection;

import cnr.ilc.rut.utils.Metadata;

public interface ConceptInterface {
	public String getId();
	public String getFQName();
	public String getDate();
	public String getSubjectField();
	public String getSerialised(String language);
	public String getSerialised();
	public Metadata getMetadata();
	public Collection<WordInterface> getWords();
}
