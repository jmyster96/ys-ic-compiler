package IC.Symbols;

import java.util.ArrayList;
import java.util.List;

import IC.AST.Formal;
import IC.AST.Method;
import IC.AST.Type;

public class MethodSymbolType extends SymbolType {
	List<Type> formalTypes = new ArrayList<Type>();
	Type returnType;

	public MethodSymbolType(Method method) {
		for (Formal formal : method.getFormals()) {
			formalTypes.add(formal.getType());
		}
		returnType = method.getType();
	}

}
