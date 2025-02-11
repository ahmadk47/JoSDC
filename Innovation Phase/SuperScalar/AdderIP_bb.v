// megafunction wizard: %PARALLEL_ADD%VBB%
// GENERATION: STANDARD
// VERSION: WM1.0
// MODULE: parallel_add 

// ============================================================
// File Name: AdderIP.v
// Megafunction Name(s):
// 			parallel_add
//
// Simulation Library Files(s):
// 			altera_mf
// ============================================================
// ************************************************************
// THIS IS A WIZARD-GENERATED FILE. DO NOT EDIT THIS FILE!
//
// 20.1.1 Build 720 11/11/2020 SJ Lite Edition
// ************************************************************

//Copyright (C) 2020  Intel Corporation. All rights reserved.
//Your use of Intel Corporation's design tools, logic functions 
//and other software and tools, and any partner logic 
//functions, and any output files from any of the foregoing 
//(including device programming or simulation files), and any 
//associated documentation or information are expressly subject 
//to the terms and conditions of the Intel Program License 
//Subscription Agreement, the Intel Quartus Prime License Agreement,
//the Intel FPGA IP License Agreement, or other applicable license
//agreement, including, without limitation, that your use is for
//the sole purpose of programming logic devices manufactured by
//Intel and sold by Intel or its authorized distributors.  Please
//refer to the applicable agreement for further details, at
//https://fpgasoftware.intel.com/eula.

module AdderIP (
	data0x,
	data1x,
	result);

	input	[10:0]  data0x;
	input	[10:0]  data1x;
	output	[10:0]  result;

endmodule

// ============================================================
// CNX file retrieval info
// ============================================================
// Retrieval info: PRIVATE: INTENDED_DEVICE_FAMILY STRING "MAX 10"
// Retrieval info: PRIVATE: SYNTH_WRAPPER_GEN_POSTFIX STRING "0"
// Retrieval info: LIBRARY: altera_mf altera_mf.altera_mf_components.all
// Retrieval info: CONSTANT: MSW_SUBTRACT STRING "NO"
// Retrieval info: CONSTANT: PIPELINE NUMERIC "0"
// Retrieval info: CONSTANT: REPRESENTATION STRING "SIGNED"
// Retrieval info: CONSTANT: RESULT_ALIGNMENT STRING "LSB"
// Retrieval info: CONSTANT: SHIFT NUMERIC "0"
// Retrieval info: CONSTANT: SIZE NUMERIC "2"
// Retrieval info: CONSTANT: WIDTH NUMERIC "11"
// Retrieval info: CONSTANT: WIDTHR NUMERIC "11"
// Retrieval info: USED_PORT: data0x 0 0 11 0 INPUT NODEFVAL "data0x[10..0]"
// Retrieval info: USED_PORT: data1x 0 0 11 0 INPUT NODEFVAL "data1x[10..0]"
// Retrieval info: USED_PORT: result 0 0 11 0 OUTPUT NODEFVAL "result[10..0]"
// Retrieval info: CONNECT: @data 0 0 11 0 data0x 0 0 11 0
// Retrieval info: CONNECT: @data 0 0 11 11 data1x 0 0 11 0
// Retrieval info: CONNECT: result 0 0 11 0 @result 0 0 11 0
// Retrieval info: GEN_FILE: TYPE_NORMAL AdderIP.v TRUE
// Retrieval info: GEN_FILE: TYPE_NORMAL AdderIP.inc FALSE
// Retrieval info: GEN_FILE: TYPE_NORMAL AdderIP.cmp FALSE
// Retrieval info: GEN_FILE: TYPE_NORMAL AdderIP.bsf FALSE
// Retrieval info: GEN_FILE: TYPE_NORMAL AdderIP_inst.v TRUE
// Retrieval info: GEN_FILE: TYPE_NORMAL AdderIP_bb.v TRUE
// Retrieval info: LIB_FILE: altera_mf
