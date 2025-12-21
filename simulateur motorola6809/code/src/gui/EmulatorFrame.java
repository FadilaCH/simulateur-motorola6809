package gui;

import cpu.CPU;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class EmulatorFrame extends JFrame {

    private CPU cpu;

    // Panels
    private JPanel registersPanel, stackPanel, memoryPanel, disassemblyPanel, consolePanel, breakpointsPanel;
    private JTextArea memoryArea, disassemblyArea, stackArea, consoleOutputArea;
    private JTextField accumulatorAField, accumulatorBField, indexXField, indexYField,
            uField, spField, dpField, ccField, pcField, instructionField, breakpointField;

    private JButton stepButton, runButton, stopButton, resetButton,
            openAsmButton, executeButton, addBreakpointButton, removeBreakpointButton;

    private static final Color BG_DARK   = new Color(20, 20, 20);
    private static final Color BG_PANEL  = new Color(30, 30, 30);
    private static final Color BG_AREA   = new Color(15, 15, 15);
    private static final Color FG_TEXT   = Color.WHITE;
    private static final Color BORDER    = new Color(80, 80, 80);
    private static final Color BUTTON_BG = new Color(45, 45, 45);
    private static final Color BUTTON_HOVER = new Color(65, 65, 65);

    private Set<Integer> breakpoints = new HashSet<>();
    private Timer runTimer;

    public EmulatorFrame(CPU cpu) {
        super("Motorola 6809 Emulator");
        this.cpu = cpu;
        initUI();
        loadDefaultProgram();
        updateAllDisplays();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(5,5));
        mainPanel.setBackground(BG_DARK);

        // ─── TOP BAR ───
        JPanel topPanel = new JPanel();
        topPanel.setBackground(BG_PANEL);

        stepButton = createButton("STEP");
        runButton  = createButton("RUN");
        stopButton = createButton("STOP");
        resetButton = createButton("RESET");
        openAsmButton = createButton("ASM EDITOR");
        executeButton = createButton("EXECUTE 5");

        stepButton.addActionListener(e -> stepInstruction());
        runButton.addActionListener(e -> startRun());
        stopButton.addActionListener(e -> stopRun());
        resetButton.addActionListener(e -> resetEmulator());
        openAsmButton.addActionListener(e -> {
            AsmEditorFrame asmEditor = new AsmEditorFrame(cpu);
            asmEditor.setVisible(true);
        });
        executeButton.addActionListener(e -> executeFiveInstructions());

        topPanel.add(stepButton);
        topPanel.add(runButton);
        topPanel.add(stopButton);
        topPanel.add(resetButton);
        topPanel.add(openAsmButton);
        topPanel.add(executeButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ─── CENTER GRID ───
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.BOTH;

        // ─── REGISTERS ───
        registersPanel = new JPanel(new GridLayout(10,2,5,5));
        registersPanel.setBackground(BG_PANEL);
        registersPanel.setBorder(createBorder("Registres"));

        accumulatorAField = createField("00");
        accumulatorBField = createField("00");
        indexXField = createField("0000");
        indexYField = createField("0000");
        uField = createField("0000");
        spField = createField("FFFF");
        dpField = createField("00");
        ccField = createField("00");
        pcField = createField("0000");
        instructionField = createField("NOP");

        addReg("Accumulator A", accumulatorAField);
        addReg("Accumulator B", accumulatorBField);
        addReg("Index X", indexXField);
        addReg("Index Y", indexYField);
        addReg("U", uField);
        addReg("Stack Pointer", spField);
        addReg("DP", dpField);
        addReg("CC", ccField);
        addReg("PC", pcField);
        addReg("Instruction", instructionField);

        gbc.gridx = 0;
        gbc.weightx = 0.22;
        centerPanel.add(registersPanel, gbc);

        // ─── MEMORY ───
        memoryPanel = new JPanel(new BorderLayout());
        memoryPanel.setBackground(BG_PANEL);
        memoryPanel.setBorder(createBorder("Mémoire"));
        memoryArea = createTextArea(false);
        memoryPanel.add(new JScrollPane(memoryArea), BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.45;
        centerPanel.add(memoryPanel, gbc);

        // ─── RIGHT PANEL ───
        JPanel rightPanel = new JPanel(new GridLayout(2,1,5,5));
        rightPanel.setBackground(BG_DARK);

        consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBackground(BG_PANEL);
        consolePanel.setBorder(createBorder("Console"));
        consoleOutputArea = createTextArea(true);
        consolePanel.add(new JScrollPane(consoleOutputArea), BorderLayout.CENTER);

        breakpointsPanel = new JPanel(new BorderLayout());
        breakpointsPanel.setBackground(BG_PANEL);
        breakpointsPanel.setBorder(createBorder("Breakpoints"));

        JPanel bpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bpPanel.setBackground(BG_PANEL);

        breakpointField = new JTextField();
        breakpointField.setColumns(6);
        breakpointField.setEditable(true);
        breakpointField.setFocusable(true);
        breakpointField.setForeground(FG_TEXT);
        breakpointField.setBackground(BG_AREA);
        breakpointField.setCaretColor(FG_TEXT);

        addBreakpointButton = createButton("ADD");
        removeBreakpointButton = createButton("REMOVE");

        addBreakpointButton.addActionListener(e -> addBreakpoint());
        removeBreakpointButton.addActionListener(e -> removeBreakpoint());

        bpPanel.add(new JLabel("Adresse (hex):"));
        bpPanel.add(breakpointField);
        bpPanel.add(addBreakpointButton);
        bpPanel.add(removeBreakpointButton);

        breakpointsPanel.add(bpPanel, BorderLayout.NORTH);

        rightPanel.add(consolePanel);
        rightPanel.add(breakpointsPanel);

        gbc.gridx = 2;
        gbc.weightx = 0.33;
        centerPanel.add(rightPanel, gbc);

        // ─── STACK ───
        stackPanel = new JPanel(new BorderLayout());
        stackPanel.setBackground(BG_PANEL);
        stackPanel.setBorder(createBorder("Pile (Stack)"));
        stackArea = createTextArea(false);
        stackPanel.add(new JScrollPane(stackArea), BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 0.4;
        centerPanel.add(stackPanel, gbc);

        // ─── DISASSEMBLY ───
        disassemblyPanel = new JPanel(new BorderLayout());
        disassemblyPanel.setBackground(BG_PANEL);
        disassemblyPanel.setBorder(createBorder("Désassemblage"));
        disassemblyArea = createTextArea(false);
        disassemblyPanel.add(new JScrollPane(disassemblyArea), BorderLayout.CENTER);

        gbc.gridy = 2;
        centerPanel.add(disassemblyPanel, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JLabel signature = new JLabel(
                "Motorola 6809 Emulator - Fadila Chritt & Hafsa Barbay",
                SwingConstants.RIGHT
        );
        signature.setForeground(new Color(130,130,130));
        mainPanel.add(signature, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        
        consoleOutputArea.append("<-------> Simulateur Motorola 6809 <----->\n");
        consoleOutputArea.append(" Utilisez ASM EDITOR pour charger un programme .\n");
    }

    private void loadDefaultProgram() {
        // Programme de démonstration par défaut
        int base = 0x8000;
        
        byte[] program = {
            (byte) 0x86, 0x05,        // LDA #$05
            (byte) 0x8B, 0x03,        // ADDA #$03
            (byte) 0x97, 0x00,        // STA $00
            (byte) 0x12,              // NOP
            (byte) 0x13               // SYNC
        };
        
        for (int i = 0; i < program.length; i++) {
            cpu.writeByte(base + i, program[i] & 0xFF);
        }
        
        cpu.reg.PC = base;
        cpu.reg.SP = 0xFF00;
        cpu.halted = false;
        
        consoleOutputArea.append("Programme prêt à exécuter ");

    }

    private void executeFiveInstructions() {
        if (cpu.halted) {
            cpu.halted = false;
            consoleOutputArea.append("réactivation de CPU\n");
        }
        
        consoleOutputArea.append("Exécution de 5 instructions.\n");
        for (int i = 0; i < 5 && !cpu.halted; i++) {
            int pcBefore = cpu.reg.PC & 0xFFFF;
            int opcode = cpu.readByte(pcBefore);
            String instName = cpu.getInstructionSet().getName(opcode);
            
            cpu.step();
            consoleOutputArea.append(String.format("  PC=%04X : %s\n", pcBefore, instName));
            
            if (breakpoints.contains(cpu.reg.PC & 0xFFFF)) {
                consoleOutputArea.append("   Breakpoint atteint\n");
                break;
            }
        }
        
        updateAllDisplays();
        consoleOutputArea.append("5 instructions exécutées\n");
    }

    private void stepInstruction() {
        if (cpu.halted) {
            consoleOutputArea.append("CPU HALTED , Taper RESET pour continuer\n");
            return;
        }
        
        try {
            int pcBefore = cpu.reg.PC & 0xFFFF;
            int opcode = cpu.readByte(pcBefore);
            String instName = cpu.getInstructionSet().getName(opcode);
            
            // Exécuter l'instruction
            cpu.step();
            
            // Mettre à jour l'affichage
            updateAllDisplays();
            
            // Afficher dans la console
            consoleOutputArea.append(String.format("PC=%04X : %s (opcode %02X)\n", 
                pcBefore, instName, opcode));
            
            // Vérifier les breakpoints
            int currentPC = cpu.reg.PC & 0xFFFF;
            if (breakpoints.contains(currentPC)) {
                consoleOutputArea.append(" Breakpoint atteint à $" + 
                    String.format("%04X", currentPC) + "\n");
                stopRun();
            }
            
        } catch (Exception e) {
            consoleOutputArea.append("Erreur: " + e.getMessage() + "\n");
            cpu.halted = true;
        }
    }

    private void startRun() {
        if (runTimer == null) {
            consoleOutputArea.append("Démarrage de l'exécution continue...\n");
            
            runTimer = new Timer(200, e -> {
                if (!cpu.halted && !breakpoints.contains(cpu.reg.PC & 0xFFFF)) {
                    stepInstruction();
                } else {
                    stopRun();
                    if (cpu.halted) {
                        consoleOutputArea.append("CPU HALTED\n");
                    }
                }
            });
            runTimer.start();
        }
    }

    private void stopRun() {
        if (runTimer != null) {
            runTimer.stop();
            runTimer = null;
            consoleOutputArea.append("Exécution arrêtée\n");
        }
    }

    private void resetEmulator() {
        stopRun();
        cpu.reset();
        loadDefaultProgram();
        breakpoints.clear();
        consoleOutputArea.append("<------> CPU réinitialisé <----->\n");
        updateAllDisplays();
    }

    private void updateAllDisplays() {
        updateRegisterDisplay();
        updateMemoryDisplay();
        updateStackDisplay();
        updateDisassembly();
    }

    private void updateRegisterDisplay() {
        accumulatorAField.setText(String.format("%02X", cpu.reg.A & 0xFF));
        accumulatorBField.setText(String.format("%02X", cpu.reg.B & 0xFF));
        indexXField.setText(String.format("%04X", cpu.reg.X & 0xFFFF));
        indexYField.setText(String.format("%04X", cpu.reg.Y & 0xFFFF));
        uField.setText(String.format("%04X", cpu.reg.U & 0xFFFF));
        spField.setText(String.format("%04X", cpu.reg.SP & 0xFFFF));
        dpField.setText(String.format("%02X", cpu.reg.DP & 0xFF));
        ccField.setText(String.format("%02X", cpu.reg.CC & 0xFF));
        pcField.setText(String.format("%04X", cpu.reg.PC & 0xFFFF));
        
        int pc = cpu.reg.PC & 0xFFFF;
        int opcode = cpu.readByte(pc);
        String instName = cpu.getInstructionSet().getName(opcode);
        instructionField.setText(instName);
    }

    private void updateMemoryDisplay() {
        StringBuilder m = new StringBuilder();
        int startAddr = cpu.reg.PC & 0xFFF0; // Aligné sur 16
        
        for (int i = 0; i < 8; i++) {
            int addr = startAddr + (i * 16);
            if (addr >= 0 && addr <= 0xFFFF) {
                m.append(String.format("%04X : ", addr));
                for (int j = 0; j < 16; j++) {
                    m.append(String.format("%02X ", cpu.readByte(addr + j)));
                }
                m.append("\n");
            }
        }
        memoryArea.setText(m.toString());
    }

    private void updateStackDisplay() {
        StringBuilder s = new StringBuilder();
        int sp = cpu.reg.SP & 0xFFFF;
        
        for (int i = 0; i < 8; i++) {
            int addr = (sp + i) & 0xFFFF;
            s.append(String.format("%04X : %02X\n", addr, cpu.readByte(addr)));
        }
        stackArea.setText(s.toString());
    }

    private void updateDisassembly() {
        StringBuilder sb = new StringBuilder();
        int pc = cpu.reg.PC & 0xFFFF;
        
        for (int i = 0; i < 8; i++) {
            int addr = (pc + i) & 0xFFFF;
            int opcode = cpu.readByte(addr);
            String instName = cpu.getInstructionSet().getName(opcode);
            
            if (instName.equals("UNKNOWN")) {
                sb.append(String.format("%04X : %02X     DB %02X\n", addr, opcode, opcode));
            } else if (instName.contains("#")) {
                int operand = cpu.readByte((addr + 1) & 0xFFFF);
                sb.append(String.format("%04X : %02X %02X  %s\n", addr, opcode, operand, 
                    instName.replace("#", String.format("#%02X", operand))));
                i++; // Skip operand
            } else {
                sb.append(String.format("%04X : %02X     %s\n", addr, opcode, instName));
            }
        }
        
        disassemblyArea.setText(sb.toString());
    }

    private void addBreakpoint() {
        try {
            String addrStr = breakpointField.getText().trim();
            if (addrStr.startsWith("$")) {
                addrStr = addrStr.substring(1);
            } else if (addrStr.startsWith("0x")) {
                addrStr = addrStr.substring(2);
            }
            
            int addr = Integer.parseInt(addrStr, 16) & 0xFFFF;
            if (!breakpoints.contains(addr)) {
                breakpoints.add(addr);
                cpu.addBreakpoint(addr);
                consoleOutputArea.append("Breakpoint ajouté à $" + String.format("%04X", addr) + "\n");
                breakpointField.setText("");
            } else {
                consoleOutputArea.append("Breakpoint existe déjà\n");
            }
        } catch (Exception e) {
            consoleOutputArea.append("Adresse invalide.\n");
        }
    }

    private void removeBreakpoint() {
        try {
            String addrStr = breakpointField.getText().trim();
            if (addrStr.startsWith("$")) {
                addrStr = addrStr.substring(1);
            } else if (addrStr.startsWith("0x")) {
                addrStr = addrStr.substring(2);
            }
            
            int addr = Integer.parseInt(addrStr, 16) & 0xFFFF;
            if (breakpoints.remove(addr)) {
                cpu.removeBreakpoint(addr);
                consoleOutputArea.append("Breakpoint supprimé à $" + String.format("%04X", addr) + "\n");
                breakpointField.setText("");
            } else {
                consoleOutputArea.append("Breakpoint non trouvé\n");
            }
        } catch (Exception e) {
            consoleOutputArea.append("Adresse invalide.\n");
        }
    }

    private JTextArea createTextArea(boolean editable) {
        JTextArea a = new JTextArea();
        a.setBackground(BG_AREA);
        a.setForeground(FG_TEXT);
        a.setCaretColor(FG_TEXT);
        a.setFont(new Font("Consolas", Font.PLAIN, 12));
        a.setEditable(editable);
        return a;
    }

    private JTextField createField(String v) {
        JTextField f = new JTextField(v);
        f.setBackground(BG_AREA);
        f.setForeground(FG_TEXT);
        f.setCaretColor(FG_TEXT);
        f.setBorder(BorderFactory.createLineBorder(BORDER));
        f.setEditable(false);
        f.setFocusable(false);
        return f;
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BUTTON_BG);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(BORDER));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));

        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(BUTTON_HOVER); }
            public void mouseExited(MouseEvent e) { b.setBackground(BUTTON_BG); }
        });
        return b;
    }

    private TitledBorder createBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                null,
                new Color(160,160,160)
        );
    }

    private void addReg(String name, JTextField field) {
        JLabel l = new JLabel(name + ":");
        l.setForeground(new Color(180,180,180));
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registersPanel.add(l);
        registersPanel.add(field);
    }
}