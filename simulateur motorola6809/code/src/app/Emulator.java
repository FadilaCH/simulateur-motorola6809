package app;

import cpu.CPU;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


  //Émulateur console du microprocesseur Motorola 6809. Permet de charger un programme, exécuter les instructions et gérer des breakpoints en mode texte.
 
public class Emulator {

    public static void main(String[] args) {
        CPU cpu = new CPU();
        Scanner sc = new Scanner(System.in);
        Set<Integer> breakpoints = new HashSet<>();

        System.out.println("=== EMULATEUR CONSOLE Motorola 6809 ===");
        System.out.println("1) Charger programme TEST");
        System.out.println("2) Charger un fichier .bin");
        System.out.println("3) Charger programme de démonstration");
        System.out.print("→ Choix : ");

        String choix = sc.nextLine().trim();
        switch (choix) {
            case "1":
                loadTestProgram(cpu);
                break;

            case "2":
                System.out.print("Chemin du fichier .bin : ");
                String path = sc.nextLine().trim();
                try {
                    File file = new File(path);
                    if (!file.exists()) {
                        System.out.println("✗ Fichier non trouvé");
                        return;
                    }
                    BinaryLoader.loadFileIntoMemory(file, cpu.mem, 0x8000);
                    cpu.reg.PC = 0x8000;
                    cpu.halted = false;
                    System.out.println("✓ Programme chargé (PC = 8000h)");
                } catch (Exception e) {
                    System.out.println("✗ Erreur : " + e.getMessage());
                    return;
                }
                break;

            case "3":
                loadDemoProgram(cpu);
                break;

            default:
                System.out.println("Choix invalide, chargement du programme TEST.");
                loadTestProgram(cpu);
                break;
        }

        boolean running = true;
        boolean continuous = false;
        int stepCount = 0;

        while (running) {

            if (cpu.halted) {
                System.out.println("\nCPU HALTED — fin de l'exécution");
                System.out.println("Instructions exécutées : " + stepCount);
                break;
            }

            if (!continuous && breakpoints.contains(cpu.reg.PC & 0xFFFF)) {
                System.out.println("\n=== BREAKPOINT @ PC=" +
                        String.format("%04X", cpu.reg.PC) + " ===");
                cpu.dumpState();
                if (!menuInteractif(cpu, sc, breakpoints)) break;
                continue;
            }

            int currentPC = cpu.reg.PC & 0xFFFF;
            int opcode = cpu.readByte(currentPC);
            String instName = cpu.getInstructionSet().getName(opcode);

            System.out.printf("\n[STEP %d] PC=%04X : %s (%02X)%n",
                    ++stepCount, currentPC, instName, opcode);

            cpu.step();
            cpu.dumpState();

            if (!continuous) {
                System.out.print("\n[n]ext  [c]ontinue  [b]reakpoint  [m]emory  [q]uit : ");
                String cmd = sc.nextLine().trim().toLowerCase();

                switch (cmd) {
                    case "n":
                    case "":
                        break;
                    case "c":
                        continuous = true;
                        System.out.println("Mode continu activé");
                        break;
                    case "b":
                        manageBreakpoints(sc, breakpoints, cpu);
                        break;
                    case "m":
                        editMemory(cpu, sc);
                        break;
                    case "q":
                        running = false;
                        break;
                    default:
                        System.out.println("Commande inconnue");
                }
            } else if (breakpoints.contains(cpu.reg.PC & 0xFFFF)) {
                continuous = false;
                System.out.println("\nBreakpoint atteint");
            }
        }

        sc.close();
        System.out.println("\nSimulation terminée");
        System.out.println("Total instructions exécutées : " + stepCount);
    }

    
      //Charge un petit programme de test en mémoire.
     
    private static void loadTestProgram(CPU cpu) {
        byte[] program = {
                (byte) 0x86, 0x05,   // LDA #05
                (byte) 0x8B, 0x03,   // ADDA #03
                (byte) 0x4C,         // INCA
                (byte) 0x12,         // NOP
                (byte) 0x13          // SYNC
        };

        int base = 0x8000;
        for (int i = 0; i < program.length; i++) {
            cpu.writeByte(base + i, Byte.toUnsignedInt(program[i]));
        }

        cpu.reg.PC = base;
        cpu.reg.SP = 0xFF00;
        cpu.halted = false;

        System.out.println("✓ Programme TEST chargé @ 8000h");
    }

    
     // Charge un programme de démonstration à partir d'une chaîne hexadécimale.
     
