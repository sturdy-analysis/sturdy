(module

  (table $x 10 funcref)
  (table $y 10 funcref)
      (elem 1 (i32.const 0)
        $dummy
        $dummy
        $dummy
      )
  (func $dummy)
  (func $dummy2 (result i32) i32.const 2)

  (func $test_a (export "test_a")(result funcref)
     i32.const 0 ;;d
     i32.const 2 ;;s
     i32.const 2 ;;n
     table.copy $x $y
     i32.const 1
     table.get $x
  )


)