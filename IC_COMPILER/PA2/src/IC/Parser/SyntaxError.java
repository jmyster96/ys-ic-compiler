package IC.Parser;

public class SyntaxError extends Exception {
	private int line;

	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(String message, int line) {
		super(message);
		this.line = line;
	}

	public String toString() {
		return this.line + ": Syntax error: " + this.getMessage();
	}
	
	public int getLine() {
	   return this.line;
	}
}
