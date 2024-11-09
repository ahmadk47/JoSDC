module ALU (operand1, operand2, shamt, opSel, result, zero );
	
	parameter data_width = 32;
	parameter sel_width = 4;
	
	// Inputs
	// ......
	
	input [data_width - 1 : 0] operand1, operand2;
	input [sel_width - 1 :0] opSel;
	input [4:0] shamt;
	
	// Outputs
	// .......
	
	output reg [data_width - 1 : 0] result;
	output reg zero;
	
	// Operation Parameters
	// ....................
	parameter   _ADD  = 'b0000, _SUB  = 'b0001, _AND = 'b0010,
				_OR   = 'b0011, _SLT  = 'b0100, _SGT = 'b0101, _NOR = 'b0110, _XOR = 'b0111,
				_SLL = 'b1000, _SRL = 'b1001;	
	
	// Perform Operation
	// .................
	
	always @ (*) begin
	
			result = 32'b0; 
		
		case(opSel)
			
			_ADD: result = operand1 + operand2;
			
			_SUB: result = operand1 - operand2;
			
			_AND: result = operand1 & operand2;
			
			_OR : result = operand1 | operand2;
			
			_NOR: result = ~(operand1 | operand2);
			
			_XOR: result = operand1 ^ operand2;
			
			_SLT: result = (operand1 < operand2) ? 1 : 0;
			
			_SGT: result = (operand1 > operand2) ? 1 : 0;
			
			_SLL: result = operand2 << shamt;
			
			_SRL: result = operand2 >> shamt;
			
			default:;

		endcase
	
	end
	
	always @ (*) begin 
		
		zero = (result == 32'b0); 
	end

endmodule