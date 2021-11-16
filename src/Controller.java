package src;


import javafx.fxml.FXML;



public class Controller {
    @FXML
    private void lancer() { // fonction lancé quand on appuie sur le bouton " lancer " de l'interface graphique
        UOHinterface.progressBar.setVisible(true); // on rend la barre de progression visible
        InspectWebLinks.launch(); // on lance l'application
    }

    @FXML
    private void writeRap() { // lance writeRapport de InspectWebLinks quand on clique sur le boutton rapport
        InspectWebLinks.writeRapport();
    }
}
