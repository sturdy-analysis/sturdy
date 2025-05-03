(module

  (table $ftbl1 3 funcref)
    (elem 0 (i32.const 0)
      $test_table_size_t1
      $test_sum
      $test_sum
    )
    
  (table $ftbl2 2 funcref)  
    (elem 1 (i32.const 1)
      $test_table_size_t2
      $test_sum
    )

  (table $ftbl3 6 funcref)
    (elem 2 (i32.const 6)
      $test_sum
    )

  (func $test_sum (export "test_sum") (param i32 i32) (result i32)
      local.get 0
      local.get 1
      i32.add
  )

  (func $test_table_size_t1 (export "test_table_size_t1")(result i32)
     table.size $ftbl1
  )

  (func $test_table_size_t2 (export "test_table_size_t2")(result i32)
     table.size $ftbl2
  )

  (func $test_table_get (export "test_table_get") (param i32) (result funcref)
     local.get 0
     table.get $ftbl2
  )

  (func $test_ref_null (export "test_ref_null") (result funcref)
     ref.null func
  )

  (func $test_call_indirect (export "test_call_indirect") (result i32)
     i32.const 1 ;;arg1
     i32.const 2 ;;arg2
     i32.const 2 ;;ftbl3
     i32.const 6 ;;idx
     call_indirect (param i32 i32)(result i32)
  )

  (func $test_table_get_t1 (export "test_table_get_t1") (param i32) (result funcref)
     local.get 0
     table.get $ftbl1
   )

  (func $test_table_get_t2 (export "test_table_get_t2") (param i32) (result funcref)
     local.get 0
     table.get $ftbl2
   )

  (func $test_table_get_t3 (export "test_table_get_t3") (param i32) (result funcref)
     local.get 0
     table.get $ftbl3
   )

  (func $test_table_set_t1 (export "test_table_set_t1") (result i32)
     i32.const 2
     ref.func 0
     table.set $ftbl1
     table.size $ftbl1
   )

  (func $test_set_call (export "test_set_call") (result i32)
     i32.const 2
     ref.func 0    
     table.set $ftbl1
     i32.const 1 ;;arg1
     i32.const 2 ;;arg2
     i32.const 0 ;;ftbl3
     i32.const 2 ;;elemidx
     call_indirect (param i32 i32)(result i32)
   )
)