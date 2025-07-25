(module $spectral-norm.wasm
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param i32 i32) (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func $__wasi_proc_exit (type 0)))
  (func $__wasm_call_ctors (type 1)
    nop)
  (func $__original_main (type 2) (result i32)
    (local i32 i32 i32 f64 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 2400
    i32.sub
    local.tee 4
    global.set $__stack_pointer
    local.get 4
    i32.const 832
    i32.add
    local.set 5
    local.get 4
    i32.const 824
    i32.add
    local.set 1
    local.get 4
    i32.const 816
    i32.add
    local.set 7
    local.get 4
    i32.const 800
    i32.add
    i32.const 8
    i32.or
    local.set 8
    loop  ;; label = @1
      local.get 2
      i32.const 3
      i32.shl
      local.tee 0
      local.get 4
      i32.const 800
      i32.add
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 0
      local.get 8
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 0
      local.get 7
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 0
      local.get 1
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 0
      local.get 5
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 2
      i32.const 5
      i32.add
      local.tee 2
      i32.const 100
      i32.ne
      br_if 0 (;@1;)
    end
    loop  ;; label = @1
      i32.const 0
      local.set 1
      loop  ;; label = @2
        local.get 1
        i32.const 1
        i32.add
        local.set 2
        f64.const 0x0p+0 (;=0;)
        local.set 3
        i32.const 0
        local.set 0
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 0
          local.get 2
          i32.add
          local.get 0
          local.get 1
          i32.add
          i32.mul
          i32.const 1
          i32.shr_u
          local.get 2
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 4
          i32.const 800
          i32.add
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.mul
          local.get 3
          f64.add
          local.set 3
          local.get 0
          i32.const 1
          i32.add
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 4
        i32.const 1600
        i32.add
        local.get 1
        i32.const 3
        i32.shl
        i32.add
        local.get 3
        f64.store
        i32.const 0
        local.set 5
        local.get 2
        local.tee 1
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      loop  ;; label = @2
        local.get 5
        i32.const 1
        i32.add
        local.set 1
        f64.const 0x0p+0 (;=0;)
        local.set 3
        i32.const 0
        local.set 0
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 0
          i32.const 1
          i32.add
          local.tee 2
          local.get 0
          local.get 1
          i32.add
          local.get 0
          local.get 5
          i32.add
          i32.mul
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 4
          i32.const 1600
          i32.add
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.mul
          local.get 3
          f64.add
          local.set 3
          local.get 2
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 4
        local.get 5
        i32.const 3
        i32.shl
        i32.add
        local.get 3
        f64.store
        local.get 1
        local.set 5
        local.get 1
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      i32.const 0
      local.set 1
      loop  ;; label = @2
        local.get 1
        i32.const 1
        i32.add
        local.set 2
        f64.const 0x0p+0 (;=0;)
        local.set 3
        i32.const 0
        local.set 0
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 0
          local.get 2
          i32.add
          local.get 0
          local.get 1
          i32.add
          i32.mul
          i32.const 1
          i32.shr_u
          local.get 2
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 4
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.mul
          local.get 3
          f64.add
          local.set 3
          local.get 0
          i32.const 1
          i32.add
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 4
        i32.const 1600
        i32.add
        local.get 1
        i32.const 3
        i32.shl
        i32.add
        local.get 3
        f64.store
        i32.const 0
        local.set 5
        local.get 2
        local.tee 1
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      loop  ;; label = @2
        local.get 5
        i32.const 1
        i32.add
        local.set 1
        f64.const 0x0p+0 (;=0;)
        local.set 3
        i32.const 0
        local.set 0
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 0
          i32.const 1
          i32.add
          local.tee 2
          local.get 0
          local.get 1
          i32.add
          local.get 0
          local.get 5
          i32.add
          i32.mul
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 4
          i32.const 1600
          i32.add
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.mul
          local.get 3
          f64.add
          local.set 3
          local.get 2
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 4
        i32.const 800
        i32.add
        local.get 5
        i32.const 3
        i32.shl
        i32.add
        local.get 3
        f64.store
        local.get 1
        local.set 5
        local.get 1
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 6
      i32.const 1
      i32.add
      local.tee 6
      i32.const 10
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 4
    i32.const 2400
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (func $main (type 3) (param i32 i32) (result i32)
    call $__original_main)
  (func $_start (type 1)
    call $__wasm_call_ctors
    call $__original_main
    call $exit
    unreachable)
  (func $dummy (type 1)
    nop)
  (func $libc_exit_fini (type 1)
    call $dummy)
  (func $exit (type 0) (param i32)
    call $dummy
    call $libc_exit_fini
    call $dummy
    local.get 0
    call $_Exit
    unreachable)
  (func $_Exit (type 0) (param i32)
    local.get 0
    call $__wasi_proc_exit
    unreachable)
  (func $_emscripten_stack_restore (type 0) (param i32)
    local.get 0
    global.set $__stack_pointer)
  (func $emscripten_stack_get_current (type 2) (result i32)
    global.get $__stack_pointer)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 258 258)
  (global $__stack_pointer (mut i32) (i32.const 66560))
  (export "memory" (memory 0))
  (export "main" (func $main))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func $_start))
  (export "_emscripten_stack_restore" (func $_emscripten_stack_restore))
  (export "emscripten_stack_get_current" (func $emscripten_stack_get_current))
  (elem (;0;) (i32.const 1) func $__wasm_call_ctors))
