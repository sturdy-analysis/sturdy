(module $reverse-complement.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32)))
  (type $t6 (func (result i32)))
  (import "env" "memmove" (func $memmove (type $t1)))
  (import "env" "toupper" (func $toupper (type $t0)))
  (import "env" "tolower" (func $tolower (type $t0)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "fileno" (func $fileno (type $t0)))
  (import "env" "read" (func $read (type $t1)))
  (import "env" "realloc" (func $realloc (type $t2)))
  (import "env" "write" (func $write (type $t1)))
  (import "env" "free" (func $free (type $t3)))
  (func $__wasm_call_ctors (type $t4)
    nop)
  (func $process (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee $l2
    global.set $__stack_pointer
    local.get $l2
    local.get $p0
    i32.store offset=28
    local.get $l2
    local.get $p1
    i32.store offset=24
    loop $L0
      local.get $l2
      local.get $l2
      i32.load offset=28
      local.tee $l3
      i32.const 1
      i32.add
      i32.store offset=28
      local.get $l3
      i32.load8_u
      i32.const 24
      local.tee $l4
      i32.shl
      local.get $l4
      i32.shr_s
      i32.const 10
      i32.ne
      br_if $L0
    end
    local.get $l2
    local.get $l2
    i32.load offset=24
    local.get $l2
    i32.load offset=28
    i32.sub
    i32.store offset=20
    local.get $l2
    i32.const 60
    local.get $l2
    i32.load offset=20
    i32.const 61
    i32.rem_u
    i32.sub
    i32.store offset=16
    local.get $l2
    i32.load offset=16
    if $I1
      local.get $l2
      local.get $l2
      i32.load offset=28
      i32.const 60
      i32.add
      local.get $l2
      i32.load offset=16
      i32.sub
      i32.store offset=12
      loop $L2
        local.get $l2
        i32.load offset=12
        local.get $l2
        i32.load offset=24
        i32.ge_u
        i32.eqz
        if $I3
          local.get $l2
          i32.load offset=12
          i32.const 1
          i32.add
          local.get $l2
          i32.load offset=12
          local.get $l2
          i32.load offset=16
          call $memmove
          drop
          local.get $l2
          i32.load offset=12
          i32.const 10
          i32.store8
          local.get $l2
          local.get $l2
          i32.load offset=12
          i32.const 61
          i32.add
          i32.store offset=12
          br $L2
        end
      end
    end
    local.get $l2
    local.get $l2
    i32.load offset=24
    i32.const 1
    i32.sub
    i32.store offset=24
    loop $L4
      local.get $l2
      i32.load offset=28
      local.get $l2
      i32.load offset=24
      i32.gt_u
      i32.eqz
      if $I5
        local.get $l2
        local.get $l2
        i32.load offset=28
        i32.load8_u
        i32.const 24
        local.tee $l5
        i32.shl
        local.get $l5
        i32.shr_s
        i32.load8_u offset=1072
        i32.store8 offset=11
        local.get $l2
        i32.load offset=28
        local.get $l2
        i32.load offset=24
        i32.load8_u
        i32.const 24
        local.tee $l6
        i32.shl
        local.get $l6
        i32.shr_s
        i32.load8_u offset=1072
        i32.store8
        local.get $l2
        i32.load offset=24
        local.get $l2
        i32.load8_u offset=11
        i32.store8
        local.get $l2
        local.get $l2
        i32.load offset=28
        i32.const 1
        i32.add
        i32.store offset=28
        local.get $l2
        local.get $l2
        i32.load offset=24
        i32.const 1
        i32.sub
        i32.store offset=24
        br $L4
      end
    end
    local.get $l2
    i32.const 32
    i32.add
    global.set $__stack_pointer)
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    global.get $__stack_pointer
    i32.const 48
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    local.get $l0
    i32.const 1060
    i32.load
    i32.store offset=44
    loop $L0
      local.get $l0
      i32.load offset=44
      i32.load8_u
      if $I1
        local.get $l0
        i32.load offset=44
        i32.load8_u offset=1
        local.set $l1
        local.get $l0
        i32.load offset=44
        i32.load8_u
        i32.const 24
        local.tee $l2
        i32.shl
        local.get $l2
        i32.shr_s
        call $toupper
        local.get $l1
        i32.store8 offset=1072
        local.get $l0
        i32.load offset=44
        i32.load8_u offset=1
        local.set $l3
        local.get $l0
        i32.load offset=44
        i32.load8_u
        i32.const 24
        local.tee $l4
        i32.shl
        local.get $l4
        i32.shr_s
        call $tolower
        local.get $l3
        i32.store8 offset=1072
        local.get $l0
        local.get $l0
        i32.load offset=44
        i32.const 2
        i32.add
        i32.store offset=44
        br $L0
      end
    end
    local.get $l0
    i32.const 1048576
    i32.store offset=40
    local.get $l0
    i32.const 8192
    i32.store offset=36
    local.get $l0
    i32.const 0
    i32.store offset=28
    local.get $l0
    local.get $l0
    i32.load offset=36
    call $malloc
    i32.store offset=24
    local.get $l0
    i32.const 0
    i32.load
    call $fileno
    i32.store offset=20
    loop $L2
      block $B3
        local.get $l0
        local.get $l0
        i32.load offset=20
        local.get $l0
        i32.load offset=24
        local.get $l0
        i32.load offset=28
        i32.add
        local.get $l0
        i32.load offset=36
        i32.const 256
        i32.sub
        local.get $l0
        i32.load offset=28
        i32.sub
        call $read
        local.tee $l5
        i32.store offset=32
        local.get $l5
        i32.eqz
        br_if $B3
        local.get $l0
        i32.load offset=32
        i32.const 0
        i32.lt_u
        br_if $B3
        local.get $l0
        local.get $l0
        i32.load offset=32
        local.get $l0
        i32.load offset=28
        i32.add
        i32.store offset=28
        local.get $l0
        i32.load offset=28
        local.get $l0
        i32.load offset=36
        i32.const 256
        i32.sub
        i32.ge_u
        if $I4
          local.get $l0
          block $B5 (result i32)
            local.get $l0
            i32.load offset=36
            i32.const 1048576
            i32.ge_u
            if $I6
              local.get $l0
              i32.load offset=36
              i32.const -1048576
              i32.sub
              br $B5
            end
            local.get $l0
            i32.load offset=36
            i32.const 1
            i32.shl
          end
          i32.store offset=36
          local.get $l0
          local.get $l0
          i32.load offset=24
          local.get $l0
          i32.load offset=36
          call $realloc
          i32.store offset=24
        end
        br $L2
      end
    end
    local.get $l0
    i32.load offset=24
    local.get $l0
    i32.load offset=28
    i32.add
    i32.const 62
    i32.store8
    local.get $l0
    local.get $l0
    i32.load offset=24
    local.get $l0
    i32.load offset=28
    i32.add
    i32.const 1
    i32.sub
    i32.store offset=12
    loop $L7
      local.get $l0
      local.get $l0
      i32.load offset=12
      i32.store offset=16
      loop $L8
        local.get $l0
        i32.load offset=16
        i32.load8_u
        i32.const 24
        local.tee $l6
        i32.shl
        local.get $l6
        i32.shr_s
        i32.const 62
        i32.eq
        i32.eqz
        if $I9
          local.get $l0
          local.get $l0
          i32.load offset=16
          i32.const 1
          i32.sub
          i32.store offset=16
          br $L8
        end
      end
      local.get $l0
      i32.load offset=16
      local.get $l0
      i32.load offset=12
      call $process
      local.get $l0
      local.get $l0
      i32.load offset=16
      i32.const 1
      i32.sub
      i32.store offset=12
      local.get $l0
      i32.load offset=12
      local.get $l0
      i32.load offset=24
      i32.ge_u
      br_if $L7
    end
    i32.const 0
    i32.load
    call $fileno
    local.get $l0
    i32.load offset=24
    local.get $l0
    i32.load offset=28
    call $write
    drop
    local.get $l0
    i32.load offset=24
    call $free
    local.get $l0
    i32.const 48
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66736))
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
  (data $.rodata (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a")
  (data $.data (i32.const 1061) "\04"))
