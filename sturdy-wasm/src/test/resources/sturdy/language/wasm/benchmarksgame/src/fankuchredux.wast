(module $fankuchredux.wasm
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param i32 i32 i32) (result i32)))
  (type (;4;) (func (param i32 i32) (result i32)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type 2)))
  (import "wasi_snapshot_preview1" "proc_exit" (func $__wasi_proc_exit (type 0)))
  (func $__wasm_call_ctors (type 1)
    nop)
  (func $tk (type 0) (param i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const -64
    i32.add
    local.tee 3
    global.set $__stack_pointer
    local.get 3
    i64.const 0
    i64.store offset=56
    local.get 3
    i64.const 0
    i64.store offset=48
    local.get 3
    i64.const 0
    i64.store offset=40
    local.get 3
    i64.const 0
    i64.store offset=32
    local.get 3
    i64.const 0
    i64.store offset=24
    local.get 3
    i64.const 0
    i64.store offset=16
    local.get 3
    i64.const 0
    i64.store offset=8
    local.get 3
    i64.const 0
    i64.store
    local.get 0
    i32.const 0
    i32.gt_s
    if  ;; label = @1
      i32.const 1168
      i32.load
      local.tee 10
      i32.const 2
      i32.shl
      local.set 11
      i32.const 1028
      i32.load
      local.set 6
      i32.const 1024
      i32.load
      local.set 7
      i32.const 1032
      i32.load
      local.set 8
      loop  ;; label = @2
        i32.const 1104
        i32.load
        local.set 1
        local.get 2
        i32.const 0
        i32.gt_s
        if  ;; label = @3
          i32.const 1104
          i32.const 1108
          local.get 2
          i32.const 2
          i32.shl
          call $memmove
          drop
        end
        local.get 2
        i32.const 2
        i32.shl
        local.tee 5
        i32.const 1104
        i32.add
        local.get 1
        i32.store
        block  ;; label = @3
          local.get 2
          local.get 3
          local.get 5
          i32.add
          local.tee 1
          i32.load
          local.tee 5
          i32.le_s
          if  ;; label = @4
            local.get 1
            i32.const 0
            i32.store
            local.get 2
            i32.const 1
            i32.add
            local.set 2
            br 1 (;@3;)
          end
          i32.const 1
          local.set 2
          local.get 1
          local.get 5
          i32.const 1
          i32.add
          i32.store
          i32.const 1028
          local.get 6
          i32.const -1
          i32.xor
          local.tee 9
          i32.store
          i32.const 1104
          i32.load
          local.tee 1
          i32.eqz
          if  ;; label = @4
            local.get 9
            local.set 6
            br 1 (;@3;)
          end
          block  ;; label = @4
            local.get 1
            i32.const 2
            i32.shl
            i32.const 1104
            i32.add
            i32.load
            i32.eqz
            if  ;; label = @5
              i32.const 1
              local.set 4
              br 1 (;@4;)
            end
            local.get 10
            if  ;; label = @5
              i32.const 1040
              i32.const 1104
              local.get 11
              call $__memcpy
              drop
            end
            i32.const 1
            local.set 4
            i32.const 1040
            i32.load
            local.tee 1
            i32.const 0
            i32.le_s
            if  ;; label = @5
              i32.const 2
              local.set 4
              local.get 1
              i32.const 2
              i32.shl
              i32.const 1040
              i32.add
              i32.load
              i32.eqz
              br_if 1 (;@4;)
              loop  ;; label = @6
                br 0 (;@6;)
              end
              unreachable
            end
            loop  ;; label = @5
              local.get 4
              i32.const 1
              i32.add
              local.set 4
              local.get 1
              i32.const 0
              i32.gt_s
              if  ;; label = @6
                i32.const 1040
                local.set 2
                local.get 1
                i32.const 2
                i32.shl
                i32.const 1040
                i32.add
                local.set 1
                loop  ;; label = @7
                  local.get 2
                  i32.load
                  local.set 5
                  local.get 2
                  local.get 1
                  i32.load
                  i32.store
                  local.get 1
                  local.get 5
                  i32.store
                  local.get 2
                  i32.const 4
                  i32.add
                  local.tee 2
                  local.get 1
                  i32.const 4
                  i32.sub
                  local.tee 1
                  i32.lt_u
                  br_if 0 (;@7;)
                end
                i32.const 1040
                i32.load
                local.set 1
              end
              local.get 1
              i32.const 2
              i32.shl
              i32.const 1040
              i32.add
              i32.load
              br_if 0 (;@5;)
            end
          end
          local.get 4
          local.get 7
          i32.gt_s
          if  ;; label = @4
            i32.const 1024
            local.get 4
            i32.store
            local.get 4
            local.set 7
          end
          i32.const 1032
          local.get 8
          local.get 4
          i32.const 0
          local.get 4
          i32.sub
          local.get 6
          i32.const -1
          i32.eq
          select
          i32.add
          local.tee 8
          i32.store
          i32.const 1
          local.set 2
          local.get 9
          local.set 6
        end
        local.get 0
        local.get 2
        i32.gt_s
        br_if 0 (;@2;)
      end
    end
    local.get 3
    i32.const -64
    i32.sub
    global.set $__stack_pointer)
  (func $__original_main (type 2) (result i32)
    (local i32 i32 i32 i32 i32 i32)
    i32.const 1168
    call $__VERIFIER_nondet_int
    local.tee 2
    i32.store
    block  ;; label = @1
      local.get 2
      i32.const 0
      i32.le_s
      br_if 0 (;@1;)
      local.get 2
      i32.const 8
      i32.ge_u
      if  ;; label = @2
        local.get 2
        i32.const 2147483640
        i32.and
        local.set 4
        loop  ;; label = @3
          local.get 0
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 0
          i32.store
          local.get 0
          i32.const 1
          i32.or
          local.tee 1
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 2
          i32.or
          local.tee 1
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 3
          i32.or
          local.tee 1
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 4
          i32.or
          local.tee 1
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 5
          i32.or
          local.tee 1
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 6
          i32.or
          local.tee 1
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 7
          i32.or
          local.tee 1
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 8
          i32.add
          local.set 0
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
      i32.const 7
      i32.and
      local.tee 5
      i32.eqz
      br_if 0 (;@1;)
      i32.const 0
      local.set 3
      loop  ;; label = @2
        local.get 0
        i32.const 2
        i32.shl
        i32.const 1104
        i32.add
        local.get 0
        i32.store
        local.get 0
        i32.const 1
        i32.add
        local.set 0
        local.get 3
        i32.const 1
        i32.add
        local.tee 3
        local.get 5
        i32.ne
        br_if 0 (;@2;)
      end
    end
    local.get 2
    call $tk
    i32.const 0)
  (func $main (type 4) (param i32 i32) (result i32)
    call $__original_main)
  (func $_start (type 1)
    call $__wasm_call_ctors
    call $__original_main
    call $exit
    unreachable)
  (func $__memcpy (type 3) (param i32 i32 i32) (result i32)
    (local i32 i32 i32)
    local.get 0
    local.get 2
    i32.add
    local.set 3
    block  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          local.get 0
          local.get 1
          i32.xor
          i32.const 3
          i32.and
          i32.eqz
          if  ;; label = @4
            local.get 0
            i32.const 3
            i32.and
            i32.eqz
            br_if 1 (;@3;)
            local.get 2
            i32.const 0
            i32.le_s
            br_if 1 (;@3;)
            local.get 0
            local.set 2
            loop  ;; label = @5
              local.get 2
              local.get 1
              i32.load8_u
              i32.store8
              local.get 1
              i32.const 1
              i32.add
              local.set 1
              local.get 2
              i32.const 1
              i32.add
              local.tee 2
              i32.const 3
              i32.and
              i32.eqz
              br_if 3 (;@2;)
              local.get 2
              local.get 3
              i32.lt_u
              br_if 0 (;@5;)
            end
            br 2 (;@2;)
          end
          block  ;; label = @4
            local.get 3
            i32.const 4
            i32.lt_u
            br_if 0 (;@4;)
            local.get 3
            i32.const 4
            i32.sub
            local.tee 4
            local.get 0
            i32.lt_u
            br_if 0 (;@4;)
            local.get 0
            local.set 2
            loop  ;; label = @5
              local.get 2
              local.get 1
              i32.load8_u
              i32.store8
              local.get 2
              local.get 1
              i32.load8_u offset=1
              i32.store8 offset=1
              local.get 2
              local.get 1
              i32.load8_u offset=2
              i32.store8 offset=2
              local.get 2
              local.get 1
              i32.load8_u offset=3
              i32.store8 offset=3
              local.get 1
              i32.const 4
              i32.add
              local.set 1
              local.get 2
              i32.const 4
              i32.add
              local.tee 2
              local.get 4
              i32.le_u
              br_if 0 (;@5;)
            end
            br 3 (;@1;)
          end
          local.get 0
          local.set 2
          br 2 (;@1;)
        end
        local.get 0
        local.set 2
      end
      local.get 3
      i32.const -4
      i32.and
      local.set 4
      block  ;; label = @2
        local.get 3
        i32.const 64
        i32.lt_u
        br_if 0 (;@2;)
        local.get 2
        local.get 4
        i32.const -64
        i32.add
        local.tee 5
        i32.gt_u
        br_if 0 (;@2;)
        loop  ;; label = @3
          local.get 2
          local.get 1
          i32.load
          i32.store
          local.get 2
          local.get 1
          i32.load offset=4
          i32.store offset=4
          local.get 2
          local.get 1
          i32.load offset=8
          i32.store offset=8
          local.get 2
          local.get 1
          i32.load offset=12
          i32.store offset=12
          local.get 2
          local.get 1
          i32.load offset=16
          i32.store offset=16
          local.get 2
          local.get 1
          i32.load offset=20
          i32.store offset=20
          local.get 2
          local.get 1
          i32.load offset=24
          i32.store offset=24
          local.get 2
          local.get 1
          i32.load offset=28
          i32.store offset=28
          local.get 2
          local.get 1
          i32.load offset=32
          i32.store offset=32
          local.get 2
          local.get 1
          i32.load offset=36
          i32.store offset=36
          local.get 2
          local.get 1
          i32.load offset=40
          i32.store offset=40
          local.get 2
          local.get 1
          i32.load offset=44
          i32.store offset=44
          local.get 2
          local.get 1
          i32.load offset=48
          i32.store offset=48
          local.get 2
          local.get 1
          i32.load offset=52
          i32.store offset=52
          local.get 2
          local.get 1
          i32.load offset=56
          i32.store offset=56
          local.get 2
          local.get 1
          i32.load offset=60
          i32.store offset=60
          local.get 1
          i32.const -64
          i32.sub
          local.set 1
          local.get 2
          i32.const -64
          i32.sub
          local.tee 2
          local.get 5
          i32.le_u
          br_if 0 (;@3;)
        end
      end
      local.get 2
      local.get 4
      i32.ge_u
      br_if 0 (;@1;)
      loop  ;; label = @2
        local.get 2
        local.get 1
        i32.load
        i32.store
        local.get 1
        i32.const 4
        i32.add
        local.set 1
        local.get 2
        i32.const 4
        i32.add
        local.tee 2
        local.get 4
        i32.lt_u
        br_if 0 (;@2;)
      end
    end
    local.get 2
    local.get 3
    i32.lt_u
    if  ;; label = @1
      loop  ;; label = @2
        local.get 2
        local.get 1
        i32.load8_u
        i32.store8
        local.get 1
        i32.const 1
        i32.add
        local.set 1
        local.get 2
        i32.const 1
        i32.add
        local.tee 2
        local.get 3
        i32.ne
        br_if 0 (;@2;)
      end
    end
    local.get 0)
  (func $dummy (type 1)
    nop)
  (func $libc_exit_fini (type 1)
    call $dummy)
  (func $exit (type 0) (param i32)
    call $dummy
    call $libc_exit_fini
    call $dummy
    local.get 0
    call $_Exit
    unreachable)
  (func $_Exit (type 0) (param i32)
    local.get 0
    call $__wasi_proc_exit
    unreachable)
  (func $memmove (type 3) (param i32 i32 i32) (result i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      local.get 1
      i32.eq
      br_if 0 (;@1;)
      local.get 1
      local.get 0
      local.get 2
      i32.add
      local.tee 3
      i32.sub
      i32.const 0
      local.get 2
      i32.const 1
      i32.shl
      i32.sub
      i32.le_u
      if  ;; label = @2
        local.get 0
        local.get 1
        local.get 2
        call $__memcpy
        return
      end
      local.get 0
      local.get 1
      i32.xor
      i32.const 3
      i32.and
      local.set 4
      block  ;; label = @2
        block  ;; label = @3
          local.get 0
          local.get 1
          i32.lt_u
          if  ;; label = @4
            local.get 4
            if  ;; label = @5
              local.get 0
              local.set 3
              br 3 (;@2;)
            end
            local.get 0
            i32.const 3
            i32.and
            i32.eqz
            if  ;; label = @5
              local.get 0
              local.set 3
              br 2 (;@3;)
            end
            local.get 0
            local.set 3
            loop  ;; label = @5
              local.get 2
              i32.eqz
              br_if 4 (;@1;)
              local.get 3
              local.get 1
              i32.load8_u
              i32.store8
              local.get 1
              i32.const 1
              i32.add
              local.set 1
              local.get 2
              i32.const 1
              i32.sub
              local.set 2
              local.get 3
              i32.const 1
              i32.add
              local.tee 3
              i32.const 3
              i32.and
              br_if 0 (;@5;)
            end
            br 1 (;@3;)
          end
          block  ;; label = @4
            local.get 4
            br_if 0 (;@4;)
            local.get 3
            i32.const 3
            i32.and
            if  ;; label = @5
              loop  ;; label = @6
                local.get 2
                i32.eqz
                br_if 5 (;@1;)
                local.get 0
                local.get 2
                i32.const 1
                i32.sub
                local.tee 2
                i32.add
                local.tee 3
                local.get 1
                local.get 2
                i32.add
                i32.load8_u
                i32.store8
                local.get 3
                i32.const 3
                i32.and
                br_if 0 (;@6;)
              end
            end
            local.get 2
            i32.const 3
            i32.le_u
            br_if 0 (;@4;)
            loop  ;; label = @5
              local.get 0
              local.get 2
              i32.const 4
              i32.sub
              local.tee 2
              i32.add
              local.get 1
              local.get 2
              i32.add
              i32.load
              i32.store
              local.get 2
              i32.const 3
              i32.gt_u
              br_if 0 (;@5;)
            end
          end
          local.get 2
          i32.eqz
          br_if 2 (;@1;)
          loop  ;; label = @4
            local.get 0
            local.get 2
            i32.const 1
            i32.sub
            local.tee 2
            i32.add
            local.get 1
            local.get 2
            i32.add
            i32.load8_u
            i32.store8
            local.get 2
            br_if 0 (;@4;)
          end
          br 2 (;@1;)
        end
        local.get 2
        i32.const 3
        i32.le_u
        br_if 0 (;@2;)
        loop  ;; label = @3
          local.get 3
          local.get 1
          i32.load
          i32.store
          local.get 1
          i32.const 4
          i32.add
          local.set 1
          local.get 3
          i32.const 4
          i32.add
          local.set 3
          local.get 2
          i32.const 4
          i32.sub
          local.tee 2
          i32.const 3
          i32.gt_u
          br_if 0 (;@3;)
        end
      end
      local.get 2
      i32.eqz
      br_if 0 (;@1;)
      loop  ;; label = @2
        local.get 3
        local.get 1
        i32.load8_u
        i32.store8
        local.get 3
        i32.const 1
        i32.add
        local.set 3
        local.get 1
        i32.const 1
        i32.add
        local.set 1
        local.get 2
        i32.const 1
        i32.sub
        local.tee 2
        br_if 0 (;@2;)
      end
    end
    local.get 0)
  (func $_emscripten_stack_restore (type 0) (param i32)
    local.get 0
    global.set $__stack_pointer)
  (func $emscripten_stack_get_current (type 2) (result i32)
    global.get $__stack_pointer)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 258 258)
  (global $__stack_pointer (mut i32) (i32.const 66720))
  (export "memory" (memory 0))
  (export "main" (func $main))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func $_start))
  (export "_emscripten_stack_restore" (func $_emscripten_stack_restore))
  (export "emscripten_stack_get_current" (func $emscripten_stack_get_current))
  (elem (;0;) (i32.const 1) func $__wasm_call_ctors))
