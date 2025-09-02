(module $spectral-norm.wasm
  (type (;0;) (func (param i32 i32) (result i32)))
  (type (;1;) (func))
  (type (;2;) (func (param i32 i32) (result f64)))
  (type (;3;) (func (param i32 i32 i32)))
  (type (;4;) (func (result i32)))
  (import "env" "printf" (func $printf (type 0)))
  (func $__wasm_call_ctors (type 1))
  (func $eval_A (type 2) (param i32 i32) (result f64)
    f64.const 0x1p+0 (;=1;)
    local.get 0
    local.get 1
    local.get 0
    i32.add
    local.tee 1
    i32.const 1
    i32.add
    local.get 1
    i32.mul
    i32.const 2
    i32.div_s
    i32.add
    i32.const 1
    i32.add
    f64.convert_i32_s
    f64.div)
  (func $eval_A_times_u (type 3) (param i32 i32 i32)
    (local i32 i32 i32 f64 i32 i32 i32)
    block  ;; label = @1
      local.get 0
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      i32.const 0
      local.set 3
      loop  ;; label = @2
        local.get 2
        local.get 3
        i32.const 3
        i32.shl
        i32.add
        local.tee 4
        i64.const 0
        i64.store
        local.get 3
        i32.const 1
        i32.add
        local.set 5
        f64.const 0x0p+0 (;=0;)
        local.set 6
        local.get 1
        local.set 7
        i32.const 0
        local.set 8
        loop  ;; label = @3
          local.get 4
          f64.const 0x1p+0 (;=1;)
          local.get 5
          local.get 3
          local.get 8
          i32.add
          local.tee 9
          i32.const 1
          i32.add
          local.get 9
          i32.mul
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 7
          f64.load
          f64.mul
          local.get 6
          f64.add
          local.tee 6
          f64.store
          local.get 7
          i32.const 8
          i32.add
          local.set 7
          local.get 0
          local.get 8
          i32.const 1
          i32.add
          local.tee 8
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 5
        local.set 3
        local.get 5
        local.get 0
        i32.ne
        br_if 0 (;@2;)
      end
    end)
  (func $eval_At_times_u (type 3) (param i32 i32 i32)
    (local i32 i32 i32 i32 f64 i32 i32 i32 i32)
    block  ;; label = @1
      local.get 0
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      i32.const 0
      local.set 3
      i32.const 2
      local.set 4
      i32.const 0
      local.set 5
      loop  ;; label = @2
        local.get 2
        local.get 5
        i32.const 3
        i32.shl
        i32.add
        local.tee 6
        i64.const 0
        i64.store
        f64.const 0x0p+0 (;=0;)
        local.set 7
        local.get 3
        local.set 8
        local.get 4
        local.set 9
        local.get 1
        local.set 10
        i32.const 0
        local.set 11
        loop  ;; label = @3
          local.get 6
          f64.const 0x1p+0 (;=1;)
          local.get 11
          i32.const 1
          i32.add
          local.tee 11
          local.get 8
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 10
          f64.load
          f64.mul
          local.get 7
          f64.add
          local.tee 7
          f64.store
          local.get 8
          local.get 9
          i32.add
          local.set 8
          local.get 10
          i32.const 8
          i32.add
          local.set 10
          local.get 9
          i32.const 2
          i32.add
          local.set 9
          local.get 0
          local.get 11
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 3
        local.get 4
        i32.add
        local.set 3
        local.get 4
        i32.const 2
        i32.add
        local.set 4
        local.get 5
        i32.const 1
        i32.add
        local.tee 5
        local.get 0
        i32.ne
        br_if 0 (;@2;)
      end
    end)
  (func $eval_AtA_times_u (type 3) (param i32 i32 i32)
    (local i32 i32 i32 i32 i32 f64 i32 i32 i32)
    global.get $__stack_pointer
    local.tee 3
    drop
    local.get 3
    local.get 0
    i32.const 3
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee 4
    drop
    block  ;; label = @1
      local.get 0
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      i32.const 0
      local.set 5
      loop  ;; label = @2
        local.get 5
        i32.const 1
        i32.add
        local.set 6
        local.get 4
        local.get 5
        i32.const 3
        i32.shl
        i32.add
        local.set 7
        f64.const 0x0p+0 (;=0;)
        local.set 8
        local.get 1
        local.set 3
        i32.const 0
        local.set 9
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 5
          local.get 9
          i32.add
          local.tee 10
          i32.const 1
          i32.add
          local.get 10
          i32.mul
          i32.const 1
          i32.shr_u
          local.get 6
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 3
          f64.load
          f64.mul
          local.get 8
          f64.add
          local.set 8
          local.get 3
          i32.const 8
          i32.add
          local.set 3
          local.get 0
          local.get 9
          i32.const 1
          i32.add
          local.tee 9
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 7
        local.get 8
        f64.store
        local.get 6
        local.set 5
        local.get 6
        local.get 0
        i32.ne
        br_if 0 (;@2;)
      end
      i32.const 0
      local.set 7
      i32.const 2
      local.set 6
      i32.const 0
      local.set 1
      loop  ;; label = @2
        local.get 2
        local.get 1
        i32.const 3
        i32.shl
        i32.add
        local.set 11
        f64.const 0x0p+0 (;=0;)
        local.set 8
        local.get 7
        local.set 3
        local.get 6
        local.set 9
        local.get 4
        local.set 10
        i32.const 0
        local.set 5
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 5
          i32.const 1
          i32.add
          local.tee 5
          local.get 3
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 10
          f64.load
          f64.mul
          local.get 8
          f64.add
          local.set 8
          local.get 3
          local.get 9
          i32.add
          local.set 3
          local.get 10
          i32.const 8
          i32.add
          local.set 10
          local.get 9
          i32.const 2
          i32.add
          local.set 9
          local.get 0
          local.get 5
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 11
        local.get 8
        f64.store
        local.get 7
        local.get 6
        i32.add
        local.set 7
        local.get 6
        i32.const 2
        i32.add
        local.set 6
        local.get 1
        i32.const 1
        i32.add
        local.tee 1
        local.get 0
        i32.ne
        br_if 0 (;@2;)
      end
    end)
  (func $_start (type 4) (result i32)
    (local i32 i32 i32 i32 i32 i32 f64 i32 i32 i32 f64 f64 f64)
    global.get $__stack_pointer
    i32.const 2416
    i32.sub
    local.tee 0
    global.set $__stack_pointer
    i32.const 0
    local.set 1
    loop  ;; label = @1
      local.get 0
      i32.const 816
      i32.add
      local.get 1
      i32.add
      local.tee 2
      i64.const 4607182418800017408
      i64.store
      local.get 2
      i32.const 32
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 2
      i32.const 24
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 2
      i32.const 16
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 2
      i32.const 8
      i32.add
      i64.const 4607182418800017408
      i64.store
      local.get 1
      i32.const 40
      i32.add
      local.tee 1
      i32.const 800
      i32.ne
      br_if 0 (;@1;)
    end
    i32.const 0
    local.set 3
    loop  ;; label = @1
      i32.const 0
      local.set 4
      loop  ;; label = @2
        local.get 4
        i32.const 1
        i32.add
        local.set 5
        i32.const 0
        local.set 2
        f64.const 0x0p+0 (;=0;)
        local.set 6
        local.get 0
        i32.const 816
        i32.add
        local.set 1
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 4
          local.get 2
          i32.add
          local.tee 7
          i32.const 1
          i32.add
          local.get 7
          i32.mul
          i32.const 1
          i32.shr_u
          local.get 5
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 1
          f64.load
          f64.mul
          local.get 6
          f64.add
          local.set 6
          local.get 1
          i32.const 8
          i32.add
          local.set 1
          local.get 2
          i32.const 1
          i32.add
          local.tee 2
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 0
        i32.const 1616
        i32.add
        local.get 4
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        local.get 5
        local.set 4
        local.get 5
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      i32.const 2
      local.set 5
      i32.const 0
      local.set 8
      i32.const 0
      local.set 9
      loop  ;; label = @2
        f64.const 0x0p+0 (;=0;)
        local.set 6
        i32.const 1
        local.set 2
        local.get 8
        local.set 1
        local.get 5
        local.set 7
        i32.const 0
        local.set 4
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 2
          local.get 1
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 0
          i32.const 1616
          i32.add
          local.get 4
          i32.add
          f64.load
          f64.mul
          local.get 6
          f64.add
          local.set 6
          local.get 1
          local.get 7
          i32.add
          local.set 1
          local.get 7
          i32.const 2
          i32.add
          local.set 7
          local.get 2
          i32.const 1
          i32.add
          local.set 2
          local.get 4
          i32.const 8
          i32.add
          local.tee 4
          i32.const 800
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 0
        i32.const 16
        i32.add
        local.get 9
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        local.get 8
        local.get 5
        i32.add
        local.set 8
        local.get 5
        i32.const 2
        i32.add
        local.set 5
        local.get 9
        i32.const 1
        i32.add
        local.tee 9
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      i32.const 0
      local.set 4
      loop  ;; label = @2
        local.get 4
        i32.const 1
        i32.add
        local.set 5
        f64.const 0x0p+0 (;=0;)
        local.set 6
        local.get 0
        i32.const 16
        i32.add
        local.set 2
        i32.const 0
        local.set 1
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 4
          local.get 1
          i32.add
          local.tee 7
          i32.const 1
          i32.add
          local.get 7
          i32.mul
          i32.const 1
          i32.shr_u
          local.get 5
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 2
          f64.load
          f64.mul
          local.get 6
          f64.add
          local.set 6
          local.get 2
          i32.const 8
          i32.add
          local.set 2
          local.get 1
          i32.const 1
          i32.add
          local.tee 1
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 0
        i32.const 1616
        i32.add
        local.get 4
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        local.get 5
        local.set 4
        local.get 5
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      i32.const 2
      local.set 5
      i32.const 0
      local.set 8
      i32.const 0
      local.set 9
      loop  ;; label = @2
        f64.const 0x0p+0 (;=0;)
        local.set 6
        i32.const 1
        local.set 2
        local.get 8
        local.set 1
        local.get 5
        local.set 7
        i32.const 0
        local.set 4
        loop  ;; label = @3
          f64.const 0x1p+0 (;=1;)
          local.get 2
          local.get 1
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          local.get 0
          i32.const 1616
          i32.add
          local.get 4
          i32.add
          f64.load
          f64.mul
          local.get 6
          f64.add
          local.set 6
          local.get 1
          local.get 7
          i32.add
          local.set 1
          local.get 7
          i32.const 2
          i32.add
          local.set 7
          local.get 2
          i32.const 1
          i32.add
          local.set 2
          local.get 4
          i32.const 8
          i32.add
          local.tee 4
          i32.const 800
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 0
        i32.const 816
        i32.add
        local.get 9
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        local.get 8
        local.get 5
        i32.add
        local.set 8
        local.get 5
        i32.const 2
        i32.add
        local.set 5
        local.get 9
        i32.const 1
        i32.add
        local.tee 9
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 3
      i32.const 1
      i32.add
      local.tee 3
      i32.const 10
      i32.ne
      br_if 0 (;@1;)
    end
    f64.const 0x0p+0 (;=0;)
    local.set 10
    i32.const 0
    local.set 2
    f64.const 0x0p+0 (;=0;)
    local.set 11
    loop  ;; label = @1
      local.get 0
      i32.const 16
      i32.add
      local.get 2
      i32.add
      local.tee 1
      i32.const 8
      i32.add
      f64.load
      local.tee 6
      local.get 6
      f64.mul
      local.get 1
      f64.load
      local.tee 12
      local.get 12
      f64.mul
      local.get 10
      f64.add
      f64.add
      local.set 10
      local.get 0
      i32.const 816
      i32.add
      local.get 2
      i32.add
      local.tee 1
      i32.const 8
      i32.add
      f64.load
      local.get 6
      f64.mul
      local.get 1
      f64.load
      local.get 12
      f64.mul
      local.get 11
      f64.add
      f64.add
      local.set 11
      local.get 2
      i32.const 16
      i32.add
      local.tee 2
      i32.const 800
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 0
    local.get 11
    local.get 10
    f64.div
    f64.sqrt
    f64.store
    i32.const 1024
    local.get 0
    call $printf
    drop
    local.get 0
    i32.const 2416
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (global (;1;) i32 (i32.const 1024))
  (global (;2;) i32 (i32.const 1031))
  (global (;3;) i32 (i32.const 1040))
  (global (;4;) i32 (i32.const 66576))
  (global (;5;) i32 (i32.const 1024))
  (global (;6;) i32 (i32.const 66576))
  (global (;7;) i32 (i32.const 131072))
  (global (;8;) i32 (i32.const 0))
  (global (;9;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "eval_A" (func $eval_A))
  (export "eval_A_times_u" (func $eval_A_times_u))
  (export "eval_At_times_u" (func $eval_At_times_u))
  (export "eval_AtA_times_u" (func $eval_AtA_times_u))
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
  (data $.rodata (i32.const 1024) "%0.9f\0a\00"))
