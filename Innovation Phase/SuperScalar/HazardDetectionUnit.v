module HazardDetectionUnit (
    takenBranch1, takenBranch2, pcSrc1, pcSrc2, memReadE1, memReadE2, 
    branch1, branch2, predictionE1, predictionE2, writeRegisterE1, writeRegisterE2, 
    rsD1, rtD1, rsD2, rtD2, Stall1, Stall2, Flush1, Flush2, CPCSignal1, CPCSignal2
);

    // Inputs
    input takenBranch1, takenBranch2; // Branch outcomes for the two instructions
    input pcSrc1, pcSrc2;             // PC source signals for the two instructions
    input memReadE1, memReadE2;       // Memory read signals for the two instructions in Execute stage
    input branch1, branch2;           // Branch signals for the two instructions
    input predictionE1, predictionE2; // Predictions for the two branches
    input [4:0] writeRegisterE1, writeRegisterE2; // Destination registers for the two instructions in Execute stage
    input [4:0] rsD1, rtD1, rsD2, rtD2; // Source registers for the two instructions in Decode stage

    // Outputs
    output reg Stall1, Stall2;        // Stall signals for the two instructions
    output reg Flush1, Flush2;        // Flush signals for the two instructions
    output reg CPCSignal1, CPCSignal2; // Correct PC signals for the two branches

    // Flush logic for the two instructions
    always @(*) begin
        Flush1 = (takenBranch1 ^ predictionE1) & branch1 | pcSrc1;
        Flush2 = (takenBranch2 ^ predictionE2) & branch2 | pcSrc2;
    end

    // Stall logic for the two instructions
    always @(*) begin
        Stall1 = 0;
        Stall2 = 0;

        // Stall logic for the first instruction
        if (memReadE1 && ((writeRegisterE1 == rsD1 || writeRegisterE1 == rtD1) && (writeRegisterE1 != 5'b0))) begin
            Stall1 = 1;
        end

        // Stall logic for the second instruction
        if (memReadE2 && ((writeRegisterE2 == rsD2 || writeRegisterE2 == rtD2) && (writeRegisterE2 != 5'b0))) begin
            Stall2 = 1;
        end
    end

    // Correct PC signal logic for the two branches
    always @(*) begin
        CPCSignal1 = (takenBranch1 ^ predictionE1) & branch1;
        CPCSignal2 = (takenBranch2 ^ predictionE2) & branch2 &~CPCSignal1;
    end

endmodule