module processor(clk, rst, PC, enable);

//inputs
input clk, rst, enable;

//outputs
output [10:0] PC;
wire Branch1, MemReadEn1, MemWriteEn1, RegWriteEn1, ALUSrc1, Jump1, PcSrc1, ForwardBranchA, ForwardBranchB;
wire Branch2, MemReadEn2, MemWriteEn2, RegWriteEn2, ALUSrc2, Jump2, PcSrc2;
wire prediction1, prediction2, CPCSignal1, CPCSignal2, branch_taken1, branch_taken2,EnablePCIFID, IFID1Reset, ID21Reset, ID22Reset, EX2Reset, Stall;
wire Stall11, Stall12, Stall21, Stall22, FlushIFID1, FlushEX,FlushMEM2, overflow1, overflow2, zero1, zero2, xnorOut1, xnorOut2;
wire [2:0] ForwardA1, ForwardA2, ForwardB1, ForwardB2;
wire [1:0] MemtoReg1, RegDst1, MemtoReg2, RegDst2;
wire [3:0] ALUOp1, ALUOp2;
wire [4:0] readRegister1, readRegister2, readRegister3, readRegister4;
wire [4:0] writeRegister1, writeRegister2, rd1, rd2, rs1, rs2, rt1, rt2, shamt1, shamt2;
wire [5:0] opCode1, opCode2, funct1, funct2;
wire [15:0] imm1, imm2;
wire [31:0] writeData1, writeData2, readData1, readData2, readData3, readData4;
wire [31:0] extImm1, extImm2, instr1, instr2;
wire [31:0] memoryReadData1, memoryReadData2;
wire [10:0] PCPlus1, PCPlus2,PCPlus1D1,PCPlus1D,PCPlus1E, branchAdderResult1, branchAdderResult2, nextPCP1;
//wire [10:0] jumpMuxOut1, jumpMuxOut2, jumpMuxOut, branchMuxOut1, branchMuxOut2, branchMuxOut, CPC, nextPC, BJPC;
wire [10:0] CorrectedPC1, CorrectedPC2,  branchAdderPC, instMemMuxOut;

wire RegWriteEn1D, RegWriteEn2D, bit26_1, bit26_2;
wire Branch1D, MemReadEn1D, MemWriteEn1D, ALUSrc1D, Jump1D, PcSrc1D;
wire Branch2D, MemReadEn2D, MemWriteEn2D, ALUSrc2D, Jump2D, PcSrc2D;
wire prediction1D1, prediction2D1, prediction1D, prediction2D;
wire [1:0] MemtoReg1D, RegDst1D, MemtoReg2D, RegDst2D;
wire [3:0] ALUOp1D, ALUOp2D;
wire [31:0] instr1D, instr2D;
wire [10:0] PCPlus2D1, PCD1, branchAdderResult1D1, branchAdderResult2D1;
wire [10:0] PCPlus2D, PCD, branchAdderResult1D, branchAdderResult2D;

wire Branch1E, Branch2E, prediction1E, prediction2E, bit26_1E, bit26_2E;
wire MemReadEn1E, MemWriteEn1E, RegWriteEn1E, ALUSrc1E;
wire MemReadEn2E, MemWriteEn2E, RegWriteEn2E, ALUSrc2E;
wire [1:0] MemToReg1E, RegDst1E, MemToReg2E, RegDst2E;
wire [3:0] ALUOp1E, ALUOp2E;
wire [4:0] writeRegister1E, writeRegister2E;
wire [10:0] PCPlus2E,PCE, branchAdderResult1E, branchAdderResult2E;
wire [31:0] extImm1E, extImm2E;
wire [4:0] rs1E, rt1E, rd1E, shamt1E;
wire [4:0] rs2E, rt2E, rd2E, shamt2E;
wire [31:0] readData1E, readData2E, readData3E, readData4E;
wire [31:0] ForwardAMuxOut1, ForwardBMuxOut1, ForwardAMuxOut2, ForwardBMuxOut2;
wire [31:0] ALUin1, ALUin2, ALUResult1, ALUResult2;

