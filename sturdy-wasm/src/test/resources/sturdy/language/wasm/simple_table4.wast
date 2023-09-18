(module
  (table $t2 2 externref)
  (table $t3 3 funcref)
  (elem $t3 (i32.const 1) $dummy)
  (func $dummy)
  (func $dummy2)

  (func $init (export "init")
     i32.const 2
     ref.func 3
     table.set $t3
     i32.const 1
     ref.func 3
     table.set $t3
  )

  (func $test_sum (export "test_sum") (param i32 i32) (result i32)
      local.get 0
      local.get 1
      i32.add
  )

  (func $test_table_size_t3 (export "test_table_size_t3")(result i32)
     table.size $t3
  )

  (func $size_after_table_set (export "size_after_table_set")(result i32)
     call $init
     table.size $t3
  )

  (func $test_ref_func (export "test_ref_func") (result funcref)
     ref.func 0
  )

  (func $test_ref_null_func (export "test_ref_null_func") (result funcref)
    ref.null func
  )

  (func $test_ref_null_extern (export "test_ref_null_extern") (result externref)
    ref.null extern
  )

  (func $test_table_get (export "test_table_get") (result funcref)
    i32.const 1
    table.get $t3
  )

  (func $test_set_null (export "test_set_null")(result i32)
    i32.const 2
    ref.func 0    
    table.set $t3
    table.size $t3
  )

)