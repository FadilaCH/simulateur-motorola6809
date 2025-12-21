package cpu;

public class Registers {

    // ===== REGISTRES DU CPU =====
    public int PC;   // Compteur de programme (16 bits)
    public int A;    // Accumulateur A (8 bits)
    public int B;    // Accumulateur B (8 bits)
    public int X;    // Registre index X (16 bits)
    public int Y;    // Registre index Y (16 bits)
    public int SP;   // Pointeur de pile système S (16 bits)
    public int U;    // Pointeur de pile utilisateur U (16 bits)
    public int CC;   // Registre des flags (8 bits)
    public int DP;   // Registre Direct Page (8 bits)

    // ===== FLAGS DU REGISTRE CC =====
    public static final int FLAG_E = 0x80;  // Mode entier
    public static final int FLAG_F = 0x40;  // Masque FIRQ
    public static final int FLAG_H = 0x20;  // Demi-retenue
    public static final int FLAG_I = 0x10;  // Masque IRQ
    public static final int FLAG_N = 0x08;  // Négatif
    public static final int FLAG_Z = 0x04;  // Zéro
    public static final int FLAG_V = 0x02;  // Overflow
    public static final int FLAG_C = 0x01;  // Carry

    // Retourne le registre D (A et B combinés)
    public int D() {
        return ((A & 0xFF) << 8) | (B & 0xFF);
    }

    // Définit le registre D dans A et B
    public void setD(int val) {
        A = (val >> 8) & 0xFF;
        B = val & 0xFF;
    }

    // Vérifie si un flag est actif
    public boolean getFlag(int f) {
        return (CC & f) != 0;
    }

    // Active ou désactive un flag
    public void setFlag(int f, boolean val) {
        if (val) CC |= f;
        else CC &= ~f;
    }

    // Efface tous les flags
    public void clearFlags() {
        CC = 0;
    }

    // Retourne les flags sous forme texte
    public String flagsToString() {
        return String.format(
            "E=%d F=%d H=%d I=%d N=%d Z=%d V=%d C=%d",
            getFlag(FLAG_E) ? 1 : 0,
            getFlag(FLAG_F) ? 1 : 0,
            getFlag(FLAG_H) ? 1 : 0,
            getFlag(FLAG_I) ? 1 : 0,
            getFlag(FLAG_N) ? 1 : 0,
            getFlag(FLAG_Z) ? 1 : 0,
            getFlag(FLAG_V) ? 1 : 0,
            getFlag(FLAG_C) ? 1 : 0
        );
    }

    // Affiche tous les registres (debug)
    public void dump() {
        System.out.printf(
            "PC=%04X  A=%02X  B=%02X  X=%04X  Y=%04X  SP=%04X  U=%04X  DP=%02X  CC=%02X  [%s]%n",
            PC & 0xFFFF, A & 0xFF, B & 0xFF,
            X & 0xFFFF, Y & 0xFFFF,
            SP & 0xFFFF, U & 0xFFFF,
            DP & 0xFF, CC & 0xFF,
            flagsToString()
        );
    }

    // Réinitialise tous les registres
    public void reset() {
        PC = 0x0000;
        A = 0;
        B = 0;
        X = 0;
        Y = 0;
        SP = 0xFFFE;
        U = 0xFFFE;
        DP = 0x00;
        CC = 0x00;
    }
}
