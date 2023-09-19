(module

  (table $t 30 30 funcref)
  (elem $t (i32.const 2) 3 1 4 1)
  (elem (i32.const 0) 2 7 1 8)
  (elem $t (i32.const 12) 7 5 2 3 6)
  (elem (i32.const 0) 5 9 2 7 6)

  (func (result i32) i32.const 0)
  (func (result i32) i32.const 1)
  (func (result i32) i32.const 2)
  (func (result i32) i32.const 3)
  (func (result i32) i32.const 4)
  (func (result i32) i32.const 5)
  (func (result i32) i32.const 6)
  (func (result i32) i32.const 7)
  (func (result i32) i32.const 8)
  (func (result i32) i32.const 9)


  (func $test_a (export "test_a")(result funcref)
     i32.const 7
     i32.const 0
     i32.const 3
     table.init $t 1
     i32.const 10
     table.get 0
  )

  (func $test_b (export "test_b")(result i32)
     elem.drop 1
  )


)