//
// Created by flo on 1/26/26.
//

int innerFunction(int innerParameter) { // 1 parameter
    int innerVariable = 2 * innerParameter; //local variable
    return innerParameter + innerVariable;
}

int outerFunction() {
    int outerVariable;
    outerVariable = innerFunction(3);
    return outerVariable;
}

int _start() {
    return outerFunction();
}