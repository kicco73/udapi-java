package cnr.ilc.lemon.resource;

import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;

public class Global implements GlobalInterface {
	final public TripleSerialiser triples = new TripleSerialiser();
	final public Metadata metadata = new Metadata();
	public String subjectField = null;
	public String language = "*";

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
		return triples.serialise();
	}

	@Override
	public String getJson() {
		return metadata.toJson(language);
	}
}
