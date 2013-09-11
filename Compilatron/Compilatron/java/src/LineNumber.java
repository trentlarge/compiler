
public class LineNumber extends Symbol {
	
	private int codeValue;

	public LineNumber(int line, int pointer) {
		super(pointer);
		codeValue = line;
	}

	public int getValue() {
		return codeValue;
	}
}
