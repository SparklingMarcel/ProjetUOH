package src;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import src.main.MultiInspect;
import javax.net.ssl.*;

import static src.UOHinterface.*;

public class InspectWebLinks implements Runnable {

    private static final String path = System.getProperty("user.dir") + File.separator + "report.txt";
    private static final String start_url = "https://uoh.fr/front/resultatsfr/";
    private static final String moz = "Mozilla";
    private static final String http = "http://";
    private static boolean rap = false;
    private static FileWriter f;
    private static int nbPage = 0;
    private static int nbInt = 0;
    private static int nbThreadFinish;
    private final int id;

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

    /**
     * Permet de créer et d'écrire le fichier final en fonction du choix ( txt ou CSV )
     */

    public static void writeRapport() {
        try {
            f.close();
            rap = true;
            // Création du radio button
            RadioButton s = (RadioButton) root.lookup("#texte");
            File selectedFile;
            // On regarde quel est le type choisi par l'user avec isSelected()
            if (s.isSelected()) { // Si l'utilisateur a choisi TXT
                selectedFile = chooseFileType(true); // Selection du dossier de sauvegarde du fichier
                if (selectedFile == null) {
                    return;
                }
                try (BufferedReader bf = new BufferedReader(new FileReader(path))) {
                    try (FileWriter bo = new FileWriter(selectedFile)) {
                        String su;
                        //On écrit dans le fichier
                        while ((su = bf.readLine()) != null) {
                            bo.write(su + "\n");
                        }
                    }
                }
            } else { // Si l'utilisateur a choisi CSV

                selectedFile = chooseFileType(false);
                if (selectedFile == null) {
                    return;
                }
                try (BufferedReader bf2 = new BufferedReader(new FileReader(path))) {
                    try (FileWriter bo2 = new FileWriter(selectedFile)) {
                        String su;

                        while ((su = bf2.readLine()) != null) {
                            bo2.write(su + "\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initCert() { // Gestion des certificats , autorisation de tous les certificats SSL uniquement pendant l'execution du programme

        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Ne rien faire pour autoriser tous les certificats
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Ne rien faire pour autoriser tous les certificats

                    }
                }
        };
        HostnameVerifier trustAllHostnames = (hostname, session) -> true;

        try {
            System.setProperty("jsse.enableSNIExtension", "false"); // On désactive la vérification des certificats par Java
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCertificates, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    /**
     * @param txt Boolean
     * @return File un fichier du type choisir par l'user (csv ou txt)
     */
    private static File chooseFileType(Boolean txt) {

        final FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter;

        if (txt) { // Si c'est l'utilisateur a choisit le format texte, on force l'utilisateur à créer un fichier de type texte, sinon CSV
            extFilter = new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt");
        } else {
            extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        }

        chooser.getExtensionFilters().add(extFilter);

        return chooser.showSaveDialog(stage); // Affichage de la fenêtre pour choisir le répertoire de création du fichier
    }

    /**
     * launch() lance le programme principal
     */
    public static void launch() {
        try {
            if (checkBox.isSelected()) { // Si le bouton de gestion des certificats est coché
                initCert();
            }
            checkBox.setDisable(true);
            launchButton.setDisable(true);
            f = new FileWriter(path);
            getNbPage();
            MultiInspect.main();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getNbPage récupère le nombre de page à analyser sur le site
     */
    private static void getNbPage() { // récupère le nombre de page total des ressources
        Document doc = null;
        try {
            doc = Jsoup.connect(start_url).userAgent(moz).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert doc != null;
        Elements links2 = doc.select("div.carte-notice-liens-footer"); // On recupère le nombre de notice par page
        Elements spanRes = doc.select("span.nb-resultats"); // On recupère le nombre de ressource total
        String afterSpan = spanRes.get(0).toString().split(">")[1];
        nbPage = (Integer.parseInt(afterSpan.split(" ")[0]) / links2.size()) + 1; // on calcul le nombre de page
    }



    /**
     * check_link vérifie si un lien est mort ou non
     *
     * @param url lien à vérifier
     * @return 0 ( lien mort ) ou 1 (lien non mort) ou 2 (lien à vérifier)
     */
    private static int check_link(String url) { // On vérifie si un lien renvoie un message d'erreur

        Response response;
        try {
            response = Jsoup.connect(url).execute();
            if (response.statusCode() == 404) {
                return 0;
            } else {
                return 1;
            }
        } catch (IOException e) {
            // On retourne 2 si le certificat est invalide ( dans le cas ou on a pas coché la checkbox)
            if (e.getMessage().equals("received handshake warning: unrecognized_name") || e.getMessage().equals("PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target")) {
                return 2;
            }
            return 0;
        }
    }


    private static HashMap<String, String> get_links_bottom_once() { // Récupére les liens en bas de page ( dans la section élargissez votre recherche
        Document doc = null;
        HashMap<String, String> hm = new HashMap<>();
        try {
            doc = Jsoup.connect(InspectWebLinks.start_url).userAgent(moz).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert doc != null;
        String s1;
        Elements links = doc.select("div.resultats-catalogue-site"); // On récupère la div élargissez votre recherche
        Pattern p = Pattern.compile("href=\".*?\""); // on cherche les liens ( href ) à l'intérieur
        for (Element l : links) {
            Matcher m = p.matcher(l.toString());
            if (m.find()) {
                s1 = m.group().subSequence(6, m.group().length() - 1).toString();
                if (!s1.startsWith("http")) { // si le site ne commence pas par http
                    s1 = http + s1;
                }
                hm.put(s1, "");
            }
        }
        return hm;
    }


    /**
     * get_links_on_page récupère tous les liens présents sur une page
     *
     * @param url url de la page où on récupère les liens
     * @return un hashMap contenant le lien de la ressource externe associée au lien de la notice
     */
    private static HashMap<String, String> get_links_on_page(String url) { // Récupère les liens des cartes notice sur chaque page une par une
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent(moz).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert doc != null;
        Elements links2 = doc.select("div.carte-notice-liens-footer"); // on recupère tous les footers de chacune des notices
        Pattern p = Pattern.compile("href=\".*?>"); // on recupère les liens dedans
        HashMap<String, String> found_url = new HashMap<>();
        String s1, s2 = "";
        for (Element link2 : links2) { // pour tous les footers différents des cartes notices
            Matcher m = p.matcher(link2.toString());
            if (m.find()) { // Si on trouve un premier lien, on le récupère ( ressource externe )
                s1 = m.group().subSequence(6, m.group().length() - 2).toString();
                if (!s1.startsWith("http")) {
                    s1 = http + s1;
                }
                if (m.find()) { // Si on trouve un deuxième lien ( le lien de la notice correspondant à la ressource )
                    s2 = m.group().subSequence(6, m.group().length() - 2).toString();
                    if (!s2.startsWith("http")) {
                        s2 = http + s2;
                    }
                    s2 = s2.replace("?lang=fr&amp;", "/?");

                }
                found_url.put(s1, s2); // on retourne une hashmap avec la ressource externe et la notice qui lui correspond
            }

        }
        return found_url;
    }

    /**
     * inspect
     *
     * @throws IOException
     */
    private static void inspect(int cptStart) {  // Permet de vérifier chaque lien et de faire ce qu'il faut en conséquence
        final Service<Void> calculateLink = new Service<>() { // On utilise un thread différent de l'interface graphique
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        int cpt = (nbPage / MultiInspect.getNbThread()) * cptStart; // On assigne à chaque thread sa page de départ
                        if (cptStart != 0) {
                            cpt++; // comme c'est une division entière, il faut assigner une page plus loin pour les thread
                        }
                        int cptMax;
                        String current_link = start_url + "?query&pagination=" + cpt + "&sort=score"; // on assigne leur page de départ
                        if (cptStart == MultiInspect.getNbThread() - 1) {
                            cptMax = nbPage; // si c'est le dernier thread, il s'arrête au nombre de page
                        } else {
                            cptMax = (nbPage / MultiInspect.getNbThread()) * (cptStart + 1); // on assigne la dernière page que doit vérifier chaque thread
                        }
                        while (cpt <= cptMax) {
                            HashMap<String, String> found_links = get_links_on_page(current_link);
                            if (cpt == 0) {
                                found_links.putAll(get_links_bottom_once()); // on donne au premier thread les liens en bas de page ( une fois seulement )
                            }
                            cpt++;
                            progressBar.setProgress(((float) nbInt / nbPage)); // on fait progresser la bar de progression
                            nbInt++; // nombre de page vérifié
                            for (Map.Entry<String, String> links : found_links.entrySet()) {
                                String new_link = links.getKey() ;
                                String fd = links.getValue();
                                if (!new_link.startsWith("https://uoh.fr")) {
                                    int x = check_link(new_link); // 2 si problème de certificat, 0 si le site renvoi un message d'erreur
                                    if (fd.equals("")) { // si le site externe n'est pas associé à une page notice, on renvoie la page sur laquelle il est
                                        fd = current_link;
                                    }
                                    if (x == 0) {
                                        addNode(new_link, fd, true); // si le site renvoie un message d'ereur on lance addNode avec certification bonne
                                    } else if (x == 2) {
                                        addNode(new_link, fd, false); // Sinon, si la certif du site est mauvaise, on lance addNode avec certification fausse
                                    }
                                }
                            }
                            current_link = start_url + "?query&pagination=" + cpt + "&sort=score"; // on assigne la prochaine page aux threads
                        }
                        return null;
                    }
                };
            }
        };

        calculateLink.stateProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED || newValue == Worker.State.SUCCEEDED) {
                nbThreadFinish++; // on compte le nombre de thread qui ont fini
                if (nbThreadFinish == MultiInspect.getNbThread()) {
                    progressBar.setVisible(false); // on désactive la  bar de progression
                    Button but = (Button) root.lookup("#rapport");
                    but.setDisable(false); // On active le boutton pour le rapport
                }
            }
        });
        calculateLink.start(); // lancement du thread de inspect
    }



    /**
     * addNode rajoute les liens mort dans l'interface graphique et les écrit dans le fichier
     *
     * @param link1
     * @param link2
     * @param certif
     */
    public synchronized static void addNode(String link1, String link2, boolean certif) { // Permet de mettre à jour l'interface graphique
        // avec les sites qui renvoie un message d'erreur ou de certificat invalide
        Platform.runLater(() -> {
            HostServices service = UOHinterface.getInstance().getHostServices();
            String brok1 = "Le site renvoie un message d'erreur ";
            String brok2 = " sur la page : ";
            String cert1 = "Le site suivant doit être vérifié manuellement : ";
            Hyperlink h1 = new Hyperlink(link1); // lien du site externe
            Hyperlink h2 = new Hyperlink(link2); // lien de la notice rattaché
            List<Hyperlink> list = new ArrayList<>();
            list.add(h1);
            list.add(h2);

            for (final Hyperlink hyperlink : list) {
                // permet d'afficher des liens clickable qui ramènent sur internet
                hyperlink.setOnAction(t -> service.showDocument(hyperlink.getText()));
            }
            if (certif) { // si ce n'est pas un problème de certificat
                try {
                    f.write("\n" + brok1 + link1 + brok2 + link2 + "\n"); // on écrit dans un fichier temporaire les liens
                } catch (IOException e) {
                    e.printStackTrace();
                }
                text.getChildren().add(new Text(brok1 + "\n"));
                text.getChildren().add(h1);
                text.getChildren().add(new Text("\n" + brok2 + "\n"));
                text.getChildren().add(h2); // on ajoute les textes et les liens à l'interface graphique
            } else { // Si c'est un problème de certificat
                try {
                    f.write("\n" + cert1 + link1 + brok2 + link2 + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                text.getChildren().add(new Text(cert1 + "\n"));
                text.getChildren().add(h1);
                text.getChildren().add(new Text("\n" + brok2 + "\n"));
                text.getChildren().add(h2);
            }
            text.getChildren().add(new Text("\n--------------------------------------\n"));
        });
    }

    @Override
    public synchronized void run() { // lancement des threads
        try {
            inspect(this.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



