//
// Created by flo on 3/27/26.
//
int g[2] = {1, 2};

int pointer_array_offset_access() {
    int *p = g;
    int x = *(p + 1);  // LOAD
    *(p + 0) = x;      // STORE
    return x;
}

int _start() {
    return pointer_array_offset_access();
}