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
    static String start_url = "https://uoh.fr/front/resultatsfr/";
    static int validLinks = 0;
    static int brokenLinks = 0;
    static int emptyLinks = 0;
    static int skippedLinks = 0;
    static String reportData = "";
    private static int nbPage = 0 ;

    public static void main(String[] args) throws IOException {
        getNbPage();
        inspect();
        generate_report(reportData,"C:\\Users\\Etu\\Desktop\\javaDeadLink\\report.txt");

    }


    private static void generate_report(String reportData, String reportPath) throws IOException {

        FileWriter report = new FileWriter(reportPath);
        report.write(reportData);
        report.close();

    }

    private static void getNbPage() throws  IOException {
        Document doc = Jsoup.connect(start_url).userAgent("Mozilla").get();
        Elements nbRes = doc.select("span");

        for (Element l : nbRes) {

            Pattern p = Pattern.compile("<span class=\"nb-resultats\">");
            Matcher m = p.matcher(l.toString());
            if(m.find()) {
                String afterSpan = l.toString().split(">")[1];
                nbPage =  (Integer.parseInt(afterSpan.split(" ")[0])/9)+1;
                return ;
            }
        }

    }


    private static int check_link(String url) {

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

    private static boolean verifLink(String current_link) {
        Pattern p = Pattern.compile("uoh\\.fr/front/resultatsfr/.*query=");
        Pattern p2 = Pattern.compile("uoh\\.fr/front/resultatsfr/\\?query=.*");
        Pattern estamp = Pattern.compile("uoh\\.fr/front/resultatsfr/#.*");
        Pattern estamp2 = Pattern.compile("uoh\\.fr/front/resultatsfr/.*#");
        Matcher m = p.matcher(current_link);
        Matcher m2 = p2.matcher(current_link);
        Matcher m3 = estamp.matcher(current_link);
        Matcher m4 = estamp2.matcher(current_link);

        return !m.find() && !m2.find() && !m3.find() && !m4.find();
    }

    private static Set<String> get_links_on_page(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        Elements links = doc.select("a");

        Set<String> found_urls = new HashSet<String>();

        for (Element link: links) {
            String sub_url = link.attr("abs:href");

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


    private static void inspect() throws IOException {

        Set<String> visited = new HashSet<String>();
        Stack<String> to_visit = new Stack<String>();
        to_visit.push(start_url);
        int cpt = 0 ;
        while (cpt <= nbPage) {
            to_visit.push(start_url+"?query&pagination="+cpt+"&sort=score");
            System.out.println(start_url+"?query&pagination"+cpt+"&sort=score\"");
            cpt++;
            while (!to_visit.isEmpty()) {
                String current_link = to_visit.pop();

                if (!visited.contains(current_link) && current_link.startsWith(start_url) && verifLink(current_link)) {
                    System.out.print(".");
                    System.out.println(current_link);
                    int response = check_link(current_link);
                    visited.add(current_link);
                    if (response == 1) {
                        validLinks += 1;
                        Set<String> found_links = get_links_on_page(current_link);
                        for (String new_link : found_links) {
                            if (!visited.contains(new_link)) {
                                if (!new_link.startsWith(start_url)) {
                                    int x = check_link(new_link);
                                    System.out.println(new_link);
                                    visited.add(new_link);
                                    if (x == 0) {
                                        brokenLinks += 1;
                                        reportData += "\n Url is broken " + new_link + " sur la page " + current_link;
                                        System.out.println("\n Url is broken " + new_link);
                                    } else {
                                        validLinks += 1;
                                    }
                                }
                                if (verifLink(new_link)) {
                                    to_visit.push(new_link);
                                }
                            }
                            to_visit.push(new_link);
                        }
                    } else if (response == 0) {
                        brokenLinks += 1;
                        System.out.println("\n Url is broken " + current_link);
                        reportData += "\n Url is broken " + current_link;
                    }
                    System.out.println(validLinks + "ValidLinks");
                    System.out.println(brokenLinks + "brokenLinks");
                } else {
                    skippedLinks += 1;
                    //System.out.println("\n Url skipped " + current_link);

                }
            }
            System.out.format("Finished! (%2d empty) (%2d skipped) (%2d broken) (%2d valid)", emptyLinks, skippedLinks, brokenLinks, validLinks);
        }
    }


}