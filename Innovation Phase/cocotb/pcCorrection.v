module pcCorrection (
    PredictionE1, PredictionE2, branch_taken1, branch_taken2, 
    PCE1, PCE2, branchAdderResultE1, branchAdderResultE2, 
    CorrectedPC1, CorrectedPC2
);

    // Inputs
    input PredictionE1, PredictionE2; // Predictions for the two branches
    input branch_taken1, branch_taken2; // Whether the branches were taken or not
    input [10:0] PCE1, PCE2; // Program counters for the two instructions in Execute stage
    input [10:0] branchAdderResultE1, branchAdderResultE2; // Branch target addresses for the two instructions

    // Outputs
    output reg [10:0] CorrectedPC1, CorrectedPC2; // Corrected PCs for the two branches

    // Correction logic for the first branch
    always @(*) begin
        CorrectedPC1 = PCE1; // Default to the current PC
        if (branch_taken1 & !PredictionE1) begin
            CorrectedPC1 = branchAdderResultE1; // Correct PC if branch was mispredicted
        end
    end

    // Correction logic for the second branch
    always @(*) begin
        CorrectedPC2 = PCE2; // Default to the current PC
        if (branch_taken2 & !PredictionE2) begin
            CorrectedPC2 = branchAdderResultE2; // Correct PC if branch was mispredicted
        end
    end

endmodule 