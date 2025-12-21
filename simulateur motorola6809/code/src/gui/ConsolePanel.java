package gui;

import javax.swing.*;
import java.awt.*;

// Panneau graphique qui sert de console d'affichage pour le CPU 6809
public class ConsolePanel extends JPanel {

    // Zone de texte où les messages de la console sont affichés
    private final JTextArea area = new JTextArea();

    // Constructeur du panneau console
    public ConsolePanel() {

        // Bordure avec un titre "Console" et des marges internes
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Console"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Organisation du panneau en BorderLayout
        setLayout(new BorderLayout());

        // La console n'est pas modifiable par l'utilisateur
        area.setEditable(false);

        // Police monospace pour un affichage clair et aligné
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Taille visible de la zone de texte
        area.setRows(15);
        area.setColumns(30);

        // Couleurs : fond sombre et texte vert (style terminal)
        area.setBackground(new Color(18, 18, 18));
        area.setForeground(new Color(0, 230, 120));

        // Scroll pour pouvoir défiler quand il y a beaucoup de texte
        JScrollPane scroll = new JScrollPane(area);
        add(scroll, BorderLayout.CENTER);
    }

    // Ajoute une ligne dans la console
    public void println(String msg) {

        // Ajout du message suivi d'un retour à la ligne
        area.append(msg + "\n");

        // Défile automatiquement vers le bas
        area.setCaretPosition(area.getDocument().getLength());
    }
}
