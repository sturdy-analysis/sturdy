(module
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32 i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (result i32)))
  (import "env" "malloc" (func $env.malloc (type $t0)))
  (import "env" "fprintf" (func $env.fprintf (type $t1)))
  (import "env" "fwrite" (func $env.fwrite (type $t2)))
  (import "env" "free" (func $env.free (type $t3)))
  (func $__wasm_call_ctors (type $t4))
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 f64) (local $l3 i32) (local $l4 f64) (local $l5 i32) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 i32) (local $l16 i32) (local $l17 f64) (local $l18 f64) (local $l19 f64) (local $l20 f64) (local $l21 f64) (local $l22 f64) (local $l23 i32) (local $l24 i32) (local $l25 i32) (local $l26 f64) (local $l27 f64) (local $l28 f64) (local $l29 f64) (local $l30 f64) (local $l31 f64) (local $l32 f64) (local $l33 f64) (local $l34 f64) (local $l35 f64) (local $l36 f64) (local $l37 f64) (local $l38 f64) (local $l39 f64) (local $l40 f64) (local $l41 f64) (local $l42 f64) (local $l43 f64) (local $l44 f64) (local $l45 f64) (local $l46 f64) (local $l47 f64) (local $l48 f64) (local $l49 f64)
    global.get $g0
    i32.const 8208
    i32.sub
    local.tee $l3
    global.set $g0
    i32.const 1
    local.set $l1
    local.get $l3
    i32.const 4112
    i32.add
    local.set $l0
    local.get $l3
    i32.const 16
    i32.add
    local.set $l5
    i32.const 32768
    call $env.malloc
    local.set $l23
    loop $L0
      local.get $l5
      local.get $l4
      f64.const 0x1p-8 (;=0.00390625;)
      f64.mul
      local.tee $l6
      f64.const -0x1p+0 (;=-1;)
      f64.add
      f64.store
      local.get $l0
      local.get $l6
      f64.const -0x1.8p+0 (;=-1.5;)
      f64.add
      f64.store
      local.get $l0
      i32.const 8
      i32.add
      local.get $l1
      f64.convert_i32_u
      f64.const 0x1p-8 (;=0.00390625;)
      f64.mul
      local.tee $l6
      f64.const -0x1.8p+0 (;=-1.5;)
      f64.add
      f64.store
      local.get $l5
      i32.const 8
      i32.add
      local.get $l6
      f64.const -0x1p+0 (;=-1;)
      f64.add
      f64.store
      local.get $l5
      i32.const 16
      i32.add
      local.set $l5
      local.get $l0
      i32.const 16
      i32.add
      local.set $l0
      local.get $l4
      f64.const 0x1p+1 (;=2;)
      f64.add
      local.set $l4
      local.get $l1
      i32.const 2
      i32.add
      local.tee $l1
      i32.const 513
      i32.ne
      br_if $L0
    end
    loop $L1
      local.get $l23
      local.get $l15
      i32.const 6
      i32.shl
      i32.add
      local.set $l25
      local.get $l3
      i32.const 16
      i32.add
      local.get $l15
      i32.const 3
      i32.shl
      i32.add
      f64.load
      local.set $l2
      i32.const 0
      local.set $l24
      i32.const 0
      local.set $l16
      loop $L2
        local.get $l3
        i32.const 4112
        i32.add
        local.tee $l0
        local.get $l16
        i32.const 3
        i32.shl
        i32.add
        local.tee $l1
        f64.load
        local.set $l26
        local.get $l24
        i32.const 6
        i32.shl
        local.get $l0
        i32.add
        local.tee $l0
        f64.load offset=56
        local.set $l7
        local.get $l0
        f64.load offset=48
        local.set $l8
        local.get $l0
        f64.load offset=40
        local.set $l9
        local.get $l0
        f64.load offset=32
        local.set $l10
        local.get $l0
        f64.load offset=24
        local.set $l11
        local.get $l0
        f64.load offset=16
        local.set $l12
        local.get $l0
        f64.load offset=8
        local.set $l13
        local.get $l0
        f64.load
        local.set $l14
        local.get $l1
        i32.const 56
        i32.add
        f64.load
        local.set $l27
        local.get $l1
        i32.const 48
        i32.add
        f64.load
        local.set $l28
        local.get $l1
        i32.const 40
        i32.add
        f64.load
        local.set $l29
        local.get $l1
        i32.const 32
        i32.add
        f64.load
        local.set $l30
        local.get $l1
        i32.const 24
        i32.add
        f64.load
        local.set $l31
        local.get $l1
        i32.const 16
        i32.add
        f64.load
        local.set $l32
        local.get $l1
        i32.const 8
        i32.add
        f64.load
        local.set $l33
        i32.const 255
        local.set $l5
        i32.const -4
        local.set $l1
        local.get $l2
        local.tee $l4
        local.tee $l6
        local.tee $l17
        local.tee $l18
        local.tee $l19
        local.tee $l20
        local.tee $l21
        local.set $l22
        loop $L3
          i32.const 127
          i32.const -1
          local.get $l14
          local.get $l14
          f64.mul
          local.tee $l34
          local.get $l22
          local.get $l22
          f64.mul
          local.tee $l35
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          local.get $l5
          i32.and
          i32.const -65
          i32.const -1
          local.get $l13
          local.get $l13
          f64.mul
          local.tee $l36
          local.get $l21
          local.get $l21
          f64.mul
          local.tee $l37
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -33
          i32.const -1
          local.get $l12
          local.get $l12
          f64.mul
          local.tee $l38
          local.get $l20
          local.get $l20
          f64.mul
          local.tee $l39
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -17
          i32.const -1
          local.get $l11
          local.get $l11
          f64.mul
          local.tee $l40
          local.get $l19
          local.get $l19
          f64.mul
          local.tee $l41
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -9
          i32.const -1
          local.get $l10
          local.get $l10
          f64.mul
          local.tee $l42
          local.get $l18
          local.get $l18
          f64.mul
          local.tee $l43
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -5
          i32.const -1
          local.get $l9
          local.get $l9
          f64.mul
          local.tee $l44
          local.get $l17
          local.get $l17
          f64.mul
          local.tee $l45
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -3
          i32.const -1
          local.get $l8
          local.get $l8
          f64.mul
          local.tee $l46
          local.get $l6
          local.get $l6
          f64.mul
          local.tee $l47
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -2
          i32.const -1
          local.get $l7
          local.get $l7
          f64.mul
          local.tee $l48
          local.get $l4
          local.get $l4
          f64.mul
          local.tee $l49
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          local.tee $l5
          i32.const 255
          i32.and
          if $I4
            local.get $l1
            local.tee $l0
            i32.const 1
            i32.add
            local.set $l1
            local.get $l7
            local.get $l7
            f64.add
            local.get $l4
            f64.mul
            local.get $l2
            f64.add
            local.set $l4
            local.get $l27
            local.get $l48
            local.get $l49
            f64.sub
            f64.add
            local.set $l7
            local.get $l8
            local.get $l8
            f64.add
            local.get $l6
            f64.mul
            local.get $l2
            f64.add
            local.set $l6
            local.get $l28
            local.get $l46
            local.get $l47
            f64.sub
            f64.add
            local.set $l8
            local.get $l9
            local.get $l9
            f64.add
            local.get $l17
            f64.mul
            local.get $l2
            f64.add
            local.set $l17
            local.get $l29
            local.get $l44
            local.get $l45
            f64.sub
            f64.add
            local.set $l9
            local.get $l10
            local.get $l10
            f64.add
            local.get $l18
            f64.mul
            local.get $l2
            f64.add
            local.set $l18
            local.get $l30
            local.get $l42
            local.get $l43
            f64.sub
            f64.add
            local.set $l10
            local.get $l11
            local.get $l11
            f64.add
            local.get $l19
            f64.mul
            local.get $l2
            f64.add
            local.set $l19
            local.get $l31
            local.get $l40
            local.get $l41
            f64.sub
            f64.add
            local.set $l11
            local.get $l12
            local.get $l12
            f64.add
            local.get $l20
            f64.mul
            local.get $l2
            f64.add
            local.set $l20
            local.get $l32
            local.get $l38
            local.get $l39
            f64.sub
            f64.add
            local.set $l12
            local.get $l13
            local.get $l13
            f64.add
            local.get $l21
            f64.mul
            local.get $l2
            f64.add
            local.set $l21
            local.get $l33
            local.get $l36
            local.get $l37
            f64.sub
            f64.add
            local.set $l13
            local.get $l14
            local.get $l14
            f64.add
            local.get $l22
            f64.mul
            local.get $l2
            f64.add
            local.set $l22
            local.get $l26
            local.get $l34
            local.get $l35
            f64.sub
            f64.add
            local.set $l14
            local.get $l0
            br_if $L3
          end
        end
        local.get $l25
        local.get $l16
        i32.const 3
        i32.shr_u
        i32.add
        local.get $l5
        i32.store8
        local.get $l16
        i32.const 8
        i32.add
        local.set $l16
        local.get $l24
        i32.const 1
        i32.add
        local.tee $l24
        i32.const 64
        i32.ne
        br_if $L2
      end
      local.get $l15
      i32.const 1
      i32.add
      local.tee $l15
      i32.const 512
      i32.ne
      br_if $L1
    end
    local.get $l3
    i64.const 512
    i64.store
    local.get $l3
    i64.const 512
    i64.store offset=8
    i32.const 0
    i32.load
    i32.const 1024
    local.get $l3
    call $env.fprintf
    drop
    local.get $l23
    i32.const 32768
    i32.const 1
    i32.const 0
    i32.load
    call $env.fwrite
    drop
    local.get $l23
    call $env.free
    local.get $l3
    i32.const 8208
    i32.add
    global.set $g0
    i32.const 0)
  (memory $memory 2)
  (global $g0 (mut i32) (i32.const 66576))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1036))
  (global $__stack_low i32 (i32.const 1040))
  (global $__stack_high i32 (i32.const 66576))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66576))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
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
  (data $d0 (i32.const 1024) "P4\0a%jd %jd\0a"))
