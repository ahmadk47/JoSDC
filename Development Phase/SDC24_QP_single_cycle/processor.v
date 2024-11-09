module processor(clk, rst, PC);

//inputs
input clk, rst;

//outputs
output [5:0] PC;

wire [31:0] instruction, writeData, readData1, readData2, extImm, ALUin2, ALUResult, memoryReadData;
wire [15:0] imm;
wire [5:0] opCode, funct, nextPC, PCPlus1, adderResult;
wire [4:0] rs, rt, rd, writeRegister, shamt;
wire [2:0] ALUOp;
wire RegDst, Branch, MemReadEn, MemtoReg, MemWriteEn, RegWriteEn, ALUSrc, zero, PCsrc;

assign opCode = instruction[31:26];
assign rd = instruction[15:11]; 
assign rs = instruction[25:21];  
assign shamt = instruction[10:6];
assign rt = instruction[20:16];  
assign imm = instruction[15:0];
assign funct = instruction[5:0];

programCounter pc(.clk(clk), .rst(rst), .PCin(nextPC), .PCout(PC));

adder PCAdder(.in1(PC), .in2(6'b1), .out(PCPlus1));

instructionMemory IM(.address(nextPC), .clock(clk), .q(instruction));

controlUnit CU(.opCode(opCode), .funct(funct), 
      .RegDst(RegDst), .Branch(Branch), .MemReadEn(MemReadEn), .MemtoReg(MemtoReg),
      .ALUOp(ALUOp), .MemWriteEn(MemWriteEn), .RegWriteEn(RegWriteEn), .ALUSrc(ALUSrc));

mux2x1 #(5) RFMux(.in1(rt), .in2(rd), .s(RegDst), .out(writeRegister)); // changed from "WriteRegister" to "writeRegister"

registerFile RF(.clk(clk), .rst(rst), .we(RegWriteEn), 
    .readRegister1(rs), .readRegister2(rt), .writeRegister(writeRegister),
    .writeData(writeData), .readData1(readData1), .readData2(readData2));
 
signextender SignExtend(.in(imm), .out(extImm));

mux2x1 #(32) ALUMux(.in1(readData2), .in2(extImm), .s(ALUSrc), .out(ALUin2));

ALU alu(.operand1(readData1), .operand2(ALUin2), .shamt(shamt) ,.opSel(ALUOp), .result(ALUResult), .zero(zero));

ANDGate branchAnd(.in1(zero), .in2(Branch), .out(PCsrc));

adder branchAdder(.in1(PCPlus1), .in2(imm[5:0]), .out(adderResult));

dataMemory DM(.address(ALUResult[7:0]), .clock(~clk), .data(readData2), .rden(MemReadEn), .wren(MemWriteEn), .q(memoryReadData));

mux2x1 #(32) WBMux(.in1(ALUResult), .in2(memoryReadData), .s(MemtoReg), .out(writeData)); // swapped order of inputs

mux2x1 #(6) PCMux(.in1(PCPlus1), .in2(adderResult), .s(PCsrc), .out(nextPC));


endmodule 