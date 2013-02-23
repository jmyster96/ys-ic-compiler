package IC.Semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import IC.UnaryOps;
import IC.AST.*;
import IC.SymbolTypes.ArraySymbolType;
import IC.SymbolTypes.ClassSymbolType;
import IC.SymbolTypes.MethodSymbolType;
import IC.SymbolTypes.PrimitiveSymbolType;
import IC.SymbolTypes.SymbolType;
import IC.SymbolTypes.SymbolTypeTable;
import IC.SymbolTypes.PrimitiveSymbolType.PrimitiveSymbolTypes;
import IC.Symbols.ClassSymbolTable;
import IC.Symbols.Symbol;
import IC.Symbols.SymbolKind;
import IC.Symbols.SymbolTable;
import IC.Symbols.SymbolTableException;

public class TypeCheckingVisitor implements
		PropagatingVisitor<TypeCheckingVisitorContext, SymbolType> {

	private List<SemanticError> errors = new ArrayList<SemanticError>();
	private SymbolTypeTable typeTable;
	private TypeComparer typeComparer;

	public TypeCheckingVisitor() {
	}

	public List<SemanticError> getErrors() {
		return errors;
	}

	/*
	 * When visit fails return null otherwise return true (!= null)
	 */
	@Override
	public SymbolType visit(Program program, TypeCheckingVisitorContext context) {
		// HACK: Initialize the type comparer here (because it needs a type
		// table).
		typeTable = program.getEnclosingScope().getTypeTable();
		typeComparer = new TypeComparer(typeTable);

		for (ICClass clazz : program.getClasses()) {
			clazz.accept(this, context);
		}
		return null;
	}

	@Override
	public SymbolType visit(ICClass clazz, TypeCheckingVisitorContext context) {
		// Set the current class symbol type in the context.
		Symbol classSymbol;
		try {
			classSymbol = clazz.getEnclosingScope().lookup(clazz.getName());
		} catch (SymbolTableException e) {
			// Not supposed to get here: class should always be in global symbol
			// table.
			return null;
		}
		context.currentClassSymbolType = (ClassSymbolType) getTypeTable()
				.getSymbolById(classSymbol.getTypeId());
		clazz.setSymbolType(context.currentClassSymbolType);
		// Visit child nodes.
		for (Method meth : clazz.getMethods()) {
			meth.accept(this, context);
		}
		for (Field fld : clazz.getFields()) {
			fld.accept(this, context);
		}
		context.currentClassSymbolType = null;
		return null;
	}

	@Override
	public SymbolType visit(Field field, TypeCheckingVisitorContext context) {
		return null;
	}

	@Override
	public SymbolType visit(VirtualMethod method,
			TypeCheckingVisitorContext context) {
		MethodSymbolType methodSymbolType = visitMethod(method, context);
		method.setSymbolType(methodSymbolType);
		verifyVirtualMethodOverridingIsLegal(method, context, methodSymbolType);

		return methodSymbolType;
	}

	private void verifyVirtualMethodOverridingIsLegal(VirtualMethod method,
			TypeCheckingVisitorContext context,
			MethodSymbolType methodSymbolType) {

		Symbol methodInBaseClass = getMethodInBaseClass(method, context);

		if (methodInBaseClass == null) {
			return;
		}
		SymbolType symbolInBaseClassType = getTypeTable().getSymbolById(
				methodInBaseClass.getTypeId());

		boolean symbolHidingLegal = false;
		String errorMessage = "";

		if (methodInBaseClass.getKind() == SymbolKind.VIRTUAL_METHOD) {
			MethodSymbolType methodInBaseClassType = (MethodSymbolType) symbolInBaseClassType;
			if (methodInBaseClassType.getFormalsTypes().size() != methodSymbolType
					.getFormalsTypes().size()) {
				errorMessage += "Wrong number of arguments";
			} else {
				symbolHidingLegal = true;
				for (int i = 0; i < methodInBaseClassType.getFormalsTypes()
						.size(); ++i) {
					if (!typeComparer.isTypeLessThanOrEquals(
							methodInBaseClassType.getFormalsTypes().get(i),
							methodSymbolType.getFormalsTypes().get(i))) {
						symbolHidingLegal = false;
						errorMessage += "Type of arg"
								+ i
								+ " ('"
								+ method.getFormals().get(i).getName()
								+ "')"
								+ " is expected to be >= '"
								+ methodInBaseClassType.getFormalsTypes()
										.get(i) + "', but it is '"
								+ methodSymbolType.getFormalsTypes().get(i)
								+ "'\n";
					}
				}
			}
			if (!typeComparer.isTypeLessThanOrEquals(
					methodSymbolType.getReturnType(),
					methodInBaseClassType.getReturnType())) {
				errorMessage += "Return type '"
						+ methodSymbolType.getReturnType()
						+ "' is expected to <= '"
						+ methodInBaseClassType.getReturnType() + "'\n";
				symbolHidingLegal = false;
			}
		} else if (methodInBaseClass.getKind() == SymbolKind.STATIC_METHOD) {
			errorMessage += "Method in base class is marked as static";
		}
		if (!symbolHidingLegal) {
			reportError("Method [" + method.getName() + "] hides '"
					+ methodInBaseClass.getKind()
					+ "' in base class. Method signature: " + methodSymbolType
					+ ", base class type: " + symbolInBaseClassType
					+ ". Errors:\n" + errorMessage, method);
		}
	}

	private Symbol getMethodInBaseClass(Method method,
			TypeCheckingVisitorContext context) {
		if (!context.currentClassSymbolType.hasBaseClass()) {
			return null;
		}
		ClassSymbolTable classScope = (ClassSymbolTable) method
				.getEnclosingScope().getParent();
		ClassSymbolTable baseClassScope = (ClassSymbolTable) classScope
				.getParent();
		try {
			return baseClassScope.lookup(method.getName());
		} catch (SymbolTableException e) {
			// This means this method doesn't hide anything. That's fine.
			return null;
		}
	}

	private void reportError(String message, ASTNode node) {
		errors.add(new SemanticError(message, node.getLine()));
	}

	@Override
	public SymbolType visit(StaticMethod method,
			TypeCheckingVisitorContext context) {
		Symbol methodInBaseClass = getMethodInBaseClass(method, context);

		if (methodInBaseClass != null) {
			reportError("Method hides another method in base class.", method);
		}

		return visitMethod(method, context);
	}

	@Override
	public SymbolType visit(LibraryMethod method,
			TypeCheckingVisitorContext context) {
		return visitMethod(method, context);
	}

	private MethodSymbolType visitMethod(Method method,
			TypeCheckingVisitorContext context) {
		int typeId = -1;
		try {
			typeId = method.getEnclosingScope().getParent()
					.lookup(method.getName()).getTypeId();
		} catch (SymbolTableException e) {
			// Not supposed to get here, unless there's a bug in
			// SymbolTableBuilder.
			e.printStackTrace();
		}
		MethodSymbolType symbolType = (MethodSymbolType) getTypeTable()
				.getSymbolById(typeId);
		context.currentMethodSymbolType = symbolType;
		for (Statement stmnt : method.getStatements()) {
			stmnt.accept(this, context);
		}
		context.currentMethodSymbolType = null;
		return symbolType;
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
		assignment.setSymbolType(getVoidType());
		return getVoidType();
	}

	private boolean checkTypeError(ASTNode node, SymbolType expectedType,
			SymbolType actualType) {
		if (!typeComparer.isTypeLessThanOrEquals(actualType, expectedType)) {
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
		return typeTable;
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
		callStatement.setSymbolType(getVoidType());
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
		returnStatement.setSymbolType(getVoidType());
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
		ifStatement.setSymbolType(getVoidType());
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
		whileStatement.setSymbolType(getVoidType());
		return getVoidType();
	}

	@Override
	public SymbolType visit(Break breakStatement,
			TypeCheckingVisitorContext context) {
		breakStatement.setSymbolType(getVoidType());
		return getVoidType();
	}

	@Override
	public SymbolType visit(Continue continueStatement,
			TypeCheckingVisitorContext context) {
		continueStatement.setSymbolType(getVoidType());
		return getVoidType();
	}

	@Override
	public SymbolType visit(StatementsBlock statementsBlock,
			TypeCheckingVisitorContext context) {
		for (Statement stmt : statementsBlock.getStatements()) {
			stmt.accept(this, context);
		}
		statementsBlock.setSymbolType(getVoidType());
		return getVoidType();
	}

	@Override
	public SymbolType visit(LocalVariable localVariable,
			TypeCheckingVisitorContext context) {
		if (localVariable.hasInitValue()) {
			SymbolType variableType = getSymbolType(localVariable,
					localVariable.getName());
			localVariable.setSymbolType(variableType);
			if (variableType != null) {
				SymbolType initValueType = localVariable.getInitValue().accept(
						this, context);

				checkTypeError(localVariable, variableType, initValueType);
			}
		}
		return null;
	}

	private SymbolType getSymbolType(ASTNode node, String symbolName) {
		SymbolType symbolType;
		try {
			int symbolTypeId = node.getEnclosingScope().lookup(symbolName)
					.getTypeId();
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
				location.setSymbolType(getVoidType());
				return getVoidType();
			} else {
				ClassSymbolType classSymbolType = (ClassSymbolType) locationType;
				String className = classSymbolType.getName();
				SymbolTable classSymbolTable;
				try {
					classSymbolTable = location.getEnclosingScope()
							.lookupScope(className);
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
					SymbolType exprType = getTypeTable().getSymbolById(
							variableSymbol.getTypeId());
					location.setSymbolType(exprType);
					return exprType;
				} catch (SymbolTableException e) {
					// HACK: We didn't do this in ScopeChecking, doing it
					// here. Checking if location has member of this name.
					errors.add(new SemanticError(e.getMessage(), location
							.getLine()));
					location.setSymbolType(getVoidType());
					return getVoidType();
				}
			}
		} else {
			location.setSymbolType(getSymbolType(location, location.getName()));
			return location.getSymbolType();
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
			location.setSymbolType(getVoidType());
			return getVoidType();
		}
		ArraySymbolType arrayType = (ArraySymbolType) locationType;
		location.setSymbolType(arrayType.getBaseType());
		return arrayType.getBaseType();
	}

	@Override
	public SymbolType visit(StaticCall call, TypeCheckingVisitorContext context) {
		Symbol methodSymbol;
		try {
			SymbolTable otherScope = call.getEnclosingScope().lookupScope(
					call.getClassName());
			methodSymbol = otherScope.lookup(call.getName());
		} catch (SymbolTableException e) {
			// Call is illegal: scope check already reported this.
			call.setSymbolType(getVoidType());
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

		call.setSymbolType(methodSymbolType.getReturnType());
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
				call.setSymbolType(getVoidType());
				return getVoidType();
			}
			// 2. Get symbol table for that class
			ClassSymbolType classLocation = (ClassSymbolType) locationType;
			SymbolTable classScope;
			try {
				classScope = call.getEnclosingScope().lookupScope(
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
				call.setSymbolType(getVoidType());
				return getVoidType();
			}

			// 3. HACK: Scope Checking: Verify the class has the method.
			try {
				methodSymbol = classScope.lookup(call.getName());
			} catch (SymbolTableException e) {
				errors.add(new SemanticError(e.getMessage(), call.getLine()));
				call.setSymbolType(getVoidType());
				return getVoidType();
			}
			methodName = classLocation.getName() + "." + call.getName();
		} else {
			// Not external: verify that there's a method in current scope.
			try {
				methodSymbol = call.getEnclosingScope().lookup(call.getName());
			} catch (SymbolTableException e) {
				// Call is illegal: scope check already reported this.
				call.setSymbolType(getVoidType());
				return getVoidType();
			}
			methodName = call.getName();
		}
		MethodSymbolType methodSymbolType = (MethodSymbolType) getTypeTable()
				.getSymbolById(methodSymbol.getTypeId());

		checkMethodCallTypeMatching(call, methodName,
				methodSymbolType.getFormalsTypes(), context);

		call.setSymbolType(methodSymbolType.getReturnType());
		return methodSymbolType.getReturnType();
	}

	@Override
	public SymbolType visit(This thisExpression,
			TypeCheckingVisitorContext context) {
		if (context.currentMethodSymbolType.isStatic()) {
			errors.add(new SemanticError(
					"Can't use 'this' expression in a static method",
					thisExpression.getLine()));
			thisExpression.setSymbolType(getVoidType());
			return getVoidType();
		}
		thisExpression.setSymbolType(context.currentClassSymbolType);
		return context.currentClassSymbolType;
	}

	@Override
	public SymbolType visit(NewClass newClass,
			TypeCheckingVisitorContext context) {
		Symbol classSymbol;
		try {
			classSymbol = newClass.getEnclosingScope().lookup(
					newClass.getName());
		} catch (SymbolTableException e) {
			// ScopeChecker already checked this...
			newClass.setSymbolType(getVoidType());
			return getVoidType();
		}
		newClass.setSymbolType(getTypeTable().getSymbolById(
				classSymbol.getTypeId()));
		return getTypeTable().getSymbolById(classSymbol.getTypeId());
	}

	@Override
	public SymbolType visit(NewArray newArray,
			TypeCheckingVisitorContext context) {
		SymbolType sizeType = newArray.getSize().accept(this, context);
		checkTypeError(newArray, getPrimitiveType(PrimitiveSymbolTypes.INT),
				sizeType);
		SymbolType arrayType = getTypeTable().getSymbolById(
				getTypeTable().getSymbolTypeId(newArray.getType(),
						newArray.getType().getDimension() + 1));
		newArray.setSymbolType(arrayType);
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
		length.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.INT));
		return getPrimitiveType(PrimitiveSymbolTypes.INT);
	}

	@Override
	public SymbolType visit(MathBinaryOp binaryOp,
			TypeCheckingVisitorContext context) {
		SymbolType leftType = binaryOp.getFirstOperand().accept(this, context);
		SymbolType rightType = binaryOp.getSecondOperand()
				.accept(this, context);
		switch (binaryOp.getOperator()) {
		case DIVIDE:
		case MINUS:
		case MOD:
		case MULTIPLY:
			checkBinaryOp(binaryOp, context,
					getPrimitiveType(PrimitiveSymbolTypes.INT));
			binaryOp.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.INT));
			return getPrimitiveType(PrimitiveSymbolTypes.INT);
		case PLUS:
			if (typeComparer.isTypeLessThanOrEquals(leftType,
					getPrimitiveType(PrimitiveSymbolTypes.STRING))
					&& typeComparer.isTypeLessThanOrEquals(rightType,
							getPrimitiveType(PrimitiveSymbolTypes.STRING))) {
				binaryOp.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.STRING));
				return getPrimitiveType(PrimitiveSymbolTypes.STRING);
			}
			if (typeComparer.isTypeLessThanOrEquals(leftType,
					getPrimitiveType(PrimitiveSymbolTypes.INT))
					&& typeComparer.isTypeLessThanOrEquals(rightType,
							getPrimitiveType(PrimitiveSymbolTypes.INT))) {
				binaryOp.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.INT));
				return getPrimitiveType(PrimitiveSymbolTypes.INT);
			}
			errors.add(new SemanticError(
					"'+' operator can be used for either INT addition or STRING concatenation. Types received: "
							+ leftType + ", " + rightType, binaryOp.getLine()));
			binaryOp.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.NULL));
			return getPrimitiveType(PrimitiveSymbolTypes.NULL);
		case GT:
		case GTE:
		case LT:
		case LTE:
			checkBinaryOp(binaryOp, context,
					getPrimitiveType(PrimitiveSymbolTypes.INT));
			binaryOp.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
			return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
		case EQUAL:
		case NEQUAL:
			if (!(typeComparer.isTypeLessThanOrEquals(leftType, rightType) || typeComparer
					.isTypeLessThanOrEquals(rightType, leftType))) {
				errors.add(new SemanticError(
						"Can't check equality in non-matching types. Types: "
								+ leftType + ", " + rightType, binaryOp
								.getLine()));
			}
			binaryOp.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
			return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
		default:
			// Should never get here: binary op must be of above types.
			binaryOp.setSymbolType(getVoidType());
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
		binaryOp.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
		return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
	}

	@Override
	public SymbolType visit(MathUnaryOp unaryOp,
			TypeCheckingVisitorContext context) {
		if (unaryOp.getOperand() instanceof Literal
				&& unaryOp.getOperator() == UnaryOps.UMINUS) {
			// HACK: For the bounds checking.
			((Literal) unaryOp.getOperand()).setYourParentIsUMinusToTrue();
		}
		return checkUnaryOp(unaryOp, context,
				getPrimitiveType(PrimitiveSymbolTypes.INT));
	}

	@Override
	public SymbolType visit(LogicalUnaryOp unaryOp,
			TypeCheckingVisitorContext context) {
		return checkUnaryOp(unaryOp, context,
				getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
	}

	private SymbolType checkUnaryOp(UnaryOp unaryOp,
			TypeCheckingVisitorContext context, SymbolType expectedType) {
		SymbolType operandType = unaryOp.getOperand().accept(this, context);
		checkTypeError(unaryOp, expectedType, operandType);
		unaryOp.setSymbolType(expectedType);
		return expectedType;
	}

	@Override
	public SymbolType visit(Literal literal, TypeCheckingVisitorContext context) {
		switch (literal.getType()) {
		case TRUE:
		case FALSE:
			literal.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
			return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
		case INTEGER:
			doBoundsChecking(literal);
			literal.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.INT));
			return getPrimitiveType(PrimitiveSymbolTypes.INT);
		case NULL:
			literal.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.NULL));
			return getPrimitiveType(PrimitiveSymbolTypes.NULL);
		case STRING:
			literal.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.STRING));
			return getPrimitiveType(PrimitiveSymbolTypes.STRING);
		default:
			// Should never get here, a literal must have one of the above
			// values.
			literal.setSymbolType(getPrimitiveType(PrimitiveSymbolTypes.VOID));
			return getVoidType();
		}
	}

	private void doBoundsChecking(Literal literal) {
		if (literal.isParentUMinus() && literal.getValue().equals("2147483648")) {
			return; // It's in bounds.
		}
		try {
			Integer.valueOf((String) literal.getValue());
		} catch (NumberFormatException e) {
			errors.add(new SemanticError("Integer is out of bounds: "
					+ literal.getValue(), literal.getLine()));
		}
	}

	@Override
	public SymbolType visit(ExpressionBlock expressionBlock,
			TypeCheckingVisitorContext context) {
		expressionBlock.setSymbolType(expressionBlock.getExpression().accept(
				this, context));
		return expressionBlock.getSymbolType();
	}

}
