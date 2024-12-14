module BranchPredictionUnit(branch_taken,clk, reset, branch, pc,pcD, prediction,predictionD, branchAdderResult, CorrectedPC, Stall);

	 input clk, reset, branch, branch_taken, Stall,predictionD;
	 input [7:0] pc, branchAdderResult,pcD;
    output reg prediction;
	 output reg [7:0] CorrectedPC;
	 
   
    reg [1:0] BHT [0:63];
    wire [5:0] index = pc[5:0];
	 wire [5:0] index2 = pcD[5:0];
    always @(*) begin
		if (~Stall) begin
        case (BHT[index])
            2'b11: 
					prediction = 1'b1; 	// Strongly Taken
				2'b10: 
					prediction = 1'b1; 	// Weakly Taken
            2'b01:
					prediction = 1'b0;	// Weakly Not Taken
				2'b00:
					prediction = 1'b0; 	// Strongly Not Taken
            default: prediction = 1'b0;
        endcase
		   if (branch_taken & !predictionD) begin
				CorrectedPC <= branchAdderResult;
			end
			else if (!branch_taken & predictionD) begin
				CorrectedPC <= pcD+6'b1;
			end
			else if (branch_taken & predictionD)begin 
				CorrectedPC <= branchAdderResult; 
			end
			else
				CorrectedPC <= pcD+6'b1;
		end
		else begin
			prediction = 0;
			CorrectedPC=0;
			end
    end

    always @(posedge clk ,negedge reset) begin : always_block
		  integer i;
        if (~reset) begin : if_block
            for (i = 0; i < 63; i = i + 1) begin
                BHT[i] <= 2'b01;
            end
        end 
		  else if (branch) begin
            case (BHT[index2])
                2'b11: 
						begin
							BHT[index2] <= branch_taken ? 2'b11 : 2'b10;
						end
                2'b10: 
						begin
							BHT[index2] <= branch_taken ? 2'b11 : 2'b01;
						end
                2'b01:
						begin
							BHT[index2] <= branch_taken ? 2'b10 : 2'b00;
						end
					 2'b00:
						begin
							BHT[index2] <= branch_taken ? 2'b01 : 2'b00;
						end
				endcase
        end
		  
		 
    end

endmodule 