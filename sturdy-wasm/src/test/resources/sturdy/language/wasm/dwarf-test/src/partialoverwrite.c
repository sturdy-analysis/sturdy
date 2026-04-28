//
// Created by flo on 3/27/26.
//
int g = 0x11223344;

int partial_byte_overwrite() {
    char *p = (char*)&g;
    p[1] = 0xFF;   // STORE (byte)
    return g;      // LOAD (full)
}

int _start() {
    return partial_byte_overwrite();
}