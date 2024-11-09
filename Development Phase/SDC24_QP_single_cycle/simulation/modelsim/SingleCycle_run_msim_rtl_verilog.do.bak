transcript on
if {[file exists rtl_work]} {
	vdel -lib rtl_work -all
}
vlib rtl_work
vmap work rtl_work

vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/processor.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/registerFile.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/controlUnit.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/ALU.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/signextender.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/programCounter.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/mux2x1.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/adder.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/ANDGate.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/instructionMemory.v}
vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/dataMemory.v}

vlog -vlog01compat -work work +incdir+C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle {C:/Users/THINKPAD/Documents/JoSDC24/SingleCycle/testbench.v}

vsim -t 1ps -L altera_ver -L lpm_ver -L sgate_ver -L altera_mf_ver -L altera_lnsim_ver -L fiftyfivenm_ver -L rtl_work -L work -voptargs="+acc"  testbench

add wave *
view structure
view signals
run -all
