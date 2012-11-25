package IC.Parser;

import java_cup.runtime.Symbol;

/**
 * This class holds information for a single token that includes the token line
 * number, ID from sym.java and its value.
 */
public class Token extends Symbol {
	public Token(int id, int line) {
		// call super constructor for Symbol(int sym_num,int l,int r)
		super(id, line + 1, 0);
	}

	public Token(int id, int line, Object value) {
		// call super constructor for Symbol(int sym_num,int l,int r,Object
		// value)
		super(id, line + 1, 0, value);
	}

	public String toString() {
		String symName = IC.Parser.sym.getSymbolName(this.sym);

		String str = this.left + ": " + symName;
		if (value != null) {
			String valueStr = value.toString();
			str += "(" + valueStr + ")";
		}
		return str;
	}
}