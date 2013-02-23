package IC.AST;

/**
 * Array length expression AST node.
 * 
 * @author Tovi Almozlino
 */
public class Length extends Expression {

	private Expression array;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public <D, U> U accept(PropagatingVisitor<D, U> v, D context) {
		return v.visit(this, context);
	}

	/**
	 * Constructs a new array length expression node.
	 * 
	 * @param array
	 *            Expression representing an array.
	 */
	public Length(Expression array) {
		super(array.getLine());
		this.array = array;
	}

	public Expression getArray() {
		return array;
	}

}
