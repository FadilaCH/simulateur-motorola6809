package cpu;

// Modes d’adressage du microprocesseur Motorola 6809
public enum AddressingMode {

    IMMEDIATE,          // valeur immédiate : LDA #$12
    DIRECT,             // adresse directe (page 0 : $00-$FF)
    EXTENDED,           // adresse 16 bits ($0000-$FFFF)
    INDEXED,            // registre index + déplacement
    INDEXED_INDIRECT,   // adresse indirecte via registre index
    RELATIVE,           // déplacement relatif au PC
    INHERENT,           // pas d’opérande (ex : NOP)
    ACCUMULATOR,        // registre A, B ou D
    IMMEDIATE_16,       // valeur immédiate 16 bits
    DIRECT_INDEXED,     // adresse directe indexée (ex : $00,X)
    EXTENDED_INDEXED,   // adresse étendue indexée
    RELATIVE_16,        // branchement relatif 16 bits
    STACK_RELATIVE,     // relatif au pointeur de pile S
    USER_STACK_RELATIVE // relatif au pointeur de pile U
}
