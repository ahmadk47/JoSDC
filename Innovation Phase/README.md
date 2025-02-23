# JoSDC

# Static Dual-Issue Processor - Usage Guide

## Overview

This project implements a **static dual-issue processor**, meaning that **two instructions are issued per cycle**, provided there are no dependency conflicts. The processor does not dynamically handle dependencies, so scheduling must be handled at compile-time. This guide explains how to prepare, assemble, and simulate your program correctly using the provided tools.

## Instruction Formatting

Instructions must be scheduled in a strict format with **no additional spaces**. Each instruction follows MIPS syntax and must be correctly aligned to avoid errors in processing.

```
<operation> <destination_register>, <source_register1>, <source_register2>
```

### Instruction Formatting Rules:

- Instructions must be written **without extra spaces**.



### Examples:

```
add $1, $2, $3    # $1 = $2 + $3
sub $4, $5, $6    # $4 = $5 - $6
and $13, $14, $15 # $13 = $14 & $15
or  $16, $17, $18 # $16 = $17 | $18
xor $19, $20, $21 # $19 = $20 ^ $21
slt $22, $23, $24 # $22 = 1 if $23 < $24 else 0
```

### Branch and Load/Store Examples:

```
beq $1, $2, label   # Branch to 'label' if $1 == $2
bne $3, $4, label   # Branch to 'label' if $3 != $4
bltz $5, label      # Branch to 'label' if $5 < 0
bgez $6, label      # Branch to 'label' if $6 >= 0
j target           # Jump to 'target'
jr $31            # Jump to the address in $31
jal target         # Jump to 'target' and save return address in $31
lw  $7, 0($8)      # Load word from address in $8 to $7
sw  $9, 4($10)     # Store word from $9 to address in $10 + 4
```

### Shift and Immediate Examples:

```
sll $2, $3, 4     # Shift left logical: $2 = $3 << 4
srl $4, $5, 2     # Shift right logical: $4 = $5 >> 2
addi $8, $9, 10   # Add immediate: $8 = $9 + 10
andi $10, $11, 15 # AND immediate: $10 = $11 & 15
ori  $12, $13, 20 # OR immediate: $12 = $13 | 20
xori $14, $15, 25 # XOR immediate: $14 = $15 ^ 25
```

Maintaining this format is crucial for correct processing.

## Workflow

### 1. **Instruction Scheduling**

- Use the **Python scheduler** (scheduler.py) to order the instructions according to dual-issue constraints.
- The scheduler outputs a text file containing the properly ordered instructions.

### 2. **Assembling Instructions**

- The **Java-based assembler** (Assembler.java) reads the scheduled instructions from the text file and converts them into machine code.
- Ensure that the assembler runs **without errors or warnings**.

### 3. **Preparing the Instruction Memory**

- A script (loadmif.py) takes the assembler's output and loads it into the **instruction memory initialization file (MIF)**.
- Verify that the **MIF file format** matches Quartus memory requirements.

### 4. **Preparing the Instruction Memory**

- The process is automated by running pipeline.py file
- You need to fill the instructions.txt file with the instructions

### 5. **Data Memory (Optional)**

- If needed, prepare and load the **data memory MIF file**.
- Ensure correct **address alignment** when loading data.

### 6. **Quartus Simulation**

- Open **Quartus** and **compile** or **run synthesis**.
- Navigate to: `Tools` → `Run Simulation Tool` → `RTL Simulation`.
- Add the required signals to the waveform window to **observe register updates, ALU results, and memory transactions**.

## Debugging and Optimization

- **Branch Handling:** Ensure branch delay slots are handled correctly in scheduling.
- **Load/Store Optimization:** Avoid unnecessary memory accesses to reduce execution cycles.
- **Pipeline Efficiency:** Utilize the dual-issue capability effectively by pairing independent instructions.

## Notes

- Ensure all scripts and tools are executed in order.
- Follow the exact instruction format to avoid errors during assembly.
- Use the Quartus simulation tools to verify processor behavior and execution correctness.
- Ensure branch target labels are correctly assigned and referenced in the scheduled instruction sequence.
- Utilize jump and branch instructions carefully to avoid pipeline stalls.
- Use Quartus' **Signal Tap or ModelSim** to inspect instruction execution in detail.

---

