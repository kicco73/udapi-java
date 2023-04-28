package cnr.ilc.lemon.resource;

public class PojoSense implements SenseInterface {
	final private String FQName;
	final private String conceptFQN;
	final private String serialised;

	public PojoSense(String FQName, String conceptFQN, String serialised) {
		this.FQName = FQName;
		this.conceptFQN = conceptFQN;
		this.serialised = serialised;
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
		return serialised;
	}

}
