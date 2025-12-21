package app;

import cpu.Memory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


 // Classe responsable du chargement des programmes en mémoire du simulateur Motorola 6809.
 
public class BinaryLoader {

    
    // Charge un fichier binaire (.bin) dans la mémoire à partir  d'une adresse donnée.
     
    public static void loadFileIntoMemory(File file, Memory mem, int baseAddress) throws IOException {

        // Lecture du fichier binaire
        try (FileInputStream fis = new FileInputStream(file)) {

            byte[] buffer = fis.readAllBytes();

            // Copie des octets en mémoire de façon séquentielle
            for (int i = 0; i < buffer.length; i++) {
                mem.writeByte(baseAddress + i, buffer[i]);
            }

            // Affichage d'information de chargement
            System.out.printf(
                "Chargé %d octets à partir de %04Xh%n",
                buffer.length,
                baseAddress
            );
        }
    }

    
     // Charge un programme écrit sous forme hexadécimale directement en mémoire.
     
    public static void loadBinaryString(String hexString, Memory mem, int baseAddress) {

        // Séparation des octets hexadécimaux
        String[] hexBytes = hexString.trim().split("\\s+");

        for (int i = 0; i < hexBytes.length; i++) {
            // Conversion hexadécimale vers entier
            int value = Integer.parseInt(hexBytes[i], 16);

            // Écriture de l'octet en mémoire
            mem.writeByte(baseAddress + i, (byte) (value & 0xFF));
        }

        // Confirmation du chargement
        System.out.printf(
            "Chargé %d octets depuis une chaîne hexadécimale à %04Xh%n",
            hexBytes.length,
            baseAddress
        );
    }
}
