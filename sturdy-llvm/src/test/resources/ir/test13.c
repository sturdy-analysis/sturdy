int flip(int x) {
    if (x < 0)
        return -x;
    else
        return x;
}

int main() {
    int a = -5;
    int b = flip(a);
    return b;
}