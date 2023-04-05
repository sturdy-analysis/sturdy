(module
  (type (;0;) (func (param i32 i32 i32) (result i32)))
  (type (;1;) (func (param i32)))
  (type (;2;) (func))
  (type (;3;) (func (param i32) (result i32)))
  (type (;4;) (func (param i32 i64 i32) (result i64)))
  (type (;5;) (func (result i32)))
  (type (;6;) (func (param i32 i32 i32 i32) (result i32)))
  (type (;7;) (func (param i32 i32 i32)))
  (import "wasi_snapshot_preview1" "fd_write" (func (;0;) (type 6)))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;1;) (type 1)))
  (func (;2;) (type 2)
    nop)
  (func (;3;) (type 7) (param i32 i32 i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 f32 f32)
    global.get 0
    i32.const 308224
    i32.sub
    local.tee 3
    local.set 8
    local.get 3
    global.set 0
    local.get 3
    local.get 1
    i32.const 2
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee 10
    global.set 0
    block  ;; label = @1
      local.get 1
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      local.get 1
      i32.const 1
      i32.and
      local.set 6
      block  ;; label = @2
        local.get 1
        i32.const 1
        i32.eq
        if  ;; label = @3
          i32.const 0
          local.set 3
          br 1 (;@2;)
        end
        local.get 1
        i32.const -2
        i32.and
        local.set 4
        i32.const 0
        local.set 3
        loop  ;; label = @3
          local.get 10
          local.get 3
          i32.const 2
          i32.shl
          i32.add
          block (result i32)  ;; label = @4
            local.get 18
            local.get 0
            local.get 3
            i32.const 3
            i32.shl
            i32.add
            f32.load offset=4
            f32.add
            local.tee 19
            f32.const 0x1.116p+17 (;=139968;)
            f32.mul
            local.tee 18
            f32.const 0x1p+32 (;=4.29497e+09;)
            f32.lt
            local.get 18
            f32.const 0x0p+0 (;=0;)
            f32.ge
            i32.and
            if  ;; label = @5
              local.get 18
              i32.trunc_f32_u
              br 1 (;@4;)
            end
            i32.const 0
          end
          i32.const 1
          i32.add
          i32.store
          block (result i32)  ;; label = @4
            local.get 19
            local.get 0
            local.get 3
            i32.const 1
            i32.or
            local.tee 7
            i32.const 3
            i32.shl
            i32.add
            f32.load offset=4
            f32.add
            local.tee 18
            f32.const 0x1.116p+17 (;=139968;)
            f32.mul
            local.tee 19
            f32.const 0x1p+32 (;=4.29497e+09;)
            f32.lt
            local.get 19
            f32.const 0x0p+0 (;=0;)
            f32.ge
            i32.and
            if  ;; label = @5
              local.get 19
              i32.trunc_f32_u
              br 1 (;@4;)
            end
            i32.const 0
          end
          local.set 9
          local.get 10
          local.get 7
          i32.const 2
          i32.shl
          i32.add
          local.get 9
          i32.const 1
          i32.add
          i32.store
          local.get 3
          i32.const 2
          i32.add
          local.set 3
          local.get 4
          i32.const 2
          i32.sub
          local.tee 4
          br_if 0 (;@3;)
        end
      end
      local.get 6
      i32.eqz
      br_if 0 (;@1;)
      local.get 10
      local.get 3
      i32.const 2
      i32.shl
      i32.add
      block (result i32)  ;; label = @2
        local.get 18
        local.get 0
        local.get 3
        i32.const 3
        i32.shl
        i32.add
        f32.load offset=4
        f32.add
        f32.const 0x1.116p+17 (;=139968;)
        f32.mul
        local.tee 18
        f32.const 0x1p+32 (;=4.29497e+09;)
        f32.lt
        local.get 18
        f32.const 0x0p+0 (;=0;)
        f32.ge
        i32.and
        if  ;; label = @3
          local.get 18
          i32.trunc_f32_u
          br 1 (;@2;)
        end
        i32.const 0
      end
      i32.const 1
      i32.add
      i32.store
    end
    i32.const 1728
    local.get 2
    i32.store
    i32.const 1736
    i32.const 0
    i32.store
    i32.const 1732
    i32.const 0
    i32.store
    local.get 1
    i32.const -4
    i32.and
    local.set 13
    local.get 1
    i32.const 3
    i32.and
    local.set 14
    local.get 8
    i32.const 4
    i32.or
    local.set 15
    i32.const 1552
    i32.load
    local.set 16
    local.get 1
    i32.const 1
    i32.sub
    i32.const 3
    i32.lt_u
    local.set 17
    loop  ;; label = @1
      i32.const 1564
      i32.load
      i32.const 1
      i32.gt_s
      local.set 2
      i32.const 0
      local.set 3
      block  ;; label = @2
        block  ;; label = @3
          block  ;; label = @4
            block  ;; label = @5
              loop  ;; label = @6
                i32.const -1
                local.set 6
                local.get 3
                i32.eqz
                if  ;; label = @7
                  i32.const 1732
                  local.get 2
                  i32.store
                  i32.const 1728
                  i32.const 1728
                  i32.load
                  local.tee 3
                  local.get 3
                  i32.const 61440
                  local.get 3
                  i32.const 61440
                  i32.lt_s
                  select
                  local.tee 6
                  i32.sub
                  i32.store
                  local.get 6
                  i32.eqz
                  br_if 2 (;@5;)
                  i32.const 1560
                  i32.load
                  local.set 4
                  local.get 8
                  local.set 3
                  local.get 6
                  i32.const 1
                  i32.and
                  if (result i32)  ;; label = @8
                    local.get 8
                    local.get 4
                    i32.const 3877
                    i32.mul
                    i32.const 29573
                    i32.add
                    i32.const 139968
                    i32.rem_u
                    local.tee 4
                    i32.store
                    local.get 15
                    local.set 3
                    local.get 6
                    i32.const 1
                    i32.sub
                  else
                    local.get 6
                  end
                  local.set 7
                  local.get 6
                  i32.const 1
                  i32.ne
                  if  ;; label = @8
                    loop  ;; label = @9
                      local.get 3
                      local.get 4
                      i32.const 3877
                      i32.mul
                      i32.const 29573
                      i32.add
                      i32.const 139968
                      i32.rem_u
                      local.tee 9
                      i32.store
                      local.get 3
                      local.get 9
                      i32.const 3877
                      i32.mul
                      i32.const 29573
                      i32.add
                      i32.const 139968
                      i32.rem_u
                      local.tee 4
                      i32.store offset=4
                      local.get 3
                      i32.const 8
                      i32.add
                      local.set 3
                      local.get 7
                      i32.const 2
                      i32.sub
                      local.tee 7
                      br_if 0 (;@9;)
                    end
                  end
                  i32.const 1560
                  local.get 4
                  i32.store
                  local.get 2
                  local.set 3
                end
                local.get 6
                i32.const -1
                i32.eq
                br_if 0 (;@6;)
              end
              local.get 6
              i32.eqz
              br_if 0 (;@5;)
              local.get 6
              i32.const 1
              i32.lt_s
              if  ;; label = @6
                local.get 8
                i32.const 245760
                i32.add
                local.set 5
                br 4 (;@2;)
              end
              i32.const 0
              local.set 12
              local.get 8
              i32.const 245760
              i32.add
              local.set 5
              i32.const 0
              local.set 4
              block  ;; label = @6
                local.get 1
                i32.const 0
                i32.le_s
                if  ;; label = @7
                  local.get 6
                  i32.const 1
                  i32.and
                  local.set 2
                  local.get 6
                  i32.const 1
                  i32.ne
                  br_if 1 (;@6;)
                  local.get 8
                  i32.const 245760
                  i32.add
                  local.set 5
                  br 3 (;@4;)
                end
                loop  ;; label = @7
                  local.get 8
                  local.get 12
                  i32.const 2
                  i32.shl
                  i32.add
                  i32.load
                  local.set 11
                  i32.const 0
                  local.set 7
                  i32.const 0
                  local.set 3
                  local.get 13
                  local.set 2
                  local.get 17
                  i32.eqz
                  if  ;; label = @8
                    loop  ;; label = @9
                      local.get 7
                      local.get 10
                      local.get 3
                      i32.const 2
                      i32.shl
                      local.tee 9
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.get 10
                      local.get 9
                      i32.const 4
                      i32.or
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.get 10
                      local.get 9
                      i32.const 8
                      i32.or
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.get 10
                      local.get 9
                      i32.const 12
                      i32.or
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.set 7
                      local.get 3
                      i32.const 4
                      i32.add
                      local.set 3
                      local.get 2
                      i32.const 4
                      i32.sub
                      local.tee 2
                      br_if 0 (;@9;)
                    end
                  end
                  local.get 14
                  local.tee 9
                  if  ;; label = @8
                    loop  ;; label = @9
                      local.get 7
                      local.get 10
                      local.get 3
                      i32.const 2
                      i32.shl
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.set 7
                      local.get 3
                      i32.const 1
                      i32.add
                      local.set 3
                      local.get 9
                      i32.const 1
                      i32.sub
                      local.tee 9
                      br_if 0 (;@9;)
                    end
                  end
                  local.get 5
                  local.get 0
                  local.get 7
                  i32.const 3
                  i32.shl
                  i32.add
                  i32.load8_u
                  i32.store8
                  block (result i32)  ;; label = @8
                    local.get 4
                    i32.const 58
                    i32.gt_s
                    if  ;; label = @9
                      local.get 5
                      i32.const 10
                      i32.store8 offset=1
                      i32.const 0
                      local.set 4
                      local.get 5
                      i32.const 2
                      i32.add
                      br 1 (;@8;)
                    end
                    local.get 4
                    i32.const 1
                    i32.add
                    local.set 4
                    local.get 5
                    i32.const 1
                    i32.add
                  end
                  local.set 5
                  local.get 6
                  local.get 12
                  i32.const 1
                  i32.add
                  local.tee 12
                  i32.ne
                  br_if 0 (;@7;)
                end
                br 3 (;@3;)
              end
              local.get 6
              i32.const -2
              i32.and
              local.set 7
              local.get 8
              i32.const 245760
              i32.add
              local.set 5
              loop  ;; label = @6
                local.get 5
                local.get 0
                i32.load8_u
                i32.store8
                block (result i32)  ;; label = @7
                  local.get 4
                  i32.const 59
                  i32.lt_s
                  if  ;; label = @8
                    local.get 4
                    i32.const 1
                    i32.add
                    local.set 4
                    local.get 5
                    i32.const 1
                    i32.add
                    br 1 (;@7;)
                  end
                  local.get 5
                  i32.const 10
                  i32.store8 offset=1
                  i32.const 0
                  local.set 4
                  local.get 5
                  i32.const 2
                  i32.add
                end
                local.tee 3
                local.get 0
                i32.load8_u
                i32.store8
                block (result i32)  ;; label = @7
                  local.get 4
                  i32.const 58
                  i32.le_s
                  if  ;; label = @8
                    local.get 4
                    i32.const 1
                    i32.add
                    local.set 4
                    local.get 3
                    i32.const 1
                    i32.add
                    br 1 (;@7;)
                  end
                  local.get 3
                  i32.const 10
                  i32.store8 offset=1
                  i32.const 0
                  local.set 4
                  local.get 3
                  i32.const 2
                  i32.add
                end
                local.set 5
                local.get 7
                i32.const 2
                i32.sub
                local.tee 7
                br_if 0 (;@6;)
              end
              br 1 (;@4;)
            end
            local.get 8
            i32.const 308224
            i32.add
            global.set 0
            return
          end
          local.get 2
          i32.eqz
          br_if 0 (;@3;)
          local.get 5
          local.get 0
          i32.load8_u
          i32.store8
          local.get 4
          i32.const 59
          i32.lt_s
          if  ;; label = @4
            local.get 4
            i32.const 1
            i32.add
            local.set 4
            local.get 5
            i32.const 1
            i32.add
            local.set 5
            br 1 (;@3;)
          end
          local.get 5
          i32.const 10
          i32.store8 offset=1
          local.get 5
          i32.const 2
          i32.add
          local.set 5
          i32.const 0
          local.set 4
        end
        local.get 4
        i32.eqz
        br_if 0 (;@2;)
        local.get 5
        i32.const 10
        i32.store8
        local.get 5
        i32.const 1
        i32.add
        local.set 5
      end
      i32.const 0
      local.set 3
      i32.const 1736
      i32.load
      i32.eqz
      if  ;; label = @2
        local.get 5
        local.get 8
        i32.const 245760
        i32.add
        i32.sub
        local.set 2
        loop  ;; label = @3
          block  ;; label = @4
            local.get 3
            br_if 0 (;@4;)
            i32.const 1736
            i32.const 1568
            i32.load
            i32.const 1
            i32.gt_s
            i32.store
            local.get 8
            i32.const 245760
            i32.add
            local.get 2
            local.get 16
            call 13
            local.tee 3
            i32.const -1
            i32.eq
            br_if 0 (;@4;)
            block  ;; label = @5
              local.get 3
              if  ;; label = @6
                i32.const 1732
                i32.load
                br_if 1 (;@5;)
                br 5 (;@1;)
              end
              i32.const 1
              call 10
              unreachable
            end
            loop  ;; label = @5
              br 0 (;@5;)
            end
            unreachable
          end
          i32.const 1736
          i32.load
          local.set 3
          br 0 (;@3;)
        end
        unreachable
      end
    end
    loop  ;; label = @1
      br 0 (;@1;)
    end
    unreachable)
  (func (;4;) (type 2)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    block (result i32)  ;; label = @1
      global.get 0
      i32.const 448
      i32.sub
      local.tee 4
      global.set 0
      i32.const 1055
      i32.const 22
      i32.const 1552
      i32.load
      local.tee 9
      call 13
      drop
      local.get 4
      i32.const 160
      i32.add
      i32.const 1104
      i32.const 288
      call 9
      drop
      global.get 0
      i32.const -64
      i32.add
      local.tee 0
      local.set 7
      local.get 0
      global.set 0
      local.get 0
      block (result i32)  ;; label = @2
        local.get 4
        i32.const 160
        i32.add
        local.tee 6
        local.tee 1
        local.set 0
        block  ;; label = @3
          local.get 1
          i32.const 3
          i32.and
          if  ;; label = @4
            loop  ;; label = @5
              local.get 0
              i32.load8_u
              i32.eqz
              br_if 2 (;@3;)
              local.get 0
              i32.const 1
              i32.add
              local.tee 0
              i32.const 3
              i32.and
              br_if 0 (;@5;)
            end
          end
          loop  ;; label = @4
            local.get 0
            local.tee 3
            i32.const 4
            i32.add
            local.set 0
            local.get 3
            i32.load
            local.tee 5
            i32.const -1
            i32.xor
            local.get 5
            i32.const 16843009
            i32.sub
            i32.and
            i32.const -2139062144
            i32.and
            i32.eqz
            br_if 0 (;@4;)
          end
          local.get 3
          local.get 1
          i32.sub
          local.get 5
          i32.const 255
          i32.and
          i32.eqz
          br_if 1 (;@2;)
          drop
          loop  ;; label = @4
            local.get 3
            i32.load8_u offset=1
            local.set 5
            local.get 3
            i32.const 1
            i32.add
            local.tee 0
            local.set 3
            local.get 5
            br_if 0 (;@4;)
          end
        end
        local.get 0
        local.get 1
        i32.sub
      end
      local.tee 3
      i32.const 75
      i32.add
      i32.const -16
      i32.and
      i32.sub
      local.tee 5
      global.set 0
      block  ;; label = @2
        local.get 3
        i32.const -59
        i32.lt_s
        br_if 0 (;@2;)
        local.get 3
        i32.const 60
        i32.add
        local.tee 0
        i32.const 1
        local.get 0
        i32.const 1
        i32.gt_s
        select
        local.tee 1
        i32.const 1
        i32.and
        local.set 8
        local.get 0
        i32.const 2
        i32.ge_s
        if  ;; label = @3
          local.get 1
          i32.const 2147483646
          i32.and
          local.set 1
          loop  ;; label = @4
            local.get 2
            local.get 5
            i32.add
            local.get 6
            local.get 2
            local.get 3
            i32.rem_s
            i32.add
            i32.load8_u
            i32.store8
            local.get 5
            local.get 2
            i32.const 1
            i32.or
            local.tee 0
            i32.add
            local.get 6
            local.get 0
            local.get 3
            i32.rem_s
            i32.add
            i32.load8_u
            i32.store8
            local.get 2
            i32.const 2
            i32.add
            local.set 2
            local.get 1
            i32.const 2
            i32.sub
            local.tee 1
            br_if 0 (;@4;)
          end
        end
        local.get 8
        i32.eqz
        br_if 0 (;@2;)
        local.get 2
        local.get 5
        i32.add
        local.get 6
        local.get 2
        local.get 3
        i32.rem_s
        i32.add
        i32.load8_u
        i32.store8
      end
      local.get 7
      i32.const 10
      i32.store8 offset=60
      i32.const 1552
      i32.load
      local.set 6
      i32.const 5000
      local.set 0
      i32.const 0
      local.set 1
      loop  ;; label = @2
        i32.const 60
        local.set 2
        local.get 7
        local.get 1
        local.get 5
        i32.add
        block (result i32)  ;; label = @3
          local.get 0
          i32.const 59
          i32.le_s
          if  ;; label = @4
            local.get 0
            local.get 7
            i32.add
            i32.const 10
            i32.store8
            local.get 0
            local.set 2
          end
          local.get 2
        end
        call 9
        local.tee 8
        local.get 2
        i32.const 1
        i32.add
        local.get 6
        call 13
        drop
        local.get 1
        local.get 2
        i32.add
        local.tee 1
        local.get 3
        i32.const 0
        local.get 1
        local.get 3
        i32.gt_s
        select
        i32.sub
        local.set 1
        local.get 0
        local.get 2
        i32.sub
        local.tee 0
        i32.const 0
        i32.gt_s
        br_if 0 (;@2;)
      end
      local.get 8
      i32.const -64
      i32.sub
      global.set 0
      i32.const 1732
      i32.const 0
      i32.store
      i32.const 1078
      i32.const 25
      local.get 9
      call 13
      drop
      local.get 4
      i32.const 32
      i32.add
      i32.const 1392
      i32.const 120
      call 9
      drop
      local.get 4
      i32.const 32
      i32.add
      i32.const 15
      i32.const 7500
      call 3
      i32.const 1024
      i32.const 30
      local.get 9
      call 13
      drop
      local.get 4
      i32.const 1544
      i64.load
      i64.store offset=24
      local.get 4
      i32.const 1536
      i64.load
      i64.store offset=16
      local.get 4
      i32.const 1528
      i64.load
      i64.store offset=8
      local.get 4
      i32.const 1520
      i64.load
      i64.store
      local.get 4
      i32.const 4
      i32.const 12500
      call 3
      local.get 4
      i32.const 448
      i32.add
      global.set 0
      i32.const 0
    end
    call 10
    unreachable)
  (func (;5;) (type 3) (param i32) (result i32)
    i32.const 0)
  (func (;6;) (type 4) (param i32 i64 i32) (result i64)
    i64.const 0)
  (func (;7;) (type 0) (param i32 i32 i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    global.get 0
    i32.const 32
    i32.sub
    local.tee 3
    global.set 0
    local.get 3
    local.get 0
    i32.load offset=28
    local.tee 5
    i32.store offset=16
    local.get 0
    i32.load offset=20
    local.set 4
    local.get 3
    local.get 2
    i32.store offset=28
    local.get 3
    local.get 1
    i32.store offset=24
    local.get 3
    local.get 4
    local.get 5
    i32.sub
    local.tee 1
    i32.store offset=20
    local.get 1
    local.get 2
    i32.add
    local.set 5
    i32.const 2
    local.set 7
    local.get 3
    i32.const 16
    i32.add
    local.set 1
    block (result i32)  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          local.get 0
          i32.load offset=60
          local.get 3
          i32.const 16
          i32.add
          i32.const 2
          local.get 3
          i32.const 12
          i32.add
          call 0
          local.tee 4
          if (result i32)  ;; label = @4
            i32.const 2776
            local.get 4
            i32.store
            i32.const -1
          else
            i32.const 0
          end
          i32.eqz
          if  ;; label = @4
            loop  ;; label = @5
              local.get 5
              local.get 3
              i32.load offset=12
              local.tee 4
              i32.eq
              br_if 2 (;@3;)
              local.get 4
              i32.const -1
              i32.le_s
              br_if 3 (;@2;)
              local.get 1
              local.get 4
              local.get 1
              i32.load offset=4
              local.tee 8
              i32.gt_u
              local.tee 6
              i32.const 3
              i32.shl
              i32.add
              local.tee 9
              local.get 4
              local.get 8
              i32.const 0
              local.get 6
              select
              i32.sub
              local.tee 8
              local.get 9
              i32.load
              i32.add
              i32.store
              local.get 1
              i32.const 12
              i32.const 4
              local.get 6
              select
              i32.add
              local.tee 9
              local.get 9
              i32.load
              local.get 8
              i32.sub
              i32.store
              local.get 5
              local.get 4
              i32.sub
              local.set 5
              local.get 0
              i32.load offset=60
              local.get 1
              i32.const 8
              i32.add
              local.get 1
              local.get 6
              select
              local.tee 1
              local.get 7
              local.get 6
              i32.sub
              local.tee 7
              local.get 3
              i32.const 12
              i32.add
              call 0
              local.tee 4
              if (result i32)  ;; label = @6
                i32.const 2776
                local.get 4
                i32.store
                i32.const -1
              else
                i32.const 0
              end
              i32.eqz
              br_if 0 (;@5;)
            end
          end
          local.get 5
          i32.const -1
          i32.ne
          br_if 1 (;@2;)
        end
        local.get 0
        local.get 0
        i32.load offset=44
        local.tee 1
        i32.store offset=28
        local.get 0
        local.get 1
        i32.store offset=20
        local.get 0
        local.get 1
        local.get 0
        i32.load offset=48
        i32.add
        i32.store offset=16
        local.get 2
        br 1 (;@1;)
      end
      local.get 0
      i32.const 0
      i32.store offset=28
      local.get 0
      i64.const 0
      i64.store offset=16
      local.get 0
      local.get 0
      i32.load
      i32.const 32
      i32.or
      i32.store
      i32.const 0
      local.get 7
      i32.const 2
      i32.eq
      br_if 0 (;@1;)
      drop
      local.get 2
      local.get 1
      i32.load offset=4
      i32.sub
    end
    local.set 0
    local.get 3
    i32.const 32
    i32.add
    global.set 0
    local.get 0)
  (func (;8;) (type 5) (result i32)
    i32.const 2776)
  (func (;9;) (type 0) (param i32 i32 i32) (result i32)
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
        local.tee 4
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
  (func (;10;) (type 1) (param i32)
    (local i32)
    i32.const 2788
    i32.load
    local.tee 1
    if  ;; label = @1
      loop  ;; label = @2
        local.get 1
        call 11
        local.get 1
        i32.load offset=56
        local.tee 1
        br_if 0 (;@2;)
      end
    end
    i32.const 2792
    i32.load
    call 11
    i32.const 1720
    i32.load
    call 11
    local.get 0
    call 1
    unreachable)
  (func (;11;) (type 1) (param i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      i32.eqz
      br_if 0 (;@1;)
      local.get 0
      i32.load offset=76
      drop
      local.get 0
      i32.load offset=20
      local.get 0
      i32.load offset=28
      i32.gt_u
      if  ;; label = @2
        local.get 0
        i32.const 0
        i32.const 0
        local.get 0
        i32.load offset=36
        call_indirect (type 0)
        drop
      end
      local.get 0
      i32.load offset=4
      local.tee 1
      local.get 0
      i32.load offset=8
      local.tee 2
      i32.ge_u
      br_if 0 (;@1;)
      local.get 0
      local.get 1
      local.get 2
      i32.sub
      i64.extend_i32_s
      i32.const 1
      local.get 0
      i32.load offset=40
      call_indirect (type 4)
      drop
    end)
  (func (;12;) (type 0) (param i32 i32 i32) (result i32)
    (local i32 i32 i32)
    block  ;; label = @1
      local.get 1
      local.get 2
      i32.load offset=16
      local.tee 3
      if (result i32)  ;; label = @2
        local.get 3
      else
        block (result i32)  ;; label = @3
          local.get 2
          local.get 2
          i32.load8_u offset=74
          local.tee 3
          i32.const 1
          i32.sub
          local.get 3
          i32.or
          i32.store8 offset=74
          local.get 2
          i32.load
          local.tee 3
          i32.const 8
          i32.and
          if  ;; label = @4
            local.get 2
            local.get 3
            i32.const 32
            i32.or
            i32.store
            i32.const -1
            br 1 (;@3;)
          end
          local.get 2
          i64.const 0
          i64.store offset=4 align=4
          local.get 2
          local.get 2
          i32.load offset=44
          local.tee 3
          i32.store offset=28
          local.get 2
          local.get 3
          i32.store offset=20
          local.get 2
          local.get 3
          local.get 2
          i32.load offset=48
          i32.add
          i32.store offset=16
          i32.const 0
        end
        br_if 1 (;@1;)
        local.get 2
        i32.load offset=16
      end
      local.get 2
      i32.load offset=20
      local.tee 5
      i32.sub
      i32.gt_u
      if  ;; label = @2
        local.get 2
        local.get 0
        local.get 1
        local.get 2
        i32.load offset=36
        call_indirect (type 0)
        return
      end
      block  ;; label = @2
        local.get 2
        i32.load8_s offset=75
        i32.const 0
        i32.lt_s
        if  ;; label = @3
          i32.const 0
          local.set 3
          br 1 (;@2;)
        end
        local.get 1
        local.set 4
        loop  ;; label = @3
          local.get 4
          local.tee 3
          i32.eqz
          if  ;; label = @4
            i32.const 0
            local.set 3
            br 2 (;@2;)
          end
          local.get 0
          local.get 3
          i32.const 1
          i32.sub
          local.tee 4
          i32.add
          i32.load8_u
          i32.const 10
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 2
        local.get 0
        local.get 3
        local.get 2
        i32.load offset=36
        call_indirect (type 0)
        local.tee 4
        local.get 3
        i32.lt_u
        br_if 1 (;@1;)
        local.get 0
        local.get 3
        i32.add
        local.set 0
        local.get 1
        local.get 3
        i32.sub
        local.set 1
        local.get 2
        i32.load offset=20
        local.set 5
      end
      local.get 5
      local.get 0
      local.get 1
      call 9
      drop
      local.get 2
      local.get 2
      i32.load offset=20
      local.get 1
      i32.add
      i32.store offset=20
      local.get 1
      local.get 3
      i32.add
      local.set 4
    end
    local.get 4)
  (func (;13;) (type 0) (param i32 i32 i32) (result i32)
    local.get 1
    block (result i32)  ;; label = @1
      local.get 2
      i32.load offset=76
      i32.const -1
      i32.le_s
      if  ;; label = @2
        local.get 0
        local.get 1
        local.get 2
        call 12
        br 1 (;@1;)
      end
      local.get 0
      local.get 1
      local.get 2
      call 12
    end
    local.tee 0
    i32.eq
    if  ;; label = @1
      local.get 1
      i32.eqz
      i32.eqz
      return
    end
    local.get 0
    local.get 1
    i32.div_u)
  (func (;14;) (type 5) (result i32)
    global.get 0)
  (func (;15;) (type 1) (param i32)
    local.get 0
    global.set 0)
  (func (;16;) (type 3) (param i32) (result i32)
    global.get 0
    local.get 0
    i32.sub
    i32.const -16
    i32.and
    local.tee 0
    global.set 0
    local.get 0)
  (table (;0;) 5 5 funcref)
  (memory (;0;) 256 256)
  (global (;0;) (mut i32) (i32.const 5245680))
  (export "memory" (memory 0))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func 4))
  (export "__errno_location" (func 8))
  (export "stackSave" (func 14))
  (export "stackRestore" (func 15))
  (export "stackAlloc" (func 16))
  (elem (;0;) (i32.const 1) func 2 5 7 6)
  (data (;0;) (i32.const 1024) ">THREE Homo sapiens frequency\0a\00>ONE Homo sapiens alu\0a\00>TWO IUB ambiguity codes\0a\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00a\00\00\00q=\8a>c\00\00\00\8f\c2\f5=g\00\00\00\8f\c2\f5=t\00\00\00q=\8a>B\00\00\00\0a\d7\a3<D\00\00\00\0a\d7\a3<H\00\00\00\0a\d7\a3<K\00\00\00\0a\d7\a3<M\00\00\00\0a\d7\a3<N\00\00\00\0a\d7\a3<R\00\00\00\0a\d7\a3<S\00\00\00\0a\d7\a3<V\00\00\00\0a\d7\a3<W\00\00\00\0a\d7\a3<Y\00\00\00\0a\d7\a3<\00\00\00\00\00\00\00\00a\00\00\00\e9\1c\9b>c\00\00\00r\bdJ>g\00\00\00\d7IJ>t\00\00\00r_\9a>(\06")
  (data (;1;) (i32.const 1560) "*\00\00\00\01\00\00\00\01\00\00\00\00\00\00\00\05")
  (data (;2;) (i32.const 1588) "\02")
  (data (;3;) (i32.const 1612) "\03\00\00\00\04\00\00\00\d8\06\00\00\00\04")
  (data (;4;) (i32.const 1636) "\01")
  (data (;5;) (i32.const 1651) "\0a\ff\ff\ff\ff")
  (data (;6;) (i32.const 1720) "(\06"))
