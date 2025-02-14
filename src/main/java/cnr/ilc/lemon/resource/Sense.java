package cnr.ilc.lemon.resource;

import cnr.ilc.sparql.TripleSerialiser;

public class Sense implements SenseInterface {
	protected TripleSerialiser triples = new TripleSerialiser();
	final private String FQName;
	private String conceptFQN = null;

	public Sense(TermInterface word, String id, String definition) {
		FQName = String.format("%s_sense%s", word.getFQName(), id);

		triples.add(word.getFQName(), "ontolex:sense", FQName);        
		triples.add(FQName, "rdf:type", "ontolex:LexicalSense"); 

		if (definition != null)
			triples.addString(FQName, "skos:definition", definition);

		conceptFQN = word.getConceptFQN();
		if (conceptFQN != null) {
			triples.add(FQName, "ontolex:reference", conceptFQN); 
		}
	}

	@Override
	public String getFQName() {
		return FQName;
	}

	@Override
	public String getConceptFQN() {
		return conceptFQN;
	}

	@Override
	public String getSerialised() {
		return triples.serialise();
	}
	
}
