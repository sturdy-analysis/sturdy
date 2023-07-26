(module
  (table $ftbl1 0 funcref)
    (elem (i32.const 0)
      $test_table_size
      $test_sum
      $test_sum
    )
    
  (table $ftbl2 1 funcref)  
    (elem 1 (i32.const 1)
      $test_sum
    )

  (table $ftbl3 1 funcref)
    (elem 2 (i32.const 6)
      $test_sum
    )

  (func $test_sum (export "test_sum") (param i32 i32) (result i32)
      local.get 0
      local.get 1
      i32.add
  )

  (func $test_table_size (export "test_table_size") (result i32)
     table.size $ftbl1
  )

  ;;(func $test_table_get (export "test_table_get") (result funcref)
       ;;table.get $ftbl1
  ;;)
  (func $test_ref_null (export "test_ref_null") (result externref)
       ;;ref.null extern
       ref.null
)