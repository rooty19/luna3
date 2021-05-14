package chapter4

//import chisel3._ 
import chisel3.iotesters._

class RV32_Fetch_test extends ChiselFlatSpec{
    behavior of "RV32_Fetch"

    it should "RV32_Fetch test" in{
        Driver.execute(Array(
            "-tn=RV32_Fetch", 
            "-td=test_run_dir/chapter4/RV32_Fetch",
            "-tgvo=on", "-tbn=verilator"), () => new RV32_Fetch){
                c => new PeekPokeTester(c){
                    reset()
                    poke(c.io.PC_brunch, 0x00000100)
                    poke(c.io.DO_brunch, false)
                    poke(c.io.DO_next, false)
                    step(1)
                    expect(c.io.PC_now, 0x00000000)
                    poke(c.io.DO_next, true)
                    step(1)
                    expect(c.io.PC_now, 0x00000004)
                    step(1)
                    expect(c.io.PC_now, 0x00000008)
                    step(1)
                    poke(c.io.DO_brunch, true)
                    step(1)
                    expect(c.io.PC_now, 0x00000100)
                    poke(c.io.DO_brunch, false)
                    step(1)
                    expect(c.io.PC_now, 0x00000104)
                    step(5)
                }
            }should be (true)
    }
}