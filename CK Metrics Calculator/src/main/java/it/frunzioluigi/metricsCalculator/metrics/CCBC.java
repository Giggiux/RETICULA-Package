package it.frunzioluigi.metricsCalculator.metrics;

import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import it.frunzioluigi.metricsCalculator.utils.Utils;
import it.frunzioluigi.metricsCalculator.utils.VSM;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giggiux on 3/13/17.
 */
public class CCBC {

	public void calculate(CKReport report) {
		Collection<CKNumber> allReports = report.all();

		calculate(allReports);
	}

	public void calculate(Collection<CKNumber> allReports) {

		Map<String, Map<String, Double>> classesWordsCount = new HashMap<>(allReports.size());

		for (CKNumber ck : allReports) {
			double sum = 0.;

			Map<String, Double> ck1Map = classesWordsCount.get(ck.getClassName());

			if (ck1Map == null) {
				ck1Map = wordsCount(ck.getFile());
				classesWordsCount.put(ck.getClassName(), ck1Map);
			}


			for (CKNumber ck2 : allReports) {
				if (ck.equals(ck2)) continue;

				Map<String, Double> ck2Map = classesWordsCount.get(ck2.getClassName());

				if (ck2Map == null) {
					ck2Map = wordsCount(ck2.getFile());
					classesWordsCount.put(ck2.getClassName(), ck2Map);
				}

				sum += VSM.calculate(ck1Map, ck2Map);

			}

			int size = allReports.size();

			double avg = sum / (size * (size - 1) / 2);

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
