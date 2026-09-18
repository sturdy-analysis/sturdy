(module
  (type $sig (func))

  (table 0 funcref)

  (func (export "main") (param i32)
    i32.const 0
    call_indirect (type $sig)
  )
)