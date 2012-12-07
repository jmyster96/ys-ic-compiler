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
    try {
      Options options = Options.parseCommandLineArgs(args);

      Symbol libParseSymbol = parseLibraryFile(options.libicPath);
      ICClass libraryClass = (ICClass) libParseSymbol.value;

      Symbol parseSymbol = parseICFile(options.sourcePath);
      Program root = (Program) parseSymbol.value;

      if (options.printAST) {
        // Pretty-print the program to System.out
        PrettyPrinter libPrinter = new PrettyPrinter(options.libicPath);
        System.out.println(libPrinter.visit(libraryClass));
        PrettyPrinter printer = new PrettyPrinter(options.sourcePath);
        System.out.println(printer.visit(root));
      }
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
      Symbol parseSymbol = parser.parse();
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
      Symbol parseSymbol = parser.parse();
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
  
  static class Options {
    String libicPath;
    String sourcePath;
    boolean printAST;
    private Options() {
      this.libicPath = "libic.sig";
      this.printAST = false;
    }
    static void handleWrongSyntax() {
      System.err.println("Wrong instantiation of Compiler.");
      System.err.println("Usage: java IC.Compiler <file.ic> [ -L</path/to/libic.sig> ] [ -print-ast ]");
      System.exit(1);
    }
    static Options parseCommandLineArgs(String[] args) {
      if (args.length == 0) {
        handleWrongSyntax();
      }
      Options options = new Options();
      options.sourcePath = args[0];
      for (int i = 1; i < args.length; ++i) {
        String arg = args[i];
        if (arg.startsWith("-L")) {
          options.libicPath = arg.substring(2);
        } else if (arg.equals("-print-ast")) {
          options.printAST = true;
        } else {
          handleWrongSyntax();
        }
      }
      return options;
    }
  }
}
