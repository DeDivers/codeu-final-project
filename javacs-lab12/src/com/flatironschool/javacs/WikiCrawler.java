package com.flatironschool.javacs;

import java.io.IOException;
import java.io.Console;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
	// keeps track of where we started
	private final String source;
	
	// the index where the results go
	private JedisIndex index;
	
	// queue of URLs to be indexed
	private Queue<String> queue = new LinkedList<String>();
	
	// fetcher used to get pages from Wikipedia
	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Constructor.
	 * 
	 * @param source
	 * @param index
	 */
	public WikiCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 * 
	 * @return
	 */
	public int queueSize() {
		return queue.size();	
	}

	/**
	 * Gets a URL from the queue and indexes it.
	 * @param b 
	 * 
	 * @return Number of pages indexed.
	 * @throws IOException
	 */
	public String crawl(boolean testing) throws IOException {
		if (queue.isEmpty()) {
			return null;
		}
		String s = queue.poll();
		// System.out.println(s);
		Elements e;
        if (testing) {
        	e = wf.readWikipedia(s);
        } else {
        	while (index.isIndexed(s)) {
        		s = queue.poll();
        	}
        	e = wf.fetchWikipedia(s);
        }
        index.indexPage(s, e);
        queueInternalLinks(e);
		return s;
	}
	
	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
    	for (Element para: paragraphs) {
    		for (Element e: para.select("a[href]")) {
    			String str = e.attr("href");
    			if (str.startsWith("/wiki/")) {
    				String s = e.attr("abs:href");
    				queue.offer(s);
    			}
    		}
    	}
	}

	public static void main(String[] args) throws IOException {
		
		// make a WikiCrawler
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		String source = "https://en.wikipedia.org/wiki/Chiesa_di_San_Costanzo";
		WikiCrawler wc = new WikiCrawler(source, index);
		
		// for testing purposes, load up the queue
		Elements paragraphs = wf.fetchWikipedia(source);
		wc.queueInternalLinks(paragraphs);

		// loop until we index a new page
		String s = "";
		while (s != null) {
			s = wc.crawl(false);
			
		} 
		
		Map<String, Double> map = index.getCounts("the");
		for (Entry<String, Double> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}
}
