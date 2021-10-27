package src;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javafx.stage.FileChooser;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import src.main.multiInspect;

import javax.net.ssl.SSLPeerUnverifiedException;

import static src.UOHinterface.*;

public class InspectWebLinks implements Runnable {

    private static final String path = System.getProperty("user.dir") + File.separator + "report.txt";
    private static final String start_url = "https://uoh.fr/front/resultatsfr/";
    private static boolean rap = false;
    private static FileWriter f;
    private static int nbPage = 0;
    private static int nbInt = 0;
    private static int nbThreadFinish;
    private int id;

    public InspectWebLinks(int id) {
        this.id = id;
    }

    public static String getPath() {
        return path;
    }

    public static boolean isRap() {
        return rap;
    }

    public static FileWriter getF() {
        return f;
    }

    public static void main(String[] args) {
        launch();
    }

    public static void writeRapport() {


        try {
            f.close();
            rap = true;
            RadioButton s = (RadioButton) root.lookup("#texte");
            File selectedFile ;
            if (s.isSelected()) {
                selectedFile = chooseFileType(true);
                BufferedReader bf = new BufferedReader(new FileReader(path));
                FileWriter bo = new FileWriter(selectedFile);
                String su = "";
                while ((su = bf.readLine()) != null) {
                    System.out.println(su);
                    bo.write(su + "\n");
                }
                bo.close();
                bf.close();
                new File(path).delete();
            } else {
                selectedFile = chooseFileType(false);
                BufferedReader bf = new BufferedReader(new FileReader(path));
                FileWriter bo = new FileWriter(selectedFile);
                String su = "";
                while ((su = bf.readLine()) != null) {
                    System.out.println(su);
                    bo.write(su + "\n");
                }
                bo.close();
                bf.close();
                new File(path).delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static File chooseFileType(Boolean txt) {
        final FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter ;
        if(txt) {
            extFilter = new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt");
        }
        else {
            extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        }
        chooser.getExtensionFilters().add(extFilter);
        File selected = null ;
        while(selected==null) {
            selected =chooser.showSaveDialog(stage);
        }
        return  selected;
    }

    public static void launch() {
        try {

            bl.setDisable(true);
            f = new FileWriter(path);
            getNbPage();
            multiInspect.main();


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
        Elements links2 = doc.select("div.carte-notice-liens-footer");

        Elements nbRes = doc.select("span");

        for (Element l : nbRes) {
            Pattern p = Pattern.compile("<span class=\"nb-resultats\">");
            Matcher m = p.matcher(l.toString());
            if (m.find()) {
                String afterSpan = l.toString().split(">")[1];
                nbPage = (Integer.parseInt(afterSpan.split(" ")[0]) / links2.size()) + 1;
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
        } catch (SSLPeerUnverifiedException e) {
            System.out.println("BLABLABLABLA");
            return 2;

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.out.println("BLBLBLBLBLBLL");
            if (e.getMessage().equals("received handshake warning: unrecognized_name") || e.getMessage().equals("PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target")) {
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
    private static void inspect(int cptStart) throws IOException {
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
                            pb.setProgress(((float) nbInt / nbPage));
                            System.out.println(pb.getProgress());
                            nbInt++;
                            System.out.println(current_link);
                            System.out.println(nbInt);
                            System.out.println("-------------------------------------------------");
                            HashMap<String, String> found_links = get_links_on_page(current_link);
                            for (String new_link : found_links.keySet()) {
                                System.out.println(new_link);
                                if (!new_link.startsWith("https://uoh.fr")) {
                                    int x = check_link(new_link);
                                    String fd = found_links.get(new_link);
                                    if (fd.equals("")) {
                                        fd = current_link;
                                    }
                                    if (x == 0) {
                                        System.out.println("-------------------------------");
                                        addNode(new_link, fd, true);
                                    } else if (x == 2) {
                                        addNode(new_link, fd, false);
                                    }
                                }
                            }
                            current_link = start_url + "?query&pagination=" + cpt + "&sort=score";
                        }
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
                                nbThreadFinish++;
                                if (nbThreadFinish == 4) {
                                    pb = (ProgressBar) UOHinterface.root.lookup("#progBar");
                                    pb.setVisible(false);
                                    Button but = (Button) root.lookup("#rapport");
                                    but.setDisable(false);
                                }
                        }
                    }
                });
        calculateLink.start();
    }


    public synchronized static void addNode(String link1, String link2, boolean certif) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                HostServices service = UOHinterface.getInstance().getHostServices();
                String brok1 = " Le site renvoie un message d'erreur ";
                String brok2 = " Sur la page ";
                String cert1 = "Le site suivant doit être vérifié manuellement :";


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
                    try {
                        f.write("\n" + brok1 + link1 + brok2 + link2 + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    text.getChildren().add(new Text("Le site suivant est down:\n"));
                    text.getChildren().add(h1);
                    text.getChildren().add(new Text("\nsur la page:\n"));
                    text.getChildren().add(h2);
                    System.out.println("ECRITICI---------------------------------");
                    System.out.println(("\n" + brok1 + link1 + brok2 + link2 + "\n"));
                } else {
                    try {
                        f.write("\n" + cert1 + " " + link1 + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    text.getChildren().add(new Text(cert1 + "\n"));
                    text.getChildren().add(h1);
                }
                text.getChildren().add(new Text("\n--------------------------------------\n"));

            }
        });
    }

    @Override
    public synchronized void run() {
        try {
            inspect(this.id);
        } catch (Exception e) {

        }
    }
}



