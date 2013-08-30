CC = clang

all:
	$(CC) -O2 -std=c99 -Wall -lm -o simulatron simulatron.c

debug:
	$(CC) -g -std=c99 -Wall -lm -o simulatron simulatron.c

clean:
	$(RM) simulatron

.PHONY: all debug clean
