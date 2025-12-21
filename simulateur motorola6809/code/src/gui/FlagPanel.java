package gui;

import cpu.CPU;
import cpu.Registers;
import javax.swing.*;
import java.awt.*;

// Panel qui affiche les flags du processeur 6809 (N, Z, V, C)
public class FlagPanel extends JPanel {

    private final CPU cpu;           // Référence au CPU pour lire les flags
    private final JCheckBox n = new JCheckBox("N"); // Flag N (Negative)
    private final JCheckBox z = new JCheckBox("Z"); // Flag Z (Zero)
    private final JCheckBox v = new JCheckBox("V"); // Flag V (Overflow)
    private final JCheckBox c = new JCheckBox("C"); // Flag C (Carry)

    // Constructeur : initialise le panel et ajoute les checkboxes
    public FlagPanel(CPU cpu) {
        this.cpu = cpu;
        setBorder(BorderFactory.createTitledBorder("Flags")); // Bordure avec titre
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));    // Organisation horizontale avec espace

        // Les checkboxes sont **non éditables** (on ne peut pas cliquer dessus)
        n.setEnabled(false);
        z.setEnabled(false);
        v.setEnabled(false);
        c.setEnabled(false);

        // Ajouter les checkboxes au panel
        add(n); add(z); add(v); add(c);

        // Initialiser leur état
        refresh();
    }

    // Met à jour l'état des flags selon le registre CC du CPU
    public void refresh() {
        n.setSelected(cpu.reg.getFlag(Registers.FLAG_N));
        z.setSelected(cpu.reg.getFlag(Registers.FLAG_Z));
        v.setSelected(cpu.reg.getFlag(Registers.FLAG_V));
        c.setSelected(cpu.reg.getFlag(Registers.FLAG_C));

        // Mettre à jour la couleur : vert si actif, gris sinon
        color(n);
        color(z);
        color(v);
        color(c);
    }

    // Fonction auxiliaire pour colorer un flag
    private void color(JCheckBox f) {
        f.setForeground(
            f.isSelected() ? new Color(0,220,120) : Color.GRAY
        );
    }
}
