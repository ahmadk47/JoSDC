onerror {resume}
quietly virtual function -install /testbench/uut/ID_EX -env /testbench/uut/ID_EX { &{/testbench/uut/ID_EX/Q[115], /testbench/uut/ID_EX/Q[114], /testbench/uut/ID_EX/Q[113], /testbench/uut/ID_EX/Q[112], /testbench/uut/ID_EX/Q[111], /testbench/uut/ID_EX/Q[110], /testbench/uut/ID_EX/Q[109], /testbench/uut/ID_EX/Q[108], /testbench/uut/ID_EX/Q[107], /testbench/uut/ID_EX/Q[106], /testbench/uut/ID_EX/Q[105], /testbench/uut/ID_EX/Q[104], /testbench/uut/ID_EX/Q[103], /testbench/uut/ID_EX/Q[102], /testbench/uut/ID_EX/Q[101], /testbench/uut/ID_EX/Q[100], /testbench/uut/ID_EX/Q[99], /testbench/uut/ID_EX/Q[98], /testbench/uut/ID_EX/Q[97], /testbench/uut/ID_EX/Q[96], /testbench/uut/ID_EX/Q[95], /testbench/uut/ID_EX/Q[94], /testbench/uut/ID_EX/Q[93], /testbench/uut/ID_EX/Q[92], /testbench/uut/ID_EX/Q[91], /testbench/uut/ID_EX/Q[90], /testbench/uut/ID_EX/Q[89], /testbench/uut/ID_EX/Q[88], /testbench/uut/ID_EX/Q[87], /testbench/uut/ID_EX/Q[86], /testbench/uut/ID_EX/Q[85], /testbench/uut/ID_EX/Q[84] }} a7a
quietly virtual signal -install /testbench/uut/ID_EX { /testbench/uut/ID_EX/D[115:84]} a7a1
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix unsigned /testbench/clk
add wave -noupdate -radix unsigned /testbench/rst
add wave -noupdate -radix unsigned /testbench/enable
add wave -noupdate -radix unsigned /testbench/PC
add wave -noupdate -divider {Instruction Memory}
add wave -noupdate -radix hexadecimal /testbench/uut/IM/q
add wave -noupdate -divider {Register File}
add wave -noupdate -radix unsigned /testbench/uut/RF/writeRegister
add wave -noupdate -radix unsigned /testbench/uut/RF/writeData
add wave -noupdate -divider {Data Memory}
add wave -noupdate -radix unsigned /testbench/uut/DM/data
add wave -noupdate -radix unsigned /testbench/uut/DM/q
add wave -noupdate -divider ALU
add wave -noupdate -radix unsigned /testbench/uut/alu/opSel
add wave -noupdate -radix unsigned /testbench/uut/alu/result
add wave -noupdate -radix hexadecimal /testbench/uut/IF_ID/Q
add wave -noupdate -radix unsigned /testbench/uut/rsE
add wave -noupdate -radix unsigned /testbench/uut/readData1E
add wave -noupdate -radix unsigned /testbench/uut/rtE
add wave -noupdate -radix unsigned /testbench/uut/readData2E
add wave -noupdate -radix unsigned /testbench/uut/rdE
add wave -noupdate -radix unsigned /testbench/uut/ALUMux/out
add wave -noupdate -radix unsigned /testbench/uut/ForwardAMuxOut
add wave -noupdate -radix unsigned /testbench/uut/ForwardBMuxOut
add wave -noupdate -radix unsigned /testbench/uut/ForwardA
add wave -noupdate -radix unsigned /testbench/uut/ForwardB
add wave -noupdate /testbench/uut/memToRegW
add wave -noupdate /testbench/uut/branchNew
add wave -noupdate /testbench/uut/branchTaken
add wave -noupdate /testbench/uut/prediction
add wave -noupdate /testbench/uut/CPCSignal
add wave -noupdate /testbench/uut/EnablePCIFID
add wave -noupdate /testbench/uut/Stall
add wave -noupdate /testbench/uut/branch
add wave -noupdate /testbench/uut/Flush
add wave -noupdate -radix unsigned /testbench/uut/BPU/prediction
add wave -noupdate -radix unsigned /testbench/uut/BPU/BHT
add wave -noupdate -radix unsigned /testbench/uut/BPU/index
add wave -noupdate /testbench/uut/CorrectedPC
add wave -noupdate -radix unsigned /testbench/uut/BPU/pc
add wave -noupdate -radix unsigned /testbench/uut/BPU/pcD
add wave -noupdate -radix unsigned /testbench/uut/BPU/CorrectedPC
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {207565 ps} 0}
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
WaveRestoreZoom {589507 ps} {712132 ps}
