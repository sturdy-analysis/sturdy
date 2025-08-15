(module
  (table $t1 0 funcref)
  (table $t2 0 funcref)

  (func $dummy)
  (func $dummy2 (result i32) i32.const 2)

  (func $test_table_grow (export "test_table_grow")(result i32)
     ref.func 0
     i32.const 10
     table.grow $t1
     ;;table.size $t1
  )

  (func $test_table_fill (export "test_table_fill")(result i32)
     i32.const 7
     ref.func 1
     i32.const 10
     table.fill $t1
     table.size $t1
  )

)