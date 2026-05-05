#include<stdint.h>
#include<math.h>
#include<stdbool.h>

#ifdef MOPSA
#include"mopsa_include.h"
#else
#include"goblint_include.h"
#endif

void test_linear_constraint() {
    i32 x = i32_interval(-100, 100);
    i32 y = 3;
    i32 z = 2*x + y;
    assert(z == 2*x + 3);
}

f64 sin(f64 x) {
    return x - (pow(x,3)/6 + (pow(x,5)/120 - pow(x,7)/5040));
}

void test_sin_bounds() {
    f64 x = sin(f64_interval(-2.0, 2.0));
    assert(x <= 5.0);
    assert(-5 <= x);
}

void test_max_select() {
    i32 x = i32_interval(-100, 100);
    i32 y = i32_interval(-100, 100);
    i32 z = (x <= y) ? y : x;
    assert(z >= x);
    assert(z >= y);
}

void test_builtin_max() {
    f32 x = f32_interval(-100, 100);
    f32 y = f32_interval(-100, 100);
    f32 z = fmaxf(x, y);
    assert(z >= x);
    assert(z >= y);
}

void test_abs_if_join_on_stack() {
    i32 x = i32_interval(-100, 100);
    i32 y = (x >= 0) ? x : (0-x);
    assert(y >= x);
}

void test_abs_if_join_on_local() {
    i32 x = i32_interval(-100, 100);
    i32 y;
    if(x >= 0) {
        y = x;
    } else {
        y = 0 - x;
    }
    assert(y >= x);
}

void test_abs_if_br_if_join_on_local() {
    i32 x = i32_interval(-100, 100);
    i32 y = x;
    if(x >= 0)
        goto exit;
    y = 0 - x;
exit:
    assert(y >= x);
}

void test_abs_select() {
    i32 x = i32_interval(-100, 100);
    i32 y = (x >= 0) ? x : (0-x);
    assert(y >= x);
}

i32 plus_two(i32 x) {
    return x + 2;
}

void test_plus_two_function_call() {
    i32 x = i32_interval(-100, 100);
    i32 y = plus_two(x);
    assert(y == x + 2);
}

void test_loop_to_100() {
    i32 x = i32_interval(0, 10);
    while(true) {
        if(x >= 100)
            break;
        assert(x <= 100);
        x = x + 1;
        continue;
    }
    assert(x == 100);
}

void test_loop_condition_at_end() {
    i32 x = i32_interval(0, 10);
    while(true) {
        assert(x < 100);
        x = x + 1;
        if(x < 100)
            continue;
        break;
    }
    assert(x == 100);
}

void test_loop_over_int_array() {
    i32 array = i32_interval(0, 100);
    i32 i = 0;
    while(true) {
        if(i >= 1000)
            break;
        i32 address = array + (i << 2);
        assert(array <= address);
        assert(address <= (array + 4 * 1000));
        i = i + 1;
        continue;
    }
}

void test_mandelbrot_loop() {
    i32 x = 0;
    i32 y = 0;
    while(true) {
        if(y >= 100)
            break;
        while(true) {
            if(x >= 200)
                break;
            assert((y * 200 + x) <= (100 * 200));
            x = x + 1;
            continue;
        }
        y = y + 1;
        continue;
    }
}

void test_plus_five() {
    i32 x = i32_interval(0,100);
    x = x + 1;
    x = x + 1;
    x = x + 1;
    x = x + 1;
    x = x + 1;
    assert(x >= 5);
    assert(x <= 105);
}

void test_reassignment() {
    i32 x = 1;
    assert(x == 1);
    x = 2;
    assert(x == 2);
    x = 3;
    assert(x == 3);
}

void test_swap() {
   i32 x = i32_interval(-100,100);
   i32 y = i32_interval(-100,100);
   i32 a = x;
   i32 b = y;
   i32 tmp = a;
   a = b;
   b = tmp;
   assert(a == y);
   assert(b == x);
}

// Recursive tests
i32 tail_rec_loop(i32 x) {
    return (x < 100) ? tail_rec_loop(x + 1) : x;
}

void test_tail_rec_loop_to_100() {
    i32 x = i32_interval(0,10);
    i32 y = tail_rec_loop(x);
    assert(y == 100);
}

i32 tail_rec_loop_n(i32 x, i32 n) {
    return (x < n) ? tail_rec_loop_n(x + 1, n) : x;
}

void test_tail_rec_loop_to_n() {
    i32 x = i32_interval(0,10);
    i32 n = i32_interval(50,100);
    i32 y = tail_rec_loop_n(x,n);
    assert(y == n);
}

