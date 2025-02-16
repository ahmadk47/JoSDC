SIM ?= icarus
TOPLEVEL_LANG ?= verilog
LIBPYTHON ?= $(LIBPYTHON_LOC)
CFLAGS += -I$(PYTHON_INCLUDE) -L$(LIBPYTHON) -lpython310

VERILOG_SOURCES += $(PWD)/dut.v
VERILOG_SOURCES += $(PWD)/ALU.v
VERILOG_SOURCES += $(PWD)/registerFile.v
VERILOG_SOURCES += $(PWD)/controlUnit.v
VERILOG_SOURCES += $(PWD)/forwardingUnit.v
VERILOG_SOURCES += $(PWD)/HazardDetectionUnit.v
VERILOG_SOURCES += $(PWD)/BranchPredictionUnit.v
VERILOG_SOURCES += $(PWD)/pcCorrection.v
VERILOG_SOURCES += $(PWD)/dual_issue_inst_mem.v
VERILOG_SOURCES += $(PWD)/dual_issue_data_mem.v
VERILOG_SOURCES += $(PWD)/Pipes.v
VERILOG_SOURCES += $(PWD)/muxes.v
VERILOG_SOURCES += $(PWD)/adder.v
VERILOG_SOURCES += $(PWD)/AdderIP.v
VERILOG_SOURCES += $(PWD)/ORGate.v
VERILOG_SOURCES += $(PWD)/ANDGate.v

TOPLEVEL = dut
MODULE = testbench

# Compilation flags
COMPILE_ARGS += -Wall
COMPILE_ARGS += -Wno-implicit

# Enable waveform dump
EXTRA_ARGS += --trace-fst

# Include cocotb's make rules
include $(shell cocotb-config --makefiles)/Makefile.sim

