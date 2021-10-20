package src;

import java.io.File;
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
    static int certifLinks = 0;
    static FileWriter f;
    private static int nbPage = 0;

    public static void main(String[] args) {
        try {

            String path = System.getProperty("user.dir") + File.separator + "report.txt";
            System.out.println(path);
            f = new FileWriter(path);


        } catch (IOException e) {
            e.printStackTrace();
        }
        getNbPage();
        try {
            inspect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getNbPage récupère le nombre de page à analyser sur le site
     */
    private static void getNbPage() {
        Document doc = null;
        try {
            doc = Jsoup.connect(start_url).userAgent("Mozilla").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert doc != null;
        Elements nbRes = doc.select("span");

        for (Element l : nbRes) {

            Pattern p = Pattern.compile("<span class=\"nb-resultats\">");
            Matcher m = p.matcher(l.toString());
            if (m.find()) {
                String afterSpan = l.toString().split(">")[1];
                nbPage = (Integer.parseInt(afterSpan.split(" ")[0]) / 9) + 1;
                return;
            }
        }

    }

    /**
     * check_link vérifie si un lien est mort ou non
     *
     * @param url lien à vérifier
     * @return 0 ( lien mort ) ou 0 (lien non mort)
     */
    private static int check_link(String url) {
        Pattern p = Pattern.compile(".*\\.pdf$|.*\\.PDF$");
        Matcher m = p.matcher(url);
        if (m.find()) {
            return 2;
        }
        Response response;
        try {
            response = Jsoup.connect(url).execute();
            if (response.statusCode() == 404) {
                return 0;
            } else {
                return 1;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            if (e.getMessage().equals("received handshake warning: unrecognized_name") || e.getMessage().equals(
                    "PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested"
            )) {
                return 2;
            }

            return 0;
        }
    }

    /**
     * verifLink verifie si le lien doit être analysé ou non
     *
     * @param current_link lien à vérifier
     * @return true si le lien est valide et doit être analysé et false si le lien contient
     * un des pattern et ne doit pas être analysé
     */
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

    /**
     * get_links_on_page récupère tous les liens présents sur une page
     *
     * @param url url de la page où on récupère les liens
     * @return un hashMap contenant le lien de la ressource externe associée au lien de la notice
     */
    private static HashMap<String, String> get_links_on_page(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert doc != null;
        Elements links2 = doc.select("div.carte-notice-liens-footer");
        Pattern p = Pattern.compile("href=\".*?>");
        HashMap<String, String> found_url = new HashMap<>();
        String s1, s2 = "";
        for (Element link2 : links2) {
            Matcher m = p.matcher(link2.toString());
            if (m.find()) {
                s1 = m.group().subSequence(6, m.group().length() - 2).toString();
                if (s1.charAt(0) != 'h') {
                    s1 = "http://" + s1;
                }
                if (m.find()) {
                    s2 = m.group().subSequence(6, m.group().length() - 2).toString();
                    if (s2.charAt(0) != 'h') {
                        s2 = "http://" + s2;
                    }
                    s2 = s2.replace("?lang=fr&amp;", "/?");

                }
                found_url.put(s1, s2);
            }

        }
        return found_url;
    }

    /**
     * inspect
     *
     * @throws IOException
     */
    private static void inspect() throws IOException {

        Set<String> visited = new HashSet<>();
        Stack<String> to_visit = new Stack<>();
        to_visit.push(start_url);
        int cpt = 0;
        while (cpt <= nbPage) {
            to_visit.push(start_url + "?query&pagination=" + cpt + "&sort=score");
            System.out.println(start_url + "?query&pagination" + cpt + "&sort=score\"");
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
                        HashMap<String, String> found_links = get_links_on_page(current_link);
                        for (String new_link : found_links.keySet()) {
                            if (!visited.contains(new_link)) {
                                int x = check_link(new_link);
                                System.out.println(new_link);
                                visited.add(new_link);
                                if (x == 0) {
                                    String fd = found_links.get(new_link);
                                    brokenLinks += 1;
                                    f.write("\n Le site renvoie un message d'erreur " + new_link + " sur la page " + fd + "\n");
                                    System.out.println("\n Url is broken " + new_link + " sur la page " + fd);
                                } else if (x == 2) {
                                    certifLinks += 1;
                                    System.out.println("certificat invalide");
                                    f.write("\n le certificat du site n'est pas valide, il faut vérifier le site manuellement ou il s'agit d'un pdf à vérifier " + current_link + "\n");
                                } else {
                                    validLinks++;
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
                        f.write("\n La page UOH " + current_link + " est down" + "\n");
                    } else if (response == 2) {
                        certifLinks += 1;
                        System.out.println("certificat invalide");
                        f.write("\n le certificat du site n'est pas valide, il faut vérifier le site manuellement ou alors il s'agit d'un PDF à verifier" + current_link + "\n");
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