package it.frunzioluigi.metricsCalculator.utils;

import com.google.common.collect.Sets;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Map;
import java.util.Set;

public class VSM {
	public static double calculate(Map<String, Double> v1, Map<String, Double> v2) {
		Set<String> both = Sets.newHashSet(v1.keySet());
		both.addAll(v2.keySet());

		double[] d1 = new double[both.size()];
		double[] d2 = new double[both.size()];

		int i = 0;
		for (String key : both) {
			d1[i] = 0;
			d2[i] = 0;

			if (v1.containsKey(key))
				d1[i] = v1.get(key);

			if (v2.containsKey(key))
				d2[i] = v2.get(key);

			i++;
		}

		RealVector vector1 = new ArrayRealVector(d1);
		RealVector vector2 = new ArrayRealVector(d2);

		try {
			return vector1.cosine(vector2);
		} catch (MathArithmeticException e) {
			return Double.NaN;
		}
	}
}
