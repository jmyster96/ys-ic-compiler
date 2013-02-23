package IC.AST;

/**
 * AST visitor interface. Declares methods for visiting each type of AST node.
 * 
 * @author Tovi Almozlino
 */
public interface PropagatingVisitor<D, U> {

	public U visit(Program program, D context);

	public U visit(ICClass icClass, D context);

	public U visit(Field field, D context);

	public U visit(VirtualMethod method, D context);

	public U visit(StaticMethod method, D context);

	public U visit(LibraryMethod method, D context);

	public U visit(Formal formal, D context);

	public U visit(PrimitiveType type, D context);

	public U visit(UserType type, D context);

	public U visit(Assignment assignment, D context);

	public U visit(CallStatement callStatement, D context);

	public U visit(Return returnStatement, D context);

	public U visit(If ifStatement, D context);

	public U visit(While whileStatement, D context);

	public U visit(Break breakStatement, D context);

	public U visit(Continue continueStatement, D context);

	public U visit(StatementsBlock statementsBlock, D context);

	public U visit(LocalVariable localVariable, D context);

	public U visit(VariableLocation location, D context);

	public U visit(ArrayLocation location, D context);

	public U visit(StaticCall call, D context);

	public U visit(VirtualCall call, D context);

	public U visit(This thisExpression, D context);

	public U visit(NewClass newClass, D context);

	public U visit(NewArray newArray, D context);

	public U visit(Length length, D context);

	public U visit(MathBinaryOp binaryOp, D context);

	public U visit(LogicalBinaryOp binaryOp, D context);

	public U visit(MathUnaryOp unaryOp, D context);

	public U visit(LogicalUnaryOp unaryOp, D context);

	public U visit(Literal literal, D context);

	public U visit(ExpressionBlock expressionBlock, D context);
}
