package chapter4

import chisel3._
import chisel3.util._

class Registerfile extends Module{
    val io = IO(new Bundle{
        val ra1 = Input(UInt(5.W))
        val ra2 = Input(UInt(5.W))
        
        val rs1 = Output(UInt(32.W))
        val rs2 = Output(UInt(32.W))        

        val wa1 = Input(UInt(5.W))
        val wd1 = Input(UInt(32.W))
        val we1 = Input(Bool())
    })
    
    val mem = Mem(32,UInt(32.W))
    
    io.rs1 := mem(io.ra1)
    io.rs2 := mem(io.ra2)

    when(io.we1){
        if(io.wa1 != "b00000".U){
            mem(io.wa1) := io.wd1
        }
    }
}


class RV32_Decode extends Module {
    val io = IO(new Bundle{
        val PC_f2d = Input(UInt(32.W))
        val inst_f2d = Input(UInt(32.W))
        val DO_flush = Input(Bool())
        val regf_wa1 = Input(UInt(5.W))
        val regf_wd1 = Input(UInt(32.W))
        val regf_we1 = Input(Bool())
        
        val bus_d2e = Output(new luna3_Bus_Set)
        val inst_d2e = Output(new luna3_RV32I_instruct_set)
    })

    val busi = Wire(new luna3_Bus_Set)
    val regf = Module(new Registerfile)

    busi := 0.U.asTypeOf(new luna3_Bus_Set)
    // Divide Instruction
    busi.opcode := io.inst_f2d(6,0)
    busi.rd     := io.inst_f2d(11,7)
    busi.funct3 := io.inst_f2d(14,12)
    busi.ra1    := io.inst_f2d(19,15)
    busi.ra2    := io.inst_f2d(24,20)
    busi.funct7_R := io.inst_f2d(31,25)
    busi.pc     := io.PC_f2d
    busi.inst   := io.inst_f2d

    // Instruction -> Regfile
    regf.io.ra1 := busi.ra1
    regf.io.ra2 := busi.ra2
    busi.rs1    := regf.io.rs1
    busi.rs2    := regf.io.rs2
    regf.io.wa1   := io.regf_wa1
    regf.io.wd1   := io.regf_wd1
    regf.io.we1   := io.regf_we1

    busi.imm      := 0.U

    // Immediate
    val imm_I_raw    = io.inst_f2d(31,20).asUInt()
    val imm_S_raw    = Cat(io.inst_f2d(31,25), io.inst_f2d(11,7)).asUInt()
    val imm_B_raw    = Cat(io.inst_f2d(31,31), io.inst_f2d(7,7), io.inst_f2d(30,25), io.inst_f2d(11,8), 0.U(1.W)).asUInt()
    val imm_U_raw    = Cat(io.inst_f2d(31,12).asUInt(), 0.U(20.W))
    val imm_J_raw    = Cat(io.inst_f2d(31,31), io.inst_f2d(19,12), io.inst_f2d(20,20), io.inst_f2d(30,21), 0.U(1.W)).asUInt()

    val imm_I = Wire(SInt(32.W))
    val imm_S = Wire(SInt(32.W))
    val imm_U = Wire(SInt(32.W))
    val imm_B = Wire(SInt(32.W))
    val imm_J = Wire(SInt(32.W))      
    imm_I := imm_I_raw.asSInt()
    imm_S := imm_S_raw.asSInt()
    imm_U := imm_U_raw.asSInt()
    imm_B := imm_B_raw.asSInt()
    imm_J := imm_J_raw.asSInt()

    // Pipeline
    val RV32I_InstType = new RV32I_InstType_B
    val busi_Reg = RegInit(0.U.asTypeOf(new luna3_Bus_Set))
    when(io.DO_flush){
        busi_Reg := 0.U.asTypeOf(new luna3_Bus_Set)
        busi_Reg.inst := "h00000013".U
        busi_Reg.opcode := "h13".U
        busi_Reg.rd := 0.U
        busi_Reg.ra1 := 0.U
        busi_Reg.ra2 := 0.U
    }.otherwise{
        busi_Reg := busi
        // Forwarding
        when((busi.ra1 === io.regf_wa1)&(io.regf_wa1 =/= 0.U)&(io.regf_we1)){busi_Reg.rs1 := io.regf_wd1}
        when((busi.ra2 === io.regf_wa1)&(io.regf_wa1 =/= 0.U)&(io.regf_we1)){busi_Reg.rs2 := io.regf_wd1}

        when(busi.opcode === RV32I_InstType.op_I_ALU)        {busi_Reg.imm := imm_I.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_I_LOAD)  {busi_Reg.imm := imm_I.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_I_FRNCE) {busi_Reg.imm := imm_I.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_I_CSR)   {busi_Reg.imm := imm_I.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_I_JALR)  {busi_Reg.imm := imm_I.asUInt()}        
        .elsewhen(busi.opcode === RV32I_InstType.op_S)       {busi_Reg.imm := imm_S.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_B)       {busi_Reg.imm := imm_B.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_U_LUI)   {busi_Reg.imm := imm_U.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_U_AUIPC) {busi_Reg.imm := imm_U.asUInt()}
        .elsewhen(busi.opcode === RV32I_InstType.op_J)       {busi_Reg.imm := imm_J.asUInt()}
    }
    io.bus_d2e := busi_Reg


    //val RV32I_InstSet = Wire(new luna3_RV32I_instruct_set)
    val RV32I_InstSet_Reg = RegInit(0.U.asTypeOf(new luna3_RV32I_instruct_set))
    //.otherwise    {RV32I_InstSet_Reg := RV32I_InstSet}
    

