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
  (data $d0 (i32.const 1024) "%d\0aPfannkuchen(%d) = %d\0a\00range: must be 3 <= n <= 12\0a")
  (@custom ".debug_loc" "\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\02\9f\01\00\00\00\01\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\04\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\01\00\00\00\01\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\09\9f\01\00\00\00\01\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05\16\00I\13\03\0e:\0b;\0b\00\00\06$\00\03\0e>\0b\0b\0b\00\00\07$\00\03\0e\0b\0b>\0b\00\00\084\00I\13:\0b;\0b\02\18\00\00\09.\01\11\01\12\06@\18\97B\191\13\00\00\0a4\00\02\171\13\00\00\0b\05\00\02\181\13\00\00\0c.\01\03\0e:\0b;\0b'\19?\19 \0b\00\00\0d\05\00\03\0e:\0b;\0bI\13\00\00\0e4\00\03\0e:\0b;\0bI\13\00\00\0f.\01\03\0e:\0b;\0bI\13?\19 \0b\00\00\10\0f\00I\13\00\00\11.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\12\05\00\02\18\03\0e:\0b;\0bI\13\00\00\134\00\02\17\03\0e:\0b;\0bI\13\00\00\14\1d\011\13\11\01\12\06X\0bY\0bW\0b\00\00\15.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\16\89\82\01\001\13\11\01\00\00\17.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\18\05\00I\13\00\00\19\18\00\00\00\1a&\00I\13\00\00\1b.\01\03\0e:\0b;\0b'\19<\19?\19\87\01\19\00\00\00")
  (@custom ".debug_info" "\d2\02\00\00\04\00\00\00\00\00\04\01\d4\00\00\00\1d\00\b1\00\00\00\00\00\00\00R\00\00\00\00\00\00\00\00\00\00\00\02\be\00\00\007\00\00\00\01\10\05\03@\04\00\00\03C\00\00\00\04U\00\00\00\10\00\05N\00\00\006\00\00\00\01\0d\06\0b\00\00\00\05\04\07\c0\00\00\00\08\07\02\14\00\00\00N\00\00\00\01\12\05\03\80\04\00\00\02N\00\00\00N\00\00\00\01\14\05\03\84\04\00\00\02-\00\00\00N\00\00\00\01\15\05\03\88\04\00\00\08\9c\00\00\00\01P\05\03\19\04\00\00\03\a8\00\00\00\04U\00\00\00\1d\00\06\1d\00\00\00\06\01\08\bc\00\00\00\01W\05\03\00\04\00\00\03\a8\00\00\00\04U\00\00\00\19\00\02\1b\00\00\007\00\00\00\01\0f\05\03\d0\04\00\00\02\12\00\00\007\00\00\00\01\0f\05\03\90\04\00\00\02'\00\00\00N\00\00\00\01\13\05\03\10\05\00\00\09\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\91\01\00\00\0a\00\00\00\00\9d\01\00\00\0aH\00\00\00\a8\01\00\00\0a\82\00\00\00\b3\01\00\00\0a\ba\00\00\00\be\01\00\00\00\09\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9fg\01\00\00\0b\04\ed\00\00\9fo\01\00\00\0a\d8\00\00\00z\01\00\00\0a\f5\00\00\00\85\01\00\00\00\0cG\00\00\00\01*\01\0d+\00\00\00\01*N\00\00\00\0e>\00\00\00\01-N\00\00\00\0e\be\00\00\00\01,C\00\00\00\00\0f\22\00\00\00\01\17N\00\00\00\01\0e\00\00\00\00\01\1a\ca\01\00\00\0e\02\00\00\00\01\1a\ca\01\00\00\0e>\00\00\00\01\19N\00\00\00\0e\be\00\00\00\01\1aC\00\00\00\00\10C\00\00\00\11\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f;\00\00\00\014\12\04\ed\00\00\9f+\00\00\00\014N\00\00\00\13\13\01\00\00>\00\00\00\016N\00\00\00\0eE\00\00\00\016N\00\00\00\14g\01\00\00\00\00\00\00\00\00\00\00\019\07\0aK\01\00\00z\01\00\00\0ah\01\00\00\85\01\00\00\00\14\91\01\00\00\00\00\00\00\00\00\00\00\01C\18\0a\86\01\00\00\9d\01\00\00\0a\c0\01\00\00\a8\01\00\00\0a\ec\01\00\00\b3\01\00\00\0a$\02\00\00\be\01\00\00\00\00\15\00\00\00\00\00\00\00\00\04\ed\00\00\9f\04\00\00\00\01JN\00\00\00\13B\02\00\00>\00\00\00\01LN\00\00\00\16\cf\01\00\00\00\00\00\00\16\ac\02\00\00\00\00\00\00\16\c8\02\00\00\00\00\00\00\00\17@\00\00\00\02\f1N\00\00\00\18\be\02\00\00\19\00\10\c3\02\00\00\1a\a8\00\00\00\1b\0f\00\00\00\02\e3\18N\00\00\00\00\00")
  (@custom ".debug_ranges" "\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "y\00x\00_start\00int\00exit\00maxflips\00char\00flip\00max_n\00checksum\00elem\00tk\00i\00printf\00rotate\00odd\00/home/sven/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00fankuchredux.c\00__ARRAY_SIZE_TYPE__\00clang version 19.1.7\00")
  (@custom ".debug_line" "@\00\00\00\04\00:\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00fankuchredux.c\00\00\00\00stdlib.h\00\01\00\00\00")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0619.1.7")
  (@custom "target_features" "\04+\0fmutable-globals+\08sign-ext+\0freference-types+\0amultivalue"))
