#include <math.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char * argv[]) {
	if(argc == 1) {
		fprintf(stderr, "Usage: %s <filename>\n", argv[0]);
		return 1;
	}

	FILE * program = fopen(argv[1], "r");
	if(program == NULL) {
		perror("Could not open program");
		return 2;
	}

	short number = 100, width, instruction = 0;
	int accumulator = 0;

	char option[20];
	int result = fscanf(program, "#%20s\n", option);
	while(result) {
		sscanf(option, "memory %hd", &number);

		result = fscanf(program, "#%20s\n", option);
	}

	if(number < 0 || number > 32767) {
		fprintf(stderr, "Error: Invalid memory size\n");
		return 1;
	}

	width = pow(10, ceil(log10(number)));

	int * memory = calloc(number, sizeof(int));

	short i = 0;
	do {
		if(i >= number) {
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

	while(1) {
		if(instruction >= number) {
			fprintf(stderr, "Error: Hit end of memory space\n");
			free(memory);
			return 5;
		}

		if(memory[instruction] == 0) {
			fprintf(stderr, "Warning: Executing uninitialized space at %d\n", instruction);
			while(memory[instruction] == 0) {
				instruction++;
				if(instruction >= number) {
					fprintf(stderr, "Error: Hit end of memory space\n");
					free(memory);
					return 5;
				}
			}
		}

		short opcode = memory[instruction] / width;
		short argument = memory[instruction] % width;

		if(argument < 0 || argument >= number) {
			fprintf(stderr, "Error: Invalid argument %d at %d\n", argument, instruction);
			free(memory);
			return 4;
		}

		instruction++;

		switch(opcode) {
			case 10:
				scanf("%d", &(memory[argument]));
				break;
			case 11:
				printf("%d\n", memory[argument]);
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
				if(memory[argument] == 0) {
					fprintf(stderr, "Error: Division by zero at %d\n", instruction);
					free(memory);
					return 4;
				}
				accumulator /= memory[argument];
				break;
			case 33:
				accumulator *= memory[argument];
				break;
			case 40:
				instruction = argument;
				break;
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
				free(memory);
				return 4;
		}
	}
}
