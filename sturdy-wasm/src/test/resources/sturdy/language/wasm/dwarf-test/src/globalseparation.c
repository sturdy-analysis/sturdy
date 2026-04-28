//
// Created by flo on 3/27/26.
//
int g1 = 1;
int g2 = 2;

int multiple_globals_separation() {
    int x = g1;  // LOAD g1
    g2 = x;      // STORE g2
    return g2;
}

int _start() {
    return multiple_globals_separation();
}