
public class Variable extends Symbol {

	private int value;

	public Variable(int value, int pointer) {
		super(pointer);
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
