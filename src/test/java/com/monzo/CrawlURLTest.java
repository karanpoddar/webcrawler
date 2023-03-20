package com.monzo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import org.junit.Test;

import com.monzo.CrawlURL.URL_STATE;

public class CrawlURLTest {
    @Test
    public void testGetDepth() {
        try {
            CrawlURL url = new CrawlURL("www.monzo.com", 1);
            assertEquals(1, url.getDepth());
        } catch (MalformedURLException ex) {
            fail();
        }
    }

    @Test
    public void testGetDomain() {
        try {
            CrawlURL url1 = new CrawlURL("www.monzo.com", 1);
            CrawlURL url2 = new CrawlURL("http://www.monzo.com/blogs/hello", 1);
            CrawlURL url3 = new CrawlURL("http://www.blog.monzo.com/hello", 1);

            assertEquals("monzo.com", url1.getDomain());
            assertEquals("monzo.com", url2.getDomain());
            assertEquals("monzo.com", url3.getDomain());
        } catch (MalformedURLException ex) {
            fail();
        }
    }

    @Test
    public void testGetState() {
        try {
            CrawlURL url = new CrawlURL("www.monzo.com", 1);

            assertEquals(URL_STATE.UNKNOWN, url.getState());
            url.setStateInQueue();
            assertEquals(URL_STATE.IN_QUEUE, url.getState());
            url.setStateProcessed();
            assertEquals(URL_STATE.PROCESSED, url.getState());
        } catch (MalformedURLException ex) {
            fail();
        }
    }

    @Test
    public void testGetUrl() {
        try {
            CrawlURL url1 = new CrawlURL("www.monzo.com", 1);
            CrawlURL url2 = new CrawlURL("http://www.monzo.com/#main", 1);

            assertEquals("https://www.monzo.com/", url1.getUrl());
            assertEquals("http://www.monzo.com/", url2.getUrl());
        } catch (MalformedURLException ex) {
            fail();
        }
    }

    @Test
    public void testGetUrlWithoutScheme() {
        try {
            CrawlURL url1 = new CrawlURL("https://www.monzo.com", 1);
            CrawlURL url2 = new CrawlURL("http://www.monzo.com/", 1);

            assertEquals("www.monzo.com/", url1.getUrlWithoutScheme());
            assertEquals("www.monzo.com/", url2.getUrlWithoutScheme());
        } catch (MalformedURLException ex) {
            fail();
        }
    }

    @Test
    public void testIsInvalid() {
        try {
            CrawlURL url_invalid_scheme = new CrawlURL("ftp://www.monzo.com", 1);
            CrawlURL url_valid = new CrawlURL("http://www.monzo.com/blogs/hello", 1);
            CrawlURL url_null_scheme = new CrawlURL("www.blog.monzo.com/hello", 1);
            CrawlURL url_invalid_string = new CrawlURL("random_string", 1);

            assertTrue("Invalid scheme", url_invalid_scheme.isInvalid());
            assertFalse("Valid URL", url_valid.isInvalid());
            assertFalse("Null scheme", url_null_scheme.isInvalid());
            assertTrue("Invalid string", url_invalid_string.isInvalid());
        } catch (MalformedURLException ex) {
            fail();
        }
    }
}
