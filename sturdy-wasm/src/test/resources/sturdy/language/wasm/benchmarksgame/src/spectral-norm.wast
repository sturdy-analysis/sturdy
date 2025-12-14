(module $spectral-norm.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func))
  (type $t2 (func (param i32 i32) (result f64)))
  (type $t3 (func (param i32 i32 i32)))
  (type $t4 (func (result i32)))
  (import "env" "printf" (func $printf (type $t0)))
  (func $__wasm_call_ctors (type $t1))
  (func $eval_A (type $t2) (param $p0 i32) (param $p1 i32) (result f64)
    f64.const 0x1p+0 (;=1;)
    local.get $p0
    local.get $p1
    local.get $p0
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
  (func $eval_A_times_u (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    block $B0
      local.get $p0
      i32.const 1
      i32.lt_s
      br_if $B0
      i32.const 0
      local.set $l3
      loop $L1
        local.get $p2
        local.get $l3
        i32.const 3
        i32.shl
        i32.add
        local.tee $l4
        i64.const 0
        i64.store
        local.get $l3
        i32.const 1
        i32.add
        local.set $l5
        f64.const 0x0p+0 (;=0;)
        local.set $l6
        local.get $p1
        local.set $l7
        i32.const 0
        local.set $l8
        loop $L2
          local.get $l4
          f64.const 0x1p+0 (;=1;)
          local.get $l5
          local.get $l3
          local.get $l8
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
          local.get $l7
          f64.load
          f64.mul
          local.get $l6
          f64.add
          local.tee $l6
          f64.store
          local.get $l7
          i32.const 8
          i32.add
          local.set $l7
          local.get $p0
          local.get $l8
          i32.const 1
          i32.add
          local.tee $l8
          i32.ne
          br_if $L2
        end
        local.get $l5
        local.set $l3
        local.get $l5
        local.get $p0
        i32.ne
        br_if $L1
      end
    end)
  (func $eval_At_times_u (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 f64) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32)
    block $B0
      local.get $p0
      i32.const 1
      i32.lt_s
      br_if $B0
      i32.const 0
      local.set $l3
      i32.const 2
      local.set $l4
      i32.const 0
      local.set $l5
      loop $L1
        local.get $p2
        local.get $l5
        i32.const 3
        i32.shl
        i32.add
        local.tee $l6
        i64.const 0
        i64.store
        f64.const 0x0p+0 (;=0;)
        local.set $l7
        local.get $l3
        local.set $l8
        local.get $l4
        local.set $l9
        local.get $p1
        local.set $l10
        i32.const 0
        local.set $l11
        loop $L2
          local.get $l6
          f64.const 0x1p+0 (;=1;)
          local.get $l11
          i32.const 1
          i32.add
          local.tee $l11
          local.get $l8
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l10
          f64.load
          f64.mul
          local.get $l7
          f64.add
          local.tee $l7
          f64.store
          local.get $l8
          local.get $l9
          i32.add
          local.set $l8
          local.get $l10
          i32.const 8
          i32.add
          local.set $l10
          local.get $l9
          i32.const 2
          i32.add
          local.set $l9
          local.get $p0
          local.get $l11
          i32.ne
          br_if $L2
        end
        local.get $l3
        local.get $l4
        i32.add
        local.set $l3
        local.get $l4
        i32.const 2
        i32.add
        local.set $l4
        local.get $l5
        i32.const 1
        i32.add
        local.tee $l5
        local.get $p0
        i32.ne
        br_if $L1
      end
    end)
  (func $eval_AtA_times_u (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 f64) (local $l9 i32) (local $l10 i32) (local $l11 i32)
    global.get $__stack_pointer
    local.tee $l3
    drop
    local.get $l3
    local.get $p0
    i32.const 3
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee $l4
    drop
    block $B0
      local.get $p0
      i32.const 1
      i32.lt_s
      br_if $B0
      i32.const 0
      local.set $l5
      loop $L1
        local.get $l5
        i32.const 1
        i32.add
        local.set $l6
        local.get $l4
        local.get $l5
        i32.const 3
        i32.shl
        i32.add
        local.set $l7
        f64.const 0x0p+0 (;=0;)
        local.set $l8
        local.get $p1
        local.set $l3
        i32.const 0
        local.set $l9
        loop $L2
          f64.const 0x1p+0 (;=1;)
          local.get $l5
          local.get $l9
          i32.add
          local.tee $l10
          i32.const 1
          i32.add
          local.get $l10
          i32.mul
          i32.const 1
          i32.shr_u
          local.get $l6
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l3
          f64.load
          f64.mul
          local.get $l8
          f64.add
          local.set $l8
          local.get $l3
          i32.const 8
          i32.add
          local.set $l3
          local.get $p0
          local.get $l9
          i32.const 1
          i32.add
          local.tee $l9
          i32.ne
          br_if $L2
        end
        local.get $l7
        local.get $l8
        f64.store
        local.get $l6
        local.set $l5
        local.get $l6
        local.get $p0
        i32.ne
        br_if $L1
      end
      i32.const 0
      local.set $l7
      i32.const 2
      local.set $l6
      i32.const 0
      local.set $p1
      loop $L3
        local.get $p2
        local.get $p1
        i32.const 3
        i32.shl
        i32.add
        local.set $l11
        f64.const 0x0p+0 (;=0;)
        local.set $l8
        local.get $l7
        local.set $l3
        local.get $l6
        local.set $l9
        local.get $l4
        local.set $l10
        i32.const 0
        local.set $l5
        loop $L4
          f64.const 0x1p+0 (;=1;)
          local.get $l5
          i32.const 1
          i32.add
          local.tee $l5
          local.get $l3
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l10
          f64.load
          f64.mul
          local.get $l8
          f64.add
          local.set $l8
          local.get $l3
          local.get $l9
          i32.add
          local.set $l3
          local.get $l10
          i32.const 8
          i32.add
          local.set $l10
          local.get $l9
          i32.const 2
          i32.add
          local.set $l9
          local.get $p0
          local.get $l5
          i32.ne
          br_if $L4
        end
        local.get $l11
        local.get $l8
        f64.store
        local.get $l7
        local.get $l6
        i32.add
        local.set $l7
        local.get $l6
        i32.const 2
        i32.add
        local.set $l6
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
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 f64) (local $l11 f64) (local $l12 f64)
    global.get $__stack_pointer
    i32.const 2416
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    i32.const 0
    local.set $l1
    loop $L0
      local.get $l0
      i32.const 816
      i32.add
      local.get $l1
      i32.add
      local.tee $l2
      i64.const 4607182418800017408
      i64.store
      local.get $l2
      i32.const 32
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l2
      i32.const 24
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l2
      i32.const 16
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l2
      i32.const 8
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get $l1
      i32.const 40
      i32.add
      local.tee $l1
      i32.const 800
      i32.ne
      br_if $L0
    end
    i32.const 0
    local.set $l3
    loop $L1
      i32.const 0
      local.set $l4
      loop $L2
        local.get $l4
        i32.const 1
        i32.add
        local.set $l5
        i32.const 0
        local.set $l2
        f64.const 0x0p+0 (;=0;)
        local.set $l6
        local.get $l0
        i32.const 816
        i32.add
        local.set $l1
        loop $L3
          f64.const 0x1p+0 (;=1;)
          local.get $l4
          local.get $l2
          i32.add
          local.tee $l7
          i32.const 1
          i32.add
          local.get $l7
          i32.mul
          i32.const 1
          i32.shr_u
          local.get $l5
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l1
          f64.load
          f64.mul
          local.get $l6
          f64.add
          local.set $l6
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
          br_if $L3
        end
        local.get $l0
        i32.const 1616
        i32.add
        local.get $l4
        i32.const 3
        i32.shl
        i32.add
        local.get $l6
        f64.store
        local.get $l5
        local.set $l4
        local.get $l5
        i32.const 100
        i32.ne
        br_if $L2
      end
      i32.const 2
      local.set $l5
      i32.const 0
      local.set $l8
      i32.const 0
      local.set $l9
      loop $L4
        f64.const 0x0p+0 (;=0;)
        local.set $l6
        i32.const 1
        local.set $l2
        local.get $l8
        local.set $l1
        local.get $l5
        local.set $l7
        i32.const 0
        local.set $l4
        loop $L5
          f64.const 0x1p+0 (;=1;)
          local.get $l2
          local.get $l1
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l0
          i32.const 1616
          i32.add
          local.get $l4
          i32.add
          f64.load
          f64.mul
          local.get $l6
          f64.add
          local.set $l6
          local.get $l1
          local.get $l7
          i32.add
          local.set $l1
          local.get $l7
          i32.const 2
          i32.add
          local.set $l7
          local.get $l2
          i32.const 1
          i32.add
          local.set $l2
          local.get $l4
          i32.const 8
          i32.add
          local.tee $l4
          i32.const 800
          i32.ne
          br_if $L5
        end
        local.get $l0
        i32.const 16
        i32.add
        local.get $l9
        i32.const 3
        i32.shl
        i32.add
        local.get $l6
        f64.store
        local.get $l8
        local.get $l5
        i32.add
        local.set $l8
        local.get $l5
        i32.const 2
        i32.add
        local.set $l5
        local.get $l9
        i32.const 1
        i32.add
        local.tee $l9
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
        local.set $l5
        f64.const 0x0p+0 (;=0;)
        local.set $l6
        local.get $l0
        i32.const 16
        i32.add
        local.set $l2
        i32.const 0
        local.set $l1
        loop $L7
          f64.const 0x1p+0 (;=1;)
          local.get $l4
          local.get $l1
          i32.add
          local.tee $l7
          i32.const 1
          i32.add
          local.get $l7
          i32.mul
          i32.const 1
          i32.shr_u
          local.get $l5
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l2
          f64.load
          f64.mul
          local.get $l6
          f64.add
          local.set $l6
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
          br_if $L7
        end
        local.get $l0
        i32.const 1616
        i32.add
        local.get $l4
        i32.const 3
        i32.shl
        i32.add
        local.get $l6
        f64.store
        local.get $l5
        local.set $l4
        local.get $l5
        i32.const 100
        i32.ne
        br_if $L6
      end
      i32.const 2
      local.set $l5
      i32.const 0
      local.set $l8
      i32.const 0
      local.set $l9
      loop $L8
        f64.const 0x0p+0 (;=0;)
        local.set $l6
        i32.const 1
        local.set $l2
        local.get $l8
        local.set $l1
        local.get $l5
        local.set $l7
        i32.const 0
        local.set $l4
        loop $L9
          f64.const 0x1p+0 (;=1;)
          local.get $l2
          local.get $l1
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get $l0
          i32.const 1616
          i32.add
          local.get $l4
          i32.add
          f64.load
          f64.mul
          local.get $l6
          f64.add
          local.set $l6
          local.get $l1
          local.get $l7
          i32.add
          local.set $l1
          local.get $l7
          i32.const 2
          i32.add
          local.set $l7
          local.get $l2
          i32.const 1
          i32.add
          local.set $l2
          local.get $l4
          i32.const 8
          i32.add
          local.tee $l4
          i32.const 800
          i32.ne
          br_if $L9
        end
        local.get $l0
        i32.const 816
        i32.add
        local.get $l9
        i32.const 3
        i32.shl
        i32.add
        local.get $l6
        f64.store
        local.get $l8
        local.get $l5
        i32.add
        local.set $l8
        local.get $l5
        i32.const 2
        i32.add
        local.set $l5
        local.get $l9
        i32.const 1
        i32.add
        local.tee $l9
        i32.const 100
        i32.ne
        br_if $L8
      end
      local.get $l3
      i32.const 1
      i32.add
      local.tee $l3
      i32.const 10
      i32.ne
      br_if $L1
    end
    f64.const 0x0p+0 (;=0;)
    local.set $l10
    i32.const 0
    local.set $l2
    f64.const 0x0p+0 (;=0;)
    local.set $l11
    loop $L10
      local.get $l0
      i32.const 16
      i32.add
      local.get $l2
      i32.add
      local.tee $l1
      i32.const 8
      i32.add
      f64.load
      local.tee $l6
      local.get $l6
      f64.mul
      local.get $l1
      f64.load
      local.tee $l12
      local.get $l12
      f64.mul
      local.get $l10
      f64.add
      f64.add
      local.set $l10
      local.get $l0
      i32.const 816
      i32.add
      local.get $l2
      i32.add
      local.tee $l1
      i32.const 8
      i32.add
      f64.load
      local.get $l6
      f64.mul
      local.get $l1
      f64.load
      local.get $l12
      f64.mul
      local.get $l11
      f64.add
      f64.add
      local.set $l11
      local.get $l2
      i32.const 16
      i32.add
      local.tee $l2
      i32.const 800
      i32.ne
      br_if $L10
    end
    local.get $l0
    local.get $l11
    local.get $l10
    f64.div
    f64.sqrt
    f64.store
    i32.const 1024
    local.get $l0
    call $printf
    drop
    local.get $l0
    i32.const 2416
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
  (data $.rodata (i32.const 1024) "%0.9f\0a\00")
  (@custom ".debug_loc" "\ff\ff\ff\ff*\00\00\00\00\00\00\00\14\00\00\00\03\00\11\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff*\00\00\00%\00\00\00?\00\00\00\03\00\11\00\9f}\00\00\00\7f\00\00\00\04\00\ed\02\01\9f\7f\00\00\00\8e\00\00\00\04\00\ed\00\08\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\bd\00\00\00\00\00\00\00\1c\00\00\00\03\00\11\00\9f\a2\00\00\00\a4\00\00\00\04\00\ed\02\00\9f\a4\00\00\00\a9\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\bd\00\00\00\1e\00\00\00H\00\00\00\03\00\11\00\9fZ\00\00\00\5c\00\00\00\04\00\ed\02\02\9f\5c\00\00\00\a9\00\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffk\01\00\00\10\00\00\00.\00\00\00\03\00\11\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffk\01\00\00\00\00\00\00?\01\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffk\01\00\00\00\00\00\00\b2\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffk\01\00\000\00\00\00T\00\00\00\03\00\11\00\9f\8d\00\00\00\8f\00\00\00\04\00\ed\02\01\9f\8f\00\00\00\b2\00\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffk\01\00\005\01\00\007\01\00\00\04\00\ed\02\00\9f7\01\00\00<\01\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffk\01\00\00\c9\00\00\00\d9\00\00\00\03\00\11\00\9f\eb\00\00\00\05\01\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ac\02\00\00\dc\02\00\00\de\02\00\00\04\00\ed\02\00\9f\de\02\00\00\fe\02\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ac\02\00\00\f0\00\00\00\f2\00\00\00\04\00\ed\02\00\9f\f2\00\00\00#\01\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ac\02\00\00\ad\01\00\00\af\01\00\00\04\00\ed\02\00\9f\af\01\00\00\ba\01\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ac\02\00\00\10\02\00\00\12\02\00\00\04\00\ed\02\00\9f\12\02\00\00C\02\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ac\02\00\00\ce\02\00\00\d0\02\00\00\04\00\ed\02\00\9f\d0\02\00\00\e3\02\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ac\02\00\00\22\03\00\00#\03\00\00\04\00\ed\02\01\9f%\03\00\00~\03\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ac\02\00\00D\03\00\00E\03\00\00\04\00\ed\02\01\9fG\03\00\00~\03\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\07.\01\11\01\12\06@\18\97B\191\13\00\00\08\05\00\02\181\13\00\00\09.\01\03\0e:\0b;\0b'\19I\13?\19 \0b\00\00\0a\05\00\03\0e:\0b;\0bI\13\00\00\0b4\00\02\171\13\00\00\0c\1d\001\13\11\01\12\06X\0bY\0bW\0b\00\00\0d.\01\03\0e:\0b;\0b'\19?\19 \0b\00\00\0e4\00\03\0e:\0b;\0bI\13\00\00\0f\0f\00I\13\00\00\10&\00I\13\00\00\11\05\00\02\171\13\00\00\124\00\02\181\13\00\00\134\001\13\00\00\14\1d\011\13\11\01\12\06X\0bY\0bW\0b\00\00\154\00\03\0eI\134\19\00\00\16!\00I\137\13\00\00\17.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\184\00\1c\0d\03\0e:\0b;\0bI\13\00\00\194\00\02\17\03\0e:\0b;\0bI\13\00\00\1a\05\00\1c\0d1\13\00\00\1b4\00\1c\0f1\13\00\00\1c\89\82\01\001\13\11\01\00\00\1d.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\1e\05\00I\13\00\00\1f\18\00\00\00\00")
  (@custom ".debug_info" "\9c\04\00\00\04\00\00\00\00\00\04\01\0a\01\00\00\1d\00\d1\00\00\00\00\00\00\00g\00\00\00\00\00\00\00\00\00\00\00\023\00\00\00\010\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\07\00\05P\00\00\00\06\01\06\e1\00\00\00\08\07\07\05\00\00\00#\00\00\00\07\ed\03\00\00\00\00\9fw\00\00\00\08\04\ed\00\00\9f\83\00\00\00\08\04\ed\00\01\9f\8e\00\00\00\00\09\f7\00\00\00\01\09\9a\00\00\00\01\0aW\00\00\00\01\09\a1\00\00\00\0aU\00\00\00\01\09\a1\00\00\00\00\05`\00\00\00\04\08\05L\00\00\00\05\04\07*\00\00\00\91\00\00\00\07\ed\03\00\00\00\00\9fT\01\00\00\08\04\ed\00\00\9f\5c\01\00\00\08\04\ed\00\01\9fg\01\00\00\08\04\ed\00\02\9fr\01\00\00\0b\00\00\00\00}\01\00\00\0b\1d\00\00\00\88\01\00\00\0cw\00\00\00v\00\00\00\15\00\00\00\01\11\1f\00\07\bd\00\00\00\ac\00\00\00\07\ed\03\00\00\00\00\9f\a3\01\00\00\08\04\ed\00\00\9f\ab\01\00\00\08\04\ed\00\01\9f\b6\01\00\00\08\04\ed\00\02\9f\c1\01\00\00\0bV\00\00\00\cc\01\00\00\0b\8f\00\00\00\d7\01\00\00\0cw\00\00\00\16\01\00\00\0b\00\00\00\01\1b\1f\00\0d(\00\00\00\01\0b\01\0a\f5\00\00\00\01\0b\a1\00\00\00\0a:\00\00\00\01\0b\94\01\00\00\0a9\00\00\00\01\0b\9e\01\00\00\0eW\00\00\00\01\0d\a1\00\00\00\0eU\00\00\00\01\0d\a1\00\00\00\00\0f\99\01\00\00\10\9a\00\00\00\0f\9a\00\00\00\0d\07\00\00\00\01\15\01\0a\f5\00\00\00\01\15\a1\00\00\00\0a:\00\00\00\01\15\94\01\00\00\0a9\00\00\00\01\15\9e\01\00\00\0eW\00\00\00\01\17\a1\00\00\00\0eU\00\00\00\01\17\a1\00\00\00\00\07k\01\00\00?\01\00\00\07\ed\03\00\00\00\00\9f\a8\02\00\00\08\04\ed\00\00\9f\b0\02\00\00\11\03\01\00\00\bb\02\00\00\11\e5\00\00\00\c6\02\00\00\12\04\ed\00\00\9f\d1\02\00\00\13\da\02\00\00\14T\01\00\00\92\01\00\00\8b\00\00\00\01 \10\08\04\ed\00\00\9f\5c\01\00\00\08\04\ed\00\01\9fg\01\00\00\08\04\ed\00\04\9fr\01\00\00\0b\c8\00\00\00}\01\00\00\0b!\01\00\00\88\01\00\00\0cw\00\00\00\ca\01\00\00\15\00\00\00\01\11\1f\00\14\a3\01\00\00%\02\00\00\84\00\00\00\01 '\0bZ\01\00\00\cc\01\00\00\0b\86\01\00\00\d7\01\00\00\0cw\00\00\00S\02\00\00\0b\00\00\00\01\1b\1f\00\00\0d\17\00\00\00\01\1f\01\0a\f5\00\00\00\01\1f\a1\00\00\00\0a:\00\00\00\01\1f\94\01\00\00\0a7\00\00\00\01\1f\9e\01\00\00\15\fe\00\00\00\e6\02\00\00\0e\05\00\00\00\01 \ed\02\00\00\00\05C\00\00\00\07\04\03\9a\00\00\00\16F\00\00\00\14\02\00\00\00\17\ac\02\00\00~\03\00\00\04\ed\00\00\9f<\00\00\00\01\22\a1\00\00\00\18\e4\00\f5\00\00\00\01&\8e\04\00\00\19\b1\01\00\00W\00\00\00\01$\a1\00\00\00\19\8d\02\00\00\00\00\00\00\01'\9a\00\00\00\19\b9\02\00\00\03\00\00\00\01'\9a\00\00\00\0e:\00\00\00\01'\93\04\00\00\0e\05\00\00\00\01'\93\04\00\00\14\a8\02\00\00p\03\00\00\f6\00\00\00\01+\07\1a\e4\00\b0\02\00\00\1bd\d1\02\00\00\12\03\91\d0\0c\da\02\00\00\14T\01\00\00p\03\00\00_\00\00\00\01 \10\1a\e4\00\5c\01\00\00\0b\dd\01\00\00\88\01\00\00\0cw\00\00\00p\03\00\00\15\00\00\00\01\11\1f\00\14\a3\01\00\00\fd\03\00\00i\00\00\00\01 '\0b\09\02\00\00\cc\01\00\00\0cw\00\00\00\fd\03\00\00\04\00\00\00\01\1b\1f\00\00\14\a8\02\00\00\90\04\00\00\f7\00\00\00\01,\07\1a\e4\00\b0\02\00\00\1bd\d1\02\00\00\12\03\91\d0\0c\da\02\00\00\14T\01\00\00\90\04\00\00_\00\00\00\01 \10\1a\e4\00\5c\01\00\00\0b5\02\00\00\88\01\00\00\0cw\00\00\00\90\04\00\00\15\00\00\00\01\11\1f\00\14\a3\01\00\00\1d\05\00\00j\00\00\00\01 '\0ba\02\00\00\cc\01\00\00\0cw\00\00\00\1d\05\00\00\04\00\00\00\01\1b\1f\00\00\1cr\04\00\00\1a\06\00\00\00\1dY\00\00\00\02\f3\a1\00\00\00\1e\84\04\00\00\1f\00\0f\89\04\00\00\10?\00\00\00\10\a1\00\00\00\03\9a\00\00\00\04F\00\00\00d\00\00")
  (@custom ".debug_ranges" "\05\00\00\00(\00\00\00*\00\00\00\bb\00\00\00\bd\00\00\00i\01\00\00k\01\00\00\aa\02\00\00\ac\02\00\00*\06\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "vv\00vBv\00eval_At_times_u\00eval_AtA_times_u\00eval_A_times_u\00AtAu\00_start\00unsigned int\00char\00j\00i\00printf\00double\00/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00spectral-norm.c\00__ARRAY_SIZE_TYPE__\00N\00eval_A\00__vla_expr0\00clang version 19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)\00")
  (@custom ".debug_line" "\89\03\00\00\04\00;\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00spectral-norm.c\00\00\00\00stdlib.h\00\01\00\00\00\00\05\02\05\00\00\00\1a\05-\0a\9e\055\06\ac\050 \058X\05: \05<<\05* \05) \05\1f \02\01\00\01\01\00\05\02*\00\00\00\03\0b\01\05\0c\0a\ca\05\03\06 \03r.\05\07\06\03\10\ba\05\0c\06f\03p<\055\06\03\09\02'\01\050\06\ac\058f\05< \05* \05) \05+\06(\05\1d\06X\05\10\c8\05\14\90\05\10 \05\07<\03o<\05\0c\06\03\0eJ\05\03\06X\05\01\06O\02\01\00\01\01\00\05\02\bd\00\00\00\03\15\01\05\0c\0a\ca\05\03\06 \03h.\05\07\06\03\1a\08<\05\00\06\03ff\05:\06\03\09\02/\01\06\03w \05-\03\09f\05* \05).\05+\06\03\12 \05\1d\06X\05\00\03eJ\05\10\03\1bX\05\07\08\90\05\03\069\05\10\06\08 \05\0c \05\03X\05\01\06O\02\01\00\01\01\00\05\02k\01\00\00\03\1f\01\05\03\0a\08X\06\03`\ac\05\0c\06\03\0ef\05\03\06 \03r.\05\07\06\03\10\08.\06\03p\08f\055\06\03\09\ac\050\06\90\058f\05< \05*<\05) \05+\06(\05\1d\06X\05\10\9e\05\14\90\05\10 \05\07<\05\1d<\03ot\05\0c\06\03\0eJ\05\03\06X\03r\e4\05\07\06\03\1a\82\06\03f\e4\05:\06\03\09\08\d6\06\03w<\05-\03\09J\05* \05).\05+\06\03\12 \05\1d\06X\05\10f\03et\03\1bJ\05\07\e4\05\00\03e<\05\03\06\03\18t\05\10\06\08 \05\0c \05\03X\05B\06R\02\01\00\01\01\00\05\02\ac\02\00\00\03\22\01\05\18\0a\08\db\06\03X\08\90\03(J\03X\d6\03(J\03X\d6\03(J\03X\d6\03(J\03X\d6\05\0c\03(J\05\03t\03Xt\055\06\03\09\021\01\050\06\90\058f\05< \05*<\05) \05+\06(\05\1d\06X\05\10\9e\05\14t\03o \05\10\03\11X\05\07 \06;\05\1d\bb\06\03oX\05\0c\06\03\0e\90\05\03\06 \03r\e4\05-\06\03\09\02.\01\05*\06.\05) \05+\06\03\12 \05\1d\06\ba\05\10f\05\07\08\e4\06;\05\00\06\03f\ac\05\03\06\03\18X\05\10\06\08 \03h \05\0c\03\18X\05\03 \03ht\055\06\03\09\02*\01\050\06\90\058f\05< \05*<\05) \05+\06(\05\1d\06X\05\10\9e\05\14t\03o \05\10\03\11X\05\07 \06;\05\1d\bb\06\03oX\05\0c\06\03\0e\90\05\03\06 \03r\e4\05-\06\03\09\02.\01\05*\06.\05) \05+\06\03\12 \05\1d\06\ba\05\10f\05\07\08\e4\06;\05\00\06\03f\ba\05\03\06\03\18X\05\10\06\08 \03h \05\0c\03\18X\05\03 \05\11\06\03\11t\06\03W \05\0c\03)J\05\03 \05 \06\08\c0\05(\06\08 \05 X\05(X\05\1b\ac\05\19\08\12\05\1b<\05\19X\03Q\90\05\0c\03/J\05\03t\05\1c\06=\05\14\06t\05\03 \05\01\06\08\22\02\0f\00\01\01")
  (@custom "name" "\00\13\12spectral-norm.wasm\01_\07\00\06printf\01\11__wasm_call_ctors\02\06eval_A\03\0eeval_A_times_u\04\0feval_At_times_u\05\10eval_AtA_times_u\06\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clangZ19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)")
  (@custom "target_features" "\04+\0amultivalue+\0fmutable-globals+\0freference-types+\08sign-ext"))
