package chapter4

import chisel3._
import chisel3.util._

class RV32_Instruction_Set_Debug extends Module {
    val io=IO(new Bundle{
        val instr = Input(UInt(32.W))
        val RV32I_InstSet = Output(new luna3_RV32I_instruct_set)
    })

    val opcode = io.instr(6,0)
    val rd     = io.instr(11,7)
    val funct3 = io.instr(14,12)
    val ra1    = io.instr(19,15)
    val ra2    = io.instr(24,20)
    val funct7_R = io.instr(31,25)
    val imm_I_raw = Cat(io.instr(31,20), 0.U(20.W))

    val RV32I_InstType = (new RV32I_InstType_B)

    io.RV32I_InstSet.add       := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b000".U )
    io.RV32I_InstSet.sub       := (opcode === RV32I_InstType.op_R & funct7_R === "b0100000".U & funct3 === "b000".U )
    io.RV32I_InstSet.sll       := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b001".U )
    io.RV32I_InstSet.slt       := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b010".U )
    io.RV32I_InstSet.sltu      := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b011".U )
    io.RV32I_InstSet.xor       := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b100".U )
    io.RV32I_InstSet.srl       := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b101".U )
    io.RV32I_InstSet.sra       := (opcode === RV32I_InstType.op_R & funct7_R === "b0100000".U & funct3 === "b101".U )
    io.RV32I_InstSet.or        := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b110".U )
    io.RV32I_InstSet.and       := (opcode === RV32I_InstType.op_R & funct7_R === "b0000000".U & funct3 === "b111".U )

    io.RV32I_InstSet.addi      := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b000".U)
    io.RV32I_InstSet.slti      := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b010".U)
    io.RV32I_InstSet.sltiu     := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b011".U)
    io.RV32I_InstSet.xori      := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b100".U)
    io.RV32I_InstSet.ori       := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b110".U)
    io.RV32I_InstSet.andi      := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b111".U)
    io.RV32I_InstSet.slli      := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b001".U & funct7_R === "b0000000".U)
    io.RV32I_InstSet.srli      := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b101".U & funct7_R === "b0000000".U)
    io.RV32I_InstSet.srai      := (opcode === RV32I_InstType.op_I_ALU & funct3 === "b101".U & funct7_R === "b0100000".U)

    io.RV32I_InstSet.lb        := (opcode === RV32I_InstType.op_I_LOAD & funct3 === "b000".U)
    io.RV32I_InstSet.lh        := (opcode === RV32I_InstType.op_I_LOAD & funct3 === "b001".U)
    io.RV32I_InstSet.lw        := (opcode === RV32I_InstType.op_I_LOAD & funct3 === "b010".U)
    io.RV32I_InstSet.lbu       := (opcode === RV32I_InstType.op_I_LOAD & funct3 === "b100".U)
    io.RV32I_InstSet.lhu       := (opcode === RV32I_InstType.op_I_LOAD & funct3 === "b101".U)

    io.RV32I_InstSet.jalr      := (opcode === RV32I_InstType.op_I_JALR)

    io.RV32I_InstSet.fence     := (opcode === RV32I_InstType.op_I_FRNCE & funct3 === "b000".U)
    io.RV32I_InstSet.fencei    := (opcode === RV32I_InstType.op_I_FRNCE & funct3 === "b001".U)
        
    io.RV32I_InstSet.ecall     := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b000".U & imm_I_raw === 0.U)
    io.RV32I_InstSet.ebreak    := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b000".U & imm_I_raw === 1.U)
    io.RV32I_InstSet.csrrw     := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b001".U)
    io.RV32I_InstSet.csrrs     := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b010".U)
    io.RV32I_InstSet.csrrc     := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b011".U)
    io.RV32I_InstSet.csrrwi    := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b101".U)
    io.RV32I_InstSet.csrrsi    := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b110".U)
    io.RV32I_InstSet.csrrci    := (opcode === RV32I_InstType.op_I_CSR & funct3 === "b111".U)

    io.RV32I_InstSet.sb        := (opcode === RV32I_InstType.op_S & funct3 === "b000".U)
    io.RV32I_InstSet.sh        := (opcode === RV32I_InstType.op_S & funct3 === "b001".U)
    io.RV32I_InstSet.sw        := (opcode === RV32I_InstType.op_S & funct3 === "b010".U)

    io.RV32I_InstSet.beq       := (opcode === RV32I_InstType.op_B & funct3 === "b000".U)
    io.RV32I_InstSet.bne       := (opcode === RV32I_InstType.op_B & funct3 === "b001".U)
    io.RV32I_InstSet.blt       := (opcode === RV32I_InstType.op_B & funct3 === "b100".U)
    io.RV32I_InstSet.bge       := (opcode === RV32I_InstType.op_B & funct3 === "b101".U)
    io.RV32I_InstSet.bltu      := (opcode === RV32I_InstType.op_B & funct3 === "b110".U)
    io.RV32I_InstSet.bgeu      := (opcode === RV32I_InstType.op_B & funct3 === "b111".U)

    io.RV32I_InstSet.lui       := (opcode === RV32I_InstType.op_U_LUI)
    io.RV32I_InstSet.auipc     := (opcode === RV32I_InstType.op_U_AUIPC)
        
    io.RV32I_InstSet.jal       := (opcode === RV32I_InstType.op_J)
}