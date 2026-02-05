//
// Created by flo on 2/5/26.
//

int sum_vla(int n) {
    int arr[n];      // ← VLA
    int otherarr[2*n]; // <- 2nd vla
    int sum = 0;

    for (int i = 0; i < n; i++) {
        arr[i] = i;
        otherarr[n + i] = n + (i % 2);
        sum += arr[i] + otherarr[n + i];
    }
    return sum;
}

int _start() {
    return sum_vla(5);
}