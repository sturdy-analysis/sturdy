(module
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param i32) (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;0;) (type 0)))
  (func (;1;) (type 1)
    nop)
  (func (;2;) (type 1)
    (local i32 i32 i32 i32 i32 i32 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64)
    global.get 0
    i32.const -8192
    i32.add
    local.tee 0
    global.set 0
    loop  ;; label = @1
      local.get 0
      local.get 1
      i32.const 3
      i32.shl
      local.tee 2
      i32.add
      local.get 1
      f64.convert_i32_s
      f64.const 0x1p-8 (;=0.00390625;)
      f64.mul
      local.tee 6
      f64.const -0x1p+0 (;=-1;)
      f64.add
      f64.store
      local.get 0
      i32.const 4096
      i32.add
      local.get 2
      i32.add
      local.get 6
      f64.const -0x1.8p+0 (;=-1.5;)
      f64.add
      f64.store
      local.get 0
      local.get 1
      i32.const 1
      i32.or
      local.tee 2
      i32.const 3
      i32.shl
      local.tee 3
      i32.add
      local.get 2
      f64.convert_i32_s
      f64.const 0x1p-8 (;=0.00390625;)
      f64.mul
      local.tee 6
      f64.const -0x1p+0 (;=-1;)
      f64.add
      f64.store
      local.get 0
      i32.const 4096
      i32.add
      local.get 3
      i32.add
      local.get 6
      f64.const -0x1.8p+0 (;=-1.5;)
      f64.add
      f64.store
      local.get 1
      i32.const 2
      i32.add
      local.tee 1
      i32.const 512
      i32.ne
      br_if 0 (;@1;)
    end
    loop  ;; label = @1
      local.get 0
      local.get 5
      i32.const 3
      i32.shl
      i32.add
      f64.load
      local.set 6
      i32.const 0
      local.set 3
      i32.const 0
      local.set 4
      loop  ;; label = @2
        local.get 4
        i32.const 3
        i32.shl
        local.tee 1
        local.get 0
        i32.const 4096
        i32.add
        i32.add
        f64.load
        local.set 23
        local.get 0
        i32.const 4096
        i32.add
        local.get 3
        i32.const 6
        i32.shl
        i32.add
        local.tee 2
        f64.load offset=56
        local.set 7
        local.get 2
        f64.load offset=48
        local.set 8
        local.get 2
        f64.load offset=40
        local.set 9
        local.get 2
        f64.load offset=32
        local.set 10
        local.get 2
        f64.load offset=24
        local.set 11
        local.get 2
        f64.load offset=16
        local.set 12
        local.get 2
        f64.load offset=8
        local.set 13
        local.get 2
        f64.load
        local.set 14
        local.get 0
        i32.const 4096
        i32.add
        local.get 1
        i32.const 56
        i32.or
        i32.add
        f64.load
        local.set 24
        local.get 0
        i32.const 4096
        i32.add
        local.get 1
        i32.const 48
        i32.or
        i32.add
        f64.load
        local.set 25
        local.get 0
        i32.const 4096
        i32.add
        local.get 1
        i32.const 40
        i32.or
        i32.add
        f64.load
        local.set 26
        local.get 0
        i32.const 4096
        i32.add
        local.get 1
        i32.const 32
        i32.or
        i32.add
        f64.load
        local.set 27
        local.get 0
        i32.const 4096
        i32.add
        local.get 1
        i32.const 24
        i32.or
        i32.add
        f64.load
        local.set 28
        local.get 0
        i32.const 4096
        i32.add
        local.get 1
        i32.const 16
        i32.or
        i32.add
        f64.load
        local.set 29
        local.get 0
        i32.const 4096
        i32.add
        local.get 1
        i32.const 8
        i32.or
        i32.add
        f64.load
        local.set 30
        i32.const 5
        local.set 2
        i32.const 255
        local.set 1
        local.get 6
        local.tee 15
        local.tee 16
        local.tee 17
        local.tee 18
        local.tee 19
        local.tee 20
        local.tee 21
        local.set 22
        loop  ;; label = @3
          i32.const 127
          i32.const -1
          local.get 14
          local.get 14
          f64.mul
          local.tee 31
          local.get 22
          local.get 22
          f64.mul
          local.tee 32
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          local.get 1
          i32.and
          i32.const -65
          i32.const -1
          local.get 13
          local.get 13
          f64.mul
          local.tee 33
          local.get 21
          local.get 21
          f64.mul
          local.tee 34
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -33
          i32.const -1
          local.get 12
          local.get 12
          f64.mul
          local.tee 35
          local.get 20
          local.get 20
          f64.mul
          local.tee 36
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -17
          i32.const -1
          local.get 11
          local.get 11
          f64.mul
          local.tee 37
          local.get 19
          local.get 19
          f64.mul
          local.tee 38
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -9
          i32.const -1
          local.get 10
          local.get 10
          f64.mul
          local.tee 39
          local.get 18
          local.get 18
          f64.mul
          local.tee 40
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -5
          i32.const -1
          local.get 9
          local.get 9
          f64.mul
          local.tee 41
          local.get 17
          local.get 17
          f64.mul
          local.tee 42
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -3
          i32.const -1
          local.get 8
          local.get 8
          f64.mul
          local.tee 43
          local.get 16
          local.get 16
          f64.mul
          local.tee 44
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          i32.const -2
          i32.const -1
          local.get 7
          local.get 7
          f64.mul
          local.tee 45
          local.get 15
          local.get 15
          f64.mul
          local.tee 46
          f64.add
          f64.const 0x1p+2 (;=4;)
          f64.gt
          select
          i32.and
          local.tee 1
          i32.const 255
          i32.and
          if  ;; label = @4
            local.get 6
            local.get 7
            local.get 7
            f64.add
            local.get 15
            f64.mul
            f64.add
            local.set 15
            local.get 24
            local.get 45
            local.get 46
            f64.sub
            f64.add
            local.set 7
            local.get 6
            local.get 8
            local.get 8
            f64.add
            local.get 16
            f64.mul
            f64.add
            local.set 16
            local.get 25
            local.get 43
            local.get 44
            f64.sub
            f64.add
            local.set 8
            local.get 6
            local.get 9
            local.get 9
            f64.add
            local.get 17
            f64.mul
            f64.add
            local.set 17
            local.get 26
            local.get 41
            local.get 42
            f64.sub
            f64.add
            local.set 9
            local.get 6
            local.get 10
            local.get 10
            f64.add
            local.get 18
            f64.mul
            f64.add
            local.set 18
            local.get 27
            local.get 39
            local.get 40
            f64.sub
            f64.add
            local.set 10
            local.get 6
            local.get 11
            local.get 11
            f64.add
            local.get 19
            f64.mul
            f64.add
            local.set 19
            local.get 28
            local.get 37
            local.get 38
            f64.sub
            f64.add
            local.set 11
            local.get 6
            local.get 12
            local.get 12
            f64.add
            local.get 20
            f64.mul
            f64.add
            local.set 20
            local.get 29
            local.get 35
            local.get 36
            f64.sub
            f64.add
            local.set 12
            local.get 6
            local.get 13
            local.get 13
            f64.add
            local.get 21
            f64.mul
            f64.add
            local.set 21
            local.get 30
            local.get 33
            local.get 34
            f64.sub
            f64.add
            local.set 13
            local.get 6
            local.get 14
            local.get 14
            f64.add
            local.get 22
            f64.mul
            f64.add
            local.set 22
            local.get 23
            local.get 31
            local.get 32
            f64.sub
            f64.add
            local.set 14
            local.get 2
            i32.const 1
            i32.sub
            local.tee 2
            br_if 1 (;@3;)
          end
        end
        local.get 4
        i32.const 8
        i32.add
        local.set 4
        local.get 3
        i32.const 1
        i32.add
        local.tee 3
        i32.const 64
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 5
      i32.const 1
      i32.add
      local.tee 5
      i32.const 512
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 0
    i32.const -8192
    i32.sub
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
