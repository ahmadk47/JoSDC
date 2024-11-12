import java.util.ArrayList;
import java.util.HashMap;

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
        opcodes.put("sgt", "000000");
        opcodes.put("nor", "000000");
        opcodes.put("xor", "000000");
        opcodes.put("jr", "000000");
        opcodes.put("sll", "000000");
        opcodes.put("srl", "000000");
        opcodes.put("addi", "001000");
        opcodes.put("lw", "100011");
        opcodes.put("sw", "101011");
        opcodes.put("beq", "000100");
        opcodes.put("bne", "000101");
        opcodes.put("j", "000010");
        opcodes.put("jal", "000011");
        opcodes.put("ori", "001101");
        opcodes.put("xori", "010110");
        opcodes.put("andi", "001100");
        opcodes.put("slti", "001010");

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
            if (instruction.startsWith("bltz") || instruction.startsWith("bgez")) {
                machineCode.addAll(expandPseudoInstruction(instruction));
            } else {
                String assembled = assemble(instruction);
                machineCode.add(assembled);
                currentAddress += 1; // Increment by 1 for word-addressable memory
            }
        }

        return machineCode;
    }

    private ArrayList<String> expandPseudoInstruction(String instruction) throws Exception {
        ArrayList<String> expandedInstructions = new ArrayList<>();
        String[] parts = instruction.replace(",", "").split("\\s+");
        String pseudoOp = parts[0].toLowerCase();
        String rs = parts[1].substring(1);
        String label = parts[2];
        String rd1 = "1"; // Using $1 as temporary register

        if (pseudoOp.equals("bltz")) {
            // BLTZ (branch on less than zero):
            // Replaces with:
            // slt $1, $rs, $0
            // bne $1, $0, label
            expandedInstructions.add(assemble("slt $" + rd1 + ", $" + rs + ", $0"));
            expandedInstructions.add(assemble("bne $" + rd1 + ", $0" + ", " + label));
        } else if (pseudoOp.equals("bgez")) {
            // bgez (branch on greater than zero):
            // Replaces with:
            // slt $1, $rs, $0
            // beq $1, $0, label
            expandedInstructions.add(assemble("slt $" + rd1 + ", $" + rs + ", $0"));
            expandedInstructions.add(assemble("beq $" + rd1 + ", $0" + ", " + label));
        }

        // Update the current address by the number of instructions added
        currentAddress += expandedInstructions.size();
        return expandedInstructions;
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
            case "sgt":
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
            case "andi":
            case "slti":
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
            case "j":
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
                "addi $2, $0, -123", // $2 = -123
                "addi $3, $0, -1542", // $3 = -1542
                "addi $4, $0, 523", // $4 = 523
                "addi $5, $0, 892", // $5 = 892
                "addi $6, $0, 32767", // $6 = 32767
            
                "add $13, $6, $4", // $13 = $6 + $4 = 33290 --> overflow
            
                "addi $6, $0, 1", // $6 = 32768 --> overflow
            
                "sub $7, $5, $4", // $7 = $5 - $4 = 369
                "sub $8, $2, $3", // $8 = $2 - $3 = 1419
                "sub $9, $3, $5", // $9 = $3 - $5 = -2434
            
                "addi $10, $0, 32767", // $10 = 32767
                "and $11, $10, $0", // $11 = 0
            
                "addi $12, $0, 32767", // $12 = 32767
                "andi $12, $12, 0", // $12 = 0
            
                "or $14, $10, $0", // $14 = 32767
            
                "addi $15, $0, 32767", // $15 = 32767
            
                "nor $16, $10, $15", // $16 = -32768
            
                "xori $17, $15, 15123", // $17 = 32767 XOR 15123 = 17644
            
                "slt $18, $4, $5", // $18 = 1
                "slt $19, $5, $4", // $19 = 0
            
                "slt $22, $2, $3", // $22 = 0
                "slt $23, $3, $2", // $23 = 1
            
                "sgt $20, $4, $5", // $20 = 0
                "sgt $21, $5, $4", // $21 = 1
            
                "sgt $24, $2, $3", // $24 = 1
                "sgt $23, $3, $2", // $23 = 0
            
                "ori $15, $15, 0", // $15 = 32767
            
                "sll $29, $2, 2", // $29 = -492
                "addi $3, $0, 1542",
                "srl $30, $3, 2", // $30 = -385.5 --> -385
            
                "sw $15, 0($0)", // DM[0] = 32767
                "lw $24, 0($0)", // $24 = 32767
            
                "sw $3, 1($0)", // DM[1] = -1542
                "lw $25, 1($0)", // $25 = -1542
            
                "sw $2, 2($0)", // DM[2] = -123
                "lw $26, 2($0)", // $26 = -123
            
                "addi $27, $0, 5", // $27 = 5
            
                "LOOP:",
                "addi $28, $28, 1", // LOOP UNTIL $28 = $27 = 5
                "bne $27, $28, LOOP",
            
                "LOOP2:",
                "addi $28, $28, -1", // LOOP UNTIL $28 = 0
                "bne $28, $0, LOOP2",
            
                "addi $29, $0, 5",
            
                "jal SUBROUTINE",
                "j SKIP",
            
                "SUBROUTINE:",
                "addi $29, $29, -1",
                "bne $29, $0, SUBROUTINE",
                "jr $31",
            
                "SKIP:",
                "addi $30, $0, 25"
            };
            
            
            
            
            assembler.firstPass(program);

            ArrayList<String> machineCode = assembler.secondPass();

            System.out.println("WIDTH=32;");
            System.out.println("DEPTH=256;");
            System.out.println("ADDRESS_RADIX=UNS;");
            System.out.println("DATA_RADIX=BIN;");
            System.out.println("CONTENT BEGIN");

            int addr = 0;
            for (String code : machineCode) {
                System.out.printf("    %d   : %s;\n", addr++, code);
            }

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
