package cpu;

import java.util.*;
import device.Keyboard6809;

// CPU Motorola 6809
public class CPU {

    // ===== COEUR =====
    public final Registers reg = new Registers(); // registres
    public final Memory mem = new Memory();       // mémoire
    private final InstructionSet iset = new InstructionSet(); // jeu d'instructions

    public boolean halted = false;                // état halt
    private boolean waitingForInterrupt = false;  // attente interruption

    // ===== IRQ =====
    private boolean irqPending = false;           // IRQ en attente

    // ===== PERIPHERIQUES =====
    private Keyboard6809 keyboard;                // clavier

    // ===== DEBUG =====
    private final List<Integer> breakpoints = new ArrayList<>(); // breakpoints
    private int instructionsExecuted = 0;         // compteur instructions
    private int lastPostByte = 0;                  // dernier postbyte
    private int lastEffectiveAddress = 0;          // dernière adresse effective

    // ===== CONSTRUCTEUR =====
    // Initialise le CPU
    public CPU() {
        reset();
    }

    // ===== ATTACHEMENTS =====
    // Attache le clavier
    public void attachKeyboard(Keyboard6809 kb) {
        this.keyboard = kb;
    }

    // ===== RESET =====
    // Réinitialise le CPU
    public void reset() {
        reg.reset();
        mem.clear();
        halted = false;
        waitingForInterrupt = false;
        instructionsExecuted = 0;
        breakpoints.clear();
        lastPostByte = 0;
        lastEffectiveAddress = 0;
        irqPending = false;
    }

    // ===== IRQ =====
    // Déclenche une IRQ si autorisée
    public void triggerIRQ() {
        if (!reg.getFlag(Registers.FLAG_I)) {
            irqPending = true;
        }
    }

    // ===== BREAKPOINTS =====
    // Ajoute un breakpoint
    public void addBreakpoint(int addr) {
        int a = addr & 0xFFFF;
        if (!breakpoints.contains(a)) breakpoints.add(a);
    }

    // Supprime un breakpoint
    public void removeBreakpoint(int addr) {
        breakpoints.remove(Integer.valueOf(addr & 0xFFFF));
    }

    // Retourne les breakpoints
    public List<Integer> getBreakpoints() {
        return new ArrayList<>(breakpoints);
    }

    // ===== FETCH =====
    // Lit un octet à PC
    public int fetchByte() {
        int b = readByte(reg.PC);
        reg.PC = (reg.PC + 1) & 0xFFFF;
        return b;
    }

    // Lit un mot 16 bits à PC
    public int fetchWord() {
        int hi = fetchByte();
        int lo = fetchByte();
        return ((hi << 8) | lo) & 0xFFFF;
    }

    // ===== MEMOIRE =====
    // Lecture octet mémoire
    public int readByte(int addr) {
        addr &= 0xFFFF;
        if (keyboard != null && (addr == 0xFF00 || addr == 0xFF01)) {
            return keyboard.read(addr);
        }
        return Byte.toUnsignedInt(mem.readByte(addr));
    }

    // Lecture mot 16 bits
    public int readWord(int addr) {
        int hi = readByte(addr);
        int lo = readByte(addr + 1);
        return ((hi << 8) | lo) & 0xFFFF;
    }

    // Écriture octet mémoire
    public void writeByte(int addr, int val) {
        mem.writeByte(addr & 0xFFFF, (byte) (val & 0xFF));
    }

    // Écriture mot 16 bits
    public void writeWord(int addr, int val) {
        writeByte(addr, (val >> 8) & 0xFF);
        writeByte(addr + 1, val & 0xFF);
    }

    // ===== PILE =====
    // Push octet
    public void pushStack(int v) {
        reg.SP = (reg.SP - 1) & 0xFFFF;
        mem.writeByte(reg.SP, (byte) (v & 0xFF));
    }

    // Pop octet
    public int popStack() {
        int v = Byte.toUnsignedInt(mem.readByte(reg.SP));
        reg.SP = (reg.SP + 1) & 0xFFFF;
        return v;
    }

    // Push mot
    public void pushWord(int v) {
        pushStack((v >> 8) & 0xFF);
        pushStack(v & 0xFF);
    }

    // Pop mot
    public int popWord() {
        int lo = popStack();
        int hi = popStack();
        return ((hi << 8) | lo) & 0xFFFF;
    }

