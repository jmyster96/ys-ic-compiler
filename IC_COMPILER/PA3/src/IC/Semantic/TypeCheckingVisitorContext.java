package IC.Semantic;

import IC.Symbols.ClassSymbolType;
import IC.Symbols.MethodSymbolType;

public class TypeCheckingVisitorContext {
	ClassSymbolType currentClassSymbolType; // Used for 'this' type checking
	MethodSymbolType currentMethodSymbolType; // Used for 'return' type checking
}
