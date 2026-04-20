import java.util.ArrayList;
import java.util.List;

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

        List<String> sites = scanTopX(x);

        for (String site : sites) {
            addSite(site);
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
    private List<String> scanTopX(int x) {
        return new ArrayList<>();
    }
}