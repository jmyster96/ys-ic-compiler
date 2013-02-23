package IC.Parser;

import IC.ICCompilerError;

public class SyntaxError extends ICCompilerError {
	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(String message, int line) {
		super(message, line);
	}

	public SyntaxError(String message, int line, String value) {
		super(message, line, value);
	}

	public String toString() {
	  String msg = "Line " + this.line + ": Syntax error: " + this.getMessage();
	  if (value != null) {
	    msg += "; " + value;
	  }
		return msg;
	}
}
