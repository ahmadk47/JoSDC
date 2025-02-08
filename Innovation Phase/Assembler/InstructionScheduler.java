import java.util.*;

public class InstructionScheduler {
    private static final int UNROLL_FACTOR = 3;
    private int nextRegister = 8;
    private static class Instruction {
        String original;
        String op;
        List<String> operands;
        Set<String> readRegs;
        Set<String> writeRegs;
        boolean isBranch;
        boolean isMemory;
        boolean isArithmetic;
        int originalIndex;
        
        public Instruction(String line, int index) {
            this.original = line.trim();
            this.readRegs = new HashSet<>();
            this.writeRegs = new HashSet<>();
            this.originalIndex = index;
            parseLine(line);
        }
        
        private void parseLine(String line) {
            if (line.trim().isEmpty() || line.startsWith("#")) {
                this.op = "comment";
                return;
            }
            if (line.contains(":")) {
                this.op = "label";
                return;
            }
            
            String[] parts = line.replace(",", "").split("\\s+");
            this.op = parts[0].toLowerCase();
            this.operands = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));
            
            analyzeInstruction();
        }
        
        private void analyzeInstruction() {
            switch (op) {
                // Arithmetic operations
                case "add": case "sub":
                case "and": case "or": case "xor": case "nor":
                case "slt": case "sll": case "srl":
                    isArithmetic = true;
                    if (operands.size() >= 3) {
                        writeRegs.add(operands.get(0));
                        readRegs.add(operands.get(1));
                        readRegs.add(operands.get(2));
                    }
                    break;
                
                // Immediate arithmetic
                case "addi": case "subi":
                case "andi": case "ori": case "xori":
                case "slti":
                    isArithmetic = true;
                    if (operands.size() >= 2) {
                        writeRegs.add(operands.get(0));
                        readRegs.add(operands.get(1));
                    }
                    break;
                
                // Memory operations
                case "lw":case "sw":
                    isMemory = true;
                    if (operands.size() >= 2) {
                        if (op.startsWith("l")) {  // Load
                            writeRegs.add(operands.get(0));
                        } else {  // Store
                            readRegs.add(operands.get(0));
                        }
                        String[] memOp = parseMemoryOperand(operands.get(1));
                        readRegs.add(memOp[1]);  // Base register
                    }
                    break;
                
                // Branch/Jump operations
                case "beq": case "bne": case "bltz": case "bgez": case "j": case "jal":
                case "jr": 
                    isBranch = true;
                    if (operands.size() >= 2 && !op.startsWith("j")) {
                        readRegs.add(operands.get(0));
                        if (!operands.get(1).matches("^-?\\d+$")) {
                            readRegs.add(operands.get(1));
                        }
                    }
                    break;
            }
        }
        
        private String[] parseMemoryOperand(String memOp) {
            String[] parts = memOp.split("[\\(\\)]");
            String offset = parts[0].isEmpty() ? "0" : parts[0];
            String reg = parts[1];
            if (reg.startsWith("$")) {
                reg = reg.substring(1);
            }
            return new String[]{offset, reg};
        }
    }

    // Represents a scheduled packet of up to 2 instructions
    private static class InstructionPacket {
        List<Instruction> instructions;
        
        public InstructionPacket() {
            this.instructions = new ArrayList<>(2);  // Max size 2
        }
        
        public boolean canAdd(Instruction inst) {
            if (instructions.size() >= 2) return false;
            
            // Check structural hazards
            if (instructions.isEmpty()) return true;
            
            Instruction existing = instructions.get(0);
            
            // Can't have two branch instructions
            if (inst.isBranch && existing.isBranch) return false;
            
            // Can't have two memory instructions
            if (inst.isMemory && existing.isMemory) return false;
            
            return true;
        }
        
        public boolean addInstruction(Instruction inst) {
            if (canAdd(inst)) {
                instructions.add(inst);
                return true;
            }
            return false;
        }
    }

    // Track register dependencies
    private static class DependencyTracker {
        private Map<String, Integer> lastWrite;
        private Map<String, Integer> lastRead;
        
        public DependencyTracker() {
            this.lastWrite = new HashMap<>();
            this.lastRead = new HashMap<>();
        }
        
        public boolean hasDependency(Instruction inst, int currentIndex) {
            // Check RAW dependencies
            for (String reg : inst.readRegs) {
                if (lastWrite.containsKey(reg)) {
                    int lastWriteIndex = lastWrite.get(reg);
                    if (lastWriteIndex >= currentIndex - 1) return true;
                }
            }
            
            // Check WAW dependencies
            for (String reg : inst.writeRegs) {
                if (lastWrite.containsKey(reg)) {
                    int lastWriteIndex = lastWrite.get(reg);
                    if (lastWriteIndex >= currentIndex - 1) return true;
                }
            }
            
            // Check WAR dependencies
            for (String reg : inst.writeRegs) {
                if (lastRead.containsKey(reg)) {
                    int lastReadIndex = lastRead.get(reg);
                    if (lastReadIndex >= currentIndex - 1) return true;
                }
            }
            
            return false;
        }
        
        public void updateDependencies(Instruction inst, int index) {
            for (String reg : inst.readRegs) {
                lastRead.put(reg, index);
            }
            for (String reg : inst.writeRegs) {
                lastWrite.put(reg, index);
            }
        }
    }
    public String[] schedule(String[] program) {
        // Parse instructions
        List<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < program.length; i++) {
            if (!program[i].trim().isEmpty()) {
                instructions.add(new Instruction(program[i], i));
            }
        }

        // Identify and unroll loops
        List<Loop> loops = identifyLoops(instructions);
        List<String> optimized = new ArrayList<>();
        int currentPos = 0;

        // Process instructions with loop unrolling
        for (Loop loop : loops) {
            // Add instructions before the loop
            while (currentPos < loop.startIndex) {
                optimized.add(instructions.get(currentPos).original);
                currentPos++;
            }

            if (loop.isUnrollable()) {
                // Unroll and add the loop
                optimized.addAll(unrollLoop(loop, instructions));
            } else {
                // Add original loop if not unrollable
                for (int i = loop.startIndex; i <= loop.endIndex; i++) {
                    optimized.add(instructions.get(i).original);
                }
            }
            currentPos = loop.endIndex + 1;
        }

        // Add remaining instructions
        while (currentPos < instructions.size()) {
            optimized.add(instructions.get(currentPos).original);
            currentPos++;
        }

        // Now perform instruction scheduling on the optimized code
        return scheduleInstructions(optimized.toArray(new String[0]));
    }

    public String[] scheduleInstructions(String[] program) {
        // Parse instructions
        List<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < program.length; i++) {
            if (!program[i].trim().isEmpty()) {
                instructions.add(new Instruction(program[i], i));
            }
        }
        
        List<String> scheduledProgram = new ArrayList<>();
        DependencyTracker depTracker = new DependencyTracker();
        
        int i = 0;
        while (i < instructions.size()) {
            Instruction inst1 = instructions.get(i);
            
            // Handle labels - must be on their own line
            if (inst1.op.equals("label")) {
                scheduledProgram.add(inst1.original);
                i++;
                continue;
            }
            
            // Skip comments
            if (inst1.op.equals("comment")) {
                i++;
                continue;
            }
            
            // Try to find a second instruction that can be paired
            Instruction inst2 = null;
            int j = i + 1;
            while (j < instructions.size()) {
                Instruction candidate = instructions.get(j);
                
                // Skip labels and comments when looking for second instruction
                if (candidate.op.equals("label") || candidate.op.equals("comment")) {
                    break;
                }
                
                // Check if instructions can be paired
                if (canScheduleTogether(inst1, candidate) && 
                    !depTracker.hasDependency(candidate, j)) {
                    inst2 = candidate;
                    instructions.remove(j);  // Remove it so we don't schedule it again
                    break;
                }
                j++;
            }
            
            // Update dependency tracking
            depTracker.updateDependencies(inst1, i);
            if (inst2 != null) {
                depTracker.updateDependencies(inst2, i+1);
                
                // Add both instructions in desired order
                if (shouldSwapOrder(inst1, inst2)) {
                    scheduledProgram.add(inst2.original);
                    scheduledProgram.add(inst1.original);
                } else {
                    scheduledProgram.add(inst1.original);
                    scheduledProgram.add(inst2.original);
                }
            } else {
                // No paired instruction found, add single instruction
                scheduledProgram.add(inst1.original);
            }
            
            i++;
        }
        
        return scheduledProgram.toArray(new String[0]);
    }
    private boolean canScheduleTogether(Instruction inst1, Instruction inst2) {
        // Can't pair if either is a label
        if (inst1.op.equals("label") || inst2.op.equals("label")) {
            return false;
        }
        
        // Can't pair two branch instructions
        if (inst1.isBranch && inst2.isBranch) {
            return false;
        }
        
        // Can't pair two memory instructions
        if (inst1.isMemory && inst2.isMemory) {
            return false;
        }
        
        // Check for dependencies between the instructions
        if (hasDirectDependency(inst1, inst2)) {
            return false;
        }
        
        return true;
    }
    
    private boolean hasDirectDependency(Instruction inst1, Instruction inst2) {
        // Check RAW
        for (String reg : inst2.readRegs) {
            if (inst1.writeRegs.contains(reg)) {
                return true;
            }
        }
        
        // Check WAW
        for (String reg : inst2.writeRegs) {
            if (inst1.writeRegs.contains(reg)) {
                return true;
            }
        }
        
        // Check WAR
        for (String reg : inst2.writeRegs) {
            if (inst1.readRegs.contains(reg)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean shouldSwapOrder(Instruction inst1, Instruction inst2) {
        // Keep branches at the end of the pair
        if (inst2.isBranch) {
            return false;
        }
        if (inst1.isBranch) {
            return true;
        }
        
        // Prefer memory operations first
        if (inst1.isMemory && !inst2.isMemory) {
            return false;
        }
        if (!inst1.isMemory && inst2.isMemory) {
            return true;
        }
        
        // Default to original order
        return false;
    }

    // Helper method to convert scheduled packets to string array
    public String[] getScheduledProgram(List<InstructionPacket> packets) {
        List<String> scheduled = new ArrayList<>();
        for (InstructionPacket packet : packets) {
            if (packet.instructions.size() == 2) {
                scheduled.add(packet.instructions.get(0).original + " || " + 
                            packet.instructions.get(1).original);
            } else {
                scheduled.add(packet.instructions.get(0).original);
            }
        }
        return scheduled.toArray(new String[0]);
    }
   

    private static class Loop {
        int startIndex;
        int endIndex;
        String label;
        List<Instruction> body;
        Instruction branchInst;
        String counterReg;
        String boundReg;
        int step;
        Set<String> modifiedRegs;

        public Loop(int start, int end, String label, List<Instruction> body, Instruction branch) {
            this.startIndex = start;
            this.endIndex = end;
            this.label = label;
            this.body = body;
            this.branchInst = branch;
            this.modifiedRegs = new HashSet<>();
            analyzeLoop();
        }

        private void analyzeLoop() {
            // Find counter register and step size
            for (Instruction inst : body) {
                if (inst.op.equals("addi") && inst.operands.get(0).equals(inst.operands.get(1))) {
                    counterReg = inst.operands.get(0);
                    try {
                        step = Integer.parseInt(inst.operands.get(2));
                    } catch (NumberFormatException e) {
                        step = 1;
                    }
                }
                // Collect modified registers
                modifiedRegs.addAll(inst.writeRegs);
            }

            // Find bound register from branch instruction
            if (branchInst.op.equals("beq") || branchInst.op.equals("bne")) {
                boundReg = branchInst.operands.get(1);
            }
            else if(branchInst.op.equals("bgez") || branchInst.op.equals("bltz")){
                boundReg = branchInst.operands.get(0);
            }
        }

        public boolean isUnrollable() {
            return counterReg != null && boundReg != null && body.size() <= 5;
        }
    }

    private static class RegisterRenamer {
        private Map<String, String> registerMap;
        private int nextRegister;

        public RegisterRenamer(int startRegister) {
            this.registerMap = new HashMap<>();
            this.nextRegister = startRegister;
        }

        public String getRenamedRegister(String original) {
            if (!original.startsWith("$")) {
                return original;  // Not a register
            }
            return registerMap.computeIfAbsent(original, k -> "$" + nextRegister++);
        }

        public void clear() {
            registerMap.clear();
        }
    }

    private List<String> unrollLoop(Loop loop, List<Instruction> instructions) {
        List<String> unrolled = new ArrayList<>();
        RegisterRenamer renamer = new RegisterRenamer(nextRegister);

        // Add original loop label
        unrolled.add(loop.label + ":");

        // Generate unrolled iterations
        for (int i = 0; i < UNROLL_FACTOR; i++) {
            // Create new register mappings for this iteration
            RegisterRenamer iterationRenamer = new RegisterRenamer(nextRegister + (i * loop.modifiedRegs.size()));

            // Process each instruction in loop body
            for (Instruction inst : loop.body) {
                if (!inst.op.equals("beq")&&!inst.op.equals("bgez")&&!inst.op.equals("bltz") && !inst.op.equals("bne") && !inst.isBranch) {
                    String newInst = rewriteInstruction(inst, iterationRenamer);
                    unrolled.add(newInst);
                }
            }

            // Add early exit check except for last iteration
            if (i < UNROLL_FACTOR - 1) {
                String exitCheckSlt = generateExitCheckSlt(loop, i, iterationRenamer);
                unrolled.add(exitCheckSlt);
                String exitCheckBne = generateExitCheckBne(loop, i, iterationRenamer);
                unrolled.add(exitCheckBne);

            }
        }

        // Update original registers with final values
        for (String reg : loop.modifiedRegs) {
            String lastRenamedReg = renamer.getRenamedRegister(reg);
            unrolled.add("add " + reg + ", " + lastRenamedReg + ", $0");
        }

        // Add loop continuation check
        unrolled.add(rewriteBranchInstruction(loop.branchInst, loop.label));
        unrolled.add("exit_" + loop.label + ":");

        // Update nextRegister for future use
        nextRegister += UNROLL_FACTOR * loop.modifiedRegs.size();

        return unrolled;
    }

    private String rewriteInstruction(Instruction inst, RegisterRenamer renamer) {
        String result = inst.op;
        
        for (String operand : inst.operands) {
            if (operand.startsWith("$")) {
                result += " " + renamer.getRenamedRegister(operand) + ",";
            } else {
                result += " " + operand + ",";
            }
        }
        
        // Remove trailing comma
        return result.substring(0, result.length() - 1);
    }

    private String generateExitCheckSlt(Loop loop, int iteration, RegisterRenamer renamer) {
        String conditionReg = renamer.getRenamedRegister("$condition");
        String counterReg = renamer.getRenamedRegister(loop.counterReg);
        String boundReg = loop.boundReg;
        String exitCheck = "slt " + conditionReg + ", " + boundReg + ", " + counterReg;
        return exitCheck;
    }
    private String generateExitCheckBne(Loop loop, int iteration, RegisterRenamer renamer) {
        String conditionReg = renamer.getRenamedRegister("$condition");
        String exitCheck = "bne " + conditionReg + ", $0, exit_" + loop.label;
        return exitCheck;
    }

    private String rewriteBranchInstruction(Instruction branch, String label) {
        return branch.original.replace(branch.operands.get(branch.operands.size() - 1), label);
    }

    private List<Loop> identifyLoops(List<Instruction> instructions) {
        List<Loop> loops = new ArrayList<>();
        Map<String, Integer> labelPositions = new HashMap<>();
        
        // First pass: collect label positions
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            if (inst.op.equals("label")) {
                labelPositions.put(inst.original.replace(":", "").trim(), i);
            }
        }
        
        // Second pass: identify loops
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            if (inst.isBranch && inst.operands != null && inst.operands.size() > 0) {
                String targetLabel = inst.operands.get(inst.operands.size() - 1);
                if (labelPositions.containsKey(targetLabel)) {
                    int targetPos = labelPositions.get(targetLabel);
                    if (targetPos < i) {  // Backward branch - likely a loop
                        List<Instruction> loopBody = new ArrayList<>(
                            instructions.subList(targetPos + 1, i)
                        );
                        loops.add(new Loop(targetPos, i, targetLabel, loopBody, inst));
                    }
                }
            }
        }
        return loops;
    }

    public String[] getProgram() {
        return new String[]{
            "xori $24, $0, 0x5",
            "xori $25, $0, 0x3",
            "jal mul_fun",
            "j finish",
            "mul_fun:",
            "andi $23, $0, 0",
            "addi $22, $25, -1",
            "loop:",
            "add  $23, $23, $24",
            "addi $22, $22, -1",
            "bgez $22, loop",
            "jr $31",
            "finish:",
            "nop"
        };
    }

    public static void main(String[] args) {
        InstructionScheduler scheduler = new InstructionScheduler();
        
        // Get sample program
        String[] program = scheduler.getProgram();
        
        // Schedule the program
        String[] scheduled = scheduler.schedule(program);
        
        System.out.println("Original Program:");
        for (String inst : program) {
            System.out.println(inst);
        }
        
        System.out.println("\nScheduled Program:");
        for (String inst : scheduled) {
            System.out.println(inst);
        }
    }
}

