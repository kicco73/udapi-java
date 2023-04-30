package cnr.ilc.lemon.resource;

import java.util.Collection;

public interface ConceptInterface {
	public String getId();
	public String getFQName();
	public String getDate();
	public String getSubjectField();
	public String getSerialised(String language);
	public String getSerialised();
	public String getJson();
	public Collection<TermInterface> getTerms();
	public String getSubjectFieldFQN();
}
