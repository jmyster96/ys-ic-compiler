package IC.AST;

import java.io.IOException;
import java.io.Writer;

/**
 * Pretty printing visitor - travels along the AST and prints info about each
 * node, in an easy-to-comprehend format.
 * 
 * @author Tovi Almozlino
 */
public class PrettyPrinter implements Visitor {

	private int depth = 0; // depth of indentation
	private Writer writer;

	private String ICFilePath;

	/**
	 * Constructs a new pretty printer visitor.
	 * 
	 * @param ICFilePath
	 *            The path + name of the IC file being compiled.
	 */
	public PrettyPrinter(String ICFilePath, Writer writer) {
		this.ICFilePath = ICFilePath;
		this.writer = writer;
	}

	private void indent(ASTNode node) {
		append("\n");
		for (int i = 0; i < depth; ++i)
			append(" ");
		if (node != null)
			append(node.getLine() + ": ");
	}

	private void indent() {
		indent(null);
	}
	
	private void append(String message) {
		System.out.print(message);
	}

	public Object visit(Program program) {
		indent();
		append("Abstract Syntax Tree: " + ICFilePath + "\n");
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		return null;
	}

	public Object visit(ICClass icClass) {
		indent(icClass);
		append("Declaration of class: " + icClass.getName());
		if (icClass.hasSuperClass())
			append(", subclass of " + icClass.getSuperClassName());
		depth += 2;
		for (Field field : icClass.getFields())
			field.accept(this);
		for (Method method : icClass.getMethods())
			method.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(PrimitiveType type) {
		indent(type);
		append("Primitive data type: ");
		if (type.getDimension() > 0)
			append(type.getDimension() + "-dimensional array of ");
		append(type.getName());
		return null;
	}

	public Object visit(UserType type) {
		indent(type);
		append("User-defined data type: ");
		if (type.getDimension() > 0)
			append(type.getDimension() + "-dimensional array of ");
		append(type.getName());
		return null;
	}

	public Object visit(Field field) {
		indent(field);
		append("Declaration of field: " + field.getName());
		++depth;
		field.getType().accept(this);
		--depth;
		return null;
	}

	public Object visit(LibraryMethod method) {
		indent(method);
		append("Declaration of library method: " + method.getName());
		depth += 2;
		method.getType().accept(this);
		for (Formal formal : method.getFormals())
			formal.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(Formal formal) {
		indent(formal);
		append("Parameter: " + formal.getName());
		++depth;
		formal.getType().accept(this);
		--depth;
		return null;
	}

	public Object visit(VirtualMethod method) {
		indent(method);
		append("Declaration of virtual method: " + method.getName());
		depth += 2;
		method.getType().accept(this);
		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements())
			statement.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(StaticMethod method) {
		indent(method);
		append("Declaration of static method: " + method.getName());
		depth += 2;
		method.getType().accept(this);
		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Statement statement : method.getStatements())
			statement.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(Assignment assignment) {
		indent(assignment);
		append("Assignment statement");
		depth += 2;
		assignment.getVariable().accept(this);
		assignment.getAssignment().accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(CallStatement callStatement) {
		indent(callStatement);
		append("Method call statement");
		++depth;
		callStatement.getCall().accept(this);
		--depth;
		return null;
	}

	public Object visit(Return returnStatement) {
		indent(returnStatement);
		append("Return statement");
		if (returnStatement.hasValue())
			append(", with return value");
		if (returnStatement.hasValue()) {
			++depth;
			returnStatement.getValue().accept(this);
			--depth;
		}
		return null;
	}

	public Object visit(If ifStatement) {
		indent(ifStatement);
		append("If statement");
		if (ifStatement.hasElse())
			append(", with Else operation");
		depth += 2;
		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(While whileStatement) {
		indent(whileStatement);
		append("While statement");
		depth += 2;
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(Break breakStatement) {
		indent(breakStatement);
		append("Break statement");
		return null;
	}

	public Object visit(Continue continueStatement) {
		indent(continueStatement);
		append("Continue statement");
		return null;
	}

	public Object visit(StatementsBlock statementsBlock) {
		indent(statementsBlock);
		append("Block of statements");
		depth += 2;
		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(LocalVariable localVariable) {
		indent(localVariable);
		append("Declaration of local variable: "
				+ localVariable.getName());
		if (localVariable.hasInitValue()) {
			append(", with initial value");
			++depth;
		}
		++depth;
		localVariable.getType().accept(this);
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this);
			--depth;
		}
		--depth;
		return null;
	}

	public Object visit(VariableLocation location) {
		indent(location);
		append("Reference to variable: " + location.getName());
		if (location.isExternal())
			append(", in external scope");
		if (location.isExternal()) {
			++depth;
			location.getLocation().accept(this);
			--depth;
		}
		return null;
	}

	public Object visit(ArrayLocation location) {
		indent(location);
		append("Reference to array");
		depth += 2;
		location.getArray().accept(this);
		location.getIndex().accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(StaticCall call) {
		indent(call);
		append("Call to static method: " + call.getName()
				+ ", in class " + call.getClassName());
		depth += 2;
		for (Expression argument : call.getArguments())
			argument.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(VirtualCall call) {
		indent(call);
		append("Call to virtual method: " + call.getName());
		if (call.isExternal())
			append(", in external scope");
		depth += 2;
		if (call.isExternal())
			call.getLocation().accept(this);
		for (Expression argument : call.getArguments())
			argument.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(This thisExpression) {
		indent(thisExpression);
		append("Reference to 'this' instance");
		return null;
	}

	public Object visit(NewClass newClass) {
		indent(newClass);
		append("Instantiation of class: " + newClass.getName());
		return null;
	}

	public Object visit(NewArray newArray) {
		indent(newArray);
		append("Array allocation");
		depth += 2;
		newArray.getType().accept(this);
		newArray.getSize().accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(Length length) {
		indent(length);
		append("Reference to array length");
		++depth;
		length.getArray().accept(this);
		--depth;
		return null;
	}

	public Object visit(MathBinaryOp binaryOp) {
		indent(binaryOp);
		append("Mathematical binary operation: "
				+ binaryOp.getOperator().getDescription());
		depth += 2;
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		indent(binaryOp);
		append("Logical binary operation: "
				+ binaryOp.getOperator().getDescription());
		depth += 2;
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(MathUnaryOp unaryOp) {
		indent(unaryOp);
		append("Mathematical unary operation: "
				+ unaryOp.getOperator().getDescription());
		++depth;
		unaryOp.getOperand().accept(this);
		--depth;
		return null;
	}

	public Object visit(LogicalUnaryOp unaryOp) {
		indent(unaryOp);
		append("Logical unary operation: "
				+ unaryOp.getOperator().getDescription());
		++depth;
		unaryOp.getOperand().accept(this);
		--depth;
		return null;
	}

	public Object visit(Literal literal) {
		indent(literal);
		append(literal.getType().getDescription() + ": "
				+ literal.getType().toFormattedString(literal.getValue()));
		return writer;
	}

	public Object visit(ExpressionBlock expressionBlock) {
		indent(expressionBlock);
		append("Parenthesized expression");
		++depth;
		expressionBlock.getExpression().accept(this);
		--depth;
		return writer;
	}
}