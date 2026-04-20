(module
  (table $t1 2 funcref)
  (elem $t1 (i32.const 1) $dummy)

  (global i32 i32.const 0)
  (global funcref ref.null func)
  (func $dummy)
  (func $test_random (export "test_random")(param $i i32)(param $j i32)(result funcref)
     global.get 0
     table.get $t1
  )
  (func $test_glob (export "test_glob")(result funcref)
     global.get 1
  )

)