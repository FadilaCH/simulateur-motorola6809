package cpu;

import java.util.HashMap;
import java.util.Map;

// Jeu d'instructions du Motorola 6809
public class InstructionSet {

    // Interface fonctionnelle pour une instruction
    @FunctionalInterface
    public interface Instruction {
        void execute(CPU cpu);
    }

    private final Map<Integer, Instruction> instructions = new HashMap<>(); // opcode -> instruction
    private final Map<Integer, String> names = new HashMap<>();// opcode -> nom

    // Constructeur
    public InstructionSet() {
        loadAllInstructions();
    }

    // Retourne l'instruction associée à un opcode
    public Instruction get(int opcode) {
        return instructions.get(opcode);
    }

    // Retourne le nom de l'instruction
    public String getName(int opcode) {
        return names.getOrDefault(opcode, "UNKNOWN");
    }

    //  FLAGS 
    // Met à jour N et Z pour une valeur 8 bits
    private void setNZ8(CPU cpu, int val) {
        int v = val & 0xFF;
        cpu.reg.setFlag(Registers.FLAG_N, (v & 0x80) != 0);
        cpu.reg.setFlag(Registers.FLAG_Z, v == 0);
    }

    // Met à jour N et Z pour une valeur 16 bits
    private void setNZ16(CPU cpu, int val) {
        int v = val & 0xFFFF;
        cpu.reg.setFlag(Registers.FLAG_N, (v & 0x8000) != 0);
        cpu.reg.setFlag(Registers.FLAG_Z, v == 0);
    }

    // Flags pour addition 8 bits
    private void setNZVC8_add(CPU cpu, int a, int operand, int result) {
        int res8 = result & 0xFF;
        cpu.reg.setFlag(Registers.FLAG_N, (res8 & 0x80) != 0);
        cpu.reg.setFlag(Registers.FLAG_Z, res8 == 0);
        cpu.reg.setFlag(Registers.FLAG_C, (result & 0x100) != 0);
        boolean v = (((a ^ operand) & 0x80) == 0) && (((a ^ res8) & 0x80) != 0);
        cpu.reg.setFlag(Registers.FLAG_V, v);
        cpu.reg.setFlag(Registers.FLAG_H, ((a ^ operand ^ res8) & 0x10) != 0);
    }

    // Flags pour soustraction 8 bits
    private void setNZVC8_sub(CPU cpu, int a, int operand, int result) {
        int res8 = result & 0xFF;
        cpu.reg.setFlag(Registers.FLAG_N, (res8 & 0x80) != 0);
        cpu.reg.setFlag(Registers.FLAG_Z, res8 == 0);
        cpu.reg.setFlag(Registers.FLAG_C, (result & 0x100) != 0);
        boolean v = (((a ^ operand) & 0x80) != 0) && (((a ^ res8) & 0x80) != 0);
        cpu.reg.setFlag(Registers.FLAG_V, v);
    }

    //  CHARGEMENT DES INSTRUCTIONS 
    private void loadAllInstructions() {
        loadArithmeticInstructions();
        loadShiftInstructions();
        loadLogicalInstructions();
        loadIncDecInstructions();
        loadLoadStoreInstructions();
        loadStackInstructions();
        loadRegisterTransfers();
        loadBranchJumpInstructions();
        loadPointerInstructions();
        loadInterruptInstructions();
        System.out.println("InstructionSet: " + instructions.size() + " instructions chargées");
    }

    // Ajoute une instruction
    private void add(int opcode, String name, Instruction inst) {
        instructions.put(opcode, inst);
        names.put(opcode, name);
    }

    //  ARITHMETIQUE 
    private void loadArithmeticInstructions() {

        add(0x8B, "ADDA #", cpu -> { 
            int imm = cpu.fetchByte();
            int a = cpu.reg.A & 0xFF;
            int r = a + imm;
            cpu.reg.A = r & 0xFF;
            setNZVC8_add(cpu, a, imm, r);
        });

        add(0xCB, "ADDB #", cpu -> { 
            int imm = cpu.fetchByte();
            int b = cpu.reg.B & 0xFF;
            int r = b + imm;
            cpu.reg.B = r & 0xFF;
            setNZVC8_add(cpu, b, imm, r);
        });

        add(0x80, "SUBA #", cpu -> { // A = A - imm
            int imm = cpu.fetchByte();
            int a = cpu.reg.A & 0xFF;
            int r = a - imm;
            cpu.reg.A = r & 0xFF;
            setNZVC8_sub(cpu, a, imm, r);
        });

        add(0x3D, "MUL", cpu -> { // A * B -> D
            int r = (cpu.reg.A & 0xFF) * (cpu.reg.B & 0xFF);
            cpu.reg.setD(r & 0xFFFF);
            cpu.reg.setFlag(Registers.FLAG_C, (r & 0x80) != 0);
            setNZ16(cpu, cpu.reg.D());
        });
    }

