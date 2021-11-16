package src.main;

import src.InspectWebLinks;

public class MultiInspect {
    private static final int nbThread = 4 ; // le nombre de thread qu'on utilise
    private MultiInspect() {

    }
    public static void main() { // Creation des threads
        for (int i = 0; i < nbThread; i++) {
            Thread object = new Thread(new InspectWebLinks(i));
            object.start();
        }
    }

    public static int getNbThread() { // retourne le nombre de thread
        return nbThread;
    }
}
