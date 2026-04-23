(module $fankuchredux.wasm
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32 i32) (result i32)))
  (type $t2 (func (param i32 i32 i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t0)))
  (import "env" "printf" (func $printf (type $t1)))
  (import "env" "fwrite" (func $fwrite (type $t2)))
  (import "env" "exit" (func $exit (type $t3)))
  (func $tk (type $t3) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
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
          local.get $l5
          i32.const 2
          i32.shl
          local.tee $l7
          i32.eqz
          br_if $B2
          i32.const 1232
          i32.const 1236
          local.get $l7
          memory.copy
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
            block $B8
              local.get $l1
              i32.eqz
              br_if $B8
              i32.const 1168
              i32.const 1232
              local.get $l1
              memory.copy
            end
            block $B9
              i32.const 0
              i32.load offset=1168
              local.tee $l6
              i32.const 1
              i32.lt_s
              br_if $B9
              i32.const 1
              local.set $l9
              loop $L10
                block $B11
                  local.get $l6
                  i32.const 1
                  i32.lt_s
                  br_if $B11
                  i32.const 1168
                  local.set $l5
                  local.get $l6
                  i32.const 2
                  i32.shl
                  i32.const 1168
                  i32.add
                  local.set $l6
                  loop $L12
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
                    br_if $L12
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
                i32.const 1168
                i32.add
                i32.load
                br_if $L10
                br $B6
              end
            end
            i32.const 2
            local.set $l9
            local.get $l6
            i32.const 2
            i32.shl
            i32.const 1168
            i32.add
            i32.load
            i32.eqz
            br_if $B6
            loop $L13
              br $L13
            end
          end
          block $B14
            local.get $l9
            local.get $l3
            i32.le_s
            br_if $B14
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
  (func $_start (type $t0) (result i32)
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
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "%d\0aPfannkuchen(%d) = %d\0a\00range: must be 3 <= n <= 12\0a\00")
  (@custom "name" "\00\12\11fankuchredux.wasm\01:\06\00\15__VERIFIER_nondet_int\01\06printf\02\06fwrite\03\04exit\04\02tk\05\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
