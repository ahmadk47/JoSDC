module BranchPredictionUnit (
    clk, reset, branch1, branch2, branch_taken1, branch_taken2, 
    pc1, pc2, pcE1, pcE2, prediction1, prediction2
);

    // Inputs
    input clk, reset;
    input branch1, branch2;            // Branch signals for the two instructions
    input branch_taken1, branch_taken2; // Whether the branches were taken or not
    input [7:0] pc1, pc2;              // Program counters for the two instructions
    input [7:0] pcE1, pcE2;            // Program counters for the two instructions in Execute stage

    // Outputs
    output reg prediction1, prediction2; // Predictions for the two branches

    // Branch History Table (BHT)
    reg [1:0] BHT [0:63]; // 64-entry BHT, 2-bit saturating counters

    // Indexes for accessing the BHT
    wire [5:0] index1 = pc1[5:0];  // Index for the first branch
    wire [5:0] index2 = pc2[5:0];  // Index for the second branch
    wire [5:0] indexE1 = pcE1[5:0]; // Index for the first branch in Execute stage
    wire [5:0] indexE2 = pcE2[5:0]; // Index for the second branch in Execute stage

    // Prediction logic for the first branch
    always @(*) begin
        case (BHT[index1])
            2'b11: prediction1 = 1'b1; // Strongly Taken
            2'b10: prediction1 = 1'b1; // Weakly Taken
            2'b01: prediction1 = 1'b0; // Weakly Not Taken
            2'b00: prediction1 = 1'b0; // Strongly Not Taken
            default: prediction1 = 1'b0;
        endcase
    end

    // Prediction logic for the second branch
    always @(*) begin
        case (BHT[index2])
            2'b11: prediction2 = 1'b1; // Strongly Taken
            2'b10: prediction2 = 1'b1; // Weakly Taken
            2'b01: prediction2 = 1'b0; // Weakly Not Taken
            2'b00: prediction2 = 1'b0; // Strongly Not Taken
            default: prediction2 = 1'b0;
        endcase
    end

    // Update logic for the BHT
    always @(posedge clk or negedge reset) begin
        integer i;
        if (~reset) begin
            // Initialize BHT to Weakly Not Taken (2'b01)
            for (i = 0; i < 64; i = i + 1) begin
                BHT[i] <= 2'b01;
            end
        end else begin
            // Update BHT for the first branch
            if (branch1) begin
                case (BHT[indexE1])
                    2'b11: BHT[indexE1] <= branch_taken1 ? 2'b11 : 2'b10;
                    2'b10: BHT[indexE1] <= branch_taken1 ? 2'b11 : 2'b01;
                    2'b01: BHT[indexE1] <= branch_taken1 ? 2'b10 : 2'b00;
                    2'b00: BHT[indexE1] <= branch_taken1 ? 2'b01 : 2'b00;
                endcase
            end

            // Update BHT for the second branch
            if (branch2) begin
                case (BHT[indexE2])
                    2'b11: BHT[indexE2] <= branch_taken2 ? 2'b11 : 2'b10;
                    2'b10: BHT[indexE2] <= branch_taken2 ? 2'b11 : 2'b01;
                    2'b01: BHT[indexE2] <= branch_taken2 ? 2'b10 : 2'b00;
                    2'b00: BHT[indexE2] <= branch_taken2 ? 2'b01 : 2'b00;
                endcase
            end
        end
    end

endmodule