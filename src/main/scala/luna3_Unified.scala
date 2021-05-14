package chapter4

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class RV32_Unified extends Module{
    val io = IO(new Bundle{
        val PC_f2d = Output(UInt(32.W))
        val inst_f2d = Output(UInt(32.W))
        val insttype_f2d = Output(new luna3_RV32I_instruct_set)

        val PC_d2e = Output(UInt(32.W))
        val inst_d2e = Output(UInt(32.W))
        val insttype_d2e = Output(new luna3_RV32I_instruct_set)

        val PC_e2m = Output(UInt(32.W))
        val inst_e2m = Output(UInt(32.W))
        val insttype_e2m = Output(new luna3_RV32I_instruct_set)

        val bus_e2m = Output(new luna3_Bus_Set)
    })
    
    val rv32_fetch = Module(new RV32_Fetch())
    val rv32_decode = Module(new RV32_Decode())
    val rv32_exec = Module(new RV32_exec())
    val rv32_memwb = Module(new RV32_memwb())

    rv32_fetch.io.DO_next := true.B

    // Fetch -> Decode
        rv32_decode.io.PC_f2d := rv32_fetch.io.PC_now
        rv32_decode.io.inst_f2d := rv32_fetch.io.inst

    // Decode -> Exec
        rv32_exec.io.bus_d2e := rv32_decode.io.bus_d2e
        rv32_exec.io.inst_d2e := rv32_decode.io.inst_d2e

    // Exec -> memwb
        rv32_memwb.io.bus_e2m := rv32_exec.io.bus_e2m
        rv32_memwb.io.inst_e2m := rv32_exec.io.inst_e2m
        
    // memwb -> Decode (WriteBack)
        rv32_decode.io.regf_wa1 := rv32_memwb.io.regf_wa1
        rv32_decode.io.regf_wd1 := rv32_memwb.io.regf_wd1
        rv32_decode.io.regf_we1 := rv32_memwb.io.regf_we1
        
    // brunch, flush
        rv32_fetch.io.DO_brunch := rv32_exec.io.DO_brunch
        rv32_decode.io.DO_flush := rv32_exec.io.DO_brunch
        rv32_memwb.io.DO_flush := rv32_exec.io.DO_brunch

        rv32_fetch.io.PC_brunch := rv32_exec.io.bus_e2m.addr

    // Forwarding
        val rv32_forward = Module(new RV32_forward)
        rv32_forward.io.bus_d2e := rv32_decode.io.bus_d2e
        rv32_forward.io.bus_e2m := rv32_exec.io.bus_e2m
        rv32_forward.io.opcode_wb := rv32_memwb.io.opcode
        rv32_forward.io.wa1_wb    := rv32_memwb.io.regf_wa1
        rv32_forward.io.wd1_wb    := rv32_memwb.io.regf_wd1
        
        rv32_exec.io.In_rs1 := rv32_forward.io.In_rs1
        rv32_exec.io.In_rs2 := rv32_forward.io.In_rs2

        // rv32_exec.io.opcode_wb := rv32_memwb.io.opcode
        // rv32_exec.io.wa1_wb    := rv32_memwb.io.regf_wa1
        // rv32_exec.io.wd1_wb    := rv32_memwb.io.regf_wd1        

    // Debug Ports
    val InstDebug_f2d = Module(new RV32_Instruction_Set_Debug)
        InstDebug_f2d.io.instr := rv32_fetch.io.inst
        io.insttype_f2d := InstDebug_f2d.io.RV32I_InstSet
        io.PC_f2d := rv32_fetch.io.PC_now
        io.inst_f2d := rv32_fetch.io.inst

    val InstDebug_d2e = Module(new RV32_Instruction_Set_Debug)
        InstDebug_d2e.io.instr := rv32_decode.io.bus_d2e.inst
        io.insttype_d2e := InstDebug_d2e.io.RV32I_InstSet
        io.PC_d2e := rv32_decode.io.bus_d2e.pc
        io.inst_d2e := rv32_decode.io.bus_d2e.inst

    val InstDebug_e2m = Module(new RV32_Instruction_Set_Debug)
        InstDebug_e2m.io.instr := rv32_exec.io.bus_e2m.inst
        io.insttype_e2m := InstDebug_e2m.io.RV32I_InstSet
        io.PC_e2m := rv32_exec.io.bus_e2m.pc
        io.inst_e2m := rv32_exec.io.bus_e2m.inst

    io.bus_e2m := rv32_exec.io.bus_e2m
}