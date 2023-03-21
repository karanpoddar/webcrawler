# Introduction
This web crawler has been developed as part of monzo’s take home assignment. The requirement was to crawl all the links in “monzo.com” but stay within the “monzo.com” domain.

# Design
The crawler has been designed with the ability for future extension.  It is a multithreaded design with multiple tunable parameters. The idea is that individual threads will fetch the website and put it in a queue for further processing (in this implementation, we are simply printing the URL and not putting the data in the queue).

The current implementation allows setting the allowed depth of the crawl from the seed URL, configure the number of threads, manage delay between crawling a domain and allow crawling beyond the seed URL.

# How to use it
The tunable parameters can be passed using the command line argument. The program can take multiple seed URLs passed using the ```--domain``` command. All other parameters are optional.
1. ```--threads```: To set the number of threads. Default value is 1
2. ```--depth```: Maximum crawl depth. Default is -1 meaning no limit
3. ```--delay```: Delay between 2 consecutive fetches from the same top level domain. Default is 1000ms
4. ```--limitCrawling```: Limit the crawl to stay within the seed URL domains. Default is true.

# Future work
1. **Robots.txt**: Support for respecting the robots.txt of a domain needs to be added
2. **Allowlist**: The allowlist will define the URLs/Regex which are allowed to crawl.
3. **Blocklist**: The blocklist will define the URLs/Regex which are not allowed to crawl.
4. **Logging**: Logging needs to be added at various points for monitoring and alerting
5. **Sandboxing**: In future versions, we might want the fetching to be done within a sandbox for security reasons
6. **Retries**: If fetching a URL fails, we need to provide a mechanism to retry and the value of number of retries can be set by the user
