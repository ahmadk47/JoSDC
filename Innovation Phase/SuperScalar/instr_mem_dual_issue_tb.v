module instr_mem_dual_issue_tb;

    reg [7:0] addr;              // Declare addr as a reg
    wire [31:0] instr1, instr2;  // Outputs from the memory module

    // Instantiate the dual-issue instruction memory
    dual_issue_instr_mem imem (
        .addr(addr),
        .instr1(instr1),
        .instr2(instr2)
    );

    // Test procedure
    initial begin
        // Test fetching instructions
        addr = 8'b0; // Initialize addr to 0
        #10;
        $display("addr = %h, instr1 = %b, instr2 = %b", addr, instr1, instr2);

        addr = 8'd2; // Set addr to 2
        #10;
        $display("addr = %h, instr1 = %b, instr2 = %b", addr, instr1, instr2);

        addr = 8'd4; // Set addr to 4
        #10;
        $display("addr = %h, instr1 = %b, instr2 = %b", addr, instr1, instr2);
		  
		   addr = 8'd6; // Set addr to 4
        #10;
        $display("addr = %h, instr1 = %b, instr2 = %b", addr, instr1, instr2);
		  
		   addr = 8'd8; // Set addr to 4
        #10;
        $display("addr = %h, instr1 = %b, instr2 = %b", addr, instr1, instr2);

        $finish; // End simulation
    end

endmodule