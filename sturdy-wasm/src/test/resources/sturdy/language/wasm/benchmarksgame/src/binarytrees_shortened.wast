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
  (func (;3;) (type 0) (param i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    block (result i32)  ;; label = @1
      local.get 0
      i32.eqz
      if  ;; label = @2
        i32.const 0
        local.set 0
        i32.const 0
        br 1 (;@1;)
      end
      local.get 0
      i32.const 1
      i32.sub
      local.tee 1
      call 3
      local.set 0
      local.get 1
      call 3
    end
    )
  (func (;4;) (type 1) (param i32)
    nop)
  (func (;5;) (type 2)
    i32.const 7
    call 3
    drop
    )
  (func (;6;) (type 3) (result i32)
    i32.const 1028)
  (func (;7;) (type 0) (param i32) (result i32)
    i32.const 1024
    )

  (table (;0;) 2 2 funcref)
  (memory (;0;) 256 256)
  (global (;0;) (mut i32) (i32.const 5244416))
  (export "memory" (memory 0))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func 5))
  (export "__errno_location" (func 6))
  (elem (;0;) (i32.const 1) 1)
  (data (;0;) (i32.const 1025) "\06P"))
