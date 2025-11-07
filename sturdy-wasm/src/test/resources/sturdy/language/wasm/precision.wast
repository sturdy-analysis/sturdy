(module
  (import "env" "i32.interval" (func $i32.interval (param i32 i32) (result i32)))
  (import "env" "f32.interval" (func $f32.interval (param f32 f32) (result f32)))
  (import "env" "assert" (func $assert (param i32)))

  (func $x_minus_x_eq_zero (export "x_minus_x_eq_zero") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (local.set $y (i32.sub (local.get $x) (local.get $x)))
    (call $assert (i32.eq (local.get $y) (i32.const 0)))
  )

  (func $max_upper_bound (export "max_upper_bound") (local $x f32) (local $y f32) (local $z f32)
    (local.set $x (call $f32.interval (f32.const -100) (f32.const 100)))
    (local.set $y (call $f32.interval (f32.const -100) (f32.const 100)))
    (local.set $z (f32.max (local.get $x) (local.get $y)))
    (call $assert (i32.and (f32.ge (local.get $z) (local.get $x)) (f32.ge (local.get $z) (local.get $y))))
  )

  (func $loop_to_100 (export "loop_to_100") (local $x i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 10)))
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $x) (i32.const 100)))
        (call $assert (i32.lt_s (local.get $x) (i32.const 100)))
        (local.set $x (i32.add (local.get $x) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.ge_s (local.get $x) (i32.const 100)))
  )

  (func $loop_to_n (export "loop_to_n") (local $i i32) (local $n i32)
    (local.set $i (i32.const 0))
    (local.set $n (call $i32.interval (i32.const 10) (i32.const 100)))
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $i) (local.get $n)))
        (local.set $i (i32.add (local.get $i) (i32.const 1)))
        (br $continue)
      )
    )
    (call $assert (i32.ge_s (local.get $i) (local.get $n)))
  )

  (func $recursive_id (param $x i32) (result i32)
    (if $l1 (result i32) (i32.le_s (local.get $x) (i32.const 0))
      (then (local.get $x))
      (else (i32.add (i32.const 1) (call $recursive_id (i32.sub (local.get $x) (i32.const 1)))))
    )
    return
  )

  (func $input_of_recrusive_id_is_same_as_output (export "input_of_recrusive_id_is_same_as_output") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (call $recursive_id (local.get $x)))
    (call $assert (i32.eq (local.get $x) (local.get $y)))
  )

  (func $addition (export "addition") (local $x i32) (local $y i32) (local $z i32)
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


  (func (export "plus_five") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const 0) (i32.const 100)))
    (local.set $y (local.get $x))
                                                          (; Non-Relational  Relational Analysis ;)
                                                          (; Locals          Locals  AddrTrans     Store ;)
                                                          (; x ∈ [0,100]     x = x1  x1 = xr       xr ∈ [0,100]              ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [1,101]     x = x2  x2 = xr, ...  xr = xo + 1, xo ∈ [0,100] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [2,102]     x = x3  x3 = xr, ...  xr = xo + 1, xo ∈ [0,101] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [3,103]     x = x4  x4 = xr, ...  xr = xo + 1, xo ∈ [0,102] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [4,104]     x = x5  x5 = xr, ...  xr = xo + 1, xo ∈ [0,103] ;)
    (local.set $x (i32.add (local.get $x) (i32.const 1))) (; x ∈ [5,105]     x = x6  x6 = xr, ...  xr = xo + 1, xo ∈ [0,104] ;)

    (call $assert (i32.eq (local.get $x) (i32.add (local.get $y) (i32.const 5))))
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

  (func (export "abs_if_join_on_local") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (if (i32.ge_s (local.get $x) (i32.const 0))
      (then (local.set $y (local.get $x)))
      (else (local.set $y (i32.sub (i32.const 0) (local.get $x))))
    )
    (call $assert (i32.ge_s (local.get $y) (i32.const 0)))
  )

  (func (export "abs_br_if_join_on_local") (local $x i32) (local $y i32)
    (local.set $x (call $i32.interval (i32.const -100) (i32.const 100)))
    (block $exit
      (local.set $y (local.get $x))
      (br_if $exit (i32.le_s (i32.const 0) (local.get $x)))
      (local.set $y (i32.sub (i32.const 0) (local.get $x)))
    )
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
    (call $assert
      (i32.and
        (i32.ge_s (local.get $y) (i32.const 0))
        (i32.le_s (local.get $y) (i32.const 1))
      )
    )
  )


)
