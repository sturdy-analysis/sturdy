(module
  (table funcref
    (elem
      $noop
      $noop2
    )
  )

  (func $noop (result i32)
    (i32.const 0)
  )

  (func $noop2 (result i32 i32)
     (i32.const 0)(i32.const 1)
  )

  (func $test1 (export "test1") (result i32)
    (i32.const 1)
    (call $noop)
    (i32.add)
  )


)