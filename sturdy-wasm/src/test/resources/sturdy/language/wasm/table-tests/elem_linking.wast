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

