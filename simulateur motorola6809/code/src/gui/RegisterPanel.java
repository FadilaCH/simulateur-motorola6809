package gui;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;

// Panneau graphique pour afficher les registres du CPU
public class RegisterPanel extends JPanel {

    private final CPU cpu;  // Référence au CPU pour lire les registres

    // Labels pour afficher les valeurs des registres
    private final JLabel a = new JLabel();
    private final JLabel b = new JLabel();
    private final JLabel x = new JLabel();
    private final JLabel y = new JLabel();
    private final JLabel pc = new JLabel();

    public RegisterPanel(CPU cpu) {
        this.cpu = cpu;

        // Bordure avec titre
        setBorder(BorderFactory.createTitledBorder("Registers"));

        // Layout en grille : 5 lignes, 2 colonnes, avec espacement horizontal et vertical de 6
        setLayout(new GridLayout(5, 2, 6, 6));

        // Ajouter les labels des registres et leurs valeurs
        add(new JLabel("A:")); add(a);
        add(new JLabel("B:")); add(b);
        add(new JLabel("X:")); add(x);
        add(new JLabel("Y:")); add(y);
        add(new JLabel("PC:")); add(pc);

        refresh(); // Remplir initialement avec les valeurs actuelles
    }

    // Met à jour l'affichage des registres
    public void refresh() {
        a.setText(hex8(cpu.reg.A));      // Affiche A en hexadécimal 2 chiffres
        b.setText(hex8(cpu.reg.B));      // Affiche B en hexadécimal 2 chiffres
        x.setText(hex16(cpu.reg.X));     // Affiche X en hexadécimal 4 chiffres
        y.setText(hex16(cpu.reg.Y));     // Affiche Y en hexadécimal 4 chiffres
        pc.setText(hex16(cpu.reg.PC));   // Affiche PC en hexadécimal 4 chiffres

        // Appliquer un style (couleur) selon la valeur et l'importance
        style(a, cpu.reg.A, false);
        style(b, cpu.reg.B, false);
        style(x, cpu.reg.X, false);
        style(y, cpu.reg.Y, false);
        style(pc, cpu.reg.PC, true); // PC en couleur bleu pour le rendre important
    }

    // Définir la couleur du label selon la valeur
    private void style(JLabel lbl, int value, boolean important) {
        lbl.setForeground(
            important ? new Color(90,150,255)      // Bleu pour les registres importants
            : (value != 0 ? new Color(0,220,120)  // Vert si valeur non nulle
            : Color.GRAY)                          // Gris si valeur = 0
        );
    }

    // Conversion d'un entier 8 bits en chaîne hex
    private String hex8(int v) {
        return String.format("%02X", v & 0xFF);
    }

    // Conversion d'un entier 16 bits en chaîne hex
    private String hex16(int v) {
        return String.format("%04X", v & 0xFFFF);
    }
}
