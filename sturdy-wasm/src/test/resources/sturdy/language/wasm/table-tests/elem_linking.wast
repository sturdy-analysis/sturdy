;; Test the element section

;; Element sections across multiple modules change the same table

(module $module1
  (type $out-i32 (func (result i32)))
  (table (export "shared-table") 10 funcref)
  (elem (i32.const 8) $const-i32-a)
  (func $const-i32-a (type $out-i32) (i32.const 1))
  (func (export "call-7") (type $out-i32)
    (call_indirect (type $out-i32) (i32.const 7))
  )
  (func (export "call-8") (type $out-i32)
    (call_indirect (type $out-i32) (i32.const 8))
  )
)

(register "module1" $module1)

(assert_trap (invoke $module1 "call-7") "uninitialized element")
(assert_return (invoke $module1 "call-8") (i32.const 1))

(module $module2
  (type $out-i32 (func (result i32)))
  (import "module1" "shared-table" (table 10 funcref))
  (elem (i32.const 7) $const-i32-c)
  (func $const-i32-c (type $out-i32) (i32.const 2))
)

(assert_return (invoke $module1 "call-7") (i32.const 2))
