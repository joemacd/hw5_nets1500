package com.nets1500;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DNSFailureAnalyzer {
    private Map<String, List<String>> siteToNameServers;

    /*
     * @param a finished DNSDependencyGraph.
     */
    public DNSFailureAnalyzer(DNSDependencyGraph graph) {
        this.siteToNameServers = graph.getSiteToNameServers();
    }

    /*
     * Attempt to link provider to nameserver. This is based on keywords in the nameserver hostname.
     *
     * Example:
     * ns1.google.com. -> Google
     */
    String getProvider(String nameServer) {
        String ns = nameServer.toLowerCase();
        if (ns.endsWith(".")) ns = ns.substring(0, ns.length() - 1);

        if (ns.contains("google")) {
            return "Google";
        } else if (ns.contains("cloudflare")) {
            return "Cloudflare";
        } else if (ns.contains("awsdns")) {
            return "AWS Route 53";
        } else if (ns.contains("akam")) {
            return "Akamai";
        } else if (ns.contains("azure") || ns.contains("microsoft")) {
            return "Microsoft Azure";
        } else if (ns.contains("dynect") || ns.contains("oracle")) {
            return "Oracle Dyn";
        } else if (ns.contains("ultradns")) {
            return "UltraDNS";
        } else if (ns.contains("wikimedia")) {
            return "Wikimedia";
        } else if (ns.contains("facebook")) {
            return "Meta";
        }

        String[] parts = ns.split("\\.");
        return parts.length >= 2 ? parts[parts.length - 2] + "." + parts[parts.length - 1] : ns;
    }

    /*
     * Print how many websites depend on each DNS provider.
     */
    public void printProviderStats() {
        System.out.println("\nProvider Stats:");

        Map<String, Set<String>> providerToSites = buildProviderToSites();

        for (String provider : providerToSites.keySet()) {
            Set<String> sites = providerToSites.get(provider);
            System.out.println(provider + ": " + sites.size() + " site(s) " + sites);
        }
    }

    /*
     * Simulate the failure of one DNS provider.
     * If Google fails, this prints every site that depends on Google DNS.
     */
    public void simulateProviderFailure(String provider) {
        simulateProviderFailure(provider, buildProviderToSites());
    }

    private void simulateProviderFailure(String provider, Map<String, Set<String>> providerToSites) {
        System.out.println("\nProvider Failure Simulation:");
        System.out.println("Failed provider: " + provider);

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
        simulateProviderFailure(largestProvider, providerToSites);
    }

    /*
     * Test whether sites with multiple nameservers actually use
     * multiple DNS providers.
     */
    public void testProviderRedundancy() {
        System.out.println("\nProvider Redundancy Test:");

        int singleNameserver = 0;
        int multipleNameserversSingleProvider = 0;
        int multipleProviders = 0;

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
                singleNameserver++;
            } else if (providers.size() == 1) {
                System.out.println("Result: multiple nameservers, but only one provider");
                multipleNameserversSingleProvider++;
            } else {
                System.out.println("Result: provider-diverse DNS setup");
                multipleProviders++;
            }

            System.out.println();
        }

        int multipleNameservers = multipleNameserversSingleProvider + multipleProviders;
        System.out.println("Summary:");
        System.out.println("Single nameserver:                    " + singleNameserver);
        System.out.println("Multiple nameservers, one provider:   " + multipleNameserversSingleProvider);
        System.out.println("Multiple nameservers, many providers: " + multipleProviders);
        if (multipleNameservers > 0) {
            int pct = (multipleNameserversSingleProvider * 100) / multipleNameservers;
            System.out.println("Of sites with multiple nameservers, " + pct + "% use only one provider");
        }
    }

    /*
     * Test whether top websites self-host their DNS or outsource to third-party providers.
     *
     * Sites can also be hybrid (some nameservers self-hosted, some outsourced).
     */
    public void testSelfHosting() {
        System.out.println("\nSelf-hosting vs. Outsourcing:");

        int selfHosted = 0;
        int outsourced = 0;
        int hybrid = 0;

        for (String site : siteToNameServers.keySet()) {
            List<String> nameServers = siteToNameServers.get(site);

            long ownCount = nameServers.stream()
                .map(ns -> ns.endsWith(".") ? ns.substring(0, ns.length() - 1) : ns)
                .filter(ns -> ns.endsWith("." + site) || ns.equals(site))
                .count();

            String result;
            if (ownCount == nameServers.size()) {
                result = "self-hosted";
                selfHosted++;
            } else if (ownCount > 0) {
                result = "hybrid";
                hybrid++;
            } else {
                result = "outsourced";
                outsourced++;
            }

            System.out.println(site + " [" + result + "]");
            for (String ns : nameServers) {
                System.out.println("  " + ns);
            }
        }

        System.out.println("\nSummary:");
        System.out.println("Self-hosted: " + selfHosted);
        System.out.println("Hybrid:      " + hybrid);
        System.out.println("Outsourced:  " + outsourced);
    }

    /*
     * Build a provider-to-sites map from the graph.
     */
    private Map<String, Set<String>> buildProviderToSites() {
        Map<String, Set<String>> providerToSites = new HashMap<>();

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
