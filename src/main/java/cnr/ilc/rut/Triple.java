package cnr.ilc.rut;

public class Triple<F,S,T> {
	final public F first;
	final public S second;
	final public T third;

	public Triple(F first, S second, T third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
}
