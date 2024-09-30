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

    public String regToBinary(String reg) {
        HashMap<String, String> binaryRegs = new HashMap<>();
        binaryRegs.put("$zero", "00000");
        binaryRegs.put("$at", "00001");
        binaryRegs.put("$v0", "00010");
        binaryRegs.put("$v1", "00011");
        binaryRegs.put("$a0", "00100");
        binaryRegs.put("$a1", "00101");
        binaryRegs.put("$a2", "00110");
        binaryRegs.put("$a3", "00111");
        binaryRegs.put("$t0", "01000");
        binaryRegs.put("$t1", "01001");
        binaryRegs.put("$t2", "01010");
        binaryRegs.put("$t3", "01011");
        binaryRegs.put("$t4", "01100");
        binaryRegs.put("$t5", "01101");
        binaryRegs.put("$t6", "01110");
        binaryRegs.put("$t7", "01111");
        binaryRegs.put("$s0", "10000");
        binaryRegs.put("$s1", "10001");
        binaryRegs.put("$s2", "10010");
        binaryRegs.put("$s3", "10011");
        binaryRegs.put("$s4", "10100");
        binaryRegs.put("$s5", "10101");
        binaryRegs.put("$s6", "10110");
        binaryRegs.put("$s7", "10111");
        binaryRegs.put("$t8", "01100");
        binaryRegs.put("$t9", "01101");
        binaryRegs.put("$k0", "01110");
        binaryRegs.put("$k1", "01111");
        binaryRegs.put("$gp", "11100");
        binaryRegs.put("$sp", "11101");
        binaryRegs.put("$fp", "11110");
        binaryRegs.put("$ra", "11111");

        return binaryRegs.get(reg);
    }

    public String toBinary(int value, int bits) {
        // Convert value to binary with a fixed bit size
        if (value >= 0) {
            return String.format("%" + bits + "s", Integer.toBinaryString(value)).replace(' ', '0');
        } else {
            // Handle negative values using two's complement
            return Integer.toBinaryString((1 << bits) + value).substring(32 - bits);
        }
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

    public String assemble(String instructionLine) throws Exception {
        // Parse the instruction line, handling commas
        String[] parts = instructionLine.replace(",", "").split(" ");
        String instruction = parts[0];

        switch (instruction) {
            case "add":
            case "sub":
            case "and":
            case "or":
            case "slt":
                String rd = parts[1];
                String rs = parts[2];
                String rt = parts[3];
                return assembleRType(instruction, rs, rt, rd);

            case "addi":
                rt = parts[1];
                rs = parts[2];
                int immediate = Integer.parseInt(parts[3]);
                return assembleIType(instruction, rs, rt, immediate);

            case "lw":
            case "sw":
                rt = parts[1];
                String offsetRs = parts[2];
                String[] offsetAndRs = offsetRs.split("\\(");
                int offset = Integer.parseInt(offsetAndRs[0]);
                rs = offsetAndRs[1].replace(")", "");
                return assembleIType(instruction, rs, rt, offset);

            case "beq":
                rs = parts[1];
                rt = parts[2];
                int offsetImmediate = Integer.parseInt(parts[3]);
                return assembleIType(instruction, rs, rt, offsetImmediate);

            default:
                throw new Exception("Instruction " + instruction + " is not supported.");
        }
    }

    public static void main(String[] args) {
        try {
            Assembler assembler = new Assembler();
            String [] machineCodes = {"add $t0, $t1, $t2", "addi $t0, $t1, 100","lw $t0, 100($t1)","sw $t0, 100($t1)","beq $t0, $t1, 25","add $t0, $t1, $t2"};
            String machineCode = "";
            for (int i=0; i<machineCodes.length;i++){
                machineCode=assembler.assemble(machineCodes[i]);
                System.out.println(machineCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
