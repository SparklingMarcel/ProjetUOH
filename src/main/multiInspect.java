package src.main;

import src.InspectWebLinks;

public class multiInspect {

    public static void main() {
        int n = 4; // Number of threads
        for (int i = 0; i < n; i++) {
            Thread object
                    = new Thread(new InspectWebLinks(i));

            object.start();
        }
    }
}
