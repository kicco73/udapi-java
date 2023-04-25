package cnr.ilc.rut.resource;

import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;

public class Global {
	final public TripleSerialiser triples = new TripleSerialiser();
	final public Metadata metadata = new Metadata();
	public String subjectField = null;
	public String language = "*";
}
