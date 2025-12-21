package app;

import gui.DarkTheme;
import gui.EmulatorFrame; // <-- CORRECTION: utiliser EmulatorFrame
import cpu.CPU;

import javax.swing.*;

public class MainLauncher {
    public static void main(String[] args) {
        // Appliquer le thème sombre
        DarkTheme.apply();

        String[] options = {"Interface Graphique", "Mode Console", "Quitter"};

        int choice = JOptionPane.showOptionDialog(
                null,
                "Simulateur Motorola 6809 ",
                "Lanceur du Simulateur",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]
        );

        switch (choice) {
            case 0:
                SwingUtilities.invokeLater(() -> {
                    CPU cpu = new CPU();
                    // LANCER EmulatorFrame 
                    new EmulatorFrame(cpu).setVisible(true);
                });
                break;
            case 1:
                Emulator.main(args); // Mode console
                break;
            default:
                System.exit(0);
        }
    }
}