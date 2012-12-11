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
	    // Reads all options and file paths and makes sure they exist.
      Options options = Options.parseCommandLineArgs(args);

      // Parses the library signature file.
      Symbol libParseSymbol = parseLibraryFile(options.libicPath);
      if (libParseSymbol == null) {
	      // Parsing failed. Errors have been printed.
	      System.exit(0);
      }
      ICClass libraryClass = (ICClass) libParseSymbol.value;

      // Parses the IC source file.
      Symbol parseSymbol = parseICFile(options.sourcePath);
      if (parseSymbol == null) {
	      // Parsing failed. Errors have been printed.
	      System.exit(0);
      }
      Program root = (Program) parseSymbol.value;

      if (options.printAST) {
        // If asked in the command line, pretty-print the program
        // (and the Library signature file) to System.out.
        PrettyPrinter libPrinter = new PrettyPrinter(options.libicPath, new OutputStreamWriter(System.out));
        libPrinter.visit(libraryClass);
        PrettyPrinter printer = new PrettyPrinter(options.sourcePath, new OutputStreamWriter(System.out));
        printer.visit(root);
        System.out.println();
      }
    } catch (IOException e) {
      // We were asked to gracefully return 0 on errors.
      System.out.println(e);
      System.exit(0);
    }
  }

  private static Symbol parseLibraryFile(String libicSigPath) throws IOException {
    Reader libSigFile = new FileReader(libicSigPath);
    Lexer lexer = new Lexer(libSigFile);
    LibraryParser parser = new LibraryParser(lexer);
    return runParser(parser, libicSigPath, "library file " + libicSigPath);
  }

  private static Symbol parseICFile(String filepath) throws IOException {
    Reader icSourceFile = new FileReader(filepath);
    Lexer lexer = new Lexer(icSourceFile);
    Parser parser = new Parser(lexer);
    return runParser(parser, filepath, filepath);
  }
  
  private static Symbol runParser(java_cup.runtime.lr_parser parser, String filepath, String displayFilepath)
      throws IOException {
    try {
      Symbol parseSymbol = parser.parse();
      System.out.println("Parsed " + displayFilepath + " successfully!");
      return parseSymbol;
    } catch (LexicalError e) {
      System.out.println("Lexical error while parsing " + displayFilepath + ".");
      System.out.println(e);
      printLine(filepath, e.getLine());
      return null;
    } catch (SyntaxError e) {
      System.out.println("Syntax error while parsing " + displayFilepath + ".");
      System.out.println(e);
      printLine(filepath, e.getLine());
      return null;
    } catch (Exception e) {
      // Not supposed to get here because our parser only throws SyntaxError
      System.out.println("Unexpected error while parsing " + displayFilepath + ".");
      System.out.println(e);
      e.printStackTrace();
      return null;
    }
  }

  private static void printLine(String filepath, int line) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(filepath));
    int currentLine = 0;
    String strLine;
    while ((strLine = in.readLine()) != null)   {
      if (++currentLine == line) {
        // Print the content on the console
        System.out.println("Line " + line + ": " + strLine);
        break;
      }
    } 
  }

  static class Options {
    private String libicPath;
    private String sourcePath;
    private boolean printAST;
    private Options() {
      this.libicPath = "libic.sig";
      this.printAST = false;
      this.sourcePath = null;
    }
    private static void handleWrongSyntax() {
      System.out.println("Can't run compiler.");
      System.out.println("Usage:\n\tjava IC.Compiler <file.ic> [ -L</path/to/libic.sig> ] [ -print-ast ]");
      System.exit(1);
    }
    public static Options parseCommandLineArgs(String[] args) {
      Options options = new Options();
      if (args.length == 0) {
        handleWrongSyntax();
      }
      
      for (int i = 0; i < args.length; ++i) {
        String arg = args[i];
        if (arg.startsWith("-L")) {
          options.libicPath = arg.substring(2);
        } else if (arg.equals("-print-ast")) {
          options.printAST = true;
        } else if (!arg.startsWith("-") && options.sourcePath == null) {
          options.sourcePath = arg;
        } else {
          System.out.println("Unrecognized flag: " + arg);
          handleWrongSyntax();
        }
      }
      if (options.sourcePath == null) {
        handleWrongSyntax();
      }
      options.makeSureValid();
      return options;
    }
    private void makeSureValid() {
      File f = new File(libicPath);
      boolean valid = true;
      if (!f.exists()) {
        System.out.println("Can't find library signature file at path: " + libicPath);
        valid = false;
      }
      f = new File(sourcePath);
      if (!f.exists()) {
        System.out.println("Can't find source file at path: " + sourcePath);
        valid = false;
      }
      if (!valid) {
        handleWrongSyntax();
      }  
    }
  }
}
