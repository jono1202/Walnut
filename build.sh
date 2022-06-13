#!/bin/bash

javac -d bin/ -cp src ./src/Automata/*.java
javac -d bin/ -cp src ./src/dk/brics/automaton/*.java
javac -d bin/ -cp src ./src/Token/*.java
javac -d bin/ -cp src ./src/Main/*.java

read -p "Press enter to continue"
