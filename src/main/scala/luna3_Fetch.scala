package chapter4

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class RV32_Fetch extends Module {
    val io=IO(new Bundle{
        val DO_stall_count = Input(UInt(2.W))
        val DO_brunch = Input(Bool())
        val PC_brunch = Input(UInt(32.W))
        val PC_now = Output(UInt(32.W))
        val inst = Output(UInt(32.W))
    })

        val PC_reg = RegInit(0x00000000.U)

        when (io.DO_stall_count === "b10".U){
            when (io.DO_brunch){
                PC_reg := io.PC_brunch
            } .otherwise {
                PC_reg := PC_reg + 0x00000004.U
            }
        }
        val Imem = Mem(0xffffff, UInt(32.W))
        loadMemoryFromFile(Imem, "src/main/scala/imem.txt")
        io.inst := Imem(PC_reg(31,2))
        io.PC_now := PC_reg

}