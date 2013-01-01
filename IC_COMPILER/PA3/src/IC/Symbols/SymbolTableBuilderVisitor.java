package IC.Symbols;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.Type;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;

public class SymbolTableBuilderVisitor implements Visitor {

	@Override
	public Object visit(Program program) {
		GlobalSymbolTable globalTable = new GlobalSymbolTable();

		Map<String, StaticClassSymbolTable> symbolTableForClass = new HashMap<String, StaticClassSymbolTable>();

		for (ICClass clazz : program.getClasses()) {
			Symbol classSymbol = new Symbol(clazz.getName(), SymbolKind.CLASS,
					new ClassSymbolType(clazz.getName()));
			globalTable.insert(classSymbol);

			StaticClassSymbolTable classTable = (StaticClassSymbolTable) clazz
					.accept(this);
			symbolTableForClass.put(clazz.getName(), classTable);
		}

		for (ICClass clazz : program.getClasses()) {
			StaticClassSymbolTable classTable = symbolTableForClass.get(clazz
					.getName());
			SymbolTable parentSymbolTable = clazz.hasSuperClass() ? symbolTableForClass
					.get(clazz.getSuperClassName()) : globalTable;
			parentSymbolTable.addChild(classTable);
		}

		return globalTable;
	}

	@Override
	public Object visit(ICClass icClass) {
		StaticClassSymbolTable staticClassTable = new StaticClassSymbolTable();
		InstanceClassSymbolTable instanceClassTable = new InstanceClassSymbolTable();
		for (Field field : icClass.getFields()) {
			Symbol fieldSymbol = new Symbol(field.getName(), SymbolKind.FIELD,
					createSymbolType(field.getType()));
			instanceClassTable.insert(fieldSymbol);
		}

		for (Method method : icClass.getMethods()) {
			Symbol methodSymbol;
			SymbolTable parentSymbolTable;
			if (method instanceof VirtualMethod) {
				methodSymbol = new Symbol(method.getName(),
						SymbolKind.VIRTUAL_METHOD, createSymbolType(method));
				parentSymbolTable = instanceClassTable;
			} else { // method is a StaticMethod or a LibraryMethod)
				methodSymbol = new Symbol(method.getName(),
						SymbolKind.STATIC_METHOD, createSymbolType(method));
				parentSymbolTable = staticClassTable;
			}
			MethodSymbolTable methodTable = (MethodSymbolTable) method
					.accept(this);
			parentSymbolTable.addChild(methodTable);
			parentSymbolTable.insert(methodSymbol);
		}
		return staticClassTable;
	}

	private SymbolType createSymbolType(Method method) {
		return new MethodSymbolType(method);
	}

	private SymbolType createSymbolType(Type type) {
		if (type instanceof PrimitiveType) {
			return new PrimitiveSymbolType((PrimitiveType) type);
		} else {
			return new ClassSymbolType(type.getName());
		}
	}

	@Override
	public Object visit(Field field) {
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		return buildMethodSymbolTable(method);
	}

	private Object buildMethodSymbolTable(Method method) {
		MethodSymbolTable table = new MethodSymbolTable();
		for (Formal formal : method.getFormals()) {
			Symbol symbol = new Symbol(formal.getName(),
					SymbolKind.LOCAL_VARIABLE,
					createSymbolType(formal.getType()));
			table.insert(symbol);
		}
		getSymbolsAndChildTablesFromStatementList(table, method.getStatements());
		return table;
	}

	private void getSymbolsAndChildTablesFromStatementList(SymbolTable table,
			List<Statement> statements) {
		for (Statement statement : statements) {
			SymbolTable fromStatement = (SymbolTable) statement.accept(this);
			for (Symbol symbol : fromStatement.symbols.values()) {
				table.insert(symbol);
			}
			for (SymbolTable childTable : fromStatement.children) {
				table.addChild(childTable);
			}
		}
	}

	@Override
	public Object visit(StaticMethod method) {
		return buildMethodSymbolTable(method);
	}

	@Override
	public Object visit(LibraryMethod method) {
		return buildMethodSymbolTable(method);
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
		SymbolTable table = new StatementBlockSymbolTable();
		getSymbolsAndChildTablesFromStatementList(table,
				statementsBlock.getStatements());
		return table;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// Create a temporary anonymous SymbolTable implementation just for
		// transferring the symbols to the caller.
		SymbolTable table = new SymbolTable() {
		};
		table.insert(new Symbol(localVariable.getName(),
				SymbolKind.LOCAL_VARIABLE, createSymbolType(localVariable
						.getType())));
		return table;
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
