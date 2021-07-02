CXX = riscv32-unknown-elf-g++
LD	= riscv32-unknown-elf-ld
OBJCOPY = riscv32-unknown-elf-objcopy
OBJDUMP = riscv32-unknown-elf-objdump
CXXFLAGS = -march=rv32im
LDFLAGS = -L/opt/riscv/elf/lib -Tlink.ld -static -e 0x00000000
TargetP = imem.txt
SourceP = fibonacci

$ (TargetP) : 
	g++ -o bin2hex bin2hex.cpp 
	$(CXX) $(CXXFLAGS) -c -g0 -o $(SourceP).o $(SourceP).cpp
	$(CXX) $(CXXFLAGS) -static -c -g0 -o $(SourceP)_spike $(SourceP).cpp
	$(CXX) $(CXXFLAGS) -c -g0 -o start.o start.s
	$(LD) start.o $(SourceP).o $(LDFLAGS) -o $(SourceP)
	$(OBJCOPY) -O binary $(SourceP) $(SourceP).bin
	./bin2hex $(SourceP).bin > $(TargetP) 
	$(OBJDUMP) -S $(SourceP)

clean:
	rm start.o $(SourceP).o $(SourceP).bin $(TargetP) $(SourceP)_spike $(SourceP) bin2hex pk
