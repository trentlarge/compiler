import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

public class Util {
	public static void print(String message) {
		if(GraphicsEnvironment.isHeadless())
			System.out.println(message);
		else
			JOptionPane.showMessageDialog(null, message, "Compilatron", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void printError(String message) {
		if(GraphicsEnvironment.isHeadless())
			System.out.println(message);
		else
			JOptionPane.showMessageDialog(null, message, "Compilatron", JOptionPane.ERROR_MESSAGE);
	}
}
