(module
  (import "env" "i32.interval" (func $i32.interval (param i32 i32) (result i32)))
  (import "env" "f32.interval" (func $f32.interval (param f32 f32) (result f32)))
  (import "env" "f64.interval" (func $f64.interval (param f64 f64) (result f64)))
  (import "env" "pow" (func $f64.pow (param f64 f64) (result f64)))
  (import "env" "assert" (func $assert (param i32)))

  (func (export "linear_constraint") (local $x i32) (local $y i32) (local $z i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (i32.const 3))
    (local.set $z (i32.add (i32.mul (i32.const 2) (local.get $x)) (local.get $y)))
    (call $assert (i32.eq (local.get $z) (i32.add (i32.mul (i32.const 2) (local.get $x)) (i32.const 3))))
  )

  (func $sin (param $x f64) (result f64)
    (f64.sub
      (local.get $x)
      (f64.add
        (f64.div
          (call $f64.pow (local.get $x) (f64.const 3))
          (f64.const 6)
        )
        (f64.sub
          (f64.div
            (call $f64.pow (local.get $x) (f64.const 5))
            (f64.const 120)
          )
          (f64.div
            (call $f64.pow (local.get $x) (f64.const 7))
            (f64.const 5040)
          )
        )
      )
    )
    return
  )

  (func (export "sin_bounds") (local $x f64)
    (local.set $x (call $sin (call $f64.interval (f64.const -2.0) (f64.const 2.0))))
    (call $assert (f64.le (local.get $x) (f64.const 5.0)))
    (call $assert (f64.le (f64.const -5.0) (local.get $x) ))
  )

  (func (export "max_select") (local $x i32) (local $y i32) (local $z i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $z (select (local.get $y) (local.get $x) (i32.le_s (local.get $x) (local.get $y)) ))
    (call $assert (i32.ge_s (local.get $z) (local.get $x)))
    (call $assert (i32.ge_s (local.get $z) (local.get $y)))
  )

  (func (export "builtin_max") (local $x f32) (local $y f32) (local $z f32)
    (local.set $x (call $f32.interval (f32.const -100) (f32.const 100)))
    (local.set $y (call $f32.interval (f32.const -100) (f32.const 100)))
    (local.set $z (f32.max (local.get $x) (local.get $y)))
    (call $assert (f32.ge (local.get $z) (local.get $x)))
    (call $assert (f32.ge (local.get $z) (local.get $y)))
  )

  (func (export "abs_if_join_on_stack") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y
      (if (result i32) (i32.ge_s (local.get $x) (i32.const 0))
        (then (local.get $x))
        (else (i32.sub (i32.const 0) (local.get $x)))
      )
    )
    (call $assert (i32.ge_s (local.get $y) (local.get $x)))
  )

  (func (export "abs_if_join_on_local") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (if (i32.ge_s (local.get $x) (i32.const 0))
      (then (local.set $y (local.get $x)))
      (else (local.set $y (i32.sub (i32.const 0) (local.get $x))))
    )
    (call $assert (i32.ge_s (local.get $y) (local.get $x)))
  )

  (func (export "abs_br_if_join_on_local") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (block $exit
      (local.set $y (local.get $x))
      (br_if $exit (i32.le_s (i32.const 0) (local.get $x)))
      (local.set $y (i32.sub (i32.const 0) (local.get $x)))
    )
    (call $assert (i32.ge_s (local.get $y) (local.get $x)))
  )

  (func (export "abs_select") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (select (local.get $x) (i32.sub (i32.const 0) (local.get $x)) (i32.le_s (i32.const 0) (local.get $x))))
    (call $assert (i32.ge_s (local.get $y) (local.get $x)))
  )


  (func $plus_two (param $x i32) (result i32)
    (return (i32.add (local.get $x) (i32.const 2)))
  )

  (func (export "plus_two_function_call") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $plus_two (local.get $x)))
    (call $assert (i32.eq (i32.add (local.get $x) (i32.const 2)) (local.get $y)))
  )

  (func (export "loop_to_100") (local $x i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 10)))
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $x) (i32.const 100)))
        (call $assert (i32.lt_s (local.get $x) (i32.const 100)))
        (local.set $x (i32.add (local.get $x) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.eq (local.get $x) (i32.const 100)))
  )

  (func (export "loop_condition_at_end") (local $x i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 10)))
    (loop $continue
      (call $assert (i32.lt_s (local.get $x) (i32.const 100)))
      (local.set $x (i32.add (local.get $x) (i32.const 1)))
      (br_if $continue (i32.lt_s (local.get $x) (i32.const 100)))
    )
    (call $assert (i32.eq (local.get $x) (i32.const 100)))
  )

  (func (export "loop_over_int_array") (local $array i32) (local $i i32) (local $address i32)
    (local.set $array (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $i (i32.const 0))
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $i) (i32.const 1000)))
        (local.set $address (i32.add (local.get $array) (i32.shl (local.get $i) (i32.const 2))))
        (call $assert (i32.le_s (local.get $array) (local.get $address)))
        (call $assert (i32.le_s (local.get $address) (i32.add (local.get $array) (i32.mul (i32.const 4) (i32.const 1000)))))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $continue)
      )
    )
  )

  (func (export "mandelbrot_loop") (local $x i32) (local $y i32)
    (block $outerExit
      (loop $outerLoop
        (br_if $outerExit (i32.ge_s (local.get $y) (i32.const 100)))
        (block $innerExit
          (loop $innerLoop
            (br_if $innerExit (i32.ge_s (local.get $x) (i32.const 200)))
            (call $assert (i32.le_s (i32.add (i32.mul (local.get $y) (i32.const 200)) (local.get $x)) (i32.mul (i32.const 100) (i32.const 200))))
            (local.set $x (i32.add (local.get $x) (i32.const 1)))
            (br $innerLoop)
          )
        )
        (local.set $y (i32.add (local.get $y) (i32.const 1)))
        (br $outerLoop)
      )
    )
  )

  (func (export "plus_five") (local $x i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
                                                          (; Non-Relational  Relational Analysis ;)
                                                          (; Locals          Locals  AddrTrans     Store ;)
                                                          (; x ∈ [0,100]     x = x1  x1 = xr       xr ∈ [0,100]              ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [1,101]     x = x2  x2 = xr, ...  xr = xo + 1, xo ∈ [0,100] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [2,102]     x = x3  x3 = xr, ...  xr = xo + 1, xo ∈ [0,101] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [3,103]     x = x4  x4 = xr, ...  xr = xo + 1, xo ∈ [0,102] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [4,104]     x = x5  x5 = xr, ...  xr = xo + 1, xo ∈ [0,103] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [5,105]     x = x6  x6 = xr, ...  xr = xo + 1, xo ∈ [0,104] ;)

    (call $assert (i32.le_s (i32.const 5) (local.get $x)))
    (call $assert (i32.le_s (local.get $x) (i32.const 105)))
  )

  (func (export "reassignment") (local $x i32)
    (local.set $x (i32.const 1))
    (call $assert (i32.eq (local.get $x) (i32.const 1)))
    (local.set $x (i32.const 2))
    (call $assert (i32.eq (local.get $x) (i32.const 2)))
    (local.set $x (i32.const 3))
    (call $assert (i32.eq (local.get $x) (i32.const 3)))
  )

  (func (export "swap") (local $x i32) (local $y i32) (local $a i32) (local $b i32)(local $tmp i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $a (local.get $x))
    (local.set $b (local.get $y))
    (local.set $tmp (local.get $a))
    (local.set $a (local.get $b))
    (local.set $b (local.get $tmp))
    (call $assert (i32.eq (local.get $a) (local.get $y)))
    (call $assert (i32.eq (local.get $b) (local.get $x)))
  )

  ;; Recursive Tests
  (func $tail_rec_loop_to_100 (param $x i32) (result i32)
    (if (result i32) (i32.lt_s (local.get $x) (i32.const 100))
      (then (call $tail_rec_loop_to_100 (i32.add (local.get $x) (i32.const 1))))
      (else (local.get $x))
    )
  )

  (func (export "tail_rec_loop_to_100") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 10)))
    (local.set $y (call $tail_rec_loop_to_100 (local.get $x)))
    (call $assert (i32.eq (local.get $y) (i32.const 100)))
  )

  (func $tail_rec_loop_to_n (param $x i32) (param $n i32) (result i32)
    (if (result i32) (i32.lt_s (local.get $x) (local.get $n))
      (then (call $tail_rec_loop_to_n (i32.add (local.get $x) (i32.const 1)) (local.get $n)))
      (else (local.get $x))
    )
  )

  (func (export "tail_rec_loop_to_n") (local $x i32) (local $y i32) (local $n i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 10)))
    (local.set $n (call $i32.interval (i32.const 50) (i32.const 100)))
    (local.set $y (call $tail_rec_loop_to_n (local.get $x) (local.get $n)))
    (call $assert (i32.eq (local.get $y) (local.get $n)))
  )

  (func $fac (param $x i32) (result i32)
    (if (result i32) (i32.le_s (local.get $x) (i32.const 1))
      (then (i32.const 1))
      (else
        (i32.mul
          (local.get $x)
          (call $fac (i32.sub (local.get $x) (i32.const 1)))
        )
      )
    )
  )

  (func (export "fac_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fac (local.get $x)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )


  (func $fac-acc (param $x i32) (param $y i32) (result i32)
    (if (result i32) (i32.eqz (local.get $x))
      (then (local.get $y))
      (else
        (call $fac-acc
          (i32.sub (local.get $x) (i32.const 1))
          (i32.mul (local.get $x) (local.get $y))
        )
      )
    )
  )

  (func (export "fac_acc_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fac-acc (local.get $x) (i32.const 1)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func $fib (param $x i32) (result i32)
    (if (result i32) (i32.le_u (local.get $x) (i32.const 1))
      (then (i32.const 1))
      (else
        (i32.add
          (call $fib (i32.sub (local.get $x) (i32.const 2)))
          (call $fib (i32.sub (local.get $x) (i32.const 1)))
        )
      )
    )
  )

  (func (export "fib_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fib (local.get $x)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func $fib-acc (param $x i32) (param $y i32) (param $z i32) (result i32)
    (if (result i32) (i32.le_u (local.get $x) (i32.const 1))
      (then (local.get $z))
      (else
        (call $fib-acc
          (i32.sub (local.get $x) (i32.const 1))
          (local.get $z)
          (i32.add (local.get $z) (local.get $y))
        )
      )
    )
  )

  (func (export "fib_acc_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fib-acc (local.get $x) (i32.const 0) (i32.const 1)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func (export "fib_addition_of_predecessors") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fib (local.get $x)))
    (call $assert
      (i32.eq
        (call $fib (local.get $x))
        (i32.add (call $fib (i32.sub (local.get $x) (i32.const 1)))
                 (call $fib (i32.sub (local.get $x) (i32.const 2)))
        )
      )
    )
  )

  (func $even (export "even") (param i32) (result i32)
    (if (result i32) (i32.eq (local.get 0) (i32.const 0))
      (then (i32.const 1))
      (else
        (if (result i32) (i32.eq (local.get 0) (i32.const 1))
          (then (i32.const 0))
          (else (call $odd (i32.sub (local.get 0) (i32.const 1))))
        )
      )
    )
  )

  (func $odd (export "odd") (param i32) (result i32)
    (if (result i32) (i32.eq (local.get 0) (i32.const 0))
      (then (i32.const 0))
      (else
        (if (result i32) (i32.eq (local.get 0) (i32.const 1))
          (then (i32.const 1))
          (else (call $even (i32.sub (local.get 0) (i32.const 1))))
        )
      )
    )
  )

  (func (export "even_returns_boolean") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $even (local.get $x)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
    (call $assert (i32.le_s (local.get $y) (i32.const 1)))
  )

  (func $recursive-peano-addition (param $x i32) (param $y i32) (result i32)
    (if (result i32) (i32.eqz (local.get $x))
      (then (local.get $y))
      (else
        (call $recursive-peano-addition
          (i32.sub (local.get $x) (i32.const 1))
          (i32.add (local.get $y) (local.get 1))
        )
      )
    )
  )

  (func (export "recursive_peano_addition") (local $x i32) (local $y i32) (local $z i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $z (call $recursive-peano-addition (local.get $x) (local.get $y)))
    (call $assert (i32.eq (local.get $z) (i32.add (local.get $x) (local.get $y))))
  )

  (func $gauss-sum (param $x i32) (result i32)
    (if (result i32) (i32.le_s (local.get $x) (i32.const 0))
      (then (i32.const 0))
      (else (i32.add (local.get $x) (call $gauss-sum (i32.sub (local.get $x) (i32.const -1)))))
    )
  )

  (func (export "gauss_sum_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $gauss-sum (local.get $x)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func (export "gauss_sum_greater_than_input") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $gauss-sum (local.get $x)))
    (call $assert (i32.ge_s (local.get $y) (local.get $x)))
  )

  ;; Mopsa Tests
  (func (export "x_minus_x_eq_zero") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (i32.sub (local.get $x) (local.get $x)))
    (call $assert (i32.eq (local.get $y) (i32.const 0)))
  )

  (func (export "max_if") (local $x i32) (local $y i32) (local $z i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
    (if (i32.le_s (local.get $x) (local.get $y))
      (then (local.set $z (local.get $y)))
      (else (local.set $z (local.get $x)))
    )
    (call $assert (i32.ge_s (local.get $z) (local.get $x)))
    (call $assert (i32.ge_s (local.get $z) (local.get $y)))
  )

  (func (export "loop_to_n") (local $i i32) (local $n i32)
    (local.set $i (i32.const 0))
    (local.set $n (call $i32.interval (i32.const 10) (i32.const 100)))
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $i) (local.get $n)))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.eq (local.get $i) (local.get $n)))
  )

  (func (export "2x_plus_y_minus_x_eq_x_plus_y") (local $x i32) (local $y i32) (local $z i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $z (i32.add (i32.mul (i32.const 2) (local.get $x)) (local.get $y)))
    (call $assert (i32.eq (i32.sub (local.get $z) (local.get $x)) (i32.add (local.get $x) (local.get $y))))
  )

  (func $recursive-id (param $x i32) (result i32)
    (if $l1 (result i32) (i32.le_s (local.get $x) (i32.const 0))
      (then (local.get $x))
      (else (i32.add (i32.const 1) (call $recursive-id (i32.sub (local.get $x) (i32.const 1)))))
    )
    return
  )

  (func (export "input_of_recrusive_id_is_same_as_output") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $recursive-id (local.get $x)))
    (call $assert (i32.eq (local.get $x) (local.get $y)))
  )

  (func (export "peano_addition_loop") (local $x i32) (local $y i32) (local $z i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (i32.const 0))
    (local.set $z (i32.add (local.get $x) (local.get $y)))
    (block $exit
      (loop $continue
        (br_if $exit (i32.lt_s (local.get $x) (i32.const 0)))
        (local.set $x (i32.sub (local.get $x) (i32.const 1)))
        (local.set $y (i32.add (local.get $y) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.eq (i32.add (local.get $x) (local.get $y)) (local.get $z)))
  )


  ;; Examples from "Static Inference of Numeric Invariants by Abstract Interpretation"
  ;; https://mine.perso.lip6.fr/enseignement/mpri/2024-2025/course_ok.pdf
  (func (export "static_inference_numeric_invariants_example_5_1") (local $x i32) (local $y i32) (local $d i32)
    (local.set $x (call $i32.interval (i32.const -10) (i32.const 10)))
    (local.set $y (call $i32.interval (i32.const -10) (i32.const 10)))
    (if (i32.ge_s (local.get $x) (local.get $y))
      (then (local.set $x (local.get $y)))
      (else)
    )
    (local.set $d (i32.sub (local.get $y) (local.get $x)))
    (call $assert (i32.ge_s (local.get $d) (i32.const 0)))
  )

  (func (export "static_inference_numeric_invariants_example_5_2") (local $i i32) (local $x i32)
    (local.set $i (i32.const 1))
    (local.set $x (i32.const 0))
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $i) (i32.const 1000)))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (local.set $x (i32.add (local.get $x) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.le_s (local.get $x) (i32.const 1000)))
  )

  (func (export "static_inference_numeric_invariants_example_5_11") (local $i i32) (local $x i32)
    (local.set $x (i32.const 2))
    (local.set $i (i32.const 0))
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $i) (i32.const 10)))
        (if (i32.eq (call $i32.interval (i32.const 0) (i32.const 1)) (i32.const 0))
          (then (local.set $x (i32.sub (local.get $x) (i32.const 3))))
          (else (local.set $x (i32.add (local.get $x) (i32.const 2))))
        )
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.le_s (i32.sub (i32.const 2) (i32.mul (i32.const 3) (local.get $i))) (local.get $x)))
    (call $assert (i32.le_s (local.get $x) (i32.add (i32.const 2) (i32.mul (i32.const 2) (local.get $i)))))
  )

  ;; The following examples are take from "Static Analysis Of Binary Code With Memory Indirections Using Polyhedra"
  ;; Section 7.3

  (func (export "static_analysis_binary_code_example_12") (local $offset i32) (local $size i32)
    (local.set $offset (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $size (call $i32.interval (i32.const 10) (i32.const 20)))
    (if $IF1 (i32.or (i32.gt_s (local.get $offset) (local.get $size))
                (i32.lt_s (local.get $offset) (i32.const 0)))
      (then)
      (else

        ;; In the condition of $IF1, there is an off-by-one error, such that $offset == $size is possible.
        (if (i32.eq (local.get $offset) (local.get $size))
          (then)
          (else
            (call $assert (i32.const 0 (; false ;)))
          )
        )
      )
    )

  )

  (func (export "static_analysis_binary_code_example_13") (local $base i32) (local $end i32) (local $i i32)
    (local.set $base (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $end (call $i32.interval (i32.const 0) (i32.const 100)))
    (if (i32.ge_s (i32.sub (local.get $end) (local.get $base)) (i32.const 50))
      (then (local.set $end (i32.add (local.get $base) (i32.const 50))))
      (else)
    )
    (call $assert (i32.le_s (local.get $base) (local.get $end)))
    (call $assert (i32.le_s (i32.sub (local.get $end) (local.get $base)) (i32.const 50)))
  )
)
