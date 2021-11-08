package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class UOHinterface extends Application {
    private static UOHinterface mInstance; // Singleton
    public static ScrollPane scrollPane;
    public static Stage stage ;
    public static Parent root ;
    public static TextFlow text ;
    public static ProgressBar progressBar;
    public static Button launchButton ;
    public static CheckBox checkBox;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {


        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("sample.fxml"))); // on récupère le visuel dans le fichier sample.fxml
        progressBar = (ProgressBar) root.lookup("#progBar"); // on récupère la barre de progression
        launchButton = (Button) root.lookup("#bl"); // On recupère le bouton de lancement
        checkBox = (CheckBox) root.lookup("#cert"); // On récupère le bouton de checkbox de certification
        final Tooltip tooltip = new Tooltip(); // création de la tooltip
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setText("""
                Cochez cette case pour autoriser la vérification de tous les sites
                cela autorise la vérification des sites à certificats invalides
                """); // affichage de la tooltip quand on la survole
        checkBox.setTooltip(tooltip); // on ajoute la tooltip à la checkbox
        Scene s = new Scene(root,1200,600) ;
        text = new TextFlow(); // Le texte qui sera affiché dans l'interface graphique
        text.setTextAlignment(TextAlignment.CENTER);
        scrollPane = (ScrollPane)root.lookup("#myTxtID"); // la scrollpane
        scrollPane.setContent(text);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        stage = primaryStage ;
        System.out.println(UOHinterface.class);
        stage.getIcons().add(new Image(UOHinterface.class.getResourceAsStream("main/logoColor-1.png"))); // ajout de l'icone
        primaryStage.setResizable(false); // on empêche l'utilisateur de changer la taille de l'application
        // quand on ferme l'application
        primaryStage.setOnCloseRequest(windowEvent -> {
            try {
                if(InspectWebLinks.getF()!=null)
                    InspectWebLinks.getF().close(); // On ferme le fichier de sauvegarde temporaire

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(InspectWebLinks.isRap()) {
                new File(InspectWebLinks.getPath()).delete(); // On supprime le fichier de sauvegarde temporaire si on a créé un txt ou csv
            }
        });
        primaryStage.setTitle("UOH Liens Morts"); // Titre de l'application
        primaryStage.setScene(s);
        primaryStage.show();
    }


    public static UOHinterface getInstance() { // Singleton
        if(mInstance == null ) {
            mInstance = new UOHinterface();
        }
        return mInstance ;
    }


}