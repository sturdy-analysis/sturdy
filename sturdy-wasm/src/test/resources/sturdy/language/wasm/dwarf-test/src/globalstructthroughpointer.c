//
// Created by flo on 3/27/26.
//
struct S { int a; int b; };
struct S g = {1, 2};

int struct_pointer_field_access() {
    struct S *p = &g;
    int x = p->b;  // LOAD
    p->a = x;      // STORE
    return x;
}

int _start() {
    return struct_pointer_field_access();
}