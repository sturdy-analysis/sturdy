#include "../../stdlib.h"

int fact(int x) {
    if(x == 1) {
        return x;
    } else {
        return fact(x - 1) * x;
    }
}

int _start() {
    assert(fact(5) == 120);
    return 0;
}