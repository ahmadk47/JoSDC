module processor(clk, rst, PC, enable);

//inputs
input clk, rst, enable;

//outputs
output [7:0] PC;

wire [31:0] instruction, writeData, readData1, readData2, extImm, ALUin2, ALUResult, memoryReadData,
 memoryReadDataW, instructionD, readData1E, readData2E, extImmE, instructionE, ALUResultM, readData2M, 
 ALUResultW, ForwardAMuxOut, ForwardBMuxOut, ForwardBMuxOutM;

wire [15:0] imm;

wire [5:0] opCode, funct;

wire [7:0] nextPC, PCPlus1, branchAdderResult, jumpMuxOut, branchMuxOut, PCPlus1D, PCPlus1E, PCPlus1M, PCPlus1W, imInput;

wire [4:0] rs, rt, rd, rsE, rtE, rdE, writeRegister, shamt, writeRegisterM, writeRegisterW, shamtE;

wire [3:0] ALUOp, ALUOpE, ALUOpNew;

wire [1:0] regDst, memToReg, memToRegE, regDstE, memToRegM, memToRegW, ForwardA, ForwardB,memToRegNew, regDstNew;

wire pcSrc, jump, branch, memRead, memWrite, ALUSrc, regWrite, zero, xnorOut,
overflow, regWriteE, 
memWriteE, memReadE, ALUSrcE, regWriteM, memWriteM, memReadM, regWriteW,Flush,Stall, IFIDReset,EnablePCIFID,pcSrcNew,
jumpNew,regWriteNew,memWriteNew, memReadNew,ALUSrcNew, branchNew, branchTaken, prediction, compOut;

assign opCode = instructionD[31:26];
assign rd = instructionD[15:11]; 
assign rs = instructionD[25:21];  
assign shamt = instructionD[10:6];
assign rt = instructionD[20:16];  
assign imm = instructionD[15:0];
assign funct = instructionD[5:0];



// FETCH STAGE

programCounter pc(.clk(clk), .rst(rst), .enable(EnablePCIFID), .PCin(nextPC), .PCout(PC)); 

adder #(8) pcAdder(.in1(PC), .in2(8'b1), .out(PCPlus1));

instructionMemory IM(.address(nextPC), .aclr (~rst), .clock(clk), .q(instruction));

mux2x1 #(8) pcMux(.in1(branchMuxOut), .in2(jumpMuxOut), .s(pcSrcNew), .out(nextPC)); 

mux2x1 #(8) jumpMux(.in1(readData1[7:0]), .in2(instructionD[7:0]), .s(jumpNew), .out(jumpMuxOut));

mux2x1 #(8) branchMux(.in1(PCPlus1), .in2(branchAdderResult), .s(prediction), .out(branchMuxOut));


pipe #(40) IF_ID(.D({PCPlus1, instruction}), .Q({PCPlus1D, instructionD}), .clk(clk), .reset(~IFIDReset), .enable(EnablePCIFID)); // fill pipes top to bottom


BranchPredictionUnit BPU(.branch_taken(branchTaken), .clk(clk), .reset(rst), .branch(branchNew), .pc(PC), .prediction(prediction));


// DECODE STAGE

signextender SignExtend(.in(imm), .out(extImm));

controlUnit CU(.opCode(opCode), .funct(funct), 
     .RegDst(regDst), .Branch(branch), .MemReadEn(memRead), .MemtoReg(memToReg),
    .ALUOp(ALUOp), .MemWriteEn(memWrite), .RegWriteEn(regWrite), .ALUSrc(ALUSrc), .Jump(jump), .PcSrc(pcSrc));



registerFile RF(.clk(clk), .rst(rst), .we(regWriteW), 
   .readRegister1(rs), .readRegister2(rt), .writeRegister(writeRegisterW),
   .writeData(writeData), .readData1(readData1), .readData2(readData2));
	

Comparator #(32) branchComparator(.equal(compOut), .a(readData1), .b(readData2));

pipe #(1) CP(.D(compOut), .Q(zero), .clk(clk), .reset(rst), .enable(enable));
	
XNORGate branchXnor(.out(xnorOut), .in1(instructionD[26]), .in2(~zero));

