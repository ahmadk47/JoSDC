module HazardDetectionUnit (Stall, Flush, takenBranch, pcSrc, writeRegisterE, rsD, rtD, memReadE, branch, prediction);
 input takenBranch, pcSrc, memReadE, branch, prediction;
 input [4:0] writeRegisterE, rsD, rtD;
 output reg Stall, Flush;
	
	
	always @(*) begin
	
		Flush = ((takenBranch ^ prediction) & branch)| pcSrc;
		
	end
	
	always @(*) begin
		Stall = 0;
		if (memReadE && (writeRegisterE == rsD || writeRegisterE == rtD) && (writeRegisterE != 5'b0)) begin
			Stall = 1;
		end
		
	end

endmodule 