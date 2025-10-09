(module $binarytrees.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (result i32)))
  (type $t3 (func (param f64 f64) (result f64)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32) (result i32)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "free" (func $free (type $t1)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t2)))
  (import "env" "pow" (func $pow (type $t3)))
  (import "env" "assert" (func $assert (type $t1)))
  (func $__wasm_call_ctors (type $t4)
    nop)
  (func $NewTreeNode (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l2
    global.set $__stack_pointer
    local.get $l2
    local.get $p0
    i32.store offset=12
    local.get $l2
    local.get $p1
    i32.store offset=8
    local.get $l2
    i32.const 8
    call $malloc
    i32.store offset=4
    local.get $l2
    i32.load offset=4
    local.get $l2
    i32.load offset=12
    i32.store
    local.get $l2
    i32.load offset=4
    local.get $l2
    i32.load offset=8
    i32.store offset=4
    local.get $l2
    i32.load offset=4
    local.set $l3
    local.get $l2
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get $l3)
  (func $ItemCheck (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store offset=8
    block $B0
      local.get $l1
      i32.load offset=8
      i32.load
      i32.eqz
      if $I1
        local.get $l1
        i32.const 1
        i32.store offset=12
        br $B0
      end
      local.get $l1
      local.get $l1
      i32.load offset=8
      i32.load
      call $ItemCheck
      i32.const 1
      i32.add
      local.get $l1
      i32.load offset=8
      i32.load offset=4
      call $ItemCheck
      i32.add
      i32.store offset=12
    end
    local.get $l1
    i32.load offset=12
    local.set $l2
    local.get $l1
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get $l2)
  (func $BottomUpTree (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store offset=8
    block $B0
      local.get $l1
      i32.load offset=8
      if $I1
        local.get $l1
        local.get $l1
        i32.load offset=8
        i32.const 1
        i32.sub
        call $BottomUpTree
        local.get $l1
        i32.load offset=8
        i32.const 1
        i32.sub
        call $BottomUpTree
        call $NewTreeNode
        i32.store offset=12
        br $B0
      end
      local.get $l1
      i32.const 0
      local.tee $l2
      local.get $l2
      call $NewTreeNode
      i32.store offset=12
    end
    local.get $l1
    i32.load offset=12
    local.set $l3
    local.get $l1
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get $l3)
  (func $DeleteTree (type $t1) (param $p0 i32)
    (local $l1 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store offset=12
    local.get $l1
    i32.load offset=12
    i32.load
    if $I0
      local.get $l1
      i32.load offset=12
      i32.load
      call $DeleteTree
      local.get $l1
      i32.load offset=12
      i32.load offset=4
      call $DeleteTree
    end
    local.get $l1
    i32.load offset=12
    call $free
    local.get $l1
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $_start (type $t2) (result i32)
    (local $l0 i32) (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64)
    global.get $__stack_pointer
    i32.const 48
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    local.get $l0
    call $__VERIFIER_nondet_int
    i32.store offset=44
    local.get $l0
    call $__VERIFIER_nondet_int
    i32.store offset=36
    block $B0
      local.get $l0
      i32.load offset=44
      local.get $l0
      i32.load offset=36
      i32.const 2
      i32.add
      i32.lt_u
      if $I1
        local.get $l0
        local.get $l0
        i32.load offset=36
        i32.const 2
        i32.add
        i32.store offset=32
        br $B0
      end
      local.get $l0
      local.get $l0
      i32.load offset=44
      i32.store offset=32
    end
    local.get $l0
    local.get $l0
    i32.load offset=32
    i32.const 1
    i32.add
    i32.store offset=28
    local.get $l0
    local.get $l0
    i32.load offset=28
    call $BottomUpTree
    i32.store offset=24
    local.get $l0
    i32.load offset=24
    call $ItemCheck
    block $B2 (result i32)
      f64.const 0x1p+1 (;=2;)
      local.get $l0
      i32.load offset=28
      f64.convert_i32_u
      call $pow
      local.tee $l1
      f64.abs
      f64.const 0x1p+31 (;=2.14748e+09;)
      f64.lt
      if $I3
        local.get $l1
        i32.trunc_f64_s
        br $B2
      end
      i32.const -2147483648
    end
    i32.eq
    call $assert
    local.get $l0
    i32.load offset=24
    call $DeleteTree
    local.get $l0
    local.get $l0
    i32.load offset=32
    call $BottomUpTree
    i32.store offset=20
    local.get $l0
    local.get $l0
    i32.load offset=36
    i32.store offset=40
    loop $L4
      local.get $l0
      i32.load offset=40
      local.get $l0
      i32.load offset=32
      i32.gt_u
      i32.eqz
      if $I5
        local.get $l0
        block $B6 (result i32)
          f64.const 0x1p+1 (;=2;)
          local.get $l0
          i32.load offset=36
          local.get $l0
          i32.load offset=32
          local.get $l0
          i32.load offset=40
          i32.sub
          i32.add
          f64.convert_i32_u
          call $pow
          local.tee $l2
          f64.abs
          f64.const 0x1p+31 (;=2.14748e+09;)
          f64.lt
          if $I7
            local.get $l2
            i32.trunc_f64_s
            br $B6
          end
          i32.const -2147483648
        end
        i32.store offset=8
        local.get $l0
        i32.const 0
        i32.store offset=4
        local.get $l0
        i32.const 1
        i32.store offset=12
        loop $L8
          local.get $l0
          i32.load offset=12
          local.get $l0
          i32.load offset=8
          i32.gt_s
          i32.eqz
          if $I9
            local.get $l0
            local.get $l0
            i32.load offset=40
            call $BottomUpTree
            i32.store offset=16
            local.get $l0
            local.get $l0
            i32.load offset=16
            call $ItemCheck
            local.get $l0
            i32.load offset=4
            i32.add
            i32.store offset=4
            local.get $l0
            i32.load offset=16
            call $DeleteTree
            local.get $l0
            local.get $l0
            i32.load offset=12
            i32.const 1
            i32.add
            i32.store offset=12
            br $L8
          end
        end
        block $B10 (result i32)
          f64.const 0x1p+1 (;=2;)
          local.get $l0
          i32.load offset=40
          f64.convert_i32_u
          call $pow
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
        local.get $l0
        i32.load offset=8
        i32.mul
        local.get $l0
        i32.load offset=4
        i32.eq
        call $assert
        local.get $l0
        local.get $l0
        i32.load offset=40
        i32.const 2
        i32.add
        i32.store offset=40
        br $L4
      end
    end
    local.get $l0
    i32.load offset=20
    call $ItemCheck
    block $B12 (result i32)
      f64.const 0x1p+1 (;=2;)
      local.get $l0
      i32.load offset=32
      f64.convert_i32_u
      call $pow
      local.tee $l4
      f64.abs
      f64.const 0x1p+31 (;=2.14748e+09;)
      f64.lt
      if $I13
        local.get $l4
        i32.trunc_f64_s
        br $B12
      end
      i32.const -2147483648
    end
    i32.eq
    call $assert
    local.get $l0
    i32.const 48
    i32.add
    global.set $__stack_pointer
    i32.const 0)
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
