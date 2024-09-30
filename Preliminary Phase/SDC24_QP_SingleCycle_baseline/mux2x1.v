module mux2x1 #(parameter size = 32) (in1, in2, s, out);

	// inputs	
	input s;
	input [size:0] in1, in2;
	
	// outputs
	output [size:0] out;

	// Unit logic
	assign out = (~s) ? in1 : in2;
	
endmodule