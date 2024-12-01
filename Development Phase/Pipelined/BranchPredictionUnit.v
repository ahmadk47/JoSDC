module BranchPredictionUnit(branch_taken,clk, reset, branch, pc, prediction);

	 input clk, reset, branch, branch_taken;
	 input [7:0] pc;
    output reg prediction;
	 
   
    reg [1:0] BHT [0:255];
    wire [7:0] index = pc[7:0];
    always @(*) begin
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