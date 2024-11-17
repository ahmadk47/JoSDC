module HazardDetectionUnit (Stall, Flush, takenBranch, pcSrc, writeRegisterE, rsD, rtD, memReadE);
 input takenBranch, pcSrc, memReadE;
 input [4:0] writeRegisterE, rsD, rtD;
 output reg Stall, Flush;
	
	
	always @(*) begin
	
		Flush = takenBranch | pcSrc;
		
	end
	
	always @(*) begin
		Stall = 0;
		if (memReadE && (writeRegisterE == rsD || writeRegisterE == rtD) && (writeRegisterE != 5'b0)) begin
			Stall = 1;
		end
		
	end

endmodule 