wire RegWriteEn1M, RegWriteEn2M, Branch1M, Branch2M, bit26_1M, bit26_2M, prediction1M, prediction2M;
wire MemWriteEn1M, MemReadEn1M, MemWriteEn2M, MemReadEn2M;
wire [1:0] MemToReg1M, MemToReg2M;
wire [10:0] PCPlus2M, PCM, PCPlus1M, branchAdderResult1M, branchAdderResult2M;
wire [31:0] ALUResult1M, ALUResult2M;
wire [31:0] ForwardBMuxOut1M, ForwardBMuxOut2M, ForwardAMuxOut1M, ForwardAMuxOut2M, comp2MuxAout, comp2MuxBout;
wire [4:0] writeRegister1M, writeRegister2M, rs2M, rt2M;

wire RegWriteEn1W, RegWriteEn2W;
wire [1:0] MemToReg1W, MemToReg2W;
wire [10:0] PCPlus2W;
wire [31:0] ALUResult1W, ALUResult2W;
wire [31:0] memoryReadData1W, memoryReadData2W;
wire [4:0] writeRegister1W, writeRegister2W;


assign opCode1 = instr1D[31:26];
assign rd1 = instr1D[15:11]; 
assign rs1 = instr1D[25:21];  
assign shamt1 = instr1D[10:6];
assign rt1 = instr1D[20:16];  
assign imm1 = instr1D[15:0];
assign funct1 = instr1D[5:0];
assign bit26_1 =instr1D[26];

assign opCode2 = instr2D[31:26];
assign rd2 = instr2D[15:11]; 
assign rs2 = instr2D[25:21];  
assign shamt2 = instr2D[10:6];
assign rt2 = instr2D[20:16];  
assign imm2 = instr2D[15:0];
assign funct2 = instr2D[5:0];
assign bit26_2 =instr2D[26];

/**************************************************************FETCH STAGE*********************************************/

reg [10:0] jumpMuxOut1, jumpMuxOut2, jumpMuxOut;
reg [10:0] branchMuxOut1, branchMuxOut2, branchMuxOut;
reg [10:0] BJPC, CPC, nextPC;

// === Jump MUXes ===

// jump1Mux: selects between readData1 and instr1D based on Jump1
always @(*) begin
    if (Jump1)
        jumpMuxOut1 = instr1D[10:0];
    else
        jumpMuxOut1 = readData1[10:0];
end

// jump2Mux: selects between readData3 and instr2D based on Jump2
always @(*) begin
    if (Jump2)
        jumpMuxOut2 = instr2D[10:0];
    else
        jumpMuxOut2 = readData3[10:0];
end

// jumpMux: selects between jumpMuxOut1 and jumpMuxOut2 based on Jump2
always @(*) begin
    if (Jump2)
        jumpMuxOut = jumpMuxOut2;
    else
        jumpMuxOut = jumpMuxOut1;
end

// === Branch MUXes ===

// branch1Mux: selects between PCPlus2 and branchAdderPC based on prediction1
always @(*) begin
    if (prediction1)
        branchMuxOut1 = branchAdderPC;
    else
        branchMuxOut1 = PCPlus2;
end

// branch2Mux: selects between PCPlus2 and branchAdderResult2 based on prediction2
always @(*) begin
    if (prediction2)
        branchMuxOut2 = branchAdderResult2;
    else
        branchMuxOut2 = PCPlus2;
end

// branchMux: selects between branchMuxOut1 and branchMuxOut2 based on prediction2
always @(*) begin
    if (prediction2)
        branchMuxOut = branchMuxOut2;
    else
        branchMuxOut = branchMuxOut1;
end

// pcMux: selects between branchMuxOut and jumpMuxOut based on (PcSrc1 | PcSrc2)
always @(*) begin
    if (PcSrc1 | PcSrc2)
        BJPC = jumpMuxOut;
    else
        BJPC = branchMuxOut;
end

// cpcMux: selects between CorrectedPC1 and CorrectedPC2 based on CPCSignal2
always @(*) begin
    if (CPCSignal2)
        CPC = CorrectedPC2;
    else
        CPC = CorrectedPC1;
end

// nexPcMux: selects between BJPC and CPC based on (CPCSignal1 | CPCSignal2)
always @(*) begin
    if (CPCSignal1 | CPCSignal2)
        nextPC = CPC;
    else
        nextPC = BJPC;
end

