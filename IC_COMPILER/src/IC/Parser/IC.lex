package IC.Parser;

import java_cup.runtime.Symbol;
import java.lang.Math;

%%

%class Lexer
%public
%function next_token
%type Token
%line
%scanerror LexicalError

%state LINE_COMMENT
%state BLOCK_COMMENT

/* Handling EOF: in case the file terminated inside a comment, throws an exception */
%eofval{
  if (yystate() == BLOCK_COMMENT) {
    throw new LexicalError("Unclosed comment: End of file reached, expected '*/'.",
                           yyline, "");
  }
  return new Token(sym.EOF, yyline);
%eofval}

/* Macros */
DIGIT = [0-9]
NUMBER_LITERAL = [0]|[1-9]({DIGIT})*
LOWER_LETTER = [a-z]
UPPER_LETTER = [A-Z]
LETTER = {LOWER_LETTER}|{UPPER_LETTER}|[_]
ALPHA_NUMERIC = {DIGIT}|{LETTER}
WHITESPACE = [ \t\f\r\n]

/* 
   Valid string chars are: ASCII chars 32 - 126, except \ and ". 
   Additionally, '\\', '\n', '\t' and '\"' are valid.
*/
VALID_ASCII_CHARS = [ !#-\[\]-~]
VALID_STRING_CHARS = "\\\\"|"\\\""|"\\t"|"\\n"|{VALID_ASCII_CHARS}

%%

/* Skip whitespace */
<YYINITIAL> {WHITESPACE} {}

/* Each comment type has its own state */
<YYINITIAL> "//" { yybegin(LINE_COMMENT); }
<LINE_COMMENT> [^\n] {}
<LINE_COMMENT> [\n] { yybegin(YYINITIAL); }

<YYINITIAL> "/*" { yybegin(BLOCK_COMMENT); }
<BLOCK_COMMENT> [^\*] {}
<BLOCK_COMMENT> "*/" { yybegin(YYINITIAL); }
<BLOCK_COMMENT> [*] {}

/* Punctuation marks */
<YYINITIAL> "(" { return new Token(sym.LP, yyline); }
<YYINITIAL> ")" { return new Token(sym.RP, yyline); }
<YYINITIAL> "[" { return new Token(sym.LB, yyline); }
<YYINITIAL> "]" { return new Token(sym.RB, yyline); } 
<YYINITIAL> "{" { return new Token(sym.LCBR, yyline); }
<YYINITIAL> "}" { return new Token(sym.RCBR, yyline); }
<YYINITIAL> ";" { return new Token(sym.SEMI, yyline); }
<YYINITIAL> "." { return new Token(sym.DOT, yyline); }
<YYINITIAL> "," { return new Token(sym.COMMA, yyline); }
/* Quotes: starts and ends with a ", other valid chars in the middle. */
<YYINITIAL> [\"]({VALID_STRING_CHARS})*[\"] {
  return new Token(sym.QUOTE, yyline, yytext());
}


/* Keywords */
<YYINITIAL> "class" { return new Token(sym.CLASS, yyline); }
<YYINITIAL> "extends" { return new Token(sym.EXTENDS, yyline); }
<YYINITIAL> "static" { return new Token(sym.STATIC, yyline); }
<YYINITIAL> "void" { return new Token(sym.VOID, yyline); } 
<YYINITIAL> "int" { return new Token(sym.INT, yyline); }
<YYINITIAL> "boolean" { return new Token(sym.BOOLEAN, yyline); }
<YYINITIAL> "string" { return new Token(sym.STRING, yyline); }
<YYINITIAL> "return" { return new Token(sym.RETURN, yyline); } 
<YYINITIAL> "if" { return new Token(sym.IF, yyline); }
<YYINITIAL> "else" { return new Token(sym.ELSE, yyline); }
<YYINITIAL> "while" { return new Token(sym.WHILE, yyline); } 
<YYINITIAL> "break" { return new Token(sym.BREAK, yyline); }
<YYINITIAL> "continue" { return new Token(sym.CONTINUE, yyline); }
<YYINITIAL> "this" { return new Token(sym.THIS, yyline); }
<YYINITIAL> "new" { return new Token(sym.NEW, yyline); } 
<YYINITIAL> "length" { return new Token(sym.LENGTH, yyline); } 
<YYINITIAL> "true" { return new Token(sym.TRUE, yyline); } 
<YYINITIAL> "false" { return new Token(sym.FALSE, yyline); }
<YYINITIAL> "null" { return new Token(sym.NULL, yyline); }

<YYINITIAL> "=" { return new Token(sym.ASSIGN, yyline); }

/* Boolean and binary operators */
<YYINITIAL> "==" { return new Token(sym.EQUAL, yyline); }
<YYINITIAL> "!=" { return new Token(sym.NEQUAL, yyline); }
<YYINITIAL> ">" { return new Token(sym.GT, yyline); } 
<YYINITIAL> "<" { return new Token(sym.LT, yyline); }
<YYINITIAL> "<=" { return new Token(sym.LTE, yyline); }
<YYINITIAL> ">=" { return new Token(sym.GTE, yyline); }

<YYINITIAL> "+" { return new Token(sym.PLUS, yyline); }
<YYINITIAL> "-" { return new Token(sym.MINUS, yyline); } 
<YYINITIAL> "*" { return new Token(sym.MULTIPLY, yyline); }
<YYINITIAL> "/" { return new Token(sym.DIVIDE, yyline); }
<YYINITIAL> "%" { return new Token(sym.MOD, yyline); }

<YYINITIAL> "!" { return new Token(sym.LNEG, yyline); }

/* Conditional operators */
<YYINITIAL> "&&" { return new Token(sym.LAND, yyline); }
<YYINITIAL> "||" { return new Token(sym.LOR, yyline); }


/* Stuff that have text in them */

<YYINITIAL> {NUMBER_LITERAL} {
  if (Math.abs(Long.parseLong(yytext())) >= (long)(Math.pow(2, 31))) {
    throw new LexicalError("Integer out of bounds", yyline, yytext());
  } else {
    return new Token(sym.INTEGER, yyline, new Integer(yytext()));
  }
}

/* Explicitly disallow illegal identifiers. */
<YYINITIAL> (({DIGIT})+)({LETTER})+ {
  throw new LexicalError("A number follower by a letter is illegal", yyline, yytext());
}

/* Class identifiers start with an uppercase letter,
   regular identifiers start with a lowercase letter. */ 
<YYINITIAL> {UPPER_LETTER}({ALPHA_NUMERIC})* {
  return new Token(sym.CLASS_ID, yyline, yytext());
}
<YYINITIAL> {LOWER_LETTER}({ALPHA_NUMERIC})* { return new Token(sym.ID, yyline, yytext()); }

/* Any other character must be an error. */
<YYINITIAL> . { throw new LexicalError("illegal character", yyline, yytext()); }
