package IC.LIR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import IC.AST.LocalVariable;
import IC.AST.VariableLocation;
import IC.Symbols.Symbol;
import IC.Symbols.SymbolKind;

public class TranslatorVisitorContext {

	Set<Integer> usedTemps = new HashSet<Integer>();
	Map<String, Integer> usedLabels = new HashMap<String, Integer>();
	public String currentInnermostLoopEndLabel;
	public String currentInnermostLoopTestLabel;

	public String getTempRegister() {
		int i = 0;
		while (usedTemps.contains(i)) {
			++i;
		}
		usedTemps.add(i);
		return "R" + i;
	}

	public void freeTempRegister(String register) {
		int num;
		try {
			num = Integer.valueOf(register.substring(1));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		if (!usedTemps.contains(num)) {
			printStackTrace("Compiler error: tried to free an unused register: "
					+ register);
		}
		usedTemps.remove(num);
	}

	private void printStackTrace(String message) {
		try {
			throw new Exception(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getLabel(String lbl) {
		int newId = 0;
		if (usedLabels.containsKey(lbl)) {
			newId = usedLabels.get(lbl) + 1;
		}
		usedLabels.put(lbl, newId);
		return "_" + lbl + "_label" + newId;
	}

	public void setInnermostLabels(String testLabel, String endLabel) {
		this.currentInnermostLoopTestLabel = testLabel;
		this.currentInnermostLoopEndLabel = endLabel;
	}

	int lastSymbolTranslatedIndex = 0;
	Map<Symbol, String> symbolTranslatedName = new HashMap<Symbol, String>();

	private String getSymbolTranslatedName(Symbol symbol) {
		if (symbol.getKind() == SymbolKind.PARAMETER) {
			return symbol.getName();
		}
		if (symbolTranslatedName.containsKey(symbol)) {
			return symbolTranslatedName.get(symbol);
		}
		String translated = "symbol" + lastSymbolTranslatedIndex + "_"
				+ symbol.getName();
		symbolTranslatedName.put(symbol, translated);
		lastSymbolTranslatedIndex++;
		return translated;
	}

	public String getVariableName(VariableLocation node) {
		if (node.getSymbol() == null) {
			printStackTrace("Var has no symbol: " + node.getName());
		}
		return getSymbolTranslatedName(node.getSymbol());
	}

	public String getVariableName(LocalVariable node) {
		if (node.getSymbol() == null) {
			printStackTrace("Var has no symbol: " + node.getName());
		}
		return getSymbolTranslatedName(node.getSymbol());
	}

}
