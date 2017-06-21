package it.frunzioluigi.metricsCalculator.metrics;

import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.metric.Metric;
import it.frunzioluigi.metricsCalculator.utils.Utils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;

/**
 * Created by giggiux on 3/13/17.
 */
public class CD extends ASTVisitor implements Metric {

	private int linesOfComment = 0;

	private String fileText;

	public boolean visit(Javadoc javadoc) {
		return calculateLinesOfCommentFromNode(javadoc);
	}

	//T wODO: Understand why don't find LineComment nodes
	public boolean visit(LineComment lineComment) {
		linesOfComment += 1;
		return true;
	}

	public boolean visit(BlockComment blockComment) {
		return calculateLinesOfCommentFromNode(blockComment);
	}

	private boolean calculateLinesOfCommentFromNode(Comment commentNode) {
		String comment = Utils.getCommentText(commentNode, fileText);
		linesOfComment += comment.split("[\n|\r]").length;
		return true;
	}

	@Override
	public void execute(CompilationUnit compilationUnit, CKNumber ckNumber, CKReport ckReport) {
		try {
			fileText = Utils.readWholeFile(ckNumber.getFile());
		} catch (IOException e) {
			return;
		}

		compilationUnit.accept(this);
	}

	@Override
	public void setResult(CKNumber ckNumber) {
		ckNumber.addSpecific("linesOfComment", linesOfComment);
		int loc = ckNumber.getLoc();

		double CD = (loc != 0) ? (double) linesOfComment / loc : 1;

		int cd = Utils.doubleToInt(CD);
		ckNumber.addSpecific("CD", cd);
	}
}

