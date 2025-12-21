package gui;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;

// Barre d'outils spécifique pour le simulateur 6809
public class Toolbar6809 extends JToolBar {

    // Constructeur qui prend le CPU et les panels pour actualiser l'affichage
    public Toolbar6809(CPU cpu,
                       RegisterPanel rp,
                       FlagPanel fp,
                       MemoryPanel mp) {

        setFloatable(false); // Empêche la barre d'être détachable

        // Bouton "RUN" : exécute jusqu'au prochain breakpoint
        add(btn("▶ RUN", "Run until breakpoint", cpu::step));

        // Bouton "STEP" : exécute une seule instruction
        add(btn("⏭ STEP", "Execute one instruction", cpu::step));

        // Bouton "RESET" : réinitialise le CPU et rafraîchit les panels
        add(btn("⏹ RESET", "Reset CPU", () -> {
            cpu.reset();     // Réinitialise tous les registres et mémoire
            rp.refresh();    // Actualise les registres affichés
            fp.refresh();    // Actualise les flags affichés
            mp.refresh();    // Actualise la mémoire affichée
        }));

        addSeparator(); // Séparateur visuel entre groupes de boutons

        // Bouton "IRQ" : déclenche une interruption matérielle
        add(btn("⛔ IRQ", "Trigger hardware interrupt", cpu::triggerIRQ));
    }

    // Méthode utilitaire pour créer un bouton avec texte, tooltip et action
    private JButton btn(String text, String tip, Runnable action) {
        JButton b = new JButton(text);                    // Crée le bouton
        b.setBackground(new Color(30,30,30));            // Couleur de fond sombre
        b.setBorder(BorderFactory.createLineBorder(new Color(70,70,70))); // Bordure grise
        b.setFocusPainted(false);                        // Pas de contour de focus
        b.setToolTipText(tip);                           // Texte d'aide au survol
        b.addActionListener(e -> action.run());          // Exécute l'action lors du clic
        return b;                                        // Retourne le bouton configuré
    }
}
