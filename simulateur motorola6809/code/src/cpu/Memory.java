package cpu;

// Mémoire du CPU Motorola 6809 (64 Ko)
public class Memory {

    // Tableau mémoire : 65536 octets
    private final byte[] mem = new byte[65536];

    // Lit un octet à une adresse 16 bits
    public byte readByte(int address) {
        return mem[address & 0xFFFF]; // reste dans 0–65535
    }

    // Écrit un octet à une adresse
    public void writeByte(int address, byte value) {
        mem[address & 0xFFFF] = value;
    }

    // Lit un mot 16 bits (2 octets)
    public int readWord(int address) {
        int hi = Byte.toUnsignedInt(readByte(address));                 // octet fort
        int lo = Byte.toUnsignedInt(readByte((address + 1) & 0xFFFF)); // octet faible
        return (hi << 8) | lo; // combine en 16 bits
    }

    // Écrit un mot 16 bits (2 octets)
    public void writeWord(int address, int value) {
        writeByte(address, (byte) ((value >> 8) & 0xFF));              // octet fort
        writeByte((address + 1) & 0xFFFF, (byte) (value & 0xFF));      // octet faible
    }

    // Met toute la mémoire à zéro
    public void clear() {
        java.util.Arrays.fill(mem, (byte) 0);
    }

    // Retourne la taille mémoire
    public int size() {
        return mem.length;
    }
}
