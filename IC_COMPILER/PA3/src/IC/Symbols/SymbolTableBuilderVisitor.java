package IC.Symbols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

	private String programName;
	private SymbolTypeTable typeTable;
	private static Logger logger = Logger
			.getLogger(SymbolTableBuilderVisitor.class.getName());

	public SymbolTableBuilderVisitor(String programName) {
		this.programName = programName;
		this.typeTable = new SymbolTypeTable(programName);
	}

	@Override
	public GlobalSymbolTable visit(Program program) {
		GlobalSymbolTable globalTable = new GlobalSymbolTable(programName,
				typeTable);
		program.setGlobalSymbolTable(globalTable);

		Map<String, ClassSymbolTable> symbolTableForClass = new HashMap<String, ClassSymbolTable>();

		for (ICClass clazz : program.getClasses()) {
			Symbol classSymbol = new Symbol(clazz.getName(), SymbolKind.CLASS,
					typeTable.getSymbolTypeId(clazz));
			globalTable.insert(classSymbol);

			ClassSymbolTable classTable = (ClassSymbolTable) clazz.accept(this);
			symbolTableForClass.put(clazz.getName(), classTable);
		}

		for (ICClass clazz : program.getClasses()) {
			ClassSymbolTable classTable = symbolTableForClass.get(clazz
					.getName());
			SymbolTable parentSymbolTable = clazz.hasSuperClass() ? symbolTableForClass
					.get(clazz.getSuperClassName()) : globalTable;
			parentSymbolTable.addChild(classTable);
			if (clazz.hasSuperClass()) {
				typeTable.setSuperForClass(clazz);
			}
		}

		return globalTable;
	}

	@Override
	public ClassSymbolTable visit(ICClass icClass) {
		ClassSymbolTable classTable = new ClassSymbolTable(icClass.getName(),
				typeTable);
		icClass.setClassSymbolTable(classTable);
		for (Field field : icClass.getFields()) {
			Symbol fieldSymbol = new Symbol(field.getName(), SymbolKind.FIELD,
					typeTable.getSymbolTypeId(field.getType()));
			classTable.insert(fieldSymbol);
		}

		for (Method method : icClass.getMethods()) {
			Symbol methodSymbol;
			if (method instanceof VirtualMethod) {
				methodSymbol = new Symbol(method.getName(),
						SymbolKind.VIRTUAL_METHOD,
						typeTable.getSymbolTypeId(method));
			} else { // method is a StaticMethod or a LibraryMethod)
				methodSymbol = new Symbol(method.getName(),
						SymbolKind.STATIC_METHOD,
						typeTable.getSymbolTypeId(method));
			}
			MethodSymbolTable methodTable = (MethodSymbolTable) method
					.accept(this);
			classTable.addChild(methodTable);
			classTable.insert(methodSymbol);
		}
		return classTable;
	}

	@Override
	public Object visit(Field field) {
		return null;
	}

	@Override
	public MethodSymbolTable visit(VirtualMethod method) {
		return buildMethodSymbolTable(method);
	}

	private MethodSymbolTable buildMethodSymbolTable(Method method) {
		MethodSymbolTable table = new MethodSymbolTable(method.getName(),
				typeTable);
		method.setMethodSymbolTable(table);
		for (Formal formal : method.getFormals()) {
			Symbol symbol = new Symbol(formal.getName(), SymbolKind.PARAMETER,
					typeTable.getSymbolTypeId(formal.getType()));
			table.insert(symbol);
		}
		getSymbolsAndChildTablesFromStatementList(table, method.getStatements());
		setParentNamesForChildren(table);
		return table;
	}

	private void setParentNamesForChildren(SymbolTable table) {
		for (SymbolTable child : table.children) {
			StatementBlockSymbolTable blockChild = (StatementBlockSymbolTable) child;
			blockChild.setParentName(table.name);
			setParentNamesForChildren(child);
		}
	}

	class SymbolOrTables {
		Symbol symbol;
		List<StatementBlockSymbolTable> tables = new ArrayList<StatementBlockSymbolTable>();

		public void addTo(SymbolTable symbolTable) {
			if (symbol != null) {
				symbolTable.insert(symbol);
			}
			for (StatementBlockSymbolTable table : tables) {
				symbolTable.addChild(table);
			}
		}

		public void addTableFrom(SymbolOrTables fromOperation) {
			if (fromOperation != null && fromOperation.tables.size() > 0) {
				tables.add(fromOperation.tables.get(0));
			}
		}
	}

	private void getSymbolsAndChildTablesFromStatementList(SymbolTable table,
			List<Statement> statements) {
		for (Statement statement : statements) {
			SymbolOrTables fromStatement = (SymbolOrTables) statement
					.accept(this);
			if (fromStatement == null) {
				continue;
			}
			fromStatement.addTo(table);
		}
	}

	@Override
	public MethodSymbolTable visit(StaticMethod method) {
		return buildMethodSymbolTable(method);
	}

	@Override
	public MethodSymbolTable visit(LibraryMethod method) {
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
	public SymbolOrTables visit(If ifStatement) {
		SymbolOrTables result = new SymbolOrTables();
		SymbolOrTables fromOperation = (SymbolOrTables) ifStatement
				.getOperation().accept(this);
		result.addTableFrom(fromOperation);
		if (ifStatement.hasElse()) {
			SymbolOrTables fromElseOperation = (SymbolOrTables) ifStatement
					.getElseOperation().accept(this);
			result.addTableFrom(fromElseOperation);
		}
		return result;
	}

	@Override
	public SymbolOrTables visit(While whileStatement) {
		return (SymbolOrTables) whileStatement.getOperation().accept(this);
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
	public SymbolOrTables visit(StatementsBlock statementsBlock) {
		SymbolOrTables result = new SymbolOrTables();
		StatementBlockSymbolTable blockTable = new StatementBlockSymbolTable(
				typeTable);
		statementsBlock.setBlockSymbolTable(blockTable);
		getSymbolsAndChildTablesFromStatementList(blockTable,
				statementsBlock.getStatements());
		result.tables.add(blockTable);
		return result;
	}

	@Override
	public SymbolOrTables visit(LocalVariable localVariable) {
		SymbolOrTables result = new SymbolOrTables();
		result.symbol = new Symbol(localVariable.getName(),
				SymbolKind.LOCAL_VARIABLE,
				typeTable.getSymbolTypeId(localVariable.getType()));
		return result;
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