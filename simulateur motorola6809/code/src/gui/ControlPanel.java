package gui;

import cpu.CPU;
import javax.swing.*;

/**
 * Panneau de contrôle pour le CPU 6809
 * Contient les boutons STEP et RESET
 */
public class ControlPanel extends JPanel {

    /**
     * Constructeur
     * @param cpu le CPU à contrôler
     * @param rp le panneau des registres pour rafraîchir l'affichage
     * @param fp le panneau des flags pour rafraîchir l'affichage
     * @param mp le panneau de la mémoire pour rafraîchir l'affichage
     */
    public ControlPanel(CPU cpu,
                        RegisterPanel rp,
                        FlagPanel fp,
                        MemoryPanel mp) {

        // Bouton pour exécuter une instruction
        JButton step = new JButton("STEP");
        step.addActionListener(e -> {
            cpu.step();      // Exécute une instruction
            rp.refresh();    // Met à jour les registres
            fp.refresh();    // Met à jour les flags
            mp.refresh();    // Met à jour la mémoire
        });

        // Bouton pour réinitialiser le CPU
        JButton reset = new JButton("RESET");
        reset.addActionListener(e -> {
            cpu.reset();     // Réinitialise tous les registres et la mémoire
            rp.refresh();    // Met à jour les registres
            fp.refresh();    // Met à jour les flags
            mp.refresh();    // Met à jour la mémoire
        });

        // Ajout des boutons au panneau
        add(step);
        add(reset);
    }
}
