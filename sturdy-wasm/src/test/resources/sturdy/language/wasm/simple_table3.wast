(module

  (table $ftbl1 2 funcref)
    (elem (i32.const 0)
      $test_const
      $test_sum
      $test_table_size
    )

  (table $ftbl2 1 funcref)
    (elem 1 (i32.const 0)
      $test_const
    )

  (table $ftbl3 0 funcref)
    (elem 2 (i32.const 0)
    )


  (func $test_const (export "test_const") (result i32)
      i32.const 7
  )


  (func $test_sum (export "test_sum") (param i32 i32) (result i32)
      local.get 0
      local.get 1
      i32.add
  )

  (func $test_table_size (export "test_table_size") (result i32)
     table.size $ftbl1
  )

  (func $test_table_gwt (export "test_table_get") (result funcref)
       table.get $ftbl1
  )

  (type $return_i32 (func (result i32)))
  (func (export "test_call_const") (param i32) (result i32)
     local.get 0
     call_indirect (type $return_i32)
  )
)