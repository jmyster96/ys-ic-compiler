package IC.Parser;

import IC.ICCompilerError;

public class LexicalError extends ICCompilerError {
	public LexicalError(String message) {
		super(message);
	}

	public LexicalError(String message, int lexerLine, String value) {
		super(message, lexerLine + 1, value);
	}

	public String toString() {
		if (this.value == null) {
			return this.line + ": Lexical error: " + this.getMessage();
		} else {
			return this.line + ": Lexical error: " + this.getMessage() + " '"
					+ this.value + "'";
		}
	}
}