module programCounter (clk, rst, PCin, PCout);
	
	//inputs
	input clk, rst;
	input [5:0] PCin;
	
	//outputs 
	output reg [5:0] PCout;
	
	//Counter logic
	always@(posedge clk, negedge rst) begin
		if(~rst) begin
			PCout <= 6'b111111;
		end
		else begin
			PCout <= PCin;
		end
	end
	
endmodule
