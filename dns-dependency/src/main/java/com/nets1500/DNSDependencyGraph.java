package com.nets1500;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class DNSDependencyGraph {
    private final String top100Link = "https://seranking.com/top-websites-us.html";
    
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
    //TODO
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
        System.out.println(sites);
        return sites;
    }

    public static void main(String[] args) {
        DNSDependencyGraph graph = new DNSDependencyGraph(100);
    }
} 