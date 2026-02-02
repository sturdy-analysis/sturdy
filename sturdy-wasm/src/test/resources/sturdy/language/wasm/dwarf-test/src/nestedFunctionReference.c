//
// Created by flo on 2/2/26.
//

int innerFunction(const int *innerParameter) { // 1 parameter
    int innerVariable = 2 * (*innerParameter); //local variable
    return (*innerParameter) + innerVariable;
}

int outerFunction() {
    int outerVariable = 3;
    outerVariable = innerFunction(&outerVariable);
    return outerVariable;
}