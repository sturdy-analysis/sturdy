//
// Created by flo on 1/26/26.
//

char CharFunction(char* char_ptr) {
    *char_ptr = 'a';
    return *char_ptr;
}

char ConstCharFunction(const char* const_char_ptr) {
    //..can not overwrite..
    return *const_char_ptr;
}

int _start() {
    char firstChar = 'a';
    char secondChar = CharFunction(&firstChar);
    char thirdChar = ConstCharFunction(&firstChar);

    return 0;
}