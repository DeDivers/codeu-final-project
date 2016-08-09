package com.flatironschool.javacs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.text.Normalizer;

import io.indico.Indico;
import io.indico.api.results.IndicoResult;
import io.indico.api.results.BatchIndicoResult;
import io.indico.api.utils.IndicoException;
import io.indico.api.text.PoliticalClass;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


/**
 * Encapsulates a map from search term to frequency (count).
 * 
 * @author downey
 *
 */
public class TermCounter {
	
	private Map<String, Integer> map;
	private String label;
	
	public TermCounter(String label) {
		this.label = label;
		this.map = new HashMap<String, Integer>();
	}
	
	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns the total of all counts.
	 * 
	 * @return
	 */
	public int size() {
		int total = 0;
		for (Integer value: map.values()) {
			total += value;
		}
		return total;
	}

	/**
	 * Takes a collection of Elements and counts their words.
	 * 
	 * @param paragraphs
	 */
	public Map<PoliticalClass, Double> processElements(Elements paragraphs) {
    String text = "";
		for (Node node: paragraphs) {
			text += processTree(node);
		}
    Indico indico = new Indico("ed10971412405df6de77333f9fab3033");
    try {
      String hi = "";
      String normalizedText = Normalizer
        .normalize(text, Normalizer.Form.NFD)
        .replaceAll("[^\\p{ASCII}]", "");
      IndicoResult single = indico.political.predict(normalizedText);
      Map<PoliticalClass, Double> result = single.getPolitical();
      System.out.println("The political leanings are: " + result);
      return result;
    } catch (IndicoException exception) {
      System.out.println("Failed.");
    } catch (IOException exception) {
      System.out.println("Failed.");
    }

    return new HashMap<PoliticalClass, Double>();
	}

  /**
	 * Finds TextNodes in a DOM tree and counts their words.
	 * 
	 * @param root
	 */
	public String processTree(Node root) {
		// NOTE: we could use select to find the TextNodes, but since
		// we already have a tree iterator, let's use it.
    String text = "";
		for (Node node: new WikiNodeIterable(root)) {
			if (node instanceof TextNode) {
				text += processText(((TextNode) node).text());
			}
		}
    return text;
	}

	/**
	 * Splits `text` into words and counts them.
	 * 
	 * @param text  The text to process.
	 */
	public String processText(String text) {
		// replace punctuation with spaces, convert to lower case, and split on whitespace
		String[] array = text.replaceAll("\\pP", " ").toLowerCase().split("\\s+");
		for (int i=0; i<array.length; i++) {
			String term = array[i];
			incrementTermCount(term);
		}
    return text;
	}

	/**
	 * Increments the counter associated with `term`.
	 * 
	 * @param term
	 */
	public void incrementTermCount(String term) {
		// System.out.println(term);
		put(term, get(term) + 1);
	}

	/**
	 * Adds a term to the map with a given count.
	 * 
	 * @param term
	 * @param count
	 */
	public void put(String term, int count) {
		map.put(term, count);
	}

	/**
	 * Returns the count associated with this term, or 0 if it is unseen.
	 * 
	 * @param term
	 * @return
	 */
	public Integer get(String term) {
		Integer count = map.get(term);
		return count == null ? 0 : count;
	}

	/**
	 * Returns the set of terms that have been counted.
	 * 
	 * @return
	 */
	public Set<String> keySet() {
		return map.keySet();
	}
	
	/**
	 * Print the terms and their counts in arbitrary order.
	 */
	public void printCounts() {
		for (String key: keySet()) {
			Integer count = get(key);
			System.out.println(key + ", " + count);
		}
		System.out.println("Total of all counts = " + size());
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		
		WikiFetcher wf = new WikiFetcher();
		Elements paragraphs = wf.fetchWikipedia(url);
		
		TermCounter counter = new TermCounter(url.toString());
		counter.processElements(paragraphs);
		counter.printCounts();
	}
}
