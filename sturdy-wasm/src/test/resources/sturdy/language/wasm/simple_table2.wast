(module

  (table $ftbl1 1 funcref)
    (elem (i32.const 0)
      $test_increase
      $test_decrease
    )

  (table $ftbl2 1 funcref)
    (elem (i32.const 0)
      $test_const
    )

  (table $ftbl3 5 funcref)
    (elem (i32.const 0)
      $test_const
      $test_increase
      $test_decrease
      $test_id
      $test_sum
    )


  (func $test_const (export "test_const") (result i32)
      i32.const 7
  )


  (func $test_increase (export "test_increase") (param i32) (result i32)
      local.get 0
      i32.const 1
      i32.add
  )

  (func $test_decrease (export "test_decrease") (param i32) (result i32)
      local.get 0
      i32.const 1
      i32.sub
  )

  (func $test_id (export "test_id") (param i32 i32) (result i32 i32)
      local.get 0
      local.get 1
  )

  (func $test_sum (export "test_sum") (param i32 i32) (result i32)
      local.get 0
      local.get 1
      i32.add
  )

  (type $return_i32 (func (result i32)))
  (func (export "test_call_const") (param i32) (result i32)
     local.get 0
     call_indirect (type $return_i32)
     ;;table.get $functbl

  )


  (type $i32_to_i32 (func (param i32) (result i32)))
  (func (export "test_call_increase") (param i32 i32) (result i32)
      local.get 1
      local.get 0
      call_indirect (type $i32_to_i32)
  )


  (func (export "test_call_decrease") (param i32 i32) (result i32)
      local.get 1
      local.get 0
      call_indirect (type $i32_to_i32)
  )

)