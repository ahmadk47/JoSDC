This directory contains the final benchmarks for JoSDC'24 competition, which are:
1- Matrix Multiplication
2- Decryption
3- 3-Sum Problem Solution

each benchmark directory contains all necessary data needed for configuration and testing.

you're required to fully understand the concept of the benchmarks, to apply testing and verification.

during and after finishing verification, you're required to prepare the results for exhibition day.

also, you're required to submit the results with verification evidences for both sides, functionality and performance.

the results you may submit contain and not limited to:

Configuration & Setup:
	* Assembly Code before and after changing (in case the Assembler do)
	* Machine Code (resulted from assembler/compiler)
	* memory initialization files (mif/hex/txt... as used)

Functional Results: 
	* Register file values, data memory values.

Performance Results:
	* Fmax for all models, especially for the slower one (worst case), extracted from Quartus.
	* Performance counters measured during execution (cycles, stalls, executed instructions, branch prediction hit & miss rates).
	* Calculated performance results: Execution time, IPC or CPI, throughput.