package IC.LIR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import IC.BinaryOps;
import IC.LiteralTypes;
import IC.AST.ASTNode;
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
import IC.AST.Location;
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
import IC.SymbolTypes.PrimitiveSymbolType;
import IC.SymbolTypes.SymbolType;
import IC.SymbolTypes.PrimitiveSymbolType.PrimitiveSymbolTypes;
import IC.Symbols.ClassSymbolTable;
import IC.Symbols.Symbol;
import IC.Symbols.SymbolKind;
import IC.Symbols.SymbolTable;
import IC.Symbols.SymbolTableException;

public class TranslatorVisitor implements
		PropagatingVisitor<TranslatorVisitorContext, LIRCode> {

	List<String> stringLiterals = new ArrayList<String>();
	Map<String, List<String>> dispatchTables = new HashMap<String, List<String>>();
	Map<String, ClassLayout> classLayouts = new HashMap<String, ClassLayout>();

	@Override
	public LIRCode visit(Program program, TranslatorVisitorContext context) {
		LIRCode allCode = new VoidLIRCode();
		generateDispatchVectors(program, allCode);
		allCode.addLabel("");
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

	private void generateDispatchVectors(Program program, LIRCode allCode) {
		for (ICClass clazz : program.getClasses()) {
			if (clazz.getName().equals("Library")) {
				continue;
			}
			if (clazz.hasSuperClass()) {
				ClassLayout superClassLayout = classLayouts.get(clazz
						.getSuperClassName());
				classLayouts.put(clazz.getName(), new ClassLayout(clazz,
						superClassLayout));
			} else {
				classLayouts.put(clazz.getName(), new ClassLayout(clazz));
			}
			allCode.addDispatchVector(clazz, classLayouts.get(clazz.getName())
					.getDispatchVector());
		}
	}

	private void p(String str) {
		System.out.println(str);
	}

	@Override
	public LIRCode visit(ICClass icClass, TranslatorVisitorContext context) {
		LIRCode classCode = new VoidLIRCode();
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

	private void printMethodHeader(ICClass icClass, LIRCode classCode,
			Method method) {
		if (!(method instanceof LibraryMethod)) {
			classCode.addEmptyLine();
			classCode.addEmptyLine();
			classCode.addMethodComment("Method: " + method.getName()
					+ "() in class: " + icClass.getName());
		}
	}

	@Override
	public LIRCode visit(Field field, TranslatorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LIRCode visit(VirtualMethod method, TranslatorVisitorContext context) {
		return visitMethod(method, context);
	}

	@Override
	public LIRCode visit(StaticMethod method, TranslatorVisitorContext context) {
		return visitMethod(method, context);
	}

	private LIRCode visitMethod(Method method, TranslatorVisitorContext context) {
		LIRCode code = new VoidLIRCode();
		String className = getClassName(method);
		// Generate label
		if (method.isMain()) {
			code.addLabel("_ic_main:");
		} else {
			// Something like: _Class3_method1
			code.addLabel(String.format("_%s_%s:", className, method.getName()));
		}
		for (Formal formal : method.getFormals()) {
			formal.accept(this, context);
		}
		// Generate statements code
		for (Statement sttmt : method.getStatements()) {
			LIRCode stmtCode = sttmt.accept(this, context);
			code.addCommandsFromOtherLr(stmtCode, sttmt);
		}
		// TODO: Only add this if there isn't a return already.
		if (method.isMain()) {
			code.addCommand("Library", "__exit(0)", "Rdummy", "Exit from main");
		} else {
			code.addJumpCommand("Return", "9999");
		}
		return code;
	}

	@Override
	public LIRCode visit(LibraryMethod method, TranslatorVisitorContext context) {
		// Do nothing; we get a provided library implementation
		return new VoidLIRCode();
	}

	@Override
	public LIRCode visit(Formal formal, TranslatorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LIRCode visit(PrimitiveType type, TranslatorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LIRCode visit(UserType type, TranslatorVisitorContext context) {
		// Do nothing
		return null;
	}

	@Override
	public LIRCode visit(Assignment assignment, TranslatorVisitorContext context) {
		// Generate assignment code
		LIRCode sttmtCode = new VoidLIRCode();

		LIRCode assignmentValue = assignment.getAssignment().accept(this,
				context);
		sttmtCode.addCommandsFromOtherLr(assignmentValue,
				assignment.getAssignment());

		// TODO: Decipher where to put the value
		Location variable = assignment.getVariable();
		if (variable instanceof ArrayLocation) {
			return assignToArrayLocation(sttmtCode,
					assignmentValue.getTargetRegister(),
					(ArrayLocation) variable, context);
		} else if (variable instanceof VariableLocation) {
			return assignToVariableLocation(sttmtCode,
					assignmentValue.getTargetRegister(),
					(VariableLocation) variable, context);
		}
		System.out
				.println("Compiler error. GetDestination: Not supposed to get here.");
		return null;

		// sttmtCode.addCommand("Move", assignmentValue.getResultRegister(),
		// assignment.getVariable();
	}

	private LIRCode assignToArrayLocation(LIRCode sttmtCode,
			String valueRegister, ArrayLocation variable,
			TranslatorVisitorContext context) {
		LIRCode array = variable.getArray().accept(this, context);
		LIRCode index = variable.getIndex().accept(this, context);
		String destination = array.getTargetRegister() + "["
				+ index.getTargetRegister() + "]";
		sttmtCode.addCommandsFromOtherLr(array, variable.getArray());
		sttmtCode.addCommandsFromOtherLr(index, variable.getIndex());
		sttmtCode.addCommand("MoveArray", valueRegister, destination,
				"Assign to array");
		context.freeTempRegister(valueRegister);
		context.freeTempRegister(array.getTargetRegister());
		context.freeTempRegister(index.getTargetRegister());
		return sttmtCode;
	}

	private LIRCode assignToVariableLocation(LIRCode sttmtCode,
			String valueRegister, VariableLocation variable,
			TranslatorVisitorContext context) {
		String classRegister;
		String className;
		if (!variable.isExternal()) {
			if (!isField(variable)) {
				sttmtCode.addCommand(
						"Move",
						valueRegister,
						context.getVariableName(variable),
						"Assignment to local variable (was "
								+ variable.getName() + ")");
				context.freeTempRegister(valueRegister);
				return sttmtCode;
			}
			classRegister = context.getTempRegister();
			sttmtCode.addCommand("Move", "this", classRegister,
					"implicit this: assigning to current class member");
			className = getEnclosingClassName(variable);
		} else {
			LIRCode locationCode = variable.getLocation().accept(this, context);
			sttmtCode.addCommandsFromOtherLr(locationCode,
					variable.getLocation());
			classRegister = locationCode.getTargetRegister();
			SymbolType locationType = variable.getLocation().getSymbolType();
			className = ((ClassSymbolType) locationType).getName();
		}
		sttmtCode.addCommand(
				"MoveField",
				valueRegister,
				classRegister + "."
						+ getFieldOffset(className, variable.getName()),
				String.format("Assign to field %s.%s", className,
						variable.getName()));
		context.freeTempRegister(valueRegister);
		context.freeTempRegister(classRegister);
		return sttmtCode;
	}

	private String getEnclosingClassName(ASTNode node) {
		String className;
		SymbolTable scope = node.getEnclosingScope();
		while (!(scope instanceof ClassSymbolTable)) {
			scope = scope.getParent();
		}
		className = scope.getName();
		return className;
	}

	private boolean isField(VariableLocation variable) {
		boolean isField;
		try {
			isField = variable.getEnclosingScope().lookup(variable.getName())
					.getKind() == SymbolKind.FIELD;
		} catch (SymbolTableException e) {
			e.printStackTrace();
			isField = false;
			System.out
					.println("Compiler error: Not supposed to get here. Local variable is not in scope.");
		}
		return isField;
	}

	@Override
	public LIRCode visit(CallStatement callStatement,
			TranslatorVisitorContext context) {
		// Generate call code, do nothing with result
		LIRCode code = callStatement.getCall().accept(this, context);
		if (code.getTargetRegister() != null) {
			freeTempRegister(context, code);
		}
		return code;
	}

	@Override
	public LIRCode visit(Return returnStatement,
			TranslatorVisitorContext context) {
		// Generate return code
		if (returnStatement.hasValue()) {
			LIRCode value = returnStatement.getValue().accept(this, context);
			value.addJumpCommand("Return", value.getTargetRegister());
			freeTempRegister(context, value);
			return value;
		} else {
			LIRCode code = new VoidLIRCode();
			if (inMain(returnStatement)) {
				code.addCommand("Library", "__exit(0)", "Rdummy",
						"Exit from main");
			} else {
				code.addJumpCommand("Return", "9999");
			}
			return code;
		}
	}

	private boolean inMain(ASTNode node) {
		// HACK: Check by searching up the symbol-table-tree
		SymbolTable scope = node.getEnclosingScope();
		while (scope != null) {
			if (scope.getName().equals("main")) {
				return true;
			}
			scope = scope.getParent();
		}
		return false;
	}

	@Override
	public LIRCode visit(If ifStatement, TranslatorVisitorContext context) {
		// Generate conditional code
		LIRCode ifCode = new VoidLIRCode();
		LIRCode conditionCode = ifStatement.getCondition()
				.accept(this, context);
		String endLabel = context.getLabel("if_end");

		ifCode.addComment("Starting if code. if:");
		ifCode.addCommandsFromOtherLr(conditionCode, ifStatement.getCondition());
		ifCode.addCommand("Compare", "0", conditionCode.getTargetRegister(),
				"if condition");
		freeTempRegister(context, conditionCode);

		LIRCode then = ifStatement.getOperation().accept(this, context);

		if (ifStatement.hasElse()) {
			String falseLabel = context.getLabel("if_false");
			ifCode.addJumpCommand("JumpTrue", falseLabel);
			ifCode.addComment("... then:");
			ifCode.addCommandsFromOtherLr(then, ifStatement.getOperation());

			ifCode.addJumpCommand("Jump", endLabel);
			ifCode.addLabel(falseLabel + ":");

			LIRCode elseOp = ifStatement.getElseOperation().accept(this,
					context);
			ifCode.addComment("... else:");
			ifCode.addCommandsFromOtherLr(elseOp,
					ifStatement.getElseOperation());
		} else {
			ifCode.addJumpCommand("JumpTrue", endLabel);
			ifCode.addComment("... then:");
			ifCode.addCommandsFromOtherLr(then, ifStatement.getOperation());
		}
		ifCode.addComment("... endif.");
		ifCode.addLabel(endLabel + ":");
		return ifCode;
	}

	@Override
	public LIRCode visit(While whileStatement, TranslatorVisitorContext context) {
		String testLabel = context.getLabel("while_test");
		String endLabel = context.getLabel("while_end");

		String lastTestLabel = context.currentInnermostLoopTestLabel;
		String lastEndLabel = context.currentInnermostLoopEndLabel;

		context.setInnermostLabels(testLabel, endLabel);

		LIRCode whileCode = new VoidLIRCode();
		whileCode.addComment("While. Condition:");
		whileCode.addLabel(testLabel + ":");
		LIRCode condition = whileStatement.getCondition().accept(this, context);
		whileCode.addCommandsFromOtherLr(condition,
				whileStatement.getCondition());
		whileCode.addCommand("Compare", "0", condition.getTargetRegister(),
				"while condition");
		freeTempRegister(context, condition);

		whileCode.addJumpCommand("JumpTrue", endLabel);

		whileCode.addComment("While. Do:");
		LIRCode loopBody = whileStatement.getOperation().accept(this, context);
		whileCode.addCommandsFromOtherLr(loopBody,
				whileStatement.getOperation());

		whileCode.addJumpCommand("Jump", testLabel);
		whileCode.addComment("End while");
		whileCode.addLabel(endLabel + ":");

		context.setInnermostLabels(lastTestLabel, lastEndLabel);
		return whileCode;
	}

	private void freeTempRegister(TranslatorVisitorContext context, LIRCode code) {
		if (code == null || code instanceof VoidLIRCode) {
			return;
		}
		context.freeTempRegister(code.getTargetRegister());
	}

	@Override
	public LIRCode visit(Break breakStatement, TranslatorVisitorContext context) {
		// Generate code: goto out of context.currentInnermostLoop;
		LIRCode code = new VoidLIRCode();
		code.addCommandFormat("Jump %s  # break",
				context.currentInnermostLoopEndLabel);
		return code;
	}

	@Override
	public LIRCode visit(Continue continueStatement,
			TranslatorVisitorContext context) {
		// Generate code: goto test label of inner most loop, saved in context
		LIRCode code = new VoidLIRCode();
		code.addCommandFormat("Jump %s  # continue",
				context.currentInnermostLoopTestLabel);
		return code;
	}

	@Override
	public LIRCode visit(StatementsBlock statementsBlock,
			TranslatorVisitorContext context) {
		LIRCode lr = new VoidLIRCode();
		for (Statement sttmt : statementsBlock.getStatements()) {
			LIRCode sttmtCode = sttmt.accept(this, context);
			lr.addCommandsFromOtherLr(sttmtCode, sttmt);
		}
		return lr;
	}

	@Override
	public LIRCode visit(LocalVariable localVariable,
			TranslatorVisitorContext context) {
		if (localVariable.hasInitValue()) {
			LIRCode code = new VoidLIRCode();
			LIRCode initCode = localVariable.getInitValue().accept(this,
					context);
			code.addCommandsFromOtherLr(initCode, localVariable.getInitValue());
			code.addCommand("Move", initCode.getTargetRegister(),
					context.getVariableName(localVariable),
					"Init local variable (was " + localVariable.getName() + ")");
			freeTempRegister(context, initCode);
			return code;
		}
		// A declaration without an initialization? Boring.
		return new VoidLIRCode();
	}

	@Override
	public LIRCode visit(VariableLocation location,
			TranslatorVisitorContext context) {
		LIRCode code = newResultLR(context);
		LIRCode externalLocation;
		String className;
		if (!location.isExternal()) {
			if (!isField(location)) {
				// x --> Move x, Rn
				String comment = String.format("%s was %s (%s)",
						context.getVariableName(location), location.getName(),
						location.getSymbol().getKind());
				code.addCommand("Move", context.getVariableName(location),
						code.getTargetRegister(), comment);
				return code;
			}
			externalLocation = newResultLR(context);
			externalLocation.addCommand("Move", "this",
					externalLocation.getTargetRegister(),
					"Getting ready to access 'this' class member");
			code.addCommandsFromOtherLr(externalLocation, location);
			className = getEnclosingClassName(location);
		} else {
			// Generate code to calc external location
			externalLocation = location.getLocation().accept(this, context);
			code.addCommandsFromOtherLr(externalLocation,
					location.getLocation());
			// TODO: generate run time checks that externalLocation is not null
			// __checkNullRef(o)
			SymbolType locationType = location.getLocation().getSymbolType();
			className = ((ClassSymbolType) locationType).getName();
		}
		code.addCommandFormat(
				"MoveField %s.%s, %s  # Reading from field %s.%s",
				externalLocation.getTargetRegister(),
				String.valueOf(getFieldOffset(className, location.getName())),
				code.getTargetRegister(), className, location.getName());
		freeTempRegister(context, externalLocation);
		return code;
	}

	private int getFieldOffset(String className, String name) {
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
		return classLayouts.get(className).getFieldOffset(name);
	}

	private void printErrorMessage(String errorMessage) {
		try {
			throw new Exception(errorMessage);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public LIRCode visit(ArrayLocation location,
			TranslatorVisitorContext context) {
		LIRCode code = newResultLR(context);
		LIRCode array = location.getArray().accept(this, context);
		code.addCommandsFromOtherLr(array, location.getArray());
		// TODO: Generate run-time checks that array is not null
		// checkNullRef(a)

		LIRCode index = location.getIndex().accept(this, context);
		code.addCommandsFromOtherLr(index, location.getIndex());
		// TODO: Generate run-time checks that index is in bounds
		// checkArrayAccess(a,i)
		code.addCommandFormat("MoveArray %s[%s], %s  # %s",
				array.getTargetRegister(), index.getTargetRegister(),
				code.getTargetRegister(), "Reading from array");
		freeTempRegister(context, index);
		freeTempRegister(context, array);
		return code;
	}

	@Override
	public LIRCode visit(StaticCall call, TranslatorVisitorContext context) {
		Method callee = getCalledMethod(call);
		String className = call.getClassName();
		String methodName = call.getName();
		List<Expression> arguments = call.getArguments();
		return getStaticCallCode(context, callee, className, methodName,
				arguments);
	}

	private LIRCode getStaticCallCode(TranslatorVisitorContext context,
			Method callee, String className, String methodName,
			List<Expression> arguments) {
		LIRCode callCode;
		String targetRegister;
		if (isVoid(callee)) {
			callCode = new VoidLIRCode();
			targetRegister = "Rdummy";
		} else {
			callCode = newResultLR(context);
			targetRegister = callCode.getTargetRegister();
		}
		if (className.equals("Library")) {
			List<String> args = addArgumentsCode(arguments, callee, context,
					callCode, false);
			callCode.addCommandFormat("Library __%s(%s), %s", methodName,
					CourtesyErrorReporter.joinStrings(args, ", "),
					targetRegister);
			for (String arg : args) {
				context.freeTempRegister(arg);
			}
		} else {
			List<String> args = addArgumentsCode(arguments, callee, context,
					callCode, true);
			callCode.addCommandFormat("StaticCall %s_%s(%s), %s", className,
					methodName, CourtesyErrorReporter.joinStrings(args, ", "),
					targetRegister);
			for (String arg : args) {
				context.freeTempRegister(arg.substring(arg.indexOf("=") + 1));
			}
		}
		return callCode;
	}

	private boolean isVoid(Method method) {
		return ((MethodSymbolType) method.getSymbolType()).getReturnType()
				.equals(new PrimitiveSymbolType(PrimitiveSymbolTypes.VOID));
	}

	private List<String> addArgumentsCode(List<Expression> arguments,
			Method callee, TranslatorVisitorContext context, LIRCode callCode,
			boolean includeFormalNames) {
		List<String> argRegisters = new ArrayList<String>();
		for (Expression arg : arguments) {
			LIRCode argCode = arg.accept(this, context);
			callCode.addCommandsFromOtherLr(argCode, arg);
			argRegisters.add(argCode.getTargetRegister());
		}
		List<String> args = new ArrayList<String>();
		for (int i = 0; i < callee.getFormals().size(); ++i) {
			if (includeFormalNames) {
				args.add(callee.getFormals().get(i).getName() + "="
						+ argRegisters.get(i));
			} else {
				args.add(argRegisters.get(i));
			}
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
	public LIRCode visit(VirtualCall call, TranslatorVisitorContext context) {
		Method callee = getCalledMethod(call);
		if (!call.isExternal() && isStaticMethod(call)) {
			return getStaticCallCode(context, callee, getClassName(callee),
					call.getName(), call.getArguments());
		}
		LIRCode callCode;
		String targetRegister;
		if (isVoid(callee)) {
			callCode = new VoidLIRCode();
			targetRegister = "Rdummy";
		} else {
			callCode = newResultLR(context);
			targetRegister = callCode.getTargetRegister();
		}
		List<String> args = addArgumentsCode(call.getArguments(), callee,
				context, callCode, true);
		String locationRegister;
		if (call.isExternal()) {
			LIRCode externalLocation = call.getLocation().accept(this, context);
			callCode.addCommandsFromOtherLr(externalLocation,
					call.getLocation());
			locationRegister = externalLocation.getTargetRegister();
			// TODO: Generate run time checks that
			// externalLocation.getResultRegister() is not null
			// __checkNullRef(o)

		} else {
			// Implicit 'this'
			locationRegister = context.getTempRegister();
			callCode.addCommand("Move", "this", locationRegister,
					"implicit this");
		}
		int methodIndex = getMethodIndex(callee);
		callCode.addCommandFormat(
				"VirtualCall %s.%s(%s), %s # Calling virtual method %s.%s()",
				locationRegister, String.valueOf(methodIndex),
				CourtesyErrorReporter.joinStrings(args, ", "), targetRegister,
				getClassName(callee), callee.getName());
		context.freeTempRegister(locationRegister);
		for (String arg : args) {
			context.freeTempRegister(arg.substring(arg.indexOf("=") + 1));
		}
		return callCode;
	}

	private String getClassName(Method callee) {
		return callee.getEnclosingScope().getParent().getName();
	}

	private int getMethodIndex(Method callee) {
		String className = getClassName(callee);
		if (!classLayouts.containsKey(className)) {
			printErrorMessage("Compiler unexpected error: no layout for class "
					+ className + ".");
		}
		ClassLayout classLayout = classLayouts.get(className);
		if (!classLayout.methodToOffset.containsKey(callee)) {
			printErrorMessage("Compiler unexpected error: no offset for method.");
		}
		return classLayout.methodToOffset.get(callee);
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
			try {
				return (Method) call.getEnclosingScope()
						.lookupScope(call.getName()).getReleventAstNode();
				// System.out.println(mst + " " + mst.getMethod().getName() +
				// " "
				// + call.getName());
				// return mst.getMethod();
			} catch (SymbolTableException e) {
				// Not supposed to get here.
				p("getCalledMethod: Not supposed to get here");
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public LIRCode visit(This thisExpression, TranslatorVisitorContext context) {
		LIRCode thisCode = newResultLR(context);
		thisCode.addCommand("Move", "this", thisCode.getTargetRegister(),
				"explicit this");
		return thisCode;
	}

	@Override
	public LIRCode visit(NewClass newClass, TranslatorVisitorContext context) {
		ClassLayout layout = classLayouts.get(newClass.getName());
		int classSize = (layout.getNumField() + 1) * 4;
		// Generate code (should know the class size)

		LIRCode code = newResultLR(context);
		code.addCommandFormat("Library __allocateObject(%s), %s",
				String.valueOf(classSize), code.getTargetRegister());
		code.addCommandFormat("MoveField _DV_%s, %s.0", newClass.getName(),
				code.getTargetRegister());
		return code;
	}

	@Override
	public LIRCode visit(NewArray newArray, TranslatorVisitorContext context) {
		LIRCode code = newResultLR(context);

		ArraySymbolType arrayType = (ArraySymbolType) newArray.getSymbolType();

		LIRCode size = newArray.getSize().accept(this, context);
		code.addCommandsFromOtherLr(size, newArray.getSize());
		// TODO: Generate run-time checks that this size is > 0
		code.addCommand("Mul", "4", size.getTargetRegister(),
				"Array size is num_items * 4");
		code.addCommandFormat("Library __allocateArray(%s), %s",
				size.getTargetRegister(), code.getTargetRegister());
		freeTempRegister(context, size);
		// Generate code
		return code;
	}

	@Override
	public LIRCode visit(Length length, TranslatorVisitorContext context) {
		LIRCode lengthCode = newResultLR(context);

		LIRCode arr = length.getArray().accept(this, context);
		lengthCode.addCommandsFromOtherLr(arr, length.getArray());
		// TODO: Generate run-time checks that array is not null
		// __checkNullRef(a)
		lengthCode.addCommand("ArrayLength", arr.getTargetRegister(),
				lengthCode.getTargetRegister(), "Array length");
		freeTempRegister(context, arr);

		return lengthCode;
	}

	private ResultLIRCode newResultLR(TranslatorVisitorContext context) {
		return new ResultLIRCode(context.getTempRegister());
	}

	@Override
	public LIRCode visit(MathBinaryOp binaryOp, TranslatorVisitorContext context) {
		boolean isCalculation = isCalculationOp(binaryOp.getOperator());

		LIRCode op1 = binaryOp.getFirstOperand().accept(this, context);
		LIRCode op2 = binaryOp.getSecondOperand().accept(this, context);

		String op1ResultRegister = op1.getTargetRegister();
		String op2ResultRegister = op2.getTargetRegister();

		if (isCalculation && op2.wasImmediateOrMemory()) {
			op2ResultRegister = op2.getImmediateOrMemory();
			freeTempRegister(context, op2);
		}

		String target = isCalculation ? op1ResultRegister : context
				.getTempRegister();

		LIRCode binopCode = new ResultLIRCode(target);
		binopCode.addComment("Starting binop code: " + binaryOp.getOperator());
		binopCode.addComment("operand 1: ");
		binopCode.addCommandsFromOtherLr(op1, binaryOp.getFirstOperand());
		if (!(isCalculation && op2.wasImmediateOrMemory())) {
			binopCode.addComment("operand 2: ");
			binopCode.addCommandsFromOtherLr(op2, binaryOp.getSecondOperand());
		}
		if (isCalculation) {
			return addCalcCode(binopCode, op1ResultRegister, op2ResultRegister,
					binaryOp, context, op2.wasImmediateOrMemory());
		} else {
			return addComparisonCode(binopCode, op1ResultRegister,
					op2ResultRegister, binaryOp, context);
		}

	}

	private boolean isCalculationOp(BinaryOps op) {
		boolean isCalculation = false;
		switch (op) {
		case DIVIDE:
		case MINUS:
		case MOD:
		case MULTIPLY:
		case PLUS:
			isCalculation = true;
			break;
		case EQUAL:
		case GT:
		case GTE:
		case LT:
		case LTE:
		case NEQUAL:
			isCalculation = false;
			break;
		default:
			System.out
					.println("Compiler error: not supposed to get here (LIRGenerator, visit Math Binop).");
			isCalculation = false;
			;
		}
		return isCalculation;
	}

	private LIRCode addCalcCode(LIRCode binopCode, String op1ResultRegister,
			String op2ResultRegister, MathBinaryOp binaryOp,
			TranslatorVisitorContext context, boolean op2IsImmediate) {
		binopCode.addCommand(opToCommand(binaryOp.getOperator()),
				op2ResultRegister, op1ResultRegister, "Math operator");
		if (!op2IsImmediate) {
			context.freeTempRegister(op2ResultRegister);
		}
		return binopCode;
	}

	private LIRCode addComparisonCode(LIRCode binopCode,
			String op1ResultRegister, String op2ResultRegister,
			MathBinaryOp binaryOp, TranslatorVisitorContext context) {
		String target = binopCode.getTargetRegister();
		binopCode.addCommand("Move", "1", target, "Comparison default");
		binopCode.addCommand("Compare", op1ResultRegister, op2ResultRegister,
				"Comparison");
		String jumpCommand = opToCommand(binaryOp.getOperator());
		String jumpLabel = context.getLabel("comparison_binop");
		binopCode.addJumpCommand(jumpCommand, jumpLabel);
		binopCode.addCommand("Move", "0", target, "Comparison not true");
		binopCode.addLabel(jumpLabel + ":");
		binopCode.addComment("End of binop code: " + binaryOp.getOperator());
		context.freeTempRegister(op1ResultRegister);
		context.freeTempRegister(op2ResultRegister);

		return binopCode;
	}

	private String opToCommand(BinaryOps operator) {
		switch (operator) {
		case DIVIDE:
			return "Div";
		case MINUS:
			return "Sub";
		case MOD:
			return "Mod";
		case MULTIPLY:
			return "Mul";
		case PLUS:
			return "Add";
		case EQUAL:
			return "JumpTrue";
		case NEQUAL:
			return "JumpFalse";
		case GT:
			return "JumpL";
		case GTE:
			return "JumpLE";
		case LT:
			return "JumpG";
		case LTE:
			return "JumpGE";
		default:
			break;
		}
		return null;
	}

	@Override
	public LIRCode visit(LogicalBinaryOp binaryOp,
			TranslatorVisitorContext context) {
		LIRCode op1 = binaryOp.getFirstOperand().accept(this, context);
		LIRCode binopCode = new ResultLIRCode(op1.getTargetRegister());
		binopCode.addComment("Starting logical binop code: "
				+ binaryOp.getOperator());
		binopCode.addComment("Logical operand 1: ");
		binopCode.addCommandsFromOtherLr(op1, binaryOp.getFirstOperand());
		boolean isAnd = binaryOp.getOperator() == BinaryOps.LAND;
		binopCode.addCommand("Compare", (isAnd ? "0" : "1"),
				binopCode.getTargetRegister(), "In Logical "
						+ (isAnd ? "and" : "or"));
		String lbl = context.getLabel(isAnd ? "and_end" : "or_end");
		binopCode.addJumpCommand("JumpTrue", lbl);
		LIRCode op2 = binaryOp.getSecondOperand().accept(this, context);
		binopCode.addComment("Logical operand 2: ");
		binopCode.addCommandsFromOtherLr(op2, binaryOp.getSecondOperand());
		binopCode.addCommand(isAnd ? "And" : "Or", op1.getTargetRegister(),
				op2.getTargetRegister(), "In If, Logical "
						+ (isAnd ? "and" : "or"));
		binopCode.addLabel(lbl + ":");
		binopCode.addComment("End of logical binop code"
				+ binaryOp.getOperator());
		freeTempRegister(context, op2);
		return binopCode;
	}

	@Override
	public LIRCode visit(MathUnaryOp unaryOp, TranslatorVisitorContext context) {
		LIRCode operand = unaryOp.getOperand().accept(this, context);
		LIRCode result = new ResultLIRCode(operand.getTargetRegister());
		result.addCommandsFromOtherLr(operand, unaryOp.getOperand());
		result.addJumpCommand("Neg", result.getTargetRegister());
		// Generate code that calls

		return result;
	}

	@Override
	public LIRCode visit(LogicalUnaryOp unaryOp,
			TranslatorVisitorContext context) {
		LIRCode operand = unaryOp.getOperand().accept(this, context);
		LIRCode result = newResultLR(context);
		result.addCommandsFromOtherLr(operand, unaryOp.getOperand());
		result.addCommand("Move", "1", result.getTargetRegister(),
				"Unary negation");
		result.addCommand("Sub", operand.getTargetRegister(),
				result.getTargetRegister(), "Unary negation");
		freeTempRegister(context, operand);
		return result;
	}

	@Override
	public LIRCode visit(Literal literal, TranslatorVisitorContext context) {
		LIRCode codeResult = newResultLR(context);
		switch (literal.getType()) {
		case FALSE:
			codeResult.addCommand("Move", "0", codeResult.getTargetRegister(),
					"False literal");
			codeResult.setImmediateOrMemory("0");
			break;
		case INTEGER:
			codeResult.addCommand("Move", String.valueOf(literal.getValue()),
					codeResult.getTargetRegister(), "Integer literal");
			codeResult.setImmediateOrMemory(String.valueOf(literal.getValue()));
			break;
		case NULL:
			codeResult.addCommand("Move", "0", codeResult.getTargetRegister(),
					"Null literal");
			codeResult.setImmediateOrMemory("0");
			break;
		case STRING:
			String val = (String) literal.getQuotedString();
			int indexOf = this.stringLiterals.indexOf(val);
			if (indexOf == -1) {
				this.stringLiterals.add(val);
				indexOf = this.stringLiterals.size() - 1;
			}
			codeResult.addCommand("Move", "str" + (indexOf + 1),
					codeResult.getTargetRegister(), "String literal: " + val);
			break;
		case TRUE:
			codeResult.addCommand("Move", "1", codeResult.getTargetRegister(),
					"True literal");
			codeResult.setImmediateOrMemory("1");
			break;
		default:
			break;
		}

		return codeResult;
	}

	@Override
	public LIRCode visit(ExpressionBlock expressionBlock,
			TranslatorVisitorContext context) {
		return expressionBlock.getExpression().accept(this, context);
	}

}
