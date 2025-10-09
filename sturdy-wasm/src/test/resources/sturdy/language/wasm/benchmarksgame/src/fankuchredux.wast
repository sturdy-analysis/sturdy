(module $fankuchredux.wasm
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (param i32 i32 i32) (result i32)))
  (type $t3 (func (param i32 i32) (result i32)))
  (type $t4 (func))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t0)))
  (import "env" "fprintf" (func $fprintf (type $t2)))
  (import "env" "exit" (func $exit (type $t1)))
  (import "env" "printf" (func $printf (type $t3)))
  (func $__wasm_call_ctors (type $t4)
    nop)
  (func $flip (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l0
    i32.const 1104
    i32.store offset=8
    local.get $l0
    i32.const 1168
    i32.store offset=4
    local.get $l0
    i32.const 1232
    i32.load
    i32.store offset=12
    loop $L0
      local.get $l0
      local.get $l0
      i32.load offset=12
      local.tee $l1
      i32.const 1
      i32.sub
      i32.store offset=12
      local.get $l1
      if $I1
        local.get $l0
        local.get $l0
        i32.load offset=4
        local.tee $l2
        i32.const 4
        i32.add
        i32.store offset=4
        local.get $l2
        i32.load
        local.set $l3
        local.get $l0
        local.get $l0
        i32.load offset=8
        local.tee $l4
        i32.const 4
        i32.add
        i32.store offset=8
        local.get $l4
        local.get $l3
        i32.store
        br $L0
      end
    end
    local.get $l0
    i32.const 1
    i32.store offset=12
    loop $L2
      local.get $l0
      i32.const 1104
      i32.store offset=8
      local.get $l0
      i32.const 1104
      i32.load
      i32.const 2
      i32.shl
      i32.const 1104
      i32.add
      i32.store offset=4
      loop $L3
        local.get $l0
        i32.load offset=8
        local.get $l0
        i32.load offset=4
        i32.ge_u
        i32.eqz
        if $I4
          local.get $l0
          local.get $l0
          i32.load offset=8
          i32.load
          i32.store
          local.get $l0
          i32.load offset=4
          i32.load
          local.set $l5
          local.get $l0
          local.get $l0
          i32.load offset=8
          local.tee $l6
          i32.const 4
          i32.add
          i32.store offset=8
          local.get $l6
          local.get $l5
          i32.store
          local.get $l0
          i32.load
          local.set $l7
          local.get $l0
          local.get $l0
          i32.load offset=4
          local.tee $l8
          i32.const 4
          i32.sub
          i32.store offset=4
          local.get $l8
          local.get $l7
          i32.store
          br $L3
        end
      end
      local.get $l0
      local.get $l0
      i32.load offset=12
      i32.const 1
      i32.add
      i32.store offset=12
      i32.const 1104
      i32.load
      i32.const 2
      i32.shl
      i32.const 1104
      i32.add
      i32.load
      br_if $L2
    end
    local.get $l0
    i32.load offset=12)
  (func $rotate (type $t1) (param $p0 i32)
    (local $l1 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    local.get $p0
    i32.store offset=12
    local.get $l1
    i32.const 1168
    i32.load
    i32.store offset=8
    local.get $l1
    i32.const 1
    i32.store offset=4
    loop $L0
      local.get $l1
      i32.load offset=4
      local.get $l1
      i32.load offset=12
      i32.gt_s
      i32.eqz
      if $I1
        local.get $l1
        i32.load offset=4
        i32.const 2
        i32.shl
        i32.const 1164
        i32.add
        local.get $l1
        i32.load offset=4
        i32.const 2
        i32.shl
        i32.const 1168
        i32.add
        i32.load
        i32.store
        local.get $l1
        local.get $l1
        i32.load offset=4
        i32.const 1
        i32.add
        i32.store offset=4
        br $L0
      end
    end
    local.get $l1
    i32.load offset=12
    i32.const 2
    i32.shl
    i32.const 1168
    i32.add
    local.get $l1
    i32.load offset=8
    i32.store)
  (func $tk (type $t1) (param $p0 i32)
    (local $l1 i32) (local $l2 i64) (local $l3 i32) (local $l4 i32)
    global.get $__stack_pointer
    i32.const 80
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store offset=76
    local.get $l1
    i32.const 0
    i32.store offset=72
    local.get $l1
    i32.const 56
    i32.add
    i64.const 0
    local.tee $l2
    i64.store
    local.get $l1
    i32.const 48
    i32.add
    local.get $l2
    i64.store
    local.get $l1
    i32.const 40
    i32.add
    local.get $l2
    i64.store
    local.get $l1
    i32.const 32
    i32.add
    local.get $l2
    i64.store
    local.get $l1
    i32.const 24
    i32.add
    local.get $l2
    i64.store
    local.get $l1
    i32.const 16
    i32.add
    local.get $l2
    i64.store
    local.get $l1
    local.get $l2
    i64.store offset=8
    local.get $l1
    local.get $l2
    i64.store
    loop $L0
      local.get $l1
      i32.load offset=72
      local.get $l1
      i32.load offset=76
      i32.lt_s
      if $I1
        local.get $l1
        i32.load offset=72
        call $rotate
        local.get $l1
        i32.load offset=72
        i32.const 2
        i32.shl
        local.get $l1
        i32.add
        i32.load
        local.get $l1
        i32.load offset=72
        i32.ge_s
        if $I2
          local.get $l1
          local.get $l1
          i32.load offset=72
          local.tee $l3
          i32.const 1
          i32.add
          i32.store offset=72
          local.get $l3
          i32.const 2
          i32.shl
          local.get $l1
          i32.add
          i32.const 0
          i32.store
          br $L0
        end
        local.get $l1
        i32.load offset=72
        i32.const 2
        i32.shl
        local.get $l1
        i32.add
        local.tee $l4
        local.get $l4
        i32.load
        i32.const 1
        i32.add
        i32.store
        local.get $l1
        i32.const 1
        i32.store offset=72
        i32.const 1092
        i32.const 1092
        i32.load
        i32.const -1
        i32.xor
        i32.store
        i32.const 1168
        i32.load
        if $I3
          local.get $l1
          block $B4 (result i32)
            i32.const 1168
            i32.load
            i32.const 2
            i32.shl
            i32.const 1168
            i32.add
            i32.load
            if $I5
              call $flip
              br $B4
            end
            i32.const 1
          end
          i32.store offset=68
          local.get $l1
          i32.load offset=68
          i32.const 1088
          i32.load
          i32.gt_s
          if $I6
            i32.const 1088
            local.get $l1
            i32.load offset=68
            i32.store
          end
          i32.const 1096
          block $B7 (result i32)
            i32.const 1092
            i32.load
            if $I8
              i32.const 0
              local.get $l1
              i32.load offset=68
              i32.sub
              br $B7
            end
            local.get $l1
            i32.load offset=68
          end
          i32.const 1096
          i32.load
          i32.add
          i32.store
        end
        br $L0
      end
    end
    local.get $l1
    i32.const 80
    i32.add
    global.set $__stack_pointer)
  (func $_start (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    i32.const 1232
    call $__VERIFIER_nondet_int
    i32.store
    block $B0
      i32.const 1232
      i32.load
      i32.const 3
      i32.ge_s
      if $I1
        i32.const 1232
        i32.load
        i32.const 15
        i32.le_s
        br_if $B0
      end
      i32.const 0
      i32.load
      i32.const 1049
      i32.const 0
      call $fprintf
      drop
      i32.const 1
      call $exit
      unreachable
    end
    local.get $l0
    i32.const 0
    i32.store offset=12
    loop $L2
      local.get $l0
      i32.load offset=12
      i32.const 1232
      i32.load
      i32.ge_s
      i32.eqz
      if $I3
        local.get $l0
        i32.load offset=12
        i32.const 2
        i32.shl
        i32.const 1168
        i32.add
        local.get $l0
        i32.load offset=12
        i32.store
        local.get $l0
        local.get $l0
        i32.load offset=12
        i32.const 1
        i32.add
        i32.store offset=12
        br $L2
      end
    end
    i32.const 1232
    i32.load
    call $tk
    i32.const 1096
    i32.load
    local.set $l1
    i32.const 1232
    i32.load
    local.set $l2
    local.get $l0
    i32.const 1088
    i32.load
    i32.store offset=8
    local.get $l0
    local.get $l2
    i32.store offset=4
    local.get $l0
    local.get $l1
    i32.store
    i32.const 1024
    local.get $l0
    call $printf
    drop
    local.get $l0
    i32.const 16
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66784))
  (global $t i32 (i32.const 1104))
  (global $s i32 (i32.const 1168))
  (global $max_n i32 (i32.const 1232))
  (global $odd i32 (i32.const 1092))
  (global $maxflips i32 (i32.const 1088))
  (global $checksum i32 (i32.const 1096))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1236))
  (global $__stack_low i32 (i32.const 1248))
  (global $__stack_high i32 (i32.const 66784))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66784))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "flip" (func $flip))
  (export "t" (global $t))
  (export "s" (global $s))
  (export "max_n" (global $max_n))
  (export "rotate" (func $rotate))
  (export "tk" (func $tk))
  (export "odd" (global $odd))
  (export "maxflips" (global $maxflips))
  (export "checksum" (global $checksum))
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
  (data $.rodata (i32.const 1024) "%d\0aPfannkuchen(%d) = %d\0a\00range: must be 3 <= n <= 12\0a"))
