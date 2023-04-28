package cnr.ilc.lemon.resource;

public class PojoGlobal implements GlobalInterface {
	final private String language;
	final private String subjectField;
	final private String serialised;
	final private String json;

	public PojoGlobal(String language, String subjectField, String serialised, String json) {
		this.language = language;
		this.subjectField = subjectField;
		this.serialised = serialised;
		this.json = json;
	}

	@Override
	public String getSubjectField() {
		return subjectField;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public String getSerialised() {
		return serialised;
	}

	@Override
	public String getJson() {
		return json;
	}

}
