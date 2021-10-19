package src ;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class InspectWebLinks {

    static int validLinks = 0;
    static int brokenLinks = 0;
    static int emptyLinks = 0;
    static int skippedLinks = 0;
    static String reportData = "";

    public static void main(String[] args) throws IOException {

        inspect("https://uoh.fr/front/resultatsfr/");
        generate_report(reportData,"C:\\Users\\Etu\\Desktop\\javaDeadLink\\report.txt");

    }


    private static void generate_report(String reportData, String reportPath) throws IOException {

        FileWriter report = new FileWriter(reportPath);
        report.write(reportData);
        report.close();

    }


    private static int check_link(String url) {
//
//		if(url == null || url.isEmpty()){
//			return 0;
//		}
        Response response;
        try {
            response = Jsoup.connect(url).execute();
            if(response.statusCode() == 404) {
                return 0;
            }
            else {
                return 1;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 0;
        }
    }

    private static Set<String> get_links_on_page(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        Elements links = doc.select("a");

        Set<String> found_urls = new HashSet<String>();

        for (Element link: links) {
            System.out.println(link.toString());
            String sub_url = link.attr("abs:href");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"+sub_url);

            if (sub_url == null || sub_url.isEmpty()) {
                //System.out.println("\n Url is empty " + link.outerHtml() + " at " + url);

                emptyLinks += 1;
            }
            else {
                found_urls.add(sub_url);
            }

        }
        return found_urls;
    }

    private static void inspect(String start_url) throws IOException {
        Pattern p = Pattern.compile("uoh\\.fr/front/resultatsfr/.*query=") ;
        Pattern p2 = Pattern.compile("uoh\\.fr/front/resultatsfr/\\?query=.*") ;
        Set<String> visited = new HashSet<String>();
        Stack<String> to_visit = new Stack<String>();
        to_visit.push(start_url);
        int cpt = 0 ;

        while (!to_visit.isEmpty() && cpt<100) {

            String current_link = to_visit.pop();
            Matcher m = p.matcher(current_link);
            Matcher m2 = p2.matcher(current_link);
            if (!visited.contains(current_link) && current_link.startsWith(start_url) && !m.find() && !m2.find()) {
                System.out.print(".");
                System.out.println(current_link);
                int response = check_link(current_link);
                visited.add(current_link);
                if (response == 1) {
                    validLinks += 1;
                    Set<String> found_links = get_links_on_page(current_link);
                    for(String new_link: found_links) {
                        cpt++ ;
                        if (!visited.contains(new_link)) {
                            if(!new_link.startsWith(start_url)) {
                                int x = check_link(new_link);
                                System.out.println(new_link);
                                visited.add(new_link);
                                if(x==0) {
                                    brokenLinks+=1;
                                    reportData += "\n Url is broken " + new_link + " sur la page "+current_link;
                                    System.out.println("\n Url is broken " + new_link);
                                }
                                else{
                                    validLinks+=1;
                                }
                            }
                            if(!m.find() || !m2.find()) {
                                to_visit.push(new_link);
                            }
                        }
                    }
                } else if (response == 0) {
                    brokenLinks += 1;
                    System.out.println("\n Url is broken " + current_link);
                    reportData += "\n Url is broken " + current_link;
                }
                System.out.println(validLinks+"ValidLinks");
                System.out.println(brokenLinks+"brokenLinks");
            }
            else {
                skippedLinks += 1;
                //System.out.println("\n Url skipped " + current_link);

            }
        }
        System.out.format("Finished! (%2d empty) (%2d skipped) (%2d broken) (%2d valid)", emptyLinks, skippedLinks, brokenLinks, validLinks);
    }


}