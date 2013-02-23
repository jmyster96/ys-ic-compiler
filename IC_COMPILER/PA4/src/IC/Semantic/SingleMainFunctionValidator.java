package IC.Semantic;

import java.util.ArrayList;
import java.util.List;

import IC.DataTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
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
import IC.AST.Return;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;
import IC.Parser.CourtesyErrorReporter;

public class SingleMainFunctionValidator implements Visitor {

	@Override
	public SemanticError visit(Program program) {
		List<String> classesWithMain = new ArrayList<String>();
		int lineNumberWithSecondMain = -1;
		for (ICClass clazz : program.getClasses()) {
			int lineNumberOfClassMainMethod = (Integer) clazz.accept(this);
			if (lineNumberOfClassMainMethod >= 0) {
				classesWithMain.add("class " + clazz.getName());
				lineNumberWithSecondMain = lineNumberOfClassMainMethod;
			}
		}
		if (classesWithMain.size() > 1) {
			return new SemanticError(
					"More than one class has 'main' function: "
							+ CourtesyErrorReporter
									.joinStrings(classesWithMain),
					lineNumberWithSecondMain);
		}
		if (classesWithMain.size() == 0) {
			return new SemanticError(
					"Main function wasn't found in any of the classes.",
					program.getLine());
		}
		return null;
	}

	@Override
	public Integer visit(ICClass icClass) {
		for (Method method : icClass.getMethods()) {
			int lineNumberOfMain = (Integer) method.accept(this);
			if (lineNumberOfMain >= 0) {
				return lineNumberOfMain;
			}
		}
		return -1;
	}

	@Override
	public Object visit(Field field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer visit(VirtualMethod method) {
		return -1;
	}

	@Override
	public Integer visit(StaticMethod method) {
		if (!method.getName().equals("main")) {
			return -1;
		}
		if (!(method.getType() instanceof PrimitiveType)) {
			return -1;
		}
		PrimitiveType returnType = (PrimitiveType) method.getType();
		if (returnType.getDataType() != DataTypes.VOID) {
			return -1;
		}
		if (method.getFormals().size() != 1
				|| !(method.getFormals().get(0).getType() instanceof PrimitiveType)) {
			return -1;
		}
		PrimitiveType formalType = (PrimitiveType) method.getFormals().get(0)
				.getType();
		if (formalType.getDataType() != DataTypes.STRING
				|| formalType.getDimension() != 1) {
			return -1;
		}
		return method.getLine();
	}

	@Override
	public Integer visit(LibraryMethod method) {
		return -1;
	}

	@Override
	public Object visit(Formal formal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		return null;
	}

}
