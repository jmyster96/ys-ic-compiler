package IC;

import IC.Parser.*;
import java.io.*;

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
	    // On other errors we do crash.
	    System.err.println(e);
	    System.exit(1);
	}
    }
}
