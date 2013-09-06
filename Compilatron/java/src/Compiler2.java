public class Compiler {
	Scanner scanner;

	public Compiler(File file) {
		scanner = new Scanner(file);
	}

	public int[] compile() {
		while(scanner.hasNextLine()) {
			String[] command = scanner.nextLine().split(" ");
		}
	}
}
