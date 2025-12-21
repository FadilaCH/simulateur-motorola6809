package app;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;

// Panneau pour afficher les registres du CPU 6809
public class RegisterPanel extends JPanel {

    private final CPU cpu; // CPU dont on affiche les registres

    // Labels pour chaque registre et infos supplémentaires
    private final JLabel pcLabel, aLabel, bLabel, dLabel, xLabel, yLabel, spLabel, uLabel, dpLabel, ccLabel, eaLabel, postLabel;

    // Constructeur : initialise les labels et la mise en page
    public RegisterPanel(CPU cpu) {
        this.cpu = cpu;

        setLayout(new GridLayout(0, 1)); // grille verticale
        setBorder(BorderFactory.createTitledBorder("Registres")); // bordure avec titre

        // Création des labels pour chaque registre
        pcLabel = mk("PC");
        aLabel = mk("A");
        bLabel = mk("B");
        dLabel = mk("D");
        xLabel = mk("X");
        yLabel = mk("Y");
        spLabel = mk("SP");
        uLabel = mk("U");
        dpLabel = mk("DP");
        ccLabel = mk("CC");
        eaLabel = mk("EA");
        postLabel = mk("PostByte");

        // Ajout des labels au panneau
        add(pcLabel);
        add(aLabel);
        add(bLabel);
        add(dLabel);
        add(xLabel);
        add(yLabel);
        add(spLabel);
        add(uLabel);
        add(dpLabel);
        add(ccLabel);
        add(eaLabel);
        add(postLabel);

        refresh(); // afficher l'état initial des registres
    }

    // Crée un JLabel pour un registre avec style uniforme
    private JLabel mk(String name) {
        JLabel l = new JLabel(name + " = "); // texte initial
        l.setFont(new Font("Monospaced", Font.PLAIN, 14)); // police fixe
        return l;
    }

    // Met à jour les labels avec les valeurs actuelles du CPU
    public void refresh() {
        pcLabel.setText(String.format("PC = %04X", cpu.reg.PC & 0xFFFF));
        aLabel.setText(String.format("A  = %02X", cpu.reg.A & 0xFF));
        bLabel.setText(String.format("B  = %02X", cpu.reg.B & 0xFF));
        dLabel.setText(String.format("D  = %04X", cpu.reg.D() & 0xFFFF));
        xLabel.setText(String.format("X  = %04X", cpu.reg.X & 0xFFFF));
        yLabel.setText(String.format("Y  = %04X", cpu.reg.Y & 0xFFFF));
        spLabel.setText(String.format("SP = %04X", cpu.reg.SP & 0xFFFF));
        uLabel.setText(String.format("U  = %04X", cpu.reg.U & 0xFFFF));
        dpLabel.setText(String.format("DP = %02X", cpu.reg.DP & 0xFF));
        ccLabel.setText(String.format("CC = %02X", cpu.reg.CC & 0xFF));
        eaLabel.setText(String.format("EA = %04X", cpu.getLastEffectiveAddress() & 0xFFFF));
        postLabel.setText(String.format("PostByte = %02X", cpu.getLastPostByte() & 0xFF));
    }
}