    // ===== EXECUTION =====
    // Exécute une instruction
    public void step() {
        if (halted || waitingForInterrupt) return;

        if (irqPending) {
            handleIRQ();
            irqPending = false;
            return;
        }

        if (breakpoints.contains(reg.PC & 0xFFFF)) return;

        int opcode = fetchByte();
        InstructionSet.Instruction inst = iset.get(opcode);

        if (inst == null && opcode == 0x10)
            inst = iset.get(0x100 | fetchByte());
        else if (inst == null && opcode == 0x11)
            inst = iset.get(0x1100 | fetchByte());

        if (inst == null) {
            System.err.printf("Opcode inconnu %02X @ %04X%n",
                    opcode, (reg.PC - 1) & 0xFFFF);
            halted = true;
            return;
        }

        inst.execute(this);
        instructionsExecuted++;
    }

    // ===== IRQ HANDLER =====
    // Gère une interruption IRQ
    private void handleIRQ() {
        reg.setFlag(Registers.FLAG_E, true);

        pushWord(reg.PC);
        pushWord(reg.U);
        pushWord(reg.Y);
        pushWord(reg.X);
        pushStack(reg.DP);
        pushStack(reg.B);
        pushStack(reg.A);
        pushStack(reg.CC);

        reg.setFlag(Registers.FLAG_I, true);
        reg.PC = readWord(0xFFF8);
        halted = false;
    }

    // ===== DEBUG =====
    public int getInstructionsExecuted() {
        return instructionsExecuted;
    }

    public int getLastPostByte() {
        return lastPostByte;
    }

    public int getLastEffectiveAddress() {
        return lastEffectiveAddress;
    }

    public InstructionSet getInstructionSet() {
        return iset;
    }

    public void dumpState() {
        reg.dump();
    }

    // ===== ADRESSES =====
    public int calculateDirectAddress() {
        int offset = fetchByte();
        return ((reg.DP & 0xFF) << 8) | (offset & 0xFF);
    }

    public int calculateIndexedAddress() {
        int offset = (byte) fetchByte();
        return (reg.X + offset) & 0xFFFF;
    }

    public int calculateExtendedAddress() {
        return fetchWord();
    }

    // ===== MULTI PUSH / PULL =====
    public void pushMultiple(int mask) {
        if ((mask & 0x80) != 0) pushWord(reg.PC);
        if ((mask & 0x40) != 0) pushWord(reg.U);
        if ((mask & 0x20) != 0) pushWord(reg.Y);
        if ((mask & 0x10) != 0) pushWord(reg.X);
        if ((mask & 0x08) != 0) pushStack(reg.DP);
        if ((mask & 0x04) != 0) pushStack(reg.B);
        if ((mask & 0x02) != 0) pushStack(reg.A);
        if ((mask & 0x01) != 0) pushStack(reg.CC);
    }

    public void pullMultiple(int mask) {
        if ((mask & 0x01) != 0) reg.CC = popStack();
        if ((mask & 0x02) != 0) reg.A  = popStack();
        if ((mask & 0x04) != 0) reg.B  = popStack();
        if ((mask & 0x08) != 0) reg.DP = popStack();
        if ((mask & 0x10) != 0) reg.X  = popWord();
        if ((mask & 0x20) != 0) reg.Y  = popWord();
        if ((mask & 0x40) != 0) reg.U  = popWord();
        if ((mask & 0x80) != 0) reg.PC = popWord();
    }

    // ===== REGISTRES =====
    public void exchangeRegisters(int r1, int r2) {
        int v1 = getRegisterValue(r1);
        int v2 = getRegisterValue(r2);
        setRegisterValue(r1, v2);
        setRegisterValue(r2, v1);
    }

    public void transferRegisters(int src, int dst) {
        setRegisterValue(dst, getRegisterValue(src));
    }

    private int getRegisterValue(int code) {
        return switch (code) {
            case 0 -> reg.D();
            case 1 -> reg.X;
            case 2 -> reg.Y;
            case 3 -> reg.U;
            case 4 -> reg.SP;
            case 5 -> reg.PC;
            case 8 -> reg.A;
            case 9 -> reg.B;
            case 10 -> reg.CC;
            case 11 -> reg.DP;
            default -> 0;
        };
    }

    private void setRegisterValue(int code, int value) {
        switch (code) {
            case 0 -> reg.setD(value);
            case 1 -> reg.X = value;
            case 2 -> reg.Y = value;
            case 3 -> reg.U = value;
            case 4 -> reg.SP = value;
            case 5 -> reg.PC = value;
            case 8 -> reg.A = value;
            case 9 -> reg.B = value;
            case 10 -> reg.CC = value;
            case 11 -> reg.DP = value;
        }
    }
}
