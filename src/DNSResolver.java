import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class DNSResolver {
    /*
    * Get the TLD nameservers, general domain nameservers, and IP addresses of a given URL
    */
    public void resolve(String URL) throws Exception {
        // We will get the DNS providers for hostname
        String hostname = URI.create(URL).toURL().getHost();

        String[] piecesOfHostname = hostname.split("\\.");

        // get top level-domain and construct our full domain from it
        String topLevelDomain = piecesOfHostname[piecesOfHostname.length - 1];
        String domain = piecesOfHostname[piecesOfHostname.length - 2] + "." + topLevelDomain;

        // we will use DirContext to communicate with DNS
        DirContext ctx = new InitialDirContext(new Hashtable<>(Map.of(
            Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory"
        )));

        // Top Level Domain NameServers
        System.out.println("Top Level Domain NameServers (." + topLevelDomain + "):");
        List<String> topLevelDomainNameServers = queryNameServer(ctx, topLevelDomain);
        for (String nameServer : topLevelDomainNameServers) {
            System.out.println(nameServer);
        }

        // Domain Servers
        System.out.println("Domain NameServers (" + domain + "):");
        List<String> domainNameServers = queryNameServer(ctx, domain);
        for (String nameServer : domainNameServers) {
            System.out.println(nameServer);
        }

        // Get IP Addresses
        System.out.println("Resolved IP Addresses (" + hostname + "):");
        InetAddress[] ipAddresses = InetAddress.getAllByName(hostname);
        for (InetAddress ipAddress : ipAddresses) {
            System.out.println(ipAddress.getHostAddress());
        }

        ctx.close();
    }

    /*
    * Get the list of name servers for a given domain name by querying with DirContext
    */
    private List<String> queryNameServer(DirContext ctx, String name) {
        List<String> results = new ArrayList<>();
        try {
            Attributes attrs = ctx.getAttributes("dns:/" + name, new String[]{"NS"});
            Attribute nameServers = attrs.get("NS");
            if (nameServers != null) {
                for (int i = 0; i < nameServers.size(); i++) {
                    results.add(nameServers.get(i).toString());
                }
            } else {
                results.add("No NameServer records found");
            }
        } catch (NamingException e) {
            results.add("Error: " + e.getMessage());
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        Scanner bob = new Scanner(System.in);
        System.out.print("Enter a URL: ");
        String url = bob.nextLine();
        bob.close();

        new DNSResolver().resolve(url);
    }
}
