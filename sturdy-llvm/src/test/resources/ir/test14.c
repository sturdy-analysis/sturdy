int inc(int x) {
    return x + 1;
}

int main() {
    int i = 0;
    while (i < 5)
        i = inc(i);
    return i;
}