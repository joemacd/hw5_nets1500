package com.nets1500;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.io.FileWriter;


public class DNSDependencyGraph {
    private final String top100Link = "https://seranking.com/top-websites-us.html";
    private Map<String, List<String>> siteToNameServers = new HashMap<>();
    private Map<String, List<String>> nameServerToSites = new HashMap<>();

    /*
    * 1 <= x <= 100
    */
   public DNSDependencyGraph(int x) {
        // validate input
        if (x < 1 || x > 100) {
            throw new IllegalArgumentException("1 <= x <= 100");
        }
    
        try {
            List<String> sites = scanTopX(x);
            for (String site : sites) {
                addSite(site);
            }
        } catch (IOException e) {
            throw new RuntimeException("Site List not found", e);
        }
        

        
   }

   /*
    * Add site to our DNS Dependency Graph
    */
    private void addSite(String site) {
        try {
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

            DirContext ctx = new InitialDirContext(new Hashtable<>(Map.of(
                Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory"
            )));

            Attributes attrs = ctx.getAttributes("dns:/" + domain, new String[]{"NS"});
            Attribute nameServers = attrs.get("NS");

            List<String> nsList = new ArrayList<>();

            if (nameServers != null) {
                for (int i = 0; i < nameServers.size(); i++) {
                    String ns = nameServers.get(i).toString().toLowerCase();
                    nsList.add(ns);

                    nameServerToSites.putIfAbsent(ns, new ArrayList<>());
                    nameServerToSites.get(ns).add(domain);
                }
            }

            siteToNameServers.put(domain, nsList);

            System.out.println("Added site: " + domain);
            System.out.println("Name servers: " + nsList);

            ctx.close();

        } catch (NamingException e) {
            System.out.println("Could not resolve DNS for site: " + site);
            System.out.println("Reason: " + e.getMessage());
        }
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
        try {
            FileWriter writer = new FileWriter(filename);

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
            writer.close();

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