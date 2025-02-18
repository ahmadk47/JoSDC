`timescale 1ns/1ps
module testbench;

	reg clk, rst, enable;
	
	wire [10:0] PC;
	
	initial begin
		clk = 0;
		rst = 0;
		enable = 1;
		#4 rst = 1;
		#3000 $stop;
	end
	
	always #5 clk = ~clk;
	
	processor uut(clk, rst, PC, enable);
	
	
endmodule
