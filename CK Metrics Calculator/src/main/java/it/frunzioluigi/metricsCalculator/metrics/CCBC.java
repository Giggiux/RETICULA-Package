package it.frunzioluigi.metricsCalculator.metrics;

import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import it.frunzioluigi.metricsCalculator.utils.Utils;
import it.frunzioluigi.metricsCalculator.utils.VSM;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by giggiux on 3/13/17.
 */
public class CCBC {

	public void calculate(CKReport report) {
		Collection<CKNumber> allReports = report.all();

		calculate(allReports);
	}

	public void calculate(Collection<CKNumber> allReports) {

		//TODO: Future work - add words count as metric, so not to recompute every time in plugin. Extends CKNumber for this.
		Map<String, Map<String, Double>> classesWordsCount = new HashMap<>(allReports.size());

		CKNumber[] cks = new CKNumber[allReports.size()];
		cks = allReports.toArray(cks);

		int cksLength = cks.length;

		double[] couplings = new double[cksLength];

		for (int i = 0; i < cksLength; i++) {
			CKNumber ck = cks[i];

			Map<String, Double> ck1Map = classesWordsCount.computeIfAbsent(ck.getClassName(), (String useless) -> {
				return wordsCount(ck.getFile());
			});

			for (int j = i + 1; j < cksLength; j++) {
				CKNumber ck2 = cks[j];
				Map<String, Double> ck2Map = classesWordsCount.computeIfAbsent(ck2.getClassName(), (String useless) -> {
					return wordsCount(ck2.getFile());
				});

				double vsm = VSM.calculate(ck1Map, ck2Map);

				couplings[i] += vsm;
				couplings[j] += vsm;
			}

			int size = allReports.size();
			double avg = couplings[i] / (size * (size - 1) / 2.);
			int avgToInt = Utils.doubleToInt(avg);
			ck.addSpecific("CCBC", avgToInt);
		}

	}

	public void calculate(Map<String, CKNumber> allReports, Map<String, CKNumber> report) {
		Map<String, Map<String, Double>> classesWordsCount = new HashMap<>(report.size());

		for (CKNumber ck : report.values()) {
			double sum = 0.;

			Map<String, Double> ck1Map = classesWordsCount.computeIfAbsent(ck.getClassName(), (String useless) -> {
				return wordsCount(ck.getFile());
			});

			for (CKNumber ck2 : allReports.values()) {
				if (ck.getClassName().equals(ck2.getClassName())) continue;

				Map<String, Double> ck2Map = classesWordsCount.computeIfAbsent(ck2.getClassName(), (String useless) -> {
					return wordsCount(ck2.getFile());
				});

				sum += VSM.calculate(ck1Map, ck2Map);
			}

			int size = allReports.size();
			double avg = sum / (size * (size - 1) / 2.);
			int avgToInt = Utils.doubleToInt(avg);
			ck.addSpecific("CCBC", avgToInt);
		}

	}

	private Map<String, Double> wordsCount(String file) {


		// ReadFile
		String classText = "";
		try {
			classText = Utils.readWholeFile(file);
			return Utils.stringToCountMap(classText);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new HashMap<>();


	}
}
