import java.util.Scanner;

public class Compiler {
	Scanner scanner;
	int[] memory;
	ArrayList<Integer> constants;
	HashMap<Intger, Integer> line_numbers;
	HashMap<String, Integer> variables;
	int line_number, pointer, data_pointer;

	public Compiler(File file) {
		scanner = new Scanner(file);
		memory = new int[100]();
		symbols = new ArrayList<Symbol>();
		variables = new ArrayList<Integer>();
		variables = new HashMap<String, Integer>();
		line_number = 0;
		pointer = 0;
		data_pointer = 99;
	}

	public int[] compile() {
		while(scanner.hasNextLine()) {
			if(pointer >= data_pointer)
				throw new OutOfMemoryException();

			String[] command = scanner.nextLine().split(" ");

			int next_line_number = Integer.parseInt(command[0]);
			if(next_line_number <= line_number)
				throw new LineNumberException();
			line_numbers.set(next_line_number, pointer);
			line_number = next_line_number;

			if(command[1].equalsIgnoreCase("rem"))
				continue;
			else if(command[1].equalsIgnoreCase("input")) {
				variables.set(command[2], data_pointer);
				memory[pointer] = 1000 + data_pointer;
				data_pointer--;
			}
			else if(command[1].equalsIgnoreCase("print")) {
				if(!variables.containsKey(command[1]))
					throw new UndefinedVariableException();

				memory[pointer] = 1100 + command[1];
			}
			else if(command[1].equalsIgnoreCase("let")) {
				if(!variables.containsKey(command[2])) {
					variables.set(command[2], data_pointer);
					data_pointer--;
				}

				int value = Integer.parseInt(command[3]);

				if(!constants.contains(value))
					constants.add(value);

				memory[pointer] = 2000 + constants.indexOf(value);
				pointer++;
				memory[pointer] = 2100 + command[1];
			}
			else if(command[1].equalsIgnoreCase("goto")) {
				memory[pointer] = 4000 + Integer.parseInt(command[2]);
			}
			else if(command[1].equalsIgnoreCase("if"))
				continue;
			else if(command[1].equalsIgnoreCase("end"))
				memory[pointer] = 4300;

			pointer++;
		}
	}

	private void parseRelation(String relation) {
	}

	private void parseExpression(String expression) {
	}
}
