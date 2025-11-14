(module
  (import "env" "i32.interval" (func $i32.interval (param i32 i32) (result i32)))
  (import "env" "f32.interval" (func $f32.interval (param f32 f32) (result f32)))
  (import "env" "f64.interval" (func $f64.interval (param f64 f64) (result f64)))

  (import "env" "i32.phi" (func $i32.phi (param i32 i32) (result i32)))

  (import "env" "pow" (func $f64.pow (param f64 f64) (result f64)))
  (import "env" "assert" (func $assert (param i32)))

  (func (export "max_if") (local $x i32) (local $y i32) (local $z1 i32) (local $z2 i32) (local $z3 i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
    (if (i32.le_s (local.get $x) (local.get $y))
      (then (local.set $z1 (local.get $y)))
      (else (local.set $z2 (local.get $x)))
    )
    (local.set $z3 (select (local.get $z1) (local.get $z2) (i32.le_s (local.get $x) (local.get $y))))
    (call $assert (i32.ge_s (local.get $z3) (local.get $x)))
    (call $assert (i32.ge_s (local.get $z3) (local.get $y)))
  )

  (func (export "max_select") (local $x i32) (local $y i32) (local $z i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $z (select (local.get $y) (local.get $x) (i32.le_s (local.get $x) (local.get $y))))
    (call $assert (i32.ge_s (local.get $z) (local.get $x)))
    (call $assert (i32.ge_s (local.get $z) (local.get $y)))
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

  (func (export "loop_to_100") (local $x1 i32) (local $x2 i32) (local $x3 i32)
    (local.set $x1 (call $i32.interval (i32.const 0) (i32.const 10)))
    (block $exit
      (loop $continue
        (local.set $x2 (call $i32.phi (local.get $x1) (local.get $x3)))
        (br_if $exit (i32.ge_s (local.get $x2) (i32.const 100)))
        (call $assert (i32.lt_s (local.get $x2) (i32.const 100)))
        (local.set $x3 (i32.add (local.get $x2) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.ge_s (local.get $x3) (i32.const 100)))
  )

  (func (export "plus_five") (local $x1 i32) (local $x2 i32) (local $x3 i32) (local $x4 i32) (local $x5 i32) (local $x6 i32)
    (local.set $x1 (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $x2 (i32.add (local.get $x1) (i32.const 1)))
    (local.set $x3 (i32.add (local.get $x2) (i32.const 1)))
    (local.set $x4 (i32.add (local.get $x3) (i32.const 1)))
    (local.set $x5 (i32.add (local.get $x4) (i32.const 1)))
    (local.set $x6 (i32.add (local.get $x5) (i32.const 1)))

    (call $assert (i32.le_s (i32.const 5) (local.get $x6)))
    (call $assert (i32.le_s (local.get $x6) (i32.const 105)))
  )

  (func (export "abs_if_join_on_stack") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y
      (if (result i32) (i32.ge_s (local.get $x) (i32.const 0))
        (then (local.get $x))
        (else (i32.sub (i32.const 0) (local.get $x)))
      )
    )
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func (export "abs_if_join_on_local") (local $x i32) (local $y1 i32) (local $y2 i32) (local $y3 i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (if (i32.ge_s (local.get $x) (i32.const 0))
      (then (local.set $y1 (local.get $x)))
      (else (local.set $y2 (i32.sub (i32.const 0) (local.get $x))))
    )
    (local.set $y3 (call $i32.phi (local.get $y1) (local.get $y2)))
    (call $assert (i32.ge_s (local.get $y3) (i32.const 0)))
  )

  (func (export "abs_br_if_join_on_local") (local $x i32) (local $y1 i32) (local $y2 i32) (local $y3 i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (block $exit
      (local.set $y1 (local.get $x))
      (br_if $exit (i32.le_s (i32.const 0) (local.get $x)))
      (local.set $y2 (i32.sub (i32.const 0) (local.get $x)))
    )
    (local.set $y3 (call $i32.phi (local.get $y1) (local.get $y2)))
    (call $assert (i32.ge_s (local.get $y3) (i32.const 0)))
  )

  (func (export "abs_select") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (select (local.get $x) (i32.sub (i32.const 0) (local.get $x)) (i32.le_s (i32.const 0) (local.get $x))))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func $fac (export "fac") (param i32) (result i32)
    (if (result i32) (i32.le_s (local.get 0) (i32.const 1))
      (then (i32.const 1))
      (else
        (i32.mul
          (local.get 0)
          (call $fac (i32.sub (local.get 0) (i32.const 1)))
        )
      )
    )
  )

  (func (export "fac_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fac (local.get $x)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )


  (func $fac-acc (export "fac-acc") (param i32 i32) (result i32)
    (if (result i32) (i32.eqz (local.get 0))
      (then (local.get 1))
      (else
        (call $fac-acc
          (i32.sub (local.get 0) (i32.const 1))
          (i32.mul (local.get 0) (local.get 1))
        )
      )
    )
  )

  (func (export "fac_acc_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fac-acc (local.get $x) (i32.const 1)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func $fib (export "fib") (param i32) (result i32)
    (if (result i32) (i32.le_u (local.get 0) (i32.const 1))
      (then (i32.const 1))
      (else
        (i32.add
          (call $fib (i32.sub (local.get 0) (i32.const 2)))
          (call $fib (i32.sub (local.get 0) (i32.const 1)))
        )
      )
    )
  )

  (func (export "fib_positive") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $fib (local.get $x)))
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
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

  (func (export "mandelbrot_loop") (local $x1 i32) (local $x2 i32) (local $x3 i32) (local $y1 i32) (local $y2 i32) (local $y3 i32)
    (local.set $x1 (i32.const 1))
    (local.set $y1 (i32.const 1))

    (block $outerExit
      (loop $outerLoop
        (local.set $y2 (call $i32.phi (local.get $y1) (local.get $y3)))
        (br_if $outerExit (i32.ge_s (local.get $y2) (i32.const 100)))
        (block $innerExit
          (loop $innerLoop
            (local.set $x2 (call $i32.phi (local.get $x1) (local.get $x3)))
            (br_if $innerExit (i32.ge_s (local.get $x2) (i32.const 200)))
            (call $assert (i32.le_s (i32.add (i32.mul (local.get $y2) (i32.const 200)) (local.get $x2)) (i32.mul (i32.const 100) (i32.const 200))))
            (local.set $x3 (i32.add (local.get $x2) (i32.const 1)))
            (br $innerLoop)
          )
        )
        (local.set $y3 (i32.add (local.get $y2) (i32.const 1)))
        (br $outerLoop)
      )
    )
  )


  ;; Mopsa Tests
  (func (export "x_minus_x_eq_zero") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (i32.sub (local.get $x) (local.get $x)))
    (call $assert (i32.eq (local.get $y) (i32.const 0)))
  )

  (func (export "builtin_max") (local $x f32) (local $y f32) (local $z f32)
    (local.set $x (call $f32.interval (f32.const -100) (f32.const 100)))
    (local.set $y (call $f32.interval (f32.const -100) (f32.const 100)))
    (local.set $z (f32.max (local.get $x) (local.get $y)))
    (call $assert (f32.ge (local.get $z) (local.get $x)))
    (call $assert (f32.ge (local.get $z) (local.get $y)))
  )

  (func (export "loop_to_n") (local $i1 i32) (local $i2 i32) (local $i3 i32) (local $n i32)
    (local.set $i1 (i32.const 0))
    (local.set $n (call $i32.interval (i32.const 10) (i32.const 100)))
    (block $exit
      (loop $continue
        (local.set $i2 (call $i32.phi (local.get $i1) (local.get $i3)))
        (br_if $exit (i32.ge_s (local.get $i1) (local.get $n)))
        (local.set $i3 (i32.add (local.get $i2) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.ge_s (local.get $i3) (local.get $n)))
  )

  (func $recursive_id (param $x i32) (result i32)
    (if $l1 (result i32) (i32.le_s (local.get $x) (i32.const 0))
      (then (local.get $x))
      (else (i32.add (i32.const 1) (call $recursive_id (i32.sub (local.get $x) (i32.const 1)))))
    )
    return
  )

    (func (export "2x_plus_y_minus_x_eq_x_plus_y") (local $x i32) (local $y i32) (local $z i32)
      (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
      (local.set $y (call $i32.interval (i32.const -100) (i32.const 100)))
      (local.set $z (i32.add (i32.mul (i32.const 2) (local.get $x)) (local.get $y)))
      (call $assert (i32.eq (i32.sub (local.get $z) (local.get $x)) (i32.add (local.get $x) (local.get $y))))
    )

  (func (export "input_of_recrusive_id_is_same_as_output") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $recursive_id (local.get $x)))
    (call $assert (i32.eq (local.get $x) (local.get $y)))
  )

  (func (export "addition_loop") (local $x1 i32) (local $x2 i32) (local $x3 i32) (local $y1 i32) (local $y2 i32) (local $y3 i32) (local $z i32)
    (local.set $x1 (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y1 (i32.const 0))
    (local.set $z (i32.add (local.get $x1) (local.get $y1)))
    (block $exit
      (loop $continue
        (local.set $x2 (call $i32.phi (local.get $x1) (local.get $x3)))
        (local.set $y2 (call $i32.phi (local.get $y1) (local.get $y3)))
        (br_if $exit (i32.lt_s (local.get $x2) (i32.const 0)))
        (local.set $x3 (i32.sub (local.get $x2) (i32.const 1)))
        (local.set $y3 (i32.add (local.get $y2) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.eq (i32.add (local.get $x3) (local.get $y3)) (local.get $z)))
  )


  ;; Examples from "Static Inference of Numeric Invariants by Abstract Interpretation"
  ;; https://mine.perso.lip6.fr/enseignement/mpri/2024-2025/course_ok.pdf
  (func (export "static_inference_numeric_invariants_example_5_1") (local $x1 i32) (local $x2 i32) (local $x3 i32) (local $y1 i32) (local $d i32)
    (local.set $x1 (call $i32.interval (i32.const -10) (i32.const 10)))
    (local.set $y1 (call $i32.interval (i32.const -10) (i32.const 10)))
    (if (i32.ge_s (local.get $x1) (local.get $y1))
      (then (local.set $x2 (local.get $y1)))
      (else)
    )
    (local.set $x3 (call $i32.phi (local.get $x1) (local.get $x2)))
    (local.set $d (i32.sub (local.get $y1) (local.get $x3)))
    (call $assert (i32.ge_s (local.get $d) (i32.const 0)))
  )

  (func (export "static_inference_numeric_invariants_example_5_2") (local $i1 i32) (local $i2 i32) (local $i3 i32) (local $x1 i32) (local $x2 i32) (local $x3 i32)
    (local.set $i1 (i32.const 1))
    (local.set $x1 (i32.const 0))
    (block $exit
      (loop $continue
        (local.set $i2 (call $i32.phi (local.get $i1) (local.get $i3)))
        (local.set $x2 (call $i32.phi (local.get $x1) (local.get $x3)))
        (br_if $exit (i32.ge_s (local.get $i2) (i32.const 1000)))
        (local.set $i3 (i32.add (local.get $i2) (i32.const 1)))
        (local.set $x3 (i32.add (local.get $x2) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.le_s (local.get $x3) (i32.const 1000)))
  )

  (func (export "static_inference_numeric_invariants_example_5_11") (local $i1 i32) (local $i2 i32) (local $i3 i32) (local $x1 i32) (local $x2 i32) (local $x3 i32) (local $x4 i32) (local $x5 i32) (local $x6 i32)
    (local.set $x1 (i32.const 2))
    (local.set $i1 (i32.const 0))
    (block $exit
      (loop $continue
        (local.set $i2 (call $i32.phi (local.get $i1) (local.get $i3)))
        (local.set $x2 (call $i32.phi (local.get $x1) (local.get $x5)))
        (br_if $exit (i32.ge_s (local.get $i2) (i32.const 10)))
        (if (i32.eq (call $i32.interval (i32.const 0) (i32.const 1)) (i32.const 0))
          (then (local.set $x4 (i32.sub (local.get $x2) (i32.const 3))))
          (else (local.set $x5 (i32.add (local.get $x2) (i32.const 2))))
        )
        (local.set $x6 (call $i32.phi (local.get $x4) (local.get $x5)))
        (local.set $i3 (i32.add (local.get $i2) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.le_s (i32.sub (i32.const 2) (i32.mul (i32.const 3) (local.get $i3))) (local.get $x6)))
    (call $assert (i32.le_s (local.get $x6) (i32.add (i32.const 2) (i32.mul (i32.const 2) (local.get $i3)))))
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

  (func (export "static_analysis_binary_code_example_13") (local $base i32) (local $end1 i32) (local $end2 i32) (local $end3 i32) (local $i i32)
    (local.set $base (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $end1 (call $i32.interval (i32.const 0) (i32.const 100)))
    (if (i32.ge_s (i32.sub (local.get $end1) (local.get $base)) (i32.const 50))
      (then (local.set $end2 (i32.add (local.get $base) (i32.const 50))))
      (else)
    )
    (local.set $end3 (call $i32.phi (local.get $end1) (local.get $end2)))
    (call $assert (i32.le_s (local.get $base) (local.get $end3)))
    (call $assert (i32.le_s (i32.sub (local.get $end3) (local.get $base)) (i32.const 50)))
  )
)
