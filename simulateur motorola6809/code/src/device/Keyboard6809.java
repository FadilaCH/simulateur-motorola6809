package device;

import cpu.CPU;
import cpu.Registers;

// Simulation d’un clavier mappé en mémoire pour le processeur 6809
// DATA  ($FF00) : contient le code de la touche
// STATUS($FF01) : indique si une touche est disponible (1 = oui, 0 = non)
public class Keyboard6809 {

    public static final int DATA = 0xFF00;   // Adresse mémoire des données clavier
    public static final int STATUS = 0xFF01; // Adresse mémoire du statut clavier

    private final CPU cpu;           // Référence vers le CPU
    private boolean keyAvailable = false; // Vrai si une touche est en attente
    private int keyCode = 0;          // Code ASCII de la touche pressée

    // Constructeur : reçoit le CPU pour interagir avec lui
    public Keyboard6809(CPU cpu) {
        this.cpu = cpu;
    }

    // Appelé quand l'utilisateur appuie sur une touche
    public void pressKey(char c) {
        keyCode = c & 0xFF;      // Conversion du caractère en code 8 bits
        keyAvailable = true;     // Une touche est maintenant disponible

        // Écriture des informations clavier dans la mémoire
        cpu.writeByte(DATA, keyCode);
        cpu.writeByte(STATUS, 1);

        // Déclenche une interruption pour prévenir le CPU
        cpu.triggerIRQ();
    }

    // Lecture du clavier par le CPU via la mémoire
    public int read(int addr) {

        // Lecture du code de la touche
        if (addr == DATA) {
            keyAvailable = false;      // La touche est consommée
            cpu.writeByte(STATUS, 0);  // Mise à jour du statut
            return keyCode;
        }

        // Lecture du statut clavier
        if (addr == STATUS) {
            return keyAvailable ? 1 : 0;
        }

        // Autres adresses : aucune donnée
        return 0;
    }
}
