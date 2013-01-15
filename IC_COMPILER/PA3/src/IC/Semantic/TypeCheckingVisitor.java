package IC.Semantic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import IC.AST.*;
import IC.Parser.CourtesyErrorReporter;
import IC.Symbols.ArraySymbolType;
import IC.Symbols.ClassSymbolTable;
import IC.Symbols.GlobalSymbolTable;
import IC.Symbols.MethodSymbolTable;
import IC.Symbols.MethodSymbolType;
import IC.Symbols.PrimitiveSymbolType;
import IC.Symbols.PrimitiveSymbolType.PrimitiveSymbolTypes;
import IC.Symbols.ClassSymbolType;
import IC.Symbols.Symbol;
import IC.Symbols.SymbolKind;
import IC.Symbols.SymbolTable;
import IC.Symbols.SymbolTableException;
import IC.Symbols.SymbolType;
import IC.Symbols.SymbolTypeTable;

public class TypeCheckingVisitor implements
		PropagatingVisitor<TypeCheckingVisitorContext, SymbolType> {

	private Stack<SymbolTable> symScopeStack = new Stack<SymbolTable>();
	private List<SemanticError> errors = new ArrayList<SemanticError>();

	public TypeCheckingVisitor() {
	}

	public List<SemanticError> getErrors() {
		return errors;
	}

	private SymbolTable getCurrentScope() {
		return symScopeStack.peek();
	}

	/*
	 * When visit fails return null otherwise return true (!= null)
	 */
	@Override
	public SymbolType visit(Program program, TypeCheckingVisitorContext context) {
		// recursive call to class
		symScopeStack.push(program.getGlobalSymbolTable());
		for (ICClass clazz : program.getClasses()) {
			clazz.accept(this, context);
		}
		symScopeStack.pop();
		return null;
	}

	@Override
	public SymbolType visit(ICClass clazz, TypeCheckingVisitorContext context) {
		symScopeStack.push(clazz.getClassSymbolTable());
		// Set the current class symbol type in the context.
		Symbol classSymbol;
		try {
			classSymbol = getCurrentScope().lookup(clazz.getName());
		} catch (SymbolTableException e) {
			// Not supposed to get here: class should always be in global symbol
			// table.
			return null;
		}
		context.currentClassSymbolType = (ClassSymbolType) getTypeTable()
				.getSymbolById(classSymbol.getTypeId());
		// Visit child nodes.
		for (Method meth : clazz.getMethods()) {
			meth.accept(this, context);
		}
		for (Field fld : clazz.getFields()) {
			fld.accept(this, context);
		}
		context.currentClassSymbolType = null;
		symScopeStack.pop();
		return null;
	}

	@Override
	public SymbolType visit(Field field, TypeCheckingVisitorContext context) {
		return null;
	}

	@Override
	public SymbolType visit(VirtualMethod method,
			TypeCheckingVisitorContext context) {
		visitMethod(method, context);
		return null;
	}

	@Override
	public SymbolType visit(StaticMethod method,
			TypeCheckingVisitorContext context) {
		visitMethod(method, context);
		return null;
	}

	@Override
	public SymbolType visit(LibraryMethod method,
			TypeCheckingVisitorContext context) {
		visitMethod(method, context);
		return null;
	}

	private void visitMethod(Method method, TypeCheckingVisitorContext context) {
		symScopeStack.push(method.getMethodSymbolTable());
		int typeId = -1;
		try {
			typeId = method.getMethodSymbolTable().getParent()
					.lookup(method.getName()).getTypeId();
		} catch (SymbolTableException e) {
			// Not supposed to get here, unless there's a bug in
			// SymbolTableBuilder.
			e.printStackTrace();
		}
		if (typeId != -1) {
			context.currentMethodSymbolType = (MethodSymbolType) getTypeTable()
					.getSymbolById(typeId);
			for (Statement stmnt : method.getStatements()) {
				stmnt.accept(this, context);
			}
			context.currentMethodSymbolType = null;
		}
		symScopeStack.pop();
	}

	@Override
	public SymbolType visit(Formal formal, TypeCheckingVisitorContext context) {
		return null;
	}

	@Override
	public SymbolType visit(PrimitiveType type,
			TypeCheckingVisitorContext context) {
		return null;
	}

	@Override
	public SymbolType visit(UserType type, TypeCheckingVisitorContext context) {
		return null;
	}

	@Override
	public SymbolType visit(Assignment assignment,
			TypeCheckingVisitorContext context) {
		SymbolType variable = assignment.getVariable().accept(this, context);
		SymbolType expression = assignment.getAssignment()
				.accept(this, context);
		checkTypeError(assignment, variable, expression);
		return getVoidType();
	}

	private boolean checkTypeError(ASTNode node, SymbolType expectedType,
			SymbolType actualType) {
		if (!getTypeTable().isTypeLessThanOrEquals(actualType, expectedType)) {
			errors.add(new SemanticError("Type error in node '"
					+ node.getClass().getSimpleName() + "': unexpected type: '"
					+ actualType
					+ "', expected a type that is less than or equals to: '"
					+ expectedType + "'", node.getLine()));
			return false;
		}
		return true;
	}

	private SymbolTypeTable getTypeTable() {
		return getCurrentScope().getTypeTable();
	}

	private SymbolType getVoidType() {
		return new PrimitiveSymbolType(
				PrimitiveSymbolType.PrimitiveSymbolTypes.VOID);
	}

	private SymbolType getPrimitiveType(PrimitiveSymbolTypes type) {
		return new PrimitiveSymbolType(type);
	}

	@Override
	public SymbolType visit(CallStatement callStatement,
			TypeCheckingVisitorContext context) {
		callStatement.getCall().accept(this, context);
		return getVoidType();
	}

	@Override
	public SymbolType visit(Return returnStatement,
			TypeCheckingVisitorContext context) {
		SymbolType returnType = context.currentMethodSymbolType.getReturnType();
		// 4 options:
		// 1. void method, with return [expr]; -- Error
		// 2. void method, with return; -- OK
		// 3. non-void method, with return [expr] -- Check matching types
		// 4. non-void method, with return; -- Error
		if (returnStatement.hasValue()
				&& context.currentMethodSymbolType.getReturnType().equals(
						getVoidType())) {
			errors.add(new SemanticError(
					"A 'void' method is trying to return a value",
					returnStatement.getLine()));
		} else if (!returnStatement.hasValue()
				&& !context.currentMethodSymbolType.getReturnType().equals(
						getVoidType())) {
			errors.add(new SemanticError(
					"A non-'void' method should return a value",
					returnStatement.getLine()));
		} else if (returnStatement.hasValue()) {
			SymbolType expression = returnStatement.getValue().accept(this,
					context);
			checkTypeError(returnStatement, returnType, expression);
		}
		return getVoidType();
	}

	@Override
	public SymbolType visit(If ifStatement, TypeCheckingVisitorContext context) {
		SymbolType conditionExpression = ifStatement.getCondition().accept(
				this, context);
		checkTypeError(
				ifStatement,
				getPrimitiveType(PrimitiveSymbolType.PrimitiveSymbolTypes.BOOLEAN),
				conditionExpression);
		ifStatement.getOperation().accept(this, context);
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this, context);
		}
		return getVoidType();
	}

	@Override
	public SymbolType visit(While whileStatement,
			TypeCheckingVisitorContext context) {
		SymbolType conditionExpression = whileStatement.getCondition().accept(
				this, context);
		checkTypeError(
				whileStatement,
				getPrimitiveType(PrimitiveSymbolType.PrimitiveSymbolTypes.BOOLEAN),
				conditionExpression);
		whileStatement.getOperation().accept(this, context);
		return getVoidType();
	}

	@Override
	public SymbolType visit(Break breakStatement,
			TypeCheckingVisitorContext context) {
		return getVoidType();
	}

	@Override
	public SymbolType visit(Continue continueStatement,
			TypeCheckingVisitorContext context) {
		return getVoidType();
	}

	@Override
	public SymbolType visit(StatementsBlock statementsBlock,
			TypeCheckingVisitorContext context) {
		symScopeStack.push(statementsBlock.getStatementsBlockSymbolTable());
		for (Statement stmt : statementsBlock.getStatements()) {
			stmt.accept(this, context);
		}
		symScopeStack.pop();
		return getVoidType();
	}

	@Override
	public SymbolType visit(LocalVariable localVariable,
			TypeCheckingVisitorContext context) {
		if (localVariable.hasInitValue()) {
			SymbolType variableType = getSymbolType(localVariable.getName());
			if (variableType != null) {
				SymbolType initValueType = localVariable.getInitValue().accept(
						this, context);

				checkTypeError(localVariable, variableType, initValueType);
			}
		}
		return null;
	}

	private SymbolType getSymbolType(String symbolName) {
		SymbolType symbolType;
		try {
			int symbolTypeId = getCurrentScope().lookup(symbolName).getTypeId();
			symbolType = getTypeTable().getSymbolById(symbolTypeId);

		} catch (SymbolTableException e) {
			// Not supposed to get here: a variable was just declared.
			symbolType = null;
		}
		return symbolType;
	}

	@Override
	public SymbolType visit(VariableLocation location,
			TypeCheckingVisitorContext context) {
		if (location.isExternal()) {
			SymbolType locationType = location.getLocation().accept(this,
					context);
			if (!(locationType instanceof ClassSymbolType)) {
				errors.add(new SemanticError(
						"Location is not a class, can't look for field '"
								+ location.getName()
								+ "' under expression of type '" + locationType
								+ "'", location.getLine()));
				return getVoidType();
			} else {
				ClassSymbolType classSymbolType = (ClassSymbolType) locationType;
				String className = classSymbolType.getName();
				SymbolTable classSymbolTable;
				try {
					classSymbolTable = getCurrentScope().lookupScope(className);
				} catch (SymbolTableException e) {
					// Not supposed to get here: if there's a symbol of this
					// type
					System.out.println("Unexpected compiler error.");
					e.printStackTrace();
					return null;
				}
				try {
					Symbol variableSymbol = classSymbolTable.lookup(location
							.getName());
					return getTypeTable().getSymbolById(
							variableSymbol.getTypeId());
				} catch (SymbolTableException e) {
					// HACK: We didn't do this in ScopeChecking, doing it
					// here. Checking if location has member of this name.
					errors.add(new SemanticError(e.getMessage(), location
							.getLine()));
					return getVoidType();
				}
			}
		} else {
			return getSymbolType(location.getName());
		}
	}

	@Override
	public SymbolType visit(ArrayLocation location,
			TypeCheckingVisitorContext context) {
		SymbolType indexType = location.getIndex().accept(this, context);
		checkTypeError(location, getPrimitiveType(PrimitiveSymbolTypes.INT),
				indexType);
		SymbolType locationType = location.getArray().accept(this, context);
		if (!(locationType instanceof ArraySymbolType)) {
			errors.add(new SemanticError(
					"Value is treated as an array when it is actaully of type '"
							+ locationType + "'", location.getLine()));
			return getVoidType();
		}
		ArraySymbolType arrayType = (ArraySymbolType) locationType;
		return arrayType.getBaseType();
	}

	@Override
	public SymbolType visit(StaticCall call, TypeCheckingVisitorContext context) {
		Symbol methodSymbol;
		try {
			SymbolTable otherScope = getCurrentScope().lookupScope(
					call.getClassName());
			methodSymbol = otherScope.lookup(call.getName());
		} catch (SymbolTableException e) {
			// Call is illegal: scope check already reported this.
			return getVoidType();
		}

		// Scope checking should have made sure that this is a STATIC_METHOD
		// symbol.
		MethodSymbolType methodSymbolType = (MethodSymbolType) getTypeTable()
				.getSymbolById(methodSymbol.getTypeId());

		String methodName = call.getClassName() + "." + call.getName();

		if (!checkMethodCallTypeMatching(call, methodName,
				methodSymbolType.getFormalsTypes(), context)) {
			// Do nothing: just report errors, but return the true return type
			// of the method.
		}

		return methodSymbolType.getReturnType();
	}

	private boolean checkMethodCallTypeMatching(Call call, String methodName,
			List<SymbolType> argumentsExpectedTypes,
			TypeCheckingVisitorContext context) {
		List<SymbolType> argumentsTypes = new ArrayList<SymbolType>();
		for (Expression arg : call.getArguments()) {
			argumentsTypes.add(arg.accept(this, context));
		}

		boolean isCallLegal = true;
		if (argumentsExpectedTypes.size() != argumentsTypes.size()) {
			errors.add(new SemanticError(
					"Wrong number of arguments on call to method ["
							+ methodName + "]. Expected: "
							+ argumentsExpectedTypes.size() + ", got: "
							+ argumentsTypes.size(), call.getLine()));
			isCallLegal = false;
		} else {
			for (int i = 0; i < argumentsExpectedTypes.size(); ++i) {
				isCallLegal &= checkTypeError(call,
						argumentsExpectedTypes.get(i), argumentsTypes.get(i));
			}
		}
		return isCallLegal;
	}

	@Override
	public SymbolType visit(VirtualCall call, TypeCheckingVisitorContext context) {
		for (Expression arg : call.getArguments()) {
			arg.accept(this, context);
		}
		Symbol methodSymbol;
		String methodName;
		if (call.isExternal()) {
			// 1. Get Class type of location (type checking should have this
			// info)
			SymbolType locationType = call.getLocation().accept(this, context);
			if (!(locationType instanceof ClassSymbolType)) {
				errors.add(new SemanticError(
						"Can't invoke a method: expression is not a reference to a class object.",
						call.getLine()));
				return getVoidType();
			}
			// 2. Get symbol table for that class
			ClassSymbolType classLocation = (ClassSymbolType) locationType;
			SymbolTable classScope;
			try {
				classScope = getCurrentScope().lookupScope(
						classLocation.getName());
			} catch (SymbolTableException e) {
				// This means that the expression evaluates to a class symbol of
				// a class that doesn't exist. This means that somewhere there
				// was a
				// variable that was defined with this class name, and
				// ScopeChecker
				// would've reported it there.
				errors.add(new SemanticError(
						"Can't invoke a method: expression couldn't find class of type "
								+ classLocation.getName() + ".", call.getLine()));
				return getVoidType();
			}

			// 3. HACK: Scope Checking: Verify the class has the method.
			try {
				methodSymbol = classScope.lookup(call.getName());
			} catch (SymbolTableException e) {
				errors.add(new SemanticError(e.getMessage(), call.getLine()));
				return getVoidType();
			}
			methodName = classLocation.getName() + "." + call.getName();
		} else {
			// Not external: verify that there's a method in current scope.
			try {
				methodSymbol = getCurrentScope().lookup(call.getName());
			} catch (SymbolTableException e) {
				// Call is illegal: scope check already reported this.
				return getVoidType();
			}
			methodName = call.getName();
		}
		MethodSymbolType methodSymbolType = (MethodSymbolType) getTypeTable()
				.getSymbolById(methodSymbol.getTypeId());

		checkMethodCallTypeMatching(call, methodName,
				methodSymbolType.getFormalsTypes(), context);

		return methodSymbolType.getReturnType();
	}

	@Override
	public SymbolType visit(This thisExpression,
			TypeCheckingVisitorContext context) {
		return context.currentClassSymbolType;
	}

	@Override
	public SymbolType visit(NewClass newClass,
			TypeCheckingVisitorContext context) {
		Symbol classSymbol;
		try {
			classSymbol = getCurrentScope().lookup(newClass.getName());
		} catch (SymbolTableException e) {
			// ScopeChecker already checked this...
			return getVoidType();
		}
		return getTypeTable().getSymbolById(classSymbol.getTypeId());
	}

	@Override
	public SymbolType visit(NewArray newArray,
			TypeCheckingVisitorContext context) {
		SymbolType sizeType = newArray.getSize().accept(this, context);
		checkTypeError(newArray, getPrimitiveType(PrimitiveSymbolTypes.INT),
				sizeType);
		SymbolType arrayType = getTypeTable().getSymbolById(
				getTypeTable().getSymbolTypeId(newArray.getType(), 1));
		return arrayType;
	}

	@Override
	public SymbolType visit(Length length, TypeCheckingVisitorContext context) {
		SymbolType arrayType = length.getArray().accept(this, context);
		if (!(arrayType instanceof ArraySymbolType)) {
			errors.add(new SemanticError(
					"length can only be run on arrays; got type: " + arrayType,
					length.getLine()));
		}
		return getPrimitiveType(PrimitiveSymbolTypes.INT);
	}

	@Override
	public SymbolType visit(MathBinaryOp binaryOp,
			TypeCheckingVisitorContext context) {
		switch (binaryOp.getOperator()) {
		case DIVIDE:
		case MINUS:
		case MOD:
		case MULTIPLY:
		case PLUS:
			checkBinaryOp(binaryOp, context,
					getPrimitiveType(PrimitiveSymbolTypes.INT));
			return getPrimitiveType(PrimitiveSymbolTypes.INT);
		case GT:
		case GTE:
		case LT:
		case LTE:
			checkBinaryOp(binaryOp, context,
					getPrimitiveType(PrimitiveSymbolTypes.INT));
			return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
		case EQUAL:
		case NEQUAL:
			SymbolType leftType = binaryOp.getFirstOperand().accept(this,
					context);
			SymbolType rightType = binaryOp.getSecondOperand().accept(this,
					context);
			if (!(getTypeTable().isTypeLessThanOrEquals(leftType, rightType) || getTypeTable()
					.isTypeLessThanOrEquals(rightType, leftType))) {
				errors.add(new SemanticError(
						"Can't check equality in non-matching types. Types: "
								+ leftType + ", " + rightType, binaryOp
								.getLine()));
			}
			return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
		default:
			// Should never get here: binary op must be of above types.
			return getVoidType();
		}
	}

	private void checkBinaryOp(BinaryOp binaryOp,
			TypeCheckingVisitorContext context, SymbolType opType) {
		SymbolType leftType = binaryOp.getFirstOperand().accept(this, context);
		SymbolType rightType = binaryOp.getSecondOperand()
				.accept(this, context);
		checkTypeError(binaryOp, opType, leftType);
		checkTypeError(binaryOp, opType, rightType);
	}

	@Override
	public SymbolType visit(LogicalBinaryOp binaryOp,
			TypeCheckingVisitorContext context) {
		checkBinaryOp(binaryOp, context,
				getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
		return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
	}

	@Override
	public SymbolType visit(MathUnaryOp unaryOp,
			TypeCheckingVisitorContext context) {
		return checkUnaryOp(unaryOp, context,
				getPrimitiveType(PrimitiveSymbolTypes.INT));
	}

	private SymbolType checkUnaryOp(UnaryOp unaryOp,
			TypeCheckingVisitorContext context, SymbolType expectedType) {
		SymbolType operandType = unaryOp.getOperand().accept(this, context);
		checkTypeError(unaryOp, expectedType, operandType);
		return expectedType;
	}

	@Override
	public SymbolType visit(LogicalUnaryOp unaryOp,
			TypeCheckingVisitorContext context) {
		return checkUnaryOp(unaryOp, context,
				getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
	}

	@Override
	public SymbolType visit(Literal literal, TypeCheckingVisitorContext context) {
		switch (literal.getType()) {
		case TRUE:
		case FALSE:
			return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
		case INTEGER:
			return getPrimitiveType(PrimitiveSymbolTypes.INT);
		case NULL:
			return getPrimitiveType(PrimitiveSymbolTypes.NULL);
		case STRING:
			return getPrimitiveType(PrimitiveSymbolTypes.STRING);
		default:
			// Should never get here, a literal must have one of the above
			// values.
			return getVoidType();
		}
	}

	@Override
	public SymbolType visit(ExpressionBlock expressionBlock,
			TypeCheckingVisitorContext context) {
		return expressionBlock.getExpression().accept(this, context);
	}

}
