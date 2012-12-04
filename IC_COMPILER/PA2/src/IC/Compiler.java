package IC;

import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.Parser.*;
import java.io.*;
import java_cup.runtime.Symbol;

public class Compiler {
    public static void main(String[] args) {
	// check that received one parameter
	if (args.length == 0) {
	    System.err.println("Missing input file argument");
	    System.exit(-1);
	}
	try {
	    Reader txtFile = new FileReader(args[0]);
	    Lexer lexer = new Lexer(txtFile);
	    Parser parser = new Parser(lexer);
	    // TODO: Get as command line argument
	    parser.printTokens = true;

	    Symbol parseSymbol = parser.parse();
	    System.out.println("Parsed " + args[0] + " successfully!");

	    Program root = (Program) parseSymbol.value;
	    System.out.println("Root is null? " + (root == null));
	    
	    // Pretty-print the program to System.out
	    PrettyPrinter printer = new PrettyPrinter(args[0]);
	    printer.visit(root);
	    
	    // Read the tokens from the scanner, one by one, and print
	    // each one according to spec (Token.toString() takes care
	    // of that).
	    Token tkn;
	    do {
		tkn = lexer.next_token();
		System.out.println(tkn.toString());
	    } while (tkn.sym != sym.EOF);
	} catch (LexicalError e) {
	    // We were asked to gracefully return 0 on user-code related exceptions.
	    System.out.println(e);
	    System.exit(0);
	} catch (IOException e) {
	    // On those errors we do crash.
	    System.err.println(e);
	    System.exit(1);
	} catch (Exception e) {
	    // Those are supposed to be Parser exceptions.
	    e.printStackTrace();
	    System.out.println(e);
	    System.exit(0);
	}
    }
}
