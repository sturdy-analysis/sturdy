(module
  (table $t1 2 3 funcref)
  (table $t2 6 funcref)
  (table $t3 2 funcref)

  (func $test_table_size_t1 (export "test_table_size_t1")(result i32)
     table.size $t1
  )

  (func $test_dummy (export "test_dummy")(result i32)
     i32.const 4
  )

  (func $test_table_set_t1 (export "test_table_set_t1")
     i32.const 1
     ref.func 1     
     table.set $t1
  )

  (func $test_set_call (export "test_set_call") (result i32)
     call $test_table_set_t1
     i32.const 0 ;;t1
     i32.const 1 ;;idx
     call_indirect (result i32)
   )

  (func $test_t3 (export "test_t3")(result funcref)
     i32.const 1
     ref.func 0     
     table.set $t3
     i32.const 0
     table.get $t3
  )
)