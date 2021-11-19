package src;


import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Controller extends Application {

    private static Controller controller; // Singleton

    @FXML
    private Button rapport;
    @FXML
    private Button launchButton;
    @FXML
    private ToggleGroup group;
    @FXML
    private RadioButton texte;

    public RadioButton getTexte() {
        return texte;
    }

    public Button getRapport() {
        return rapport;
    }

    public ProgressBar getProgBar() {
        return progBar;
    }

    @FXML
    private ProgressBar progBar;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private CheckBox cert;
    private TextFlow text;

    @FXML
    private void lancer() { // fonction lancé quand on appuie sur le bouton " lancer " de l'interface graphique
        progBar.setVisible(true); // on rend la barre de progression visible
        InspectWebLinks.launch(); // on lance l'application
        cert.setDisable(true);
        launchButton.setDisable(true);
    }

    @FXML
    public void initialize() {
        text = new TextFlow(); // Le texte qui sera affiché dans l'interface graphique
        text.setTextAlignment(TextAlignment.CENTER);
        scrollPane.setContent(text);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        addToolTip();
    }


    public boolean checkCheck() {
        return cert.isSelected();
    }

    public void setProgBar(float val) {
        this.progBar.setProgress(val);
    }

    private void addToolTip() {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setText("""
                Cochez cette case pour autoriser la vérification de tous les sites
                cela autorise la vérification des sites à certificats invalides
                """); // affichage de la tooltip quand on la survole
        cert.setTooltip(tooltip); // on ajoute la tooltip à la checkbox
    }

    public static Controller getInstance() { // Singleton
        return controller;
    }

    private static void control(Controller c) {
        controller = c ;
    }
    public Controller() {
        control(this);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fm = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = fm.load(); // on récupère le visuel dans le fichier sample.fxml
        Scene s = new Scene(root, 1200, 600);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(Controller.class.getResourceAsStream("main/logoColor-1.png")))); // ajout de l'icone
        primaryStage.setResizable(false); // on empêche l'utilisateur de changer la taille de l'application
        // quand on ferme l'application
        primaryStage.setOnCloseRequest(windowEvent -> {
            try {
                if (InspectWebLinks.getF() != null)
                    InspectWebLinks.getF().close(); // On ferme le fichier de sauvegarde temporaire

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (InspectWebLinks.isRap()) {
                new File(InspectWebLinks.getPath()).delete(); // On supprime le fichier de sauvegarde temporaire si on a créé un txt ou csv
            }
        });
        primaryStage.setTitle("UOH Liens Morts"); // Titre de l'application
        primaryStage.setScene(s);
        primaryStage.show();
    }

    @FXML
    private void writeRap() { // lance writeRapport de InspectWebLinks quand on clique sur le boutton rapport
        InspectWebLinks.writeRapport();
    }

    public synchronized void addNode(String link1, String link2, boolean certif) { // Permet de mettre à jour l'interface graphique
        // avec les sites qui renvoie un message d'erreur ou de certificat invalide
        Platform.runLater(() -> {
            HostServices service = Controller.getInstance().getHostServices();
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
                    InspectWebLinks.getF().write("\n" + brok1 + link1 + brok2 + link2 + "\n"); // on écrit dans un fichier temporaire les liens
                } catch (IOException e) {
                    e.printStackTrace();
                }
                text.getChildren().add(new Text(brok1 + "\n"));
                text.getChildren().add(h1);
                text.getChildren().add(new Text("\n" + brok2 + "\n"));
                text.getChildren().add(h2); // on ajoute les textes et les liens à l'interface graphique
            } else { // Si c'est un problème de certificat
                try {
                    InspectWebLinks.getF().write("\n" + cert1 + link1 + brok2 + link2 + "\n");
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
}
