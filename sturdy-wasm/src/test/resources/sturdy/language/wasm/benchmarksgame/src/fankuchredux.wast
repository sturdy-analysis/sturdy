(module $fankuchredux.wasm
  (type $t0 (func (param i32 i32 i32) (result i32)))
  (type $t1 (func (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32 i32 i32 i32) (result i32)))
  (type $t4 (func (param i32)))
  (type $t5 (func))
  (import "env" "memcpy" (func $memcpy (type $t0)))
  (import "env" "memmove" (func $memmove (type $t0)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t1)))
  (import "env" "printf" (func $printf (type $t2)))
  (import "env" "fwrite" (func $fwrite (type $t3)))
  (import "env" "exit" (func $exit (type $t4)))
  (func $__wasm_call_ctors (type $t5))
  (func $flip (type $t1) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32)
    i32.const 1168
    i32.const 1232
    i32.const 0
    i32.load offset=1296
    call $memcpy
    local.set $l0
    block $B0
      block $B1
        i32.const 0
        i32.load offset=1168
        local.tee $l1
        i32.const 1
        i32.lt_s
        br_if $B1
        i32.const 1
        local.set $l2
        loop $L2
          block $B3
            local.get $l1
            i32.const 1
            i32.lt_s
            br_if $B3
            i32.const 1168
            local.set $l3
            local.get $l1
            i32.const 2
            i32.shl
            i32.const 1168
            i32.add
            local.set $l1
            loop $L4
              local.get $l3
              i32.load
              local.set $l4
              local.get $l3
              local.get $l1
              i32.load
              i32.store
              local.get $l1
              local.get $l4
              i32.store
              local.get $l3
              i32.const 4
              i32.add
              local.tee $l3
              local.get $l1
              i32.const -4
              i32.add
              local.tee $l1
              i32.lt_u
              br_if $L4
            end
            i32.const 0
            i32.load offset=1168
            local.set $l1
          end
          local.get $l2
          i32.const 1
          i32.add
          local.set $l2
          local.get $l1
          i32.const 2
          i32.shl
          local.get $l0
          i32.add
          i32.load
          br_if $L2
          br $B0
        end
      end
      i32.const 2
      local.set $l2
      local.get $l1
      i32.const 2
      i32.shl
      local.get $l0
      i32.add
      i32.load
      i32.eqz
      br_if $B0
      loop $L5
        br $L5
      end
    end
    local.get $l2)
  (func $rotate (type $t4) (param $p0 i32)
    (local $l1 i32)
    i32.const 0
    i32.load offset=1232
    local.set $l1
    block $B0
      local.get $p0
      i32.const 1
      i32.lt_s
      br_if $B0
      i32.const 1232
      i32.const 1236
      local.get $p0
      i32.const 2
      i32.shl
      call $memmove
      drop
    end
    local.get $p0
    i32.const 2
    i32.shl
    i32.const 1232
    i32.add
    local.get $l1
    i32.store)
  (func $tk (type $t4) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32)
    block $B0
      local.get $p0
      i32.const 1
      i32.lt_s
      br_if $B0
      i32.const 0
      i32.load offset=1296
      local.set $l1
      i32.const 0
      i32.load offset=1156
      local.set $l2
      i32.const 0
      i32.load offset=1152
      local.set $l3
      i32.const 0
      i32.load offset=1160
      local.set $l4
      i32.const 0
      local.set $l5
      loop $L1
        i32.const 0
        i32.load offset=1232
        local.set $l6
        block $B2
          local.get $l5
          i32.const 1
          i32.lt_s
          br_if $B2
          i32.const 1232
          i32.const 1236
          local.get $l5
          i32.const 2
          i32.shl
          call $memmove
          drop
        end
        local.get $l5
        i32.const 2
        i32.shl
        local.tee $l7
        i32.const 1232
        i32.add
        local.get $l6
        i32.store
        block $B3
          block $B4
            local.get $l7
            i32.const 1088
            i32.add
            local.tee $l6
            i32.load
            local.tee $l7
            local.get $l5
            i32.lt_s
            br_if $B4
            local.get $l6
            i32.const 0
            i32.store
            local.get $l5
            i32.const 1
            i32.add
            local.set $l5
            br $B3
          end
          i32.const 0
          local.get $l2
          i32.const -1
          i32.xor
          local.tee $l8
          i32.store offset=1156
          i32.const 1
          local.set $l5
          local.get $l6
          local.get $l7
          i32.const 1
          i32.add
          i32.store
          block $B5
            i32.const 0
            i32.load offset=1232
            local.tee $l6
            br_if $B5
            local.get $l8
            local.set $l2
            br $B3
          end
          block $B6
            block $B7
              local.get $l6
              i32.const 2
              i32.shl
              i32.const 1232
              i32.add
              i32.load
              br_if $B7
              i32.const 1
              local.set $l9
              br $B6
            end
            i32.const 1168
            i32.const 1232
            local.get $l1
            call $memcpy
            local.set $l10
            block $B8
              i32.const 0
              i32.load offset=1168
              local.tee $l6
              i32.const 1
              i32.lt_s
              br_if $B8
              i32.const 1
              local.set $l9
              loop $L9
                block $B10
                  local.get $l6
                  i32.const 1
                  i32.lt_s
                  br_if $B10
                  i32.const 1168
                  local.set $l5
                  local.get $l6
                  i32.const 2
                  i32.shl
                  i32.const 1168
                  i32.add
                  local.set $l6
                  loop $L11
                    local.get $l5
                    i32.load
                    local.set $l7
                    local.get $l5
                    local.get $l6
                    i32.load
                    i32.store
                    local.get $l6
                    local.get $l7
                    i32.store
                    local.get $l5
                    i32.const 4
                    i32.add
                    local.tee $l5
                    local.get $l6
                    i32.const -4
                    i32.add
                    local.tee $l6
                    i32.lt_u
                    br_if $L11
                  end
                  i32.const 0
                  i32.load offset=1168
                  local.set $l6
                end
                local.get $l9
                i32.const 1
                i32.add
                local.set $l9
                local.get $l6
                i32.const 2
                i32.shl
                local.get $l10
                i32.add
                i32.load
                br_if $L9
                br $B6
              end
            end
            i32.const 2
            local.set $l9
            local.get $l6
            i32.const 2
            i32.shl
            local.get $l10
            i32.add
            i32.load
            i32.eqz
            br_if $B6
            loop $L12
              br $L12
            end
          end
          block $B13
            local.get $l9
            local.get $l3
            i32.le_s
            br_if $B13
            i32.const 0
            local.get $l9
            i32.store offset=1152
            local.get $l9
            local.set $l3
          end
          i32.const 0
          local.get $l4
          local.get $l9
          i32.const 0
          local.get $l9
          i32.sub
          local.get $l2
          i32.const -1
          i32.eq
          select
          i32.add
          local.tee $l4
          i32.store offset=1160
          i32.const 1
          local.set $l5
          local.get $l8
          local.set $l2
        end
        local.get $l5
        local.get $p0
        i32.lt_s
        br_if $L1
      end
    end)
  (func $_start (type $t1) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    i32.const 0
    local.set $l1
    i32.const 0
    call $__VERIFIER_nondet_int
    local.tee $l2
    i32.store offset=1296
    block $B0
      local.get $l2
      i32.const -16
      i32.add
      i32.const -13
      i32.lt_u
      br_if $B0
      local.get $l2
      i32.const 7
      i32.and
      local.set $l3
      block $B1
        local.get $l2
        i32.const -1
        i32.add
        i32.const 7
        i32.lt_u
        br_if $B1
        local.get $l2
        i32.const 8
        i32.and
        local.set $l4
        i32.const 0
        local.set $l1
        i32.const 1232
        local.set $l5
        loop $L2
          local.get $l5
          local.get $l1
          i32.store
          local.get $l5
          i32.const 28
          i32.add
          local.get $l1
          i32.const 7
          i32.add
          i32.store
          local.get $l5
          i32.const 24
          i32.add
          local.get $l1
          i32.const 6
          i32.add
          i32.store
          local.get $l5
          i32.const 20
          i32.add
          local.get $l1
          i32.const 5
          i32.add
          i32.store
          local.get $l5
          i32.const 16
          i32.add
          local.get $l1
          i32.const 4
          i32.add
          i32.store
          local.get $l5
          i32.const 12
          i32.add
          local.get $l1
          i32.const 3
          i32.add
          i32.store
          local.get $l5
          i32.const 8
          i32.add
          local.get $l1
          i32.const 2
          i32.add
          i32.store
          local.get $l5
          i32.const 4
          i32.add
          local.get $l1
          i32.const 1
          i32.add
          i32.store
          local.get $l5
          i32.const 32
          i32.add
          local.set $l5
          local.get $l1
          i32.const 8
          i32.add
          local.tee $l1
          local.get $l4
          i32.ne
          br_if $L2
        end
      end
      block $B3
        local.get $l3
        i32.eqz
        br_if $B3
        local.get $l1
        i32.const 2
        i32.shl
        i32.const 1232
        i32.add
        local.set $l5
        loop $L4
          local.get $l5
          local.get $l1
          i32.store
          local.get $l5
          i32.const 4
          i32.add
          local.set $l5
          local.get $l1
          i32.const 1
          i32.add
          local.set $l1
          local.get $l3
          i32.const -1
          i32.add
          local.tee $l3
          br_if $L4
        end
      end
      local.get $l2
      call $tk
      local.get $l0
      i32.const 0
      i32.load offset=1160
      i32.store
      local.get $l0
      i32.const 0
      i32.load offset=1296
      i32.store offset=4
      local.get $l0
      i32.const 0
      i32.load offset=1152
      i32.store offset=8
      i32.const 1024
      local.get $l0
      call $printf
      drop
      local.get $l0
      i32.const 16
      i32.add
      global.set $__stack_pointer
      i32.const 0
      return
    end
    i32.const 1049
    i32.const 28
    i32.const 1
    i32.const 0
    i32.load
    call $fwrite
    drop
    i32.const 1
    call $exit
    unreachable)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66848))
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
  (data $.rodata (i32.const 1024) "%d\0aPfannkuchen(%d) = %d\0a\00range: must be 3 <= n <= 12\0a\00")
  (@custom ".debug_loc" "\ff\ff\ff\ff\06\00\00\00\0f\00\00\00\1e\00\00\00\04\00\ed\02\01\9fY\00\00\00[\00\00\00\04\00\ed\00\01\9f\7f\00\00\00\81\00\00\00\04\00\ed\02\01\9f\81\00\00\00\90\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\09\00\00\00\1e\00\00\00\04\00\ed\02\00\9fx\00\00\00z\00\00\00\04\00\ed\02\00\9fz\00\00\00\90\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00 \00\00\008\00\00\00\03\00\11\01\9f\98\00\00\00\a7\00\00\00\04\00\ed\00\02\9f\a7\00\00\00\bb\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00b\00\00\00\90\00\00\00\04\00\ed\00\04\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\cb\00\00\00\00\00\00\00/\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\cb\00\00\00\0e\00\00\00B\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\0f\01\00\00\00\00\00\00<\00\00\00\03\00\11\00\9f\a5\00\00\00\a7\00\00\00\04\00\ed\00\05\9f\a8\00\00\00\e9\01\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\0f\01\00\00>\00\00\00j\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\0f\01\00\00I\00\00\00\97\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\0f\01\00\00E\01\00\00G\01\00\00\04\00\ed\00\06\9fk\01\00\00m\01\00\00\04\00\ed\02\01\9fm\01\00\00|\01\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\0f\01\00\00d\01\00\00f\01\00\00\04\00\ed\02\00\9ff\01\00\00|\01\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\0f\01\00\00\0e\01\00\00$\01\00\00\03\00\11\01\9f\84\01\00\00\93\01\00\00\04\00\ed\00\09\9f\93\01\00\00\a7\01\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\0f\01\00\00N\01\00\00|\01\00\00\04\00\ed\00\07\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\05\03\00\00\cb\00\00\00\cd\00\00\00\04\00\ed\02\00\9f\cd\00\00\00\d2\00\00\00\04\00\ed\00\01\9f\00\01\00\00\09\01\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\05\03\00\00a\01\00\00t\01\00\00\04\00\ed\02\00\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05\16\00I\13\03\0e:\0b;\0b\00\00\06$\00\03\0e>\0b\0b\0b\00\00\07$\00\03\0e\0b\0b>\0b\00\00\084\00I\13:\0b;\0b\02\18\00\00\09.\01\11\01\12\06@\18\97B\191\13\00\00\0a4\00\02\171\13\00\00\0b\05\00\02\181\13\00\00\0c.\01\03\0e:\0b;\0b'\19?\19 \0b\00\00\0d\05\00\03\0e:\0b;\0bI\13\00\00\0e4\00\03\0e:\0b;\0bI\13\00\00\0f.\01\03\0e:\0b;\0bI\13?\19 \0b\00\00\10\0f\00I\13\00\00\11.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\12\05\00\02\18\03\0e:\0b;\0bI\13\00\00\134\00\02\17\03\0e:\0b;\0bI\13\00\00\14\1d\011\13\11\01\12\06X\0bY\0bW\0b\00\00\15.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\16\0b\01\11\01\12\06\00\00\17\89\82\01\001\13\11\01\00\00\18.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\19\05\00I\13\00\00\1a\18\00\00\00\1b&\00I\13\00\00\1c.\01\03\0e:\0b;\0b'\19<\19?\19\87\01\19\00\00\00")
  (@custom ".debug_info" "\eb\02\00\00\04\00\00\00\00\00\04\01\e6\00\00\00\1d\00\c3\00\00\00\00\00\00\00Y\00\00\00\00\00\00\00\00\00\00\00\02\d0\00\00\007\00\00\00\01\10\05\03@\04\00\00\03C\00\00\00\04U\00\00\00\10\00\05N\00\00\00=\00\00\00\01\0d\06\12\00\00\00\05\04\07\d2\00\00\00\08\07\02\1b\00\00\00N\00\00\00\01\12\05\03\80\04\00\00\02U\00\00\00N\00\00\00\01\14\05\03\84\04\00\00\024\00\00\00N\00\00\00\01\15\05\03\88\04\00\00\08\9c\00\00\00\01P\05\03\19\04\00\00\03\a8\00\00\00\04U\00\00\00\1d\00\06$\00\00\00\06\01\08\bc\00\00\00\01X\05\03\00\04\00\00\03\a8\00\00\00\04U\00\00\00\19\00\02\22\00\00\007\00\00\00\01\0f\05\03\d0\04\00\00\02\19\00\00\007\00\00\00\01\0f\05\03\90\04\00\00\02.\00\00\00N\00\00\00\01\13\05\03\10\05\00\00\09\06\00\00\00\c4\00\00\00\07\ed\03\00\00\00\00\9f\91\01\00\00\0a\00\00\00\00\9d\01\00\00\0aH\00\00\00\a8\01\00\00\0a\82\00\00\00\b3\01\00\00\0a\ba\00\00\00\be\01\00\00\00\09\cb\00\00\00B\00\00\00\07\ed\03\00\00\00\00\9fg\01\00\00\0b\04\ed\00\00\9fo\01\00\00\0a\d8\00\00\00z\01\00\00\0a\f5\00\00\00\85\01\00\00\00\0cN\00\00\00\01*\01\0d2\00\00\00\01*N\00\00\00\0eE\00\00\00\01-N\00\00\00\0e\d0\00\00\00\01,C\00\00\00\00\0f)\00\00\00\01\17N\00\00\00\01\0e\00\00\00\00\01\1a\ca\01\00\00\0e\02\00\00\00\01\1a\ca\01\00\00\0eE\00\00\00\01\19N\00\00\00\0e\d0\00\00\00\01\1aC\00\00\00\00\10C\00\00\00\11\0f\01\00\00\f4\01\00\00\07\ed\03\00\00\00\00\9fB\00\00\00\014\12\04\ed\00\00\9f2\00\00\00\014N\00\00\00\13\13\01\00\00E\00\00\00\016N\00\00\00\0eL\00\00\00\016N\00\00\00\14g\01\00\00O\01\00\00J\00\00\00\019\07\0aK\01\00\00z\01\00\00\0ah\01\00\00\85\01\00\00\00\14\91\01\00\00\13\02\00\00\a7\00\00\00\01C\18\0a\86\01\00\00\9d\01\00\00\0a\c0\01\00\00\a8\01\00\00\0a\ec\01\00\00\b3\01\00\00\0a$\02\00\00\be\01\00\00\00\00\15\05\03\00\00\7f\01\00\00\04\ed\00\00\9f\0b\00\00\00\01JN\00\00\00\13B\02\00\00E\00\00\00\01LN\00\00\00\16`\04\00\00$\00\00\00\13|\02\00\00\04\00\00\00\01P\d7\02\00\00\00\17\cf\01\00\00\18\04\00\00\17\c5\02\00\00P\04\00\00\17\e1\02\00\00\82\04\00\00\00\18G\00\00\00\02\f3N\00\00\00\19\d7\02\00\00\1a\00\10\dc\02\00\00\1b\a8\00\00\00\1c\16\00\00\00\02\e5\19N\00\00\00\00\00")
  (@custom ".debug_ranges" "\06\00\00\00\ca\00\00\00\cb\00\00\00\0d\01\00\00\0f\01\00\00\03\03\00\00\05\03\00\00\84\04\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "y\00x\00output\00_start\00int\00exit\00maxflips\00char\00flip\00max_n\00checksum\00elem\00tk\00i\00printf\00rotate\00odd\00/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00fankuchredux.c\00__ARRAY_SIZE_TYPE__\00clang version 19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)\00")
  (@custom ".debug_line" "\f6\02\00\00\04\00:\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00fankuchredux.c\00\00\00\00stdlib.h\00\01\00\00\00\05\04\0a\00\05\02\09\00\00\00\03\1f\01\05\11\06\ba\05\04\90\05\11\82\05\1b\06i\05#\06\ac\03]<\03#\ba\05\19<\05\0e\06\08Y\05\19\06\90\05\17t\05\22<\05\14\ac\03\5c \05\1f\03$f\05#\06\1f\06\03]X\05\0f\06\03&<\06\03Z\90\05\08\06\03%X\06\03[<\05\0d\06\03&J\06\03Z\ac\05\04\03&\08<\03ZJ\06\03'.\02\03\00\01\01\05\08\0a\00\05\02\ce\00\00\00\03-\01\06\03R\ac\05\12\06\03/f\05\04\06 \05$.\05\04\f2\05$ \03Qt\05\04\06\030X\05\09\06\82\05\01\06Y\02\01\00\01\01\00\05\02\0f\01\00\00\034\01\05\0d\0a\93\05\04\06 \03H.\05\08\06\03.\024\01\06\03R\90\05\12\06\03/f\05\04\06 \03Q.\03/\f2\05$ \03Qt\05\04\06\030X\05\09\06\9e\05\0b\06\03\0a\08\12\05\10\06f\05\0bX\03F.\05\11\06\03;J\05\0d\06t\03E<\06\03\c1\00\90\05\0b\06 \06\08,\85\06t\03\be\7fJ\03\c2\00J\03\be\7f.\05\0e\06\03\c3\00\90\06\03\bd\7f\c8\03\c3\00J\03\bd\7f.\05\04\06\03 \c8\06\03`\9e\05\1b\06\03#J\05#\06\ac\03]<\03#\ba\05\19<\05\0e\06\08Y\05\19\06\90\05\17t\05\22<\05\14\ac\03\5c \05\1f\03$f\05#\06\1f\06\03]X\05\0f\06\03&<\06\03Z\90\05\08\06\03%X\06\03[<\05\0d\06\03&J\06\03Z\ac\05\04\03&\08<\05\0e\06\03\1dJ\05\10/\06\03\bc\7f\90\05%\03\c4\00.\03\bc\7f\c8\05\16\06\03\c5\00\90\05\13\06\90\03\bb\7f\08 \05\0d\06\038 \05\04\06X\05\01\06\03\10J\02\01\00\01\01\00\05\02\05\03\00\00\03\ca\00\01\05\0a\0a\08?\05\0c\06f\05\0af\05\12\06\e5\06\03\b1\7ff\05\04\06\03\d5\00J\06\03\ab\7f\e4\03\d5\00J\05%\e4\03\ab\7f\90\03\d5\00J\03\ab\7f\90\03\d5\00J\03\ab\7f\90\03\d5\00J\03\ab\7f\90\03\d5\00J\03\ab\7f\90\03\d5\00J\03\ab\7f\90\03\d5\00J\03\ab\7f\90\03\d5\00J\05\04\c8\05\1ct\05\04 \03\ab\7f\f2\03\d5\00J\05%\9e\05\04\c8\05\1ct\03\ab\7f<\05\04\03\d5\00J\06u\05)\84\05\04\06\ac\05)<\053J\05\04t\05)<\05:J\05\04t\06\08\22\05)\aa\05\040\06\03\a6\7f \05\07\06\03\d1\00 \06\03\af\7ff\05\0f\03\d1\00J\05\07\90\06\91\02\08\00\01\01")
  (@custom "name" "\00\12\11fankuchredux.wasm\01l\0b\00\06memcpy\01\07memmove\02\15__VERIFIER_nondet_int\03\06printf\04\06fwrite\05\04exit\06\11__wasm_call_ctors\07\04flip\08\06rotate\09\02tk\0a\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clangZ19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)")
  (@custom "target_features" "\04+\0amultivalue+\0fmutable-globals+\0freference-types+\08sign-ext"))
