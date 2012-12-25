package IC.Parser;

public class SyntaxError extends Exception {
	private int line;
	private String value;

	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(String message, int line) {
		super(message);
		this.line = line;
	}

	public SyntaxError(String message, int line, String value) {
		super(message);
		this.line = line;
		this.value = value;
	}

	public String toString() {
	  String msg = "Line " + this.line + ": Syntax error: " + this.getMessage();
	  if (value != null) {
	    msg += "; " + value;
	  }
		return msg;
	}
	
	public int getLine() {
	   return this.line;
	}
}
