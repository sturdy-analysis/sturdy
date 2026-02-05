//
// Created by flo on 2/5/26.
//

struct Point {
    int x;
    int y;
    int z;
};

int functionWithStructParameterPointer(struct Point* point) {
    return point->x + point->y + point->z;
}

int _start() {
    struct Point myPoint = {10, 20, 30};
    return functionWithStructParameterPointer(&myPoint);
}