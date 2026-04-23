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
  (data $d0 (i32.const 1024) "%0.9f\0a")
  (@custom ".debug_loc" "\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\08\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\02\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\07.\01\11\01\12\06@\18\97B\191\13\00\00\08\05\00\02\181\13\00\00\09.\01\03\0e:\0b;\0b'\19I\13?\19 \0b\00\00\0a\05\00\03\0e:\0b;\0bI\13\00\00\0b4\00\02\171\13\00\00\0c\1d\001\13\11\01\12\06X\0bY\0bW\0b\00\00\0d.\01\03\0e:\0b;\0b'\19?\19 \0b\00\00\0e4\00\03\0e:\0b;\0bI\13\00\00\0f\0f\00I\13\00\00\10&\00I\13\00\00\11\05\00\02\171\13\00\00\124\00\02\181\13\00\00\134\001\13\00\00\14\1d\011\13\11\01\12\06X\0bY\0bW\0b\00\00\154\00\03\0eI\134\19\00\00\16!\00I\137\13\00\00\17.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\184\00\1c\0d\03\0e:\0b;\0bI\13\00\00\194\00\02\17\03\0e:\0b;\0bI\13\00\00\1a\05\00\1c\0d1\13\00\00\1b4\00\1c\0f1\13\00\00\1c\89\82\01\001\13\11\01\00\00\1d.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\1e\05\00I\13\00\00\1f\18\00\00\00\00")
  (@custom ".debug_info" "\9c\04\00\00\04\00\00\00\00\00\04\01\f3\00\00\00\1d\00\c6\00\00\00\00\00\00\00g\00\00\00\00\00\00\00\00\00\00\00\023\00\00\00\010\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\07\00\05P\00\00\00\06\01\06\d6\00\00\00\08\07\07\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9fw\00\00\00\08\04\ed\00\00\9f\83\00\00\00\08\04\ed\00\01\9f\8e\00\00\00\00\09\ec\00\00\00\01\09\9a\00\00\00\01\0aW\00\00\00\01\09\a1\00\00\00\0aU\00\00\00\01\09\a1\00\00\00\00\05`\00\00\00\04\08\05L\00\00\00\05\04\07\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9fT\01\00\00\08\04\ed\00\00\9f\5c\01\00\00\08\04\ed\00\01\9fg\01\00\00\08\04\ed\00\02\9fr\01\00\00\0b\00\00\00\00}\01\00\00\0b\1d\00\00\00\88\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\11\1f\00\07\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\a3\01\00\00\08\04\ed\00\00\9f\ab\01\00\00\08\04\ed\00\01\9f\b6\01\00\00\08\04\ed\00\02\9f\c1\01\00\00\0bV\00\00\00\cc\01\00\00\0b\8f\00\00\00\d7\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\1b\1f\00\0d(\00\00\00\01\0b\01\0a\ea\00\00\00\01\0b\a1\00\00\00\0a:\00\00\00\01\0b\94\01\00\00\0a9\00\00\00\01\0b\9e\01\00\00\0eW\00\00\00\01\0d\a1\00\00\00\0eU\00\00\00\01\0d\a1\00\00\00\00\0f\99\01\00\00\10\9a\00\00\00\0f\9a\00\00\00\0d\07\00\00\00\01\15\01\0a\ea\00\00\00\01\15\a1\00\00\00\0a:\00\00\00\01\15\94\01\00\00\0a9\00\00\00\01\15\9e\01\00\00\0eW\00\00\00\01\17\a1\00\00\00\0eU\00\00\00\01\17\a1\00\00\00\00\07\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\a8\02\00\00\08\04\ed\00\00\9f\b0\02\00\00\11\03\01\00\00\bb\02\00\00\11\e5\00\00\00\c6\02\00\00\12\04\ed\00\00\9f\d1\02\00\00\13\da\02\00\00\14T\01\00\00\00\00\00\00\00\00\00\00\01 \10\08\04\ed\00\00\9f\5c\01\00\00\08\04\ed\00\01\9fg\01\00\00\08\04\ed\00\04\9fr\01\00\00\0b\c8\00\00\00}\01\00\00\0b!\01\00\00\88\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\11\1f\00\14\a3\01\00\00\00\00\00\00\00\00\00\00\01 '\0bZ\01\00\00\cc\01\00\00\0b\86\01\00\00\d7\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\1b\1f\00\00\0d\17\00\00\00\01\1f\01\0a\ea\00\00\00\01\1f\a1\00\00\00\0a:\00\00\00\01\1f\94\01\00\00\0a7\00\00\00\01\1f\9e\01\00\00\15\08\01\00\00\e6\02\00\00\0e\05\00\00\00\01 \ed\02\00\00\00\05C\00\00\00\07\04\03\9a\00\00\00\16F\00\00\00\14\02\00\00\00\17\00\00\00\00\00\00\00\00\04\ed\00\00\9f<\00\00\00\01\22\a1\00\00\00\18\e4\00\ea\00\00\00\01&\8e\04\00\00\19\b1\01\00\00W\00\00\00\01$\a1\00\00\00\19\8d\02\00\00\00\00\00\00\01'\9a\00\00\00\19\b9\02\00\00\03\00\00\00\01'\9a\00\00\00\0e:\00\00\00\01'\93\04\00\00\0e\05\00\00\00\01'\93\04\00\00\14\a8\02\00\00\00\00\00\00\00\00\00\00\01+\07\1a\e4\00\b0\02\00\00\1bd\d1\02\00\00\12\03\91\d0\0c\da\02\00\00\14T\01\00\00\00\00\00\00\00\00\00\00\01 \10\1a\e4\00\5c\01\00\00\0b\dd\01\00\00\88\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\11\1f\00\14\a3\01\00\00\00\00\00\00\00\00\00\00\01 '\0b\09\02\00\00\cc\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\1b\1f\00\00\14\a8\02\00\00\00\00\00\00\00\00\00\00\01,\07\1a\e4\00\b0\02\00\00\1bd\d1\02\00\00\12\03\91\d0\0c\da\02\00\00\14T\01\00\00\00\00\00\00\00\00\00\00\01 \10\1a\e4\00\5c\01\00\00\0b5\02\00\00\88\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\11\1f\00\14\a3\01\00\00\00\00\00\00\00\00\00\00\01 '\0ba\02\00\00\cc\01\00\00\0cw\00\00\00\00\00\00\00\00\00\00\00\01\1b\1f\00\00\1cr\04\00\00\00\00\00\00\00\1dY\00\00\00\02\f1\a1\00\00\00\1e\84\04\00\00\1f\00\0f\89\04\00\00\10?\00\00\00\10\a1\00\00\00\03\9a\00\00\00\04F\00\00\00d\00\00")
  (@custom ".debug_ranges" "\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "vv\00vBv\00eval_At_times_u\00eval_AtA_times_u\00eval_A_times_u\00AtAu\00_start\00unsigned int\00char\00j\00i\00printf\00double\00/home/sven/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00spectral-norm.c\00__ARRAY_SIZE_TYPE__\00N\00eval_A\00clang version 19.1.7\00__vla_expr0\00")
  (@custom ".debug_line" "A\00\00\00\04\00;\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00spectral-norm.c\00\00\00\00stdlib.h\00\01\00\00\00")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0619.1.7")
  (@custom "target_features" "\04+\0fmutable-globals+\08sign-ext+\0freference-types+\0amultivalue"))
