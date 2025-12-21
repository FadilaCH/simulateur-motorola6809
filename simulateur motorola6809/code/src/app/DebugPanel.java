package app;

import cpu.CPU;
import javax.swing.*;
import java.awt.*;


 // Panneau de débogage du simulateur 6809. Affiche la pile et le désassemblage des instructions.
 
public class DebugPanel extends JPanel {

    private final DefaultListModel<String> stackModel = new DefaultListModel<>();
    private final JList<String> stackList = new JList<>(stackModel);
    private final JTextArea disassembly = new JTextArea();
    private final CPU cpu;

    
     // Initialise le panneau avec la pile et le désassemblage.
     
    public DebugPanel(CPU cpu) {
        this.cpu = cpu;
        setLayout(new BorderLayout());

        JScrollPane stackScroll = new JScrollPane(stackList);
        stackScroll.setBorder(BorderFactory.createTitledBorder("Stack (SP)"));

        disassembly.setFont(new Font("Monospaced", Font.PLAIN, 12));
        disassembly.setEditable(false);
        JScrollPane disScroll = new JScrollPane(disassembly);
        disScroll.setBorder(BorderFactory.createTitledBorder("Disassembly"));

        add(stackScroll, BorderLayout.WEST);
        add(disScroll, BorderLayout.CENTER);
    }

    
     // Met à jour l'affichage du panneau.
    
    public void refresh() {
        refreshStack();
        refreshDisassembly();
    }

    
     // Met à jour le contenu de la pile à partir du registre SP.
     
    private void refreshStack() {
        stackModel.clear();
        int sp = cpu.reg.SP & 0xFFFF;

        for (int i = 0; i < 8; i++) {
            int addr = (sp + i) & 0xFFFF;
            int val = cpu.readByte(addr);
            stackModel.addElement(String.format("%04X : %02X", addr, val));
        }
    }

    
     // Met à jour le désassemblage à partir du PC.
     
    private void refreshDisassembly() {
        disassembly.setText("");
        int pc = cpu.reg.PC & 0xFFFF;

        for (int i = 0; i < 6; i++) {
            int opcode = cpu.readByte(pc);
            String name = cpu.getInstructionSet().getName(opcode);
            String line = name;

            if ((opcode & 0xF0) == 0x80) {
                int operand = cpu.readByte(pc + 1);
                line = name + String.format(" #%02X", operand);
                disassembly.append(
                        String.format("%04X : %02X %02X  %s%n", pc, opcode, operand, line)
                );
                pc += 2;
            } else {
                disassembly.append(
                        String.format("%04X : %02X     %s%n", pc, opcode, line)
                );
                pc += 1;
            }
        }
    }

    
     //Retourne la taille d'une instruction selon son opcode.
     
    private int getInstructionLength(int opcode) {
        if ((opcode & 0xF0) == 0x80) return 2;
        if (opcode == 0x10 || opcode == 0x11) return 2;
        return 1;
    }
}
