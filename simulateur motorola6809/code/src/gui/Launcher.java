package gui;

import cpu.CPU;
import javax.swing.*;

// Classe principale pour lancer le simulateur 6809
public class Launcher {
    public static void main(String[] args) {
        // Appliquer le thème sombre à toutes les fenêtres
        DarkTheme.apply();

        // Créer et afficher la fenêtre graphique dans le thread Swing
        SwingUtilities.invokeLater(() -> {
            CPU cpu = new CPU();               // Créer le CPU
            EmulatorFrame frame = new EmulatorFrame(cpu); // Créer la fenêtre principale
            frame.setVisible(true);            // Afficher la fenêtre

            // Informations dans la console
            System.out.println("Simulateur Motorola 6809 démarré");
            System.out.println("Utilisez ASM EDITOR pour écrire du code 6809");
            System.out.println("Syntaxe supportée: LDA #$05, ADDA #$03, etc.");
        });
    }
}
