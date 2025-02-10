onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix unsigned /testbench/clk
add wave -noupdate -radix unsigned /testbench/rst
add wave -noupdate -radix unsigned /testbench/enable
add wave -noupdate -radix unsigned /testbench/PC
add wave -noupdate -radix hexadecimal /testbench/uut/instMem/q_a
add wave -noupdate -radix hexadecimal /testbench/uut/instMem/q_b
add wave -noupdate -radix unsigned /testbench/uut/writeRegister1W
add wave -noupdate -radix unsigned /testbench/uut/writeData1
add wave -noupdate -radix unsigned /testbench/uut/writeRegister2W
add wave -noupdate -radix unsigned /testbench/uut/writeData2
add wave -noupdate -radix unsigned /testbench/uut/readData1
add wave -noupdate -radix unsigned /testbench/uut/readData1E
add wave -noupdate -radix unsigned /testbench/uut/readData2
add wave -noupdate -radix unsigned /testbench/uut/readData2E
add wave -noupdate -radix unsigned /testbench/uut/readData3
add wave -noupdate -radix unsigned /testbench/uut/readData3E
add wave -noupdate -radix unsigned /testbench/uut/readData4
add wave -noupdate -radix unsigned /testbench/uut/readData4E
add wave -noupdate -radix unsigned /testbench/uut/RegDst1
add wave -noupdate -radix unsigned /testbench/uut/RegDst2
add wave -noupdate -radix unsigned /testbench/uut/RegDst1D
add wave -noupdate -radix unsigned /testbench/uut/RegDst2D
add wave -noupdate -radix unsigned /testbench/uut/RegDst1E
add wave -noupdate -radix unsigned /testbench/uut/RegDst2E
add wave -noupdate -radix unsigned /testbench/uut/rs1E
add wave -noupdate -radix unsigned /testbench/uut/rt1E
add wave -noupdate -radix unsigned /testbench/uut/rd1E
add wave -noupdate -radix unsigned /testbench/uut/rs2E
add wave -noupdate -radix unsigned /testbench/uut/rt2E
add wave -noupdate -radix unsigned /testbench/uut/rd2E
add wave -noupdate -radix unsigned /testbench/uut/rs1
add wave -noupdate -radix unsigned /testbench/uut/rt1
add wave -noupdate -radix unsigned /testbench/uut/rd1
add wave -noupdate -radix unsigned /testbench/uut/rs2
add wave -noupdate -radix unsigned /testbench/uut/rt2
add wave -noupdate -radix unsigned /testbench/uut/rd2
add wave -noupdate -radix hexadecimal /testbench/uut/opCode1
add wave -noupdate -radix hexadecimal /testbench/uut/opCode2
add wave -noupdate -radix hexadecimal /testbench/uut/funct1
add wave -noupdate -radix hexadecimal /testbench/uut/funct2
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {60563 ps} 0}
quietly wave cursor active 1
configure wave -namecolwidth 215
configure wave -valuecolwidth 100
configure wave -justifyvalue left
configure wave -signalnamewidth 0
configure wave -snapdistance 10
configure wave -datasetprefix 0
configure wave -rowmargin 4
configure wave -childrowmargin 2
configure wave -gridoffset 0
configure wave -gridperiod 1
configure wave -griddelta 40
configure wave -timeline 0
configure wave -timelineunits ns
update
WaveRestoreZoom {10701 ps} {111016 ps}
