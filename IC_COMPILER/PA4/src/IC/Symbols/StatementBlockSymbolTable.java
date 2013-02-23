package IC.Symbols;

import IC.AST.ASTNode;
import IC.SymbolTypes.SymbolTypeTable;

public class StatementBlockSymbolTable extends SymbolTable {

	public StatementBlockSymbolTable(ASTNode relevantAstNode, SymbolTypeTable typeTable) {
		super("statement block", relevantAstNode, typeTable);
	};

	public void setParentName(String parentName) {
		this.setName("statement block in " + parentName);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Statement Block Symbol Table";
	}


}
