import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Simulatron {
	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser();
		File file = new File("");

		if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			file = chooser.getSelectedFile();

		Scanner scanner;
		try {
			scanner = new Scanner(file);
		}
		catch(FileNotFoundException e) {
			System.err.println("Error: File not found");
			return;
		}

		boolean debug = JOptionPane.showConfirmDialog(null, "Would you like to enable debugging?", "Simulatron", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		boolean debug_prompt = debug;

		int[] memory = new int[100];
		int i = 0;

		try {
			//Check the entire file for numbers
			while(scanner.hasNext()) {
				try {
					int value = Integer.parseInt(scanner.next());
					if(value > 9999 || value < -9999) { //Check it's length
						System.err.println("Error: Memory value " + value + " out of range at " + i);
						return;
					}
					memory[i] = value;
					i++;
				}
				catch(NumberFormatException e) {} //Ignore non-numerical tokens as comments
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.err.println("Error: Not enough memory to load program");
			return;
		}

		Simulator simulator = new Simulator(memory);

		boolean error = false;
		int breakpoint = -1;
		boolean run = true;
		while(run) {
			try {
				//Check if we hit a breakpoint
				boolean hit_breakpoint = false;
				if(debug && simulator.getInstruction() == breakpoint) {
					hit_breakpoint = true;
					System.err.println("Hit breakpoint at " + simulator.getInstruction());
				}

				//Check if a debug prompt is necessary
				while(debug && (debug_prompt || error || hit_breakpoint)) {
					System.err.print("(dbg) ");
					//Grab the command each argument
					Scanner stdin = new Scanner(System.in);
					String[] command = stdin.nextLine().split(" ");

					//Restart from beginning
					if(command[0].equalsIgnoreCase("r") || command[0].equalsIgnoreCase("run")) {
						error = false;
						debug_prompt = false;
						simulator.setInstruction(0);
						break;
					}
					//Just turn off prompt and break out of debug loop
					else if(command[0].equalsIgnoreCase("c") || command[0].equalsIgnoreCase("continue")) {
						debug_prompt = false;
						break;
					}
					else if(command[0].equalsIgnoreCase("p") || command[0].equalsIgnoreCase("print")) {
						try {
							if(command.length > 2)
								throw new Exception();
							if(command.length == 2) {
								int n = Integer.parseInt(command[1]);
								if(n > 99 || n < 0) {
									System.err.println("Error: n out of range");
									continue;
								}
								simulator.print(n);
							}
							else {
								simulator.print();
							}
						}
						//Some type of argument failure
						catch(Exception e) {
							System.err.println("Usage: p(rint) [n]");
						}
					}
					else if(command[0].equalsIgnoreCase("w") || command[0].equalsIgnoreCase("write")) {
						try {
							if(command.length > 3)
								throw new Exception();
							int n = Integer.parseInt(command[1]);
							if(n > 99 || n < 0) {
								System.err.println("Error: n out of range");
								continue;
							}
							int val = Integer.parseInt(command[2]);
							if(val > 9999 || val < -9999) {
								System.err.println("Error: val out of range");
								continue;
							}
							simulator.set(n, val);
						}
						catch(Exception e) {
							System.err.println("Usage: w(rite) <n> <val>");
						}
					}
					else if(command[0].equalsIgnoreCase("s") || command[0].equalsIgnoreCase("step")) {
						debug_prompt = true;
						break;
					}
					else if(command[0].equalsIgnoreCase("b") || command[0].equalsIgnoreCase("break")) {
						try {
							if(command.length > 2)
								throw new Exception();
							if(command.length == 2) {
								int n = Integer.parseInt(command[1]);
								if(n > 99 || n < 0) {
									System.err.println("Error: n out of range");
									continue;
								}
								breakpoint = n;
							}
							else {
								breakpoint = 0;
								System.err.println("Breakpoint cleared");
							}
						}
						catch(Exception e) {
							System.err.println("Usage: b(reak) [n]");
						}
					}
					else if(command[0].equalsIgnoreCase("q") || command[0].equalsIgnoreCase("quit")) {
						return;
					}
					else if(command[0].equalsIgnoreCase("h") || command[0].equalsIgnoreCase("help")) {
						System.err.println("r(un)		Run program from beginning");
						System.err.println("c(ontinue)		Continue program from current position");
						System.err.println("p(rint) [n]		Print the instruction pointer, instruction register, accumulator, and memory or, if specified, print the memory space n");
						System.err.println("w(rite) <n> <val>	Write val to memory space n");
						System.err.println("s(tep)		Step one instruction");
						System.err.println("b(reak) [n]		Set a breakpoint at memory space n or if n is not specified, clear the breakpoint");
						System.err.println("q(uit)		Quit");
						System.err.println("h(elp)		Display this help");
					}
					else {
						System.err.println("Unknown command");
					}
				}

				//If we have a standing error, exit
				if(error) {
					run = false;
					continue;
				}

				if(!simulator.step()) {
					if(debug)
						error = true; //End with a debug prompt if in debug mode
					else
						run = false;
				}
			}
			catch(OutOfMemoryException e) {
				System.err.println("Error: Hit end of memory while executing");
				error = true;
			}
			catch(DivisionByZeroException e) {
				System.err.println("Error: Attempt to divide by zero at " + simulator.getInstruction());
				error = true;
			}
			catch(InvalidOpcodeException e) {
				System.err.println("Error: Invalid opcode " + simulator.get(simulator.getInstruction()) + " at " + simulator.getInstruction());
				error = true;
			}
			catch(AccumulatorOverflowException e) {
				System.err.println("Error: Accumulator overflow at " + simulator.getInstruction());
				error = true;
			}
			catch(InputMismatchException e) {
				System.err.println("Error: Input not a number");
				error = true;
			}
		}
	}
}
