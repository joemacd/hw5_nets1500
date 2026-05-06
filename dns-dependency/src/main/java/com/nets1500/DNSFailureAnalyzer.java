package com.nets1500;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * DNSFailureAnalyzer handles the analysis part of the project.
 *
 * It uses the dependency graph built by DNSDependencyGraph to:
 * - identify DNS providers
 * - test project hypotheses
 * - simulate provider failures
 * - measure how many websites are affected
 */
public class DNSFailureAnalyzer {
    private Map<String, List<String>> siteToNameServers;

    /*
     * Constructor takes in a finished DNSDependencyGraph.
     */
    public DNSFailureAnalyzer(DNSDependencyGraph graph) {
        this.siteToNameServers = graph.getSiteToNameServers();
    }

    /*
     * Guess which DNS provider owns a nameserver.
     *
     * This is based on keywords in the nameserver hostname.
     *
     * Example:
     * ns1.google.com. -> Google
     * ns-557.awsdns-05.net. -> AWS Route 53
     */
    private String getProvider(String nameServer) {
        nameServer = nameServer.toLowerCase();

        if (nameServer.contains("google")) {
            return "Google";
        } else if (nameServer.contains("cloudflare")) {
            return "Cloudflare";
        } else if (nameServer.contains("awsdns")) {
            return "AWS Route 53";
        } else if (nameServer.contains("akam")) {
            return "Akamai";
        } else if (nameServer.contains("azure") || nameServer.contains("microsoft")) {
            return "Microsoft Azure";
        } else if (nameServer.contains("dynect") || nameServer.contains("oracle")) {
            return "Oracle Dyn";
        } else if (nameServer.contains("ultradns")) {
            return "UltraDNS";
        } else if (nameServer.contains("wikimedia")) {
            return "Wikimedia";
        } else if (nameServer.contains("facebook")) {
            return "Meta";
        }

        return "Other";
    }

    /*
     * Print how many websites depend on each DNS provider.
     *
     * This helps test:
     * H1: A small number of providers support many websites.
     * H2: Popular websites rely on major DNS providers.
     * H3: The graph has a hub-like structure.
     */
    public void printProviderStats() {
        System.out.println("\n========== PROVIDER STATS ==========");

        Map<String, Set<String>> providerToSites = buildProviderToSites();

        for (String provider : providerToSites.keySet()) {
            Set<String> sites = providerToSites.get(provider);
            System.out.println(provider + ": " + sites.size() + " site(s) " + sites);
        }
    }

    /*
     * Simulate the failure of one DNS provider.
     *
     * Example:
     * If Google fails, this prints every site that depends on Google DNS.
     */
    public void simulateProviderFailure(String provider) {
        System.out.println("\n========== PROVIDER FAILURE SIMULATION ==========");
        System.out.println("Failed provider: " + provider);

        Map<String, Set<String>> providerToSites = buildProviderToSites();
        Set<String> affectedSites = providerToSites.get(provider);

        if (affectedSites == null || affectedSites.isEmpty()) {
            System.out.println("No affected sites found for this provider.");
            return;
        }

        System.out.println("Affected sites:");

        for (String site : affectedSites) {
            System.out.println("- " + site);
        }

        System.out.println("Total affected sites: " + affectedSites.size());
    }

    /*
     * Simulate failure of the provider with the most dependent websites.
     *
     * This helps test:
     * H4: Failure of the largest provider affects more websites
     * than failure of smaller providers.
     */
    public void simulateLargestProviderFailure() {
        Map<String, Set<String>> providerToSites = buildProviderToSites();

        String largestProvider = null;
        int maxSites = 0;

        for (String provider : providerToSites.keySet()) {
            int count = providerToSites.get(provider).size();

            if (count > maxSites) {
                maxSites = count;
                largestProvider = provider;
            }
        }

        if (largestProvider == null) {
            System.out.println("No provider data available.");
            return;
        }

        System.out.println("\nLargest provider by dependent sites: " + largestProvider);
        simulateProviderFailure(largestProvider);
    }

    /*
     * Test whether sites with multiple nameservers actually use
     * multiple DNS providers.
     *
     * This helps test:
     * H5: Multiple nameservers do not always mean multiple providers.
     */
    public void testProviderRedundancy() {
        System.out.println("\n========== PROVIDER REDUNDANCY TEST ==========");

        for (String site : siteToNameServers.keySet()) {
            List<String> nameServers = siteToNameServers.get(site);

            Set<String> providers = new HashSet<>();

            for (String ns : nameServers) {
                providers.add(getProvider(ns));
            }

            System.out.println(site);
            System.out.println("Nameserver count: " + nameServers.size());
            System.out.println("Provider count: " + providers.size());
            System.out.println("Providers: " + providers);

            if (nameServers.size() == 1) {
                System.out.println("Result: single nameserver");
            } else if (providers.size() == 1) {
                System.out.println("Result: multiple nameservers, but only one provider");
            } else {
                System.out.println("Result: provider-diverse DNS setup");
            }

            System.out.println();
        }
    }

    /*
     * Build a provider-to-sites map from the graph.
     *
     * Original graph:
     * site -> nameservers
     *
     * This method converts it into:
     * provider -> affected sites
     */
    private Map<String, Set<String>> buildProviderToSites() {
        Map<String, Set<String>> providerToSites = new java.util.HashMap<>();

        for (String site : siteToNameServers.keySet()) {
            List<String> nameServers = siteToNameServers.get(site);

            for (String ns : nameServers) {
                String provider = getProvider(ns);

                providerToSites.putIfAbsent(provider, new HashSet<>());
                providerToSites.get(provider).add(site);
            }
        }

        return providerToSites;
    }
}
