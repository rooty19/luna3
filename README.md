# Luna3 (RV32I Core With Chisel)
## What is this?
    Luna3 is a tiny RISC-V core with chisel (my first learning Chisel)
    RV32I(now, Not Implemented CSR, timer, interrupt, etc..) in-order, 5 stage pipeline
    CPU behavior may be not correct, so you should use also spike/pk
## Requirement
    Install bellow:
    - Chisel
    - Verilator
    - GTKWave
    - riscv32-unknown-elf-*
    - x86_64-linux-gnu-*

## How to Simulate?
    In src/main/scala/, type "make" (Sample fibonacci program will be compiled/linked/converted, show Makefile)
    Then in this folder, launch sbt, type "testOnly chapter4.RV32_Unified_test"
    to launch GTKWave, type "gtkwave [PATH_TO test_run_dir/chapter4/RV32_Unified/RV32_Unified.vcd]"
    wave layout is "./RV32I_wavelayout.gtkw"

## to do
    CSR
    Add M/U/S mode
    Interrupt/Timer support
    Connect to AXI Lite