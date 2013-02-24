package IC.SymbolTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.PrimitiveType;
import IC.AST.Type;
import IC.SymbolTypes.PrimitiveSymbolType.PrimitiveSymbolTypes;

public class SymbolTypeTable {
	String programName;
	List<SymbolType> symbolTypes = new ArrayList<SymbolType>();
	Map<SymbolType, Integer> symbolTypesIds = new HashMap<SymbolType, Integer>();

	public SymbolTypeTable(String programName) {
		this.programName = programName;
		addPrimitiveTypes();
	}

	private void addPrimitiveTypes() {
		for (PrimitiveSymbolTypes type : PrimitiveSymbolTypes.values()) {
			addOrGetSymbolType(new PrimitiveSymbolType(type));
		}
	}

	// ///////////////
	// public methods: get SymbolType instances

	public SymbolType getSymbolById(int id) {
		return symbolTypes.get(id - 1);
	}

	// ///////////////
	// public methods: create SymbolType instances.

	public int getSymbolTypeId(Type type, int dimension) {
		return addOrGetSymbolTypeId(createSymbolType(type, dimension));
	}

	public int getSymbolTypeId(boolean isStatic, Method method) {
		return addOrGetSymbolTypeId(createSymbolType(isStatic, method));
	}

	public int getSymbolTypeId(ICClass clazz) {
		return addOrGetSymbolTypeId(createSymbolType(clazz));
	}

	// ////////////////
	// Private methods

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

	private SymbolType createSymbolType(boolean isStatic, Method method) {
		// Create types for formals
		List<SymbolType> formalsTypes = new ArrayList<SymbolType>();
		for (Formal formal : method.getFormals()) {
			formalsTypes.add(getSymbolById(getSymbolTypeId(formal.getType(),
					formal.getType().getDimension())));
		}
		// Create type for return type
		SymbolType returnType = getSymbolById(getSymbolTypeId(method.getType(),
				method.getType().getDimension()));
		// Create type for method
		return new MethodSymbolType(isStatic, formalsTypes, returnType, method);
	}

	private SymbolType createSymbolType(Type type, int dimension) {
		// Create the basic type, and wrap it in arrays if needed
		SymbolType basicType = addOrGetSymbolType(astTypeToSymbolType(type));
		return createSymbolType(basicType, dimension);
	}

	private SymbolType createSymbolType(SymbolType basicType, int dimension) {
		// An array T[][] is created with a base type T[].
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
			sb.append(type.additionalStringData());
			sb.append("\n");
		}
		return sb.toString();
	}

	public void setSuperForClass(ICClass clazz) {
		if (clazz.hasSuperClass()) {
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
