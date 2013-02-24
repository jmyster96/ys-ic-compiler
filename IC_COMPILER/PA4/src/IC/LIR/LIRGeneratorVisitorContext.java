package IC.LIR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import IC.AST.While;

public class LIRGeneratorVisitorContext {

	Set<Integer> usedTemps = new HashSet<Integer>();
	Map<String, Integer> usedLabels = new HashMap<String, Integer>();
	public String currentInnermostLoopEndLabel;
	public String currentInnermostLoopTestLabel;

	public String getTempRegister() {
		int i = 1;
		while (usedTemps.contains(i)) {
			++i;
		}
		usedTemps.add(i);
		return "R" + i;
	}

	public void freeTempRegister(String register) {
		int num = Integer.valueOf(register.substring(1));
		if (!usedTemps.contains(num)) {
			System.out
					.println("Compiler error: tried to free an unused register.");
		}
		usedTemps.remove(num);
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

}
