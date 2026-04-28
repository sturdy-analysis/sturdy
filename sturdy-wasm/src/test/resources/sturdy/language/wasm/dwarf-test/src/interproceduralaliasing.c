//
// Created by flo on 3/27/26.
//
int g = 5;

void write_global(int *p, int v) {
    *p = v;   // STORE
}

int interprocedural_global_write() {
    int x = g;            // LOAD
    write_global(&g, x+1);// STORE via call
    return g;
}