//mux2x1 #(11) jump1Mux(.in1(readData1[10:0]), .in2(instr1D[10:0]), .s(Jump1), .out(jumpMuxOut1));
//mux2x1 #(11) jump2Mux(.in1(readData3[10:0]), .in2(instr2D[10:0]), .s(Jump2), .out(jumpMuxOut2));
//mux2x1 #(11) jumpMux(.in1(jumpMuxOut1), .in2(jumpMuxOut2), .s(PcSrc2), .out(jumpMuxOut));
//
//mux2x1 #(11) branch1Mux(.in1(PCPlus2), .in2(branchAdderPC), .s(prediction1), .out(branchMuxOut1));
//mux2x1 #(11) branch2Mux(.in1(PCPlus2), .in2(branchAdderResult2), .s(prediction2), .out(branchMuxOut2));
//mux2x1 #(11) branchMux(.in1(branchMuxOut1), .in2(branchMuxOut2), .s(prediction2), .out(branchMuxOut));
//
//mux2x1 #(11) pcMux(.in1(branchMuxOut), .in2(jumpMuxOut), .s(PcSrc1|PcSrc2), .out(BJPC));
//
//mux2x1 #(11) cpcMux(.in1(CorrectedPC1), .in2(CorrectedPC2), .s(CPCSignal2), .out(CPC)); 
//
//mux2x1 #(11) nexPcMux(.in1(BJPC), .in2(CPC), .s(CPCSignal1|CPCSignal2), .out(nextPC));

programCounter pc(.clk(clk), .rst(rst), .enable(EnablePCIFID), .PCin(nextPC), .PCout(PC));
//
//AdderIP nextPcAdder(.data0x(nextPC),.data1x(11'd1),.result(nextPCP1));
//AdderIP branchPcAdder(.data0x(branchAdderResult1),.data1x(11'd1),.result(branchAdderPC));
//AdderIP pcAdder(.data0x(PC),.data1x(11'd2),.result(PCPlus2));
//AdderIP pcAdder2(.data0x(PC),.data1x(11'd1),.result(PCPlus1)); 

adder #(11)	branchPcAdder(.in1(branchAdderResult1),.in2(11'd1),.out(branchAdderPC));
adder #(11) nextPcAdder(.in1(nextPC), .in2(11'd1), .out(nextPCP1));
adder #(11) pcAdder(.in1(PC), .in2(11'd2), .out(PCPlus2));
adder #(11) pcAdder2(.in1(PC), .in2(11'd1), .out(PCPlus1));

mux2x1En #(11) instMemMux(.in1(nextPCP1), .in2(branchAdderResult1), .s(prediction1),.en(EnablePCIFID), .out(instMemMuxOut));

dual_issue_inst_mem instMem(
								.address_a(nextPC),
								.address_b(instMemMuxOut),
								.clock(clk),
								.q_a(instr1),
								.q_b(instr2));


AdderIP branchAdder1(.data0x(PCPlus1),.data1x(instr1[10:0]),.result(branchAdderResult1));
AdderIP branchAdder2(.data0x(PCPlus2),.data1x(instr2[10:0]),.result(branchAdderResult2));
//adder #(11) branchAdder1(.in1(PCPlus1), .in2(instr1[10:0]), .out(branchAdderResult1));
//adder #(11) branchAdder2(.in1(PCPlus2), .in2(instr2[10:0]), .out(branchAdderResult2));

BranchPredictionUnit BPU (
    .clk(clk), .reset(rst), .branch1(Branch1M), .branch2(Branch2M), .branch_taken1(branch_taken1), .branch_taken2(branch_taken2), 
    .pc1(PC), .pc2(PCPlus1), .pcE1(PCM), .pcE2(PCPlus1M), .prediction1(prediction1), .prediction2(prediction2)
);

pipe #(121) IF_ID(.D({PCPlus2,PCPlus1,PC,branchAdderResult1,branchAdderResult2, prediction1, prediction2, instr1, instr2}), 
		.Q({PCPlus2D,PCPlus1D,PCD,branchAdderResult1D,branchAdderResult2D, prediction1D, prediction2D, instr1D, instr2D}), 
		.clk(clk), .reset(rst), .enable(EnablePCIFID), .flush(FlushIFID1)); 




/**************************************************************DECODE STAGE*********************************************/

// registerFile (clk, rst, we1, we2, readRegister1, readRegister2, readRegister3, readRegister4,writeRegister1,writeRegister2,writeData1, writeData2, readData1, readData2, readData3, readData4);


controlUnit CU1(.opCode(opCode1), .funct(funct1),
				    .Branch(Branch1), .MemReadEn(MemReadEn1), .MemWriteEn(MemWriteEn1), .RegWriteEn(RegWriteEn1),
					 .ALUSrc(ALUSrc1), .Jump(Jump1), .PcSrc(PcSrc1),  .MemtoReg(MemtoReg1), .RegDst(RegDst1), .ALUOp(ALUOp1));
				   
