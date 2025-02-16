import cocotb
from cocotb.clock import Clock
from cocotb.triggers import RisingEdge, FallingEdge, Timer, NextTimeStep
from cocotb.binary import BinaryValue
from cocotb.result import TestFailure
import random
from enum import Enum
from collections import namedtuple
import logging

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger('processor_tb')

# Instruction formats
class InstructionType(Enum):
    R_TYPE = 1
    I_TYPE = 2
    J_TYPE = 3

Instruction = namedtuple('Instruction', ['opcode', 'rs', 'rt', 'rd', 'shamt', 'funct'])
ITypeInstruction = namedtuple('ITypeInstruction', ['opcode', 'rs', 'rt', 'immediate'])
JTypeInstruction = namedtuple('JTypeInstruction', ['opcode', 'address'])

class ProcessorTest:
    def __init__(self, dut):
        """Initialize the processor test environment"""
        self.dut = dut
        self.clock = Clock(dut.clk, 10, 'ns')
        self.register_values = [0] * 32  # For tracking expected register values
        self.memory_values = {}  # For tracking expected memory values
        
    async def initialize(self):
        """Initialize the processor"""
        # Start clock
        cocotb.start_soon(self.clock.start())
        
        # Initialize inputs
        self.dut.rst.value = 1
        self.dut.enable.value = 0
        
        # Wait for 2 clock cycles
        for _ in range(2):
            await RisingEdge(self.dut.clk)
            
        # Deassert reset
        self.dut.rst.value = 0
        self.dut.enable.value = 1
        
        # Wait one more cycle for stable state
        await RisingEdge(self.dut.clk)
        
    def create_r_type_instr(self, opcode, rs, rt, rd, shamt, funct):
        """Create a 32-bit R-type instruction"""
        return ((opcode & 0x3F) << 26) | ((rs & 0x1F) << 21) | \
               ((rt & 0x1F) << 16) | ((rd & 0x1F) << 11) | \
               ((shamt & 0x1F) << 6) | (funct & 0x3F)
               
    def create_i_type_instr(self, opcode, rs, rt, imm):
        """Create a 32-bit I-type instruction"""
        return ((opcode & 0x3F) << 26) | ((rs & 0x1F) << 21) | \
               ((rt & 0x1F) << 16) | (imm & 0xFFFF)
               
    def create_j_type_instr(self, opcode, addr):
        """Create a 32-bit J-type instruction"""
        return ((opcode & 0x3F) << 26) | (addr & 0x3FFFFFF)

    async def check_register_write(self, reg_num, expected_value):
        """Verify register write operations"""
        await RisingEdge(self.dut.clk)
        actual_value = self.dut.RegFile.registers[reg_num].value.integer
        assert actual_value == expected_value, \
            f"Register {reg_num} value mismatch. Expected: {expected_value}, Got: {actual_value}"

class InstructionGenerator:
    """Generate test instructions for the processor"""
    
    @staticmethod
    def generate_alu_test_sequence():
        """Generate a sequence of ALU test instructions"""
        instructions = []
        
        # ADD R1, R2, R3
        instructions.append(Instruction(0x00, 2, 3, 1, 0, 0x20))
        
        # SUB R4, R5, R6
        instructions.append(Instruction(0x00, 5, 6, 4, 0, 0x22))
        
        # AND R7, R8, R9
        instructions.append(Instruction(0x00, 8, 9, 7, 0, 0x24))
        
        # OR R10, R11, R12
        instructions.append(Instruction(0x00, 11, 12, 10, 0, 0x25))
        
        return instructions

    @staticmethod
    def generate_memory_test_sequence():
        """Generate a sequence of memory test instructions"""
        instructions = []
        
        # SW R1, 0(R2)
        instructions.append(ITypeInstruction(0x2B, 2, 1, 0))
        
        # LW R3, 0(R2)
        instructions.append(ITypeInstruction(0x23, 2, 3, 0))
        
        return instructions

    @staticmethod
    def generate_branch_test_sequence():
        """Generate a sequence of branch test instructions"""
        instructions = []
        
        # BEQ R1, R2, offset
        instructions.append(ITypeInstruction(0x04, 1, 2, 4))
        
        # BNE R3, R4, offset
        instructions.append(ITypeInstruction(0x05, 3, 4, 4))
        
        return instructions

@cocotb.test()
async def test_full_processor(dut):
    """Main test sequence for the processor"""
    processor = ProcessorTest(dut)
    await processor.initialize()
    
    # Test ALU Operations
    await test_alu_operations(dut, processor)
    
    # Test Memory Operations
    await test_memory_operations(dut, processor)
    
    # Test Branch Operations
    await test_branch_operations(dut, processor)
    
    # Test Forwarding
    await test_forwarding(dut, processor)
    
    # Test Hazard Detection
    await test_hazard_detection(dut, processor)
    
    # Test Dual-Issue
    await test_dual_issue(dut, processor)

async def test_alu_operations(dut, processor):
    """Test ALU operations"""
    logger.info("Testing ALU Operations")
    
    # Test ADD
    instr = processor.create_r_type_instr(0x00, 1, 2, 3, 0, 0x20)
    dut.instr1.value = instr
    
    # Initialize source registers
    dut.RegFile.registers[1].value = 10
    dut.RegFile.registers[2].value = 20
    
    await RisingEdge(dut.clk)
    await RisingEdge(dut.clk)
    
    # Check result
    await processor.check_register_write(3, 30)

