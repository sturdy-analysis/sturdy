(module
  (table $t 10 funcref)
  (elem $t (i32.const 0) 0)

  (table $t2 1 funcref)
  (func $return_const (export "return_const")(param i32)(result i32)
    local.get 0
  )
)
