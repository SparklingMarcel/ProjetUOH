package projectuoh.demo;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.File;
import java.io.IOException;

public class UOHinterface extends Application {
    private static UOHinterface mInstance;
    public static ScrollPane p ;
    public static Stage stage ;
    public static Parent root ;
    public static TextFlow text ;
    public static ProgressBar pb ;
    public static Button bl ;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        System.out.println((getClass().getResource("projectuoh/demo/sample.fxml")));
        FXMLLoader fxmlLoader = new FXMLLoader(UOHinterface.class.getResource("sample.fxml"));
        System.out.println("test");
        root = fxmlLoader.load();
        pb = (ProgressBar) root.lookup("#progBar");
        bl = (Button) root.lookup("#bl");
        Scene s = new Scene(root,1200,600) ;
        text = new TextFlow();
        text.setTextAlignment(TextAlignment.CENTER);
        p = (ScrollPane)root.lookup("#myTxtID");
        p.setContent(text);
        p.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        p.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        stage = primaryStage ;
        System.out.println(p.getLayoutX());
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    if(InspectWebLinks.getF()!=null)
                        InspectWebLinks.getF().close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(InspectWebLinks.isRap()) {
                    new File(InspectWebLinks.getPath()).delete();
                }
            }
        });
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