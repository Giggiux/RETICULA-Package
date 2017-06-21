package it.frunzioluigi.metricsCalculator.metrics;


import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.metric.Metric;
import it.frunzioluigi.metricsCalculator.utils.Utils;
import it.frunzioluigi.metricsCalculator.utils.VSM;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giggiux on 3/13/17.
 */
public class C3 extends ASTVisitor implements Metric {

	ArrayList<Map<String, Double>> methodMaps = new ArrayList<>();

	public boolean visit(MethodDeclaration node) {

		Block methodBody = node.getBody();

		if (methodBody != null) {
			Map<String, Double> occurrences;
			try {
				occurrences = Utils.stringToCountMap(methodBody.toString());
			} catch (Exception e) {
				e.printStackTrace();
				occurrences = new HashMap<>();
			}

			methodMaps.add(occurrences);
		}


		return true;
	}


	@Override
	public void execute(CompilationUnit compilationUnit, CKNumber ckNumber, CKReport ckReport) {
		compilationUnit.accept(this);

	}

	@Override
	public void setResult(CKNumber ckNumber) {
		double sum = 0.;

		for (int i = 0; i < methodMaps.size(); i++) {
			for (int j = i + 1; j < methodMaps.size(); j++) {
				sum += VSM.calculate(methodMaps.get(i), methodMaps.get(j));
			}
		}

		int size = methodMaps.size();

		double avg = sum / ((size * (size - 1)) / 2);

		int avgToInt = Utils.doubleToInt(avg);

		ckNumber.addSpecific("C3", avgToInt);
	}
}
