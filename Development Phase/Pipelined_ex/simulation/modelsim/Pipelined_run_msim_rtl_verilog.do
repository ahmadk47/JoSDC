transcript on
if {[file exists rtl_work]} {
	vdel -lib rtl_work -all
}
vlib rtl_work
vmap work rtl_work

vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/BranchPredictionUnit.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/muxes.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/processor.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/registerFile.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/controlUnit.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/ALU.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/signextender.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/programCounter.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/adder.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/ANDGate.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/instructionMemory.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/dataMemory.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/XNOR.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/Pipes.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/Comparator.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/forwardingUnit.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/HazardDetectionUnit.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/ORGate.v}
vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/pcCorrection.v}

vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined_ex {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined_ex/testbench.v}

vsim -t 1ps -L altera_ver -L lpm_ver -L sgate_ver -L altera_mf_ver -L altera_lnsim_ver -L fiftyfivenm_ver -L rtl_work -L work -voptargs="+acc"  testbench

add wave *
view structure
view signals
run -all
