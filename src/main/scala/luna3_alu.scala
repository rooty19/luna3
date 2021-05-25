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
        val wa1_wb = Input(UInt(5.W))
        val wd1_wb = Input(UInt(32.W))
        val opcode_wb = Input(UInt(6.W))

        // Pipeline Stall
        val DO_stall_count = Output(UInt(2.W))
        val e2m_bne = Output(Bool())
    })
    io.e2m_bne := io.inst_e2m.bne
// Forwarding
    val luna3_FA_Exec = Module(new luna3_Forwarding_Set)
        luna3_FA_Exec.io.opcode := io.bus_d2e.opcode
        val luna3_ra1EN_Exec = luna3_FA_Exec.io.ra1_enable
        val luna3_ra2EN_Exec = luna3_FA_Exec.io.ra2_enable
        val luna3_rdEN_Exec = luna3_FA_Exec.io.rd_enable

    val luna3_FA_Mem = Module(new luna3_Forwarding_Set)
        luna3_FA_Mem.io.opcode := io.bus_e2m.opcode
        val luna3_ra1EN_Mem = luna3_FA_Mem.io.ra1_enable
        val luna3_ra2EN_Mem = luna3_FA_Mem.io.ra2_enable
        val luna3_rdEN_Mem = luna3_FA_Mem.io.rd_enable

    val luna3_FA_WB = Module(new luna3_Forwarding_Set)
        luna3_FA_WB.io.opcode := io.opcode_wb
        val luna3_ra1EN_WB = luna3_FA_WB.io.ra1_enable
        val luna3_ra2EN_WB = luna3_FA_WB.io.ra2_enable
        val luna3_rdEN_WB = luna3_FA_WB.io.rd_enable

    val DO_stall_count_reg = RegInit("b10".U(2.W))
    val DO_stall_count_reg_Next = RegInit("b00".U(2.W))
    DO_stall_count_reg_Next := DO_stall_count_reg
    val rs2_match = Cat(luna3_rdEN_Mem, luna3_rdEN_WB, (luna3_ra2EN_Exec & luna3_rdEN_Mem)&(io.bus_d2e.ra2 === io.bus_e2m.rd)&(io.bus_e2m.rd =/= 0.U)&(DO_stall_count_reg_Next === "b10".U), (luna3_ra2EN_Exec & luna3_rdEN_WB)&(io.bus_d2e.ra2 === io.wa1_wb)&(io.wa1_wb =/= 0.U))
    val rs1_match = Cat(luna3_rdEN_Mem, luna3_rdEN_WB, (luna3_ra1EN_Exec & luna3_rdEN_Mem)&(io.bus_d2e.ra1 === io.bus_e2m.rd)&(io.bus_e2m.rd =/= 0.U)&(DO_stall_count_reg_Next === "b10".U), (luna3_ra1EN_Exec & luna3_rdEN_WB)&(io.bus_d2e.ra1 === io.wa1_wb)&(io.wa1_wb =/= 0.U))

    val In_rs2 = MuxCase(io.bus_d2e.rs2, Array(
        (rs2_match === "b0101".U) -> io.wd1_wb,
        (rs2_match === "b0111".U) -> io.wd1_wb,
        (rs2_match === "b1010".U) -> io.bus_e2m.data,
        (rs2_match === "b1011".U) -> io.bus_e2m.data,
        (rs2_match === "b1101".U) -> io.wd1_wb,
        (rs2_match === "b1110".U) -> io.bus_e2m.data,
        (rs2_match === "b1111".U) -> io.bus_e2m.data,
    ))

    val In_rs1 = MuxCase(io.bus_d2e.rs1, Array(
        (rs1_match === "b0101".U) -> io.wd1_wb,
        (rs1_match === "b0111".U) -> io.wd1_wb,
        (rs1_match === "b1010".U) -> io.bus_e2m.data,
        (rs1_match === "b1011".U) -> io.bus_e2m.data,
        (rs1_match === "b1101".U) -> io.wd1_wb,
        (rs1_match === "b1110".U) -> io.bus_e2m.data,
        (rs1_match === "b1111".U) -> io.bus_e2m.data,
    ))
// End of Forwaring
    val bus_i = RegInit(0.U.asTypeOf(new luna3_Bus_Set))  

    val buf_rs1 = RegInit(0.U(32.W))
    val buf_rs2 = RegInit(0.U(32.W))
    val mux_rs1 = RegInit(false.B)
    val mux_rs2 = RegInit(false.B)    
    val DO_wd2alu = RegInit(false.B)

