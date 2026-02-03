//
// Created by flo on 1/26/26.
//

int innerFunction(int innerParameter) { // 1 parameter
    int innerVariable = 2 * innerParameter; //local variable
    return innerParameter + innerVariable;
}

int outerFunction(int x) {
    int outerVariable;
    outerVariable = innerFunction(x);
    return outerVariable;
}

int _start() {
    return outerFunction(3);
}