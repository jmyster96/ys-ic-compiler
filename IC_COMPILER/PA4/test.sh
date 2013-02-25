(ant build &&
./run_compiler.sh $1 &&
java -cp ~/Downloads/microLir/build:lib/java-cup-11a.jar microLIR.Main "${1:0:`expr "$1" : '.*\.'`}"lir  -verbose:0)
echo ""