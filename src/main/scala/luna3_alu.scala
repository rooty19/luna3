package chapter4

import chisel3._
import chisel3.util._
import javax.swing.InputMap

class RV32_exec extends Module {
    val io = IO( new Bundle{
        val bus_d2e = Input(new luna3_Bus_Set)
        val inst_d2e = Input(new luna3_RV32I_instruct_set)        
        val DO_flush = Output(Bool())
        val DO_brunch = Output(Bool())
        val bus_e2m = Output(new luna3_Bus_Set)
        val inst_e2m = Output(new luna3_RV32I_instruct_set) 
        // Forwarding
        val In_rs1 = Input(UInt(32.W))
        val In_rs2 = Input(UInt(32.W))
        // val opcode_wb = Input(UInt(7.W))
        // val wa1_wb = Input(UInt(5.W))
        // val wd1_wb = Input(UInt(32.W))
    })

val bus_i = RegInit(0.U.asTypeOf(new luna3_Bus_Set))  

// Imm
    val DO_flush_reg = RegInit(false.B)
    val DO_brunch_reg = RegInit(false.B)
    DO_flush_reg := false.B
    DO_brunch_reg := false.B

    val inst_i_reg = RegInit(0.U.asTypeOf(new luna3_RV32I_instruct_set))
    when(DO_flush_reg){inst_i_reg := (0.U.asTypeOf(new luna3_RV32I_instruct_set))}
    .otherwise        {inst_i_reg := io.inst_d2e}

    io.inst_e2m := inst_i_reg
    bus_i.pc := io.bus_d2e.pc
    bus_i.inst := io.bus_d2e.inst
    
    bus_i.opcode := io.bus_d2e.opcode
    bus_i.rd := io.bus_d2e.rd
    bus_i.funct3 := io.bus_d2e.funct3
    bus_i.ra1 := io.bus_d2e.ra1
    bus_i.ra2 := io.bus_d2e.ra2
    bus_i.funct7_R := io.bus_d2e.funct7_R
    bus_i.data := "h00000000".U
    bus_i.addr := "h00000000".U
    
    when(DO_flush_reg)              {DO_flush_reg := false.B
                                    DO_brunch_reg := false.B
                                    bus_i.data := "h00000000".U
                                    bus_i.addr := "h00000000".U
                                    bus_i.rd := 0.U
                                    }    
    .elsewhen(io.inst_d2e.add )     { bus_i.data :=io.In_rs1 + io.In_rs2}
    .elsewhen(io.inst_d2e.sub ) 	{ bus_i.data :=io.In_rs1 - io.In_rs2}
    .elsewhen(io.inst_d2e.sll ) 	{ bus_i.data :=io.In_rs1 << io.In_rs2(4,0)}
    .elsewhen(io.inst_d2e.slt ) 	{ bus_i.data :=(io.In_rs1.asSInt() < io.In_rs2.asSInt()).asUInt()}
    .elsewhen(io.inst_d2e.sltu )	{ bus_i.data :=io.In_rs1 < io.In_rs2}
    .elsewhen(io.inst_d2e.xor ) 	{ bus_i.data :=io.In_rs1 ^ io.In_rs2}
    .elsewhen(io.inst_d2e.srl ) 	{ bus_i.data :=io.In_rs1 >> io.In_rs2(4,0)}
    .elsewhen(io.inst_d2e.sra ) 	{ bus_i.data := (io.In_rs1.asSInt() >> io.In_rs2(4,0)).asUInt()}
    .elsewhen(io.inst_d2e.or )  	{ bus_i.data :=io.In_rs1 | io.In_rs2}
    .elsewhen(io.inst_d2e.and ) 	{ bus_i.data :=io.In_rs1 & io.In_rs2}

    // I Inst (ALU)
    .elsewhen(io.inst_d2e.addi ) 	{ bus_i.data :=io.In_rs1 + io.bus_d2e.imm}
    .elsewhen(io.inst_d2e.slti ) 	{ bus_i.data :=io.In_rs1.asSInt() < io.bus_d2e.imm.asSInt().asSInt()}
    .elsewhen(io.inst_d2e.sltiu )	{ bus_i.data :=io.In_rs1 < io.bus_d2e.imm}    
    .elsewhen(io.inst_d2e.xori ) 	{ bus_i.data :=io.In_rs1 ^ io.bus_d2e.imm} 
    .elsewhen(io.inst_d2e.ori )  	{ bus_i.data :=io.In_rs1 | io.bus_d2e.imm}
    .elsewhen(io.inst_d2e.andi ) 	{ bus_i.data :=io.In_rs1 & io.bus_d2e.imm}
    .elsewhen(io.inst_d2e.slli ) 	{ bus_i.data :=io.In_rs1 << io.In_rs2(4,0)}
    .elsewhen(io.inst_d2e.srli ) 	{ bus_i.data :=io.In_rs1 >> io.In_rs2(4,0)}
    .elsewhen(io.inst_d2e.srai ) 	{ bus_i.data := (io.In_rs1.asSInt() >> io.In_rs2(4,0)).asUInt()}

