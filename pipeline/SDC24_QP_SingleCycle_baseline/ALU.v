module ALU (operand1, operand2, opSel, result, zero );
	
	parameter data_width = 32;
	parameter sel_width = 3;
	
	// Inputs
	// ......
	
	input [data_width - 1 : 0] operand1, operand2;
	input [sel_width - 1 :0] opSel;
	
	// Outputs
	// .......
	
	output reg [data_width - 1 : 0] result;
	output reg zero;
	
	// Operation Parameters
	// ....................
	parameter   _ADD  = 'b000, _SUB  = 'b001, _AND = 'b010, // swap AND & ADD opSel values
				_OR   = 'b011, _SLT  = 'b100;	
	
	// Perform Operation
	// .................
	
	always @ (*) begin
	
			result = 32'b0; // set a default value for "result" to prevent latching
		
		case(opSel)
			
			_ADD: result = operand1 + operand2;
			
			_SUB: result = operand1 - operand2;
			
			_AND: result = operand1 & operand2;
			
			_OR : result = operand1 | operand2;
			
			_SLT: result = (operand1 < operand2) ? 1 : 0; // changed order of comparison
			
			default:;

		endcase
	
	end
	
	always @ (*) begin 
		
		zero = (result == 32'b0); // explicitly set the width for the comparison
	
	end

endmodule