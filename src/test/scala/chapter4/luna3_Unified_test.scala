package chapter4

import chisel3.iotesters._

class RV32_Unified_test extends ChiselFlatSpec{
    behavior of "RV32_Unified"

    it should "RV32_Unified test" in{
        Driver.execute(Array(
            "-tn=RV32_Unified", 
            "-td=test_run_dir/chapter4/RV32_Unified",
            "-tgvo=on", "-tbn=verilator"), () => new RV32_Unified){
                c => new PeekPokeTester(c){
                    reset()
                    step(1800)
                    // step(1)
                    expect(c.io.PC_f2d, 0x00000028)
                }
            }should be (true)
    }
}