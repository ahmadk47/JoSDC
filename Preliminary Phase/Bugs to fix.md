| File Name         |       Bugs         |  Found By |          Fixes         |  Fixed By |
|-------------------|:------------------:|----------:|:----------------------:|----------:|
| processsor.v      | rs,rd,rt assignment, @line 27 IM input incorrect, @line 33 "WriteRegister" wrong name, instruction memory IM wrong address input|     OmarK, Saleh, AhmadK|  rearrange assignments, change IM input, change to writeRegister|        -- |
| registerFile.v    |@line 16 Endiannes of the registerFile   |        AhmadK |           change endiannes      |        -- |
| controlUnit.v     |@line 52 ALUOp decimal instead of binary       |       Saleh|           change to binary        |        -- |
| ALU.v             |  @line 38 SLT OP, unsafe behaviour of "result"  |      OmarK, Saleh|reverse comparison order, set initial value for result|        -- |
| signextender.v    |      No bugs       |        -- |            --          |        -- |
| programCounter.v  |      No bugs ?     |           |                        |        -- |
| mux2x1.v          | @line 5,8(size)    |   AhmadK  |        size - 1        |        -- |
| adder.v           |      No bugs       |        -- |            --          |        -- |
| ANDGate.v         |      No bugs       |        -- |            --          |        -- |
| testbench.v       |         --         |        -- |            --          |        -- |
