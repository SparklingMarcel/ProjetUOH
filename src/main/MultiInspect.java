package src.main;

import src.InspectWebLinks;

public class MultiInspect {
    private static int nbThread = 4 ;
    public static void main() {
        int n = nbThread; // Number of threads
        for (int i = 0; i < n; i++) {
            Thread object
                    = new Thread(new InspectWebLinks(i));

            object.start();
        }
    }

    public static int getNbThread() {
        return nbThread;
    }
}