async def test_memory_operations(dut, processor):
    """Test memory operations"""
    logger.info("Testing Memory Operations")
    
    # Test Store Word
    sw_instr = processor.create_i_type_instr(0x2B, 1, 2, 0x0004)
    dut.instr1.value = sw_instr
    
    # Initialize source register and memory
    dut.RegFile.registers[2].value = 0xDEADBEEF
    
    await RisingEdge(dut.clk)
    await RisingEdge(dut.clk)
    
    # Test Load Word
    lw_instr = processor.create_i_type_instr(0x23, 1, 3, 0x0004)
    dut.instr1.value = lw_instr
    
    await RisingEdge(dut.clk)
    await RisingEdge(dut.clk)
    
    # Check loaded value
    await processor.check_register_write(3, 0xDEADBEEF)

async def test_branch_operations(dut, processor):
    """Test branch operations"""
    logger.info("Testing Branch Operations")
    
    # Test BEQ (Branch if Equal)
    beq_instr = processor.create_i_type_instr(0x04, 1, 2, 0x0004)
    dut.instr1.value = beq_instr
    
    # Set registers equal
    dut.RegFile.registers[1].value = 10
    dut.RegFile.registers[2].value = 10
    
    initial_pc = dut.PC.value.integer
    
    await RisingEdge(dut.clk)
    await RisingEdge(dut.clk)
    
    # Check if branch was taken
    assert dut.PC.value.integer == initial_pc + 4, "Branch not taken when it should have been"

async def test_forwarding(dut, processor):
    """Test forwarding unit"""
    logger.info("Testing Forwarding Unit")
    
    # Create instruction sequence that requires forwarding
    add_instr = processor.create_r_type_instr(0x00, 1, 2, 3, 0, 0x20)
    sub_instr = processor.create_r_type_instr(0x00, 3, 4, 5, 0, 0x22)
    
    dut.instr1.value = add_instr
    await RisingEdge(dut.clk)
    dut.instr2.value = sub_instr
    
    # Check forwarding signals
    await RisingEdge(dut.clk)
    assert dut.ForwardA1.value != 0, "Forwarding not activated when needed"

async def test_hazard_detection(dut, processor):
    """Test hazard detection unit"""
    logger.info("Testing Hazard Detection Unit")
    
    # Create load-use hazard
    lw_instr = processor.create_i_type_instr(0x23, 1, 2, 0)
    add_instr = processor.create_r_type_instr(0x00, 2, 3, 4, 0, 0x20)
    
    dut.instr1.value = lw_instr
    await RisingEdge(dut.clk)
    dut.instr2.value = add_instr
    
    # Check if stall is inserted
    await RisingEdge(dut.clk)
    assert dut.Stall.value == 1, "Hazard not detected"

async def test_dual_issue(dut, processor):
    """Test dual-issue execution"""
    logger.info("Testing Dual-Issue Execution")
    
    # Create two independent instructions
    add_instr1 = processor.create_r_type_instr(0x00, 1, 2, 3, 0, 0x20)
    add_instr2 = processor.create_r_type_instr(0x00, 4, 5, 6, 0, 0x20)
    
    # Set initial register values
    dut.RegFile.registers[1].value = 10
    dut.RegFile.registers[2].value = 20
    dut.RegFile.registers[4].value = 30
    dut.RegFile.registers[5].value = 40
    
    # Issue both instructions
    dut.instr1.value = add_instr1
    dut.instr2.value = add_instr2
    
    await RisingEdge(dut.clk)
    await RisingEdge(dut.clk)
    
    # Check results
    await processor.check_register_write(3, 30)  # 10 + 20
    await processor.check_register_write(6, 70)  # 30 + 40

# Additional helper functions for verification
async def monitor_pipeline_stages(dut):
    """Monitor all pipeline stages"""
    while True:
        await RisingEdge(dut.clk)
        logger.debug(f"IF Stage - PC: {dut.PC.value.integer}")
        logger.debug(f"ID Stage - Instr1: {hex(dut.instr1D.value.integer)}")
        logger.debug(f"EX Stage - ALU1 Result: {hex(dut.ALUResult1.value.integer)}")
        logger.debug(f"MEM Stage - Memory Access: {dut.MemReadEn1M.value}")
        logger.debug(f"WB Stage - WriteBack: {dut.RegWriteEn1W.value}")

async def check_pipeline_hazards(dut):
    """Monitor for pipeline hazards"""
    while True:
        await RisingEdge(dut.clk)
        if dut.Stall.value:
            logger.info(f"Pipeline stall detected at PC: {dut.PC.value.integer}")
        if dut.FlushIFID1.value:
            logger.info(f"Pipeline flush detected")

# Test configuration and utilities
def create_test_program():
    """Create a test program with a mix of instructions"""
    return [
        # Initialize registers
        ITypeInstruction(0x08, 0, 1, 10),    # ADDI R1, R0, 10
        ITypeInstruction(0x08, 0, 2, 20),    # ADDI R2, R0, 20
        
        # Test ALU operations
        Instruction(0x00, 1, 2, 3, 0, 0x20), # ADD R3, R1, R2
        Instruction(0x00, 1, 2, 4, 0, 0x22), # SUB R4, R1, R2
        
        # Test memory operations
        ITypeInstruction(0x2B, 3, 1, 0),     # SW R1, 0(R3)
        ITypeInstruction(0x23, 3, 5, 0),     # LW R5, 0(R3)
        
        # Test branches
        ITypeInstruction(0x04, 1, 2, 4),     # BEQ R1, R2, 4
        ITypeInstruction(0x05, 1, 2, 4),     # BNE R1, R2, 4
    ]