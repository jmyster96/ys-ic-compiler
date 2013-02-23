package IC.AST;

/**
 * Class instance creation AST node.
 * 
 * @author Tovi Almozlino
 */
public class NewClass extends New {

	private String name;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public <D, U> U accept(PropagatingVisitor<D, U> v, D context) {
		return v.visit(this, context);
	}

	/**
	 * Constructs a new class instance creation expression node.
	 * 
	 * @param line
	 *            Line number of expression.
	 * @param name
	 *            Name of class.
	 */
	public NewClass(int line, String name) {
		super(line);
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
