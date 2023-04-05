(module
  (memory 1)

  (func (export "taint_memory_write") (param i32 i32) (result i32)
    local.get 0 ;; address
    local.get 1 ;; value
    i32.store
    local.get 0
    i32.load
  )

  (func (export "taint_loop") (param i32) (result i32)
    (loop
      local.get 0 ;; address
      i32.const 42 ;; value
      i32.store
      local.get 0
      i32.const 1
      i32.add
      local.set 0
      local.get 0
      i32.const 4
      i32.lt_u
      br_if 0
    )
    local.get 0
  )

  (func (export "write_mem_mixed") (param i32) (result i32)
    i32.const 1
    i32.const 43
    i32.store
    local.get 0
    local.get 0
    i32.store
    i32.const 1
    i32.load
  )

)
