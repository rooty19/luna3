SECTIONS{
    . = 0x00000000;
    .text.init : {* (.text.init)}
    .text ALIGN(0x800) : {*(.text)}
    .data ALIGN(0x800) : {*(.data)}
    .bss : {*(.bss)}
    end = .;
    _end = .;
}