package tk.jlowe.smla.compiler;

import javax.print.DocFlavor.STRING;

public class LineNumber implements Symbol {
	
	private int codeValue;
	private String name;
	
	public LineNumber(String string) {
		codeValue = Integer.parseInt(string);
		name = "LineNumber" + string;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public int getValue() {
		return codeValue;
	}

}
