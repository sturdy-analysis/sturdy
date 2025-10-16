(module
  (type $t0 (func (param i32 i32 i32)))
  (type $t1 (func (param i32 i32) (result i32)))
  (type $t2 (func))
  (type $t3 (func (param i32 i32) (result f64)))
  (type $t4 (func (result i32)))
  (import "env" "printf" (func $env.printf (type $t1)))
  (func $__wasm_call_ctors (type $t2))
  (func $eval_A (type $t3) (param $p0 i32) (param $p1 i32) (result f64)
    f64.const 0x1p+0 (;=1;)
    local.get $p0
    local.get $p0
    local.get $p1
    i32.add
    local.tee $p1
    i32.const 1
    i32.add
    local.get $p1
    i32.mul
    i32.const 2
    i32.div_s
    i32.add
    i32.const 1
    i32.add
    f64.convert_i32_s
    f64.div)
  (func $eval_A_times_u (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 f64) (local $l8 i32) (local $l9 i32)
    local.get $p0
    i32.const 0
    i32.gt_s
    if $I0
      loop $L1
        local.get $p2
        local.get $l3
        i32.const 3
        i32.shl
        i32.add
        local.tee $l8
        i64.const 0
        i64.store
        local.get $l3
        i32.const 1
        i32.add
        local.set $l6
        f64.const 0x0p+0 (;=0;)
        local.set $l7
        local.get $p1
        local.set $l4
        i32.const 0
        local.set $l5
        loop $L2
          local.get $l8
          f64.const 0x1p+0 (;=1;)
          local.get $l6
          local.get $l3
          local.get $l5
          i32.add
          local.tee $l9
          i32.const 1
          i32.add
          local.get $l9
          i32.mul
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l4
          f64.load
          f64.mul
          local.get $l7
          f64.add
          local.tee $l7
          f64.store
          local.get $l4
          i32.const 8
          i32.add
          local.set $l4
          local.get $p0
          local.get $l5
          i32.const 1
          i32.add
          local.tee $l5
          i32.ne
          br_if $L2
        end
        local.get $l6
        local.tee $l3
        local.get $p0
        i32.ne
        br_if $L1
      end
    end)
  (func $eval_At_times_u (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 f64) (local $l11 i32)
    local.get $p0
    i32.const 0
    i32.gt_s
    if $I0
      i32.const 2
      local.set $l3
      loop $L1
        local.get $p2
        local.get $l5
        i32.const 3
        i32.shl
        i32.add
        local.tee $l11
        i64.const 0
        i64.store
        f64.const 0x0p+0 (;=0;)
        local.set $l10
        local.get $l4
        local.set $l6
        local.get $l3
        local.set $l7
        local.get $p1
        local.set $l8
        i32.const 0
        local.set $l9
        loop $L2
          local.get $l11
          f64.const 0x1p+0 (;=1;)
          local.get $l9
          i32.const 1
          i32.add
          local.tee $l9
          local.get $l6
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l8
          f64.load
          f64.mul
          local.get $l10
          f64.add
          local.tee $l10
          f64.store
          local.get $l6
          local.get $l7
          i32.add
          local.set $l6
          local.get $l8
          i32.const 8
          i32.add
          local.set $l8
          local.get $l7
          i32.const 2
          i32.add
          local.set $l7
          local.get $p0
          local.get $l9
          i32.ne
          br_if $L2
        end
        local.get $l3
        local.get $l4
        i32.add
        local.set $l4
        local.get $l3
        i32.const 2
        i32.add
        local.set $l3
        local.get $l5
        i32.const 1
        i32.add
        local.tee $l5
        local.get $p0
        i32.ne
        br_if $L1
      end
    end)
  (func $eval_AtA_times_u (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 f64) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32)
    global.get $g0
    local.get $p0
    i32.const 3
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.set $l10
    local.get $p0
    i32.const 0
    i32.gt_s
    if $I0
      loop $L1
        local.get $l3
        i32.const 1
        i32.add
        local.set $l4
        local.get $l10
        local.get $l3
        i32.const 3
        i32.shl
        i32.add
        f64.const 0x0p+0 (;=0;)
        local.set $l5
        local.get $p1
        local.set $l6
        i32.const 0
        local.set $l7
        loop $L2
          f64.const 0x1p+0 (;=1;)
          local.get $l3
          local.get $l7
          i32.add
          local.tee $l9
          i32.const 1
          i32.add
          local.get $l9
          i32.mul
          i32.const 1
          i32.shr_u
          local.get $l4
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l6
          f64.load
          f64.mul
          local.get $l5
          f64.add
          local.set $l5
          local.get $l6
          i32.const 8
          i32.add
          local.set $l6
          local.get $p0
          local.get $l7
          i32.const 1
          i32.add
          local.tee $l7
          i32.ne
          br_if $L2
        end
        local.get $l5
        f64.store
        local.get $l4
        local.tee $l3
        local.get $p0
        i32.ne
        br_if $L1
      end
      i32.const 0
      local.set $l8
      i32.const 2
      local.set $l4
      i32.const 0
      local.set $p1
      loop $L3
        local.get $p2
        local.get $p1
        i32.const 3
        i32.shl
        i32.add
        f64.const 0x0p+0 (;=0;)
        local.set $l5
        local.get $l8
        local.set $l6
        local.get $l4
        local.set $l7
        local.get $l10
        local.set $l9
        i32.const 0
        local.set $l3
        loop $L4
          f64.const 0x1p+0 (;=1;)
          local.get $l3
          i32.const 1
          i32.add
          local.tee $l3
          local.get $l6
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l9
          f64.load
          f64.mul
          local.get $l5
          f64.add
          local.set $l5
          local.get $l6
          local.get $l7
          i32.add
          local.set $l6
          local.get $l9
          i32.const 8
          i32.add
          local.set $l9
          local.get $l7
          i32.const 2
          i32.add
          local.set $l7
          local.get $p0
          local.get $l3
          i32.ne
          br_if $L4
        end
        local.get $l5
        f64.store
        local.get $l4
        local.get $l8
        i32.add
        local.set $l8
        local.get $l4
        i32.const 2
        i32.add
        local.set $l4
        local.get $p1
        i32.const 1
        i32.add
        local.tee $p1
        local.get $p0
        i32.ne
        br_if $L3
      end
    end)
  (func $_start (type $t4) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 f64) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 f64) (local $l10 f64) (local $l11 i32) (local $l12 f64)
    global.get $g0
    i32.const 2416
    i32.sub
    local.tee $l5
    global.set $g0
    loop $L0
      local.get $l5
      i32.const 816
      i32.add
      local.get $l2
      i32.add
      local.tee $l0
      i64.const 4607182418800017408
      i64.store
      local.get $l0
      i32.const 32
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l0
      i32.const 24
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l0
      i32.const 16
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l0
      i32.const 8
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l2
      i32.const 40
      i32.add
      local.tee $l2
      i32.const 800
      i32.ne
      br_if $L0
    end
    loop $L1
      i32.const 0
      local.set $l4
      loop $L2
        local.get $l4
        i32.const 1
        i32.add
        local.set $l0
        i32.const 0
        local.set $l1
        f64.const 0x0p+0 (;=0;)
        local.set $l3
        local.get $l5
        i32.const 816
        i32.add
        local.set $l2
        loop $L3
          f64.const 0x1p+0 (;=1;)
          local.get $l1
          local.get $l4
          i32.add
          local.tee $l6
          i32.const 1
          i32.add
          local.get $l6
          i32.mul
          i32.const 1
          i32.shr_u
          local.get $l0
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l2
          f64.load
          f64.mul
          local.get $l3
          f64.add
          local.set $l3
          local.get $l2
          i32.const 8
          i32.add
          local.set $l2
          local.get $l1
          i32.const 1
          i32.add
          local.tee $l1
          i32.const 100
          i32.ne
          br_if $L3
        end
        local.get $l5
        i32.const 1616
        i32.add
        local.get $l4
        i32.const 3
        i32.shl
        i32.add
        local.get $l3
        f64.store
        local.get $l0
        local.tee $l4
        i32.const 100
        i32.ne
        br_if $L2
      end
      i32.const 2
      local.set $l7
      i32.const 0
      local.set $l6
      i32.const 0
      local.set $l8
      loop $L4
        f64.const 0x0p+0 (;=0;)
        local.set $l3
        i32.const 1
        local.set $l1
        local.get $l6
        local.set $l2
        local.get $l7
        local.set $l0
        i32.const 0
        local.set $l4
        loop $L5
          f64.const 0x1p+0 (;=1;)
          local.get $l1
          local.get $l2
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l5
          i32.const 1616
          i32.add
          local.get $l4
          i32.add
          f64.load
          f64.mul
          local.get $l3
          f64.add
          local.set $l3
          local.get $l0
          local.get $l2
          i32.add
          local.set $l2
          local.get $l0
          i32.const 2
          i32.add
          local.set $l0
          local.get $l1
          i32.const 1
          i32.add
          local.set $l1
          local.get $l4
          i32.const 8
          i32.add
          local.tee $l4
          i32.const 800
          i32.ne
          br_if $L5
        end
        local.get $l5
        i32.const 16
        i32.add
        local.get $l8
        i32.const 3
        i32.shl
        i32.add
        local.get $l3
        f64.store
        local.get $l6
        local.get $l7
        i32.add
        local.set $l6
        local.get $l7
        i32.const 2
        i32.add
        local.set $l7
        local.get $l8
        i32.const 1
        i32.add
        local.tee $l8
        i32.const 100
        i32.ne
        br_if $L4
      end
      i32.const 0
      local.set $l4
      loop $L6
        local.get $l4
        i32.const 1
        i32.add
        local.set $l0
        f64.const 0x0p+0 (;=0;)
        local.set $l3
        local.get $l5
        i32.const 16
        i32.add
        local.set $l1
        i32.const 0
        local.set $l2
        loop $L7
          f64.const 0x1p+0 (;=1;)
          local.get $l2
          local.get $l4
          i32.add
          local.tee $l6
          i32.const 1
          i32.add
          local.get $l6
          i32.mul
          i32.const 1
          i32.shr_u
          local.get $l0
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l1
          f64.load
          f64.mul
          local.get $l3
          f64.add
          local.set $l3
          local.get $l1
          i32.const 8
          i32.add
          local.set $l1
          local.get $l2
          i32.const 1
          i32.add
          local.tee $l2
          i32.const 100
          i32.ne
          br_if $L7
        end
        local.get $l5
        i32.const 1616
        i32.add
        local.get $l4
        i32.const 3
        i32.shl
        i32.add
        local.get $l3
        f64.store
        local.get $l0
        local.tee $l4
        i32.const 100
        i32.ne
        br_if $L6
      end
      i32.const 2
      local.set $l7
      i32.const 0
      local.set $l6
      i32.const 0
      local.set $l8
      loop $L8
        f64.const 0x0p+0 (;=0;)
        local.set $l3
        i32.const 1
        local.set $l1
        local.get $l6
        local.set $l2
        local.get $l7
        local.set $l0
        i32.const 0
        local.set $l4
        loop $L9
          f64.const 0x1p+0 (;=1;)
          local.get $l1
          local.get $l2
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l5
          i32.const 1616
          i32.add
          local.get $l4
          i32.add
          f64.load
          f64.mul
          local.get $l3
          f64.add
          local.set $l3
          local.get $l0
          local.get $l2
          i32.add
          local.set $l2
          local.get $l0
          i32.const 2
          i32.add
          local.set $l0
          local.get $l1
          i32.const 1
          i32.add
          local.set $l1
          local.get $l4
          i32.const 8
          i32.add
          local.tee $l4
          i32.const 800
          i32.ne
          br_if $L9
        end
        local.get $l5
        i32.const 816
        i32.add
        local.get $l8
        i32.const 3
        i32.shl
        i32.add
        local.get $l3
        f64.store
        local.get $l6
        local.get $l7
        i32.add
        local.set $l6
        local.get $l7
        i32.const 2
        i32.add
        local.set $l7
        local.get $l8
        i32.const 1
        i32.add
        local.tee $l8
        i32.const 100
        i32.ne
        br_if $L8
      end
      local.get $l11
      i32.const 1
      i32.add
      local.tee $l11
      i32.const 10
      i32.ne
      br_if $L1
    end
    i32.const 0
    local.set $l1
    loop $L10
      local.get $l5
      i32.const 16
      i32.add
      local.get $l1
      i32.add
      local.tee $l0
      i32.const 8
      i32.add
      f64.load
      local.tee $l12
      local.get $l12
      f64.mul
      local.get $l0
      f64.load
      local.tee $l3
      local.get $l3
      f64.mul
      local.get $l9
      f64.add
      f64.add
      local.set $l9
      local.get $l5
      i32.const 816
      i32.add
      local.get $l1
      i32.add
      local.tee $l0
      i32.const 8
      i32.add
      f64.load
      local.get $l12
      f64.mul
      local.get $l0
      f64.load
      local.get $l3
      f64.mul
      local.get $l10
      f64.add
      f64.add
      local.set $l10
      local.get $l1
      i32.const 16
      i32.add
      local.tee $l1
      i32.const 800
      i32.ne
      br_if $L10
    end
    local.get $l5
    local.get $l10
    local.get $l9
    f64.div
    f64.sqrt
    f64.store
    i32.const 1024
    local.get $l5
    call $env.printf
    drop
    local.get $l5
    i32.const 2416
    i32.add
    global.set $g0
    i32.const 0)
  (memory $memory 2)
  (global $g0 (mut i32) (i32.const 66576))
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
  (data $d0 (i32.const 1024) "%0.9f\0a"))
