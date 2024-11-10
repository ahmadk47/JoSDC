transcript on
if {[file exists rtl_work]} {
	vdel -lib rtl_work -all
}
vlib rtl_work
vmap work rtl_work

vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/muxes.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/processor.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/registerFile.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/controlUnit.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/ALU.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/signextender.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/programCounter.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/adder.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/ANDGate.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/instructionMemory.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/dataMemory.v}
vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/XNOR.v}

vlog -vlog01compat -work work +incdir+C:/Users/Saleh/Desktop/Josdc\ phase\ 2/JoSDC/Development\ Phase/SDC24_DP_single_cycle {C:/Users/Saleh/Desktop/Josdc phase 2/JoSDC/Development Phase/SDC24_DP_single_cycle/testbench.v}

vsim -t 1ps -L altera_ver -L lpm_ver -L sgate_ver -L altera_mf_ver -L altera_lnsim_ver -L fiftyfivenm_ver -L rtl_work -L work -voptargs="+acc"  testbench

add wave *
view structure
view signals
run -all
