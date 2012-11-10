package IC.Parser;

import java_cup.runtime.Symbol;

/**
 * This class holds information for a single token that includes the token line
 * number, ID from sym.java and its value.
 */
public class Token extends Symbol {
	public Token(int id, int line) {
		// call super constructor for Symbol(int sym_num,int l,int r)
		super(id, ++line, 0);
	}

	public Token(int id, int line, Object value) {
		// call super constructor for Symbol(int sym_num,int l,int r,Object
		// value)
		super(id, ++line, 0, value);
	}

	public String toString() {
		String str;
		if (this.value == null) {
			str = this.left + this.symAsString();
		} else {
			str = this.left + ": " + this.symAsString() + "(" + value + ")";
		}
		return str;
	}

	public String symAsString() {
		return "";
	}
}