package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Classe utilitaire pour appliquer un thème sombre à l'interface Swing
 */
public class DarkTheme {

    /**
     * Applique le thème sombre
     */
    public static void apply() {
        try {
            // Utilisation du LookAndFeel Nimbus
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // Si échec, on ignore et Swing utilisera le LookAndFeel par défaut
        }

        // Couleurs principales
        Color bg = new Color(28, 28, 28);      // Fond principal
        Color bg2 = new Color(40, 40, 40);     // Fond secondaire (boutons, textfields)
        Color fg = new Color(220, 220, 220);   // Texte clair
        Color accent = new Color(90, 150, 255);// Couleur accent (titres, focus)
        Color grid = new Color(60, 60, 60);    // Couleur des bordures et grilles

        // ===== POLICE =====
        Font uiFont = new Font("JetBrains Mono", Font.PLAIN, 12); // Police monospace pour le code

        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("TextArea.font", uiFont);
        UIManager.put("TextField.font", uiFont);
        UIManager.put("TitledBorder.font", uiFont.deriveFont(Font.BOLD)); // Titres en gras

        // ===== COULEURS =====
        UIManager.put("control", bg);
        UIManager.put("Panel.background", bg);
        UIManager.put("Viewport.background", bg);
        UIManager.put("Label.foreground", fg);

        // Boutons
        UIManager.put("Button.background", bg2);
        UIManager.put("Button.foreground", fg);
        UIManager.put("Button.border", BorderFactory.createLineBorder(grid));
        UIManager.put("Button.focus", accent);

        // TextArea (console / éditeur)
        UIManager.put("TextArea.background", new Color(18, 18, 18));
        UIManager.put("TextArea.foreground", new Color(0, 230, 120));

        // TextField (entrée utilisateur)
        UIManager.put("TextField.background", bg2);
        UIManager.put("TextField.foreground", fg);

        // Tables (ex. mémoire, registres)
        UIManager.put("Table.background", bg);
        UIManager.put("Table.foreground", fg);
        UIManager.put("Table.gridColor", grid);

        // Séparateurs et barres d'outils
        UIManager.put("Separator.foreground", grid);
        UIManager.put("ToolBar.background", bg);
        UIManager.put("ToolBar.border", BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Couleur des titres de bordures
        UIManager.put("TitledBorder.titleColor", accent);
    }
}