i32 fac(i32 x) {
    return (x < 1) ? 1 : (x * fac(x - 1));
}

void test_fac_positive() {
    i32 x = i32_interval(0,100);
    i32 y = fac(x);
    assert(y >= 0);
}

i32 fac_acc(i32 x, i32 y) {
    return (x == 0) ? y : fac_acc(x - 1, x * y);
}

void test_fac_acc_positive() {
    i32 x = i32_interval(0,100);
    i32 y = fac_acc(x, 1);
    assert(y >= 0);
}

i32 fib(i32 x) {
    return (x <= 1) ? 1 : fib(x - 2) + fib(x - 1);
}

void test_fib_positive() {
    i32 x = i32_interval(0,100);
    i32 y = fib(x);
    assert(y >= 0);
}

i32 fib_acc(i32 x, i32 y, i32 z) {
    return (x <= 1) ? z : fib_acc(x - 1, z, z + y);
}

void test_fib_acc_positive() {
    i32 x = i32_interval(0,100);
    i32 y = fib_acc(x, 0, 1);
    assert(y >= 0);
}

i32 odd(i32);
i32 even(i32 x) {
    return (x == 0) ? 1 : (
        (x == 1) ? 0 : odd(x - 1)
    );
}

i32 odd(i32 x) {
    return (x == 0) ? 0 : (
        (x == 1) ? 1 : even(x - 1)
    );
}

void test_even_returns_boolean() {
    i32 x = i32_interval(0,100);
    i32 y = even(x);
    assert(y >= 0);
    assert(y <= 1);
}

i32 peano_addition(i32 x, i32 y) {
    return (x == 0) ? y : peano_addition(x - 1, y + 1);
}

void test_recursive_peano_addition() {
    i32 x = i32_interval(-100,100);
    i32 y = i32_interval(-100,100);
    i32 z = peano_addition(x,y);
    assert(z == x + y);
}

i32 gauss_sum(i32 x) {
    return (x <= 0) ? 0 : x + gauss_sum(x - 1);
}

void test_gauss_sum_positive() {
    i32 x = i32_interval(0,100);
    i32 y = gauss_sum(x);
    assert(y >= 0);
}

void test_gauss_sum_greater_than_input() {
    i32 x = i32_interval(0,100);
    i32 y = gauss_sum(x);
    assert(y >= x);
}

// Mopsa Tests
void test_x_minus_x_eq_zero() {
    i32 x = i32_interval(-100,100);
    i32 y = x - x;
    assert(y == 0);
}

void test_max_if() {
    i32 x = i32_interval(-100,100);
    i32 y = i32_interval(-100,100);
    i32 z = 0;
    if(x <= y) {
        z = y;
    } else {
        z = x;
    }
    assert(z >= x);
    assert(z >= y);
}

void test_loop_to_n() {
    i32 i = 0;
    i32 n = i32_interval(10,100);
    while(true) {
        if(i >= n)
            break;
        i = i + 1;
        continue;
    }
    assert(i == n);
}

void test_two_x_plus_y_minus_x_eq_x_plus_y() {
    i32 x = i32_interval(-100,100);
    i32 y = i32_interval(-100,100);
    i32 z = 2*x + y;
    assert(z - x == x + y);
}

i32 recursive_id(i32 x) {
    return (x <= 0) ? x : 1 + recursive_id(x - 1);
}

void test_input_of_recursive_id_is_same_as_output() {
    i32 x = i32_interval(0,100);
    i32 y = recursive_id(x);
    assert(x == y);
}

void test_peano_addition_loop() {
    i32 x = i32_interval(0,100);
    i32 y = 0;
    i32 z = x + y;
    while(true) {
        if(x < 0)
            break;
        x = x - 1;
        y = y + 1;
        continue;
    }
    assert (x + y == z);
}

void test_static_inference_numeric_invariants_example_5_1() {
    i32 x = i32_interval(-10,10);
    i32 y = i32_interval(-10,10);
    if(x > y)
        x = y;
    i32 d = y - x;
    assert(d >= 0);
}

void test_static_inference_numeric_invariants_example_5_2() {
    i32 i = 1;
    i32 x = 0;
    while(true) {
        if(i >= 1000)
            break;
        i = i + 1;
        x = x + 1;
        continue;
    }
    assert(x <= 1000);
}

void test_static_inference_numeric_invariants_example_5_11() {
    i32 x = 2;
    i32 i = 0;
    while(true) {
        if(i >= 10)
            break;
        if(i32_interval(0,1) == 0) {
            x = x - 3;
        } else {
            x = x + 2;
        }
        i = i + 1;
        continue;
    }
    assert(2 - 3*i <= x);
    assert(x <= 2*i);
}
