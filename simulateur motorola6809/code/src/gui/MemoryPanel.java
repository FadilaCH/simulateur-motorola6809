package gui;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;

// Panneau graphique pour afficher la mémoire du CPU
public class MemoryPanel extends JPanel {

    private final JTextArea area = new JTextArea(); // Zone de texte pour afficher la mémoire
    private final CPU cpu;                           // Référence au CPU pour lire la mémoire

    public MemoryPanel(CPU cpu) {
        this.cpu = cpu;

        // Ajouter une bordure avec titre et marge interne
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Memory"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        setLayout(new BorderLayout()); // Layout pour placer la zone de texte au centre

        // Configurer la zone de texte
        area.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Police monospace pour alignement
        area.setEditable(false);  // Lecture seule
        area.setRows(20);
        area.setColumns(32);

        // Ajouter un scroll à la zone de texte
        JScrollPane scroll = new JScrollPane(area);
        add(scroll, BorderLayout.CENTER);

        refresh(); // Remplir la zone avec la mémoire actuelle du CPU
    }

    // Met à jour l'affichage de la mémoire
    public void refresh() {
        StringBuilder sb = new StringBuilder();

        // Afficher les 256 premiers octets en blocs de 8
        for (int i = 0; i < 256; i += 8) {
            sb.append(String.format("%04X : ", i)); // Adresse de début de ligne
            for (int j = 0; j < 8; j++) {
                sb.append(String.format("%02X ", cpu.readByte(i + j))); // Valeur hex de chaque octet
            }
            sb.append("\n"); // Nouvelle ligne
        }

        area.setText(sb.toString()); // Mettre à jour la zone de texte
    }
}
