package IC.Symbols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import sun.util.logging.resources.logging;

import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.PrimitiveType;
import IC.AST.Type;
import IC.AST.UserType;
import IC.Semantic.SemanticError;
import IC.Symbols.PrimitiveSymbolType.PrimitiveSymbolTypes;

import java_cup.symbol;

public class SymbolTypeTable {
	private static final PrimitiveSymbolType NULL_TYPE = new PrimitiveSymbolType(PrimitiveSymbolTypes.NULL);
	String programName;
	List<SymbolType> symbolTypes = new ArrayList<SymbolType>();
	Map<SymbolType, Integer> symbolTypesIds = new HashMap<SymbolType, Integer>();
	private static Logger logger = Logger.getLogger(SymbolTypeTable.class
			.getName());

	public SymbolTypeTable(String programName) {
		this.programName = programName;
		addPrimitiveTypes();
	}

	private void addPrimitiveTypes() {
		for (PrimitiveSymbolTypes type : PrimitiveSymbolTypes.values()) {
			addOrGetSymbolType(new PrimitiveSymbolType(type));
		}
	}

	public SymbolType getSymbolById(int id) {
		return symbolTypes.get(id - 1);
	}

	public int getSymbolTypeId(Type type, int dimension) {
		return addOrGetSymbolTypeId(createSymbolType(type, dimension));
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
		if (symbolTypesIds.containsKey(symbolType)) {
			return symbolTypesIds.get(symbolType);
		}
		symbolTypes.add(symbolType);
		symbolTypesIds.put(symbolType, symbolTypes.size());
		return symbolTypes.size();
	}

	private SymbolType createSymbolType(Method method) {
		List<SymbolType> formalsTypes = new ArrayList<SymbolType>();
		for (Formal formal : method.getFormals()) {
			formalsTypes.add(getSymbolById(getSymbolTypeId(formal.getType(),
					formal.getType().getDimension())));
		}
		SymbolType returnType = getSymbolById(getSymbolTypeId(method.getType(),
				method.getType().getDimension()));
		return new MethodSymbolType(formalsTypes, returnType);
	}

	private SymbolType createSymbolType(Type type, int dimension) {
		SymbolType basicType = addOrGetSymbolType(astTypeToSymbolType(type));
		return createSymbolType(basicType, dimension);
	}

	private SymbolType createSymbolType(SymbolType basicType, int dimension) {
		if (dimension > 0) {
			return addOrGetSymbolType(new ArraySymbolType(createSymbolType(
					basicType, dimension - 1)));
		}
		return addOrGetSymbolType(basicType);
	}

	private SymbolType astTypeToSymbolType(Type type) {
		SymbolType basicType;
		if (type instanceof PrimitiveType) {
			basicType = new PrimitiveSymbolType((PrimitiveType) type);
		} else {
			basicType = new ClassSymbolType(type.getName());
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
		List<SymbolType> sorted = new ArrayList<SymbolType>(symbolTypes);
		Collections.sort(sorted, new Comparator<SymbolType>() {
			@Override
			public int compare(SymbolType o1, SymbolType o2) {
				return o1.getDisplaySortIndex() - o2.getDisplaySortIndex();
			}
		});
		for (SymbolType type : sorted) {
			sb.append(addOrGetSymbolTypeId(type));
			sb.append(". ");
			sb.append(type.getHeader());
			sb.append(": ");
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

	public boolean isTypeLessThanOrEquals(SymbolType type1, SymbolType type2) {
		if (type1.equals(NULL_TYPE) && type2.isReferenceType()) {
			return true;
		}
		return type1.equals(type2);
	}

}
