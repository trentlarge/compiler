#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void print(short instruction, int accumulator, int * memory, short mem_length);

int main(int argc, char * argv[]) {
	if(argc < 2 || argc > 3 || (argc == 3 && strcmp(argv[1], "-d"))) {
		fprintf(stderr, "Usage: %s [-d] <filename>\n", argv[0]);
		return 1;
	}

	short debug;
	if(argc == 3)
		debug = 1;
	else
		debug = 0;

	FILE * program = fopen(argv[argc - 1], "r");
	if(program == NULL) {
		perror("Could not open program");
		return 2;
	}

	short warnings = debug;
	short mem_length = 100, mem_width, val_width, instruction = 0;
	int accumulator = 0;

	char option[20];
	int result = fscanf(program, "#%[^\n]\n", option);
	while(result > 0 && result != EOF) {
		if(sscanf(option, "memory %hd", &mem_length) == 1);
		else if(!warnings && sscanf(option, "warnings %1hd", &warnings) == 1);
		else {
			fprintf(stderr, "Invalid pragma: %s\n", option);
			return 5;
		}

		result = fscanf(program, "#%20s\n", option);
	}

	if(mem_length < 0 || mem_length > 32767) {
		fprintf(stderr, "Error: Invalid memory size\n");
		return 1;
	}

	mem_width = pow(10, ceil(log10(mem_length)));
	val_width = 100 * mem_width;

	int * memory = calloc(mem_length, sizeof(int));

	short i = 0;
	int buffer;
	result = fscanf(program, "%d", &buffer);
	while(result > 0 && result != EOF) {
		if(i >= mem_length) {
			fprintf(stderr, "Error: Not enough memory to load program\n");
			free(memory);
			fclose(program);
			return 3;
		}

		memory[i] = buffer;

		result = fscanf(program, "%d", &buffer);
		i++;
	}

	fclose(program);

	short error = -1;
	short debug_prompt = debug;
	short breakpoint = -1;
	while(1) {
		while(debug_prompt || error != -1 || instruction == breakpoint) {
			char command[12];

			printf("(dbg) ");
			if(fgets(command, 12, stdin) == NULL || command[0] == '\n')
				continue;

			if(command[0] == 'r') {
				debug_prompt = 0;
				break;
			}
			else if(command[0] == 'p')
				print(instruction, accumulator, memory, mem_length);
			else if(command[0] == 's')
				break;
			else if(command[0] == 'b') {
				if(!sscanf(command, "%*s %hd", &breakpoint))
					printf("Usage: b <instruction>\n");
			}
			else if(command[0] == 'q') {
				free(memory);
				return 0;
			}
			else if(command[0] == 'h')
				printf("r\t- Run program from beginning or continue from current position.\np\t- Print the instruction pointer, instruction register, accumulator, and memory\ns\t- Step one instruction\nb <n>\t- Set a breakpoint at memory space n\nq\t- Quit\nh\t- Display this help\n");
			else
				printf("Unknown command\n");
		}

		if(error != -1) {
			free(memory);
			return error;
		}

		if(instruction >= mem_length) {
			fprintf(stderr, "Error: Hit end of memory space\n");
			if(debug) {
				error = 5;
				continue;
			}
			free(memory);
			return 5;
		}

		if(memory[instruction] == 0) {
			if(warnings)
				fprintf(stderr, "Warning: Executing uninitialized space at %d\n", instruction);
			while(memory[instruction] == 0) {
				instruction++;
				if(instruction >= mem_length) {
					fprintf(stderr, "Error: Hit end of memory space\n");
					if(debug) {
						error = 5;
						break;
					}
					free(memory);
					return 5;
				}
			}

			if(error != -1)
				continue;
		}

		int opcode = memory[instruction] / mem_width;
		int argument = memory[instruction] % mem_width;

		if(argument < 0 || argument >= mem_length) {
			fprintf(stderr, "Error: Invalid argument %d at %d\n", argument, instruction);
			if(debug) {
				error = 4;
				continue;
			}
			free(memory);
			return 4;
		}

		switch(opcode) {
			case 10:
				printf("> ");
				char line[20];
				if(fgets(line, 20, stdin) == NULL || !sscanf(line, "%d", &(memory[argument]))) {
					fprintf(stderr, "Error: Input not a number\n");
					if(debug) {
						error = 6;
						continue;
					}
					free(memory);
					return 6;
				}
				break;
			case 11:
				printf("%d\n", memory[argument]);
				break;
			case 20:
				accumulator = memory[argument];
				if(warnings && (accumulator >= val_width || accumulator <= -val_width))
					fprintf(stderr, "Warning: Accumulator overflow at %d\n", instruction);
				break;
			case 21:
				memory[argument] = accumulator;
				break;
			case 30:
				accumulator += memory[argument];
				if(warnings && (accumulator >= val_width || accumulator <= -val_width))
					fprintf(stderr, "Warning: Accumulator overflow at %d\n", instruction);
				break;
			case 31:
				accumulator -= memory[argument];
				if(warnings && (accumulator >= val_width || accumulator <= -val_width))
					fprintf(stderr, "Warning: Accumulator overflow at %d\n", instruction);
				break;
			case 32:
				if(memory[argument] == 0) {
					fprintf(stderr, "Error: Division by zero at %d\n", instruction);
					if(debug) {
						error = 4;
						continue;
					}
					free(memory);
					return 4;
				}
				accumulator /= memory[argument];
				if(warnings && (accumulator >= val_width || accumulator <= -val_width))
					fprintf(stderr, "Warning: Accumulator overflow at %d\n", instruction);
				break;
			case 33:
				accumulator *= memory[argument];
				if(warnings && (accumulator >= val_width || accumulator <= -val_width))
					fprintf(stderr, "Warning: Accumulator overflow at %d\n", instruction);
				break;
			case 40:
				instruction = argument;
				continue;
			case 41:
				if(accumulator < 0) {
					instruction = argument;
					continue;
				}
				break;
			case 42:
				if(accumulator == 0) {
					instruction = argument;
					continue;
				}
				break;
			case 43:
				if(debug) {
					error = 0;
					continue;
				}
				return 0;
			case 50:
				break;
			default:
				fprintf(stderr, "Error: Unknown opcode %d at %d\n", opcode, instruction);
				if(debug) {
					error = 4;
					continue;
				}
				free(memory);
				return 4;
		}

		instruction++;
	}
}

void print(short instruction, int accumulator, int * memory, short mem_length) {
	fprintf(stderr, "Instruction Pointer: %hd\n", instruction);
	fprintf(stderr, "Instruction Register: %d\n", memory[instruction]);

	fprintf(stderr, "Accumulator: %d\n", accumulator);

	int number_width = ceil(log10(mem_length));
	int mem_width = 2 + number_width;
	fprintf(stderr, "Memory:\n");
	for(int i = 0; i < mem_length;) {
		fprintf(stderr, "%*d |", number_width, i);
		for(int ii = 0; ii < 10; ii++, i++)
			fprintf(stderr, " %*d", mem_width, memory[i]);
		fprintf(stderr, "\n");
	}
}
