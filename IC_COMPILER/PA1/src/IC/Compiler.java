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
		Token tkn;
		try {
			Reader txtFile = new FileReader(args[0]);
			Lexer lexer = new Lexer(txtFile);
			// Read the tokens from the scanner, one by one, and print
			// each one according to spec (Token.toString() takes care
			// of that).
			do {
				tkn = lexer.next_token();
				System.out.println(tkn.toString());
			} while (tkn.sym != sym.EOF);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(-1);
		} catch (LexicalError e) {
			System.err.println(e);
			System.exit(-1);
		}
	}
}
