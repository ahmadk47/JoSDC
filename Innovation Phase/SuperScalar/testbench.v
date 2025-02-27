`timescale 1ns/1ps
module testbench;

	reg clk, rst, enable;
	
	wire [7:0] PC;
	
	always @ (posedge clk) begin
		if (uut.PC >= 8'd193 && uut.PC!=8'd254)
			$finish;
		end
	
	initial begin
		clk = 0;
		rst = 0;
		enable = 1;
		#4 rst = 1;
		#150000000 $stop;
	end
	
	
	always #5 clk = ~clk;
	
	processor uut(clk, rst, PC, enable);
	
	
endmodule
