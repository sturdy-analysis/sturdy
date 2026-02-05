//
// Created by flo on 2/5/26.
//

#include "../../stdlib.h"

struct MyStruct {
    int n;         // number of elements in the array
    int arr[];     // flexible array member (size determined at runtime)
};

// Function that sums the elements in the flexible array member
int sum_flex_struct(int size) {
    // Allocate enough memory for the struct + array
    struct MyStruct* s = malloc(sizeof(struct MyStruct) + sizeof(int) * size);
    if (!s) return -1;

    s->n = size;

    // Initialize the array and sum the elements
    int sum = 0;
    for (int i = 0; i < size; i++) {
        s->arr[i] = i;
        sum += s->arr[i];
    }

    free(s);
    return sum;
}

int _start() {
    return sum_flex_struct(5);
}