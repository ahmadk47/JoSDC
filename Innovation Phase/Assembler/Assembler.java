import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    private HashMap<String, String> opcodes;
    private HashMap<String, String> functCodes;
    private HashMap<String, Integer> labelAddresses;
    private int currentAddress;
    private ArrayList<String> instructions;
    private ArrayList<String> rescheduledInstructions;
    private HashMap<String, String> registerMap;

    public Assembler() {
        init();
        reset();
    }

    public void reset() {
        labelAddresses = new HashMap<>();
        currentAddress = 0;
        instructions = new ArrayList<>();
        rescheduledInstructions = new ArrayList<>();
        registerMap = new HashMap<>();
    }

    public void init() {
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
        opcodes.put("nop", "000000");

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
        functCodes.put("nop", "000000");
    }

    public void firstPass(String[] programLines) {
        currentAddress = 0;
        for (String line : programLines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.contains(":")) {
                String[] labelParts = line.split(":", 2);
                String label = labelParts[0].trim();
                labelAddresses.put(label, currentAddress);

                if (labelParts.length > 1 && !labelParts[1].trim().isEmpty()) {
                    String instruction = labelParts[1].trim();
                    instructions.add(instruction);
                    currentAddress += 1;
                }
            } else {
                instructions.add(line);
                currentAddress += 1;
            }
        }
    }

    public ArrayList<String> secondPass() throws Exception {
        ArrayList<String> machineCode = new ArrayList<>();
        rescheduledInstructions = new ArrayList<>();
        currentAddress = 0;

        // Apply loop unrolling
        ArrayList<String> unrolledInstructions = unrollLoops(instructions);

        // Apply register renaming
        ArrayList<String> renamedInstructions = renameRegisters(unrolledInstructions);

        // Reschedule instructions to minimize dependencies
        for (int i = 0; i < renamedInstructions.size(); i++) {
            String currentInstruction = renamedInstructions.get(i);

            if (i + 1 < renamedInstructions.size()) {
                String nextInstruction = renamedInstructions.get(i + 1);

                if (areIndependent(currentInstruction, nextInstruction)) {
                    rescheduledInstructions.add(currentInstruction);
                    rescheduledInstructions.add(nextInstruction);
                    i++;
                } else {
                    rescheduledInstructions.add(currentInstruction);
                    rescheduledInstructions.add("nop");
                }
            } else {
                rescheduledInstructions.add(currentInstruction);
                rescheduledInstructions.add("nop");
            }
        }

        // Assemble the rescheduled instructions
        for (String instruction : rescheduledInstructions) {
            if (instruction.startsWith("bltz") || instruction.startsWith("bgez")
                    || instruction.startsWith("BLTZ") || instruction.startsWith("BGEZ")) {
                ArrayList<String> expandedCode = expandPseudoInstruction(instruction, true);
                machineCode.addAll(expandedCode);
            } else {
                String assembled = assemble(instruction, false);
                machineCode.add(assembled);
                currentAddress += 1;
            }
        }

        return machineCode;
    }

    private ArrayList<String> unrollLoops(ArrayList<String> instructions) {
        ArrayList<String> unrolled = new ArrayList<>();
        int unrollFactor = 2;
    
        for (int i = 0; i < instructions.size(); i++) {
            String currentInstruction = instructions.get(i);
    
            if (currentInstruction.contains("BEQ") || currentInstruction.contains("BNE")) {
                String loopControlReg = extractLoopControlRegister(currentInstruction);
                if (loopControlReg != null) {
                    ArrayList<String> loopBody = extractLoopBody(instructions, i);
                    if (!loopBody.isEmpty()) {
                        for (int j = 0; j < unrollFactor; j++) {
                            for (String bodyInst : loopBody) {
                                String modifiedInst = modifyInstructionForUnrolling(bodyInst, j);
                                unrolled.add(modifiedInst);
                            }
                        }
                        // Modify branch condition to account for the unrolled iterations
                        String adjustedBranch = adjustLoopTermination(instructions.get(i), unrollFactor);
                        unrolled.add(adjustedBranch);
    
                        i += loopBody.size(); // Skip loop body since we unrolled it
                        continue;
                    }
                }
            }
            unrolled.add(currentInstruction);
        }
        return unrolled;
    }
    
    private String adjustLoopTermination(String branchInstruction, int unrollFactor) {
        String[] parts = branchInstruction.split("\\s+");
        if (parts[0].equals("BEQ") || parts[0].equals("BNE")) {
            int immediate = Integer.parseInt(parts[3]); 
            immediate -= unrollFactor; 
            parts[3] = String.valueOf(immediate);
        }
        return String.join(" ", parts);
    }
    

    private String extractLoopControlRegister(String instruction) {
        String[] parts = instruction.split("\\s+");
        if (parts[0].equals("SLT") || parts[0].equals("BEQ")) {
            return parts[2].replace("$", "");
        }
        return null;
    }

    private ArrayList<String> extractLoopBody(ArrayList<String> instructions, int startIndex) {
        ArrayList<String> loopBody = new ArrayList<>();
        
        for (int i = startIndex + 1; i < instructions.size(); i++) {
            String inst = instructions.get(i);
            
            if (inst.contains("BEQ") || inst.contains("BNE") || inst.contains("J ")) {
                break;
            }
            
            loopBody.add(inst);
        }
        
        return loopBody;
    }
    private String modifyInstructionForUnrolling(String instruction, int iteration) {
        StringBuilder sb = new StringBuilder();
        String[] parts = instruction.split("\\s+");
    
        for (String part : parts) {
            if (part.startsWith("$")) {
                // Extract only the numeric part of the register name
                String regNumStr = part.replaceAll("[^0-9]", ""); // Remove non-numeric characters
                if (!regNumStr.isEmpty()) {
                    int regNum = Integer.parseInt(regNumStr); // Only parse if it's numeric
                    sb.append("$").append(regNum + iteration * 4);
                }
            } else if (part.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                // If the part is a label, keep it unchanged
                sb.append(part);
            } else {
                // Keep other instruction parts unchanged
                sb.append(part);
            }
            sb.append(" ");
        }
    
        return sb.toString().trim();
    }
    
    

    private ArrayList<String> renameRegisters(ArrayList<String> instructions) {
        ArrayList<String> renamed = new ArrayList<>();
        Map<String, String> registerMapping = new HashMap<>();
        Set<String> liveRegisters = new HashSet<>();
    
        for (String instruction : instructions) {
            String[] parts = instruction.replace(",", "").split("\\s+");
            String[] renamedParts = Arrays.copyOf(parts, parts.length);
    
            String destReg = identifyDestinationRegister(instruction);
            Set<String> srcRegs = getSourceRegisters(instruction);
    
            // Rename only if needed (i.e., dependency exists)
            boolean renameNeeded = false;
            for (String srcReg : srcRegs) {
                if (liveRegisters.contains(srcReg)) {
                    renameNeeded = true;
                    break;
                }
            }
    
            if (renameNeeded && destReg != null && !isSpecialRegister(destReg)) {
                String newReg = findUnusedRegister(liveRegisters);
                registerMapping.put(destReg, newReg);
                liveRegisters.add(newReg);
            }
    
            // Apply renaming
            for (int i = 0; i < renamedParts.length; i++) {
                if (registerMapping.containsKey(renamedParts[i].replace("$", ""))) {
                    renamedParts[i] = "$" + registerMapping.get(renamedParts[i].replace("$", ""));
                }
            }
    
            renamed.add(String.join(" ", renamedParts));
        }
        return renamed;
    }
    

    private boolean isSpecialRegister(String reg) {
        return reg.equals("0") || reg.equals("1");
    }

    private String findUnusedRegister(Set<String> usedRegisters) {
        for (int i = 10; i < 32; i++) {
            String reg = String.valueOf(i);
            if (!usedRegisters.contains(reg)) {
                return reg;
            }
        }
        throw new RuntimeException("No free registers available");
    }

    private String identifyDestinationRegister(String instruction) {
        String[] parts = instruction.replace(",", "").split("\\s+");
        String op = parts[0].toLowerCase();

        switch (op) {
            case "add": case "sub": case "and": case "or":
            case "slt": case "sgt": case "nor": case "xor":
            case "sll": case "srl":
                return parts[1].substring(1);
            case "addi": case "ori": case "xori": case "andi":
            case "slti": case "lw":
                return parts[1].substring(1);
            default:
                return null;
        }
    }

    private boolean areIndependent(String inst1, String inst2) {
        Set<String> inst1SrcRegs = getSourceRegisters(inst1);
        Set<String> inst1DestRegs = getDestinationRegisters(inst1);
        Set<String> inst2SrcRegs = getSourceRegisters(inst2);
        Set<String> inst2DestRegs = getDestinationRegisters(inst2);

        boolean hasRAW = !Collections.disjoint(inst1DestRegs, inst2SrcRegs);
        boolean hasWAR = !Collections.disjoint(inst1SrcRegs, inst2DestRegs);
        boolean hasWAW = !Collections.disjoint(inst1DestRegs, inst2DestRegs);

        boolean isMemoryConflict = (inst1.contains("lw") && inst2.contains("sw")) ||
                                   (inst1.contains("sw") && inst2.contains("lw"));
        boolean isControlDependency = inst1.contains("beq") || inst1.contains("j") ||
                                      inst2.contains("beq") || inst2.contains("j");

        return !(hasRAW || hasWAR || hasWAW || isMemoryConflict || isControlDependency);
    }

    private ArrayList<String> expandPseudoInstruction(String instruction, boolean isPseudo) throws Exception {
        ArrayList<String> expandedInstructions = new ArrayList<>();
        String[] parts = instruction.replace(",", "").split("\\s+");
        String pseudoOp = parts[0].toLowerCase();
        String rs = parts[1].substring(1);
        String label = parts[2];
        String rd1 = "1";

        if (pseudoOp.equals("bltz")) {
            expandedInstructions.add(assemble("slt $" + rd1 + ", $" + rs + ", $0", false));
            expandedInstructions.add(assemble("bne $" + rd1 + ", $0" + ", " + label, true));
        } else if (pseudoOp.equals("bgez")) {
            expandedInstructions.add(assemble("slt $" + rd1 + ", $" + rs + ", $0", false));
            expandedInstructions.add(assemble("beq $" + rd1 + ", $0" + ", " + label, true));
        }

        currentAddress += expandedInstructions.size();
        return expandedInstructions;
    }

    private Set<String> getSourceRegisters(String instruction) {
        Set<String> srcRegs = new HashSet<>();
        String[] parts = instruction.replace(",", "").split("\\s+");
        String op = parts[0].toLowerCase();

        switch (op) {
            case "add": case "sub": case "and": case "or":
            case "slt": case "sgt": case "nor": case "xor":
            case "sll": case "srl":
                srcRegs.add(parts[2].substring(1)); // rs
                srcRegs.add(parts[3].substring(1)); // rt
                break;
            case "addi": case "ori": case "xori": case "andi":
            case "slti": case "lw": case "sw": case "beq": case "bne":
                srcRegs.add(parts[2].substring(1)); // rs
                break;
            case "jr":
                srcRegs.add(parts[1].substring(1)); // rs
                break;
            default:
                break;
        }

        return srcRegs;
    }

    private Set<String> getDestinationRegisters(String instruction) {
        Set<String> destRegs = new HashSet<>();
        String[] parts = instruction.replace(",", "").split("\\s+");
        String op = parts[0].toLowerCase();

        switch (op) {
            case "add": case "sub": case "and": case "or":
            case "slt": case "sgt": case "nor": case "xor":
            case "sll": case "srl":
                destRegs.add(parts[1].substring(1)); // rd
                break;
            case "addi": case "ori": case "xori": case "andi":
            case "slti": case "lw":
                destRegs.add(parts[1].substring(1)); // rt
                break;
            default:
                break;
        }

        return destRegs;
    }

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

    public String assemble(String instructionLine, boolean isPseudo) throws Exception {
        String[] parts = instructionLine.replace(",", "").split("\\s+");
        String instruction = parts[0].toLowerCase();

        if (!opcodes.containsKey(instruction)) {
            throw new Exception("Instruction " + instruction + " is not supported.");
        }

        switch (instruction) {
            case "add": case "sub": case "and": case "or":
            case "slt": case "sgt": case "nor": case "xor":
                if (parts.length != 4) {
                    throw new Exception(
                            "Invalid format for " + instruction + ". Expected: " + instruction + " $rd, $rs, $rt");
                }
                String rd = parts[1].substring(1);
                String rs = parts[2].substring(1);
                String rt = parts[3].substring(1);
                return assembleRType(instruction, rs, rt, rd);

            case "sll": case "srl":
                if (parts.length != 4) {
                    throw new Exception(
                            "Invalid format for " + instruction + ". Expected: " + instruction + " $rd, $rt, shamt");
                }
                String rdShift = parts[1].substring(1);
                String rtShift = parts[2].substring(1);
                int shamt = parseImmediate(parts[3]);
                return assembleRTypeShamt(instruction, rtShift, rdShift, shamt);

            case "addi": case "ori": case "xori": case "andi": case "slti":
                if (parts.length != 4) {
                    throw new Exception("Invalid format for " + instruction + ". Expected: " + instruction
                            + " $rt, $rs, immediate");
                }
                String rt2 = parts[1].substring(1);
                String rs2 = parts[2].substring(1);
                int immediate = parseImmediate(parts[3]);
                return assembleIType(instruction, rs2, rt2, immediate);

            case "beq": case "bne":
                if (parts.length != 4) {
                    throw new Exception(
                            "Invalid format for " + instruction + ". Expected: " + instruction + " $rs, $rt, label");
                }
                String rs3 = parts[1].substring(1);
                String rt3 = parts[2].substring(1);
                int offset = calculateBranchOffset(parts[3], isPseudo);
                return assembleIType(instruction, rs3, rt3, offset);

            case "lw": case "sw":
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

            case "jal": case "j":
                if (parts.length != 2) {
                    throw new Exception("Invalid format for " + instruction + ". Expected: " + instruction + " label");
                }
                if (!labelAddresses.containsKey(parts[1])) {
                    throw new Exception("Undefined label: " + parts[1]);
                }
                int address = labelAddresses.get(parts[1]);
                return assembleJType(instruction, address);

            case "jr":
                if (parts.length != 2) {
                    throw new Exception("Invalid format for jr. Expected: jr $rs");
                }
                String rsJr = parts[1].substring(1);
                return assembleRType(instruction, rsJr, "0", "0");
            
            case "nop":
                return assembleRTypeShamt(instruction, "0", "0", 0);

            default:
                throw new Exception("Instruction " + instruction + " is not supported.");
        }
    }

    private int calculateBranchOffset(String label, boolean isPseudo) throws Exception {
        if (!labelAddresses.containsKey(label)) {
            throw new Exception("Undefined label: " + label);
        }
        // Calculate relative offset in words
        // Subtract (currentAddress + 1) to account for the branch delay slot
        return (labelAddresses.get(label) - (currentAddress + (isPseudo ? 2 : 1)));
    }

    public int getLabelAddress(String label) {
        return labelAddresses.getOrDefault(label, -1);
    }

    public static void main(String[] args) {
        try {
            Assembler assembler = new Assembler();
            String[] program = {
                    "main:",
                    "ORI $2, $0, 0x1",
                    "ORI $3, $0, 0x0",
                    "ORI $4, $0, 0xA",
                    "SUM_LOOP:",
                    "ADD $3, $3, $2",
                    "ADDI $2, $2, 1",
                    "SLT $5, $4, $2",
                    "BEQ $5, $0, SUM_LOOP",
                    "SW $3, 0x0($0)",
                    "END:",
                    "NOP"
            };

            assembler.firstPass(program);

            ArrayList<String> machineCode = assembler.secondPass();

            System.out.println("Label Addresses:");
            for (String label : assembler.labelAddresses.keySet()) {
                int address = assembler.getLabelAddress(label);
                System.out.printf("Label %s : Address %d\n", label, address);
            }
            System.out.println("*****************************************************");
            System.out.println("WIDTH=32;");
            System.out.println("DEPTH=256;");
            System.out.println("ADDRESS_RADIX=UNS;");
            System.out.println("DATA_RADIX=BIN;");
            System.out.println("CONTENT BEGIN");

            int addr = 0;
            for (int i = 0; i < machineCode.size(); i++) {
                String code = machineCode.get(i);
                String instruction = assembler.rescheduledInstructions.get(i);
                System.out.printf("    %d   : %s;  -- %s\n", addr++, code, instruction);
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
}