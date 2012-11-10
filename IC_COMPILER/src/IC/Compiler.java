package IC;

//import java_cup.runtime.Symbol;
import IC.Parser.*;
import java.io.*;

public class Compiler {
    public static void main(String[] args) {
	// check that received one parameter
	if (args.length == 0) {
	    System.err.println("Missing input file argument");
	    System.exit(-1);
	}
	Token tkn;
	try {
	    FileReader txtFile = new FileReader(args[0]); 
	    Lexer lexer = new Lexer(txtFile);
	    do {
		tkn = lexer.next_token();
		System.out.println(tkn.toString());
	    } while (tkn.sym != sym.EOF);
	} catch (Exception e) {
	    System.err.println(e);
	    System.exit(-1);
	}
    }
}