controlUnit CU2 (
					  .opCode(opCode2), .funct(funct2),
					  .Branch(Branch2), .MemReadEn(MemReadEn2), .MemWriteEn(MemWriteEn2), .RegWriteEn(RegWriteEn2), 
					  .ALUSrc(ALUSrc2), .Jump(Jump2), .PcSrc(PcSrc2), .MemtoReg(MemtoReg2), .RegDst(RegDst2), .ALUOp(ALUOp2));


registerFile RegFile (
     .clk(clk), .rst(rst), .we1(RegWriteEn1W), .we2(RegWriteEn2W), .readRegister1(rs1), .readRegister2(rt1), .readRegister3(rs2)
	  , .readRegister4(rt2),.writeRegister1(writeRegister1W), .writeRegister2(writeRegister2W), 
	  .writeData1(writeData1), .writeData2(writeData2), .readData1(readData1), .readData2(readData2), .readData3(readData3), .readData4(readData4));


signextender SignExtend1(.in(imm1), .out(extImm1));
signextender SignExtend2(.in(imm2), .out(extImm2));


HazardDetectionUnit HDU(
    .takenBranch1(branch_taken1), .takenBranch2(branch_taken2), .pcSrc1(PcSrc1), .pcSrc2(PcSrc2), .memReadE1(MemReadEn1), .memReadE2(MemReadEn2), 
    .branch1(Branch1M), .branch2(Branch2M), .predictionE1(prediction1M), .predictionE2(prediction2M), .writeRegisterE1(writeRegister1E), .writeRegisterE2(writeRegister2E), 
    .rsD1(rs1), .rtD1(rt1), .rsD2(rs2), .rtD2(rt2), .Stall11(Stall11), .Stall21(Stall21), .Stall12(Stall12), .Stall22(Stall22), 
	 .FlushIFID1(FlushIFID1), .FlushEX(FlushEX),.FlushMEM2(FlushMEM2), .CPCSignal1(CPCSignal1), .CPCSignal2(CPCSignal2)
);

//ORGate IFID1Flush(.in1(FlushIFID1), .in2(~rst), .out(IFID1Reset));
//ORGate ID21Flush(.in1(FlushID21), .in2(~rst), .out(ID21Reset));
//ORGate ID22Flush(.in1(FlushID22), .in2(~rst), .out(ID22Reset));
//ORGate EX2Flush(.in1(FlushEX2), .in2(~rst), .out(EX2Reset));


ORGate4 hold(.in1(Stall21), .in2(Stall22), .in3(Stall12), .in4(Stall11), .out(Stall));
ANDGate holdGate2(.in1(~Stall), .in2(enable), .out(EnablePCIFID));

pipe #(142) ID_EX_1(
    .D({
        branchAdderResult1D, bit26_1, Branch1, MemReadEn1, MemWriteEn1, MemtoReg1, RegWriteEn1, ALUOp1,
		  RegDst1, ALUSrc1, readData1, readData2,extImm1, rs1, rt1, rd1, shamt1, prediction1D
    }),
    .Q({
        branchAdderResult1E, bit26_1E, Branch1E, MemReadEn1E, MemWriteEn1E, MemToReg1E, RegWriteEn1E, ALUOp1E, 
		  RegDst1E, ALUSrc1E, readData1E, readData2E, extImm1E, rs1E, rt1E, rd1E, shamt1E, prediction1E 
    }), 
    .clk(clk), .reset(rst), .enable(enable), .flush(FlushEX)
);

pipe #(175) ID_EX_2(
    .D({
        PCD,PCPlus1D, PCPlus2D, branchAdderResult2D, bit26_2, Branch2, MemReadEn2, MemWriteEn2, MemtoReg2, RegWriteEn2, ALUOp2,
		  RegDst2, ALUSrc2, readData3, readData4, extImm2, rs2, rt2, rd2, shamt2, prediction2D
    }),
    .Q({
        PCE,PCPlus1E, PCPlus2E, branchAdderResult2E,bit26_2E, Branch2E, MemReadEn2E, MemWriteEn2E, MemToReg2E, RegWriteEn2E, ALUOp2E,
		  RegDst2E, ALUSrc2E, readData3E, readData4E, extImm2E, rs2E, rt2E, rd2E, shamt2E, prediction2E
    }), 
    .clk(clk), .reset(rst), .enable(enable), .flush(FlushEX)
);


