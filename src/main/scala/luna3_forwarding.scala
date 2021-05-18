package chapter4

import chisel3._
import chisel3.util._

class  luna3_Forwarding_Set extends Module{
    val io = IO(new Bundle{
        val opcode = Input(UInt(7.W))
        val ra1_enable = Output(Bool())
        val ra2_enable = Output(Bool())
        val rd_enable = Output(Bool())
    })
    val InstT = new RV32I_InstType_B
    
    io.ra1_enable :=   (io.opcode === InstT.op_R |
                        io.opcode === InstT.op_I_ALU | 
                        io.opcode === InstT.op_I_LOAD |
                        io.opcode === InstT.op_I_JALR |
                        io.opcode === InstT.op_B | 
                        io.opcode === InstT.op_S
                        )

    io.ra2_enable :=   (io.opcode === InstT.op_R | 
                        io.opcode === InstT.op_S | 
                        io.opcode === InstT.op_B
                        )

    io.rd_enable :=    (io.opcode === InstT.op_R | 
                        io.opcode === InstT.op_I_ALU |
                        io.opcode === InstT.op_I_LOAD |
                        io.opcode === InstT.op_I_JALR |
                        io.opcode === InstT.op_U_LUI |
                        io.opcode === InstT.op_U_AUIPC |
                        io.opcode === InstT.op_J |
                        io.opcode === InstT.op_S
                        )
}

// class RV32_forward extends Module{
//     val io= IO(new Bundle{
//         val bus_d2e = Input(new luna3_Bus_Set)
//         val bus_e2m = Input(new luna3_Bus_Set)
//         val opcode_wb = Input(UInt(7.W))
//         val wa1_wb = Input(UInt(5.W))
//         val wd1_wb = Input(UInt(32.W))
//         val In_rs1 = Output(UInt(32.W))
//         val In_rs2 = Output(UInt(32.W))
//     })

// val luna3_FA_Exec = Module(new luna3_Forwarding_Set)
//     luna3_FA_Exec.io.opcode := io.bus_d2e.opcode
//     val luna3_ra1EN_Exec = luna3_FA_Exec.io.ra1_enable
//     val luna3_ra2EN_Exec = luna3_FA_Exec.io.ra2_enable
//     val luna3_rdEN_Exec = luna3_FA_Exec.io.rd_enable

// val luna3_FA_Mem = Module(new luna3_Forwarding_Set)
//     luna3_FA_Mem.io.opcode := io.bus_e2m.opcode
//     val luna3_ra1EN_Mem = luna3_FA_Mem.io.ra1_enable
//     val luna3_ra2EN_Mem = luna3_FA_Mem.io.ra2_enable
//     val luna3_rdEN_Mem = luna3_FA_Mem.io.rd_enable

// val luna3_FA_WB = Module(new luna3_Forwarding_Set)
//     luna3_FA_WB.io.opcode := io.opcode_wb
//     val luna3_ra1EN_WB = luna3_FA_WB.io.ra1_enable
//     val luna3_ra2EN_WB = luna3_FA_WB.io.ra2_enable
//     val luna3_rdEN_WB = luna3_FA_WB.io.rd_enable

// val rs2_match = Cat(luna3_rdEN_Mem, luna3_rdEN_WB, (luna3_ra2EN_Exec & luna3_rdEN_WB)&(io.bus_d2e.ra2 === io.bus_e2m.rd)&(io.bus_e2m.rd =/= 0.U), (luna3_ra2EN_Exec & luna3_rdEN_Mem)&(io.bus_d2e.ra2 === io.wa1_wb)&(io.bus_e2m.rd =/= 0.U))
// val rs1_match = Cat(luna3_rdEN_Mem, luna3_rdEN_WB, (luna3_ra1EN_Exec & luna3_rdEN_WB)&(io.bus_d2e.ra1 === io.bus_e2m.rd)&(io.bus_e2m.rd =/= 0.U), (luna3_ra1EN_Exec & luna3_rdEN_Mem)&(io.bus_d2e.ra1 === io.wa1_wb)&(io.bus_e2m.rd =/= 0.U))

//     io.In_rs2 := MuxCase(io.bus_d2e.rs2, Array(
//         (rs2_match === "b0101".U) -> io.wd1_wb,
//         (rs2_match === "b0111".U) -> io.wd1_wb,
//         (rs2_match === "b1010".U) -> io.bus_e2m.data,
//         (rs2_match === "b1011".U) -> io.bus_e2m.data,
//         (rs2_match === "b1101".U) -> io.wd1_wb,
//         (rs2_match === "b1110".U) -> io.bus_e2m.data,
//         (rs2_match === "b1111".U) -> io.bus_e2m.data,
//     ))

//     io.In_rs1 := MuxCase(io.bus_d2e.rs1, Array(
//         (rs1_match === "b0101".U) -> io.wd1_wb,
//         (rs1_match === "b0111".U) -> io.wd1_wb,
//         (rs1_match === "b1010".U) -> io.bus_e2m.data,
//         (rs1_match === "b1011".U) -> io.bus_e2m.data,
//         (rs1_match === "b1101".U) -> io.wd1_wb,
//         (rs1_match === "b1110".U) -> io.bus_e2m.data,
//         (rs1_match === "b1111".U) -> io.bus_e2m.data,
//     ))
// }