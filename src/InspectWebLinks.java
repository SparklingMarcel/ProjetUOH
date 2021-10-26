package src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.text.Text;
>>>>>>> Stashed changes
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import src.main.multiInspect;

import static src.UOHinterface.*;

public class InspectWebLinks implements Runnable {
    static String path = System.getProperty("user.dir") + File.separator + "report.txt";
    static String pathCsv = System.getProperty("user.dir") + File.separator + "report.csv";
>>>>>>> Stashed changes
    static String start_url = "https://uoh.fr/front/resultatsfr/";
    static int validLinks = 0;
    static int brokenLinks = 0;
    static int emptyLinks = 0;
    static int skippedLinks = 0;
    static int certifLinks = 0;
<<<<<<< Updated upstream
=======
    static boolean rap = false;
>>>>>>> Stashed changes
    static FileWriter f;
    public static int nbPage = 0;
    public static int nbInt = 0;
    int id;

    public InspectWebLinks(int id) {
        this.id = id;
    }

<<<<<<< Updated upstream
    public static void main(String args[]) {
=======
    public static void main(String[] args) {
        launch();
    }

    public static void writeRapport() {

        try {
            rap = true;
            RadioButton s = (RadioButton) root.lookup("#texte");
            f.close();
            if (s.isSelected()) {
                return;
            } else {
                BufferedReader bf = new BufferedReader(new FileReader(path));
                FileWriter bo = new FileWriter(pathCsv);
                String su = "";
                while ((su = bf.readLine()) != null) {
                    System.out.println(su);
                    bo.write(su);
                }
                bo.close();
                bf.close();
                new File(path).delete();

            }

            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void launch() {
>>>>>>> Stashed changes
        try {
            String path = System.getProperty("user.dir") + File.separator + "report.txt";
            System.out.println(path);
            f = new FileWriter(path);
            getNbPage();
<<<<<<< Updated upstream
            inspect();
            f.close();
=======
            multiInspect.main();


>>>>>>> Stashed changes
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
                }
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
     * @return 0 ( lien mort ) ou 1 (lien non mort) ou 2 (lien à vérifier)
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
            if (e.getMessage().equals("received handshake warning: unrecognized_name") || e.getMessage().equals("PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested")) {
                return 2;
            }
            return 0;
        }
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
<<<<<<< Updated upstream
    private static void inspect() throws IOException {
        String current_link = start_url ;
        int cpt = 0;
        while (cpt <= nbPage) {
            cpt++;
            System.out.println(current_link);
            System.out.println("-------------------------------------------------");
            HashMap<String, String> found_links = get_links_on_page(current_link);
            for (String new_link : found_links.keySet()) {
                System.out.println(new_link);
                int x = check_link(new_link);
                if (x == 0) {
                    String fd = found_links.get(new_link);
                    if (fd.equals("")) {
                        fd = current_link;
                    }
                    brokenLinks += 1;
                    f.write("\n Le site renvoie un message d'erreur " + new_link + " sur la page " + fd + "\n");
                } else if (x == 2) {
                    certifLinks += 1;
                    System.out.println("certificat invalide");
                    f.write("\n le certificat du site n'est pas valide, il faut vérifier le site manuellement ou il s'agit d'un pdf à vérifier " + new_link + "\n");
                }
=======
    private synchronized static void inspect(int cptStart) throws IOException {
        final Service<Void> calculateLink = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {


                        int cpt = (nbPage / 4) * cptStart;
                        if (cptStart != 0) {
                            cpt++;
                        }
                        int cptMax;
                        String current_link = start_url + "?query&pagination=" + cpt + "&sort=score";
                        if (cptStart == 3) {
                            cptMax = nbPage;
                        } else {
                            cptMax = (nbPage / 4) * (cptStart + 1);
                        }
                        while (cpt <= cptMax) {
                            cpt++;
                            nbInt++;
                            System.out.println(current_link);
                            System.out.println(nbInt);
                            System.out.println("-------------------------------------------------");
                            HashMap<String, String> found_links = get_links_on_page(current_link);
                            for (String new_link : found_links.keySet()) {
                                System.out.println(new_link);
                                int x = check_link(new_link);
                                String fd = found_links.get(new_link);
                                if (fd.equals("")) {
                                    fd = current_link;
                                }
                                if (x == 0) {

                                    brokenLinks += 1;
                                    String txt2 = " Le site renvoie un message d'erreur " + new_link + " sur la page " + fd;
                                    System.out.println("-------------------------------");
                                    addNode(new_link, fd, true);
                                    try {
                                        f.write("\n" + txt2 + "\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("test");

                                } else if (x == 2) {
                                    certifLinks += 1;
                                    String txt = "le certificat du site n'est pas valide, il faut vérifier le site manuellement ou il s'agit d'un pdf à vérifier ";
                                    System.out.println("certificat invalide");
                                    try {
                                        f.write("\n" + txt + " " + new_link + "\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("test");
                                    addNode(new_link, fd, false);


                                }

                            }
                            current_link = start_url + "?query&pagination=" + cpt + "&sort=score";
                        }
                        Button b = (Button) root.lookup("#rapport");
                        b.setDisable(false);
                        System.out.println("avant le return null");
                        return null;
                    }
                };
            }
        };

        calculateLink.stateProperty().

                addListener(new ChangeListener<Worker.State>() {

                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State
                            oldValue, Worker.State newValue) {
                        switch (newValue) {
                            case FAILED:
                            case CANCELLED:
                            case SUCCEEDED:
                                ProgressBar b = (ProgressBar) UOHinterface.root.lookup("#progBar");
                                b.setVisible(false);
                        }
                    }
                });
        calculateLink.start();
    }


    public static void addNode(String link1, String link2, boolean certif) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                HostServices service = UOHinterface.getInstance().getHostServices();
                Hyperlink h1 = new Hyperlink(link1);
                Hyperlink h2 = new Hyperlink(link2);
                List<Hyperlink> list = new ArrayList<>();
                list.add(h1);
                list.add(h2);

                for (final Hyperlink hyperlink : list) {
                    hyperlink.setOnAction(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            service.showDocument(hyperlink.getText());
                        }
                    });
                }
                if (certif) {
                    text.getChildren().add(new Text("Le site suivant est down:\n"));
                    text.getChildren().add(h1);
                    text.getChildren().add(new Text("\nsur la page:\n"));
                    text.getChildren().add(h2);
                } else {
                    text.getChildren().add(new Text("Le site suivant doit être vérifié manuellement :\n"));
                    text.getChildren().add(h1);
                }
                text.getChildren().add(new Text("\n--------------------------------------\n"));

>>>>>>> Stashed changes
            }
            current_link = start_url + "?query&pagination=" + cpt + "&sort=score";
        }
        System.out.format("Finished! (%2d empty) (%2d skipped) (%2d broken) (%2d valid)", emptyLinks, skippedLinks, brokenLinks, validLinks);
    }

    @Override
    public synchronized void run() {
        try {
            inspect(this.id);
        } catch (Exception e) {

        }
    }
}



