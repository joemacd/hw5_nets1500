package com.nets1500;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;


public class DNSDependencyGraph {
    private final String top100Link = "https://seranking.com/top-websites-us.html";
    private final DNSResolver resolver = new DNSResolver();
    private Map<String, List<String>> siteToNameServers = new HashMap<>();
    private Map<String, List<String>> nameServerToSites = new HashMap<>();

    /*
    * 1 <= x <= 100
    */
    public DNSDependencyGraph(int x) {
        if (x < 1 || x > 100) {
            throw new IllegalArgumentException("1 <= x <= 100");
        }

        DirContext ctx = null;
        try {
            List<String> sites = scanTopX(x);
            ctx = new InitialDirContext(new Hashtable<>(Map.of(
                    Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory")));
            for (String site : sites) {
                addSite(site, ctx);
            }
        } catch (IOException e) {
            throw new RuntimeException("Site list not found", e);
        } catch (NamingException e) {
            throw new RuntimeException("Could not create DNS context", e);
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (NamingException ignored) {}
            }
        }
    }

    /*
    * Add site to our DNS Dependency Graph
    */
    private void addSite(String site, DirContext ctx) {
        String domain = site.trim().toLowerCase();

        if (domain.startsWith("https://")) {
            domain = domain.substring(8);
        } else if (domain.startsWith("http://")) {
            domain = domain.substring(7);
        }

        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }

        int slashIndex = domain.indexOf("/");
        if (slashIndex != -1) {
            domain = domain.substring(0, slashIndex);
        }

        List<String> nsList = resolver.queryNameServer(ctx, domain).stream()
            .filter(ns -> !ns.startsWith("Error:") && !ns.startsWith("No NameServer"))
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        for (String ns : nsList) {
            nameServerToSites.putIfAbsent(ns, new ArrayList<>());
            nameServerToSites.get(ns).add(domain);
        }

        siteToNameServers.put(domain, nsList);

        System.out.println("Added site: " + domain);
        System.out.println("Name servers: " + nsList);
    }

    /*
    * Scan top X most visited websites from the seranking.com link
    */
    private List<String> scanTopX(int x) throws IOException {
        List<String> sites = new ArrayList<>();
        Document doc = Jsoup.connect(top100Link).get();
        // Elements cells = doc.select(".best-companies__table-cell-domain");
        Elements cells = doc.select(".best-companies__table-row-body .best-companies__table-cell-domain");
        for (int i = 0; i < Math.min(x, cells.size()); i++) {
            Element link = cells.get(i).selectFirst("a");
            if (link != null) {
                sites.add(link.text().trim());
            }
        }
        return sites;
    }

    /*
    * Print the DNS dependency graph in both directions.
    */
    public void printGraph() {
        System.out.println("\n========== SITE TO NAMESERVERS ==========");
        for (String site : siteToNameServers.keySet()) {
            System.out.println(site + " -> " + siteToNameServers.get(site));
        }

        System.out.println("\n========== NAMESERVER TO SITES ==========");
        for (String nameServer : nameServerToSites.keySet()) {
            System.out.println(nameServer + " -> " + nameServerToSites.get(nameServer));
        }
    }

    /*
    * Export graph to a GraphViz DOT file.
    */
    public void exportGraph(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("digraph DNSDependencyGraph {\n");
            writer.write("    rankdir=LR;\n");
            writer.write("    node [shape=box];\n");

            for (String site : siteToNameServers.keySet()) {
                writer.write("    \"" + site + "\" [shape=ellipse];\n");

                for (String ns : siteToNameServers.get(site)) {
                    writer.write("    \"" + site + "\" -> \"" + ns + "\";\n");
                }
            }

            writer.write("}\n");
            System.out.println("Graph exported to " + filename);

        } catch (IOException e) {
            System.out.println("Could not export graph.");
            System.out.println("Reason: " + e.getMessage());
        }
    }

    public Map<String, List<String>> getSiteToNameServers() {
        return siteToNameServers;
    }

    public Map<String, List<String>> getNameServerToSites() {
        return nameServerToSites;
    }
}