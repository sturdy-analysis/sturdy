//
// Created by flo on 2/5/26.
//

#include "../../stdlib.h"

int aseed = 4;

__attribute__((noinline))
int functionWithArrayLocal() {
    const int size = 8;
    volatile int array[size];
    for (int i = 0; i < size; i++) {
        array[i] = i + aseed * (i % aseed);
    }
    blackhole_void_p(array);
    return array[aseed % size];
}

int _start() {
    return functionWithArrayLocal();
}