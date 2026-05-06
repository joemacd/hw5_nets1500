package com.nets1500;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting DNS dependency graph...");

        DNSDependencyGraph graph = new DNSDependencyGraph(20);
        graph.printGraph();
        graph.exportGraph("dns_graph.dot");

        DNSFailureAnalyzer analyzer = new DNSFailureAnalyzer(graph);
        analyzer.printProviderStats();
        analyzer.simulateProviderFailure("Google");
        analyzer.simulateProviderFailure("AWS Route 53");
        analyzer.simulateLargestProviderFailure();
        analyzer.testProviderRedundancy();

        System.out.println("Done.");
    }
}
