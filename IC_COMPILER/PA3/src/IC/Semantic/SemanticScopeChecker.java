package IC.Semantic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import IC.AST.*;
import IC.Parser.CourtesyErrorReporter;
import IC.Symbols.GlobalSymbolTable;
import IC.Symbols.MethodSymbolTable;
import IC.Symbols.Symbol;
import IC.Symbols.SymbolKind;
import IC.Symbols.SymbolTable;
import IC.Symbols.SymbolTableException;
import IC.Symbols.SymbolType;
import IC.Symbols.SymbolTypeTable;

public class SemanticScopeChecker implements Visitor {

	private Stack<SymbolTable> symScopeStack = new Stack<SymbolTable>();
	private List<SemanticError> errors = new ArrayList<SemanticError>();

	public SemanticScopeChecker() {
	}

	public List<SemanticError> getErrors() {
		return errors;
	}

	private boolean verifySymbolIsOfKind(ASTNode node, String name,
			SymbolKind... kinds) {
		Symbol symbol;
		try {
			symbol = getCurrentScope().lookup(name);
		} catch (SymbolTableException e) {
			errors.add(new SemanticError(e.getMessage(), node.getLine()));
			return false;
		}
		// HACK: Make sure that if that symbol that is sought for is a member
		// field/method,
		// and the current scope is a static scope, this will be identified
		// here.
		// BIG FIXME (maybe we won't do that): Instead of this hack, use
		// instance
		// scope and static scope as different symbol tables, as initially
		// discussed.
		if (currentScopeIsStaticAndSymbolIsVirtualMethodOrField(symbol)) {
			errors.add(new SemanticError(
					"Trying to reference a non-static class member.", node
							.getLine()));
			return false;
		}
		return verifySymbolIsOfKind(node, symbol, kinds);
	}

	private boolean verifySymbolInOtherScopeIsOfKind(String otherScopeName,
			String symbolName, ASTNode node, SymbolKind... kinds) {
		Symbol symbol;
		try {
			SymbolTable otherScope = getCurrentScope().lookupScope(
					otherScopeName);
			symbol = otherScope.lookup(symbolName);
		} catch (SymbolTableException e) {
			errors.add(new SemanticError(e.getMessage(), node.getLine()));
			return false;
		}
		return verifySymbolIsOfKind(node, symbol, kinds);
	}

	private boolean verifySymbolIsOfKind(ASTNode node, Symbol symbol,
			SymbolKind... kinds) {
		if (!Arrays.asList(kinds).contains(symbol.getKind())) {
			String kindsStr = CourtesyErrorReporter.joinStrings(Arrays
					.asList(kinds));
			errors.add(new SemanticError("Symbol is not of kind '" + kindsStr
					+ "'", node.getLine(), symbol.getName()));
			return false;
		}
		return true;
	}

	private SymbolTable getCurrentScope() {
		return symScopeStack.peek();
	}

	/*
	 * When visit fails return null otherwise return true (!= null)
	 */
	@Override
	public Object visit(Program program) {
		// recursive call to class
		symScopeStack.push(program.getGlobalSymbolTable());
		for (ICClass clazz : program.getClasses()) {
			clazz.accept(this);
		}
		return true;
	}

	@Override
	public Object visit(ICClass clazz) {
		symScopeStack.push(clazz.getClassSymbolTable());
		for (Method meth : clazz.getMethods()) {
			meth.accept(this);
		}
		for (Field fld : clazz.getFields()) {
			fld.accept(this);
		}
		symScopeStack.pop();
		return true;
	}

	@Override
	public Object visit(Field field) {
		field.getType().accept(this);
		return true;
	}

	@Override
	public Object visit(VirtualMethod method) {
		visitMethod(method);
		return true;
	}

	@Override
	public Object visit(StaticMethod method) {
		visitMethod(method);
		return true;
	}

	@Override
	public Object visit(LibraryMethod method) {
		visitMethod(method);
		return true;
	}

	private void visitMethod(Method method) {
		symScopeStack.push(method.getMethodSymbolTable());
		for (Formal foraml : method.getFormals()) {
			foraml.accept(this);
		}
		for (Statement stmnt : method.getStatements()) {
			stmnt.accept(this);
		}
		method.getType().accept(this);
		symScopeStack.pop();
	}

	@Override
	public Object visit(Formal formal) {
		formal.getType().accept(this);
		/*
		 * validation that formal.getType() can be sent to function should be
		 * done brfore
		 */
		return true;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// always defined
		return true;
	}

	@Override
	public Object visit(UserType type) {
		verifySymbolIsOfKind(type, type.getName(), SymbolKind.CLASS);
		return true;
	}

	@Override
	public Object visit(Assignment assignment) {
		assignment.getAssignment().accept(this);
		assignment.getVariable().accept(this);
		// FIXME: type check: validate that assignment type is compatible to
		// variable type
		return true;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.getCall().accept(this);
		return true;
	}