ANDGate branchAnd(.in1(xnorOut), .in2(branchNew), .out(branchTaken));          

adder #(8) branchAdder(.in1(PCPlus1D), .in2(imm[7:0]), .out(branchAdderResult));


HazardDetectionUnit HDU (.Stall(Stall), .Flush(Flush), .takenBranch(branchTaken), .pcSrc(pcSrcNew), .writeRegisterE(writeRegister), .rsD(rs), .rtD(rt), .memReadE(memReadE), .branch(branchNew), .prediction(prediction)); // FIX HDU

ORGate IfIdReset(.in1(~rst), .in2(Flush), .out(IFIDReset));

mux2x1 #(15) CUMux(.in1({pcSrc,jump,regWrite, memToReg, memWrite, memRead, ALUOp, regDst, ALUSrc, branch}), .in2(15'b0), .s(Stall),
 .out({pcSrcNew, jumpNew, regWriteNew, memToRegNew, memWriteNew, memReadNew, ALUOpNew, regDstNew, ALUSrcNew, branchNew}));

ANDGate holdGate(.in1(~Stall), .in2(enable), .out(EnablePCIFID));
// CHECK INPUTS AND OUTPUTS
	
pipe #(136) ID_EX(.D({regWriteNew, memToRegNew, memWriteNew, memReadNew, ALUOpNew, regDstNew, ALUSrcNew, PCPlus1D, readData1, readData2, extImm, rs, rt, rd, shamt}),
 .Q({regWriteE, memToRegE, memWriteE, memReadE, ALUOpE, regDstE, ALUSrcE, PCPlus1E, readData1E, readData2E, extImmE, rsE, rtE, rdE, shamtE}), .clk(clk), .reset(rst), .enable(enable));

 
 
 
 
 
// EXECUTE STAGE



mux3to1 #(32) ForwardAMux(.in1(readData1E), .in2(ALUResultM), .in3(writeData), .s(ForwardA), .out(ForwardAMuxOut));

mux3to1 #(32) ForwardBMux(.in1(readData2E), .in2(ALUResultM), .in3(writeData), .s(ForwardB), .out(ForwardBMuxOut));

mux2x1 #(32) ALUMux(.in1(ForwardBMuxOut), .in2(extImmE), .s(ALUSrcE), .out(ALUin2));

ALU alu(.operand1(ForwardAMuxOut), .operand2(ALUin2), .shamt(shamtE) ,.opSel(ALUOpE), .result(ALUResult), .overflow(overflow));

mux3to1 #(5) RFMux(.in1(rtE), .in2(rdE), .in3(5'b11111), .s(regDstE), .out(writeRegister));


ForwardingUnit FU(.rsE(rsE), .rtE(rtE), .writeRegisterM(writeRegisterM), .writeRegisterW(writeRegisterW), .regWriteM(regWriteM), .regWriteW(regWriteW), .ForwardA(ForwardA), .ForwardB(ForwardB));

pipe #(82) EX_MEM(.D({regWriteE, memToRegE, memWriteE, memReadE, PCPlus1E, ALUResult, ForwardBMuxOut, writeRegister}), 
.Q({regWriteM, memToRegM, memWriteM, memReadM, PCPlus1M, ALUResultM, ForwardBMuxOutM, writeRegisterM}), .clk(clk), .reset(rst), .enable(enable));



// MEMORY STAGE

dataMemory DM(.address(ALUResultM[7:0]), .clock(~clk), .data(ForwardBMuxOutM), .rden(memReadM), .wren(memWriteM), .q(memoryReadData));


pipe #(80) MEM_WB(.D({regWriteM, memToRegM, PCPlus1M, ALUResultM, memoryReadData, writeRegisterM}), 
.Q({regWriteW, memToRegW, PCPlus1W, ALUResultW, memoryReadDataW, writeRegisterW}), .clk(clk), .reset(rst), .enable(enable));



// WRITEBACK STAGE

mux3to1 #(32) WBMux(.in1(ALUResultW), .in2(memoryReadDataW), .in3({{24{1'b0}} ,PCPlus1W}), .s(memToRegW), .out(writeData));


endmodule 