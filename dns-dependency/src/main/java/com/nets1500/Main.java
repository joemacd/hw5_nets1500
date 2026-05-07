package com.nets1500;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting DNS dependency graph...");

        DNSDependencyGraph graph = new DNSDependencyGraph(20);
        graph.printGraph();

        DNSFailureAnalyzer analyzer = new DNSFailureAnalyzer(graph);
        graph.exportNameServerGraph("dns_nameserver_graph.dot");
        graph.exportProviderGraph("dns_provider_graph.dot", analyzer);
        analyzer.printProviderStats();
        analyzer.simulateProviderFailure("Google");
        analyzer.simulateProviderFailure("AWS Route 53");
        analyzer.simulateLargestProviderFailure();
        analyzer.testProviderRedundancy();
        analyzer.testSelfHosting();

        System.out.println("Done.");
    }
}
