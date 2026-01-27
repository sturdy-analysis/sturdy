//
// Created by flo on 1/26/26.
//

int innerFunction(int innerParameter) { // 1 parameter
    int innerVariable = 2 * innerParameter; //local variable
    if (innerVariable <= 10) {
        int result = innerParameter;
        return result;
    } else {
        int resultElse = innerParameter + innerVariable;
        return resultElse;
    }
}

int outerFunction() {
    int outerVariable;
    outerVariable = innerFunction(3);
    return outerVariable;
}

int _start() {
    return outerFunction();
}