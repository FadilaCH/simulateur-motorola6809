package app;

import cpu.Memory;
import javax.swing.table.AbstractTableModel;

// Affiche la mémoire du CPU 6809 dans un tableau
// Chaque ligne = 16 octets, première colonne = adresse
public class MemoryTableModel extends AbstractTableModel {

    private final Memory mem; // mémoire du CPU

    // Constructeur : prend la mémoire à afficher
    public MemoryTableModel(Memory mem) {
        this.mem = mem;
    }

    // Nombre de lignes = taille mémoire / 16
    @Override
    public int getRowCount() {
        return mem.size() / 16;
    }

    // Nombre de colonnes = 1 adresse + 16 octets
    @Override
    public int getColumnCount() {
        return 17;
    }

    // Noms des colonnes : 0 = Addr, 1-16 = 0-F
    @Override
    public String getColumnName(int col) {
        if (col == 0) return "Addr";
        return String.format("%X", col - 1);
    }

    // Valeur à afficher : adresse ou octet mémoire en hex
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int base = rowIndex * 16; // adresse de départ de la ligne
        if (columnIndex == 0) return String.format("%04X", base); // colonne adresse
        int addr = base + (columnIndex - 1); // adresse mémoire
        int v = Byte.toUnsignedInt(mem.readByte(addr)); // lire octet
        return String.format("%02X", v); // affichage hex
    }

    // Colonnes éditables : seules les colonnes mémoire
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    // Met à jour la mémoire si l'utilisateur change une valeur
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex <= 0) return; // ignore adresse
        try {
            int addr = rowIndex * 16 + (columnIndex - 1); // calcul adresse
            int val = Integer.parseInt(aValue.toString(), 16) & 0xFF; // hex -> byte
            mem.writeByte(addr, (byte) val); // écrire en mémoire
        } catch (NumberFormatException ignored) {} // ignore erreur saisie
    }
}
