JC = javac
JAR = jar
JFLAGS = 

SRC = $(wildcard *.java)

ifdef SystemRoot
	RM = del /Q
	JFLAGS += -cp .;..
else
	RM = rm -f
	JFLAGS += -cp .:..
endif

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

all: $(SRC:.java=.class)

dist: all
	$(JAR) cvfe ../Simulatron.jar Simulatron *.class
	$(RM) *.class

clean:
	$(RM) *.class

.PHONY: all dist clean
