module programCounter (clk, rst, PCin, enable, PCout);
	
	//inputs
	input clk, rst,enable;
	input [7:0] PCin;
	
	//outputs 
	output reg [7:0] PCout;
	
	//Counter logic
	always@(posedge clk, posedge rst) begin
		if(rst) begin
			PCout <= 8'd255;
		end
		else if (enable) begin
			PCout <= PCin;
		end
		else
		PCout <= PCout;
	end
	
endmodule