    private static void loadDemoProgram(CPU cpu) {
        String hexProgram =
                "86 10 8B 20 4C 80 05 C6 30 CC 01 23 86 00 8E 80 00 " +
                "9F 00 86 FF 4A 5C 12 12 12 13";

        BinaryLoader.loadBinaryString(hexProgram, cpu.mem, 0x8000);
        cpu.reg.PC = 0x8000;
        cpu.reg.SP = 0xFF00;
        cpu.halted = false;

        System.out.println("✓ Programme de démonstration chargé @ 8000h");
    }

    
     // Menu affiché lorsqu'un breakpoint est atteint.
     
    private static boolean menuInteractif(CPU cpu, Scanner sc, Set<Integer> breakpoints) {
        System.out.println("\nOptions :");
        System.out.println(" s : step");
        System.out.println(" c : continuer");
        System.out.println(" m : mémoire");
        System.out.println(" a : ajouter breakpoint");
        System.out.println(" d : supprimer breakpoint");
        System.out.println(" r : afficher registres");
        System.out.println(" q : quitter");
        System.out.print("→ Choix : ");

        String input = sc.nextLine().trim().toLowerCase();

        switch (input) {
            case "s":
                cpu.step();
                cpu.dumpState();
                return true;
            case "c":
                return true;
            case "m":
                editMemory(cpu, sc);
                return true;
            case "a":
                System.out.print("Adresse (hex) : ");
                try {
                    int addr = Integer.parseInt(sc.nextLine(), 16);
                    breakpoints.add(addr & 0xFFFF);
                    cpu.addBreakpoint(addr);
                    System.out.println("Breakpoint ajouté");
                } catch (NumberFormatException e) {
                    System.out.println("Adresse invalide");
                }
                return true;
            case "d":
                System.out.print("Adresse (hex) : ");
                try {
                    int addr = Integer.parseInt(sc.nextLine(), 16);
                    breakpoints.remove(addr & 0xFFFF);
                    cpu.removeBreakpoint(addr);
                    System.out.println("Breakpoint supprimé");
                } catch (NumberFormatException e) {
                    System.out.println("Adresse invalide");
                }
                return true;
            case "r":
                cpu.dumpState();
                return true;
            case "q":
                return false;
            default:
                System.out.println("Option inconnue");
                return true;
        }
    }

    
     // Menu de gestion des breakpoints.
     
    private static void manageBreakpoints(Scanner sc, Set<Integer> breakpoints, CPU cpu) {
        System.out.println("\n1) Ajouter");
        System.out.println("2) Supprimer");
        System.out.println("3) Lister");
        System.out.print("→ Choix : ");

        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("Adresse (hex) : ");
                try {
                    int addr = Integer.parseInt(sc.nextLine(), 16);
                    breakpoints.add(addr & 0xFFFF);
                    cpu.addBreakpoint(addr);
                    System.out.println("Breakpoint ajouté");
                } catch (NumberFormatException e) {
                    System.out.println("Adresse invalide");
                }
                break;

            case "2":
                System.out.print("Adresse (hex) : ");
                try {
                    int addr = Integer.parseInt(sc.nextLine(), 16);
                    breakpoints.remove(addr & 0xFFFF);
                    cpu.removeBreakpoint(addr);
                    System.out.println("Breakpoint supprimé");
                } catch (NumberFormatException e) {
                    System.out.println("Adresse invalide");
                }
                break;

            case "3":
                if (breakpoints.isEmpty()) {
                    System.out.println("(aucun breakpoint)");
                } else {
                    for (int bp : breakpoints) {
                        System.out.println(String.format("%04X", bp));
                    }
                }
                break;
        }
    }

    
     //Lecture et modification d'une case mémoire.
     
    private static void editMemory(CPU cpu, Scanner sc) {
        System.out.print("Adresse (hex) : ");
        try {
            int addr = Integer.parseInt(sc.nextLine(), 16);
            System.out.println("Valeur = " +
                    String.format("%02X", cpu.readByte(addr)));

            System.out.print("Nouvelle valeur (hex, vide = inchangé) : ");
            String valStr = sc.nextLine().trim();
            if (!valStr.isEmpty()) {
                int val = Integer.parseInt(valStr, 16);
                cpu.writeByte(addr, val);
                System.out.println("Mémoire mise à jour");
            }
        } catch (NumberFormatException e) {
            System.out.println("Format invalide");
        }
    }
}
