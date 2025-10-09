(module $spectral-norm.wasm
  (type $t0 (func (param i32 i32 i32)))
  (type $t1 (func (param i32 i32) (result i32)))
  (type $t2 (func))
  (type $t3 (func (param i32 i32) (result f64)))
  (type $t4 (func (result i32)))
  (import "env" "printf" (func $printf (type $t1)))
  (func $__wasm_call_ctors (type $t2)
    nop)
  (func $eval_A (type $t3) (param $p0 i32) (param $p1 i32) (result f64)
    (local $l2 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l2
    local.get $p0
    i32.store offset=12
    local.get $l2
    local.get $p1
    i32.store offset=8
    f64.const 0x1p+0 (;=1;)
    local.get $l2
    i32.load offset=12
    local.get $l2
    i32.load offset=12
    local.get $l2
    i32.load offset=8
    i32.add
    local.get $l2
    i32.load offset=12
    local.get $l2
    i32.load offset=8
    i32.add
    i32.const 1
    i32.add
    i32.mul
    i32.const 2
    i32.div_s
    i32.add
    i32.const 1
    i32.add
    f64.convert_i32_s
    f64.div)
  (func $eval_A_times_u (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 f64) (local $l5 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee $l3
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=28
    local.get $l3
    local.get $p1
    i32.store offset=24
    local.get $l3
    local.get $p2
    i32.store offset=20
    local.get $l3
    i32.const 0
    i32.store offset=16
    loop $L0
      local.get $l3
      i32.load offset=16
      local.get $l3
      i32.load offset=28
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l3
        i32.load offset=20
        local.get $l3
        i32.load offset=16
        i32.const 3
        i32.shl
        i32.add
        f64.const 0x0p+0 (;=0;)
        f64.store
        local.get $l3
        i32.const 0
        i32.store offset=12
        loop $L2
          local.get $l3
          i32.load offset=12
          local.get $l3
          i32.load offset=28
          i32.ge_s
          i32.eqz
          if $I3
            local.get $l3
            i32.load offset=16
            local.get $l3
            i32.load offset=12
            call $eval_A
            local.set $l4
            local.get $l3
            i32.load offset=20
            local.get $l3
            i32.load offset=16
            i32.const 3
            i32.shl
            i32.add
            local.tee $l5
            local.get $l4
            local.get $l3
            i32.load offset=24
            local.get $l3
            i32.load offset=12
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.mul
            local.get $l5
            f64.load
            f64.add
            f64.store
            local.get $l3
            local.get $l3
            i32.load offset=12
            i32.const 1
            i32.add
            i32.store offset=12
            br $L2
          end
        end
        local.get $l3
        local.get $l3
        i32.load offset=16
        i32.const 1
        i32.add
        i32.store offset=16
        br $L0
      end
    end
    local.get $l3
    i32.const 32
    i32.add
    global.set $__stack_pointer)
  (func $eval_At_times_u (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 f64) (local $l5 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee $l3
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=28
    local.get $l3
    local.get $p1
    i32.store offset=24
    local.get $l3
    local.get $p2
    i32.store offset=20
    local.get $l3
    i32.const 0
    i32.store offset=16
    loop $L0
      local.get $l3
      i32.load offset=16
      local.get $l3
      i32.load offset=28
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l3
        i32.load offset=20
        local.get $l3
        i32.load offset=16
        i32.const 3
        i32.shl
        i32.add
        f64.const 0x0p+0 (;=0;)
        f64.store
        local.get $l3
        i32.const 0
        i32.store offset=12
        loop $L2
          local.get $l3
          i32.load offset=12
          local.get $l3
          i32.load offset=28
          i32.ge_s
          i32.eqz
          if $I3
            local.get $l3
            i32.load offset=12
            local.get $l3
            i32.load offset=16
            call $eval_A
            local.set $l4
            local.get $l3
            i32.load offset=20
            local.get $l3
            i32.load offset=16
            i32.const 3
            i32.shl
            i32.add
            local.tee $l5
            local.get $l4
            local.get $l3
            i32.load offset=24
            local.get $l3
            i32.load offset=12
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.mul
            local.get $l5
            f64.load
            f64.add
            f64.store
            local.get $l3
            local.get $l3
            i32.load offset=12
            i32.const 1
            i32.add
            i32.store offset=12
            br $L2
          end
        end
        local.get $l3
        local.get $l3
        i32.load offset=16
        i32.const 1
        i32.add
        i32.store offset=16
        br $L0
      end
    end
    local.get $l3
    i32.const 32
    i32.add
    global.set $__stack_pointer)
  (func $eval_AtA_times_u (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee $l4
    local.set $l3
    local.get $l4
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=28
    local.get $l3
    local.get $p1
    i32.store offset=24
    local.get $l3
    local.get $p2
    i32.store offset=20
    local.get $l3
    i32.load offset=28
    local.set $l5
    local.get $l3
    local.get $l4
    i32.store offset=16
    local.get $l4
    local.get $l5
    i32.const 3
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee $l6
    local.tee $l4
    global.set $__stack_pointer
    local.get $l3
    local.get $l5
    i32.store offset=12
    local.get $l3
    i32.load offset=28
    local.get $l3
    i32.load offset=24
    local.get $l6
    call $eval_A_times_u
    local.get $l3
    i32.load offset=28
    local.get $l6
    local.get $l3
    i32.load offset=20
    call $eval_At_times_u
    local.get $l3
    i32.load offset=16
    local.set $l4
    local.get $l3
    i32.const 32
    i32.add
    global.set $__stack_pointer)
  (func $_start (type $t4) (result i32)
    (local $l0 i32)
    global.get $__stack_pointer
    i32.const 1648
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    local.get $l0
    i32.const 100
    i32.store offset=1640
    local.get $l0
    local.get $l0
    i32.store offset=1636
    local.get $l0
    i32.const 0
    i32.store offset=1644
    loop $L0
      local.get $l0
      i32.load offset=1644
      i32.const 100
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l0
        i32.const 816
        i32.add
        local.get $l0
        i32.load offset=1644
        i32.const 3
        i32.shl
        i32.add
        f64.const 0x1p+0 (;=1;)
        f64.store
        local.get $l0
        local.get $l0
        i32.load offset=1644
        i32.const 1
        i32.add
        i32.store offset=1644
        br $L0
      end
    end
    local.get $l0
    i32.const 0
    i32.store offset=1644
    loop $L2
      local.get $l0
      i32.load offset=1644
      i32.const 10
      i32.ge_s
      i32.eqz
      if $I3
        i32.const 100
        local.get $l0
        i32.const 816
        i32.add
        local.get $l0
        i32.const 16
        i32.add
        call $eval_AtA_times_u
        i32.const 100
        local.get $l0
        i32.const 16
        i32.add
        local.get $l0
        i32.const 816
        i32.add
        call $eval_AtA_times_u
        local.get $l0
        local.get $l0
        i32.load offset=1644
        i32.const 1
        i32.add
        i32.store offset=1644
        br $L2
      end
    end
    local.get $l0
    f64.const 0x0p+0 (;=0;)
    f64.store offset=1616
    local.get $l0
    f64.const 0x0p+0 (;=0;)
    f64.store offset=1624
    local.get $l0
    i32.const 0
    i32.store offset=1644
    loop $L4
      local.get $l0
      i32.load offset=1644
      i32.const 100
      i32.ge_s
      i32.eqz
      if $I5
        local.get $l0
        local.get $l0
        i32.const 816
        i32.add
        local.get $l0
        i32.load offset=1644
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.get $l0
        i32.const 16
        i32.add
        local.get $l0
        i32.load offset=1644
        i32.const 3
        i32.shl
        i32.add
        f64.load
        f64.mul
        local.get $l0
        f64.load offset=1624
        f64.add
        f64.store offset=1624
        local.get $l0
        local.get $l0
        i32.const 16
        i32.add
        local.get $l0
        i32.load offset=1644
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.get $l0
        i32.const 16
        i32.add
        local.get $l0
        i32.load offset=1644
        i32.const 3
        i32.shl
        i32.add
        f64.load
        f64.mul
        local.get $l0
        f64.load offset=1616
        f64.add
        f64.store offset=1616
        local.get $l0
        local.get $l0
        i32.load offset=1644
        i32.const 1
        i32.add
        i32.store offset=1644
        br $L4
      end
    end
    local.get $l0
    local.get $l0
    f64.load offset=1624
    local.get $l0
    f64.load offset=1616
    f64.div
    f64.sqrt
    f64.store
    i32.const 1024
    local.get $l0
    call $printf
    drop
    local.get $l0
    i32.load offset=1636
    local.tee $l0
    i32.const 1648
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1031))
  (global $__stack_low i32 (i32.const 1040))
  (global $__stack_high i32 (i32.const 66576))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66576))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "eval_A" (func $eval_A))
  (export "eval_A_times_u" (func $eval_A_times_u))
  (export "eval_At_times_u" (func $eval_At_times_u))
  (export "eval_AtA_times_u" (func $eval_AtA_times_u))
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
  (data $.rodata (i32.const 1024) "%0.9f\0a"))
