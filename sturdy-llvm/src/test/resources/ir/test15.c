int add2(int x) {
    return x + 2;
}

int sub2(int x) {
    return x - 2;
}

int main() {
    int x = 10;
    if (x > 5)
        x = add2(x);
    else
        x = sub2(x);
    return x;
}