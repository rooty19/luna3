.section .text.init;
.align 6;
.globl _start;

_start:
    li sp, 0x7fbebda0
    j main