	@Override
	public Object visit(Return returnStatement) {
		if (returnStatement.hasValue()) {
			returnStatement.getValue().accept(this);
		}
		return true;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this);
		}
		return true;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);
		return true;
	}

	@Override
	public Object visit(Break breakStatement) {
		return true;
	}

	@Override
	public Object visit(Continue continueStatement) {
		return true;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		symScopeStack.push(statementsBlock.getStatementsBlockSymbolTable());
		for (Statement stmt : statementsBlock.getStatements()) {
			stmt.accept(this);
		}
		symScopeStack.pop();
		return true;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		localVariable.getType().accept(this);
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this);
		}
		return true;
	}

	@Override
	public Object visit(VariableLocation location) {
		if (location.isExternal()) {
			location.getLocation().accept(this);
		} else {
			verifySymbolIsOfKind(location, location.getName(),
					SymbolKind.LOCAL_VARIABLE, SymbolKind.PARAMETER,
					SymbolKind.FIELD);
		}

		return true;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.getIndex().accept(this);
		location.getArray().accept(this);
		return true;
	}

	@Override
	public Object visit(StaticCall call) {
		for (Expression arg : call.getArguments()) {
			arg.accept(this);
		}
		if (!verifySymbolIsOfKind(call, call.getClassName(), SymbolKind.CLASS)) {
			return true;
		}
		verifySymbolInOtherScopeIsOfKind(call.getClassName(), call.getName(),
				call, SymbolKind.STATIC_METHOD);
		return true;
	}

	@Override
	public Object visit(VirtualCall call) {
		for (Expression arg : call.getArguments()) {
			arg.accept(this);
		}
		if (call.isExternal()) {
			call.getLocation().accept(this);
			// FIXME:
			// 1. Get Class type of location (type checking should have this
			// info)
			// 2. Get symbol table for that class
			// 3. Verify that it has the method.

			// 2-3. can be done using:
			// verifySymbolExistsInOtherScope(call.getClassName(),
			// call.getName(), call);
		} else {
			// Not external: verify that there's a method in current scope.
			verifySymbolIsOfKind(call, call.getName(),
					SymbolKind.VIRTUAL_METHOD, SymbolKind.STATIC_METHOD);
		}

		return true;
	}

	@Override
	public Object visit(This thisExpression) {
		// Shouldn't be called
		return true;
	}

	@Override
	public Object visit(NewClass newClass) {
		verifySymbolIsOfKind(newClass, newClass.getName(), SymbolKind.CLASS);
		return true;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.getSize().accept(this);
		newArray.getType().accept(this);
		return true;
	}

	@Override
	public Object visit(Length length) {
		length.getArray().accept(this);
		return true;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return true;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		// FIXME: need to validate that these are a part can be up casted to the
		// specific operation types
		binaryOp.getFirstOperand();
		binaryOp.getSecondOperand();
		return true;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.getOperand().accept(this);
		// FIXME: need to validate that these are a part can be up casted to the
		// specific operation types
		unaryOp.getOperand();
		return true;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.getOperand().accept(this);
		// FIXME: need to validate that these are a part can be up casted to the
		// specific operation types
		unaryOp.getOperand();
		return true;
	}

	@Override
	public Object visit(Literal literal) {
		return true;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		expressionBlock.getExpression().accept(this);
		return true;
	}

	/**
	 * Part of the hack described above. This checks if the symbol that was
	 * currently found is in the instance-scope of a class, while the call has
	 * been made from the static-scope.
	 */
	private boolean currentScopeIsStaticAndSymbolIsVirtualMethodOrField(
			Symbol symbol) {
		Symbol scopeSymbol;
		try {
			scopeSymbol = findClosestEnclosingMethodScope();
		} catch (SymbolTableException e) {
			return false;
		}
		boolean currentScopeIsStatic = scopeSymbol != null
				&& scopeSymbol.getKind() == SymbolKind.STATIC_METHOD;
		boolean symbolIsNonStaticClassMember = symbol.getKind() == SymbolKind.FIELD
				|| symbol.getKind() == SymbolKind.VIRTUAL_METHOD;
		return currentScopeIsStatic && symbolIsNonStaticClassMember;
	}

	private Symbol findClosestEnclosingMethodScope()
			throws SymbolTableException {
		SymbolTable scope = getCurrentScope();
		while (scope != null && !(scope instanceof MethodSymbolTable)) {
			scope = scope.getParent();
		}
		if (scope == null) {
			return null;
		}
		return getScopeSymbolInEnclosingScope(scope);
	}

	private Symbol getScopeSymbolInEnclosingScope(SymbolTable scope)
			throws SymbolTableException {
		return getCurrentScope().getParent().lookup(scope.getName());
	}

}
