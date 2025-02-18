module ForwardingUnit (rsD, rtD, rsE, rtE, writeRegisterM, writeRegisterW, regWriteM, regWriteW, ForwardA, ForwardB, ForwardAD, ForwardBD);

input [4:0] rsE, rtE,rsD, rtD;       
input [4:0] writeRegisterM, writeRegisterW;
input regWriteM, regWriteW;            
output reg [1:0] ForwardA, ForwardB;
output reg [1:0] ForwardAD, ForwardBD;



always @(*) begin

	ForwardA[0] = 1'b0;
	ForwardB[0] = 1'b0;
	ForwardA[1] = 1'b0;
	ForwardB[1] = 1'b0;
	ForwardAD[0] = 1'b0;
	ForwardBD[0] = 1'b0;
	ForwardAD[1] = 1'b0;
	ForwardBD[1] = 1'b0;
	
if (regWriteM && (writeRegisterM == rsE) && (writeRegisterM != 0))
	ForwardA[0] = 1;
	
if (regWriteM && (writeRegisterM == rtE) && (writeRegisterM != 0))
	ForwardB[0] = 1;

if (regWriteW && (writeRegisterW == rsE) && (writeRegisterM != rsE || regWriteM == 0) && (writeRegisterW != 0))
	ForwardA[1] = 1;
	
if (regWriteW && (writeRegisterW == rtE) && (writeRegisterM != rtE || regWriteM == 0) && (writeRegisterW != 0))
	ForwardB[1] = 1;
	
if ((rsD!=0) & (rsD==writeRegisterM) & (writeRegisterM!=0) & regWriteM)
	ForwardAD[0] = 1;
	
if ((rtD!=0) & (rtD==writeRegisterM) & (writeRegisterM!=0) & regWriteM)
	ForwardBD[0] = 1;

if ((rsD!=0) & (rsD==writeRegisterW) & (writeRegisterW!=0) & regWriteW && (writeRegisterM != rsD || regWriteM == 0))
	ForwardAD[1] = 1;

if ((rtD!=0) & (rtD==writeRegisterW) & (writeRegisterW!=0) & regWriteW && (writeRegisterM != rtD || regWriteM == 0))
	ForwardBD[1] = 1;
end






endmodule
