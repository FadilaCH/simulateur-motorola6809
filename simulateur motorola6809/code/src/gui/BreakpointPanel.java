package gui;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;

// Panneau graphique pour gérer les breakpoints du CPU 6809
public class BreakpointPanel extends JPanel {

    private final CPU cpu; // Référence vers le CPU simulé

    // Modèle de données pour stocker les breakpoints sous forme de texte
    private final DefaultListModel<String> model = new DefaultListModel<>();

    // Liste graphique qui affiche les breakpoints
    private final JList<String> list = new JList<>(model);

    // Champ texte pour saisir une adresse hexadécimale
    private final JTextField addressField = new JTextField(6);

    // Constructeur du panneau de breakpoints
    public BreakpointPanel(CPU cpu) {
        this.cpu = cpu;

        // Bordure avec un titre et des marges internes
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Breakpoints"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Organisation générale du panneau
        setLayout(new BorderLayout());

        // Zone de défilement contenant la liste des breakpoints
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(120, 150));
        add(scroll, BorderLayout.CENTER);

        // Panneau inférieur pour l'entrée d'adresse et les boutons
        JPanel bottom = new JPanel();
        bottom.add(new JLabel("Adresse (hex):"));
        bottom.add(addressField);

        // Bouton pour ajouter un breakpoint
        JButton addBtn = new JButton("Ajouter");

        // Bouton pour supprimer un breakpoint
        JButton delBtn = new JButton("Supprimer");

        bottom.add(addBtn);
        bottom.add(delBtn);
        add(bottom, BorderLayout.SOUTH);

        // Actions associées aux boutons
        addBtn.addActionListener(e -> addBreakpoint());
        delBtn.addActionListener(e -> removeBreakpoint());
    }

    // Ajoute un breakpoint au CPU et l'affiche dans la liste
    private void addBreakpoint() {
        try {
            // Conversion de l'adresse hexadécimale en entier
            int addr = Integer.parseInt(addressField.getText(), 16);

            // Ajout du breakpoint dans le CPU
            cpu.addBreakpoint(addr);

            // Ajout de l'adresse formatée dans la liste
            model.addElement(String.format("%04X", addr & 0xFFFF));
        } catch (Exception e) {
            // Message d'erreur si l'adresse est invalide
            JOptionPane.showMessageDialog(this, "Adresse hex invalide");
        }
    }

    // Supprime le breakpoint sélectionné
    private void removeBreakpoint() {
        int idx = list.getSelectedIndex();
        if (idx >= 0) {
            // Récupération de l'adresse sélectionnée
            int addr = Integer.parseInt(model.get(idx), 16);

            // Suppression du breakpoint dans le CPU
            cpu.removeBreakpoint(addr);

            // Suppression dans la liste graphique
            model.remove(idx);
        }
    }
}
