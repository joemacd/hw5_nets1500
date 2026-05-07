DNS Dependency Analysis
NETS 1500 — Markets and Social Systems on the Internet

## Project Description:

This project analyzes the DNS infrastructure across the 100 most visited websites. Each time a user visits one of these websites, a DNS lookup is performed. Despite the critical role of this infrastructure, it is largely invisible to end-users. The goal of our project was to map the DNS dependency graph of the top 100 U.S. websites, identify which DNS providers dominate the market, and analyze what that concentration means for the resilience and structure of the web.

## Categories Used:

- Empirical Analysis (DNS dependency graph, hypothesis testing)
- Data collection via web scraping with JSoup (seranking.com) and DNS queries

## Code:

We conducted our Analysis in Java and broke it up into 3 core components:

- DNSResolver: Performs DNS nameserver record lookups for a given domain. We used this class to perform lookups across all top websites.
- DNSDependencyGraph: scrapes the top 100 websites from seranking.com using JSoup, then uses the DNSResolver class to build a bidirectional map of sites to nameservers and nameservers to sites. Then, it exports this as a GraphViz DOT file for easy visualization.
- DNSFailureAnalyzer: Analyzes the graph to classify DNS providers, simulate failures, test redundancy, and measure self-hosting rates. This is where our hypotheses were tested.

(If running on your own) to generate graph images from DOT files (requires Graphviz):
sfdp -Tpng -Goverlap=prism dns_nameserver_graph.dot -o dns_nameserver_graph.png
sfdp -Tpng -Goverlap=prism dns_provider_graph.dot -o dns_provider_graph.png

## Work Breakdown

Joe MacDougall: DNSResolver logic, self-hosting vs outsourcing hypothesis testing, hypothesis ideation, provider visualization,

Liana Veerasamy: DNSDependencyGraph, initial hypothesis ideation, NS->site dependence hypothesis tests, nameserver visualization.

Arushi Agarwal: DNSDependencyGraph, JSoup scraping, hypothesis ideation, DNSFailureAnalyzer summary statistics.

## AI Usage:

Our group used AI to assist with pieces of coding. Specifically, we used AI for vizualization library logic, debugging a deprecated DNS-related library, and for initial learning (non-code) about nameserver queries in Java.
