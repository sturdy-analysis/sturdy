(module $fankuchredux.wasm
  (type (;0;) (func (param i32 i32 i32) (result i32)))
  (type (;1;) (func (result i32)))
  (type (;2;) (func (param i32 i32) (result i32)))
  (type (;3;) (func))
  (type (;4;) (func (param i32)))
  (import "env" "memcpy" (func $memcpy (type 0)))
  (import "env" "memmove" (func $memmove (type 0)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type 1)))
  (import "env" "printf" (func $printf (type 2)))
  (func $__wasm_call_ctors (type 3))
  (func $flip (type 1) (result i32)
    (local i32 i32 i32 i32)
    block  ;; label = @1
      i32.const 0
      i32.load offset=1200
      local.tee 0
      i32.eqz
      br_if 0 (;@1;)
      i32.const 1072
      i32.const 1136
      local.get 0
      i32.const 2
      i32.shl
      call $memcpy
      drop
    end
    block  ;; label = @1
      block  ;; label = @2
        i32.const 0
        i32.load offset=1072
        local.tee 1
        i32.const 1
        i32.lt_s
        br_if 0 (;@2;)
        i32.const 1
        local.set 2
        loop  ;; label = @3
          block  ;; label = @4
            local.get 1
            i32.const 1
            i32.lt_s
            br_if 0 (;@4;)
            i32.const 1072
            local.set 0
            local.get 1
            i32.const 2
            i32.shl
            i32.const 1072
            i32.add
            local.set 1
            loop  ;; label = @5
              local.get 0
              i32.load
              local.set 3
              local.get 0
              local.get 1
              i32.load
              i32.store
              local.get 1
              local.get 3
              i32.store
              local.get 0
              i32.const 4
              i32.add
              local.tee 0
              local.get 1
              i32.const -4
              i32.add
              local.tee 1
              i32.lt_u
              br_if 0 (;@5;)
            end
            i32.const 0
            i32.load offset=1072
            local.set 1
          end
          local.get 2
          i32.const 1
          i32.add
          local.set 2
          local.get 1
          i32.const 2
          i32.shl
          i32.const 1072
          i32.add
          i32.load
          br_if 0 (;@3;)
          br 2 (;@1;)
        end
      end
      i32.const 2
      local.set 2
      local.get 1
      i32.const 2
      i32.shl
      i32.const 1072
      i32.add
      i32.load
      i32.eqz
      br_if 0 (;@1;)
      loop  ;; label = @2
        br 0 (;@2;)
      end
    end
    local.get 2)
  (func $rotate (type 4) (param i32)
    (local i32)
    i32.const 0
    i32.load offset=1136
    local.set 1
    block  ;; label = @1
      local.get 0
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      i32.const 1136
      i32.const 1140
      local.get 0
      i32.const 2
      i32.shl
      call $memmove
      drop
    end
    local.get 0
    i32.const 2
    i32.shl
    i32.const 1136
    i32.add
    local.get 1
    i32.store)
  (func $tk (type 4) (param i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 64
    i32.sub
    local.tee 1
    global.set $__stack_pointer
    local.get 1
    i32.const 56
    i32.add
    i64.const 0
    i64.store
    local.get 1
    i32.const 48
    i32.add
    i64.const 0
    i64.store
    local.get 1
    i32.const 40
    i32.add
    i64.const 0
    i64.store
    local.get 1
    i32.const 32
    i32.add
    i64.const 0
    i64.store
    local.get 1
    i32.const 24
    i32.add
    i64.const 0
    i64.store
    local.get 1
    i32.const 16
    i32.add
    i64.const 0
    i64.store
    local.get 1
    i64.const 0
    i64.store offset=8
    local.get 1
    i64.const 0
    i64.store
    block  ;; label = @1
      local.get 0
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      i32.const 0
      i32.load offset=1200
      local.tee 2
      i32.const 2
      i32.shl
      local.set 3
      i32.const 0
      i32.load offset=1060
      local.set 4
      i32.const 0
      i32.load offset=1056
      local.set 5
      i32.const 0
      i32.load offset=1064
      local.set 6
      i32.const 0
      local.set 7
      loop  ;; label = @2
        i32.const 0
        i32.load offset=1136
        local.set 8
        block  ;; label = @3
          local.get 7
          i32.const 1
          i32.lt_s
          br_if 0 (;@3;)
          i32.const 1136
          i32.const 1140
          local.get 7
          i32.const 2
          i32.shl
          call $memmove
          drop
        end
        local.get 7
        i32.const 2
        i32.shl
        local.tee 9
        i32.const 1136
        i32.add
        local.get 8
        i32.store
        block  ;; label = @3
          block  ;; label = @4
            local.get 1
            local.get 9
            i32.add
            local.tee 8
            i32.load
            local.tee 9
            local.get 7
            i32.lt_s
            br_if 0 (;@4;)
            local.get 8
            i32.const 0
            i32.store
            local.get 7
            i32.const 1
            i32.add
            local.set 7
            br 1 (;@3;)
          end
          i32.const 0
          local.get 4
          i32.const -1
          i32.xor
          local.tee 10
          i32.store offset=1060
          i32.const 1
          local.set 7
          local.get 8
          local.get 9
          i32.const 1
          i32.add
          i32.store
          block  ;; label = @4
            i32.const 0
            i32.load offset=1136
            local.tee 8
            br_if 0 (;@4;)
            local.get 10
            local.set 4
            br 1 (;@3;)
          end
          block  ;; label = @4
            block  ;; label = @5
              local.get 8
              i32.const 2
              i32.shl
              i32.const 1136
              i32.add
              i32.load
              br_if 0 (;@5;)
              i32.const 1
              local.set 11
              br 1 (;@4;)
            end
            block  ;; label = @5
              local.get 2
              i32.eqz
              br_if 0 (;@5;)
              i32.const 1072
              i32.const 1136
              local.get 3
              call $memcpy
              drop
            end
            block  ;; label = @5
              i32.const 0
              i32.load offset=1072
              local.tee 8
              i32.const 1
              i32.lt_s
              br_if 0 (;@5;)
              i32.const 1
              local.set 11
              loop  ;; label = @6
                block  ;; label = @7
                  local.get 8
                  i32.const 1
                  i32.lt_s
                  br_if 0 (;@7;)
                  i32.const 1072
                  local.set 7
                  local.get 8
                  i32.const 2
                  i32.shl
                  i32.const 1072
                  i32.add
                  local.set 8
                  loop  ;; label = @8
                    local.get 7
                    i32.load
                    local.set 9
                    local.get 7
                    local.get 8
                    i32.load
                    i32.store
                    local.get 8
                    local.get 9
                    i32.store
                    local.get 7
                    i32.const 4
                    i32.add
                    local.tee 7
                    local.get 8
                    i32.const -4
                    i32.add
                    local.tee 8
                    i32.lt_u
                    br_if 0 (;@8;)
                  end
                  i32.const 0
                  i32.load offset=1072
                  local.set 8
                end
                local.get 11
                i32.const 1
                i32.add
                local.set 11
                local.get 8
                i32.const 2
                i32.shl
                i32.const 1072
                i32.add
                i32.load
                br_if 0 (;@6;)
                br 2 (;@4;)
              end
            end
            i32.const 2
            local.set 11
            local.get 8
            i32.const 2
            i32.shl
            i32.const 1072
            i32.add
            i32.load
            i32.eqz
            br_if 0 (;@4;)
            loop  ;; label = @5
              br 0 (;@5;)
            end
          end
          block  ;; label = @4
            local.get 11
            local.get 5
            i32.le_s
            br_if 0 (;@4;)
            i32.const 0
            local.get 11
            i32.store offset=1056
            local.get 11
            local.set 5
          end
          i32.const 0
          local.get 6
          local.get 11
          i32.const 0
          local.get 11
          i32.sub
          local.get 4
          i32.const -1
          i32.eq
          select
          i32.add
          local.tee 6
          i32.store offset=1064
          i32.const 1
          local.set 7
          local.get 10
          local.set 4
        end
        local.get 7
        local.get 0
        i32.lt_s
        br_if 0 (;@2;)
      end
    end
    local.get 1
    i32.const 64
    i32.add
    global.set $__stack_pointer)
  (func $_start (type 1) (result i32)
    (local i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 0
    global.set $__stack_pointer
    i32.const 0
    call $__VERIFIER_nondet_int
    local.tee 1
    i32.store offset=1200
    block  ;; label = @1
      local.get 1
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      local.get 1
      i32.const 7
      i32.and
      local.set 2
      i32.const 0
      local.set 3
      block  ;; label = @2
        local.get 1
        i32.const 8
        i32.lt_u
        br_if 0 (;@2;)
        local.get 1
        i32.const 2147483640
        i32.and
        local.set 4
        i32.const 0
        local.set 3
        i32.const 1136
        local.set 5
        loop  ;; label = @3
          local.get 5
          local.get 3
          i32.store
          local.get 5
          i32.const 28
          i32.add
          local.get 3
          i32.const 7
          i32.add
          i32.store
          local.get 5
          i32.const 24
          i32.add
          local.get 3
          i32.const 6
          i32.add
          i32.store
          local.get 5
          i32.const 20
          i32.add
          local.get 3
          i32.const 5
          i32.add
          i32.store
          local.get 5
          i32.const 16
          i32.add
          local.get 3
          i32.const 4
          i32.add
          i32.store
          local.get 5
          i32.const 12
          i32.add
          local.get 3
          i32.const 3
          i32.add
          i32.store
          local.get 5
          i32.const 8
          i32.add
          local.get 3
          i32.const 2
          i32.add
          i32.store
          local.get 5
          i32.const 4
          i32.add
          local.get 3
          i32.const 1
          i32.add
          i32.store
          local.get 5
          i32.const 32
          i32.add
          local.set 5
          local.get 3
          i32.const 8
          i32.add
          local.tee 3
          local.get 4
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 2
      i32.eqz
      br_if 0 (;@1;)
      local.get 3
      i32.const 2
      i32.shl
      i32.const 1136
      i32.add
      local.set 5
      loop  ;; label = @2
        local.get 5
        local.get 3
        i32.store
        local.get 5
        i32.const 4
        i32.add
        local.set 5
        local.get 3
        i32.const 1
        i32.add
        local.set 3
        local.get 2
        i32.const -1
        i32.add
        local.tee 2
        br_if 0 (;@2;)
      end
    end
    local.get 1
    call $tk
    local.get 0
    i32.const 0
    i32.load offset=1064
    i32.store
    local.get 0
    i32.const 0
    i32.load offset=1200
    i32.store offset=4
    local.get 0
    i32.const 0
    i32.load offset=1056
    i32.store offset=8
    i32.const 1024
    local.get 0
    call $printf
    drop
    local.get 0
    i32.const 16
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 66752))
  (global (;1;) i32 (i32.const 1200))
  (global (;2;) i32 (i32.const 1072))
  (global (;3;) i32 (i32.const 1136))
  (global (;4;) i32 (i32.const 1060))
  (global (;5;) i32 (i32.const 1056))
  (global (;6;) i32 (i32.const 1064))
  (global (;7;) i32 (i32.const 1024))
  (global (;8;) i32 (i32.const 1204))
  (global (;9;) i32 (i32.const 1216))
  (global (;10;) i32 (i32.const 66752))
  (global (;11;) i32 (i32.const 1024))
  (global (;12;) i32 (i32.const 66752))
  (global (;13;) i32 (i32.const 131072))
  (global (;14;) i32 (i32.const 0))
  (global (;15;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "flip" (func $flip))
  (export "max_n" (global 1))
  (export "t" (global 2))
  (export "s" (global 3))
  (export "rotate" (func $rotate))
  (export "tk" (func $tk))
  (export "odd" (global 4))
  (export "maxflips" (global 5))
  (export "checksum" (global 6))
  (export "_start" (func $_start))
  (export "__dso_handle" (global 7))
  (export "__data_end" (global 8))
  (export "__stack_low" (global 9))
  (export "__stack_high" (global 10))
  (export "__global_base" (global 11))
  (export "__heap_base" (global 12))
  (export "__heap_end" (global 13))
  (export "__memory_base" (global 14))
  (export "__table_base" (global 15))
  (data $.rodata (i32.const 1024) "%d\0aPfannkuchen(%d) = %d\0a\00"))