    // ===== SHIFTS / ROTATIONS =====
    private void loadShiftInstructions() {

        add(0x4C, "INCA", cpu -> { // A++
            cpu.reg.A = (cpu.reg.A + 1) & 0xFF;
            setNZ8(cpu, cpu.reg.A);
        });

        add(0x4A, "DECA", cpu -> { // A--
            cpu.reg.A = (cpu.reg.A - 1) & 0xFF;
            setNZ8(cpu, cpu.reg.A);
        });
    }

    // ===== LOGIQUE =====
    private void loadLogicalInstructions() {

        add(0x88, "EORA #", cpu -> { // A ^= imm
            cpu.reg.A ^= cpu.fetchByte();
            setNZ8(cpu, cpu.reg.A);
        });

        add(0x8A, "ORA #", cpu -> { // A |= imm
            cpu.reg.A |= cpu.fetchByte();
            setNZ8(cpu, cpu.reg.A);
        });
    }

    // ===== INC / DEC =====
    private void loadIncDecInstructions() {

        add(0x4F, "CLRA", cpu -> { // A = 0
            cpu.reg.A = 0;
            setNZ8(cpu, 0);
        });

        add(0x12, "NOP", cpu -> { /* rien */ });
    }

    // ===== LOAD / STORE =====
    private void loadLoadStoreInstructions() {

        add(0x86, "LDA #", cpu -> { // A = imm
            cpu.reg.A = cpu.fetchByte() & 0xFF;
            setNZ8(cpu, cpu.reg.A);
        });

        add(0x97, "STA direct", cpu -> { // mem = A
            int addr = cpu.calculateDirectAddress();
            cpu.writeByte(addr, cpu.reg.A);
            setNZ8(cpu, cpu.reg.A);
        });
    }

    // ===== PILE =====
    private void loadStackInstructions() {

        add(0x34, "PSHS", cpu -> { // push multiple
            cpu.pushMultiple(cpu.fetchByte());
        });

        add(0x35, "PULS", cpu -> { // pull multiple
            cpu.pullMultiple(cpu.fetchByte());
        });
    }

    // ===== TRANSFERT REGISTRES =====
    private void loadRegisterTransfers() {

        add(0x1E, "EXG", cpu -> { // échange registres
            int spec = cpu.fetchByte();
            cpu.exchangeRegisters((spec >> 4) & 0x0F, spec & 0x0F);
        });

        add(0x1F, "TFR", cpu -> { // transfert registres
            int spec = cpu.fetchByte();
            cpu.transferRegisters((spec >> 4) & 0x0F, spec & 0x0F);
        });
    }

    // ===== BRANCHES =====
    private void loadBranchJumpInstructions() {

        add(0x20, "BRA", cpu -> { // branche toujours
            cpu.reg.PC = (cpu.reg.PC + (byte) cpu.fetchByte()) & 0xFFFF;
        });

        add(0x39, "RTS", cpu -> { // retour sous-programme
            cpu.reg.PC = cpu.popWord();
        });
    }

    // ===== POINTEURS =====
    private void loadPointerInstructions() {

        add(0x30, "LEAX", cpu -> { // X = adresse indexée
            cpu.reg.X = cpu.calculateIndexedAddress();
            cpu.reg.setFlag(Registers.FLAG_Z, cpu.reg.X == 0);
        });
    }

    // ===== INTERRUPTIONS =====
    private void loadInterruptInstructions() {

        add(0x13, "SYNC", cpu -> { // halt
            cpu.halted = true;
        });

        add(0x3B, "RTI", cpu -> { // retour interruption
            cpu.reg.CC = cpu.popStack();
            cpu.reg.A = cpu.popStack();
            cpu.reg.B = cpu.popStack();
            cpu.reg.DP = cpu.popStack();
            cpu.reg.X = cpu.popWord();
            cpu.reg.Y = cpu.popWord();
            cpu.reg.U = cpu.popWord();
            cpu.reg.PC = cpu.popWord();
        });
    }
}
