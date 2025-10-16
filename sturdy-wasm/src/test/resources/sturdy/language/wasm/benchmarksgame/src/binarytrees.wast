(module
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (result i32)))
  (type $t3 (func (param f64) (result f64)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32) (result i32)))
  (import "env" "malloc" (func $env.malloc (type $t0)))
  (import "env" "free" (func $env.free (type $t1)))
  (import "env" "__VERIFIER_nondet_int" (func $env.__VERIFIER_nondet_int (type $t2)))
  (import "env" "exp2" (func $env.exp2 (type $t3)))
  (import "env" "assert" (func $env.assert (type $t1)))
  (func $__wasm_call_ctors (type $t4))
  (func $NewTreeNode (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32)
    i32.const 8
    call $env.malloc
    local.tee $l2
    local.get $p1
    i32.store offset=4
    local.get $l2
    local.get $p0
    i32.store
    local.get $l2)
  (func $ItemCheck (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    local.get $p0
    i32.load
    local.tee $l2
    i32.eqz
    if $I0
      i32.const 1
      return
    end
    loop $L1
      local.get $l2
      call $ItemCheck
      local.get $l1
      i32.add
      i32.const 1
      i32.add
      local.set $l1
      local.get $p0
      i32.load offset=4
      local.tee $p0
      i32.load
      local.tee $l2
      br_if $L1
    end
    local.get $l1
    i32.const 1
    i32.add)
  (func $BottomUpTree (type $t0) (param $p0 i32) (result i32)
    local.get $p0
    if $I0
      local.get $p0
      i32.const 1
      i32.sub
      local.tee $p0
      call $BottomUpTree
      local.get $p0
      call $BottomUpTree
      call $NewTreeNode
      return
    end
    i32.const 0
    i32.const 0
    call $NewTreeNode)
  (func $DeleteTree (type $t1) (param $p0 i32)
    (local $l1 i32)
    local.get $p0
    i32.load
    local.tee $l1
    if $I0
      local.get $l1
      call $DeleteTree
      local.get $p0
      i32.load offset=4
      call $DeleteTree
    end
    local.get $p0
    call $env.free)
  (func $_start (type $t2) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 f64) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    call $env.__VERIFIER_nondet_int
    local.set $l1
    block $B0 (result i32)
      call $env.__VERIFIER_nondet_int
      local.tee $l2
      i32.const 2
      i32.add
      local.tee $l0
      local.get $l1
      local.get $l0
      local.get $l1
      i32.gt_u
      select
      local.tee $l4
      i32.const 1
      i32.add
      local.tee $l0
      f64.convert_i32_u
      call $env.exp2
      local.tee $l3
      f64.abs
      f64.const 0x1p+31 (;=2.14748e+09;)
      f64.lt
      if $I1
        local.get $l3
        i32.trunc_f64_s
        br $B0
      end
      i32.const -2147483648
    end
    local.set $l1
    local.get $l0
    call $BottomUpTree
    local.tee $l0
    call $ItemCheck
    local.get $l1
    i32.eq
    call $env.assert
    local.get $l0
    call $DeleteTree
    local.get $l4
    call $BottomUpTree
    local.get $l2
    local.get $l4
    i32.le_u
    if $I2
      local.get $l2
      local.get $l4
      i32.add
      local.set $l7
      loop $L3
        i32.const 0
        local.set $l5
        block $B4 (result i32)
          local.get $l7
          local.get $l2
          i32.sub
          f64.convert_i32_u
          call $env.exp2
          local.tee $l3
          f64.abs
          f64.const 0x1p+31 (;=2.14748e+09;)
          f64.lt
          if $I5
            local.get $l3
            i32.trunc_f64_s
            br $B4
          end
          i32.const -2147483648
        end
        local.tee $l0
        i32.const 0
        i32.gt_s
        if $I6
          local.get $l0
          local.set $l1
          loop $L7
            local.get $l2
            call $BottomUpTree
            local.tee $l8
            call $ItemCheck
            local.set $l9
            local.get $l8
            call $DeleteTree
            local.get $l5
            local.get $l9
            i32.add
            local.set $l5
            local.get $l1
            i32.const 1
            i32.sub
            local.tee $l1
            br_if $L7
          end
        end
        block $B8 (result i32)
          local.get $l2
          f64.convert_i32_u
          call $env.exp2
          local.tee $l3
          f64.abs
          f64.const 0x1p+31 (;=2.14748e+09;)
          f64.lt
          if $I9
            local.get $l3
            i32.trunc_f64_s
            br $B8
          end
          i32.const -2147483648
        end
        local.get $l0
        i32.mul
        local.get $l5
        i32.eq
        call $env.assert
        local.get $l2
        i32.const 2
        i32.add
        local.tee $l2
        local.get $l4
        i32.le_u
        br_if $L3
      end
    end
    block $B10 (result i32)
      local.get $l4
      f64.convert_i32_u
      call $env.exp2
      local.tee $l3
      f64.abs
      f64.const 0x1p+31 (;=2.14748e+09;)
      f64.lt
      if $I11
        local.get $l3
        i32.trunc_f64_s
        br $B10
      end
      i32.const -2147483648
    end
    local.set $l5
    call $ItemCheck
    local.get $l5
    i32.eq
    call $env.assert
    i32.const 0)
  (memory $memory 2)
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
  (export "NewTreeNode" (func $NewTreeNode))
  (export "ItemCheck" (func $ItemCheck))
  (export "BottomUpTree" (func $BottomUpTree))
  (export "DeleteTree" (func $DeleteTree))
  (export "_start" (func $_start))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base)))
