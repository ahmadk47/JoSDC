module ForwardingUnit (rsE1, rtE1, rsE2, rtE2, writeRegisterM1, writeRegisterM2, writeRegisterW1, writeRegisterW2, regWriteM1, regWriteM2, regWriteW1, regWriteW2, ForwardA1, ForwardB1, ForwardA2, ForwardB2);

    // Inputs
    input [4:0] rsE1, rtE1;            // Source registers for the first instruction in Execute stage
    input [4:0] rsE2, rtE2;            // Source registers for the second instruction in Execute stage
    input [4:0] writeRegisterM1;       // Destination register for the first instruction in Memory stage
    input [4:0] writeRegisterM2;       // Destination register for the second instruction in Memory stage
    input [4:0] writeRegisterW1;       // Destination register for the first instruction in Writeback stage
    input [4:0] writeRegisterW2;       // Destination register for the second instruction in Writeback stage
    input regWriteM1, regWriteM2;      // Register write signals for Memory stage (for both instructions)
    input regWriteW1, regWriteW2;      // Register write signals for Writeback stage (for both instructions)

    // Outputs
    output reg [1:0] ForwardA1, ForwardB1; // Forwarding signals for the first instruction
    output reg [1:0] ForwardA2, ForwardB2; // Forwarding signals for the second instruction

    always @(*) begin
        // Initialize forwarding signals to 0
        ForwardA1 = 2'b00;
        ForwardB1 = 2'b00;
        ForwardA2 = 2'b00;
        ForwardB2 = 2'b00;

        // Forwarding logic for the first instruction (Instruction 1)
        if (regWriteM1 && (writeRegisterM1 == rsE1) && (writeRegisterM1 != 0))
            ForwardA1 = 2'b01; // Forward from Memory stage (Instruction 1)
        else if (regWriteW1 && (writeRegisterW1 == rsE1) && (writeRegisterM1 != rsE1 || !regWriteM1) && (writeRegisterW1 != 0))
            ForwardA1 = 2'b10; // Forward from Writeback stage (Instruction 1)

        if (regWriteM1 && (writeRegisterM1 == rtE1) && (writeRegisterM1 != 0))
            ForwardB1 = 2'b01; // Forward from Memory stage (Instruction 1)
        else if (regWriteW1 && (writeRegisterW1 == rtE1) && (writeRegisterM1 != rtE1 || !regWriteM1) && (writeRegisterW1 != 0))
            ForwardB1 = 2'b10; // Forward from Writeback stage (Instruction 1)

        // Forwarding logic for the second instruction (Instruction 2)
        if (regWriteM2 && (writeRegisterM2 == rsE2) && (writeRegisterM2 != 0))
            ForwardA2 = 2'b01; // Forward from Memory stage (Instruction 2)
        else if (regWriteW2 && (writeRegisterW2 == rsE2) && (writeRegisterM2 != rsE2 || !regWriteM2) && (writeRegisterW2 != 0))
            ForwardA2 = 2'b10; // Forward from Writeback stage (Instruction 2)

        if (regWriteM2 && (writeRegisterM2 == rtE2) && (writeRegisterM2 != 0))
            ForwardB2 = 2'b01; // Forward from Memory stage (Instruction 2)
        else if (regWriteW2 && (writeRegisterW2 == rtE2) && (writeRegisterM2 != rtE2 || !regWriteM2) && (writeRegisterW2 != 0))
            ForwardB2 = 2'b10; // Forward from Writeback stage (Instruction 2)
    end

endmodule