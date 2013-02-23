package IC.Symbols;

import IC.AST.ASTNode;
import IC.AST.ICClass;
import IC.SymbolTypes.SymbolTypeTable;

public class ClassSymbolTable extends SymbolTable {

	public ClassSymbolTable(ICClass icClass, SymbolTypeTable typeTable) {
		super(icClass.getName(), icClass, typeTable);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Class Symbol Table";
	}
}
