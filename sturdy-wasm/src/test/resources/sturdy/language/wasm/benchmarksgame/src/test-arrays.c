#include "../../stdlib.h"

int _start(int length) {
    int* x = (int*) malloc(length);
    int sum = 0;
    for(int i = 0; i < length; i++) {
        sum += blackhole_int(x[blackhole_int(i)]);
    }
    return sum;
}