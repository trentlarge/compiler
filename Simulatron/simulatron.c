#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void print(short instruction, int accumulator, int * memory, short mem_length);

int main(int argc, char * argv[]) {
	if(argc < 2 || argc > 3) {
		fprintf(stderr, "Usage: %s [-d] <filename>\n", argv[0]);
		return 1;
	}

	short debug;
	if(argc == 3 && strcmp(argv[2], "-d") == 0)
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
	int result = fscanf(program, "#%20s\n", option);
	while(result) {
		if(sscanf(option, "memory %hd", &mem_length) == 1);
		else if(!warnings && sscanf(option, "warnings %1hd", &warnings) == 1);
		else {
			fprintf(stderr, "Invalid pragma found: %s", option);
			return 5;
		}

		result = fscanf(program, "#%20s\n", option);
	}

	if(mem_length < 0 || mem_length > 32767) {
		fprintf(stderr, "Error: Invalid memory size\n");
		return 1;
	}

	mem_width = pow(10, ceil(log10(mem_length)));
	val_width = mem_width * mem_width;

	int * memory = calloc(mem_length, sizeof(int));

	short i = 0;
	do {
		if(i >= mem_length) {
			fprintf(stderr, "Error: Not enough memory to load program\n");
			free(memory);
			fclose(program);
			return 3;
		}

		result = fscanf(program, "%d", &(memory[i]));
		i++;
	}
	while(result > 0 && result != EOF);

	fclose(program);

	short error = 0;
	short debug_prompt = debug;
	short breakpoint = -1;
	while(1) {
		while(debug_prompt || error || instruction == breakpoint) {
			char command;
			char args[10];

			printf("(dgb) ");
			short num_args = scanf("%1s %10s", &command, args);
			if(num_args == 0)
				continue;

			if(command == 'r') {
				debug_prompt = 0;
				break;
			}
			else if(command == 'p')
				print(instruction, accumulator, memory, mem_length);
			else if(command == 's')
				break;
			else if(command == 'b') {
				if(!sscanf(args, "%hd", &breakpoint))
					printf("Usage: b <instruction>\n");
			}
			else if(command == 'q')
				return 0;
			else if(command == 'h')
				printf("r - Run program from beginning or continue from current position.\np - Print the instruction pointer, instruction register, accumulator, and memory\ns - Step one instruction\nb - Set a breakpoint\nq - Quit\nh - Display this help\n");
			else
				printf("Unknown command");
		}

		if(error) {
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
						continue;
					}
					free(memory);
					return 5;
				}
			}
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
				if(!scanf("%d", &(memory[argument]))) {
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
				if(accumulator < 0)
					instruction = argument;
				break;
			case 42:
				if(accumulator == 0)
					instruction = argument;
				break;
			case 43:
				free(memory);
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
	fprintf(stderr, "Memory:\n");
	for(int i = 0; i < mem_length; i++) {
		fprintf(stderr, "%*d |", i, number_width);
		for(int ii = 0; ii < 10; ii++, i++)
			fprintf(stderr, " %d", memory[i]);
		fprintf(stderr, "\n");
	}
}
