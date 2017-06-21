package it.frunzioluigi.metricsCalculator.metrics;

import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.metric.Metric;
import it.frunzioluigi.metricsCalculator.utils.Utils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.IOException;

import static raykernel.apps.readability.eval.Main.getReadability;

/**
 * Created by giggiux on 3/13/17.
 */
public class CR extends ASTVisitor implements Metric {

	private double methodsReadabilitySum = 0.;
	private int methodsCount = 0;

	private String fileText;

	public boolean visit(MethodDeclaration node) {

		methodsCount += 1;

		Block methodBody = node.getBody();

		String methodText = "";

		if (methodBody != null) methodText += methodBody.toString();

		methodText += "\n ###";

		methodsReadabilitySum += getReadability(methodText);

		return true;
	}

	@Override
	public void execute(CompilationUnit compilationUnit, CKNumber ckNumber, CKReport ckReport) {

		try {
			fileText = Utils.readWholeFile(ckNumber.getFile());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		compilationUnit.accept(this);

	}

	@Override
	public void setResult(CKNumber ckNumber) {
		ckNumber.addSpecific("CR", Utils.doubleToInt(methodsReadabilitySum / methodsCount));
	}
}
