import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;

public class Compilatron {
	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser();
		File file;
		FileWriter output;

		try {
			file = chooser.getSelectedFile();
		}
		catch(Exception e) {
			Util.printError("Could not open file: " + e);
		}

		Compiler compiler = new Compiler(file);
		int[] memory = compiler.compile();

		try {
			output = new FileWriter(chooser.getSelectedFile());
		}
		catch(IOException e) {
			Util.printError("Could not open file: " + e);
			System.exit(1);
		}
		try {
			for(int i = 0; i < memory.length; i++) {
				output.write(memory[i]);
				for(int ii = 1; ii < 10; ii++)
					output.write(" " + memory[i + ii]);
				output.write("\n");
			}
		}
		catch(IOException e) {
			Util.printError("Could not write to file: " + e);
			System.exit(1);
		}
	}
}
