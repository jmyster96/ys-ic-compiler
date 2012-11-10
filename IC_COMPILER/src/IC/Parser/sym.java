package IC.Parser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** CUP generated class containing symbol constants. */
public class sym {
	/* terminals */
	public static final int DIVIDE = 3;
	public static final int LCBR = 11;
	public static final int LTE = 20;
	public static final int UMINUS = 49;
	public static final int INTEGER = 47;
	public static final int SEMI = 13;
	public static final int CONTINUE = 27;
	public static final int INT = 42;
	public static final int MINUS = 5;
	public static final int STATIC = 38;
	public static final int LT = 21;
	public static final int LP = 7;
	public static final int COMMA = 14;
	public static final int CLASS = 43;
	public static final int RP = 8;
	public static final int PLUS = 4;
	public static final int MULTIPLY = 2;
	public static final int QUOTE = 48;
	public static final int ASSIGN = 17;
	public static final int IF = 30;
	public static final int THIS = 39;
	public static final int ID = 46;
	public static final int DOT = 15;
	public static final int BOOLEAN = 41;
	public static final int EOF = 0;
	public static final int RETURN = 37;
	public static final int RCBR = 12;
	public static final int LB = 9;
	public static final int LAND = 23;
	public static final int EQUAL = 16;
	public static final int TRUE = 32;
	public static final int NEW = 35;
	public static final int error = 1;
	public static final int RB = 10;
	public static final int LOR = 24;
	public static final int NULL = 36;
	public static final int MOD = 6;
	public static final int BREAK = 26;
	public static final int VOID = 40;
	public static final int GTE = 18;
	public static final int ELSE = 31;
	public static final int WHILE = 29;
	public static final int NEQUAL = 22;
	public static final int CLASS_ID = 45;
	public static final int EXTENDS = 28;
	public static final int STRING = 44;
	public static final int LNEG = 25;
	public static final int FALSE = 33;
	public static final int GT = 19;
	public static final int LENGTH = 34;


	private static Map<Integer, String> terminalNames;

	public static String getTerminalName(int terminalIndex) {
		if (terminalNames == null) {
			initTerminalNames();
		}
		return terminalNames.get(terminalIndex);
	}

	/**
	 * Uses reflection to get the name of the symbol stored in this.sym.
	 * 
	 * Needed because we had to use final static int's instead of enums.
	 */
	private static void initTerminalNames() {
		terminalNames = new HashMap<Integer, String>();
		Field[] fields = IC.Parser.sym.class.getFields();
		for (Field field : fields) {
			if (field.getType() == int.class) {
				try {
					int fieldValue = field.getInt(null);
					String fieldName = field.getName();
					terminalNames.put(fieldValue, fieldName);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
