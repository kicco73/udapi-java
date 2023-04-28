/**
 * @author Enrico Carniani
 */

package cnr.ilc.lemon.resource;

import java.util.Collection;

import cnr.ilc.rut.utils.Metadata;

public interface WordInterface {
	public String getLemma();
	public String getPartOfSpeech();
	public String getLanguage();
	public ConceptInterface getConcept();
	public Metadata getMetadata();
	public String getSerialised();
	public String getFQName();
	public String getConceptFQN();
	public String getCreator();
	public Collection<SenseInterface> getSenses();
}
