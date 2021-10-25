package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

public class UOHinterface extends Application {
    private static UOHinterface mInstance;
    public static ScrollPane p ;
    public static Scene s ;
    public static Stage stage ;
    public static Parent root ;
    public static VBox pane ;
    public static TextFlow text ;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        System.out.println((getClass().getResource("sample.fxml")));
        root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        s = new Scene(root,800,600) ;
        text = new TextFlow();
        p = (ScrollPane)root.lookup("#myTxtID");
        pane = new VBox() ;
        pane.getChildren().add(text);
        p.setContent(pane);
        stage = primaryStage ;
        System.out.println(p.getLayoutX());
        primaryStage.setTitle("UOH Liens Morts");
        primaryStage.setScene(s);
        primaryStage.show();
    }


    public static UOHinterface getInstance() {
        if(mInstance == null ) {
            mInstance = new UOHinterface();
        }
        return mInstance ;
    }


}