/**
 * Various global functions
 * 
 * @author Foster Mclane and Jonathan Lowe
 */

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

public class Util {
	/**
	 * Print a normal message using a JOptionPane when in graphical mode or a text message in headless mode
	 * 
	 * @param message The message
	 */
	public static void print(String message) {
		if(GraphicsEnvironment.isHeadless())
			System.out.println(message);
		else
			JOptionPane.showMessageDialog(null, message, "Compilatron", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Print an error message using a JOptionPane when in graphical mode or a text message in headless mode
	 * 
	 * @param message The error
	 */
	public static void printError(String message) {
		if(GraphicsEnvironment.isHeadless())
			System.out.println(message);
		else
			JOptionPane.showMessageDialog(null, message, "Compilatron", JOptionPane.ERROR_MESSAGE);
	}
}
