package app;

import cpu.CPU;

import javax.swing.*;
import java.awt.*;
import java.io.File;


  //Interface graphique pour le simulateur Motorola 6809. Permet de visualiser la mémoire, les registres, la console et le panneau de debug.
 // Fournit des boutons pour exécuter, arrêter, réinitialiser et charger des programmes.
 
public class EmulatorGUI extends JFrame {

    private DebugPanel debugPanel;
    private final CPU cpu;
    private final MemoryTableModel memModel;
    private final JTable memTable;
    private final RegisterPanel regPanel;
    private final JTextArea console;
    private Thread runner;
    private volatile boolean runFlag = false;

    
    // initialise tous les composants et charge un programme test.
     
    public EmulatorGUI(CPU cpu) {
        super("Simulateur Motorola 6809 - GUI");
        this.cpu = cpu;

        // Petit programme test dans la mémoire
        cpu.writeByte(0x8000, 0x86);
        cpu.writeByte(0x8001, 0x05);
        cpu.writeByte(0x8002, 0x8B);
        cpu.writeByte(0x8003, 0x03);
        cpu.writeByte(0x8004, 0x4C);
        cpu.writeByte(0x8005, 0x12);
        cpu.writeByte(0x8006, 0x13);
        cpu.reg.PC = 0x8000;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Table mémoire
        memModel = new MemoryTableModel(cpu.mem);
        memTable = new JTable(memModel);
        memTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        memTable.setRowHeight(20);
        JScrollPane memScroll = new JScrollPane(memTable);
        memScroll.setBorder(BorderFactory.createTitledBorder("Mémoire 6809 (64KB)"));

        // Panneau des registres
        regPanel = new RegisterPanel(cpu);

        // Console texte
        console = new JTextArea(8, 80);
        console.setEditable(false);
        console.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane consoleScroll = new JScrollPane(console);
        consoleScroll.setBorder(BorderFactory.createTitledBorder("Console"));

        // Panneau de contrôle avec les boutons
        ControlPanel ctrl = new ControlPanel(
                e -> step(),
                e -> run(),
                e -> stopRun(),
                e -> loadBinary(),
                e -> reset()
        );

        // Organiser la fenêtre principale
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, memScroll, regPanel);
        split.setDividerLocation(760);
        add(split, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(ctrl, BorderLayout.NORTH);
        south.add(consoleScroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        console.append("=== Simulateur Motorola 6809 ===\n");
        console.append("GUI prête. Charger un programme ou utiliser les tests intégrés.\n");

        // Ajouter le panneau de debug à droite
        debugPanel = new DebugPanel(cpu);
        add(debugPanel, BorderLayout.EAST);
    }

    
     // Exécute une instruction unique et met à jour l'affichage.
     
    private void step() {
        if (cpu.halted) {
            console.append("CPU halted.\n");
            return;
        }

        int pcBefore = cpu.reg.PC & 0xFFFF;
        int opcode = cpu.readByte(pcBefore);
        String name = cpu.getInstructionSet().getName(opcode);

        cpu.step();
        updateUI();

        console.append(String.format("PC=%04X: %s (opcode %02X)%n", pcBefore, name, opcode));

        // Affiche info supp si instruction a PostByte
        if (cpu.getLastPostByte() != 0) {
            console.append(String.format("  PostByte=%02X EA=%04X%n", cpu.getLastPostByte(), cpu.getLastEffectiveAddress()));
        }
    }

    
     //Lance le CPU  
     
    private void run() {
        if (runFlag || cpu.halted) return;

        runFlag = true;
        runner = new Thread(() -> {
            while (runFlag && !cpu.halted) {
                cpu.step();
                SwingUtilities.invokeLater(this::updateUI);
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
            runFlag = false;
        });
        runner.start();
        console.append("Exécution démarrée...\n");
    }

    
     // Arrête l'exécution continue du CPU.
     
    private void stopRun() {
        runFlag = false;
        if (runner != null) runner.interrupt();
        console.append("Exécution arrêtée.\n");
    }

    
      //Réinitialise le CPU et met à jour l'affichage.
     
    private void reset() {
        cpu.reset();
        updateUI();
        console.setText("");
        console.append("CPU réinitialisé.\n");
    }

    
     //Charge un fichier binaire choisi par l'utilisateur.
     
    private void loadBinary() {
        JFileChooser chooser = new JFileChooser(".");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                app.BinaryLoader.loadFileIntoMemory(f, cpu.mem, 0x8000);
                cpu.reg.PC = 0x8000;
                updateUI();
                console.append("Fichier chargé: " + f.getName() + "\n");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            }
        }
    }

    
     // Met à jour la mémoire, les registres et le panneau de debug.
     
    private void updateUI() {
        memModel.fireTableDataChanged();
        regPanel.refresh();
        highlightPC();
        debugPanel.refresh();
    }

    
    private void highlightPC() {
        int pc = cpu.reg.PC & 0xFFFF;
        int row = pc / 16;
        int col = (pc % 16) + 1;

        if (row >= 0 && row < memTable.getRowCount() &&
            col >= 0 && col < memTable.getColumnCount()) {

            memTable.changeSelection(row, col, false, false);
            memTable.scrollRectToVisible(memTable.getCellRect(row, col, true));
        }
    }
}
