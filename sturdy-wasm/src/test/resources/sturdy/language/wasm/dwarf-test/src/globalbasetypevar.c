//
// Created by flo on 3/27/26.
//

int globalint = 5;

int functionWithGlobalAccess() {
    int localint = globalint;
    globalint = localint + 1;
    return localint;
}

int _start() {
    return functionWithGlobalAccess();
}