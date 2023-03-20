package com.monzo;

import com.beust.jcommander.*;
import com.monzo.CrawlURL.URL_STATE;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * WebCrawler uses a provided list of domains regex and parses the HTML body.
 * The processing of the body can be defined by the user using the interface
 * class 'ProcessBody'
 * 
 * Parameters:
 *   - List of domains: List of domains to crawl. Eg: "monzo.com,*.monzo.com"
 *   - Depth: How much deep should the crawler go into a given starting domain.
 *			  Default is -1 meaning no limit.
 *   - Delay: Pause time in Milliseconds. The crawler will pause by this amount
 *			  before hitting the domain again. Default is 1000 ms
 *   - threads: Number of threads to use for crawling. Default is 1
 *   - limitCrawling: If true, keep crawling within the provided top level domains.
 *            Default is true
 */
class WebCrawler {
    // URLs to crawl
    private Queue<CrawlURL> urlsToCrawl  = new ConcurrentLinkedQueue<>();
    
    // 0 if in Queue, 1 if processed. If absent, no records
    private Map<String, CrawlURL> urlMap = new ConcurrentHashMap<>();

    // Timestamp when a domain was last queried
    private Map<String, Long> domainTimestamps = new ConcurrentHashMap<>();

    @Parameter(names={"--domains"}, description = "Comma-separated list of domains to crawl", required = true)
    private String domains;
    @Parameter(names={"--depth"}, description = "Max depth to crawl")
    private int maxDepth = -1;
    @Parameter(names={"--delay"}, description = "Delay in milliseconds between quering the same domain")
    private int delay = 1000;
    @Parameter(names={"--threads"}, description = "Number of threads to use for crawling")
    private int numberOfThreads = 1;
    @Parameter(names={"--limitCrawling"}, description = "Crawl only the provided top level domains")
    private boolean limitedCrawling = true;
    
    private ExecutorService executor; // thread pool

    public static void main(String args[]) throws InterruptedException {
        WebCrawler crawler = new WebCrawler();
        JCommander.newBuilder()
            .addObject(crawler)
            .build()
            .parse(args);
        
        crawler.parseInputDomains();
        crawler.startCrawling();
    }

    WebCrawler() {
    }

    private void parseInputDomains() {
        CSVReader reader = new CSVReader(new StringReader(domains));
        try {
            String[] tokenizedDomains = reader.readNext();
            for (String domain : tokenizedDomains) {
                System.out.println(domain);
                CrawlURL url = new CrawlURL(domain, 0, URL_STATE.IN_QUEUE);
                urlsToCrawl.add(url);
                urlMap.put(url.getUrlWithoutScheme(), url);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return;
    }

    private void startCrawling() throws InterruptedException {
        executor = Executors.newFixedThreadPool(numberOfThreads);
        executor.execute(() ->
            crawl()
        );
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private void crawl() {
        while (!urlsToCrawl.isEmpty()) {
            CrawlURL url = urlsToCrawl.poll();
            String domain = url.getDomain();
            long currentTimeMillis = System.currentTimeMillis();
            long lastTimestamp = domainTimestamps.getOrDefault(domain, 0L);
            if (url.getState() == URL_STATE.PROCESSED) {
                continue;
            } else if ((currentTimeMillis - lastTimestamp) < delay) {
                urlsToCrawl.offer(url); // put the url back in the queue
                continue;
            }
            domainTimestamps.put(domain, currentTimeMillis);
            url.setStateProcessed();

            System.out.println("Visiting: " + url.getUrl());
            // fetch the web page using Jsoup
            Document doc;
            try {
                doc = Jsoup.connect(url.getUrl()).get();
            } catch (IOException ex) {
                ex.printStackTrace();
                continue;
            }

            // get all links on the page
            Elements links = doc.select("a[href]");

            // add new urls to the queue
            for (Element link : links) {
                String newUrl = link.absUrl("href");
                try {
                    CrawlURL newCrawlUrl = new CrawlURL(newUrl, url.getDepth() + 1);
                    if (newCrawlUrl.isInvalid() || !isAllowed(newCrawlUrl)) {
                        continue;
                    }
                    newCrawlUrl = urlMap.getOrDefault(newCrawlUrl.getUrlWithoutScheme(), newCrawlUrl);
                    if (newCrawlUrl.getState() == URL_STATE.UNKNOWN && newCrawlUrl.getDepth() <= maxDepth) {
                        urlsToCrawl.add(newCrawlUrl);
                        newCrawlUrl.setStateInQueue();
                        urlMap.put(newCrawlUrl.getUrlWithoutScheme(), newCrawlUrl);
                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    boolean isAllowed(CrawlURL crawlURL) {
        if (limitedCrawling && !domainTimestamps.containsKey(crawlURL.getDomain())) {
            return false;
        }
        return true;
    }
}
