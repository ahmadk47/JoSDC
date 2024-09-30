module controlUnit(opCode, funct,
				   RegDst, Branch, MemReadEn, MemtoReg,
				   ALUOp, MemWriteEn, RegWriteEn, ALUSrc);
				   
		
	// inputs 
	input wire [5:0] opCode, funct;
	
	// outputs (signals)
	output reg RegDst, Branch, MemReadEn, MemtoReg, MemWriteEn, RegWriteEn, ALUSrc;
	output reg [2:0] ALUOp;
	
	// parameters (opCodes/functs)
	parameter _RType = 6'h0, _addi = 6'h8, _lw = 6'h23, _sw = 6'h2b, _beq = 6'h4;
	parameter _add_ = 6'h20, _sub_ = 6'h22, _and_ = 6'h24, _or_ = 6'h25, _slt_ = 6'h2a;
	
	
	// unit logic - generate signals
	always @(*) begin
	
		RegDst = 1'b0; Branch = 1'b0; MemReadEn = 1'b0; MemtoReg = 1'b0;
		MemWriteEn = 1'b0; RegWriteEn = 1'b0; ALUSrc = 1'b0;
		ALUOp = 3'b0;
		
		case(opCode)
				
			_RType : begin
				
				RegDst = 1'b1;
				Branch = 1'b0;
				MemReadEn = 1'b0;
				MemtoReg = 1'b0;
				MemWriteEn = 1'b0;
				RegWriteEn = 1'b1;
				ALUSrc = 1'b0;
					
				case (funct) 
					
					_add_ : begin
						ALUOp = 3'b000;
					end
						
					_sub_ : begin
						ALUOp = 3'b001;
					end
						
					_and_ : begin
						ALUOp = 3'b010;
					end
						
					_or_ : begin
						ALUOp = 3'd011;
					end
						
					_slt_ : begin
						ALUOp = 3'b100;
					end
					
					default: ;
				
				endcase
				
			end
				
			_addi : begin
				RegDst = 1'b0;
				Branch = 1'b0;
				MemReadEn = 1'b0;
				MemtoReg = 1'b0;
				ALUOp = 3'b000;
				MemWriteEn = 1'b0;
				RegWriteEn = 1'b1;
				ALUSrc = 1'b1;				
			end
				
			_lw : begin
				RegDst = 1'b1;
				Branch = 1'b0;
				MemReadEn = 1'b0;
				MemtoReg = 1'b1;
				ALUOp = 3'b000;
				MemWriteEn = 1'b1;
				RegWriteEn = 1'b1;		
				ALUSrc = 1'b1;		
			end
				
			_sw : begin
				Branch = 1'b0;
				MemReadEn = 1'b0;
				ALUOp = 3'b000;
				MemWriteEn = 1'b1;
				RegWriteEn = 1'b0;
				ALUSrc = 1'b1;				
			end
				
			_beq : begin
				Branch = 1'b1;
				MemReadEn = 1'b0;
				ALUOp = 3'b001;
				MemWriteEn = 1'b0;
				RegWriteEn = 1'b0;
				ALUSrc = 1'b1;				
			end
			
			default: ;
				
		endcase
			
	end
	
	
endmodule
