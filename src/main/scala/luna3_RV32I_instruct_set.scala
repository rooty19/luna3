package chapter4

import chisel3._
import chisel3.util._

// constant and bus define
class luna3_Bus_Set extends Bundle{
    val pc = UInt(32.W)
    val inst = UInt(32.W)
    val opcode = UInt(7.W)
    val rd     = UInt(5.W)
    val funct3 = UInt(3.W)
    val ra1     = UInt(5.W)
    val ra2     = UInt(5.W)
    val funct7_R= UInt(7.W)
    val rs1     = UInt(32.W)
    val rs2     = UInt(32.W)
    val imm     = UInt(32.W)
    val addr    = UInt(32.W)
    val data    = UInt(32.W)    
}

class luna3_Forwarding_Line extends Bundle{
    val ra1_enable = Bool()
    val ra2_enable = Bool()
    val rd_enable = Bool()
}

class RV32I_InstType_B extends Bundle{
    val op_R        = "b0110011".U
    val op_I_ALU    = "b0010011".U
    val op_I_LOAD   = "b0000011".U
    val op_I_FRNCE  = "b0001111".U
    val op_I_CSR    = "b1110111".U
    val op_I_JALR   = "b1100111".U
    val op_S        = "b0100011".U
    val op_B        = "b1100011".U
    val op_U_LUI    = "b0110111".U
    val op_U_AUIPC  = "b0010111".U    
    val op_J        = "b1101111".U
    }

class luna3_RV32I_instruct_set extends Bundle{
    // R Inst
        val add = Bool()
        val sub = Bool()
        val sll = Bool()
        val slt = Bool()
        val sltu = Bool()
        val xor = Bool()
        val srl = Bool()
        val sra = Bool()
        val or = Bool()
        val and = Bool()

    // I Inst (ALU)
        val addi = Bool()
        val slti = Bool()
        val sltiu = Bool()
        val xori = Bool()
        val ori = Bool()
        val andi = Bool()
        val slli = Bool()
        val srli = Bool()
        val srai = Bool()

    // I Inst (Load)
        val lb = Bool()
        val lh = Bool()
        val lw = Bool()
        val lbu = Bool()
        val lhu = Bool()

    // I Inst (jalr)
        val jalr = Bool()

    // I Inst (Control Status Reg)
        val fence = Bool()
        val fencei = Bool()

        val ecall = Bool()
        val ebreak = Bool()
        val csrrw = Bool()
        val csrrs = Bool()
        val csrrc = Bool()
        val csrrwi = Bool()
        val csrrsi = Bool()
        val csrrci = Bool()

    // S inst
        val sb = Bool()
        val sh = Bool()
        val sw = Bool()

    // B Inst
        val beq = Bool()
        val bne = Bool()
        val blt = Bool()
        val bge = Bool()
        val bltu = Bool()
        val bgeu = Bool()

    // U Inst
        val lui = Bool()
        val auipc = Bool()
        
    // J Inst
        val jal  = Bool()
}