//
// Created by flo on 2/5/26.
//

int functionWithArrayLocal() {
    int sum = 0;
    int array[8] = {0, 1, 2, 3, 4, 5, 6, 7};
    for (int i = 0; i < 8; i++) {
        sum += array[i];
    }
    return sum;
}

int _start() {
    return functionWithArrayLocal();
}