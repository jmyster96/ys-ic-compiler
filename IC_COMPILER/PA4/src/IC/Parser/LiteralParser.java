package IC.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import IC.AST.Field;
import IC.AST.Method;

/**
 * A helper class for the Parser, that allows accumulating Fields and Methods
 * in a statically-typed manner. Used while parsing class members.
 */
class LiteralParser {
  /**
   * Expected input: a valid string from the lexer. That is, the legal chars 
   * are: ASCII chars 32 - 126, except \ and ". 
   * Additionally, '\\', '\n', '\t' and '\"' are valid.
   * Additionally, the input has opening and trailing quote signs (").
   *
   * Output: The string literal, unescaped and unquoted.
   */
  public static String parseString(int line, String tokenValue) {
    return tokenValue
        .substring(1, tokenValue.length() - 1)
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\t", "\t");
  }
}