/**************************************************************EXECUT(E & ING MYSELF RN) STAGE*********************************************/

mux3to1 #(5) RFMux1(.in1(rt1E), .in2(rd1E), .in3(5'b11111), .s(RegDst1E), .out(writeRegister1E));
mux3to1 #(5) RFMux2(.in1(rt2E), .in2(rd2E), .in3(5'b11111), .s(RegDst2E), .out(writeRegister2E));

ForwardingUnit FU (.rsE1(rs1E), .rtE1(rt1E), .rsE2(rs2E), .rtE2(rt2E), .rsM2(rs2M), .rtM2(rt2M), .writeRegisterM1(writeRegister1M),
						 .writeRegisterM2(writeRegister2M), .writeRegisterW1(writeRegister1W), .writeRegisterW2(writeRegister2W), 
						.regWriteM1(RegWriteEn1M), .regWriteM2(RegWriteEn2M), .regWriteW1(RegWriteEn1W), .regWriteW2(RegWriteEn2W), .branch2M(Branch2M),
						.ForwardA1(ForwardA1), .ForwardB1(ForwardB1), .ForwardA2(ForwardA2), .ForwardB2(ForwardB2), .ForwardBranchA(ForwardBranchA), .ForwardBranchB(ForwardBranchB));


mux5to1 #(32) ForwardA1Mux(.in1(readData1E), .in2(ALUResult1M),.in3(ALUResult2M), .in4(writeData1),.in5(writeData2), .s(ForwardA1), .out(ForwardAMuxOut1));
mux5to1 #(32) ForwardB1Mux(.in1(readData2E), .in2(ALUResult1M),.in3(ALUResult2M), .in4(writeData1),.in5(writeData2), .s(ForwardB1), .out(ForwardBMuxOut1));
mux2x1 #(32) ALUMux1(.in1(ForwardBMuxOut1), .in2(extImm1E), .s(ALUSrc1E), .out(ALUin1));
ALU alu1(.operand1(ForwardAMuxOut1), .operand2(ALUin1), .shamt(shamt1E) ,.opSel(ALUOp1E), .result(ALUResult1), .overflow(overflow1));

mux5to1 #(32) ForwardA2Mux(.in1(readData3E),  .in2(ALUResult1M),.in3(ALUResult2M), .in4(writeData1),.in5(writeData2), .s(ForwardA2), .out(ForwardAMuxOut2));
mux5to1 #(32) ForwardB2Mux(.in1(readData4E),  .in2(ALUResult1M),.in3(ALUResult2M), .in4(writeData1),.in5(writeData2), .s(ForwardB2), .out(ForwardBMuxOut2));
mux2x1 #(32) ALUMux2(.in1(ForwardBMuxOut2), .in2(extImm2E), .s(ALUSrc2E), .out(ALUin2));
ALU alu2(.operand1(ForwardAMuxOut2), .operand2(ALUin2), .shamt(shamt2E) ,.opSel(ALUOp2E), .result(ALUResult2), .overflow(overflow2));

  




pipe #(120) EX_MEM(
    .D({branchAdderResult1E, RegWriteEn1E, MemToReg1E, MemWriteEn1E, MemReadEn1E, 
        Branch1E, bit26_1E, prediction1E, ALUResult1, ForwardBMuxOut1,ForwardAMuxOut1, writeRegister1E
    }),
    .Q({branchAdderResult1M, RegWriteEn1M, MemToReg1M, MemWriteEn1M, MemReadEn1M, 
		  Branch1M, bit26_1M, prediction1M, ALUResult1M, ForwardBMuxOut1M, ForwardAMuxOut1M, writeRegister1M
    }), 
    .clk(clk), 
    .reset(rst), 
    .enable(enable),
	 .flush(FlushEX)
);

pipe #(163) EX_MEM_2(
	 .D({PCE, PCPlus1E, branchAdderResult2E, RegWriteEn2E, MemToReg2E, MemWriteEn2E, MemReadEn2E, rs2E, rt2E,
        PCPlus2E, Branch2E, bit26_2E,prediction2E, ALUResult2, ForwardBMuxOut2, ForwardAMuxOut2, writeRegister2E
    }),
    .Q({PCM, PCPlus1M, branchAdderResult2M, RegWriteEn2M, MemToReg2M, MemWriteEn2M, MemReadEn2M, rs2M, rt2M,
        PCPlus2M, Branch2M, bit26_2M, prediction2M, ALUResult2M, ForwardBMuxOut2M, ForwardAMuxOut2M, writeRegister2M
    }), 
    .clk(clk), 
    .reset(rst), 
    .enable(enable),
	 .flush(FlushEX));


