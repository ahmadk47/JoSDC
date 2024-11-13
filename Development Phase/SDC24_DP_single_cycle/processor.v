module processor(clk, rst, PC);

//inputs
input clk, rst;

//outputs
output [7:0] PC;

wire [31:0] instruction, writeData, readData1, readData2, extImm, ALUin2, ALUResult, memoryReadData;
wire [15:0] imm;
wire [5:0] opCode, funct;
wire [7:0] nextPC, PCPlus1, branchAdderResult, jumpMuxOut, branchMuxOut;
wire [4:0] rs, rt, rd, writeRegister, shamt;
wire [3:0] ALUOp;
wire [1:0] regDst, memToReg;
wire pcSrc, jump, branch, memRead, memWrite, ALUSrc, regWrite, zero, xnorOut, branchMuxSel, overflow;


assign opCode = instruction[31:26];
assign rd = instruction[15:11]; 
assign rs = instruction[25:21];  
assign shamt = instruction[10:6];
assign rt = instruction[20:16];  
assign imm = instruction[15:0];
assign funct = instruction[5:0];


programCounter pc(.clk(clk), .rst(rst), .PCin(nextPC), .PCout(PC));

adder #(8) pcAdder(.in1(PC), .in2(8'b1), .out(PCPlus1));

instructionMemory IM(.address(nextPC), .clock(clk), .q(instruction));

controlUnit CU(.opCode(opCode), .funct(funct), 
     .RegDst(regDst), .Branch(branch), .MemReadEn(memRead), .MemtoReg(memToReg),
    .ALUOp(ALUOp), .MemWriteEn(memWrite), .RegWriteEn(regWrite), .ALUSrc(ALUSrc), .Jump(jump), .PcSrc(pcSrc));

mux3to1 #(5) RFMux(.in1(rt), .in2(rd), .in3(5'b11111), .s(regDst), .out(writeRegister));

registerFile RF(.clk(clk), .rst(rst), .we(regWrite), 
   .readRegister1(rs), .readRegister2(rt), .writeRegister(writeRegister),
   .writeData(writeData), .readData1(readData1), .readData2(readData2));

signextender SignExtend(.in(imm), .out(extImm));

mux2x1 #(32) ALUMux(.in1(readData2), .in2(extImm), .s(ALUSrc), .out(ALUin2));


ALU alu(.operand1(readData1), .operand2(ALUin2), .shamt(shamt) ,.opSel(ALUOp), .result(ALUResult), .zero(zero));

XNORGate branchXnor(.out(xnorOut), .in1(instruction[26]), .in2(~zero));

ANDGate branchAnd(.in1(xnorOut), .in2(branch), .out(branchMuxSel));

adder #(8) branchAdder(.in1(PCPlus1), .in2(imm[7:0]), .out(branchAdderResult));

mux2x1 #(8) branchMux(.in1(PCPlus1),.in2(branchAdderResult), .s(branchMuxSel), .out(branchMuxOut));

mux2x1 #(8) jumpMux(.in1(readData1[7:0]),.in2(instruction[7:0]), .s(jump), .out(jumpMuxOut)); // flipped

mux2x1 #(8) pcMux(.in1(branchMuxOut), .in2(jumpMuxOut), .s(pcSrc), .out(nextPC));

dataMemory DM(.address(ALUResult[7:0]), .clock(~clk), .data(readData2), .rden(memRead), .wren(memWrite), .q(memoryReadData));

mux3to1 #(32) WBMux(.in1(ALUResult), .in2(memoryReadData), .in3({{24{1'b0}} ,PCPlus1}), .s(memToReg), .out(writeData));


endmodule 