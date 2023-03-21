package com.monzo;

import com.google.common.net.InternetDomainName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * CrawlURL class parses a URL and provides APIs for the WebCrawler to
 * crawl the URL.
 */
public class CrawlURL {
    // Allowed schemes
    private static String[] customSchemes = { "http", "https" };
 
    // The URL to be crawled
    private URI uri;
 
    // The depth of this URL wrt to the seed URL
    private int depth;
 
    // Enum to express state of the URL
    public static enum URL_STATE {
        // State not set
        UNKNOWN,
        // URL is in the queue
        IN_QUEUE,
        // URL has been crawled and processed
        PROCESSED
    };
 
    // The state of the URL.
    private URL_STATE state;
 
    // True if the provided string cannot be parsed into a valid URL
    private boolean isInvalid = false;

    CrawlURL(String stringUrl, int depth) throws MalformedURLException {
        this(stringUrl, depth, URL_STATE.UNKNOWN);
    }

    /**
     * Creates a new instance of CrawlUrl around the provided string. If not a valid URL,
     * it sets the {@isInvalid} to true.
     * 
     * It only considers URLs valid with http or https scheme. It also adds a trailing slash
     * in all the URLs.
     * 
     * @param stringUrl Input URL in string format
     * @param depth The depth of this URL wrt to the seed URL
     * @param state The state of this URL
     * @throws MalformedURLException
     */
    CrawlURL(String stringUrl, int depth, URL_STATE state) throws MalformedURLException {
        this.depth = depth;
        this.state = state;
        UrlValidator validator = new UrlValidator(customSchemes);
        if (!validator.isValid(stringUrl)) {
            if (!validator.isValid("https://" + stringUrl)) {
                isInvalid = true;
                return;
            }
            stringUrl = "https://" + stringUrl;
        }
        stringUrl = stringUrl.replaceAll("#.*", "");
        if (!stringUrl.endsWith("/")) {
            stringUrl = stringUrl + "/";
        }

        try {
            uri = new URL(stringUrl).toURI();
        } catch (MalformedURLException | URISyntaxException ex) {
            ex.printStackTrace();
            isInvalid = true;
            return;
        }
        cleanUpUri();
    }

    /**
     * Returns the URL to be crawled
     * @return the URL to be crawled
     */
    public String getUrl() {
        return uri.toString();
    }

    /**
     * Returns the URL without the scheme
     * @return the URL without the scheme
     */
    public String getUrlWithoutScheme() {
        if (uri.getScheme().equals("https")) {
            return uri.toString().substring(8, uri.toString().length());
        } else {
            return uri.toString().substring(7, uri.toString().length());
        }
    }

    /**
     * Returns the depth of this URL
     * @return the depth of this URL
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the state of the URL
     * @return the state of the URL
     */
    public URL_STATE getState() {
        return state;
    }

    /**
     * Sets the state of the URL to IN_QUEUE
     */
    public void setStateInQueue() {
        state = URL_STATE.IN_QUEUE;
    }

    /**
     * Sets the state of the URL to PROCESSED
     */
    public void setStateProcessed() {
        state = URL_STATE.PROCESSED;
    }

    /**
     * Returns the top-level domain of the URL
     * @return the top-level domain of the URL
     */
    public String getDomain() {
        InternetDomainName internetDomainName = InternetDomainName.from(uri.getHost()).topPrivateDomain();
        return internetDomainName.toString();
    }

    /**
     * Returns true if the provided string cannot be successfully parsed into a valid URL
     * @return returns true if the provided string cannot be successfully parsed into a valid URL
     */
    public boolean isInvalid() {
        return isInvalid;
    }

    /**
     * Normalises the URL after it is initialised.
     */
    private void cleanUpUri() {
        uri = uri.normalize();
    }
}
