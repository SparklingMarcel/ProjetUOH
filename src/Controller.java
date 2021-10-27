package src;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleGroup;

public class Controller {
    @FXML
    ToggleGroup group;
    @FXML
    private void lancer(ActionEvent event) {
        ProgressBar b = (ProgressBar) UOHinterface.root.lookup("#progBar");
        b.setVisible(true);
        InspectWebLinks.launch();
    }

    @FXML
    private void writeRap(ActionEvent event) {
        InspectWebLinks.writeRapport();
    }
}
