//
// Created by flo on 3/27/26.
//
struct S {
    int a;
    int b;
};

struct S g = {1, 2};

int struct_global_field_access() {
    int x = g.b;  // LOAD
    g.a = x;      // STORE
    return x;
}

int _start() {
    return struct_global_field_access();
}