(module
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32)))
  (type $t6 (func (result i32)))
  (import "env" "memmove" (func $env.memmove (type $t1)))
  (import "env" "toupper" (func $env.toupper (type $t0)))
  (import "env" "tolower" (func $env.tolower (type $t0)))
  (import "env" "malloc" (func $env.malloc (type $t0)))
  (import "env" "fileno" (func $env.fileno (type $t0)))
  (import "env" "read" (func $env.read (type $t1)))
  (import "env" "realloc" (func $env.realloc (type $t2)))
  (import "env" "write" (func $env.write (type $t1)))
  (import "env" "free" (func $env.free (type $t3)))
  (func $__wasm_call_ctors (type $t4))
  (func $process (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    local.get $p1
    local.get $p0
    i32.sub
    local.set $l4
    loop $L0
      local.get $l4
      i32.const 1
      i32.sub
      local.set $l4
      local.get $p0
      local.get $l2
      i32.add
      local.get $l2
      i32.const 1
      i32.add
      local.tee $l5
      local.set $l2
      i32.load8_u
      i32.const 10
      i32.ne
      br_if $L0
    end
    local.get $p0
    local.get $l5
    i32.add
    local.set $l3
    block $B1
      local.get $p1
      local.get $p0
      i32.sub
      local.get $l4
      i32.const 61
      i32.div_u
      i32.const 61
      i32.mul
      local.tee $l4
      i32.sub
      local.tee $l2
      i32.const 60
      i32.sub
      local.get $l5
      i32.eq
      br_if $B1
      local.get $l3
      local.get $l2
      local.get $l5
      i32.sub
      i32.add
      local.tee $l2
      local.get $p1
      i32.ge_u
      br_if $B1
      local.get $p0
      local.get $p1
      i32.sub
      local.get $l4
      i32.add
      local.get $l5
      i32.add
      i32.const 60
      i32.add
      local.set $p0
      loop $L2
        local.get $l2
        i32.const 1
        i32.add
        local.get $l2
        local.get $p0
        call $env.memmove
        drop
        local.get $l2
        i32.const 10
        i32.store8
        local.get $l2
        i32.const 61
        i32.add
        local.tee $l2
        local.get $p1
        i32.lt_u
        br_if $L2
      end
    end
    local.get $p1
    i32.const 1
    i32.sub
    local.tee $l2
    local.get $l3
    i32.ge_u
    if $I3
      loop $L4
        local.get $l3
        i32.load8_s
        i32.const 1072
        i32.add
        i32.load8_u
        local.set $p0
        local.get $l3
        local.get $l2
        i32.load8_s
        i32.const 1072
        i32.add
        i32.load8_u
        i32.store8
        local.get $l2
        local.get $p0
        i32.store8
        local.get $l3
        i32.const 1
        i32.add
        local.tee $l3
        local.get $l2
        i32.const 1
        i32.sub
        local.tee $l2
        i32.le_u
        br_if $L4
      end
    end)
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    i32.const 1060
    i32.load
    local.tee $l0
    i32.load8_u
    local.tee $l1
    if $I0
      loop $L1
        local.get $l1
        i32.extend8_s
        call $env.toupper
        i32.const 1072
        i32.add
        local.get $l0
        i32.load8_u offset=1
        local.tee $l2
        i32.store8
        local.get $l0
        i32.load8_s
        call $env.tolower
        i32.const 1072
        i32.add
        local.get $l2
        i32.store8
        local.get $l0
        i32.load8_u offset=2
        local.set $l1
        local.get $l0
        i32.const 2
        i32.add
        local.set $l0
        local.get $l1
        br_if $L1
      end
    end
    i32.const 8192
    local.set $l0
    i32.const 8192
    call $env.malloc
    local.set $l3
    i32.const 7936
    local.set $l1
    block $B2
      i32.const 0
      i32.load
      call $env.fileno
      local.tee $l4
      local.get $l3
      i32.const 7936
      call $env.read
      local.tee $l6
      i32.eqz
      if $I3
        local.get $l3
        local.set $l2
        br $B2
      end
      loop $L4
        local.get $l1
        local.get $l5
        local.get $l6
        i32.add
        local.tee $l5
        i32.le_u
        if $I5
          local.get $l3
          local.get $l0
          i32.const -1048576
          i32.sub
          local.get $l0
          i32.const 1
          i32.shl
          local.get $l0
          i32.const 1048575
          i32.gt_u
          select
          local.tee $l0
          call $env.realloc
          local.set $l3
        end
        local.get $l4
        local.get $l3
        local.get $l5
        i32.add
        local.tee $l2
        local.get $l0
        i32.const 256
        i32.sub
        local.tee $l1
        local.get $l5
        i32.sub
        call $env.read
        local.tee $l6
        br_if $L4
      end
    end
    local.get $l2
    i32.const 62
    i32.store8
    local.get $l2
    i32.const 1
    i32.sub
    local.set $l9
    loop $L6
      i32.const -1
      local.set $l2
      i32.const -1
      local.set $l4
      local.get $l9
      local.tee $l8
      local.set $l1
      loop $L7
        local.get $l2
        local.tee $l6
        i32.const 1
        i32.add
        local.set $l2
        local.get $l4
        local.tee $l0
        i32.const 1
        i32.sub
        local.set $l4
        local.get $l1
        i32.load8_u
        local.get $l1
        i32.const 1
        i32.sub
        local.tee $l9
        local.set $l1
        i32.const 62
        i32.ne
        br_if $L7
      end
      local.get $l8
      i32.const 2
      i32.add
      local.set $l4
      loop $L8
        local.get $l6
        i32.const 1
        i32.sub
        local.set $l6
        local.get $l0
        local.get $l4
        i32.add
        local.get $l0
        i32.const 1
        i32.add
        local.tee $l7
        local.set $l0
        i32.load8_u
        i32.const 10
        i32.ne
        br_if $L8
      end
      local.get $l7
      local.get $l8
      i32.add
      i32.const 2
      i32.add
      local.set $l1
      block $B9
        local.get $l6
        i32.const 61
        i32.div_u
        local.tee $l4
        i32.const -61
        i32.mul
        local.tee $l2
        i32.const 62
        i32.sub
        local.get $l7
        i32.eq
        br_if $B9
        local.get $l2
        local.get $l7
        i32.sub
        local.get $l1
        i32.add
        i32.const 2
        i32.sub
        local.tee $l0
        local.get $l8
        i32.ge_u
        br_if $B9
        local.get $l4
        i32.const 61
        i32.mul
        local.get $l7
        i32.add
        i32.const 62
        i32.add
        local.set $l2
        loop $L10
          local.get $l0
          i32.const 1
          i32.add
          local.get $l0
          local.get $l2
          call $env.memmove
          drop
          local.get $l0
          i32.const 10
          i32.store8
          local.get $l0
          i32.const 61
          i32.add
          local.tee $l0
          local.get $l8
          i32.lt_u
          br_if $L10
        end
      end
      local.get $l8
      i32.const 1
      i32.sub
      local.tee $l0
      local.get $l1
      i32.ge_u
      if $I11
        loop $L12
          local.get $l1
          i32.load8_s
          i32.const 1072
          i32.add
          i32.load8_u
          local.set $l2
          local.get $l1
          local.get $l0
          i32.load8_s
          i32.const 1072
          i32.add
          i32.load8_u
          i32.store8
          local.get $l0
          local.get $l2
          i32.store8
          local.get $l1
          i32.const 1
          i32.add
          local.tee $l1
          local.get $l0
          i32.const 1
          i32.sub
          local.tee $l0
          i32.le_u
          br_if $L12
        end
      end
      local.get $l3
      local.get $l9
      i32.le_u
      br_if $L6
    end
    i32.const 0
    i32.load
    call $env.fileno
    local.get $l3
    local.get $l5
    call $env.write
    drop
    local.get $l3
    call $env.free
    i32.const 0)
  (memory $memory 2)
  (global $tbl i32 (i32.const 1072))
  (global $pairs i32 (i32.const 1060))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1200))
  (global $__stack_low i32 (i32.const 1200))
  (global $__stack_high i32 (i32.const 66736))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66736))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "process" (func $process))
  (export "tbl" (global $tbl))
  (export "_start" (func $_start))
  (export "pairs" (global $pairs))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (data $d0 (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a")
  (data $d1 (i32.const 1061) "\04"))