/* ********************************************** MEMORY STAGE ******************************************* */

ComparatorIP comp1(.dataa(ForwardBMuxOut1M),.datab(ForwardAMuxOut1M),.aeb(zero1));
XNORGate branchXnor1(.out(xnorOut1), .in1(bit26_1M), .in2(~zero1));
ANDGate branchAnd1(.in1(xnorOut1), .in2(Branch1M), .out(branch_taken1));

mux2x1 #(32) comp2MuxA (.in1(ForwardAMuxOut2M), .in2(ALUResult1M), .s(ForwardBranchA), .out(comp2MuxAout));
mux2x1 #(32) comp2MuxB (.in1(ForwardBMuxOut2M), .in2(ALUResult1M), .s(ForwardBranchB), .out(comp2MuxBout));
ComparatorIP comp2(.dataa(comp2MuxAout),.datab(comp2MuxBout),.aeb(zero2));
XNORGate branchXnor2(.out(xnorOut2), .in1(bit26_2M), .in2(~zero2));
ANDGate branchAnd2(.in1(xnorOut2), .in2(Branch2M), .out(branch_taken2)); 

pcCorrection PCC (
    .PredictionE1(prediction1M), .PredictionE2(prediction2M), .branch_taken1(branch_taken1), .branch_taken2(branch_taken2), 
    .PCE1(PCPlus1M), .PCE2(PCPlus2M), .branchAdderResultE1(branchAdderResult1M), .branchAdderResultE2(branchAdderResult2M), 
    .CorrectedPC1(CorrectedPC1), .CorrectedPC2(CorrectedPC2)
);
dual_issue_data_memory DM (
	.address_a(ALUResult1M[10:0]),
	.address_b(ALUResult2M[10:0]),
	.clock(~clk),
	.data_a(ForwardBMuxOut1M),
	.data_b(ForwardBMuxOut2M),
	.rden_a(MemReadEn1M),
	.rden_b(MemReadEn2M & ~MemReadEn1M),
	.wren_a(MemWriteEn1M),
	.wren_b(MemWriteEn2M & ~MemWriteEn1M),
	.q_a(memoryReadData1),
	.q_b(memoryReadData2)
	);

//pipe #(80) MEM_WB(.D({regWriteM, memToRegM, PCPlus1M, ALUResultM, memoryReadData, writeRegisterM}), 
//.Q({regWriteW, memToRegW, PCPlus1W, ALUResultW, memoryReadDataW, writeRegisterW}), .clk(clk), .reset(rst), .enable(enable));

pipe #(155) MEM_WB( 
    .D({
		  RegWriteEn1M, MemToReg1M,
        ALUResult1M,
        memoryReadData1,
        writeRegister1M,
        RegWriteEn2M, MemToReg2M,
        PCPlus2M,
        ALUResult2M,
        memoryReadData2,
        writeRegister2M
    }),
    .Q({
        RegWriteEn1W, MemToReg1W, 
        ALUResult1W,
        memoryReadData1W, 
        writeRegister1W,
        RegWriteEn2W, MemToReg2W, 
        PCPlus2W, 
        ALUResult2W, 
        memoryReadData2W, 
        writeRegister2W
    }), 
    .clk(clk), 
    .reset(rst),      
    .enable(enable),
	 .flush(1'b0)
);



/* ********************************************** WRITE BACK STAGE ******************************************* */

// Write-back for Instruction 1
assign writeData1 = (MemToReg1W == 2'b00) ? ALUResult1W : 
                    (MemToReg1W == 2'b01) ? memoryReadData1W : 
                    (MemToReg1W == 2'b10) ? {{21{1'b0}}, PCPlus2W-11'd1} : 
                    32'b0;

// Write-back for Instruction 2
assign writeData2 = (MemToReg2W == 2'b00) ? ALUResult2W : 
                    (MemToReg2W == 2'b01) ? memoryReadData2W : 
                    (MemToReg2W == 2'b10) ? {{21{1'b0}}, PCPlus2W-11'd1} : 
                    32'b0;



endmodule 