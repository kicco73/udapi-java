package cnr.ilc.lemon.resource;

import cnr.ilc.sparql.TripleSerialiser;

public class Sense implements SenseInterface {
	protected TripleSerialiser triples = new TripleSerialiser();
	final public String FQName;

	public Sense(WordInterface word, String id, String definition) {
		FQName = String.format("%s_sense%s", word.getFQName(), id);

		triples.add(word.getFQName(), "ontolex:sense", FQName);        
		triples.add(FQName, "rdf:type", "ontolex:LexicalSense"); 

		if (definition != null)
			triples.addString(FQName, "skos:definition", definition);

		if (word.getConcept() != null)
			triples.add(FQName, "ontolex:reference", word.getConcept().getFQName()); 

		triples.addMetaData(FQName, word.getCreator()); 
	}

	@Override
	public String getSerialised() {
		return triples.serialise();
	}
	
}
