transcript on
if {[file exists gate_work]} {
	vdel -lib gate_work -all
}
vlib gate_work
vmap work gate_work

vlog -vlog01compat -work work +incdir+. {Pipelined.vo}

vlog -vlog01compat -work work +incdir+C:/Users/DELL-G5/Desktop/github/JoSDC/Development\ Phase/Pipelined {C:/Users/DELL-G5/Desktop/github/JoSDC/Development Phase/Pipelined/testbench.v}

vsim -t 1ps -L altera_ver -L altera_lnsim_ver -L fiftyfivenm_ver -L gate_work -L work -voptargs="+acc"  testbench

add wave *
view structure
view signals
run -all
