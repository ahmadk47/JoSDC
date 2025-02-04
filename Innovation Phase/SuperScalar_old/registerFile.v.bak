// register file contains 32 register

module registerFile (clk, rst, we, 
					 readRegister1, readRegister2, writeRegister,
					 writeData, readData1, readData2);

	// inputs
	input wire clk, rst, we;
	input wire [4:0] readRegister1, readRegister2, writeRegister;
	input wire [31:0] writeData;
	
	// outputs
	output wire [31:0] readData1, readData2;
	
	// register file (registers)
	reg [31:0] registers [0:31];
	
	// Read from the register file
	assign readData1 = registers[readRegister1];
   assign readData2 = registers[readRegister2];
	
	always@(posedge clk,  negedge rst) begin : Write_on_register_file_block
	
		integer i;
		// Reset the register file
		if(~rst) begin
			for(i=0; i<32; i = i + 1) registers[i] = 0;
		end
		// Write to the register file
		else if(we) begin
			registers[writeRegister] <= writeData;
		end
		// Defualt to prevent latching
		else;
		
	end

endmodule