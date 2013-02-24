package IC.Symbols;

import IC.AST.ASTNode;
import IC.AST.Field;
import IC.AST.ICClass;
import IC.SymbolTypes.SymbolTypeTable;

public class ClassSymbolTable extends SymbolTable {
	ICClass clazz;

	public ClassSymbolTable(ICClass icClass, SymbolTypeTable typeTable) {
		super(icClass.getName(), icClass, typeTable);
		clazz = icClass;
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Class Symbol Table";
	}

	public int getFieldIndex(String fieldName) throws SymbolTableException {
		int i = 0;
		for (Field field : clazz.getFields()) {
			if (field.getName().equals(fieldName)) {
				return i;
			}
			i++;
		}
		throw new SymbolTableException("Class doesn't have a field with name "
				+ fieldName);
	}
}
