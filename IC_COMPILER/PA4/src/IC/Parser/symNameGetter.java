package IC.Parser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class symNameGetter {
	
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
	
	public static Set<Integer> getPossibleSymbols() {
		return symbolNames.keySet();
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