// Imm
    val DO_flush_reg = RegInit(false.B)
    val DO_brunch_reg = RegInit(false.B)
    DO_flush_reg := false.B
    DO_brunch_reg := false.B

    val inst_i_reg = RegInit(0.U.asTypeOf(new luna3_RV32I_instruct_set))
    val inst_buf = Reg(new luna3_RV32I_instruct_set)
    inst_buf := io.inst_d2e

    val inst_seled = Mux(mux_rs1, inst_buf, io.inst_d2e)

    when(DO_flush_reg)                       {inst_i_reg := (0.U.asTypeOf(new luna3_RV32I_instruct_set))}
    .otherwise                               {inst_i_reg := inst_seled}

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
    DO_stall_count_reg := "b10".U

    DO_wd2alu := false.B
    buf_rs1 := 0.U
    buf_rs2 := 0.U
    mux_rs1 := false.B
    mux_rs2 := false.B

    val In_rs1_m = Mux(mux_rs2, buf_rs1, In_rs1)
    val In_rs2_m = Mux(mux_rs2, buf_rs2, In_rs2)    

    // when(DO_wd2alu){
    //     buf_rs1 := In_rs1
    //     buf_rs2 := In_rs2
    //     mux_rs1 := true.B
    //     mux_rs2 := true.B
    // }
    when(DO_flush_reg)              {DO_flush_reg := false.B
                                    DO_brunch_reg := false.B
                                    bus_i.data := "h00000000".U
                                    bus_i.addr := "h00000000".U
                                    bus_i.rd := 0.U
                                    }    
    .elsewhen(DO_stall_count_reg =/= "b10".U){
        when(DO_stall_count_reg === "b00".U){
            DO_stall_count_reg := "b10".U
            mux_rs1 := true.B
            buf_rs1 := In_rs1
            buf_rs2 := In_rs2
        }.otherwise{DO_stall_count_reg := DO_stall_count_reg - 1.U}
    }
    .elsewhen(inst_seled.add )     { bus_i.data :=In_rs1_m + In_rs2_m}
    .elsewhen(inst_seled.sub ) 	{ bus_i.data :=In_rs1_m - In_rs2_m}
    .elsewhen(inst_seled.sll ) 	{ bus_i.data :=In_rs1_m << In_rs2_m(4,0)}
    .elsewhen(inst_seled.slt ) 	{ bus_i.data :=(In_rs1_m.asSInt() < In_rs2_m.asSInt()).asUInt()}
    .elsewhen(inst_seled.sltu )	{ bus_i.data :=In_rs1_m < In_rs2_m}
    .elsewhen(inst_seled.xor ) 	{ bus_i.data :=In_rs1_m ^ In_rs2_m}
    .elsewhen(inst_seled.srl ) 	{ bus_i.data :=In_rs1_m >> In_rs2_m(4,0)}
    .elsewhen(inst_seled.sra ) 	{ bus_i.data := (In_rs1_m.asSInt() >> In_rs2_m(4,0)).asUInt()}
    .elsewhen(inst_seled.or )  	{ bus_i.data :=In_rs1_m | In_rs2_m}
    .elsewhen(inst_seled.and ) 	{ bus_i.data :=In_rs1_m & In_rs2_m}

    // I Inst (ALU)
    .elsewhen(inst_seled.addi ) 	{ bus_i.data :=In_rs1_m + io.bus_d2e.imm}
    .elsewhen(inst_seled.slti ) 	{ bus_i.data :=In_rs1_m.asSInt() < io.bus_d2e.imm.asSInt().asSInt()}
    .elsewhen(inst_seled.sltiu )	{ bus_i.data :=In_rs1_m < io.bus_d2e.imm}    
    .elsewhen(inst_seled.xori ) 	{ bus_i.data :=In_rs1_m ^ io.bus_d2e.imm} 
    .elsewhen(inst_seled.ori )  	{ bus_i.data :=In_rs1_m | io.bus_d2e.imm}
    .elsewhen(inst_seled.andi ) 	{ bus_i.data :=In_rs1_m & io.bus_d2e.imm}
    .elsewhen(inst_seled.slli ) 	{ bus_i.data :=In_rs1_m << In_rs2_m(4,0)}
    .elsewhen(inst_seled.srli ) 	{ bus_i.data :=In_rs1_m >> In_rs2_m(4,0)}
    .elsewhen(inst_seled.srai ) 	{ bus_i.data := (In_rs1_m.asSInt() >> In_rs2_m(4,0)).asUInt()}

    // // I Inst (Load)
    .elsewhen(inst_seled.lb ) 	{DO_stall_count_reg := "b00".U
                                 bus_i.addr :=In_rs1_m + io.bus_d2e.imm}
    .elsewhen(inst_seled.lh ) 	{DO_stall_count_reg := "b00".U
                                 bus_i.addr :=In_rs1_m + io.bus_d2e.imm}  
    .elsewhen(inst_seled.lw ) 	{DO_stall_count_reg := "b00".U
                                 bus_i.addr :=In_rs1_m + io.bus_d2e.imm}  
    .elsewhen(inst_seled.lbu ) {DO_stall_count_reg := "b00".U
                                 bus_i.addr :=In_rs1_m + io.bus_d2e.imm}  
    .elsewhen(inst_seled.lhu ) {DO_stall_count_reg := "b00".U
                                 bus_i.addr :=In_rs1_m + io.bus_d2e.imm}

    // // I Inst (jalr)
    .elsewhen(inst_seled.jalr){DO_flush_reg := true.B
                                DO_brunch_reg := true.B
                                bus_i.data := {io.bus_d2e.pc  + "h00000004".U}
                                bus_i.addr := (In_rs1_m + io.bus_d2e.imm)&"hFFFFFFFE".U}   

    // // I Inst (Control Status Reg)
    // (inst_seled.fence ) ->     
    // (inst_seled.fencei ) ->    

    // (inst_seled.ecall ) ->     
    // (inst_seled.ebreak ) ->    
    // (inst_seled.csrrw ) ->     
    // (inst_seled.csrrs ) ->     
    // (inst_seled.csrrc ) ->     
    // (inst_seled.csrrwi ) ->    
    // (inst_seled.csrrsi ) ->    
    // (inst_seled.csrrci ) ->    

    // // S inst
    .elsewhen(inst_seled.sb ) {bus_i.data := In_rs2_m & "h000000ff".U
                                bus_i.addr := In_rs1_m + io.bus_d2e.imm}
    .elsewhen(inst_seled.sh ) {bus_i.data := In_rs2_m & "h0000ffff".U
                                bus_i.addr := In_rs1_m + io.bus_d2e.imm}
    .elsewhen(inst_seled.sw ) {bus_i.data := In_rs2_m
                                bus_i.addr := In_rs1_m + io.bus_d2e.imm}
    // B Inst
    .elsewhen(inst_seled.beq) {DO_flush_reg := (In_rs1_m === In_rs2_m)
                                DO_brunch_reg := (In_rs1_m === In_rs2_m)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(inst_seled.bne) {DO_flush_reg := (In_rs1_m =/= In_rs2_m)
                                DO_brunch_reg := (In_rs1_m =/= In_rs2_m)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(inst_seled.blt) {DO_flush_reg := ((In_rs1_m.asSInt() <= In_rs2_m.asSInt()).asBool())
                                DO_brunch_reg := ((In_rs1_m.asSInt() <= In_rs2_m.asSInt()).asBool())
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(inst_seled.bge) {DO_flush_reg := ((In_rs1_m.asSInt() > In_rs2_m.asSInt()).asBool())
                                DO_brunch_reg := ((In_rs1_m.asSInt() > In_rs2_m.asSInt()).asBool())
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}

    .elsewhen(inst_seled.bltu){DO_flush_reg := (In_rs1_m <= In_rs2_m)
                                DO_brunch_reg := (In_rs1_m <= In_rs2_m)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)} 

    .elsewhen(inst_seled.bgeu){DO_flush_reg := (In_rs1_m > In_rs2_m)
                                DO_brunch_reg := (In_rs1_m > In_rs2_m)
                                bus_i.addr := (io.bus_d2e.pc + io.bus_d2e.imm)}  

    // U Inst
    .elsewhen(inst_seled.lui)  {bus_i.data := io.bus_d2e.imm}
    .elsewhen(inst_seled.auipc){bus_i.data := io.bus_d2e.pc + io.bus_d2e.imm}     
        
    // J Inst (修正)
    .elsewhen(inst_seled.jal) {DO_flush_reg := true.B
                                DO_brunch_reg := true.B
                                bus_i.addr := io.bus_d2e.pc + io.bus_d2e.imm
                                bus_i.data := io.bus_d2e.pc + "h00000004".U} 
    // 例外(未定義命令)
    //.otherwise()

    io.bus_e2m := bus_i
    io.DO_flush := DO_flush_reg
    io.DO_brunch := DO_brunch_reg
    io.DO_stall_count := DO_stall_count_reg
}