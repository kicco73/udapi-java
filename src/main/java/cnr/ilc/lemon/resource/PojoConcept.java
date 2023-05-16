package cnr.ilc.lemon.resource;

public class PojoConcept implements ConceptInterface {
	final private String id;
	final private String FQName;
	final private String date;
	final private String subjectField;
	final private String subjectFieldFQN;
	final private String serialised;
	final private String json;

	public PojoConcept(String id, String FQName, String subjectField, String subjectFieldFQN, String date, String serialised, String json) {
		this.id = id;
		this.FQName = FQName;
		this.subjectField = subjectField;
		this.subjectFieldFQN = subjectFieldFQN;
		this.date = date;
		this.serialised = serialised;
		this.json = json;
	}

	@Override
	public String getSubjectField() {
		return subjectField;
	}


	@Override
	public String getSubjectFieldFQN() {
		return subjectFieldFQN;
	}

	@Override
	public String getSerialised() {
		return serialised;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFQName() {
		return FQName;
	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getSerialised(String language) {
		return serialised;
	}

	@Override
	public String getJson() {
		return json;
	}

	@Override
	public String getJson(String language) throws Exception {
		throw new UnsupportedOperationException("Unimplemented method getJson(language) called");
	}

}
