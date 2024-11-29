module pipe #(parameter size = 96)(Q, D, clk, reset, enable);
input [size - 1:0] D;
input clk, reset, enable;
output reg [size - 1:0] Q;



always @(posedge clk, negedge reset) begin


if (~reset)
	Q <= {size{1'b0}};
	
else if (enable & reset)
	Q <= D;

else
	Q <= Q;

end 

endmodule 