package IC.Symbols;

import IC.SymbolTypes.SymbolTypeTable;

public class ClassSymbolTable extends SymbolTable {

	public ClassSymbolTable(String name, SymbolTypeTable typeTable) {
		super(name, typeTable);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Class Symbol Table";
	}
}
