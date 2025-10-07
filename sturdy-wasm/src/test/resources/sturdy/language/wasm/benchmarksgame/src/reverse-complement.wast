(module $reverse-complement.wasm
  (type (;0;) (func (param i32 i32 i32) (result i32)))
  (type (;1;) (func (param i32) (result i32)))
  (type (;2;) (func (param i32 i32) (result i32)))
  (type (;3;) (func (param i32)))
  (type (;4;) (func))
  (type (;5;) (func (param i32 i32)))
  (type (;6;) (func (result i32)))
  (import "env" "memmove" (func $memmove (type 0)))
  (import "env" "toupper" (func $toupper (type 1)))
  (import "env" "tolower" (func $tolower (type 1)))
  (import "env" "malloc" (func $malloc (type 1)))
  (import "env" "fileno" (func $fileno (type 1)))
  (import "env" "read" (func $read (type 0)))
  (import "env" "realloc" (func $realloc (type 2)))
  (import "env" "write" (func $write (type 0)))
  (import "env" "free" (func $free (type 3)))
  (func $__wasm_call_ctors (type 4))
  (func $process (type 5) (param i32 i32)
    (local i32 i32 i32 i32 i32 i32)
    local.get 1
    local.get 0
    i32.sub
    local.set 2
    i32.const 0
    local.set 3
    loop  ;; label = @1
      local.get 2
      i32.const -1
      i32.add
      local.tee 4
      local.set 2
      local.get 3
      local.tee 5
      i32.const 1
      i32.add
      local.tee 6
      local.set 3
      local.get 0
      local.get 5
      i32.add
      i32.load8_u
      i32.const 10
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 0
    local.get 6
    i32.add
    local.set 7
    block  ;; label = @1
      local.get 1
      local.get 0
      i32.sub
      local.get 4
      i32.const 61
      i32.div_u
      i32.const 61
      i32.mul
      local.tee 3
      i32.sub
      local.tee 2
      i32.const -60
      i32.add
      local.get 6
      i32.eq
      br_if 0 (;@1;)
      local.get 7
      local.get 2
      local.get 6
      i32.sub
      i32.add
      local.tee 2
      local.get 1
      i32.ge_u
      br_if 0 (;@1;)
      local.get 0
      local.get 1
      i32.sub
      local.get 3
      i32.add
      local.get 6
      i32.add
      i32.const 60
      i32.add
      local.set 5
      local.get 2
      local.set 2
      loop  ;; label = @2
        local.get 2
        local.tee 2
        i32.const 1
        i32.add
        local.get 2
        local.get 5
        call $memmove
        drop
        local.get 2
        i32.const 10
        i32.store8
        local.get 2
        i32.const 61
        i32.add
        local.tee 3
        local.set 2
        local.get 3
        local.get 1
        i32.lt_u
        br_if 0 (;@2;)
      end
    end
    block  ;; label = @1
      local.get 7
      local.get 1
      i32.const -1
      i32.add
      local.tee 2
      i32.gt_u
      br_if 0 (;@1;)
      local.get 2
      local.set 3
      local.get 7
      local.set 2
      loop  ;; label = @2
        local.get 2
        local.tee 2
        i32.load8_s
        i32.const 1072
        i32.add
        i32.load8_u
        local.set 5
        local.get 2
        local.get 3
        local.tee 3
        i32.load8_s
        i32.const 1072
        i32.add
        i32.load8_u
        i32.store8
        local.get 3
        local.get 5
        i32.store8
        local.get 3
        i32.const -1
        i32.add
        local.tee 5
        local.set 3
        local.get 2
        i32.const 1
        i32.add
        local.tee 0
        local.set 2
        local.get 0
        local.get 5
        i32.le_u
        br_if 0 (;@2;)
      end
    end)
  (func $_start (type 6) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    block  ;; label = @1
      i32.const 0
      i32.load offset=1060
      local.tee 0
      i32.load8_u
      local.tee 1
      i32.eqz
      br_if 0 (;@1;)
      local.get 1
      local.set 1
      local.get 0
      local.set 0
      loop  ;; label = @2
        local.get 1
        i32.extend8_s
        call $toupper
        i32.const 1072
        i32.add
        local.get 0
        local.tee 0
        i32.load8_u offset=1
        i32.store8
        local.get 0
        i32.load8_s
        call $tolower
        i32.const 1072
        i32.add
        local.get 0
        i32.load8_u offset=1
        i32.store8
        local.get 0
        i32.load8_u offset=2
        local.tee 2
        local.set 1
        local.get 0
        i32.const 2
        i32.add
        local.set 0
        local.get 2
        br_if 0 (;@2;)
      end
    end
    i32.const 8192
    call $malloc
    local.set 3
    block  ;; label = @1
      block  ;; label = @2
        i32.const 0
        i32.load
        call $fileno
        local.tee 4
        local.get 3
        i32.const 7936
        call $read
        local.tee 0
        br_if 0 (;@2;)
        i32.const 0
        local.set 0
        local.get 3
        local.set 1
        local.get 3
        local.set 5
        br 1 (;@1;)
      end
      local.get 0
      local.set 2
      i32.const 7936
      local.set 6
      local.get 3
      local.set 3
      i32.const 0
      local.set 7
      i32.const 8192
      local.set 8
      loop  ;; label = @2
        local.get 8
        local.set 1
        local.get 3
        local.set 3
        block  ;; label = @3
          block  ;; label = @4
            local.get 2
            local.get 7
            i32.add
            local.tee 0
            local.get 6
            i32.ge_u
            br_if 0 (;@4;)
            local.get 1
            local.set 2
            local.get 3
            local.set 1
            br 1 (;@3;)
          end
          local.get 1
          i32.const 1048576
          i32.add
          local.get 1
          i32.const 1
          i32.shl
          local.get 1
          i32.const 1048575
          i32.gt_u
          select
          local.tee 1
          local.set 2
          local.get 3
          local.get 1
          call $realloc
          local.set 1
        end
        local.get 4
        local.get 1
        local.tee 1
        local.get 0
        i32.add
        local.tee 5
        local.get 2
        local.tee 8
        i32.const -256
        i32.add
        local.tee 6
        local.get 0
        i32.sub
        call $read
        local.tee 9
        local.set 2
        local.get 6
        local.set 6
        local.get 1
        local.set 3
        local.get 0
        local.set 7
        local.get 8
        local.set 8
        local.get 0
        local.set 0
        local.get 1
        local.set 1
        local.get 5
        local.set 5
        local.get 9
        br_if 0 (;@2;)
      end
    end
    local.get 1
    local.set 4
    local.get 0
    local.set 10
    local.get 5
    local.tee 0
    i32.const 62
    i32.store8
    local.get 0
    i32.const -1
    i32.add
    local.set 2
    loop  ;; label = @1
      i32.const 0
      local.set 0
      i32.const -1
      local.set 1
      local.get 2
      local.tee 9
      local.set 2
      loop  ;; label = @2
        local.get 0
        local.tee 7
        i32.const 1
        i32.add
        local.set 0
        local.get 1
        local.tee 8
        i32.const -1
        i32.add
        local.set 1
        local.get 2
        local.tee 6
        i32.const -1
        i32.add
        local.tee 3
        local.set 2
        local.get 6
        i32.load8_u
        i32.const 62
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 9
      i32.const 1
      i32.add
      local.set 5
      local.get 7
      local.set 0
      local.get 8
      local.set 1
      loop  ;; label = @2
        local.get 0
        i32.const -1
        i32.add
        local.tee 7
        local.set 0
        local.get 1
        local.tee 2
        i32.const 1
        i32.add
        local.tee 6
        local.set 1
        local.get 5
        local.get 2
        i32.add
        i32.load8_u
        i32.const 10
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 9
      local.get 6
      i32.add
      i32.const 1
      i32.add
      local.set 8
      block  ;; label = @2
        local.get 7
        i32.const 61
        i32.div_u
        local.tee 1
        i32.const -61
        i32.mul
        local.tee 0
        i32.const -61
        i32.add
        local.get 6
        i32.eq
        br_if 0 (;@2;)
        local.get 0
        local.get 6
        i32.sub
        local.get 8
        i32.add
        i32.const -1
        i32.add
        local.tee 0
        local.get 9
        i32.ge_u
        br_if 0 (;@2;)
        local.get 1
        i32.const 61
        i32.mul
        local.get 6
        i32.add
        i32.const 61
        i32.add
        local.set 2
        local.get 0
        local.set 0
        loop  ;; label = @3
          local.get 0
          local.tee 0
          i32.const 1
          i32.add
          local.get 0
          local.get 2
          call $memmove
          drop
          local.get 0
          i32.const 10
          i32.store8
          local.get 0
          i32.const 61
          i32.add
          local.tee 1
          local.set 0
          local.get 1
          local.get 9
          i32.lt_u
          br_if 0 (;@3;)
        end
      end
      block  ;; label = @2
        local.get 8
        local.get 9
        i32.const -1
        i32.add
        local.tee 0
        i32.gt_u
        br_if 0 (;@2;)
        local.get 0
        local.set 1
        local.get 8
        local.set 0
        loop  ;; label = @3
          local.get 0
          local.tee 0
          i32.load8_s
          i32.const 1072
          i32.add
          i32.load8_u
          local.set 2
          local.get 0
          local.get 1
          local.tee 1
          i32.load8_s
          i32.const 1072
          i32.add
          i32.load8_u
          i32.store8
          local.get 1
          local.get 2
          i32.store8
          local.get 1
          i32.const -1
          i32.add
          local.tee 2
          local.set 1
          local.get 0
          i32.const 1
          i32.add
          local.tee 6
          local.set 0
          local.get 6
          local.get 2
          i32.le_u
          br_if 0 (;@3;)
        end
      end
      local.get 3
      local.set 2
      local.get 3
      local.get 4
      i32.ge_u
      br_if 0 (;@1;)
    end
    i32.const 0
    i32.load
    call $fileno
    local.get 4
    local.get 10
    call $write
    drop
    local.get 4
    call $free
    i32.const 0)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 66736))
  (global (;1;) i32 (i32.const 1072))
  (global (;2;) i32 (i32.const 1060))
  (global (;3;) i32 (i32.const 1024))
  (global (;4;) i32 (i32.const 1200))
  (global (;5;) i32 (i32.const 1200))
  (global (;6;) i32 (i32.const 66736))
  (global (;7;) i32 (i32.const 1024))
  (global (;8;) i32 (i32.const 66736))
  (global (;9;) i32 (i32.const 131072))
  (global (;10;) i32 (i32.const 0))
  (global (;11;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "process" (func $process))
  (export "tbl" (global 1))
  (export "_start" (func $_start))
  (export "pairs" (global 2))
  (export "__dso_handle" (global 3))
  (export "__data_end" (global 4))
  (export "__stack_low" (global 5))
  (export "__stack_high" (global 6))
  (export "__global_base" (global 7))
  (export "__heap_base" (global 8))
  (export "__heap_end" (global 9))
  (export "__memory_base" (global 10))
  (export "__table_base" (global 11))
  (data $.rodata (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a\00")
  (data $.data (i32.const 1060) "\00\04\00\00"))
