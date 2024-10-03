import java.util.HashMap;

public class Assembler {

    private HashMap<String, String> opcodes;
    private HashMap<String, String> functCodes;

    // Constructor to initialize the opcode and funct maps
    public Assembler() {
        init();
    }

    public void init() {
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

        functCodes = new HashMap<>();
        functCodes.put("add", "100000");
        functCodes.put("sub", "100010");
        functCodes.put("and", "100100");
        functCodes.put("or", "100101");
        functCodes.put("slt", "101010");
    }

    // Convert numerical register to binary directly
    public String regToBinary(String reg) {
        int regNumber = Integer.parseInt(reg); // Convert the register number string to an integer
        return toBinary(regNumber, 5); // Convert the integer to a 5-bit binary string
    }

    public String toBinary(int value, int bits) {
        // Use Integer.toBinaryString to handle both positive and negative values
        String binaryString = Integer.toBinaryString(value);

        // If the binary string is longer than the specified bits, take the least
        // significant bits
        if (binaryString.length() > bits) {
            return binaryString.substring(binaryString.length() - bits);
        }

        // If it's shorter, pad with zeros
        return String.format("%" + bits + "s", binaryString).replace(' ', '0');
    }

    public String assembleRType(String instruction, String rs, String rt, String rd) {
        String opcode = opcodes.get(instruction);
        String funct = functCodes.get(instruction);
        return opcode + regToBinary(rs) + regToBinary(rt) + regToBinary(rd) + "00000" + funct;
    }

    public String assembleIType(String instruction, String rs, String rt, int immediate) {
        String opcode = opcodes.get(instruction);
        return opcode + regToBinary(rs) + regToBinary(rt) + toBinary(immediate, 16);
    }

    private int parseImmediate(String value) {
        value = value.toLowerCase().trim();
        if (value.startsWith("0x")) {
            return Integer.parseInt(value.substring(2), 16);
        } else {
            return Integer.parseInt(value);
        }
    }

    public String assemble(String instructionLine) throws Exception {
        // Parse the instruction line, handling commas
        String[] parts = instructionLine.replace(",", "").split("\\s+");
        String instruction = parts[0];

        switch (instruction) {
            case "add":
            case "sub":
            case "and":
            case "or":
            case "slt":
                String rd = parts[1].substring(1); // Remove $ from the register
                String rs = parts[2].substring(1); // Remove $ from the register
                String rt = parts[3].substring(1); // Remove $ from the register
                return assembleRType(instruction, rs, rt, rd);

            case "addi":
            case "beq":
                String rt2 = parts[1].substring(1); // Remove $ from the register
                String rs2 = parts[2].substring(1); // Remove $ from the register
                int immediate = parseImmediate(parts[3]);
                return assembleIType(instruction, rs2, rt2, immediate);

            case "lw":
            case "sw":
                String rt3 = parts[1].substring(1); // Remove $ from the register
                String[] offsetAndReg = parts[2].split("\\(");
                int offset = parseImmediate(offsetAndReg[0]);
                String rs3 = offsetAndReg[1].substring(1, offsetAndReg[1].length() - 1); // Remove $ and parentheses
                return assembleIType(instruction, rs3, rt3, offset);

            default:
                throw new Exception("Instruction " + instruction + " is not supported.");
        }
    }

    public static void main(String[] args) {
        try {
            Assembler assembler = new Assembler();
            // Register numbers are used instead of names like $t0, $t1, etc.
            String[] machineCodes = {
                    "addi $5, $0, 0xff",
                    "addi $6, $0, 255",
                    "addi $7, $0, 0x55",
                    "addi $8, $0, 85",
                    "sub $9, $5, $6",
                    "sw $9, 0x0($0)",
                    "lw $10, 0($20)",
                    "beq $6, $7, 4",
                    "or $11, $6, $7",
                    "and $12, $6, $7",
                    "add $0, $6, $7",
                    "slt $13, $0, $5"
            };
            for (String instruction : machineCodes) {
                String machineCode = assembler.assemble(instruction);
                System.out.println(machineCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}