package IC.Parser;

%%

%class Lexer
%public
%function next_token
%type Token
%line
%scanerror LexicalError

%%

"(" { return new Token(sym.LP,yyline); }
