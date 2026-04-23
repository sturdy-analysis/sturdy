(module $spectral-norm.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (result i32)))
  (import "env" "printf" (func $printf (type $t0)))
  (func $_start (type $t1) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 f64) (local $l12 f64) (local $l13 f64)
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
        i32.const 1
        local.set $l2
        local.get $l9
        i32.const 1
        i32.add
        local.set $l10
        f64.const 0x0p+0 (;=0;)
        local.set $l6
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
        local.get $l10
        local.set $l9
        local.get $l10
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
        i32.const 1
        local.set $l2
        local.get $l9
        i32.const 1
        i32.add
        local.set $l10
        f64.const 0x0p+0 (;=0;)
        local.set $l6
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
        local.get $l10
        local.set $l9
        local.get $l10
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
    local.set $l11
    i32.const 0
    local.set $l2
    f64.const 0x0p+0 (;=0;)
    local.set $l12
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
      local.tee $l13
      local.get $l13
      f64.mul
      local.get $l11
      f64.add
      f64.add
      local.set $l11
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
      local.get $l13
      f64.mul
      local.get $l12
      f64.add
      f64.add
      local.set $l12
      local.get $l2
      i32.const 16
      i32.add
      local.tee $l2
      i32.const 800
      i32.ne
      br_if $L10
    end
    local.get $l0
    local.get $l12
    local.get $l11
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
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "%0.9f\0a\00")
  (@custom "name" "\00\13\12spectral-norm.wasm\01\11\02\00\06printf\01\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
