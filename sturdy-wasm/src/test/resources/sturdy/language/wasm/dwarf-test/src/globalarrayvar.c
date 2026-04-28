//
// Created by flo on 4/7/26.
//
int globalarray[2] = {1, 2};

int array_global_offset_access() {
    int x = globalarray[1];  // LOAD
    globalarray[0] = x;      // STORE
    return x;
}

int _start() {
    return array_global_offset_access();
}