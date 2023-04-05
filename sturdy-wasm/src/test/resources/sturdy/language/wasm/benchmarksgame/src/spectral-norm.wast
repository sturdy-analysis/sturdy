(module
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param i32) (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;0;) (type 0)))
  (func (;1;) (type 1)
    nop)
  (func (;2;) (type 1)
    (local i32 i32 i32 i32 i32 i32 f64)
    global.get 0
    i32.const 2400
    i32.sub
    local.tee 3
    global.set 0
    loop  ;; label = @1
      local.get 3
      i32.const 800
      i32.add
      local.get 1
      i32.const 3
      i32.shl
      i32.add
      local.tee 0
      i64.const 4607182418800017408
      i64.store
      local.get 0
      i64.const 4607182418800017408
      i64.store offset=8
      local.get 0
      i64.const 4607182418800017408
      i64.store offset=16
      local.get 0
      i64.const 4607182418800017408
      i64.store offset=24
      local.get 0
      i64.const 4607182418800017408
      i64.store offset=32
      local.get 1
      i32.const 5
      i32.add
      local.tee 1
      i32.const 100
      i32.ne
      br_if 0 (;@1;)
    end
    loop  ;; label = @1
      i32.const 0
      local.set 1
      loop  ;; label = @2
        local.get 1
        i32.const 1
        i32.add
        local.set 4
        i32.const 0
        local.set 0
        f64.const 0x0p+0 (;=0;)
        local.set 6
        loop  ;; label = @3
          local.get 6
          local.get 3
          i32.const 800
          i32.add
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.const 0x1p+0 (;=1;)
          local.get 0
          local.get 1
          i32.add
          local.tee 2
          i32.const 1
          i32.add
          local.get 2
          i32.mul
          i32.const 1
          i32.shr_u
          local.get 4
          i32.add
          f64.convert_i32_s
          f64.div
          f64.mul
          f64.add
          local.set 6
          local.get 0
          i32.const 1
          i32.add
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 3
        i32.const 1600
        i32.add
        local.get 1
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        i32.const 0
        local.set 2
        local.get 4
        local.tee 1
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      loop  ;; label = @2
        f64.const 0x0p+0 (;=0;)
        local.set 6
        i32.const 0
        local.set 0
        loop  ;; label = @3
          local.get 6
          local.get 3
          i32.const 1600
          i32.add
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.const 0x1p+0 (;=1;)
          local.get 0
          i32.const 1
          i32.add
          local.tee 1
          local.get 0
          local.get 2
          i32.add
          local.tee 0
          i32.const 1
          i32.add
          local.get 0
          i32.mul
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          f64.mul
          f64.add
          local.set 6
          local.get 1
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 3
        local.get 2
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        local.get 2
        i32.const 1
        i32.add
        local.tee 2
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      i32.const 0
      local.set 1
      loop  ;; label = @2
        local.get 1
        i32.const 1
        i32.add
        local.set 4
        f64.const 0x0p+0 (;=0;)
        local.set 6
        i32.const 0
        local.set 0
        loop  ;; label = @3
          local.get 6
          local.get 3
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.const 0x1p+0 (;=1;)
          local.get 0
          local.get 1
          i32.add
          local.tee 2
          i32.const 1
          i32.add
          local.get 2
          i32.mul
          i32.const 1
          i32.shr_u
          local.get 4
          i32.add
          f64.convert_i32_s
          f64.div
          f64.mul
          f64.add
          local.set 6
          local.get 0
          i32.const 1
          i32.add
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 3
        i32.const 1600
        i32.add
        local.get 1
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        i32.const 0
        local.set 2
        local.get 4
        local.tee 1
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      loop  ;; label = @2
        f64.const 0x0p+0 (;=0;)
        local.set 6
        i32.const 0
        local.set 0
        loop  ;; label = @3
          local.get 6
          local.get 3
          i32.const 1600
          i32.add
          local.get 0
          i32.const 3
          i32.shl
          i32.add
          f64.load
          f64.const 0x1p+0 (;=1;)
          local.get 0
          i32.const 1
          i32.add
          local.tee 1
          local.get 0
          local.get 2
          i32.add
          local.tee 0
          i32.const 1
          i32.add
          local.get 0
          i32.mul
          i32.const 1
          i32.shr_u
          i32.add
          f64.convert_i32_s
          f64.div
          f64.mul
          f64.add
          local.set 6
          local.get 1
          local.tee 0
          i32.const 100
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 3
        i32.const 800
        i32.add
        local.get 2
        i32.const 3
        i32.shl
        i32.add
        local.get 6
        f64.store
        local.get 2
        i32.const 1
        i32.add
        local.tee 2
        i32.const 100
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 5
      i32.const 1
      i32.add
      local.tee 5
      i32.const 10
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 3
    i32.const 2400
    i32.add
    global.set 0
    i32.const 0
    call 0
    unreachable)
  (func (;3;) (type 2) (result i32)
    global.get 0)
  (func (;4;) (type 0) (param i32)
    local.get 0
    global.set 0)
  (func (;5;) (type 3) (param i32) (result i32)
    global.get 0
    local.get 0
    i32.sub
    i32.const -16
    i32.and
    local.tee 0
    global.set 0
    local.get 0)
  (func (;6;) (type 2) (result i32)
    i32.const 1024)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 256 256)
  (global (;0;) (mut i32) (i32.const 5243920))
  (export "memory" (memory 0))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func 2))
  (export "__errno_location" (func 6))
  (export "stackSave" (func 3))
  (export "stackRestore" (func 4))
  (export "stackAlloc" (func 5))
  (elem (;0;) (i32.const 1) func 1))
