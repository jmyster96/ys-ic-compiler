package IC.Parser;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

public class CourtesyErrorReporter {
	public static void tryAnalyzeError(Token currentToken, AbstractList pastTokens) {
		// Currently detects some occurences of a single case (the one we handled in PA2 bonus):
		// The cases of if (cond) int x;
		if (isTypeSymbol(currentToken.sym) && directParentIsConditional(pastTokens)) {
			System.out.println("Line " + currentToken.getLine() + 
					": (PA2 BONUS) Can't have a declaration statement as an only operation inside a conditional branch.");
		} else {
			// System.out.println(joinStrings(pastTokens));
		}
	}

	public static String joinStrings(Collection<?> parts) {
		String joined = "";
		int count = 0;
		for (Object part : parts) {
			if (count > 0)
				joined += ", ";
			joined += part;
			++count;
		}
		return joined;
	}

	private static boolean isTypeSymbol(int curSym) {
		return curSym == sym.INT || curSym == sym.STRING || curSym == sym.BOOLEAN || curSym == sym.CLASS_ID;
	}

	private static boolean directParentIsConditional(AbstractList pastTokens) {
		int size = pastTokens.size();
		return (size > 1
						&& pastTokens.get(size - 1) instanceof Token
						&& ((Token)pastTokens.get(size - 1)).sym == sym.ELSE)
				|| (size > 4
						&& pastTokens.get(size - 4) instanceof Token
						&& (((Token)pastTokens.get(size - 4)).sym == sym.IF)
						 || ((Token)pastTokens.get(size - 4)).sym == sym.WHILE);
	}
}
