onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate /testbench/clk
add wave -noupdate /testbench/rst
add wave -noupdate -radix decimal /testbench/PC
add wave -noupdate -divider {Instruction Memory}
add wave -noupdate -radix unsigned /testbench/uut/IM/address
add wave -noupdate -radix hexadecimal /testbench/uut/IM/q
add wave -noupdate -divider {Register File}
add wave -noupdate -radix unsigned /testbench/uut/RF/readRegister1
add wave -noupdate -radix unsigned /testbench/uut/RF/readData1
add wave -noupdate -radix unsigned /testbench/uut/RF/readRegister2
add wave -noupdate -radix unsigned /testbench/uut/RF/readData2
add wave -noupdate -radix unsigned /testbench/uut/RF/writeRegister
add wave -noupdate -radix unsigned /testbench/uut/RF/writeData
add wave -noupdate -divider {Data Memory}
add wave -noupdate -radix hexadecimal /testbench/uut/DM/data
add wave -noupdate -radix unsigned /testbench/uut/DM/address
add wave -noupdate -radix unsigned /testbench/uut/DM/q
add wave -noupdate -divider ALU
add wave -noupdate -radix binary /testbench/uut/alu/opSel
add wave -noupdate -radix decimal /testbench/uut/alu/result
add wave -noupdate -radix binary /testbench/uut/alu/zero
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {0 ps} 0}
quietly wave cursor active 1
configure wave -namecolwidth 213
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
WaveRestoreZoom {0 ps} {42939 ps}
