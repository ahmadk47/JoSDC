module BranchPredictionUnit(branch_taken,clk, reset, branch, pc, prediction, branchAdderResult, pcPlus1, CorrectedPC, Stall);

	 input clk, reset, branch, branch_taken, Stall;
	 input [7:0] pc, branchAdderResult, pcPlus1;
    output reg prediction;
	 output reg [7:0] CorrectedPC;
	 
   
    reg [1:0] BHT [0:255];
    wire [5:0] index = pc[5:0];
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
		end
		else begin
			prediction = 0;
			end
		  
		   if (branch_taken & !prediction) begin
				CorrectedPC <= branchAdderResult;
			end
			else if (!branch_taken & prediction) begin
				CorrectedPC <= pcPlus1;
			end
			else begin 
				CorrectedPC <= 0;
			end
    end

    always @(posedge clk ,negedge reset) begin : always_block
		  integer i;
        if (~reset) begin : if_block
            for (i = 0; i < 256; i = i + 1) begin
                BHT[i] <= 2'b00;
            end
        end 
		  else if (branch) begin
            case (BHT[index])
                2'b11: 
						begin
							BHT[index] <= branch_taken ? 2'b11 : 2'b10;
						end
                2'b10: 
						begin
							BHT[index] <= branch_taken ? 2'b11 : 2'b01;
						end
                2'b01:
						begin
							BHT[index] <= branch_taken ? 2'b10 : 2'b00;
						end
					 2'b00:
						begin
							BHT[index] <= branch_taken ? 2'b01 : 2'b00;
						end
				endcase
        end
		  
		 
    end

endmodule 