import java.util.HashMap;
import java.util.ArrayList;

public class Assembler {
    private HashMap<String, String> opcodes;
    private HashMap<String, String> functCodes;
    private HashMap<String, Integer> labelAddresses;
    private int currentAddress;
    private ArrayList<String> instructions;

    public Assembler() {
        init();
        reset();
    }

    public void reset() {
        labelAddresses = new HashMap<>();
        currentAddress = 0;
        instructions = new ArrayList<>();
    }

    public void init() {
        // Existing opcode and functCode initialization remains the same
        opcodes = new HashMap<>();
        opcodes.put("add", "000000");
        opcodes.put("sub", "000000");
        opcodes.put("and", "000000");
        opcodes.put("or", "000000");
        opcodes.put("slt", "000000");
        opcodes.put("addi", "001000");
        opcodes.put("lw", "100011");
        opcodes.put("sw", "101011");
        opcodes.put("beq", "000100");
        opcodes.put("bne", "000101");
        opcodes.put("jal", "000011");
        opcodes.put("ori", "001101");
        opcodes.put("xori", "010110");

        functCodes = new HashMap<>();
        functCodes.put("add", "100000");
        functCodes.put("sub", "100010");
        functCodes.put("and", "100100");
        functCodes.put("or", "100101");
        functCodes.put("slt", "101010");
        functCodes.put("sgt", "010100");
        functCodes.put("sll", "000000");
        functCodes.put("srl", "000010");
        functCodes.put("nor", "100111");
        functCodes.put("xor", "010101");
        functCodes.put("jr", "001000");
    }

    // First pass: collect all labels and their addresses
    public void firstPass(String[] programLines) {
        currentAddress = 0;
        for (String line : programLines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Check if line contains a label
            if (line.contains(":")) {
                String[] labelParts = line.split(":", 2);
                String label = labelParts[0].trim();
                labelAddresses.put(label, currentAddress);

                // If there's an instruction after the label, store it
                if (labelParts.length > 1 && !labelParts[1].trim().isEmpty()) {
                    instructions.add(labelParts[1].trim());
                    currentAddress += 1; // Increment by 1 for word-addressable memory
                }
            } else {
                instructions.add(line);
                currentAddress += 1; // Increment by 1 for word-addressable memory
            }
        }
    }

    // Second pass: assemble instructions with resolved labels
    public ArrayList<String> secondPass() throws Exception {
        ArrayList<String> machineCode = new ArrayList<>();
        currentAddress = 0;

        for (String instruction : instructions) {
            String assembled = assemble(instruction);
            machineCode.add(assembled);
            currentAddress += 1; // Increment by 1 for word-addressable memory
        }

        return machineCode;
    }

    private int calculateBranchOffset(String label) throws Exception {
        if (!labelAddresses.containsKey(label)) {
            throw new Exception("Undefined label: " + label);
        }
        // Calculate relative offset in words
        // Subtract current address + 1 (to account for branch delay slot)
        return (labelAddresses.get(label) - (currentAddress + 1));
    }

    public String assemble(String instructionLine) throws Exception {
        String[] parts = instructionLine.replace(",", "").split("\\s+");
        String instruction = parts[0].toLowerCase();

        if (!opcodes.containsKey(instruction)) {
            throw new Exception("Instruction " + instruction + " is not supported.");
        }

        switch (instruction) {
            case "add":
            case "sub":
            case "and":
            case "or":
            case "slt":
            case "nor":
            case "xor":
                if (parts.length != 4) {
                    throw new Exception(
                            "Invalid format for " + instruction + ". Expected: " + instruction + " $rd, $rs, $rt");
                }
                String rd = parts[1].substring(1);
                String rs = parts[2].substring(1);
                String rt = parts[3].substring(1);
                return assembleRType(instruction, rs, rt, rd);

            case "sll":
            case "srl":
                if (parts.length != 4) {
                    throw new Exception(
                            "Invalid format for " + instruction + ". Expected: " + instruction + " $rd, $rt, shamt");
                }
                String rdShift = parts[1].substring(1);
                String rtShift = parts[2].substring(1);
                int shamt = parseImmediate(parts[3]);
                return assembleRTypeShamt(instruction, rtShift, rdShift, shamt);

            case "addi":
            case "ori":
            case "xori":
                if (parts.length != 4) {
                    throw new Exception("Invalid format for " + instruction + ". Expected: " + instruction
                            + " $rt, $rs, immediate");
                }
                String rt2 = parts[1].substring(1);
                String rs2 = parts[2].substring(1);
                int immediate = parseImmediate(parts[3]);
                return assembleIType(instruction, rs2, rt2, immediate);

            case "beq":
            case "bne":
                if (parts.length != 4) {
                    throw new Exception(
                            "Invalid format for " + instruction + ". Expected: " + instruction + " $rs, $rt, label");
                }
                String rs3 = parts[1].substring(1);
                String rt3 = parts[2].substring(1);
                int offset = calculateBranchOffset(parts[3]);
                return assembleIType(instruction, rs3, rt3, offset);

            case "lw":
            case "sw":
                if (parts.length != 3) {
                    throw new Exception(
                            "Invalid format for " + instruction + ". Expected: " + instruction + " $rt, offset($rs)");
                }
                String rt4 = parts[1].substring(1);
                String[] offsetAndReg = parts[2].split("\\(");
                if (offsetAndReg.length != 2) {
                    throw new Exception("Invalid memory access format for " + instruction);
                }
                int offset2 = parseImmediate(offsetAndReg[0]);
                String rs4 = offsetAndReg[1].substring(1, offsetAndReg[1].length() - 1);
                return assembleIType(instruction, rs4, rt4, offset2);

            case "jal":
                if (parts.length != 2) {
                    throw new Exception("Invalid format for " + instruction + ". Expected: " + instruction + " label");
                }
                if (!labelAddresses.containsKey(parts[1])) {
                    throw new Exception("Undefined label: " + parts[1]);
                }
                // For J-type instructions, we use the absolute address directly
                int address = labelAddresses.get(parts[1]);
                return assembleJType(instruction, address);

            case "jr":
                if (parts.length != 2) {
                    throw new Exception("Invalid format for jr. Expected: jr $rs");
                }
                String rsJr = parts[1].substring(1);
                return assembleRType(instruction, rsJr, "0", "0");

            default:
                throw new Exception("Instruction " + instruction + " is not supported.");
        }
    }

