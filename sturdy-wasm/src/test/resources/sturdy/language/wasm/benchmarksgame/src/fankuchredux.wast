(module
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (param i32 i32 i32) (result i32)))
  (type $t3 (func (param i32 i32) (result i32)))
  (type $t4 (func (param i32 i32 i32 i32) (result i32)))
  (type $t5 (func))
  (import "env" "memcpy" (func $env.memcpy (type $t2)))
  (import "env" "memmove" (func $env.memmove (type $t2)))
  (import "env" "__VERIFIER_nondet_int" (func $env.__VERIFIER_nondet_int (type $t0)))
  (import "env" "printf" (func $env.printf (type $t3)))
  (import "env" "fwrite" (func $env.fwrite (type $t4)))
  (import "env" "exit" (func $env.exit (type $t1)))
  (func $__wasm_call_ctors (type $t5))
  (func $flip (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32)
    i32.const 1168
    i32.const 1232
    i32.const 1296
    i32.load
    call $env.memcpy
    local.set $l3
    block $B0
      i32.const 1168
      i32.load
      local.tee $l0
      i32.const 0
      i32.gt_s
      if $I1
        i32.const 1
        local.set $l1
        loop $L2
          local.get $l1
          i32.const 1
          i32.add
          local.set $l1
          local.get $l0
          i32.const 0
          i32.gt_s
          if $I3
            i32.const 1168
            local.set $l2
            local.get $l0
            i32.const 2
            i32.shl
            i32.const 1168
            i32.add
            local.set $l0
            loop $L4
              local.get $l2
              i32.load
              local.set $l4
              local.get $l2
              local.get $l0
              i32.load
              i32.store
              local.get $l0
              local.get $l4
              i32.store
              local.get $l2
              i32.const 4
              i32.add
              local.tee $l2
              local.get $l0
              i32.const 4
              i32.sub
              local.tee $l0
              i32.lt_u
              br_if $L4
            end
            i32.const 1168
            i32.load
            local.set $l0
          end
          local.get $l0
          i32.const 2
          i32.shl
          local.get $l3
          i32.add
          i32.load
          br_if $L2
        end
        br $B0
      end
      i32.const 2
      local.set $l1
      local.get $l0
      i32.const 2
      i32.shl
      local.get $l3
      i32.add
      i32.load
      i32.eqz
      br_if $B0
      loop $L5
        br $L5
      end
      unreachable
    end
    local.get $l1)
  (func $rotate (type $t1) (param $p0 i32)
    (local $l1 i32)
    i32.const 1232
    i32.load
    local.set $l1
    local.get $p0
    i32.const 0
    i32.gt_s
    if $I0
      i32.const 1232
      i32.const 1236
      local.get $p0
      i32.const 2
      i32.shl
      call $env.memmove
      drop
    end
    local.get $p0
    i32.const 2
    i32.shl
    i32.const 1232
    i32.add
    local.get $l1
    i32.store)
  (func $tk (type $t1) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32)
    local.get $p0
    i32.const 0
    i32.gt_s
    if $I0
      i32.const 1296
      i32.load
      local.set $l10
      i32.const 1156
      i32.load
      local.set $l4
      i32.const 1152
      i32.load
      local.set $l7
      i32.const 1160
      i32.load
      local.set $l8
      loop $L1
        i32.const 1232
        i32.load
        local.set $l1
        local.get $l2
        i32.const 0
        i32.gt_s
        if $I2
          i32.const 1232
          i32.const 1236
          local.get $l2
          i32.const 2
          i32.shl
          call $env.memmove
          drop
        end
        local.get $l2
        i32.const 2
        i32.shl
        local.tee $l5
        i32.const 1232
        i32.add
        local.get $l1
        i32.store
        block $B3
          local.get $l2
          local.get $l5
          i32.const 1088
          i32.add
          local.tee $l6
          i32.load
          local.tee $l1
          i32.le_s
          if $I4
            local.get $l6
            i32.const 0
            i32.store
            local.get $l2
            i32.const 1
            i32.add
            local.set $l2
            br $B3
          end
          i32.const 1156
          local.get $l4
          i32.const -1
          i32.xor
          local.tee $l5
          i32.store
          i32.const 1
          local.set $l2
          local.get $l6
          local.get $l1
          i32.const 1
          i32.add
          i32.store
          i32.const 1232
          i32.load
          local.tee $l1
          i32.eqz
          if $I5
            local.get $l5
            local.set $l4
            br $B3
          end
          block $B6
            local.get $l1
            i32.const 2
            i32.shl
            i32.const 1232
            i32.add
            i32.load
            i32.eqz
            if $I7
              i32.const 1
              local.set $l1
              br $B6
            end
            i32.const 1168
            i32.const 1232
            local.get $l10
            call $env.memcpy
            local.set $l9
            i32.const 1168
            i32.load
            local.tee $l3
            i32.const 0
            i32.gt_s
            if $I8
              i32.const 1
              local.set $l1
              loop $L9
                local.get $l1
                i32.const 1
                i32.add
                local.set $l1
                local.get $l3
                i32.const 0
                i32.gt_s
                if $I10
                  i32.const 1168
                  local.set $l2
                  local.get $l3
                  i32.const 2
                  i32.shl
                  i32.const 1168
                  i32.add
                  local.set $l3
                  loop $L11
                    local.get $l2
                    i32.load
                    local.set $l6
                    local.get $l2
                    local.get $l3
                    i32.load
                    i32.store
                    local.get $l3
                    local.get $l6
                    i32.store
                    local.get $l2
                    i32.const 4
                    i32.add
                    local.tee $l2
                    local.get $l3
                    i32.const 4
                    i32.sub
                    local.tee $l3
                    i32.lt_u
                    br_if $L11
                  end
                  i32.const 1168
                  i32.load
                  local.set $l3
                end
                local.get $l3
                i32.const 2
                i32.shl
                local.get $l9
                i32.add
                i32.load
                br_if $L9
              end
              br $B6
            end
            i32.const 2
            local.set $l1
            local.get $l3
            i32.const 2
            i32.shl
            local.get $l9
            i32.add
            i32.load
            i32.eqz
            br_if $B6
            loop $L12
              br $L12
            end
            unreachable
          end
          local.get $l1
          local.get $l7
          i32.gt_s
          if $I13
            i32.const 1152
            local.get $l1
            i32.store
            local.get $l1
            local.set $l7
          end
          i32.const 1160
          local.get $l8
          local.get $l1
          i32.const 0
          local.get $l1
          i32.sub
          local.get $l4
          i32.const -1
          i32.eq
          select
          i32.add
          local.tee $l8
          i32.store
          i32.const 1
          local.set $l2
          local.get $l5
          local.set $l4
        end
        local.get $p0
        local.get $l2
        i32.gt_s
        br_if $L1
      end
    end)
  (func $_start (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    global.get $g0
    i32.const 16
    i32.sub
    local.tee $l2
    global.set $g0
    i32.const 1296
    call $env.__VERIFIER_nondet_int
    local.tee $l3
    i32.store
    local.get $l3
    i32.const 16
    i32.sub
    i32.const -13
    i32.ge_u
    if $I0
      local.get $l3
      i32.const 7
      i32.and
      local.set $l4
      local.get $l3
      i32.const 1
      i32.sub
      i32.const 7
      i32.ge_u
      if $I1
        local.get $l3
        i32.const 8
        i32.and
        local.set $l5
        i32.const 1232
        local.set $l1
        loop $L2
          local.get $l1
          local.get $l0
          i32.store
          local.get $l1
          i32.const 28
          i32.add
          local.get $l0
          i32.const 7
          i32.add
          i32.store
          local.get $l1
          i32.const 24
          i32.add
          local.get $l0
          i32.const 6
          i32.add
          i32.store
          local.get $l1
          i32.const 20
          i32.add
          local.get $l0
          i32.const 5
          i32.add
          i32.store
          local.get $l1
          i32.const 16
          i32.add
          local.get $l0
          i32.const 4
          i32.add
          i32.store
          local.get $l1
          i32.const 12
          i32.add
          local.get $l0
          i32.const 3
          i32.add
          i32.store
          local.get $l1
          i32.const 8
          i32.add
          local.get $l0
          i32.const 2
          i32.add
          i32.store
          local.get $l1
          i32.const 4
          i32.add
          local.get $l0
          i32.const 1
          i32.add
          i32.store
          local.get $l1
          i32.const 32
          i32.add
          local.set $l1
          local.get $l0
          i32.const 8
          i32.add
          local.tee $l0
          local.get $l5
          i32.ne
          br_if $L2
        end
      end
      local.get $l4
      if $I3
        local.get $l0
        i32.const 2
        i32.shl
        i32.const 1232
        i32.add
        local.set $l1
        loop $L4
          local.get $l1
          local.get $l0
          i32.store
          local.get $l1
          i32.const 4
          i32.add
          local.set $l1
          local.get $l0
          i32.const 1
          i32.add
          local.set $l0
          local.get $l4
          i32.const 1
          i32.sub
          local.tee $l4
          br_if $L4
        end
      end
      local.get $l3
      call $tk
      local.get $l2
      i32.const 1160
      i32.load
      i32.store
      local.get $l2
      i32.const 1296
      i32.load
      i32.store offset=4
      local.get $l2
      i32.const 1152
      i32.load
      i32.store offset=8
      i32.const 1024
      local.get $l2
      call $env.printf
      drop
      local.get $l2
      i32.const 16
      i32.add
      global.set $g0
      i32.const 0
      return
    end
    i32.const 1049
    i32.const 28
    i32.const 1
    i32.const 0
    i32.load
    call $env.fwrite
    drop
    i32.const 1
    call $env.exit
    unreachable)
  (memory $memory 2)
  (global $g0 (mut i32) (i32.const 66848))
  (global $t i32 (i32.const 1168))
  (global $s i32 (i32.const 1232))
  (global $max_n i32 (i32.const 1296))
  (global $odd i32 (i32.const 1156))
  (global $maxflips i32 (i32.const 1152))
  (global $checksum i32 (i32.const 1160))
  (global $c i32 (i32.const 1088))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1300))
  (global $__stack_low i32 (i32.const 1312))
  (global $__stack_high i32 (i32.const 66848))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66848))
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
  (export "c" (global $c))
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
  (data $d0 (i32.const 1024) "%d\0aPfannkuchen(%d) = %d\0a\00range: must be 3 <= n <= 12\0a"))
