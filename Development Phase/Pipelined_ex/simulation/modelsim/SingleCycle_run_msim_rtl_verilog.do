transcript on
if {[file exists rtl_work]} {
	vdel -lib rtl_work -all
}
vlib rtl_work
vmap work rtl_work

vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/muxes.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/processor.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/registerFile.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/controlUnit.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/ALU.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/signextender.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/programCounter.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/adder.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/ANDGate.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/instructionMemory.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/dataMemory.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/XNOR.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/Pipes.v}

vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/Pipelined {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/Pipelined/testbench.v}

vsim -t 1ps -L altera_ver -L lpm_ver -L sgate_ver -L altera_mf_ver -L altera_lnsim_ver -L fiftyfivenm_ver -L rtl_work -L work -voptargs="+acc"  testbench

add wave *
view structure
view signals
run -all
