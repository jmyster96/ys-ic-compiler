package IC.Symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import sun.util.logging.resources.logging;

import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.PrimitiveType;
import IC.AST.Type;
import IC.AST.UserType;

import java_cup.symbol;

public class SymbolTypeTable {
	String programName;
	List<SymbolType> symbolTypes = new ArrayList<SymbolType>();
	private static Logger logger = Logger
			.getLogger(SymbolTypeTable.class.getName());

	public SymbolTypeTable(String programName) {
		this.programName = programName;
	}

	public SymbolType getSymbolById(int id) {
		return symbolTypes.get(id - 1);
	}

	public int getSymbolTypeId(Type type) {
		return addOrGetSymbolTypeId(createSymbolType(type));
	}

	public int getSymbolTypeId(Method method) {
		return addOrGetSymbolTypeId(createSymbolType(method));
	}

	public int getSymbolTypeId(ICClass clazz) {
		return addOrGetSymbolTypeId(createSymbolType(clazz));
	}

	private SymbolType createSymbolType(ICClass clazz) {
		return new ClassSymbolType(clazz.getName());
	}

	private int addOrGetSymbolTypeId(SymbolType symbolType) {
		if (symbolTypes.indexOf(symbolType) >= 0) {
			return symbolTypes.indexOf(symbolType) + 1;
		}
		symbolTypes.add(symbolType);
		return symbolTypes.size();
	}

	private SymbolType createSymbolType(Method method) {
		List<SymbolType> formalsTypes = new ArrayList<SymbolType>();
		for (Formal formal : method.getFormals()) {
			formalsTypes.add(getSymbolById(getSymbolTypeId(formal.getType())));
		}
		SymbolType returnType = getSymbolById(getSymbolTypeId(method.getType()));
		return new MethodSymbolType(formalsTypes, returnType);
	}

	private SymbolType createSymbolType(Type type) {
		SymbolType basicType;
		if (type instanceof PrimitiveType) {
			basicType = new PrimitiveSymbolType((PrimitiveType) type);
		} else {
			basicType = new ClassSymbolType(type.getName());
		}
		return wrapInArrayIfNeeded(type, basicType);
	}

	private SymbolType wrapInArrayIfNeeded(Type type, SymbolType basicType) {
		if (type.getDimension() > 0) {
			return new ArraySymbolType(addOrGetSymbolType(basicType),
					type.getDimension());
		}
		return basicType;
	}

	private SymbolType addOrGetSymbolType(SymbolType basicType) {
		return getSymbolById(addOrGetSymbolTypeId(basicType));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Type Table: ");
		sb.append(programName);
		sb.append("\n");
		int id = 1;
		for (SymbolType type : symbolTypes) {
			sb.append(id++);
			sb.append(". ");
			sb.append(type);
			sb.append("\n");
		}
		return sb.toString();
	}

	public void setSuperForClass(ICClass clazz) {
		if (clazz.hasSuperClass()) {
			logger.info(clazz.getName() + " has super.");
			ClassSymbolType classType = getClassSymbolByClassName(clazz
					.getName());
			classType.setBaseClassTypeId(getSymbolIdByClassName(clazz
					.getSuperClassName()));
		}
	}

	private int getSymbolIdByClassName(String className) {
		return addOrGetSymbolTypeId(new ClassSymbolType(className));
	}

	private ClassSymbolType getClassSymbolByClassName(String className) {
		return (ClassSymbolType) getSymbolById(getSymbolIdByClassName(className));
	}
}
