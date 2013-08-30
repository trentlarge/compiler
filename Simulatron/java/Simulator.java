public class Simulator {
	private short[100] memory;
	private byte instruction;
	private short accumulator;

	public Simulator(short[] memory) {
		this.memory = memory;
		instruction = 0;
		accumulator = 0;
	}

	public void load(InputStream stream) {
	}

	public boolean step() {
		if(instruction > 99)
			throw new OutOfMemoryException();

		byte opcode = memory[instruction] / 100;
		byte argument = memory[instruction] % 100;

		switch(opcode) {
			case 10:
				System.out.print("> ");
				memory[argument] = (new Scanner(System.in)).nextByte();
				break;
			case 11:
				System.out.println(memory[argument]);
				break;
			case 20:
				accumulator = memory[argument];
				break;
			case 21:
				memory[argument] = accumulator;
				break;
			case 30:
				accumulator += memory[argument];
				break;
			case 31:
				accumulator -= memory[argument];
				break;
			case 32:
				accumulator *= memory[argument];
				break;
			case 33:
				if(memory[argument] == 0)
					throw new DivisionByZeroException();
				accumulator /= memory[argument];
				break;
			case 40:
				instruction = argument;
				return true;
			case 41:
				if(accumulator < 0) {
					instruction = argument;
					return true;
				}
				break;
			case 42:
				if(accumulator == 0) {
					instruction = argument;
					return true;
				}
				break;
			case 43:
				return false;
			default:
				throw new InvalidOpcodeException();
		}

		if(accumulator > 9999 || accumulator < -9999)
			throw new AccumulatorOverflowException();

		instruction++;

		return true;
	}

	public void print() {
		System.err.println("Instruction Pointer: " + instruction);
		System.err.println("Instruction Register: " + memory[instruction]);

		System.err.println("Accumulator: " + accumulator);

		System.err.println("Memory:");
		for(int i = 0; i < 100; i) {
			System.err.printf("%2d |", i);
			for(int ii = 0; ii < 10; ii++, i++) {
				System.err.printf(" %4d", memory[i]);
			}
			System.err.println();
		}
	}

	public void print(byte n) {
		System.err.println(memory[n]);
	}

	public void set(byte n, short val) {
		memory[n] = val;
	}

	public byte getInstruction() {
		return instruction;
	}
}
