(module

  (table $t 30 30 funcref)
  (table $t2 30 30 funcref)
  (elem $t (i32.const 0) 3 1 4 1)
  (elem $t2 (i32.const 3) 8 7 6)

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


  (func $test_call_indirect (export "test_call_indirect") (result i32)
     i32.const 6 ;;idx
     call_indirect $t2 (result i32)
  )
)
