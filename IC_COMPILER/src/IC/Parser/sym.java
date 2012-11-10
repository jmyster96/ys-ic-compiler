package IC.Parser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class sym {
	// Punctuation marks
	public static final int LP = 1; // (
	public static final int RP = 2; // )
	public static final int LB = 3; // [
	public static final int RB = 4; // ]
	public static final int LCBR = 5; // {
	public static final int RCBR = 6; // }
	public static final int SEMI = 7; // ;
	public static final int DOT = 8; // .
	public static final int COMMA = 9; // ,

	// Keywords
	public static final int CLASS = 11;
	public static final int EXTENDS = 12;
	public static final int STATIC = 13;
	public static final int VOID = 14;
	public static final int INT = 15;
	public static final int BOOLEAN = 16;
	public static final int STRING = 17;
	public static final int RETURN = 18;
	public static final int IF = 19;
	public static final int ELSE = 20;
	public static final int WHILE = 21;
	public static final int BREAK = 22;
	public static final int CONTINUE = 23;
	public static final int THIS = 24;
	public static final int NEW = 25;
	public static final int LENGTH = 26;
	public static final int TRUE = 27;
	public static final int FALSE = 28;
	public static final int NULL = 29;

	// Stuff that have text in them
	public static final int ID = 30;
	public static final int CLASS_ID = 31;
	public static final int INTEGER = 32;
	public static final int QUOTE = 10; // "

	// Assignment operator
	public static final int ASSIGN = 33; // =

	// Boolean operators
	public static final int EQUAL = 34; // ==
	public static final int NEQUAL = 35; // !=
	public static final int LT = 36; // <
	public static final int GT = 37; // >
	public static final int LTE = 38; // <=
	public static final int GTE = 39; // >=

	// Binary operators
	public static final int PLUS = 40; // +
	public static final int MINUS = 41; // -
	public static final int MULTIPLY = 42; // *
	public static final int DIVIDE = 43; // /
	public static final int MOD = 44; // %

	// Unary operators
	public static final int LNEG = 45; // !

	// Conditional operators
	public static final int LAND = 46; // &&
	public static final int LOR = 47; // ||

	// Special chars
	public static final int EOF = 48; // EOF

	/**********************************************
	 * Code below this line only concerns getting back the names of the symbols,
	 * used for printing Compiler.java.
	 **********************************************/

	/**
	 * Returns the symbol's name by its value.
	 */
	public static String getSymbolName(int symbolValue) {
		if (symbolNames == null) {
			initTerminalNames();
		}
		return symbolNames.get(symbolValue);
	}
	
	private static Map<Integer, String> symbolNames;

	/**
	 * Uses reflection to get the name of the symbol by its numerical value.
	 */
	private static void initTerminalNames() {
		symbolNames = new HashMap<Integer, String>();
		Field[] fields = IC.Parser.sym.class.getFields();
		for (Field field : fields) {
			if (field.getType() == int.class) {
				int fieldValue = getFieldValue(field);
				if (fieldValue == ILLEGAL_VALUE) {
					continue;
				}
				String fieldName = field.getName();
				symbolNames.put(fieldValue, fieldName);
			}
		}
	}

	private static int ILLEGAL_VALUE = -1;

	private static int getFieldValue(Field field) {
		int fieldValue = ILLEGAL_VALUE;
		try {
			fieldValue = field.getInt(null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fieldValue;
	}

}
