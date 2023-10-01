(module
  (func (export "f") (param $x i32) (result i32) (local.get $x))
)
(register "M")

(module
  (func $f (import "M" "f") (param i32) (result i32))

  (func $g (param $x i32) (result i32)
      (i32.add (local.get $x) (i32.const 1))
  )

  (global funcref (ref.null func))
  (global funcref (ref.func $g))

)