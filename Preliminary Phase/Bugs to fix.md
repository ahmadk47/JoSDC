| File Name         |       Bugs         |  Found By |          Fixes         |  Fixed By |
|-------------------|:------------------:|----------:|:----------------------:|----------:|
| processsor.v      | rs,rd,rt assignment, @line 27 IM input incorrect|     OmarK |  rearrange assignments, change IM input |        -- |
| registerFile.v    |         --         |        -- |            --          |        -- |
| controlUnit.v     |         --         |        -- |            --          |        -- |
| ALU.v             |  @line 38 SLT OP   |      OmarK|reverse comparison order|        -- |
| signextender.v    |      No bugs       |        -- |            --          |        -- |
| programCounter.v  | incorrect pc reset |     Saleh |   let pc reset to zero |        -- |
| mux2x1.v          | @line 5,8(size)    |   AhmadK  |        size - 1        |        -- |
| adder.v           |      No bugs       |        -- |            --          |        -- |
| ANDGate.v         |      No bugs       |        -- |            --          |        -- |
| testbench.v       |         --         |        -- |            --          |        -- |
