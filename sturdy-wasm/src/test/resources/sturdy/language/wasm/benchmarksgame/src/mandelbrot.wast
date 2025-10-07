(module $mandelbrot.wasm
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func (param i32 i32 i32) (result i32)))
  (type (;2;) (func (param i32 i32 i32 i32) (result i32)))
  (type (;3;) (func (param i32)))
  (type (;4;) (func))
  (type (;5;) (func (result i32)))
  (import "env" "malloc" (func $malloc (type 0)))
  (import "env" "fprintf" (func $fprintf (type 1)))
  (import "env" "fwrite" (func $fwrite (type 2)))
  (import "env" "free" (func $free (type 3)))
  (func $__wasm_call_ctors (type 4))
  (func $_start (type 5) (result i32)
    (local i32 i32 i32 f64 f64 i32 i32 i32 f64 i32 i32 i32 i32 i32 i32 f64 f64 i32)
    global.get $__stack_pointer
    i32.const 8336
    i32.sub
    local.tee 0
    global.set $__stack_pointer
    i32.const 32768
    call $malloc
    local.set 1
    i32.const 0
    local.set 2
    f64.const 0x0p+0 (;=0;)
    local.set 3
    loop  ;; label = @1
      local.get 0
      i32.const 16
      i32.add
      local.get 2
      local.tee 2
      i32.add
      local.get 3
      local.tee 3
      f64.const 0x1p-8 (;=0.00390625;)
      f64.mul
      local.tee 4
      f64.const -0x1p+0 (;=-1;)
      f64.add
      f64.store
      local.get 0
      i32.const 4112
      i32.add
      local.get 2
      i32.add
      local.get 4
      f64.const -0x1.8p+0 (;=-1.5;)
      f64.add
      f64.store
      local.get 2
      i32.const 8
      i32.add
      local.tee 5
      local.set 2
      local.get 3
      f64.const 0x1p+0 (;=1;)
      f64.add
      local.set 3
      local.get 5
      i32.const 4096
      i32.ne
      br_if 0 (;@1;)
    end
    i32.const 0
    local.set 2
    loop  ;; label = @1
      local.get 1
      local.get 2
      local.tee 6
      i32.const 6
      i32.shl
      i32.add
      local.set 7
      local.get 0
      i32.const 16
      i32.add
      local.get 6
      i32.const 3
      i32.shl
      i32.add
      f64.load
      local.set 8
      local.get 0
      i32.const 4112
      i32.add
      local.set 2
      i32.const 0
      local.set 5
      i32.const 0
      local.set 9
      loop  ;; label = @2
        local.get 9
        local.set 10
        local.get 2
        local.set 11
        local.get 0
        i32.const 8272
        i32.add
        i32.const 16
        i32.add
        local.get 0
        i32.const 4112
        i32.add
        local.get 5
        local.tee 12
        i32.const 6
        i32.shl
        i32.add
        local.tee 2
        i32.const 16
        i32.add
        i64.load
        i64.store
        local.get 0
        i32.const 8272
        i32.add
        i32.const 24
        i32.add
        local.get 2
        i32.const 24
        i32.add
        i64.load
        i64.store
        local.get 0
        i32.const 8272
        i32.add
        i32.const 32
        i32.add
        local.get 2
        i32.const 32
        i32.add
        i64.load
        i64.store
        local.get 0
        i32.const 8272
        i32.add
        i32.const 40
        i32.add
        local.get 2
        i32.const 40
        i32.add
        i64.load
        i64.store
        local.get 0
        i32.const 8272
        i32.add
        i32.const 48
        i32.add
        local.get 2
        i32.const 48
        i32.add
        i64.load
        i64.store
        local.get 0
        i32.const 8272
        i32.add
        i32.const 56
        i32.add
        local.get 2
        i32.const 56
        i32.add
        i64.load
        i64.store
        local.get 0
        local.get 2
        i64.load
        i64.store offset=8272
        local.get 0
        local.get 2
        i64.load offset=8
        i64.store offset=8280
        i32.const 0
        local.set 2
        loop  ;; label = @3
          local.get 0
          i32.const 8208
          i32.add
          local.get 2
          local.tee 2
          i32.add
          local.get 8
          f64.store
          local.get 2
          i32.const 8
          i32.add
          local.tee 5
          local.set 2
          local.get 5
          i32.const 64
          i32.ne
          br_if 0 (;@3;)
        end
        i32.const 255
        local.set 9
        i32.const 5
        local.set 2
        loop  ;; label = @3
          local.get 2
          local.set 13
          i32.const 0
          local.set 2
          i32.const 128
          local.set 5
          local.get 9
          local.set 9
          loop  ;; label = @4
            local.get 0
            i32.const 8272
            i32.add
            local.get 2
            local.tee 2
            i32.add
            local.tee 14
            local.get 11
            local.get 2
            i32.add
            f64.load
            local.get 14
            f64.load
            local.tee 3
            local.get 3
            f64.mul
            local.tee 15
            local.get 0
            i32.const 8208
            i32.add
            local.get 2
            i32.add
            local.tee 14
            f64.load
            local.tee 4
            local.get 4
            f64.mul
            local.tee 16
            f64.sub
            f64.add
            f64.store
            local.get 14
            local.get 4
            local.get 3
            local.get 3
            f64.add
            f64.mul
            local.get 8
            f64.add
            f64.store
            local.get 2
            i32.const 8
            i32.add
            local.tee 14
            local.set 2
            local.get 5
            local.tee 17
            i32.const 254
            i32.and
            i32.const 1
            i32.shr_u
            local.set 5
            local.get 17
            i32.const -1
            i32.xor
            i32.const -1
            local.get 15
            local.get 16
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            local.get 9
            i32.and
            local.tee 17
            local.set 9
            local.get 14
            i32.const 64
            i32.ne
            br_if 0 (;@4;)
          end
          block  ;; label = @4
            local.get 17
            i32.const 255
            i32.and
            i32.eqz
            br_if 0 (;@4;)
            local.get 17
            local.set 9
            local.get 13
            i32.const -1
            i32.add
            local.tee 5
            local.set 2
            local.get 5
            br_if 1 (;@3;)
          end
        end
        local.get 7
        local.get 10
        i32.const 3
        i32.shr_u
        i32.add
        local.get 17
        i32.store8
        local.get 11
        i32.const 64
        i32.add
        local.set 2
        local.get 12
        i32.const 1
        i32.add
        local.tee 14
        local.set 5
        local.get 10
        i32.const 8
        i32.add
        local.set 9
        local.get 14
        i32.const 64
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 6
      i32.const 1
      i32.add
      local.tee 5
      local.set 2
      local.get 5
      i32.const 512
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 0
    i64.const 512
    i64.store
    local.get 0
    i64.const 512
    i64.store offset=8
    i32.const 0
    i32.load
    i32.const 1024
    local.get 0
    call $fprintf
    drop
    local.get 1
    i32.const 32768
    i32.const 1
    i32.const 0
    i32.load
    call $fwrite
    drop
    local.get 1
    call $free
    local.get 0
    i32.const 8336
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (global (;1;) i32 (i32.const 1024))
  (global (;2;) i32 (i32.const 1036))
  (global (;3;) i32 (i32.const 1040))
  (global (;4;) i32 (i32.const 66576))
  (global (;5;) i32 (i32.const 1024))
  (global (;6;) i32 (i32.const 66576))
  (global (;7;) i32 (i32.const 131072))
  (global (;8;) i32 (i32.const 0))
  (global (;9;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "_start" (func $_start))
  (export "__dso_handle" (global 1))
  (export "__data_end" (global 2))
  (export "__stack_low" (global 3))
  (export "__stack_high" (global 4))
  (export "__global_base" (global 5))
  (export "__heap_base" (global 6))
  (export "__heap_end" (global 7))
  (export "__memory_base" (global 8))
  (export "__table_base" (global 9))
  (data $.rodata (i32.const 1024) "P4\0a%jd %jd\0a\00"))
