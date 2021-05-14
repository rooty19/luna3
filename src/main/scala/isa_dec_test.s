.section .text
.global _main
_main:
        li a0, 0x0
        li a1, 0x0
        li a2, 0x1
        li a3, -1
        li a4, 10
        li a5, 0x0

_init:
        li a1, 0x0

_add:
        addi a1, a1, 0x1
        beq  a1, a2, _back
	nop
        j _add

_back:
        sub a2, a2, a3
        bne a2, a4, _init
        nop
_stop
        j _stop
