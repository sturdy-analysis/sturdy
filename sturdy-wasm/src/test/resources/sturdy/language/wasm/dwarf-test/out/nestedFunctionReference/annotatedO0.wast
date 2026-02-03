(module $nestedFunctionReference.O0.wasm
  (type $t0 (func))
  (type $t1 (func (param i32) (result i32)))
  (type $t2 (func (result i32)))
  (func $__wasm_call_ctors (type $t0))
  (func $innerFunction (type $t1) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32)
    (local.set $l1
      (global.get $__stack_pointer))
    (local.set $l2
      (i32.const 16))
    (local.set $l3 ;; set l3 to stack_pointer -16 -> frame base?
      (i32.sub
        (local.get $l1)
        (local.get $l2)))
    (i32.store offset=12 ;; write p0 into the stack frame at frame base + 12
      (local.get $l3)
      (local.get $p0))
    (local.set $l4 ;; read value from stack frame back into local variable
      (i32.load offset=12
        (local.get $l3)))
    (local.set $l5 ;; pointer dereference -> read value from address stored in l4
      (i32.load
        (local.get $l4)))
    (local.set $l6
      (i32.const 1))
    (local.set $l7 ;; multiply l7 by 2 (via bitshift left)
      (i32.shl
        (local.get $l5)
        (local.get $l6)))
    (i32.store offset=8 ;; store result stored in l7 in the stack frame (linear memory) at frame base + 8
      (local.get $l3)
      (local.get $l7))
    (local.set $l8 ;; load value from stack frame from frame base + 12 into l8
      (i32.load offset=12
        (local.get $l3)))
    (local.set $l9 ;; pointer dereference
      (i32.load
        (local.get $l8)))
    (local.set $l10 ;; load value from stack frame from frame base + 8 into l10
      (i32.load offset=8
        (local.get $l3)))
    (local.set $l11 ;; add l9 and l10 and store result in l11
      (i32.add
        (local.get $l9)
        (local.get $l10)))
    (return ;; return l11
      (local.get $l11)))
  (func $outerFunction (type $t1) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    (local.set $l1
      (global.get $__stack_pointer))
    (local.set $l2
      (i32.const 16))
    (local.set $l3 ;; l3 is the frame base (stack_pointer - 16)
      (i32.sub
        (local.get $l1)
        (local.get $l2)))
    (global.set $__stack_pointer ;; update stack_pointer
      (local.get $l3))
    (i32.store offset=12 ;; load parameter into linear memory at frame base + 12
      (local.get $l3)
      (local.get $p0))
    (local.set $l4
      (i32.const 3))
    (i32.store offset=8 ;; store constant 3 into linear memory at frame base + 8
      (local.get $l3)
      (local.get $l4))
    (local.set $l5 ;; load value from linear memory at frame base + 8 into l5
      (i32.load offset=8
        (local.get $l3)))
    (local.set $l6
      (i32.const 12))
    (local.set $l7 ;; l7 holds a pointer to linear memory pointing to the address frame base + 12
      (i32.add
        (local.get $l3)
        (local.get $l6)))
    (local.set $l8 ;; leftover operation: copy l7 into l8, 0 semantics
      (local.get $l7))
    (local.set $l9 ;; call innerFunction with the local l8 as the parameter. this is a pointer pointing inside the stack frame of outerFunction
      (call $innerFunction
        (local.get $l8)))
    (local.set $l10 ;; l5 + result of innerFunction
      (i32.add
        (local.get $l5)
        (local.get $l9)))
    (i32.store offset=8 ;; reuse offset 8 to store result of sum in linear memory
      (local.get $l3)
      (local.get $l10))
    (local.set $l11 ;; read from linear memory back into the wasm local l11
      (i32.load offset=8
        (local.get $l3)))
    (local.set $l12
      (i32.const 16))
    (local.set $l13 ;; adjust the stackpointer before returning
      (i32.add
        (local.get $l3)
        (local.get $l12)))
    (global.set $__stack_pointer
      (local.get $l13))
    (return
      (local.get $l11)))
  (func $_start (type $t2) (result i32)
    (local $l0 i32) (local $l1 i32)
    (local.set $l0 ;; store 3 in l0
      (i32.const 3))
    (local.set $l1 ;; call outerFunction with 3 (l0) and store result in l1
      (call $outerFunction
        (local.get $l0)))
    (return ;; return l1
      (local.get $l1)))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66560))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1024))
  (global $__stack_low i32 (i32.const 1024))
  (global $__stack_high i32 (i32.const 66560))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66560))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "innerFunction" (func $innerFunction))
  (export "outerFunction" (func $outerFunction))
  (export "_start" (func $_start))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))