package IC.AST;

import IC.Semantic.SemanticError;
import IC.SymbolTypes.SymbolType;
import IC.Symbols.SymbolTable;

/**
 * Abstract AST node base class.
 * 
 * @author Tovi Almozlino
 */
public abstract class ASTNode {

	private int line;
	private SymbolTable enclosingScope;
	private SymbolType symbolType;

	/**
	 * Double dispatch method, to allow a visitor to visit a specific subclass.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return A value propagated by the visitor.
	 * @throws SemanticError
	 */
	public abstract Object accept(Visitor visitor);

	/** accept propagating visitor **/
	public abstract <D, U> U accept(PropagatingVisitor<D, U> v, D context);

	/**
	 * Constructs an AST node corresponding to a line number in the original
	 * code. Used by subclasses.
	 * 
	 * @param line
	 *            The line number.
	 */
	protected ASTNode(int line) {
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	public SymbolTable getEnclosingScope() {
		return enclosingScope;
	}
	
	public void setEnclosingScope(SymbolTable enclosingScope) {
		this.enclosingScope = enclosingScope;
	}
	
	public SymbolType getSymbolType() {
		return symbolType;
	}

	public void setSymbolType(SymbolType symbolType) {
		this.symbolType = symbolType;
	}
}
