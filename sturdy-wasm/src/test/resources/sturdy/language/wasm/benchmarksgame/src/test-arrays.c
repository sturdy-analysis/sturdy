#include "../../stdlib.h"

int _start() {
    int x[10];
    for(int i = 0; i < 10; i++) {
        x[i] = i;
    }
    int y[10];
    for(int i = 0; i < 10; i++) {
        y[i] = x[i] * 2;
    }
    return y[9];
}