    // Existing helper methods remain the same
    public String regToBinary(String reg) {
        int regNumber = Integer.parseInt(reg);
        return toBinary(regNumber, 5);
    }

    public String toBinary(int value, int bits) {
        String binaryString = Integer.toBinaryString(value);
        if (binaryString.length() > bits) {
            return binaryString.substring(binaryString.length() - bits);
        }
        return String.format("%" + bits + "s", binaryString).replace(' ', '0');
    }

    public String assembleRType(String instruction, String rs, String rt, String rd) {
        String opcode = opcodes.get(instruction);
        String funct = functCodes.get(instruction);
        return opcode + regToBinary(rs) + regToBinary(rt) + regToBinary(rd) + "00000" + funct;
    }

    public String assembleRTypeShamt(String instruction, String rt, String rd, int shamt) {
        String opcode = opcodes.get(instruction);
        String funct = functCodes.get(instruction);
        return opcode + "00000" + regToBinary(rt) + regToBinary(rd) + toBinary(shamt, 5) + funct;
    }

    public String assembleIType(String instruction, String rs, String rt, int immediate) {
        String opcode = opcodes.get(instruction);
        return opcode + regToBinary(rs) + regToBinary(rt) + toBinary(immediate, 16);
    }

    public String assembleJType(String instruction, int address) {
        String opcode = opcodes.get(instruction);
        return opcode + toBinary(address, 26);
    }

    private int parseImmediate(String value) {
        value = value.toLowerCase().trim();
        if (value.startsWith("0x")) {
            return Integer.parseInt(value.substring(2), 16);
        } else {
            return Integer.parseInt(value);
        }
    }

    public static void main(String[] args) {
        try {
            Assembler assembler = new Assembler();
            String[] program = {
                    "addi $1, $0, 5",
                    "addi $2, $0, 0",
                    "addi $3, $0, 1",
                    "loop:",
                    "add  $2, $2, $1",
                    "sub  $1, $1, $3",
                    "beq  $1, $0, store",
                    "beq  $0, $0, loop",
                    "store:",
                    "sw $2, 0($0)",
                    "done:",
                    "addi $4, $2, 0",
                    "beq  $0, $0, exit",
                    "exit:"
            };

            // First pass to collect labels
            assembler.firstPass(program);

            // Generate machine code
            ArrayList<String> machineCode = assembler.secondPass();

            // Print MIF format header
            System.out.println("WIDTH=32;");
            System.out.println("DEPTH=256;");
            System.out.println("ADDRESS_RADIX=UNS;");
            System.out.println("DATA_RADIX=BIN;");
            System.out.println("CONTENT BEGIN");

            // Print memory contents
            int addr = 0;
            for (String code : machineCode) {
                System.out.printf("    %d   : %s;\n", addr++, code);
            }

            // Fill remaining memory locations with zeros using range notation
            if (addr < 256) {
                System.out.printf("    [%d..255] : %s;  -- fill the rest with zeros\n",
                        addr, "00000000000000000000000000000000");
            }

            System.out.println("END;");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getLabelAddress(String label) {
        return labelAddresses.getOrDefault(label, -1);
    }
}
