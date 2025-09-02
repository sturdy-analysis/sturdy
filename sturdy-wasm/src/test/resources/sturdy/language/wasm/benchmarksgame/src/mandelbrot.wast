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
    (local i32 f64 i32 i32 i32 i32 f64 i32 i32 i32 i32 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64)
    global.get $__stack_pointer
    i32.const 8208
    i32.sub
    local.tee 0
    global.set $__stack_pointer
    f64.const 0x0p+0 (;=0;)
    local.set 1
    i32.const 1
    local.set 2
    local.get 0
    i32.const 4112
    i32.add
    local.set 3
    local.get 0
    i32.const 16
    i32.add
    local.set 4
    i32.const 32768
    call $malloc
    local.set 5
    loop  ;; label = @1
      local.get 4
      local.get 1
      f64.const 0x1p-8 (;=0.00390625;)
      f64.mul
      local.tee 6
      f64.const -0x1p+0 (;=-1;)
      f64.add
      f64.store
      local.get 3
      local.get 6
      f64.const -0x1.8p+0 (;=-1.5;)
      f64.add
      f64.store
      local.get 3
      i32.const 8
      i32.add
      local.get 2
      f64.convert_i32_u
      f64.const 0x1p-8 (;=0.00390625;)
      f64.mul
      local.tee 6
      f64.const -0x1.8p+0 (;=-1.5;)
      f64.add
      f64.store
      local.get 4
      i32.const 8
      i32.add
      local.get 6
      f64.const -0x1p+0 (;=-1;)
      f64.add
      f64.store
      local.get 4
      i32.const 16
      i32.add
      local.set 4
      local.get 3
      i32.const 16
      i32.add
      local.set 3
      local.get 1
      f64.const 0x1p+1 (;=2;)
      f64.add
      local.set 1
      local.get 2
      i32.const 2
      i32.add
      local.tee 2
      i32.const 513
      i32.ne
      br_if 0 (;@1;)
    end
    i32.const 0
    local.set 7
    loop  ;; label = @1
      local.get 5
      local.get 7
      i32.const 6
      i32.shl
      i32.add
      local.set 8
      local.get 0
      i32.const 16
      i32.add
      local.get 7
      i32.const 3
      i32.shl
      i32.add
      f64.load
      local.set 1
      i32.const 0
      local.set 9
      i32.const 0
      local.set 10
      loop  ;; label = @2
        local.get 0
        i32.const 4112
        i32.add
        local.get 10
        i32.const 3
        i32.shl
        i32.add
        local.tee 3
        f64.load
        local.set 11
        local.get 0
        i32.const 4112
        i32.add
        local.get 9
        i32.const 6
        i32.shl
        i32.add
        local.tee 4
        f64.load offset=56
        local.set 6
        local.get 4
        f64.load offset=48
        local.set 12
        local.get 4
        f64.load offset=40
        local.set 13
        local.get 4
        f64.load offset=32
        local.set 14
        local.get 4
        f64.load offset=24
        local.set 15
        local.get 4
        f64.load offset=16
        local.set 16
        local.get 4
        f64.load offset=8
        local.set 17
        local.get 4
        f64.load
        local.set 18
        local.get 3
        i32.const 56
        i32.add
        f64.load
        local.set 19
        local.get 3
        i32.const 48
        i32.add
        f64.load
        local.set 20
        local.get 3
        i32.const 40
        i32.add
        f64.load
        local.set 21
        local.get 3
        i32.const 32
        i32.add
        f64.load
        local.set 22
        local.get 3
        i32.const 24
        i32.add
        f64.load
        local.set 23
        local.get 3
        i32.const 16
        i32.add
        f64.load
        local.set 24
        local.get 3
        i32.const 8
        i32.add
        f64.load
        local.set 25
        i32.const 255
        local.set 4
        i32.const -4
        local.set 2
        local.get 1
        local.set 26
        local.get 1
        local.set 27
        local.get 1
        local.set 28
        local.get 1
        local.set 29
        local.get 1
        local.set 30
        local.get 1
        local.set 31
        local.get 1
        local.set 32
        local.get 1
        local.set 33
        block  ;; label = @3
          loop  ;; label = @4
            local.get 2
            local.set 3
            i32.const -2
            i32.const -1
            local.get 6
            local.get 6
            f64.mul
            local.tee 34
            local.get 26
            local.get 26
            f64.mul
            local.tee 35
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            i32.const -3
            i32.const -1
            local.get 12
            local.get 12
            f64.mul
            local.tee 36
            local.get 27
            local.get 27
            f64.mul
            local.tee 37
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            i32.const -5
            i32.const -1
            local.get 13
            local.get 13
            f64.mul
            local.tee 38
            local.get 28
            local.get 28
            f64.mul
            local.tee 39
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            i32.const -9
            i32.const -1
            local.get 14
            local.get 14
            f64.mul
            local.tee 40
            local.get 29
            local.get 29
            f64.mul
            local.tee 41
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            i32.const -17
            i32.const -1
            local.get 15
            local.get 15
            f64.mul
            local.tee 42
            local.get 30
            local.get 30
            f64.mul
            local.tee 43
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            i32.const -33
            i32.const -1
            local.get 16
            local.get 16
            f64.mul
            local.tee 44
            local.get 31
            local.get 31
            f64.mul
            local.tee 45
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            i32.const -65
            i32.const -1
            local.get 17
            local.get 17
            f64.mul
            local.tee 46
            local.get 32
            local.get 32
            f64.mul
            local.tee 47
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            i32.const 127
            i32.const -1
            local.get 18
            local.get 18
            f64.mul
            local.tee 48
            local.get 33
            local.get 33
            f64.mul
            local.tee 49
            f64.add
            f64.const 0x1p+2 (;=4;)
            f64.gt
            select
            local.get 4
            i32.and
            i32.and
            i32.and
            i32.and
            i32.and
            i32.and
            i32.and
            i32.and
            local.tee 4
            i32.const 255
            i32.and
            i32.eqz
            br_if 1 (;@3;)
            local.get 3
            i32.const 1
            i32.add
            local.set 2
            local.get 6
            local.get 6
            f64.add
            local.get 26
            f64.mul
            local.get 1
            f64.add
            local.set 26
            local.get 19
            local.get 34
            local.get 35
            f64.sub
            f64.add
            local.set 6
            local.get 12
            local.get 12
            f64.add
            local.get 27
            f64.mul
            local.get 1
            f64.add
            local.set 27
            local.get 20
            local.get 36
            local.get 37
            f64.sub
            f64.add
            local.set 12
            local.get 13
            local.get 13
            f64.add
            local.get 28
            f64.mul
            local.get 1
            f64.add
            local.set 28
            local.get 21
            local.get 38
            local.get 39
            f64.sub
            f64.add
            local.set 13
            local.get 14
            local.get 14
            f64.add
            local.get 29
            f64.mul
            local.get 1
            f64.add
            local.set 29
            local.get 22
            local.get 40
            local.get 41
            f64.sub
            f64.add
            local.set 14
            local.get 15
            local.get 15
            f64.add
            local.get 30
            f64.mul
            local.get 1
            f64.add
            local.set 30
            local.get 23
            local.get 42
            local.get 43
            f64.sub
            f64.add
            local.set 15
            local.get 16
            local.get 16
            f64.add
            local.get 31
            f64.mul
            local.get 1
            f64.add
            local.set 31
            local.get 24
            local.get 44
            local.get 45
            f64.sub
            f64.add
            local.set 16
            local.get 17
            local.get 17
            f64.add
            local.get 32
            f64.mul
            local.get 1
            f64.add
            local.set 32
            local.get 25
            local.get 46
            local.get 47
            f64.sub
            f64.add
            local.set 17
            local.get 18
            local.get 18
            f64.add
            local.get 33
            f64.mul
            local.get 1
            f64.add
            local.set 33
            local.get 11
            local.get 48
            local.get 49
            f64.sub
            f64.add
            local.set 18
            local.get 3
            br_if 0 (;@4;)
          end
        end
        local.get 8
        local.get 10
        i32.const 3
        i32.shr_u
        i32.add
        local.get 4
        i32.store8
        local.get 10
        i32.const 8
        i32.add
        local.set 10
        local.get 9
        i32.const 1
        i32.add
        local.tee 9
        i32.const 64
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 7
      i32.const 1
      i32.add
      local.tee 7
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
    local.get 5
    i32.const 32768
    i32.const 1
    i32.const 0
    i32.load
    call $fwrite
    drop
    local.get 5
    call $free
    local.get 0
    i32.const 8208
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
