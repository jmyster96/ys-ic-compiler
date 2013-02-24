package IC.LIR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.BinaryOps;
import IC.LiteralTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.Call;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.Expression;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.PropagatingVisitor;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.While;
import IC.Parser.CourtesyErrorReporter;
import IC.SymbolTypes.ArraySymbolType;
import IC.SymbolTypes.ClassSymbolType;
import IC.SymbolTypes.MethodSymbolType;
import IC.SymbolTypes.SymbolType;
import IC.Symbols.ClassSymbolTable;
import IC.Symbols.Symbol;
import IC.Symbols.SymbolKind;
import IC.Symbols.SymbolTable;
import IC.Symbols.SymbolTableException;

public class LIRGeneratorVisitor implements
		PropagatingVisitor<LIRGeneratorVisitorContext, LR> {

	List<String> stringLiterals = new ArrayList<String>();
	Map<String, List<String>> dispatchTables = new HashMap<String, List<String>>();

	@Override
	public LR visit(Program program, LIRGeneratorVisitorContext context) {
		LR allCode = new VoidLR();
		for (ICClass clazz : program.getClasses()) {
			allCode.addCommandsFromOtherLr(clazz.accept(this, context), clazz);
		}
		int i = 0;
		for (String string : stringLiterals) {
			allCode.insertCommand(i, "str" + (i + 1) + ": " + string);
			i++;
		}
		allCode.insertCommand(i, "");
		return allCode;
	}

	private void p(String str) {
		System.out.println(str);
	}

	@Override
	public LR visit(ICClass icClass, LIRGeneratorVisitorContext context) {
		LR classCode = new VoidLR();
		for (Field field : icClass.getFields()) {
			field.accept(this, context);
		}
		for (Method method : icClass.getMethods()) {
			printMethodHeader(icClass, classCode, method);
			classCode.addCommandsFromOtherLr(method.accept(this, context),
					method);
		}
		return classCode;
	}

	private void printMethodHeader(ICClass icClass, LR classCode, Method method) {
		if (!(method instanceof LibraryMethod)) {
			classCode.addCommand("");
			classCode.addCommand("#");
			classCode.addCommand("# Method: " + method.getName()
					+ "() in class: " + icClass.getName());
			classCode.addCommand("# ------");
		}
	}

	@Override
	public LR visit(Field field, LIRGeneratorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LR visit(VirtualMethod method, LIRGeneratorVisitorContext context) {
		// TODO: Add to dispatch table
		return visitMethod(method, context);
	}

	@Override
	public LR visit(StaticMethod method, LIRGeneratorVisitorContext context) {
		return visitMethod(method, context);
	}

	private LR visitMethod(Method method, LIRGeneratorVisitorContext context) {
		LR code = new VoidLR();
		String className = method.getEnclosingScope().getParent().getName();
		// Generate label
		code.addCommandFormat("_%s_%s:", className, method.getName());
		for (Formal formal : method.getFormals()) {
			formal.accept(this, context);
		}
		// Generate statements code
		for (Statement sttmt : method.getStatements()) {
			LR stmtCode = sttmt.accept(this, context);
			code.addCommandsFromOtherLr(stmtCode, sttmt);
		}
		return code;
	}

	@Override
	public LR visit(LibraryMethod method, LIRGeneratorVisitorContext context) {
		// TODO: Nothing; we get a provided library implementation
		return new VoidLR();
	}

	@Override
	public LR visit(Formal formal, LIRGeneratorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LR visit(PrimitiveType type, LIRGeneratorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LR visit(UserType type, LIRGeneratorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LR visit(Assignment assignment, LIRGeneratorVisitorContext context) {
		// Generate assignment code
		LR assignmentValue = assignment.getAssignment().accept(this, context);
		LR sttmtCode = new VoidLR();
		sttmtCode.addCommandsFromOtherLr(assignmentValue,
				assignment.getAssignment());
		// TODO: Decipher where to put the value
		sttmtCode.addCommand("Mov",
				assignmentValue != null ? assignmentValue.getResultRegister()
						: "<missing assignment value>",
				"<missing variable destination>");
		// sttmtCode.addCommand("Mov", assignmentValue.getResultRegister(),
		// assignment.getVariable();
		return sttmtCode;
	}

	@Override
	public LR visit(CallStatement callStatement,
			LIRGeneratorVisitorContext context) {
		// Generate call code, do nothing with result
		return callStatement.getCall().accept(this, context);
	}

	@Override
	public LR visit(Return returnStatement, LIRGeneratorVisitorContext context) {
		// Generate return code
		if (returnStatement.hasValue()) {
			LR value = returnStatement.getValue().accept(this, context);
			value.addCommand("Return", value.getResultRegister());
			return value;
		} else {
			LR code = new VoidLR();
			code.addCommand("Return", "Rdummy");
			return code;
		}
	}

	@Override
	public LR visit(If ifStatement, LIRGeneratorVisitorContext context) {
		// Generate conditional code
		LR ifCode = new VoidLR();
		LR conditionCode = ifStatement.getCondition().accept(this, context);
		LR then = ifStatement.getOperation().accept(this, context);
		String endLabel = context.getLabel("end");
		ifCode.addCommandsFromOtherLr(conditionCode, ifStatement.getCondition());
		ifCode.addCommand("Compare", "0",
				ifCode != null ? ifCode.getResultRegister() : "--missing--");

		if (ifStatement.hasElse()) {
			String falseLabel = context.getLabel("false");
			ifCode.addCommand("JumpTrue", falseLabel);
			ifCode.addCommandsFromOtherLr(then, ifStatement.getOperation());
			ifCode.addCommand("Jump", endLabel);
			ifCode.addCommand(falseLabel + ":");
			LR elseOp = ifStatement.getElseOperation().accept(this, context);
			ifCode.addCommandsFromOtherLr(elseOp,
					ifStatement.getElseOperation());
		} else {
			ifCode.addCommand("JumpTrue", endLabel);
			ifCode.addCommandsFromOtherLr(then, ifStatement.getOperation());
		}

		ifCode.addCommand(endLabel + ":");
		return ifCode;
	}

	@Override
	public LR visit(While whileStatement, LIRGeneratorVisitorContext context) {
		String testLabel = context.getLabel("test");
		String endLabel = context.getLabel("end");

		String lastTestLabel = context.currentInnermostLoopTestLabel;
		String lastEndLabel = context.currentInnermostLoopEndLabel;

		context.setInnermostLabels(testLabel, endLabel);

		LR condition = whileStatement.getCondition().accept(this, context);
		LR loopBody = whileStatement.getOperation().accept(this, context);

		LR whileCode = new VoidLR();
		whileCode.addCommand(testLabel + ":");
		whileCode.addCommandsFromOtherLr(condition,
				whileStatement.getCondition());
		whileCode.addCommand("Compare", "0",
				condition != null ? condition.getResultRegister()
						: "Missing translation");
		whileCode.addCommand("JumpTrue", endLabel);
		freeResultRegister(context, condition);
		whileCode.addCommandsFromOtherLr(loopBody,
				whileStatement.getOperation());
		whileCode.addCommand("Jump", testLabel);
		whileCode.addCommand(endLabel + ":");

		context.setInnermostLabels(lastTestLabel, lastEndLabel);
		return whileCode;
	}

	private void freeResultRegister(LIRGeneratorVisitorContext context,
			LR condition) {
		if (condition == null) {
			return;
		}
		context.freeTempRegister(condition.getResultRegister());
	}

	@Override
	public LR visit(Break breakStatement, LIRGeneratorVisitorContext context) {
		// Generate code: goto out of context.currentInnermostLoop;
		LR code = new VoidLR();
		code.addCommandFormat("Jump %s \t\t\t # break",
				context.currentInnermostLoopEndLabel);
		return code;
	}

	@Override
	public LR visit(Continue continueStatement,
			LIRGeneratorVisitorContext context) {
		// Generate code: goto condition label of context.currentInnermostLoop;
		LR code = new VoidLR();
		code.addCommandFormat("Jump %s \t\t\t # continue",
				context.currentInnermostLoopTestLabel);
		return code;
	}

	@Override
	public LR visit(StatementsBlock statementsBlock,
			LIRGeneratorVisitorContext context) {
		LR lr = new VoidLR();
		for (Statement sttmt : statementsBlock.getStatements()) {
			LR sttmtCode = sttmt.accept(this, context);
			lr.addCommandsFromOtherLr(sttmtCode, sttmt);
		}
		return lr;
	}

	@Override
	public LR visit(LocalVariable localVariable,
			LIRGeneratorVisitorContext context) {
		if (localVariable.hasInitValue()) {
			LR initCode = localVariable.getInitValue().accept(this, context);
			LR code = new VoidLR();
			code.addCommandsFromOtherLr(initCode, localVariable.getInitValue());
			code.addCommand("Mov",
					initCode != null ? initCode.getResultRegister()
							: "--missing init code register--", localVariable
							.getName(), "Init local variable");
			return code;
		}
		return new VoidLR();
	}

	@Override
	public LR visit(VariableLocation location,
			LIRGeneratorVisitorContext context) {
		if (location.isExternal()) {
			LR code = new ResultLR(context.getTempRegister());
			// Generate code to calc external location
			LR externalLocation = location.getLocation().accept(this, context);
			code.addCommandsFromOtherLr(externalLocation,
					location.getLocation());
			// TODO: generate run time checks that externalLocation is not null
			// __checkNullRef(o)
			code.addCommandFormat("MoveField %s.%s, %s",
					externalLocation.getResultRegister(),
					getFieldNumber(location.getLocation(), location.getName()),
					code.getResultRegister());
			return code;
		} else {
			// x --> Mov x, Rn
			LR code = new ResultLR(context.getTempRegister());
			code.addCommand("Mov", location.getName(), code.getResultRegister());
			return code;
		}
	}

	private String getFieldNumber(Expression location, String name) {
		// For example:
		// class A {
		// int a;
		// static void func() {
		// A obj = new A();
		// obj.a = 5;
		// ^^^^^
		//
		// 'obj' is the location, 'a' is the name.
		// Need to figure out that 'a' is the first field in 'obj', which is
		// an instance of A.
		SymbolType locationType = location.getSymbolType();
		try {
			ClassSymbolTable referencedClassScope = (ClassSymbolTable) location
					.getEnclosingScope().lookupScope(locationType.getHeader());
			return String.valueOf(referencedClassScope.getFieldIndex(name));

		} catch (SymbolTableException e) {
			e.printStackTrace();
			System.out
					.println("Compiler unexpected error: a scope is supposed to be present.");
		}
		return "0";
	}

	@Override
	public LR visit(ArrayLocation location, LIRGeneratorVisitorContext context) {

		LR index = location.getIndex().accept(this, context);
		// TODO: Generate run-time checks that index is in bounds
		// checkArrayAccess(a,i)
		LR array = location.getArray().accept(this, context);
		// TODO: Generate run-time checks that array is not null
		// checkNullRef(a)
		LR code = new ResultLR(context.getTempRegister());
		String arrayResultRegister = array != null ? array.getResultRegister()
				: "-- missing array location --";
		String indexResultRegister = index != null ? index.getResultRegister()
				: "-- missing index --";
		code.addCommandsFromOtherLr(index, location.getIndex());
		code.addCommandsFromOtherLr(array, location.getArray());
		code.addCommandFormat("MoveArray %s[%s], %s", arrayResultRegister,
				indexResultRegister, code.getResultRegister(),
				"In array location");
		freeResultRegister(context, index);
		freeResultRegister(context, array);
		return code;
	}

	@Override
	public LR visit(StaticCall call, LIRGeneratorVisitorContext context) {
		Method callee = getCalledMethod(call);
		String className = call.getClassName();
		String methodName = call.getName();
		List<Expression> arguments = call.getArguments();
		return getStaticCallCode(context, callee, className, methodName,
				arguments);
	}

	private LR getStaticCallCode(LIRGeneratorVisitorContext context,
			Method callee, String className, String methodName,
			List<Expression> arguments) {
		// TODO: Do a library call in case call.getName().equals("Library")
		LR callCode = new ResultLR(context.getTempRegister());
		String targetRegister = callCode.getResultRegister();
		List<String> args = addArgumentsCode(arguments, callee, context,
				callCode);
		callCode.addCommandFormat("StaticCall %s_%s(%s), %s", className,
				methodName, CourtesyErrorReporter.joinStrings(args, ", "),
				targetRegister);
		return callCode;
	}

	private List<String> addArgumentsCode(List<Expression> arguments,
			Method callee, LIRGeneratorVisitorContext context, LR callCode) {
		List<String> argRegisters = new ArrayList<String>();
		for (Expression arg : arguments) {
			LR argCode = arg.accept(this, context);
			callCode.addCommandsFromOtherLr(argCode, arg);
			argRegisters.add(argCode != null ? argCode.getResultRegister()
					: "--missing--");
		}
		List<String> args = new ArrayList<String>();
		for (int i = 0; i < callee.getFormals().size(); ++i) {
			args.add(callee.getFormals().get(i).getName() + "="
					+ argRegisters.get(i));
		}
		return args;
	}

	private Method getCalledMethod(StaticCall call) {
		String scopeName = call.getClassName();
		try {
			SymbolTable scope = call.getEnclosingScope().lookupScope(scopeName);
			SymbolTable methodScope = scope.lookupScope(call.getName());
			return (Method) methodScope.getReleventAstNode();
		} catch (SymbolTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out
					.println("If we got here then scope checking done bad work.");
			return null;
		}
	}

	@Override
	public LR visit(VirtualCall call, LIRGeneratorVisitorContext context) {
		Method callee = getCalledMethod(call);
		if (!call.isExternal() && isStaticMethod(call)) {
			return getStaticCallCode(context, callee, callee
					.getEnclosingScope().getParent().getName(), call.getName(),
					call.getArguments());
		}
		LR callCode = new ResultLR(context.getTempRegister());
		List<String> args = addArgumentsCode(call.getArguments(), callee,
				context, callCode);
		String locationRegister;
		if (call.isExternal()) {
			LR externalLocation = call.getLocation().accept(this, context);
			locationRegister = externalLocation != null ? externalLocation
					.getResultRegister() : "--missing external location--";
			// TODO: Generate run time checks that
			// externalLocation.getResultRegister() is not null
			// __checkNullRef(o)
		} else {
			// TODO: Fix this to the address of current object:
			locationRegister = "Rthis";
		}
		int methodIndex = getCalledMethodIndex(callee);
		callCode.addCommandFormat("VirtualCall %s.%s(%s), %s",
				locationRegister, String.valueOf(methodIndex),
				CourtesyErrorReporter.joinStrings(args, ", "),
				callCode.getResultRegister());
		return callCode;
	}

	private boolean isStaticMethod(VirtualCall call) {
		try {
			return call.getEnclosingScope().lookup(call.getName()).getKind() == SymbolKind.STATIC_METHOD;
		} catch (SymbolTableException e) {
			// Not supposed to get here.
			p("isStaticMethod: Not supposed to get here");
			e.printStackTrace();
			return false;
		}
	}

	private Method getCalledMethod(VirtualCall call) {
		if (call.isExternal()) {
			ClassSymbolType mst = (ClassSymbolType) call.getLocation()
					.getSymbolType();
			try {
				return (Method) call.getEnclosingScope()
						.lookupScope(mst.getName()).lookupScope(call.getName())
						.getReleventAstNode();
			} catch (SymbolTableException e) {
				// Not supposed to get here.
				p("getCalledMethod: Not supposed to get here (call is external)");
				e.printStackTrace();
				return null;
			}
		} else {
			MethodSymbolType mst;
			try {
				mst = (MethodSymbolType) call
						.getEnclosingScope()
						.getTypeTable()
						.getSymbolById(
								call.getEnclosingScope().lookup(call.getName())
										.getTypeId());
				return mst.getMethod();
			} catch (SymbolTableException e) {
				// Not supposed to get here.
				p("getCalledMethod: Not supposed to get here");
				e.printStackTrace();
				return null;
			}
		}
	}

	private int getCalledMethodIndex(Method method) {
		ICClass clazz = (ICClass) method.getEnclosingScope().getParent()
				.getReleventAstNode();
		int i = 0;
		for (Method methodInClass : clazz.getMethods()) {
			if (methodInClass.getName().equals(method.getName())) {
				return i;
			}
			i++;
		}
		return 0;
	}

	@Override
	public LR visit(This thisExpression, LIRGeneratorVisitorContext context) {

		return null;
	}

	@Override
	public LR visit(NewClass newClass, LIRGeneratorVisitorContext context) {
		// Generate code (should know the class size)
		return null;
	}

	@Override
	public LR visit(NewArray newArray, LIRGeneratorVisitorContext context) {
		// Generate run-time checks that this size is > 0
		LR size = newArray.getSize().accept(this, context);
		ArraySymbolType arrayType = (ArraySymbolType) newArray.getSymbolType();
		// To compute the actual allocated size:
		SymbolType actualType = arrayType.getBaseType();
		// Generate code
		return null;
	}

	@Override
	public LR visit(Length length, LIRGeneratorVisitorContext context) {
		LR arr = length.getArray().accept(this, context);
		// Generate run-time checks that array is not null
		// __checkNullRef(a)
		return null;
	}

	@Override
	public LR visit(MathBinaryOp binaryOp, LIRGeneratorVisitorContext context) {
		LR op1 = binaryOp.getFirstOperand().accept(this, context);
		LR op2 = binaryOp.getSecondOperand().accept(this, context);
		String command = opToCommand(binaryOp.getOperator());
		return null;
	}

	private String opToCommand(BinaryOps operator) {
		switch (operator) {
		case DIVIDE:
			return "Div";
		case GT:
			break;
		case GTE:
			break;
		case LAND:
			break;
		case LOR:
			break;
		case LT:
			break;
		case LTE:
			break;
		case MINUS:
			break;
		case MOD:
			break;
		case MULTIPLY:
			break;
		case NEQUAL:
			break;
		case PLUS:
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public LR visit(LogicalBinaryOp binaryOp, LIRGeneratorVisitorContext context) {
		LR op1 = binaryOp.getFirstOperand().accept(this, context);
		LR lr = new ResultLR(op1.getResultRegister());
		lr.addCommandsFromOtherLr(op1, binaryOp.getFirstOperand());
		boolean isAnd = binaryOp.getOperator() == BinaryOps.LAND;
		lr.addCommand("Compare", isAnd ? "0" : "1", lr.getResultRegister(),
				"In If, Logical " + (isAnd ? "and" : "or"));
		String lbl = context.getLabel("or_end");
		lr.addCommand("JumpTrue", lbl);
		LR op2 = binaryOp.getSecondOperand().accept(this, context);
		lr.addCommandsFromOtherLr(op2, binaryOp.getSecondOperand());
		lr.addCommand(isAnd ? "And" : "Or", op1.getResultRegister(),
				op2.getResultRegister(), "In If, Logical "
						+ (isAnd ? "and" : "or"));
		lr.addCommand(lbl + ":");
		freeResultRegister(context, op2);
		return lr;
	}

	@Override
	public LR visit(MathUnaryOp unaryOp, LIRGeneratorVisitorContext context) {
		LR operand = unaryOp.getOperand().accept(this, context);
		// Generate code that cals

		return null;
	}

	@Override
	public LR visit(LogicalUnaryOp unaryOp, LIRGeneratorVisitorContext context) {
		LR operand = unaryOp.getOperand().accept(this, context);
		return null;
	}

	@Override
	public LR visit(Literal literal, LIRGeneratorVisitorContext context) {
		LR codeResult = new ResultLR(context.getTempRegister());
		switch (literal.getType()) {
		case FALSE:
			codeResult.addCommand("Mov", "0", codeResult.getResultRegister(),
					"False literal");
			break;
		case INTEGER:
			codeResult.addCommand("Mov", String.valueOf(literal.getValue()),
					codeResult.getResultRegister(), "Integer literal");
			break;
		case NULL:
			codeResult.addCommand("Mov", "0", codeResult.getResultRegister(),
					"Null literal");
			break;
		case STRING:
			this.stringLiterals.add((String) literal.getQuotedString());
			codeResult.addCommand("Mov", "str" + this.stringLiterals.size(),
					codeResult.getResultRegister(), "String literal");
			break;
		case TRUE:
			codeResult.addCommand("Mov", "1", codeResult.getResultRegister(),
					"True literal");
			break;
		default:
			break;
		}

		return codeResult;
	}

	@Override
	public LR visit(ExpressionBlock expressionBlock,
			LIRGeneratorVisitorContext context) {
		LR expr = expressionBlock.getExpression().accept(this, context);
		return expr;
	}

}
