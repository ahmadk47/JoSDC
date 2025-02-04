module dual_issue_instr_mem (addr,instr1,instr2, clk, rst);
	
	 input clk, rst;
	 input   [7:0] addr;         // 8-bit address (word-addressed)
    output reg  [31:0] instr1,instr2;

    reg [31:0] mem [0:255];  // 256 x 32-bit instruction memory

    // Initialize memory from a file (optional)
    initial begin
        $readmemb("C:/Users/Ahmad/Desktop/github/JoSDC/Innovation Phase/Pipelined_ex/instr_mem_init.txt", mem); // Load instructions from a file
    end

	 always @(*) begin
		 if (~rst) begin
		 instr1 <= 32'b0;   // First instruction
		 instr2 <= 32'b0;   // Second instruction (next word)
		 end
		 else begin 
		 instr1 <= mem[addr];       // First instruction
		 instr2 <= mem[addr + 1];   // Second instruction (next word)
		 end
		 
	 end

endmodule