module HazardDetectionUnit (Stall, Flush, CPCSignal, takenBranch, pcSrc, writeRegisterE, writeRegisterM, rsD, rtD, memReadE, branch, prediction,predictionD, regWriteE, memToRegM);
 input takenBranch, pcSrc, memReadE, branch, prediction, regWriteE,predictionD;
 input [1:0] memToRegM;
 input [4:0] writeRegisterE, writeRegisterM, rsD, rtD;
 output reg Stall, Flush,CPCSignal;
	
	
	always @(*) begin
	
		Flush =(takenBranch ^ predictionD) & branch|pcSrc ;
		
	end
	
	always @(*) begin
		Stall = 0;
		if (memReadE && (writeRegisterE == rsD || writeRegisterE == rtD) && (writeRegisterE != 5'b0)) begin
			Stall = 1;
		end
		else if (branch && (regWriteE && (writeRegisterE ==rsD || writeRegisterE ==rtD )) || (memToRegM && (writeRegisterM ==rsD || writeRegisterM ==rtD))) begin
			Stall = 1;
		end
	end
	
	always @(*) begin 
		CPCSignal = (takenBranch ^ predictionD) & branch;
		
	end

endmodule 