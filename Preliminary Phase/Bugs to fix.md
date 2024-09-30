| File Name         |       Bugs         |  Found By |          Fixes         |  Fixed By |
|-------------------|:------------------:|----------:|:----------------------:|----------:|
| processsor.v      | rs,rd,rt assignment, @line 27 IM input incorrect, @line 33 "WriteRegister" wrong name|     OmarK, Saleh|  rearrange assignments, change IM input, change to writeRegister|        -- |
| registerFile.v    |  -- |    --  |           --    |        -- |
| controlUnit.v     |@line 52 ALUOp decimal instead of binary       |       Saleh|           change to binary        |        -- |
| ALU.v             |  @line 38 SLT OP, unsafe behaviour of "result", AND & ADD opSel  |      OmarK, Saleh, ahmadK|reverse comparison order, set initial value for result, swap opSel's|        -- |
| signextender.v    |      No bugs       |        -- |            --          |        -- |
| programCounter.v  |      No bugs ?     |          -- |           --             |        -- |
| mux2x1.v          | @line 5,8(size)    |   AhmadK  |        size - 1        |        -- |
| adder.v           |      No bugs       |        -- |            --          |        -- |
| ANDGate.v         |      No bugs       |        -- |            --          |        -- |
| testbench.v       |         --         |        -- |            --          |        -- |
