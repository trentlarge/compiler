import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

public class Util {
	private static void print(String message) {
		if(GraphicsEnvironment.isHeadless())
			System.out.println(message);
		else
			JOptionPane.showMessageDialog();
	}

	static void printError(String message) {
		if(GraphicsEnvironment.isHeadless())
			System.out.println(message);
		else
			JOptionPane.showMessageDialog();
	}
}
