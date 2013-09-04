//package tk.jlowe.smla.compiler;

public class Variable implements Symbol {
	
	private String variableName;
	
	public Variable(String name){
		variableName = "Variable" + name;
	}
	
	public int hashCode(){
		return variableName.hashCode();
	}
}
