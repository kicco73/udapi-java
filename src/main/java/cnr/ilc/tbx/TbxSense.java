package cnr.ilc.tbx;

import java.util.Map;

import cnr.ilc.lemon.resource.Sense;
import cnr.ilc.lemon.resource.Word;

public class TbxSense extends Sense {

	public TbxSense(Word word, Map<String,String> context) {
		super(word, "", null);
		triples.add(FQName, "ontolex:usage", context, word.getLanguage());
	}

}
