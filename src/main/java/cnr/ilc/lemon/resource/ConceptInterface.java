package cnr.ilc.lemon.resource;

public interface ConceptInterface {
	public String getId();
	public String getFQName();
	public String getDate();
	public String getSubjectField();
	public String getSubjectFieldFQN();
	public String getSerialised(String language);
	public String getSerialised();
	public String getJson(String language) throws Exception;
	public String getJson();
}
