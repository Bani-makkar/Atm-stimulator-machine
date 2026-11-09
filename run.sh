#!/usr/bin/env zsh
set -e

SCRIPT_DIR="${0:A:h}"
cd "$SCRIPT_DIR"

echo "Compiling ATM Machine Simulator..."
mkdir -p out
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
rm -f sources.txt

# Include any optional JDBC jars in lib/ if present
CLASSPATH="out"
if [ -d "lib" ]; then
  CLASSPATH="out:lib/*"
fi

echo "Starting ATM Machine Simulator..."
java -cp "$CLASSPATH" com.atm.Main "$@"
