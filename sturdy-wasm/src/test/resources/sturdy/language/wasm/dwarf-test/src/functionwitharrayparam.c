//
// Created by flo on 3/3/26.
//

int functionWithArrayLocal(int *array, int size) {
    int sum = 0;
    for (int i = 0; i < size; i++) {
        sum += array[i];
    }
    return sum;
}

int _start() {
    int array[8] = {0, 1, 2, 3, 4, 5, 6, 7};
    return functionWithArrayLocal(array, 8);
}