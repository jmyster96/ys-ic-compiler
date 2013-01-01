package IC.Semantic;

public class SemanticError extends Exception {
	private int line;
	private String value;

	public SemanticError(String message) {
		super(message);
	}

	public SemanticError(String message, int line) {
		super(message);
		this.line = line;
	}

	public SemanticError(String message, int line, String value) {
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
