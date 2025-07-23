(module $binarytrees.wasm
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (param i32) (result i32)))
  (type (;3;) (func (result i32)))
  (type (;4;) (func (param i32 i32) (result i32)))
  (import "env" "malloc" (func $malloc (type 2)))
  (import "env" "free" (func $free (type 0)))
  (import "wasi_snapshot_preview1" "proc_exit" (func $__wasi_proc_exit (type 0)))
  (func $__wasm_call_ctors (type 1)
    nop)
  (func $ItemCheck (type 2) (param i32) (result i32)
    (local i32 i32)
    local.get 0
    i32.load
    local.tee 2
    i32.eqz
    if  ;; label = @1
      i32.const 1
      return
    end
    loop  ;; label = @1
      local.get 2
      call $ItemCheck
      local.get 1
      i32.add
      i32.const 1
      i32.add
      local.set 1
      local.get 0
      i32.load offset=4
      local.tee 0
      i32.load
      local.tee 2
      br_if 0 (;@1;)
    end
    local.get 1
    i32.const 1
    i32.add)
  (func $BottomUpTree (type 2) (param i32) (result i32)
    (local i32 i32)
    block (result i32)  ;; label = @1
      local.get 0
      i32.eqz
      if  ;; label = @2
        i32.const 0
        br 1 (;@1;)
      end
      local.get 0
      i32.const 1
      i32.sub
      local.tee 0
      call $BottomUpTree
      local.set 1
      local.get 0
      call $BottomUpTree
    end
    local.set 2
    i32.const 8
    call $malloc
    local.tee 0
    local.get 2
    i32.store offset=4
    local.get 0
    local.get 1
    i32.store
    local.get 0)
  (func $DeleteTree (type 0) (param i32)
    (local i32)
    local.get 0
    i32.load
    local.tee 1
    if  ;; label = @1
      local.get 1
      call $DeleteTree
      local.get 0
      i32.load offset=4
      call $DeleteTree
    end
    local.get 0
    call $free)
  (func $__original_main (type 3) (result i32)
    (local i32 i32)
    i32.const 7
    call $BottomUpTree
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    drop
    i32.const 1
    local.set 0
    loop  ;; label = @1
      i32.const 4
      call $BottomUpTree
      local.tee 1
      call $ItemCheck
      drop
      local.get 1
      call $DeleteTree
      local.get 0
      i32.const 1
      i32.add
      local.tee 0
      i32.const 65
      i32.ne
      br_if 0 (;@1;)
    end
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 6
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    drop
    local.get 0
    call $DeleteTree
    i32.const 0)
  (func $main (type 4) (param i32 i32) (result i32)
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
  (func $emscripten_stack_get_current (type 3) (result i32)
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
