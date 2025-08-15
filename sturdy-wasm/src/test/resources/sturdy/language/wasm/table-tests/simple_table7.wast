(module
  (table $t 5 16 funcref)

  (func $dummy)
  (func $test_grow (export "test_grow")(param i32)(result i32)
   ref.func 0
   local.get 0
   table.grow $t
  )

  (func $test_fill (export "test_fill")(result funcref)
  i32.const 2
  ref.func 0
  i32.const 3
  table.fill $t
  i32.const 0
  table.get $t
  )

)