    // Instrunction Decode
    RV32I_InstSet_Reg.add       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b000".U )
    RV32I_InstSet_Reg.sub       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0100000".U & busi.funct3 === "b000".U )
    RV32I_InstSet_Reg.sll       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b001".U )
    RV32I_InstSet_Reg.slt       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b010".U )
    RV32I_InstSet_Reg.sltu      := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b011".U )
    RV32I_InstSet_Reg.xor       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b100".U )
    RV32I_InstSet_Reg.srl       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b101".U )
    RV32I_InstSet_Reg.sra       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0100000".U & busi.funct3 === "b101".U )
    RV32I_InstSet_Reg.or        := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b110".U )
    RV32I_InstSet_Reg.and       := (busi.opcode === RV32I_InstType.op_R & busi.funct7_R === "b0000000".U & busi.funct3 === "b111".U )

    RV32I_InstSet_Reg.addi      := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b000".U)
    RV32I_InstSet_Reg.slti      := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b010".U)
    RV32I_InstSet_Reg.sltiu     := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b011".U)
    RV32I_InstSet_Reg.xori      := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b100".U)
    RV32I_InstSet_Reg.ori       := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b110".U)
    RV32I_InstSet_Reg.andi      := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b111".U)
    RV32I_InstSet_Reg.slli      := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b001".U & busi.funct7_R === "b0000000".U)
    RV32I_InstSet_Reg.srli      := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b101".U & busi.funct7_R === "b0000000".U)
    RV32I_InstSet_Reg.srai      := (busi.opcode === RV32I_InstType.op_I_ALU & busi.funct3 === "b101".U & busi.funct7_R === "b0100000".U)

    RV32I_InstSet_Reg.lb        := (busi.opcode === RV32I_InstType.op_I_LOAD & busi.funct3 === "b000".U)
    RV32I_InstSet_Reg.lh        := (busi.opcode === RV32I_InstType.op_I_LOAD & busi.funct3 === "b001".U)
    RV32I_InstSet_Reg.lw        := (busi.opcode === RV32I_InstType.op_I_LOAD & busi.funct3 === "b010".U)
    RV32I_InstSet_Reg.lbu       := (busi.opcode === RV32I_InstType.op_I_LOAD & busi.funct3 === "b100".U)
    RV32I_InstSet_Reg.lhu       := (busi.opcode === RV32I_InstType.op_I_LOAD & busi.funct3 === "b101".U)

    RV32I_InstSet_Reg.jalr      := (busi.opcode === RV32I_InstType.op_I_JALR)

    RV32I_InstSet_Reg.fence     := (busi.opcode === RV32I_InstType.op_I_FRNCE & busi.funct3 === "b000".U)
    RV32I_InstSet_Reg.fencei    := (busi.opcode === RV32I_InstType.op_I_FRNCE & busi.funct3 === "b001".U)
        
    RV32I_InstSet_Reg.ecall     := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b000".U & imm_I_raw === 0.U)
    RV32I_InstSet_Reg.ebreak    := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b000".U & imm_I_raw === 1.U)
    RV32I_InstSet_Reg.csrrw     := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b001".U)
    RV32I_InstSet_Reg.csrrs     := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b010".U)
    RV32I_InstSet_Reg.csrrc     := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b011".U)
    RV32I_InstSet_Reg.csrrwi    := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b101".U)
    RV32I_InstSet_Reg.csrrsi    := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b110".U)
    RV32I_InstSet_Reg.csrrci    := (busi.opcode === RV32I_InstType.op_I_CSR & busi.funct3 === "b111".U)

    RV32I_InstSet_Reg.sb        := (busi.opcode === RV32I_InstType.op_S & busi.funct3 === "b000".U)
    RV32I_InstSet_Reg.sh        := (busi.opcode === RV32I_InstType.op_S & busi.funct3 === "b001".U)
    RV32I_InstSet_Reg.sw        := (busi.opcode === RV32I_InstType.op_S & busi.funct3 === "b010".U)

    RV32I_InstSet_Reg.beq       := (busi.opcode === RV32I_InstType.op_B & busi.funct3 === "b000".U)
    RV32I_InstSet_Reg.bne       := (busi.opcode === RV32I_InstType.op_B & busi.funct3 === "b001".U)
    RV32I_InstSet_Reg.blt       := (busi.opcode === RV32I_InstType.op_B & busi.funct3 === "b100".U)
    RV32I_InstSet_Reg.bge       := (busi.opcode === RV32I_InstType.op_B & busi.funct3 === "b101".U)
    RV32I_InstSet_Reg.bltu      := (busi.opcode === RV32I_InstType.op_B & busi.funct3 === "b110".U)
    RV32I_InstSet_Reg.bgeu      := (busi.opcode === RV32I_InstType.op_B & busi.funct3 === "b111".U)

    RV32I_InstSet_Reg.lui       := (busi.opcode === RV32I_InstType.op_U_LUI)
    RV32I_InstSet_Reg.auipc     := (busi.opcode === RV32I_InstType.op_U_AUIPC)
        
    RV32I_InstSet_Reg.jal       := (busi.opcode === RV32I_InstType.op_J)
    
    when(io.DO_flush){RV32I_InstSet_Reg := 0.U.asTypeOf(new luna3_RV32I_instruct_set)
                      RV32I_InstSet_Reg.addi := true.B}
    //Pipeline
    io.inst_d2e := RV32I_InstSet_Reg



}


