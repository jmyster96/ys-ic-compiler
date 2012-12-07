package IC;

import IC.AST.ICClass;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.Parser.*;
import java.io.*;
import java_cup.runtime.Symbol;

public class Compiler {
  public static void main(String[] args) {
    // check that received one parameter
    if (args.length == 0) {
      System.err.println("Syntax:");
      System.err.println("Syntax:");
      System.exit(1);
    }
    try {
      String sourceFilePath = args[0];
      String libicSigPath = "libic.sig";
      if (args.length == 2) {
        if (!args[1].startsWith("-L")) {
          System.err.println("Syntax:");
          System.exit(1);
        }
        libicSigPath = args[1].substring(2);
      }

      Symbol libParseSymbol = parseLibraryFile(libicSigPath);
      ICClass libraryClass = (ICClass) libParseSymbol.value;

      Symbol parseSymbol = parseICFile(sourceFilePath);
      Program root = (Program) parseSymbol.value;

      // Pretty-print the program to System.out
      PrettyPrinter printer = new PrettyPrinter(args[0]);
      System.out.println(printer.visit(libraryClass));
      System.out.println(printer.visit(root));
    } catch (IOException e) {
      // On those errors we do crash.
      System.err.println(e);
      System.exit(1);
    }
  }

  private static Symbol parseLibraryFile(String libicSigPath) throws IOException {
    Reader libSigFile = new FileReader(libicSigPath);
    Lexer lexer = new Lexer(libSigFile);
    LibraryParser parser = new LibraryParser(lexer);

    try {
      Symbol parseSymbol = parser.debug_parse();
      System.out.println("Parsed " + libicSigPath + " successfully!");
      return parseSymbol;
    } catch (LexicalError e) {
      // We were asked to gracefully return 0 on user-code related exceptions.
      System.out.println(e);
      return null;
    } catch (Exception e) {
      // Those are supposed to be Parser exceptions.
      e.printStackTrace();
      System.out.println(e);
      return null;
    }
  }

  private static Symbol parseICFile(String filepath) throws IOException {
    Reader icSourceFile = new FileReader(filepath);
    Lexer lexer = new Lexer(icSourceFile);
    Parser parser = new Parser(lexer);

    try {
      Symbol parseSymbol = parser.debug_parse();
      System.out.println("Parsed " + filepath + " successfully!");
      return parseSymbol;
    } catch (LexicalError e) {
      // We were asked to gracefully return 0 on user-code related exceptions.
      System.out.println(e);
      return null;
    } catch (Exception e) {
      // Those are supposed to be Parser exceptions.
      e.printStackTrace();
      System.out.println(e);
      return null;
    }
  }
  
}
