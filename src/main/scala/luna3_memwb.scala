package chapter4

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class RV32_memwb extends Module{
    val io = IO(new Bundle{
        val bus_e2m   = Input(new luna3_Bus_Set)
        val inst_e2m = Input(new luna3_RV32I_instruct_set)
        val DO_flush = Input(Bool())
        val opcode   = Output(UInt(7.W))
        val regf_wa1 = Output(UInt(5.W))
        val regf_wd1 = Output(UInt(32.W))
        val regf_we1 = Output(Bool())
    })

// Mem Stage

    // Write to MEM
    val dmem = Mem(0xffffff, UInt(32.W))
    loadMemoryFromFile(dmem, "src/main/scala/dmem.txt")
    when(io.inst_e2m.sb)     {dmem(io.bus_e2m.addr) := (io.bus_e2m.data & "h000000ff".U)}
    .elsewhen(io.inst_e2m.sh){dmem(io.bus_e2m.addr) := (io.bus_e2m.data & "h0000ffff".U)}
    .elsewhen(io.inst_e2m.sw){dmem(io.bus_e2m.addr) :=  io.bus_e2m.data}
    

    // MEM Stage To WB Stage
    val regf_wa1_reg = RegInit(5.U) 
    val regf_wd1_reg = RegInit(32.U)
    val regf_we1_reg = RegInit(false.B)
    regf_wa1_reg := "b00000".U
    regf_wd1_reg := "h00000000".U
    regf_we1_reg := false.B

    val itype = new RV32I_InstType_B
    
    when(   io.bus_e2m.opcode === itype.op_R        |
            io.bus_e2m.opcode === itype.op_I_ALU    | 
            io.bus_e2m.opcode === itype.op_I_JALR   |
            io.bus_e2m.opcode === itype.op_U_LUI    |
            io.bus_e2m.opcode === itype.op_U_AUIPC  |
            io.bus_e2m.opcode === itype.op_J
        ){
        regf_wa1_reg := io.bus_e2m.rd
        regf_wd1_reg := io.bus_e2m.data
        regf_we1_reg := true.B
    }.elsewhen(io.bus_e2m.opcode === itype.op_I_LOAD){
        regf_wa1_reg := io.bus_e2m.rd
        regf_we1_reg := true.B

        when     (io.inst_e2m.lb)   {regf_wd1_reg := ((dmem(io.bus_e2m.addr) & "h000000ff".U).asSInt()).asUInt()}
        .elsewhen(io.inst_e2m.lbu)  {regf_wd1_reg := (dmem(io.bus_e2m.addr)  & "h000000ff".U)}
        .elsewhen(io.inst_e2m.lh)   {regf_wd1_reg := ((dmem(io.bus_e2m.addr) & "h0000ffff".U).asSInt()).asUInt()}
        .elsewhen(io.inst_e2m.lhu)  {regf_wd1_reg := (dmem(io.bus_e2m.addr)  & "h0000ffff".U)}
        .elsewhen(io.inst_e2m.lw)   {regf_wd1_reg := (dmem(io.bus_e2m.addr))}
    }.otherwise{
        regf_wa1_reg := io.bus_e2m.rd
        regf_wd1_reg := io.bus_e2m.data
        regf_we1_reg := false.B
    }
    

// WB Stage
    io.regf_wa1 := regf_wa1_reg
    io.regf_wd1 := regf_wd1_reg
    io.regf_we1 := regf_we1_reg
    

// Forwarding
    val opcode_Reg = RegInit(0.U(7.W))
    opcode_Reg := io.bus_e2m.opcode
    io.opcode := opcode_Reg
}