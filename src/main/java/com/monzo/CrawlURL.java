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
    private static String[] customSchemes = { "http", "https" };
    private URI uri;
    private int depth;
    public static enum URL_STATE {
        UNKNOWN,
        IN_QUEUE,
        PROCESSED
    };
    private URL_STATE state;
    private boolean isInvalid = false;

    CrawlURL(String stringUrl, int depth) throws MalformedURLException {
        this(stringUrl, depth, URL_STATE.UNKNOWN);
    }

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

    public String getUrl() {
        return uri.toString();
    }

    public String getUrlWithoutScheme() {
        if (uri.getScheme().equals("https")) {
            return uri.toString().substring(8, uri.toString().length());
        } else {
            return uri.toString().substring(7, uri.toString().length());
        }
    }

    public int getDepth() {
        return depth;
    }

    public URL_STATE getState() {
        return state;
    }

    public void setStateInQueue() {
        state = URL_STATE.IN_QUEUE;
    }

    public void setStateProcessed() {
        state = URL_STATE.PROCESSED;
    }

    public String getDomain() {
        InternetDomainName internetDomainName = InternetDomainName.from(uri.getHost()).topPrivateDomain();
        return internetDomainName.toString();
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    private void cleanUpUri() {
        uri = uri.normalize();
    }
}
