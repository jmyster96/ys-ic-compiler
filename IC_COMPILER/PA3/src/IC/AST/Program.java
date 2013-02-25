package IC.AST;

import java.util.List;

import IC.Semantic.SemanticError;
import IC.Symbols.GlobalSymbolTable;

/**
 * Root AST node for an IC program.
 * 
 * @author Tovi Almozlino
 */
public class Program extends ASTNode {

	private List<ICClass> classes;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public <D, U> U accept(PropagatingVisitor<D, U> v, D context) {
		return v.visit(this, context);
	}

	/**
	 * Constructs a new program node.
	 * 
	 * @param classes
	 *            List of all classes declared in the program.
	 */
	public Program(List<ICClass> classes) {
		super(0);
		this.classes = classes;
	}

	public List<ICClass> getClasses() {
		return classes;
	}

	GlobalSymbolTable symbolTable;

	public GlobalSymbolTable getGlobalSymbolTable() {
		return symbolTable;
	}

	public void setGlobalSymbolTable(GlobalSymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}
}