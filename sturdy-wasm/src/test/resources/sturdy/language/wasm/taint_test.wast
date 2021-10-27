(module
  (memory 1)

  (func (export "taint_memory_write") (param i32 i32) (result i32)
    local.get 0 ;; address
    local.get 1 ;; value
    i32.store
    local.get 0
    i32.load
  )
)
