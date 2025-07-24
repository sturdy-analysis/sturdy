int main() {
    int i = 0;
    int x = 0;
    while (i < 10) {
        if (i % 2 == 0)
            x = x + 3;
        i = i + 1;
    }
    return x;
}