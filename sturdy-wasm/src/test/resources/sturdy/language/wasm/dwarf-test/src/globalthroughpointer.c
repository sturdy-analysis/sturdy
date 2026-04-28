//
// Created by flo on 3/27/26.
//
int g = 7;

int pointer_global_aliasing() {
    int *p = &g;
    int x = *p;   // LOAD
    *p = x + 1;   // STORE
    return x;
}

int _start() {
    return pointer_global_aliasing();
}