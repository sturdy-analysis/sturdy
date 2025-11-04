(module
  (import "env" "interval" (func $interval (param i32 i32) (result i32)))
  (func $loop_to_100 (export "loop_to_100") (param $x i32) (result i32)
    (block $exit
      (loop $continue
        (br_if $exit (i32.ge_s (local.get $x) (i32.const 100)))
        (local.set $x (i32.add (local.get $x) (i32.const 1)))
        (br $continue)
      )
    )
    (return (local.get $x))
  )
)

(assert_return
  (invoke "loop_to_100" (call $interval (i32.const 0) (i32.const 10)))
  (i32.const 100)
)