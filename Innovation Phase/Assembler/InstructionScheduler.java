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
            isArithmetic = false;
            isMemory = false;
            isBranch = false;
            
            switch (op) {
                // Arithmetic operations
                case "add": case "sub": case "mul": case "div":
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
                case "addi": case "subi": case "xori":
                case "andi": case "ori": case "slti":
                    isArithmetic = true;
                    if (operands.size() >= 2) {
                        writeRegs.add(operands.get(0));
                        readRegs.add(operands.get(1));
                    }
                    break;
                
                // Memory operations
                case "lw": case "sw":
                    isMemory = true;
                    if (operands.size() >= 2) {
                        if (op.equals("lw")) {
                            writeRegs.add(operands.get(0));
                        } else {
                            readRegs.add(operands.get(0));
                        }
                        String[] memOp = parseMemoryOperand(operands.get(1));
                        readRegs.add(memOp[1]);  // Base register
                    }
                    break;
                
                // Branch/Jump operations
                case "beq": case "bne": case "bltz": case "bgez": 
                case "j": case "jal": case "jr":
                    isBranch = true;
                    if (op.equals("jal")) {
                        writeRegs.add("31"); // $ra
                    }
                    if (op.equals("jr")) {
                        readRegs.add(operands.get(0));
                    }
                    if (!op.startsWith("j")) {
                        readRegs.add(operands.get(0));
                        if (operands.size() > 1 && !operands.get(1).matches("^-?\\d+$")) {
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

    private static class DependencyTracker {
        private Map<String, Integer> lastWrite;
        private Map<String, Integer> lastRead;
        private int lastBranch;
        
        public DependencyTracker() {
            this.lastWrite = new HashMap<>();
            this.lastRead = new HashMap<>();
            this.lastBranch = -1;
        }
        
        public boolean hasDependency(Instruction inst, int currentIndex) {
            // Control dependency
            if (lastBranch >= currentIndex - 1) return true;
            
            // RAW dependencies
            for (String reg : inst.readRegs) {
                if (lastWrite.containsKey(reg)) {
                    int lastWriteIndex = lastWrite.get(reg);
                    if (lastWriteIndex >= currentIndex - 1) return true;
                }
            }
            
            // WAW dependencies
            for (String reg : inst.writeRegs) {
                if (lastWrite.containsKey(reg)) {
                    int lastWriteIndex = lastWrite.get(reg);
                    if (lastWriteIndex >= currentIndex - 1) return true;
                }
            }
            
            // WAR dependencies
            for (String reg : inst.writeRegs) {
                if (lastRead.containsKey(reg)) {
                    int lastReadIndex = lastRead.get(reg);
                    if (lastReadIndex >= currentIndex - 1) return true;
                }
            }
            
            return false;
        }
        
        public void updateDependencies(Instruction inst, int index) {
            if (inst.isBranch) {
                lastBranch = index;
            }
            for (String reg : inst.readRegs) {
                lastRead.put(reg, index);
            }
            for (String reg : inst.writeRegs) {
                lastWrite.put(reg, index);
            }
        }
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
            for (Instruction inst : body) {
                if (inst.op.equals("addi") && inst.operands.get(0).equals(inst.operands.get(1))) {
                    counterReg = inst.operands.get(0);
                    try {
                        step = Integer.parseInt(inst.operands.get(2));
                    } catch (NumberFormatException e) {
                        step = 1;
                    }
                }
                modifiedRegs.addAll(inst.writeRegs);
            }

            if (branchInst.op.equals("beq") || branchInst.op.equals("bne")) {
                boundReg = branchInst.operands.get(1);
            }
            else if(branchInst.op.equals("bgez") || branchInst.op.equals("bltz")) {
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
                return original;
            }
            return registerMap.computeIfAbsent(original, k -> "$" + nextRegister++);
        }

        public void clear() {
            registerMap.clear();
        }
    }

    public String[] schedule(String[] program) {
        List<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < program.length; i++) {
            if (!program[i].trim().isEmpty()) {
                instructions.add(new Instruction(program[i], i));
            }
        }

        List<Loop> loops = identifyLoops(instructions);
        List<String> optimized = new ArrayList<>();
        int currentPos = 0;

        for (Loop loop : loops) {
            while (currentPos < loop.startIndex) {
                optimized.add(instructions.get(currentPos).original);
                currentPos++;
            }

            if (loop.isUnrollable()) {
                optimized.addAll(unrollLoop(loop, instructions));
            } else {
                for (int i = loop.startIndex; i <= loop.endIndex; i++) {
                    optimized.add(instructions.get(i).original);
                }
            }
            currentPos = loop.endIndex + 1;
        }

        while (currentPos < instructions.size()) {
            optimized.add(instructions.get(currentPos).original);
            currentPos++;
        }

        return scheduleInstructions(optimized.toArray(new String[0]));
    }

    public String[] scheduleInstructions(String[] program) {
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
            
            if (inst1.op.equals("label") || inst1.op.equals("comment")) {
                scheduledProgram.add(inst1.original);
                i++;
                continue;
            }
            
            Instruction inst2 = null;
            int j = i + 1;
            
            while (j < instructions.size()) {
                Instruction candidate = instructions.get(j);
                
                if (candidate.op.equals("label") || candidate.op.equals("comment")) {
                    break;
                }
                
                if (canScheduleTogether(inst1, candidate) && 
                    !depTracker.hasDependency(candidate, j) &&
                    !hasDirectDependency(inst1, candidate)) {
                    inst2 = candidate;
                    instructions.remove(j);
                    break;
                }
                j++;
            }
            
            depTracker.updateDependencies(inst1, i);
            
            if (inst2 != null) {
                depTracker.updateDependencies(inst2, i+1);
                
                if (shouldSwapOrder(inst1, inst2)) {
                    scheduledProgram.add(inst2.original);
                    scheduledProgram.add(inst1.original);
                } else {
                    scheduledProgram.add(inst1.original);
                    scheduledProgram.add(inst2.original);
                }
            } else {
                // If there's a dependency, add NOP
                if (i + 1 < instructions.size() && 
                    (hasDirectDependency(inst1, instructions.get(i + 1)) || 
                     depTracker.hasDependency(instructions.get(i + 1), i + 1))) {
                    scheduledProgram.add(inst1.original);
                    scheduledProgram.add("nop");
                } else {
                    scheduledProgram.add(inst1.original);
                }
            }
            
            i++;
        }
        
        return scheduledProgram.toArray(new String[0]);
    }

    private boolean canScheduleTogether(Instruction inst1, Instruction inst2) {
        if (inst1.op.equals("label") || inst2.op.equals("label")) return false;
        if (inst1.isBranch && inst2.isBranch) return false;
        if (inst1.isMemory && inst2.isMemory) return false;
        
        // Don't reorder branch condition setup (like slt) with its branch
        if (inst2.isBranch && inst2.readRegs.stream().anyMatch(reg -> inst1.writeRegs.contains(reg))) {
            return false;
        }
        
        if (inst1.isBranch) return false;
        if (inst1.op.equals("jal") || inst2.op.equals("jal")) return false;
        
        return true;
    }

    private boolean hasDirectDependency(Instruction inst1, Instruction inst2) {
        // If inst2 is a branch and inst1 sets a condition register that the branch uses,
        // they must be kept in order
        if (inst2.isBranch && inst2.readRegs.stream().anyMatch(reg -> inst1.writeRegs.contains(reg))) {
            return false;
        }
        
        // Regular dependency checks
        // RAW
        for (String reg : inst2.readRegs) {
            if (inst1.writeRegs.contains(reg)) return true;
        }
        
        // WAW
        for (String reg : inst2.writeRegs) {
            if (inst1.writeRegs.contains(reg)) return true;
        }
        
        // WAR
        for (String reg : inst2.writeRegs) {
            if (inst1.readRegs.contains(reg)) return true;
        }
        
        return false;
    }
    
   

    private boolean shouldSwapOrder(Instruction inst1, Instruction inst2) {
        if (inst2.isBranch) return false;
        if (inst1.isBranch) return true;
        if (inst1.isMemory && inst2.isArithmetic) return false;
        if (inst2.isMemory && inst1.isArithmetic) return true;
        return false;
    }

    private List<String> unrollLoop(Loop loop, List<Instruction> instructions) {
        List<String> unrolled = new ArrayList<>();
        RegisterRenamer renamer = new RegisterRenamer(nextRegister);

        unrolled.add(loop.label + ":");

        for (int i = 0; i < UNROLL_FACTOR; i++) {
            RegisterRenamer iterationRenamer = new RegisterRenamer(nextRegister + (i * loop.modifiedRegs.size()));

            for (Instruction inst : loop.body) {
                if (!inst.op.equals("beq") && !inst.op.equals("bgez") && 
                    !inst.op.equals("bltz") && !inst.op.equals("bne") && 
                    !inst.isBranch) {
                    String newInst = rewriteInstruction(inst, iterationRenamer);
                    unrolled.add(newInst);
                }
            }

            if (i < UNROLL_FACTOR - 1) {
                String exitCheckSlt = generateExitCheckSlt(loop, i, iterationRenamer);
                unrolled.add(exitCheckSlt);
                String exitCheckBne = generateExitCheckBne(loop, i, iterationRenamer);
                unrolled.add(exitCheckBne);
            }
        }

        for (String reg : loop.modifiedRegs) {
            String lastRenamedReg = renamer.getRenamedRegister(reg);
            unrolled.add("add " + reg + ", " + lastRenamedReg + ", $0");
        }

        unrolled.add(rewriteBranchInstruction(loop.branchInst, loop.label));
        unrolled.add("exit_" + loop.label + ":");

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
        return result.substring(0, result.length() - 1);
    }

    private String generateExitCheckSlt(Loop loop, int iteration, RegisterRenamer renamer) {
        String conditionReg = renamer.getRenamedRegister("$condition");
        String counterReg = renamer.getRenamedRegister(loop.counterReg);
        String boundReg = loop.boundReg;
        return "slt " + conditionReg + ", " + boundReg + ", " + counterReg;
    }

    private String generateExitCheckBne(Loop loop, int iteration, RegisterRenamer renamer) {
        String conditionReg = renamer.getRenamedRegister("$condition");
        return "bne " + conditionReg + ", $0, exit_" + loop.label;
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
    //Remove Duplicates Benchmark
    public static String[] getProgram1(){
        return new String[]{
            "ADDI $8, $0, 14",
            "ADDI $9, $0, 0",
            "ADDI $10, $0, 0",
        "outerLoop:",
            "SLT $11, $9, $8",
            "BEQ $11, $0, finish",
            "ADD $12, $9, $0",
            "ADDI $14, $0, 0",
            "LW $13, 0x0($12)",
            "ADDI $15, $0, 0",
        "innerLoop:",
            "SLT $24, $14, $10",
            "BEQ $24, $0, addToResult",
            "ADD $25, $14, $0",
            "NOP",
            "LW $16, 0xE($25)",
            "NOP",
            "BEQ $13, $16, duplicateFound",
            "ADDI $14, $14, 1",
            "JAL innerLoop",
        "duplicateFound:",
            "ADDI $15, $0, 1",
            "JAL outerContinue",
            "NOP",
        "addToResult:",
            "BEQ $15, $0, storeValue",
            "NOP",
            "JAL outerContinue",
        "storeValue:",
            "ADD $25, $10, $0",
            "NOP",
            "SW $13, 0xE($25)",
            "ADDI $10, $10, 1",
        "outerContinue:",
            "ADDI $9, $9, 1",
            "JAL outerLoop",
        "finish:",
            "NOP"
        };
        
        
    }
    //Multiplication Using Addition BenchMark
    public static String[] getProgram2(){
        return new String[]{
           "XORI $24, $0, 0x5",
            "XORI $25, $0, 0x3",
            "JAL Mul_Fun",
            "NOP",
            "J Finish",
            "Mul_Fun:",
            "ANDI $23, $0, 0",
            "ADDI $22, $25, -1",
            "Mul_Loop:",
            "ADD $23, $23, $24",
            "ADDI $22, $22, -1",
            "BGEZ $22, Mul_Loop",
            "JR $31",
            "Finish:",
            "NOP"
        };
    }
//Scalar Multiplication Using Addition BenchMark
    public static String[] getProgram3(){
        return new String[]{
            "ADDI $17, $0, 10",
            "ANDI $18, $0, 0",
            "XORI $24, $0, 2",
            "Outer_Loop:",
            "ADD $19, $18, $0",
            "NOP",
            "LW $25, 0x0($19)",
            "JAL Mul_Fun",
            "SW $23, 0x0($19)",
            "ADDI $18, $18, 1",
            "SUB $20, $18, $17",
            "NOP",
            "BLTZ $20, Outer_Loop",
            "NOP",
            "J Finish",
            "Mul_Fun:",
            "ANDI $23, $0, 0",
            "ADDI $22, $25, -1",
            "Mul_Loop:",
            "ADD $23, $23, $24",
            "ADDI $22, $22, -1",
            "NOP",
            "BGEZ $22, Mul_Loop",
            "NOP",
            "JR $31",
            "Finish:",
            "NOP"
        };
    }
    //Sparse Matrix Count Benchmark
    public static String[] getProgram4(){
        return new String[]{
            "ADDI $8, $0, 0",
            "ADDI $16, $0, 4",
            "ADDI $17, $0, 3",
            "ADDI $20, $0, 0",
            "ADDI $9, $0, 0",
            "NOP",
            "outerLoop:",
            "SLT $11, $9, $16",
            "BEQ $11, $0, exitOuter",
            "ADDI $14, $0, 0",
            "ADDI $15, $0, 0",
            "Multiply:",
            "SLT $11, $15, $9",
            "BEQ $11, $0, endMultiply",
            "ADD $14, $14, $17",
            "ADDI $15, $15, 1",
            "JAL Multiply",
            "endMultiply:",
            "ADD $14, $14, $0",
            "NOP",
            "ADD $14, $14, $20",
            "ADDI $10, $0, 0",
            "innerLoop:",
            "SLT $11, $10, $17",
            "BEQ $11, $0, exitInner",
            "ADD $15, $10, $0",
            "NOP",
            "ADD $24, $14, $15",
            "NOP",
            "LW $24, 0x0($24)",
            "NOP",
            "BNE $24, $0, notZero",
            "ADDI $8, $8, 1",
            "notZero:",
            "ADDI $10, $10, 1",
            "JAL innerLoop",
            "exitInner:",
            "ADDI $9, $9, 1",
            "JAL outerLoop",
            "exitOuter:",
            "NOP"
        }; 
    }

    //Selection Sort Benchmark
    public static String[] getProgram5(){
        return new String[]{
            "ADDI $13, $0, 20",
            "ADD $11, $0, $0",
        "LOOP1:",
            "XOR $21, $11, $0",
            "ADD $12, $0, $0",
        "LOOP2:",
            "XOR $22, $12, $0",
            "LW $8, 0x0($21)",
            "LW $9, 0x0($22)",
            "NOP",
        "IF:",
            "SLT $10, $8, $9",
            "BEQ $10, $0, ENDIF",
            "ADD $3, $8, $0",
            "NOP",
            "ADD $8, $9, $0",
            "NOP",
            "ADD $9, $3, $0",
            "SW $8, 0x0($21)",
            "NOP",
            "SW $9, 0x0($22)",
        "ENDIF:",
            "ADDI $12, $12, 1",
            "NOP",
            "SLT $10, $12, $13",
            "BNE $10, $0, LOOP2",
            "ADDI $11, $11, 1",
            "NOP",
            "SLT $10, $11, $13",
            "BNE $10, $0, LOOP1"
        };
        
    }
    //Max & Min BenchMark
    public static String[] getProgram6(){
        return new String[]{
            "ORI $2, $0, 0x0",
            "ADDI $20, $0, 0xA",
            "XORI $31, $0, 0x1",
            "ANDI $5, $0, 0x0",
            "LW $10, 0x0($5)",
            "NOP",
            "LW $15, 0x0($5)",
        "LOOP:",
            "ADDI $2, $2, 1",
            "NOP",
            "SGT $25, $20, $2",
            "BNE $25, $31, END",
            "ADD  $5, $2, $0 ",
            "NOP",
            "LW $16, 0x0($5)",
            "NOP",
            "SGT $26, $16, $10",
            "BEQ $26, $0, MIN",
            "OR $10, $16, $0",
            "J LOOP",
        "MIN:",
            "SLT $27, $16, $15",
            "BEQ $27, $0, LOOP",
            "ADD $15, $16, $0",
            "J LOOP",
        "END:",
            "NOP"
        };         
    }
    // Binary Search Benchmark
    public static String[] getProgram7(){
        return new String[]{
            "ADDI $1, $0, 0x0",
            "ADDI $2, $0, 0xB",
            "ADDI $3, $0, 0x7",
            "NOP",
        "loop:",
            "SLT $7, $2, $1",
            "BNE $0, $7, notFound",
            "ADD $4, $2, $1",
            "NOP",
            "SRL $5, $4, 1",
            "NOP",
            "LW $6, 0x0($5)",
            "NOP",
            "BEQ $3, $6, found",
            "NOP",
            "SLT $6, $6, $3",
            "BEQ $6, $0, leftHalf",
            "NOP",
            "J rightHalf",
        "leftHalf:",
            "ADDI $2, $5, 0xFFFF",
            "J loop",
        "rightHalf:",
            "ADDI $1, $5, 0x1",
            "J loop",
        "found:",
            "ADD $8, $0, $5",
            "J finish",
        "notFound:",
            "ADDI $8, $0, 0xFFFF",
            "J finish",
        "finish:",
            "NOP"
        };
        
          
    }
    // Example usage
    public static void main(String[] args) {
        InstructionScheduler scheduler = new InstructionScheduler();
        
        // Test case 1: Basic arithmetic and memory operations
        String[] program = InstructionScheduler.getProgram6();
       
        System.out.println("Test Case 1");
        String[] scheduled = scheduler.schedule(program);
        printProgram(program, scheduled);

    
    }
    
    private static void printProgram(String[] original, String[] scheduled) {
        System.out.println("Original program:");
        for (String inst : original) {
            System.out.println(inst);
        }
    }
}