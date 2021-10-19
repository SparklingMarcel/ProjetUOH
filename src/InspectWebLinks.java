package src ;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
    static int certifLinks = 0 ;
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
        Pattern p = Pattern.compile(".*\\.pdf");
        Matcher m = p.matcher(url);
        if(m.find()) {
            return 1;
        }
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
            if(e.getMessage().equals("received handshake warning: unrecognized_name")) {
                return 2;
            }

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

    private static HashMap<String,String> get_links_on_page(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        Elements links = doc.select("a");
        Elements links2 = doc.select("div.carte-notice-liens-footer");
        Pattern p = Pattern.compile("href=\".*?>");
        HashMap<String,String> found_url = new HashMap<String,String>();
        int cpt = 0 ;
        for(Element link2 : links2) {
            Matcher m = p.matcher(link2.toString());
            if(m.find()) {
                String s1 = m.group().subSequence(6, m.group().length()-2).toString();
                if(s1.charAt(0)!='h') {
                    s1="http://"+s1;
                }
                if(m.find()){
                    String s2 = m.group().subSequence(6, m.group().length()-2).toString();
                    if(s2.charAt(0)!='h') {
                        s2= "http://"+s2;
                    }
                    found_url.put(s1,s2);
                }


            }

        }
        /*
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
        */

        return found_url;
    }


    private static void inspect() throws IOException {

        Set<String> visited = new HashSet<String>();
        Stack<String> to_visit = new Stack<String>();
        to_visit.push(start_url);
        int cpt = 0 ;
        while (cpt <= nbPage) {
            to_visit.push(start_url+"?query&pagination="+cpt+"&sort=score");
            System.out.println(start_url+"?query&pagination"+cpt+"&sort=score\"");
            System.out.println("-------------------------------------------------");
            cpt++;
            while (!to_visit.isEmpty()) {
                String current_link = to_visit.pop();
                if (!visited.contains(current_link) && current_link.startsWith(start_url)) {
                    System.out.print(".");
                    System.out.println(current_link);
                    int response = check_link(current_link);
                    visited.add(current_link);
                    if (response == 1) {
                        validLinks += 1;
                        HashMap<String,String> found_links = get_links_on_page(current_link);
                        for (String new_link : found_links.keySet()) {
                            if (!visited.contains(new_link)) {
                                int x = check_link(new_link);
                                System.out.println(new_link);
                                visited.add(new_link);
                                if (x == 0) {
                                    brokenLinks += 1;
                                    reportData += "\n Le site renvoie un message d'erreur " + new_link + " sur la page " + found_links.get(new_link);
                                    System.out.println("\n Url is broken " + new_link+ " sur la page " + found_links.get(new_link));
                                } else {
                                    validLinks += 1;
                                }

                                if (verifLink(new_link)) {
                                    to_visit.push(new_link);
                                }
                            }
                            to_visit.push(new_link);
                        }
                    } else if (response == 0) {
                        brokenLinks += 1;
                        System.out.println("\n La page UOH " + current_link + " est down");
                        reportData += "\n La page UOH " + current_link + " est down";
                    }
                      else if (response == 2)  {
                          certifLinks+=1;
                          System.out.println("certificat invalide");
                          reportData += "\n le certificat du site n'est pas valide, il faut vérifier le site manuellement"+current_link;
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