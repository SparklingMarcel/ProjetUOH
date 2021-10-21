package src;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import src.InspectWebLinks;

public class Controller {
    @FXML
    private void lancer(ActionEvent event) {
        InspectWebLinks.main();
    }
}