    // // I Inst (Load)
    .elsewhen(io.inst_d2e.lb ) 	{ bus_i.addr :=io.In_rs1 + io.bus_d2e.imm}
    .elsewhen(io.inst_d2e.lh ) 	{ bus_i.addr :=io.In_rs1 + io.bus_d2e.imm}  
    .elsewhen(io.inst_d2e.lw ) 	{ bus_i.addr :=io.In_rs1 + io.bus_d2e.imm}  
    .elsewhen(io.inst_d2e.lbu ) { bus_i.addr :=io.In_rs1 + io.bus_d2e.imm}  
    .elsewhen(io.inst_d2e.lhu ) { bus_i.addr :=io.In_rs1 + io.bus_d2e.imm}

    // // I Inst (jalr)
    .elsewhen(io.inst_d2e.jalr){DO_flush_reg := true.B
                                DO_brunch_reg := true.B
                                bus_i.data := {io.bus_d2e.pc  + "h00000004".U}
                                bus_i.addr := (io.In_rs1 + io.bus_d2e.imm)&"hFFFFFFFE".U}   

    // // I Inst (Control Status Reg)
    // (io.inst_d2e.fence ) ->     
    // (io.inst_d2e.fencei ) ->    

    // (io.inst_d2e.ecall ) ->     
    // (io.inst_d2e.ebreak ) ->    
    // (io.inst_d2e.csrrw ) ->     
    // (io.inst_d2e.csrrs ) ->     
    // (io.inst_d2e.csrrc ) ->     
    // (io.inst_d2e.csrrwi ) ->    
    // (io.inst_d2e.csrrsi ) ->    
    // (io.inst_d2e.csrrci ) ->    

    // // S inst
    .elsewhen(io.inst_d2e.sb ) {bus_i.data := io.In_rs2 & "h000000ff".U
                                bus_i.addr := io.In_rs1 + io.bus_d2e.imm}
    .elsewhen(io.inst_d2e.sh ) {bus_i.data := io.In_rs2 & "h0000ffff".U
                                bus_i.addr := io.In_rs1 + io.bus_d2e.imm}
    .elsewhen(io.inst_d2e.sw ) {bus_i.data := io.In_rs2
                                bus_i.addr := io.In_rs1 + io.bus_d2e.imm}
    // B Inst
    .elsewhen(io.inst_d2e.beq) {DO_flush_reg := (io.In_rs1 === io.In_rs2)
                                DO_brunch_reg := (io.In_rs1 === io.In_rs2)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(io.inst_d2e.bne) {DO_flush_reg := (io.In_rs1 =/= io.In_rs2)
                                DO_brunch_reg := (io.In_rs1 =/= io.In_rs2)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(io.inst_d2e.blt) {DO_flush_reg := ((io.In_rs1.asSInt() <= io.In_rs2.asSInt()).asBool())
                                DO_brunch_reg := ((io.In_rs1.asSInt() <= io.In_rs2.asSInt()).asBool())
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(io.inst_d2e.bge) {DO_flush_reg := ((io.In_rs1.asSInt() > io.In_rs2.asSInt()).asBool())
                                DO_brunch_reg := ((io.In_rs1.asSInt() > io.In_rs2.asSInt()).asBool())
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(io.inst_d2e.bltu){DO_flush_reg := (io.In_rs1 <= io.In_rs2)
                                DO_brunch_reg := (io.In_rs1 <= io.In_rs2)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)} 

    .elsewhen(io.inst_d2e.bgeu){DO_flush_reg := (io.In_rs1 > io.In_rs2)
                                DO_brunch_reg := (io.In_rs1 > io.In_rs2)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}  

    // U Inst
    .elsewhen(io.inst_d2e.lui)  {bus_i.data := io.bus_d2e.imm}
    .elsewhen(io.inst_d2e.auipc){bus_i.data := io.bus_d2e.pc + io.bus_d2e.imm}     
        
    // J Inst (修正)
    .elsewhen(io.inst_d2e.jal) {DO_flush_reg := true.B
                                DO_brunch_reg := true.B
                                bus_i.addr := io.bus_d2e.pc + io.bus_d2e.imm
                                bus_i.data := io.bus_d2e.pc + "h00000004".U} 
    // 例外(未定義命令)
    //.otherwise()

    io.bus_e2m := bus_i
    io.DO_flush := DO_flush_reg
    io.DO_brunch := DO_brunch_reg
}