(module
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param i32 i32 i32)))
  (type (;4;) (func (param i32) (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;0;) (type 0)))
  (func (;1;) (type 1)
    nop)
  (func (;2;) (type 1)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    i32.const 1112
    i32.const 2
    i32.store
    i32.const 1104
    i64.const 4294967296
    i64.store
    i32.const 1168
    i32.const 3
    i32.store
    global.get 0
    i32.const -64
    i32.add
    local.tee 4
    global.set 0
    local.get 4
    i64.const 0
    i64.store offset=56
    local.get 4
    i64.const 0
    i64.store offset=48
    local.get 4
    i64.const 0
    i64.store offset=40
    local.get 4
    i64.const 0
    i64.store offset=32
    local.get 4
    i64.const 0
    i64.store offset=24
    local.get 4
    i64.const 0
    i64.store offset=16
    local.get 4
    i64.const 0
    i64.store offset=8
    local.get 4
    i64.const 0
    i64.store
    i32.const 1168
    i32.load
    local.tee 7
    i32.const 2
    i32.shl
    local.set 8
    loop  ;; label = @1
      i32.const 1104
      i32.load
      local.set 6
      local.get 3
      i32.const 1
      i32.ge_s
      if  ;; label = @2
        block  ;; label = @3
          i32.const 1108
          local.set 2
          i32.const 4
          local.get 3
          i32.const 2
          i32.shl
          local.tee 0
          i32.sub
          i32.const 0
          local.get 0
          i32.const 1
          i32.shl
          i32.sub
          i32.le_u
          if  ;; label = @4
            i32.const 1104
            i32.const 1108
            local.get 0
            call 3
            br 1 (;@3;)
          end
          i32.const 1104
          local.set 1
          local.get 0
          i32.const 3
          i32.gt_u
          if  ;; label = @4
            loop  ;; label = @5
              local.get 1
              local.get 2
              i32.load
              i32.store
              local.get 2
              i32.const 4
              i32.add
              local.set 2
              local.get 1
              i32.const 4
              i32.add
              local.set 1
              local.get 0
              i32.const 4
              i32.sub
              local.tee 0
              i32.const 3
              i32.gt_u
              br_if 0 (;@5;)
            end
          end
          local.get 0
          if  ;; label = @4
            loop  ;; label = @5
              local.get 1
              local.get 2
              i32.load8_u
              i32.store8
              local.get 1
              i32.const 1
              i32.add
              local.set 1
              local.get 2
              i32.const 1
              i32.add
              local.set 2
              local.get 0
              i32.const 1
              i32.sub
              local.tee 0
              br_if 0 (;@5;)
            end
          end
        end
      end
      local.get 3
      i32.const 2
      i32.shl
      local.tee 5
      i32.const 1104
      i32.add
      local.get 6
      i32.store
      block (result i32)  ;; label = @2
        local.get 3
        local.get 4
        local.get 5
        i32.add
        local.tee 0
        i32.load
        local.tee 5
        i32.le_s
        if  ;; label = @3
          local.get 0
          i32.const 0
          i32.store
          local.get 3
          i32.const 1
          i32.add
          br 1 (;@2;)
        end
        local.get 0
        local.get 5
        i32.const 1
        i32.add
        i32.store
        i32.const 1028
        i32.const 1028
        i32.load
        local.tee 9
        i32.const -1
        i32.xor
        i32.store
        i32.const 1
        i32.const 1104
        i32.load
        local.tee 5
        i32.eqz
        br_if 0 (;@2;)
        drop
        block  ;; label = @3
          local.get 5
          i32.const 2
          i32.shl
          i32.const 1104
          i32.add
          i32.load
          i32.eqz
          if  ;; label = @4
            i32.const 1
            local.set 1
            br 1 (;@3;)
          end
          local.get 7
          if  ;; label = @4
            i32.const 1040
            i32.const 1104
            local.get 8
            call 3
          end
          i32.const 1
          local.set 1
          i32.const 1040
          i32.load
          local.tee 0
          i32.const 0
          i32.le_s
          if  ;; label = @4
            i32.const 2
            local.set 1
            local.get 0
            i32.const 2
            i32.shl
            i32.const 1040
            i32.add
            i32.load
            i32.eqz
            br_if 1 (;@3;)
            loop  ;; label = @5
              br 0 (;@5;)
            end
            unreachable
          end
          loop  ;; label = @4
            block  ;; label = @5
              local.get 0
              i32.const 1
              i32.lt_s
              br_if 0 (;@5;)
              i32.const 1040
              local.get 0
              i32.const 2
              i32.shl
              local.tee 2
              i32.const 1040
              i32.add
              local.tee 6
              i32.load
              local.tee 5
              i32.store
              local.get 6
              local.get 0
              i32.store
              i32.const 1044
              local.set 3
              local.get 5
              local.set 0
              local.get 2
              i32.const 1036
              i32.add
              local.tee 2
              i32.const 1044
              i32.le_u
              br_if 0 (;@5;)
              loop  ;; label = @6
                local.get 3
                i32.load
                local.set 5
                local.get 3
                local.get 2
                i32.load
                i32.store
                local.get 2
                local.get 5
                i32.store
                local.get 3
                i32.const 4
                i32.add
                local.tee 3
                local.get 2
                i32.const 4
                i32.sub
                local.tee 2
                i32.lt_u
                br_if 0 (;@6;)
              end
              i32.const 1040
              i32.load
              local.set 0
            end
            local.get 1
            i32.const 1
            i32.add
            local.set 1
            local.get 0
            i32.const 2
            i32.shl
            i32.const 1040
            i32.add
            i32.load
            br_if 0 (;@4;)
          end
        end
        i32.const 1024
        i32.load
        local.get 1
        i32.lt_s
        if  ;; label = @3
          i32.const 1024
          local.get 1
          i32.store
        end
        i32.const 1032
        i32.const 1032
        i32.load
        local.get 1
        i32.const 0
        local.get 1
        i32.sub
        local.get 9
        i32.const -1
        i32.eq
        select
        i32.add
        i32.store
        i32.const 1
      end
      local.tee 3
      i32.const 3
      i32.lt_s
      br_if 0 (;@1;)
    end
    local.get 4
    i32.const -64
    i32.sub
    global.set 0
    i32.const 0
    call 0
    unreachable)
  (func (;3;) (type 3) (param i32 i32 i32)
    (local i32 i32)
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
            i32.const 1
            i32.lt_s
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
      block  ;; label = @2
        local.get 3
        i32.const -4
        i32.and
        local.tee 0
        i32.const 64
        i32.lt_u
        br_if 0 (;@2;)
        local.get 2
        local.get 0
        i32.const -64
        i32.add
        local.tee 4
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
          local.get 4
          i32.le_u
          br_if 0 (;@3;)
        end
      end
      local.get 0
      local.get 2
      i32.le_u
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
        local.get 0
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
    end)
  (func (;4;) (type 2) (result i32)
    global.get 0)
  (func (;5;) (type 0) (param i32)
    local.get 0
    global.set 0)
  (func (;6;) (type 4) (param i32) (result i32)
    global.get 0
    local.get 0
    i32.sub
    i32.const -16
    i32.and
    local.tee 0
    global.set 0
    local.get 0)
  (func (;7;) (type 2) (result i32)
    i32.const 1172)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 256 256)
  (global (;0;) (mut i32) (i32.const 5244064))
  (export "memory" (memory 0))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func 2))
  (export "__errno_location" (func 7))
  (export "stackSave" (func 4))
  (export "stackRestore" (func 5))
  (export "stackAlloc" (func 6))
  (elem (;0;) (i32.const 1) func 1))
