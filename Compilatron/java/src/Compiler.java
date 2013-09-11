import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
	static Pattern relation_pattern = Pattern.compile("(.+)(>|<|>=|<=|==|!=)+(.+)");
	static Pattern expression_pattern = Pattern.compile("([a-zA-Z0-9+-*/()]+)");

	Scanner scanner;
	int[] memory;
	ArrayList<Integer> constants;
	HashMap<Integer, Integer> line_numbers;
	HashMap<String, Integer> variables;
	int last_line_number, pointer, data_pointer;

	public Compiler(File file) throws FileNotFoundException {
		scanner = new Scanner(file);
		memory = new int[100];
		constants = new ArrayList<Integer>();
		line_numbers = new HashMap<Integer, Integer>();
		variables = new HashMap<String, Integer>();
		last_line_number = 0;
		pointer = 0;
		data_pointer = 99;
	}

	public int[] compile() throws OutOfMemoryException, LineNumberException, InvalidVariableException, UndefinedVaribleException, IllegalArgumentException {
		while(scanner.hasNextLine()) {
			if(pointer >= data_pointer)
				throw new OutOfMemoryException();

			String[] command = scanner.nextLine().split(" "); //Everything is separated by spaces

			//Double check line numbers and save each one as a symbol
			int line_number = Integer.parseInt(command[0]);
			if(line_number <= last_line_number)
				throw new LineNumberException();
			line_numbers.put(line_number, pointer);
			last_line_number = line_number;

			if(command[1].equalsIgnoreCase("rem")) //Ignore comment lines
				continue;
			else if(command[1].equalsIgnoreCase("input")) { //Make a new variable and remember it
				if(!Character.isLetter(command[2].charAt(0)))
					throw new InvalidVariableException();

				variables.put(command[2], data_pointer);
				memory[pointer] = 1000 + data_pointer;
				data_pointer--;
			}
			else if(command[1].equalsIgnoreCase("print")) { //Simply print variable
				if(!variables.containsKey(command[2]))
					throw new UndefinedVariableException();

				memory[pointer] = 1100 + variables.get(command[2]);
			}
			else if(command[1].equalsIgnoreCase("let")) { //If a variable doesn't exist, create it then parse the expression
				if(!variables.containsKey(command[2])) {
					variables.put(command[2], data_pointer);
					data_pointer--;
				}

				parseExpression(command[3], variables.get(command[2]));
			}
			else if(command[1].equalsIgnoreCase("goto")) { //Put a new goto
				memory[pointer] = 4000 + Integer.parseInt(command[2]);
			}
			else if(command[1].equalsIgnoreCase("if")) { //Yay for if's
				if(!command[3].equalsIgnoreCase("goto"))
					throw new IllegalArgumentException();

				parseRelation(command[2], Integer.parseInt(command[4])); //Call parse relation
			}
			else if(command[1].equalsIgnoreCase("end")) //Put a halt
				memory[pointer] = 4300;

			pointer++;
		}
	}

	private void parseRelation(String relation, int goto_symbol) throws SyntaxException {
		//Check relations based on regexes
		Matcher matcher = relation_pattern.matcher(relation);
		if(!matcher.matches())
			throw new SyntaxException();

		parseExpression(matcher.group(1), data_pointer);
		data_pointer--;
		parseExpression(matcher.group(3), data_pointer);

		memory[pointer] = 2000 + data_pointer;
		pointer++;

		if(matcher.group(2).charAt(0) == '>') {
			memory[pointer] = 3100 + data_pointer + 1; //First number
			pointer++;
			memory[pointer] = 4200 + goto_symbol; //Branch negative to "goto"
			if(matcher.group(2).charAt(1) == '=') {
				pointer++;
				memory[pointer] = 4100 + goto_symbol; //Also branch zero if equal to
			}
		}
		else if(matcher.group(2).charAt(0) == '<') {
			memory[pointer - 1] = 2000 + data_pointer + 1;
			memory[pointer] = 3100 + data_pointer;
			pointer++;
			memory[pointer] = 4200 + goto_symbol;
			if(matcher.group(2).charAt(1) == '=') {
				pointer++;
				memory[pointer] = 4100 + goto_symbol;
			}
		}
		else if(matcher.group(2).equals("==")) {
			memory[pointer] = 3100 + data_pointer + 1;
			pointer++;
			memory[pointer] = 4100 + goto_symbol;
		}
		else if(matcher.group(2).equals("!=")) {
			if(!constants.contains(-1))
				constants.add(-1);

			memory[pointer] = 3100 + data_pointer + 1;
			pointer++;
			memory[pointer] = 4200 + goto_symbol;
			pointer++;
			memory[pointer] = 3300 + constants.indexOf(-1);
			pointer++;
			memory[pointer] = 4200 + goto_symbol;
		}

		pointer++;
		data_pointer--;
	}

	private void parseExpression(String expression, int value_pointer) throws SyntaxException {
		//Check expressions based on regexes
		Matcher matcher = expression_pattern.matcher(expression);
		if(!matcher.matches())
			throw new SyntaxException();
	}
}
