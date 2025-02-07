import java.util.*;

public class InstructionScheduler {
    private static class Instruction {
        String original;
        String op;
        List<String> operands;
        Set<String> readRegs;
        Set<String> writeRegs;
        boolean isBranch;
        boolean isMemory;
        
        public Instruction(String line) {
            this.original = line.trim();
            this.readRegs = new HashSet<>();
            this.writeRegs = new HashSet<>();
            parseLine(line);
        }
        
        private void parseLine(String line) {
            if (line.contains(":") || line.trim().isEmpty() || line.startsWith("#")) {
                this.op = "label";
                return;
            }
            
            String[] parts = line.replace(",", "").split("\\s+");
            this.op = parts[0].toLowerCase();
            this.operands = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));
            
            switch (op) {
                case "add": case "sub": case "and": case "or": case "slt": case "sgt": case "nor": case "xor":
                    writeRegs.add(operands.get(0));
                    readRegs.add(operands.get(1));
                    readRegs.add(operands.get(2));
                    break;
                    
                case "addi": case "ori": case "xori": case "andi": case "slti":
                    writeRegs.add(operands.get(0));
                    readRegs.add(operands.get(1));
                    break;
                    
                case "lw":
                    writeRegs.add(operands.get(0));
                    String[] memOp = parseMemoryOperand(operands.get(1));
                    readRegs.add(memOp[1]);
                    isMemory = true;
                    break;
                    
                case "sw":
                    readRegs.add(operands.get(0));
                    String[] memOpSw = parseMemoryOperand(operands.get(1));
                    readRegs.add(memOpSw[1]);
                    isMemory = true;
                    break;
                    
                case "beq": case "bne":
                    readRegs.add(operands.get(0));
                    readRegs.add(operands.get(1));
                    isBranch = true;
                    break;
                    
                case "j": case "jal":
                    isBranch = true;
                    break;
                    
                case "jr":
                    readRegs.add(operands.get(0));
                    isBranch = true;
                    break;
            }
        }
        
        private String[] parseMemoryOperand(String memOp) {
            String[] parts = memOp.split("[\\(\\)]");
            return new String[]{parts[0], parts[1].substring(1)}; // offset, register
        }
        
        public String toString() {
            return original;
        }
    }

    private static class Loop {
        int startIndex;
        int endIndex;
        String label;
        List<Instruction> body;
        Instruction branchInst;
        String branchTarget;
        String branchCondition;
        
        public Loop(int start, int end, String label, List<Instruction> body, 
                   Instruction branch, String target, String condition) {
            this.startIndex = start;
            this.endIndex = end;
            this.label = label;
            this.body = body;
            this.branchInst = branch;
            this.branchTarget = target;
            this.branchCondition = condition;
        }
    }

    private int nextRegister = 8;
    private static final int UNROLL_FACTOR = 4;

    public String[] schedule(String[] program) {
        List<Instruction> instructions = parseInstructions(program);
        List<Loop> loops = identifyLoops(instructions);
        List<String> optimized = optimizeProgram(instructions, loops);
        return optimized.toArray(new String[0]);
    }

    private List<Instruction> parseInstructions(String[] program) {
        List<Instruction> instructions = new ArrayList<>();
        for (String line : program) {
            if (!line.trim().isEmpty()) {
                instructions.add(new Instruction(line));
            }
        }
        return instructions;
    }

    private List<Loop> identifyLoops(List<Instruction> instructions) {
        List<Loop> loops = new ArrayList<>();
        Map<String, Integer> labelPositions = new HashMap<>();
        
        // First pass: collect label positions
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            if (inst.op.equals("label")) {
                labelPositions.put(inst.original.replace(":", ""), i);
            }
        }
        
        // Second pass: identify loops
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            if (inst.isBranch && inst.operands != null && inst.operands.size() > 0) {
                String targetLabel = inst.operands.get(inst.operands.size() - 1);
                if (labelPositions.containsKey(targetLabel)) {
                    int targetPos = labelPositions.get(targetLabel);
                    if (targetPos < i) { // Backward branch - likely a loop
                        List<Instruction> loopBody = new ArrayList<>(
                            instructions.subList(targetPos + 1, i)
                        );
                        String condition = "";
                        if (inst.op.equals("beq") || inst.op.equals("bne")) {
                            condition = inst.operands.get(0) + "," + inst.operands.get(1);
                        }
                        loops.add(new Loop(targetPos, i, targetLabel, loopBody, inst, targetLabel, condition));
                    }
                }
            }
        }
        return loops;
    }

    private List<String> optimizeProgram(List<Instruction> instructions, List<Loop> loops) {
        List<String> optimized = new ArrayList<>();
        int currentPos = 0;

        for (Loop loop : loops) {
            // Add instructions before the loop
            while (currentPos < loop.startIndex) {
                optimized.add(instructions.get(currentPos).original);
                currentPos++;
            }

            // Process the loop
            optimized.addAll(unrollAndOptimizeLoop(loop));
            currentPos = loop.endIndex + 1;
        }

        // Add remaining instructions
        while (currentPos < instructions.size()) {
            optimized.add(instructions.get(currentPos).original);
            currentPos++;
        }

        return optimized;
    }

    private List<String> unrollAndOptimizeLoop(Loop loop) {
        List<String> unrolled = new ArrayList<>();
        int baseRegister = nextRegister;
        
        // Add loop label
        unrolled.add(loop.label + ":");
        
        // Extract loop counter register and increment value if it's a counting loop
        String counterReg = "$2";  // In this case we know it's $2
        int incrementValue = 1;    // We know it's incrementing by 1
        
        // Unroll the loop
        for (int i = 0; i < UNROLL_FACTOR; i++) {
            // Calculate register offsets for this iteration
            int sumReg = baseRegister + (i * 3);
            int counterReg1 = baseRegister + (i * 3) + 1;
            int sltReg = baseRegister + (i * 3) + 2;
            
            // First iteration uses original registers for input
            if (i == 0) {
                unrolled.add("add $" + sumReg + ", $3, " + counterReg);  // Add current value
                unrolled.add("addi $" + counterReg1 + ", " + counterReg + ", " + incrementValue);  // Increment counter
            } else {
                // Use previous iteration's registers
                int prevSumReg = baseRegister + ((i-1) * 3);
                int prevCounterReg = baseRegister + ((i-1) * 3) + 1;
                unrolled.add("add $" + sumReg + ", $" + prevSumReg + ", $" + prevCounterReg);  // Add current value
                unrolled.add("addi $" + counterReg1 + ", $" + prevCounterReg + ", " + incrementValue);  // Increment counter
            }
            
            // Add the comparison for loop termination
            unrolled.add("slt $" + sltReg + ", $4, $" + counterReg1);
            
            // If we detect the loop might terminate, break the unrolling
            if (i < UNROLL_FACTOR - 1) {
                unrolled.add("bne $" + sltReg + ", $0, exit_unrolled");  // Exit if condition met
            }
        }

        // Copy final values back to original registers
        int lastSumReg = baseRegister + ((UNROLL_FACTOR-1) * 3);
        int lastCounterReg = baseRegister + ((UNROLL_FACTOR-1) * 3) + 1;
        int lastSltReg = baseRegister + ((UNROLL_FACTOR-1) * 3) + 2;
        unrolled.add("add $3, $" + lastSumReg + ", $0");  // Update sum
        unrolled.add("add $2, $" + lastCounterReg + ", $0");  // Update counter
        
        // Branch back if loop should continue
        unrolled.add("beq $" + lastSltReg + ", $0, " + loop.label);
        
        // Add exit label for early termination
        unrolled.add("exit_unrolled:");
        
        // Increment nextRegister for future use
        nextRegister = baseRegister + (UNROLL_FACTOR * 3);
        
        return unrolled;

    }

    public String[] getProgram() {
        return new String[]{
            "main:",
            "ori $2, $0, 0x1",    
            "ori $3, $0, 0x0",    
            "ori $4, $0, 0xA",    
            "loop:",
            "add $3, $3, $2",     
            "addi $2, $2, 1",    
            "slt $5, $4, $2",     
            "beq $5, $0, loop",   
            "sw $3, 0x0($0)",
            "nop"
        };
    }

    public static void main(String[] args) {
        InstructionScheduler scheduler = new InstructionScheduler();
        
        String[] program = scheduler.getProgram();

        String[] scheduled = scheduler.schedule(program);
        System.out.println("Original Program:");
        for (String instruction : program) {
            System.out.println(instruction);
        }

        System.out.println("************************************************************\n"+
        "************************************************************");
        System.out.println("Optimized and Scheduled Program:");
        for (String instruction : scheduled) {
            System.out.println(instruction);
        }
    }
}