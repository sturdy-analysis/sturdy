(module
  (type (;0;) (func (param i32)))
  (type (;1;) (func (result i32)))
  (type (;2;) (func (param i32) (result i32)))
  (type (;3;) (func (param f64) (result f64)))
  (type (;4;) (func))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;0;) (type 0)))
  (func (;1;) (type 4)
    (local i32)
    i32.const 42
    local.tee 0
    if  ;; label = @1
      local.get 0
      call 0
      unreachable
    end)
  (func (;2;) (type 0)
      (local i32)
      local.get 0
      (if (result i32)
        (then (i32.const 42))
        (else (i32.const 0))
      )
      call 0
      unreachable
  )
  (func (;3;) (type 0)
      local.get 0
      (if
        (then (i32.const 42) (call 0))
        (else (i32.const 0) (call 0))
      )
  )
  (export "_start_orig" (func 1))
  (export "_start2" (func 2))
  (export "_start3" (func 3))
)