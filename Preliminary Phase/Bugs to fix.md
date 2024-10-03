| File Name         |       Bugs         |  Found By |          Fixes         |  Fixed By |
|-------------------|:------------------:|----------:|:----------------------:|----------:|
| processsor.v      | rs,rd,rt assignment, @line 33 "WriteRegister" wrong name, @51 order of WB mux is reversed|     OmarK, Saleh, AhmadK|  rearrange assignments, change IM input, change to writeRegister, swap WB mux inputs|        -- |
| registerFile.v    |  -- |    --  |           --    |        -- |
| controlUnit.v     |@line 52 ALUOp decimal instead of binary       |       Saleh|           change to binary        |        -- |
| ALU.v             |  @line 38 SLT OP, unsafe behaviour of "result", AND & ADD opSel, @line 47 comparison wrong (compares w 1 bit) |      OmarK, Saleh, ahmadK|reverse comparison order, set initial value for result, swap opSel's, change to compare w 32 bits|        -- |
| signextender.v    |      No bugs       |        -- |            --          |        -- |
| programCounter.v  |      No bugs ?     |          -- |           --             |        -- |
| mux2x1.v          | @line 5,8(size)    |   AhmadK  |        size - 1        |        -- |
| adder.v           |      No bugs       |        -- |            --          |        -- |
| ANDGate.v         |      No bugs       |        -- |            --          |        -- |
| testbench.v       |         --         |        -- |            --          |        -- |
