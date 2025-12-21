package gui;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;

// Fenêtre graphique pour écrire et charger des programmes assembleur 6809
// Elle permet de tester rapidement le CPU sans modifier le code principal
public class AsmEditorFrame extends JFrame {

    private JTextArea editorArea;   // Zone pour écrire le code assembleur
    private JButton assembleButton; // Bouton pour assembler et charger
    private JButton loadButton;     // Bouton pour charger un programme simple
    private CPU cpu;                // Référence vers le CPU simulé

    // Constructeur de la fenêtre ASM
    public AsmEditorFrame(CPU cpu) {
        super("ASM Editor - Motorola 6809");
        this.cpu = cpu;

        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Zone de texte avec police monospace (plus lisible pour l'assembleur)
        editorArea = new JTextArea();
        editorArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        editorArea.setBackground(new Color(15,15,15));
        editorArea.setForeground(Color.WHITE);
        editorArea.setCaretColor(Color.WHITE);

        // Exemple de programme affiché au démarrage
        String example =
                "; === Programme test Motorola 6809 ===\n" +
                "        ORG $8000\n\n" +
                "START   LDA #$05\n" +
                "        ADDA #$03\n" +
                "        STA $00\n" +
                "        LDB #$FF\n" +
                "        LDX #$1234\n" +
                "        INCA\n" +
                "        NOP\n" +
                "        JMP START\n\n" +
                "; === Fin du programme ===\n";

        editorArea.setText(example);

        // Création des boutons
        assembleButton = new JButton("Assembler");
        loadButton = new JButton("Charger programme");

        // Actions des boutons
        assembleButton.addActionListener(e -> assembleAndLoad());
        loadButton.addActionListener(e -> loadSimpleProgram());

        // Panneau des boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(assembleButton);
        buttonPanel.add(loadButton);

        // Organisation de la fenêtre
        setLayout(new BorderLayout());
        add(new JScrollPane(editorArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Assemble et charge un programme prédéfini en mémoire
    private void assembleAndLoad() {
        try {
            // Programme machine correspondant à l'exemple
            byte[] program = {
                (byte) 0x86, 0x05,        // LDA #$05
                (byte) 0x8B, 0x03,        // ADDA #$03
                (byte) 0x97, 0x00,        // STA $00
                (byte) 0xC6, (byte) 0xFF,// LDB #$FF
                (byte) 0x8E, 0x12, 0x34,  // LDX #$1234
                (byte) 0x4C,              // INCA
                (byte) 0x12,              // NOP
                (byte) 0x7E, (byte) 0x80, 0x00 // JMP $8000
            };

            int baseAddress = 0x8000;

            // Chargement du programme en mémoire
            for (int i = 0; i < program.length; i++) {
                cpu.writeByte(baseAddress + i, program[i] & 0xFF);
            }

            // Initialisation des registres
            cpu.reg.PC = baseAddress;
            cpu.reg.SP = 0xFF00;
            cpu.reg.A = 0;
            cpu.reg.B = 0;
            cpu.reg.X = 0;
            cpu.reg.Y = 0;
            cpu.reg.U = 0xFF00;
            cpu.reg.DP = 0x00;
            cpu.reg.CC = 0x00;
            cpu.halted = false;

            // Message de confirmation
            JOptionPane.showMessageDialog(this,
                    "Programme assemblé et chargé.\n" +
                    "Adresse de départ : $8000\n" +
                    "Utilisez STEP pour exécuter.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Charge un petit programme de test avec SYNC
    private void loadSimpleProgram() {
        try {
            // Programme simple qui s'arrête avec SYNC
            byte[] program = {
                (byte) 0x86, 0x05, // LDA #$05
                (byte) 0x8B, 0x03, // ADDA #$03
                (byte) 0x97, 0x00, // STA $00
                (byte) 0x12,       // NOP
                (byte) 0x13        // SYNC
            };

            int baseAddress = 0x8000;

            // Chargement en mémoire
            for (int i = 0; i < program.length; i++) {
                cpu.writeByte(baseAddress + i, program[i] & 0xFF);
            }

            // Initialisation minimale des registres
            cpu.reg.PC = baseAddress;
            cpu.reg.SP = 0xFF00;
            cpu.reg.A = 0;
            cpu.reg.B = 0;
            cpu.reg.X = 0;
            cpu.reg.Y = 0;
            cpu.halted = false;

            JOptionPane.showMessageDialog(this,
                    "Programme de test chargé.\n" +
                    "Fin avec SYNC.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
