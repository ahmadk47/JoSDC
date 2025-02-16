import java.util.*;
public class AssemblyPipeline {
    // Simulate MIPS execution to track register values
    private static int[] registers = new int[32];
    
    private static void simulateExecution(String[] program) {
        // Reset registers
        registers = new int[32];
        
        for (String inst : program) {
            inst = inst.trim();
            if (inst.endsWith(":") || inst.isEmpty()) continue;
            
            String[] parts = inst.replace(",", "").split("\\s+");
            String op = parts[0].toLowerCase();
            
            switch (op) {
                case "ori":
                    int rt = Integer.parseInt(parts[1].substring(1));
                    int rs = Integer.parseInt(parts[2].substring(1));
                    int imm = parts[3].toLowerCase().startsWith("0x") ? 
                             Integer.parseInt(parts[3].substring(2), 16) : 
                             Integer.parseInt(parts[3]);
                    registers[rt] = registers[rs] | imm;
                    break;
                    
                case "add":
                    int rd = Integer.parseInt(parts[1].substring(1));
                    rs = Integer.parseInt(parts[2].substring(1));
                    rt = Integer.parseInt(parts[3].substring(1));
                    registers[rd] = registers[rs] + registers[rt];
                    break;
                    
                case "addi":
                    rt = Integer.parseInt(parts[1].substring(1));
                    rs = Integer.parseInt(parts[2].substring(1));
                    imm = Integer.parseInt(parts[3]);
                    registers[rt] = registers[rs] + imm;
                    break;
                    
                case "slt":
                    rd = Integer.parseInt(parts[1].substring(1));
                    rs = Integer.parseInt(parts[2].substring(1));
                    rt = Integer.parseInt(parts[3].substring(1));
                    registers[rd] = registers[rs] < registers[rt] ? 1 : 0;
                    break;
            }
        }
    }
    public static ArrayList<String> getScheduledProgram(ArrayList<String> scheduledProgram){
        return scheduledProgram;
    }
    public static ArrayList<String> getOriginalProgram(ArrayList<String> originalProgram){
        return originalProgram;
    }

    public static void main(String[] args) {
        try {
            // Create instances
            InstructionScheduler scheduler = new InstructionScheduler();
            Assembler assembler = new Assembler();

            // Original program defined in scheduler's main
            String[] originalProgram = scheduler.getProgram1();
            
            // Simulate original program execution
            System.out.println("Original Program Execution:");
            simulateExecution(originalProgram);
           

            // Get scheduled program
            String[] scheduledProgram = scheduler.schedule(originalProgram);
            
            // Simulate scheduled program execution
            System.out.println("\nScheduled Program Execution:");
            simulateExecution(scheduledProgram);
           
            
            // Generate machine code for both versions
            assembler.firstPass(originalProgram);
            ArrayList<String> originalMachineCode = assembler.secondPass();
            assembler.reset();

            assembler.firstPass(scheduledProgram);
            ArrayList<String> scheduledMachineCode = assembler.secondPass();

            System.out.println("\nProgram size comparison:");
            System.out.println("Original program instructions: " + originalProgram.length);
            System.out.println("Scheduled program instructions: " + scheduledProgram.length);
            System.out.println("Original machine code size: " + originalMachineCode.size());
            System.out.println("Scheduled machine code size: " + scheduledMachineCode.size());

            // System.out.println("Label Addresses:");
            // for (String label : assembler.labelAddresses.keySet()) {
            //     int address = assembler.getLabelAddress(label);
            //     System.out.printf("Label %s : Address %d\n", label, address);
            // }
            
            System.out.println("*****************************************************");
            System.out.println("WIDTH=32;");
            System.out.println("DEPTH=2048;");
            System.out.println("ADDRESS_RADIX=UNS;");
            System.out.println("DATA_RADIX=BIN;");
            System.out.println("CONTENT BEGIN");

            int addr = 0;
            for (String code : originalMachineCode) {
                System.out.printf("    %d   : %s;\n", addr++, code);
            }

            if (addr < 2048) {
                System.out.printf("    [%d..2047] : %s;  -- fill the rest with zeros\n",
                        addr, "00000000000000000000000000000000");
            }

            System.out.println("END;");

            System.out.println("*********************Modified Program********************");

            System.out.println("*****************************************************");
            System.out.println("WIDTH=32;");
            System.out.println("DEPTH=2048;");
            System.out.println("ADDRESS_RADIX=UNS;");
            System.out.println("DATA_RADIX=BIN;");
            System.out.println("CONTENT BEGIN");


            int addr1 = 0;
            for (String code : scheduledMachineCode) {
                System.out.printf("    %d   : %s;\n", addr1++, code);
            }

            if (addr1 < 2048) {
                System.out.printf("    [%d..2047] : %s;  -- fill the rest with zeros\n",
                        addr1, "00000000000000000000000000000000");
            }

            System.out.println("END;");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}