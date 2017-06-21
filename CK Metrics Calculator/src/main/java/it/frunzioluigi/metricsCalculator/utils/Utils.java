package it.frunzioluigi.metricsCalculator.utils;


import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.eclipse.jdt.core.dom.Comment;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by giggiux on 3/14/17.
 */
public class Utils {


	final static List<String> javaStopWords = new ArrayList<>();

	{
		try {
			String file = readWholeFile("resources/java-stop-words.txt");
			javaStopWords.addAll(0, Arrays.asList(file.split("[\n|\r]")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static Map<String, Double> stringToCountMap(String textFile) throws Exception {
		CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();

		stopWords.addAll(javaStopWords);

		textFile = String.join(" ", textFile.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));

		textFile = textFile.replaceAll("\\.|_|-", " ");

		StandardTokenizer tokenStream = new StandardTokenizer();
		PorterStemmer stemmer = new PorterStemmer();

		tokenStream.setReader(new StringReader(textFile.trim()));

		StopFilter filterStream = new StopFilter(tokenStream, stopWords);

		CharTermAttribute attr = filterStream.addAttribute(CharTermAttribute.class);

		filterStream.reset();

		Map<String, Double> occurrences = new HashMap<>();


		while (filterStream.incrementToken()) {
			String word = attr.toString().toLowerCase();

			stemmer.setCurrent(word);
			stemmer.stem();

			word = stemmer.getCurrent();

			Double oldCount = occurrences.get(word);
			if (oldCount == null) {
				oldCount = 0.;
			}
			occurrences.put(word, oldCount + 1);
		}

//		System.out.println(occurrences);
		return occurrences;
	}


	public static int doubleToInt(double a) {
		return (int) (a * 1000);
	}

	public static String readWholeFile(String path) throws IOException {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}

	public static String getCommentText(Comment commentNode, String fileText) {
		int start = commentNode.getStartPosition();
		int end = start + commentNode.getLength();
		return fileText.substring(start, end);
	}
}

