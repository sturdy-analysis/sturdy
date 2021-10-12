(module
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func (param i32)))
  (type (;2;) (func))
  (type (;3;) (func (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;0;) (type 1)))
  (func (;1;) (type 2)
    nop)
  (func (;2;) (type 0) (param i32) (result i32)
    local.get 0)
  (func (;3;) (type 1) (param i32)
    block  ;; label = @1
      local.get 0
      i32.eqz
      br_if 0
      i32.const 0
      call 3
    end
    )
  (func (;4;) (type 1) (param i32)
    nop)
  (func (;5;) (type 2)
    i32.const 7
    call 3
    )
  (func (;6;) (type 3) (result i32)
    i32.const 1028)
  (func (;7;) (type 0) (param i32) (result i32)
    i32.const 1024
    )

  (memory (;0;) 1)
  (export "_start" (func 5))
  )
