(module
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func (param i32)))
  (type (;2;) (func (param i32 i32 i32) (result i32)))
  (type (;3;) (func (param i32 i32 i32 i32) (result i32)))
  (type (;4;) (func))
  (type (;5;) (func (param i32 i64 i32) (result i64)))
  (type (;6;) (func (result i32)))
  (type (;7;) (func (param i32 i64 i32 i32) (result i32)))
  (type (;8;) (func (param i32 i32)))
  (type (;9;) (func (param i32 i32 i32)))
  (import "wasi_snapshot_preview1" "fd_write" (func (;0;) (type 3)))
  (import "wasi_snapshot_preview1" "fd_close" (func (;1;) (type 0)))
  (import "wasi_snapshot_preview1" "fd_read" (func (;2;) (type 3)))
  (import "wasi_snapshot_preview1" "fd_seek" (func (;3;) (type 7)))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;4;) (type 1)))
  (func (;5;) (type 4)
    nop)
  (func (;6;) (type 4)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    block (result i32)  ;; label = @1
      i32.const 1072
      i32.load
      local.tee 5
      i32.load8_u
      local.tee 3
      if  ;; label = @2
        loop  ;; label = @3
          local.get 3
          i32.const 24
          i32.shl
          i32.const 24
          i32.shr_s
          local.tee 0
          i32.const 95
          i32.and
          local.get 0
          local.get 0
          i32.const 97
          i32.sub
          i32.const 26
          i32.lt_u
          select
          i32.const 1376
          i32.add
          local.get 5
          i32.load8_u offset=1
          i32.store8
          local.get 5
          i32.load8_s
          local.tee 0
          i32.const 32
          i32.or
          local.get 0
          local.get 0
          i32.const 65
          i32.sub
          i32.const 26
          i32.lt_u
          select
          i32.const 1376
          i32.add
          local.get 5
          i32.load8_u offset=1
          i32.store8
          local.get 5
          i32.load8_u offset=2
          local.set 3
          local.get 5
          i32.const 2
          i32.add
          local.set 5
          local.get 3
          br_if 0 (;@3;)
        end
      end
      i32.const 8192
      local.set 5
      i32.const 7936
      local.set 3
      i32.const 8192
      call 15
      local.set 10
      block  ;; label = @2
        block (result i32)  ;; label = @3
          block  ;; label = @4
            i32.const 1064
            i32.load
            local.tee 0
            i32.load offset=76
            i32.const 0
            i32.lt_s
            br_if 0 (;@4;)
          end
          local.get 0
          i32.load offset=60
          local.tee 14
        end
        local.get 10
        i32.const 7936
        call 13
        local.tee 2
        i32.eqz
        if  ;; label = @3
          local.get 10
          local.set 0
          br 1 (;@2;)
        end
        loop  ;; label = @3
          local.get 14
          block (result i32)  ;; label = @4
            local.get 3
            local.get 2
            local.get 13
            i32.add
            local.tee 13
            i32.le_u
            if  ;; label = @5
              block (result i32)  ;; label = @6
                local.get 5
                i32.const -1048576
                i32.sub
                local.get 5
                i32.const 1
                i32.shl
                local.get 5
                i32.const 1048575
                i32.gt_u
                select
                local.tee 5
                local.set 11
                local.get 10
                i32.eqz
                if  ;; label = @7
                  local.get 11
                  call 15
                  br 1 (;@6;)
                end
                local.get 11
                i32.const -64
                i32.ge_u
                if  ;; label = @7
                  i32.const 3576
                  i32.const 48
                  i32.store
                  i32.const 0
                  br 1 (;@6;)
                end
                block (result i32)  ;; label = @7
                  i32.const 16
                  local.get 11
                  i32.const 11
                  i32.add
                  i32.const -8
                  i32.and
                  local.get 11
                  i32.const 11
                  i32.lt_u
                  select
                  local.set 7
                  i32.const 0
                  local.set 0
                  local.get 10
                  i32.const 8
                  i32.sub
                  local.tee 2
                  i32.load offset=4
                  local.tee 3
                  i32.const -8
                  i32.and
                  local.set 6
                  block  ;; label = @8
                    local.get 3
                    i32.const 3
                    i32.and
                    i32.eqz
                    if  ;; label = @9
                      i32.const 0
                      local.get 7
                      i32.const 256
                      i32.lt_u
                      br_if 2 (;@7;)
                      drop
                      local.get 7
                      i32.const 4
                      i32.add
                      local.get 6
                      i32.le_u
                      if  ;; label = @10
                        local.get 2
                        local.set 0
                        local.get 6
                        local.get 7
                        i32.sub
                        i32.const 4060
                        i32.load
                        i32.const 1
                        i32.shl
                        i32.le_u
                        br_if 2 (;@8;)
                      end
                      i32.const 0
                      br 2 (;@7;)
                    end
                    block  ;; label = @9
                      local.get 6
                      local.get 7
                      i32.ge_u
                      if  ;; label = @10
                        local.get 6
                        local.get 7
                        i32.sub
                        local.tee 9
                        i32.const 16
                        i32.lt_u
                        br_if 1 (;@9;)
                        local.get 2
                        local.get 3
                        i32.const 1
                        i32.and
                        local.get 7
                        i32.or
                        i32.const 2
                        i32.or
                        i32.store offset=4
                        local.get 2
                        local.get 7
                        i32.add
                        local.tee 1
                        local.get 9
                        i32.const 3
                        i32.or
                        i32.store offset=4
                        local.get 2
                        local.get 6
                        i32.const 4
                        i32.or
                        i32.add
                        local.tee 0
                        local.get 0
                        i32.load
                        i32.const 1
                        i32.or
                        i32.store
                        local.get 1
                        local.get 9
                        call 17
                        br 1 (;@9;)
                      end
                      local.get 2
                      local.get 6
                      i32.add
                      local.tee 8
                      i32.const 3604
                      i32.load
                      i32.eq
                      if  ;; label = @10
                        i32.const 3592
                        i32.load
                        local.get 6
                        i32.add
                        local.tee 9
                        local.get 7
                        i32.le_u
                        br_if 2 (;@8;)
                        local.get 2
                        local.get 3
                        i32.const 1
                        i32.and
                        local.get 7
                        i32.or
                        i32.const 2
                        i32.or
                        i32.store offset=4
                        local.get 2
                        local.get 7
                        i32.add
                        local.tee 1
                        local.get 9
                        local.get 7
                        i32.sub
                        local.tee 0
                        i32.const 1
                        i32.or
                        i32.store offset=4
                        i32.const 3592
                        local.get 0
                        i32.store
                        i32.const 3604
                        local.get 1
                        i32.store
                        br 1 (;@9;)
                      end
                      local.get 8
                      i32.const 3600
                      i32.load
                      i32.eq
                      if  ;; label = @10
                        i32.const 3588
                        i32.load
                        local.get 6
                        i32.add
                        local.tee 1
                        local.get 7
                        i32.lt_u
                        br_if 2 (;@8;)
                        block  ;; label = @11
                          local.get 1
                          local.get 7
                          i32.sub
                          local.tee 0
                          i32.const 16
                          i32.ge_u
                          if  ;; label = @12
                            local.get 2
                            local.get 3
                            i32.const 1
                            i32.and
                            local.get 7
                            i32.or
                            i32.const 2
                            i32.or
                            i32.store offset=4
                            local.get 2
                            local.get 7
                            i32.add
                            local.tee 6
                            local.get 0
                            i32.const 1
                            i32.or
                            i32.store offset=4
                            local.get 1
                            local.get 2
                            i32.add
                            local.tee 1
                            local.get 0
                            i32.store
                            local.get 1
                            local.get 1
                            i32.load offset=4
                            i32.const -2
                            i32.and
                            i32.store offset=4
                            br 1 (;@11;)
                          end
                          local.get 2
                          local.get 3
                          i32.const 1
                          i32.and
                          local.get 1
                          i32.or
                          i32.const 2
                          i32.or
                          i32.store offset=4
                          local.get 1
                          local.get 2
                          i32.add
                          local.tee 0
                          local.get 0
                          i32.load offset=4
                          i32.const 1
                          i32.or
                          i32.store offset=4
                          i32.const 0
                          local.set 0
                          i32.const 0
                          local.set 6
                        end
                        i32.const 3600
                        local.get 6
                        i32.store
                        i32.const 3588
                        local.get 0
                        i32.store
                        br 1 (;@9;)
                      end
                      local.get 8
                      i32.load offset=4
                      local.tee 1
                      i32.const 2
                      i32.and
                      br_if 1 (;@8;)
                      local.get 1
                      i32.const -8
                      i32.and
                      local.get 6
                      i32.add
                      local.tee 4
                      local.get 7
                      i32.lt_u
                      br_if 1 (;@8;)
                      local.get 4
                      local.get 7
                      i32.sub
                      local.set 15
                      block  ;; label = @10
                        local.get 1
                        i32.const 255
                        i32.le_u
                        if  ;; label = @11
                          local.get 8
                          i32.load offset=8
                          local.tee 9
                          local.get 1
                          i32.const 3
                          i32.shr_u
                          local.tee 0
                          i32.const 3
                          i32.shl
                          i32.const 3620
                          i32.add
                          i32.eq
                          drop
                          local.get 9
                          local.get 8
                          i32.load offset=12
                          local.tee 1
                          i32.eq
                          if  ;; label = @12
                            i32.const 3580
                            i32.const 3580
                            i32.load
                            i32.const -2
                            local.get 0
                            i32.rotl
                            i32.and
                            i32.store
                            br 2 (;@10;)
                          end
                          local.get 9
                          local.get 1
                          i32.store offset=12
                          local.get 1
                          local.get 9
                          i32.store offset=8
                          br 1 (;@10;)
                        end
                        local.get 8
                        i32.load offset=24
                        local.set 12
                        block  ;; label = @11
                          local.get 8
                          local.get 8
                          i32.load offset=12
                          local.tee 1
                          i32.ne
                          if  ;; label = @12
                            local.get 8
                            i32.load offset=8
                            local.tee 0
                            i32.const 3596
                            i32.load
                            i32.lt_u
                            drop
                            local.get 0
                            local.get 1
                            i32.store offset=12
                            local.get 1
                            local.get 0
                            i32.store offset=8
                            br 1 (;@11;)
                          end
                          block  ;; label = @12
                            local.get 8
                            i32.const 20
                            i32.add
                            local.tee 6
                            i32.load
                            local.tee 0
                            br_if 0 (;@12;)
                            local.get 8
                            i32.const 16
                            i32.add
                            local.tee 6
                            i32.load
                            local.tee 0
                            br_if 0 (;@12;)
                            i32.const 0
                            local.set 1
                            br 1 (;@11;)
                          end
                          loop  ;; label = @12
                            local.get 6
                            local.set 9
                            local.get 0
                            local.tee 1
                            i32.const 20
                            i32.add
                            local.tee 6
                            i32.load
                            local.tee 0
                            br_if 0 (;@12;)
                            local.get 1
                            i32.const 16
                            i32.add
                            local.set 6
                            local.get 1
                            i32.load offset=16
                            local.tee 0
                            br_if 0 (;@12;)
                          end
                          local.get 9
                          i32.const 0
                          i32.store
                        end
                        local.get 12
                        i32.eqz
                        br_if 0 (;@10;)
                        block  ;; label = @11
                          local.get 8
                          local.get 8
                          i32.load offset=28
                          local.tee 9
                          i32.const 2
                          i32.shl
                          i32.const 3884
                          i32.add
                          local.tee 0
                          i32.load
                          i32.eq
                          if  ;; label = @12
                            local.get 0
                            local.get 1
                            i32.store
                            local.get 1
                            br_if 1 (;@11;)
                            i32.const 3584
                            i32.const 3584
                            i32.load
                            i32.const -2
                            local.get 9
                            i32.rotl
                            i32.and
                            i32.store
                            br 2 (;@10;)
                          end
                          local.get 12
                          i32.const 16
                          i32.const 20
                          local.get 12
                          i32.load offset=16
                          local.get 8
                          i32.eq
                          select
                          i32.add
                          local.get 1
                          i32.store
                          local.get 1
                          i32.eqz
                          br_if 1 (;@10;)
                        end
                        local.get 1
                        local.get 12
                        i32.store offset=24
                        local.get 8
                        i32.load offset=16
                        local.tee 0
                        if  ;; label = @11
                          local.get 1
                          local.get 0
                          i32.store offset=16
                          local.get 0
                          local.get 1
                          i32.store offset=24
                        end
                        local.get 8
                        i32.load offset=20
                        local.tee 0
                        i32.eqz
                        br_if 0 (;@10;)
                        local.get 1
                        local.get 0
                        i32.store offset=20
                        local.get 0
                        local.get 1
                        i32.store offset=24
                      end
                      local.get 15
                      i32.const 15
                      i32.le_u
                      if  ;; label = @10
                        local.get 2
                        local.get 3
                        i32.const 1
                        i32.and
                        local.get 4
                        i32.or
                        i32.const 2
                        i32.or
                        i32.store offset=4
                        local.get 2
                        local.get 4
                        i32.const 4
                        i32.or
                        i32.add
                        local.tee 0
                        local.get 0
                        i32.load
                        i32.const 1
                        i32.or
                        i32.store
                        br 1 (;@9;)
                      end
                      local.get 2
                      local.get 3
                      i32.const 1
                      i32.and
                      local.get 7
                      i32.or
                      i32.const 2
                      i32.or
                      i32.store offset=4
                      local.get 2
                      local.get 7
                      i32.add
                      local.tee 1
                      local.get 15
                      i32.const 3
                      i32.or
                      i32.store offset=4
                      local.get 2
                      local.get 4
                      i32.const 4
                      i32.or
                      i32.add
                      local.tee 0
                      local.get 0
                      i32.load
                      i32.const 1
                      i32.or
                      i32.store
                      local.get 1
                      local.get 15
                      call 17
                    end
                    local.get 2
                    local.set 0
                  end
                  local.get 0
                end
                local.tee 0
                if  ;; label = @7
                  local.get 0
                  i32.const 8
                  i32.add
                  br 1 (;@6;)
                end
                i32.const 0
                local.get 11
                call 15
                local.tee 2
                i32.eqz
                br_if 0 (;@6;)
                drop
                local.get 2
                local.get 10
                i32.const -4
                i32.const -8
                local.get 10
                i32.const 4
                i32.sub
                i32.load
                local.tee 0
                i32.const 3
                i32.and
                select
                local.get 0
                i32.const -8
                i32.and
                i32.add
                local.tee 0
                local.get 11
                local.get 0
                local.get 11
                i32.lt_u
                select
                call 19
                local.get 10
                call 16
                local.get 2
              end
              local.set 10
            end
            local.get 10
            local.get 13
            i32.add
            local.tee 0
          end
          local.get 5
          i32.const 256
          i32.sub
          local.tee 3
          local.get 13
          i32.sub
          call 13
          local.tee 2
          br_if 0 (;@3;)
        end
      end
      local.get 0
      i32.const 62
      i32.store8
      local.get 0
      i32.const 1
      i32.sub
      local.set 3
      loop  ;; label = @2
        i32.const 0
        local.set 5
        loop  ;; label = @3
          local.get 5
          local.tee 0
          i32.const 1
          i32.sub
          local.set 5
          local.get 0
          local.get 3
          i32.add
          local.tee 15
          i32.load8_u
          i32.const 62
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 3
        local.get 0
        i32.const 1
        i32.add
        local.tee 2
        i32.add
        local.set 0
        loop  ;; label = @3
          local.get 0
          i32.load8_u
          local.set 1
          local.get 3
          local.get 2
          local.tee 6
          i32.const 1
          i32.add
          local.tee 2
          i32.add
          local.tee 5
          local.set 0
          local.get 1
          i32.const 10
          i32.ne
          br_if 0 (;@3;)
        end
        block  ;; label = @3
          i32.const 60
          local.get 6
          i32.const -1
          i32.xor
          i32.const 61
          i32.rem_u
          local.tee 0
          i32.sub
          local.tee 9
          i32.eqz
          br_if 0 (;@3;)
          local.get 0
          local.get 2
          i32.add
          local.tee 0
          i32.const -1
          i32.gt_s
          br_if 0 (;@3;)
          local.get 0
          local.get 3
          i32.add
          local.set 2
          loop  ;; label = @4
            local.get 9
            local.set 0
            block  ;; label = @5
              local.get 2
              i32.const 1
              i32.add
              local.tee 4
              local.get 2
              local.tee 1
              i32.eq
              br_if 0 (;@5;)
              local.get 1
              local.get 0
              local.get 4
              i32.add
              local.tee 14
              i32.sub
              i32.const 0
              local.get 0
              i32.const 1
              i32.shl
              i32.sub
              i32.le_u
              if  ;; label = @6
                local.get 4
                local.get 1
                local.get 0
                call 19
                br 1 (;@5;)
              end
              local.get 1
              local.get 4
              i32.xor
              i32.const 3
              i32.and
              local.set 12
              block  ;; label = @6
                block  ;; label = @7
                  local.get 1
                  local.get 4
                  i32.gt_u
                  if  ;; label = @8
                    local.get 12
                    br_if 2 (;@6;)
                    local.get 4
                    i32.const 3
                    i32.and
                    i32.eqz
                    br_if 1 (;@7;)
                    loop  ;; label = @9
                      local.get 0
                      i32.eqz
                      br_if 4 (;@5;)
                      local.get 4
                      local.get 1
                      i32.load8_u
                      i32.store8
                      local.get 1
                      i32.const 1
                      i32.add
                      local.set 1
                      local.get 0
                      i32.const 1
                      i32.sub
                      local.set 0
                      local.get 4
                      i32.const 1
                      i32.add
                      local.tee 4
                      i32.const 3
                      i32.and
                      br_if 0 (;@9;)
                    end
                    br 1 (;@7;)
                  end
                  block  ;; label = @8
                    local.get 12
                    br_if 0 (;@8;)
                    local.get 14
                    i32.const 3
                    i32.and
                    if  ;; label = @9
                      loop  ;; label = @10
                        local.get 0
                        i32.eqz
                        br_if 5 (;@5;)
                        local.get 4
                        local.get 0
                        i32.const 1
                        i32.sub
                        local.tee 0
                        i32.add
                        local.tee 14
                        local.get 0
                        local.get 1
                        i32.add
                        i32.load8_u
                        i32.store8
                        local.get 14
                        i32.const 3
                        i32.and
                        br_if 0 (;@10;)
                      end
                    end
                    local.get 0
                    i32.const 3
                    i32.le_u
                    br_if 0 (;@8;)
                    loop  ;; label = @9
                      local.get 4
                      local.get 0
                      i32.const 4
                      i32.sub
                      local.tee 0
                      i32.add
                      local.get 0
                      local.get 1
                      i32.add
                      i32.load
                      i32.store
                      local.get 0
                      i32.const 3
                      i32.gt_u
                      br_if 0 (;@9;)
                    end
                  end
                  local.get 0
                  i32.eqz
                  br_if 2 (;@5;)
                  loop  ;; label = @8
                    local.get 4
                    local.get 0
                    i32.const 1
                    i32.sub
                    local.tee 0
                    i32.add
                    local.get 0
                    local.get 1
                    i32.add
                    i32.load8_u
                    i32.store8
                    local.get 0
                    br_if 0 (;@8;)
                  end
                  br 2 (;@5;)
                end
                local.get 0
                i32.const 3
                i32.le_u
                br_if 0 (;@6;)
                loop  ;; label = @7
                  local.get 4
                  local.get 1
                  i32.load
                  i32.store
                  local.get 1
                  i32.const 4
                  i32.add
                  local.set 1
                  local.get 4
                  i32.const 4
                  i32.add
                  local.set 4
                  local.get 0
                  i32.const 4
                  i32.sub
                  local.tee 0
                  i32.const 3
                  i32.gt_u
                  br_if 0 (;@7;)
                end
              end
              local.get 0
              i32.eqz
              br_if 0 (;@5;)
              loop  ;; label = @6
                local.get 4
                local.get 1
                i32.load8_u
                i32.store8
                local.get 4
                i32.const 1
                i32.add
                local.set 4
                local.get 1
                i32.const 1
                i32.add
                local.set 1
                local.get 0
                i32.const 1
                i32.sub
                local.tee 0
                br_if 0 (;@6;)
              end
            end
            local.get 2
            i32.const 10
            i32.store8
            local.get 2
            i32.const 61
            i32.add
            local.tee 2
            local.get 3
            i32.lt_u
            br_if 0 (;@4;)
          end
        end
        local.get 6
        i32.const -2
        i32.le_s
        if  ;; label = @3
          local.get 3
          i32.const 1
          i32.sub
          local.set 3
          loop  ;; label = @4
            local.get 5
            i32.load8_s
            i32.const 1376
            i32.add
            i32.load8_u
            local.set 0
            local.get 5
            local.get 3
            i32.load8_s
            i32.const 1376
            i32.add
            i32.load8_u
            i32.store8
            local.get 3
            local.get 0
            i32.store8
            local.get 5
            i32.const 1
            i32.add
            local.tee 5
            local.get 3
            i32.const 1
            i32.sub
            local.tee 3
            i32.le_u
            br_if 0 (;@4;)
          end
        end
        local.get 15
        i32.const 1
        i32.sub
        local.tee 3
        local.get 10
        i32.ge_u
        br_if 0 (;@2;)
      end
      block  ;; label = @2
        i32.const 1060
        i32.load
        local.tee 0
        i32.load offset=76
        i32.const 0
        i32.lt_s
        br_if 0 (;@2;)
      end
      local.get 0
      i32.load offset=60
      local.set 0
      global.get 0
      i32.const 16
      i32.sub
      local.tee 2
      global.set 0
      local.get 2
      local.get 13
      i32.store offset=12
      local.get 2
      local.get 10
      i32.store offset=8
      local.get 0
      local.get 2
      i32.const 8
      i32.add
      i32.const 1
      local.get 2
      i32.const 4
      i32.add
      call 0
      local.tee 0
      if  ;; label = @2
        i32.const 3576
        local.get 0
        i32.store
      end
      local.get 2
      i32.load offset=4
      drop
      local.get 2
      i32.const 16
      i32.add
      global.set 0
      local.get 10
      call 16
      i32.const 0
    end
    call 4
    unreachable)
  (func (;7;) (type 0) (param i32) (result i32)
    i32.const 0)
  (func (;8;) (type 5) (param i32 i64 i32) (result i64)
    i64.const 0)
  (func (;9;) (type 2) (param i32 i32 i32) (result i32)
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
            i32.const 3576
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
                i32.const 3576
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
  (func (;10;) (type 0) (param i32) (result i32)
    local.get 0
    i32.load offset=60
    call 1)
  (func (;11;) (type 2) (param i32 i32 i32) (result i32)
    (local i32 i32 i32 i32)
    global.get 0
    i32.const 32
    i32.sub
    local.tee 3
    global.set 0
    local.get 3
    local.get 1
    i32.store offset=16
    local.get 3
    local.get 2
    local.get 0
    i32.load offset=48
    local.tee 4
    i32.const 0
    i32.ne
    i32.sub
    i32.store offset=20
    local.get 0
    i32.load offset=44
    local.set 5
    local.get 3
    local.get 4
    i32.store offset=28
    local.get 3
    local.get 5
    i32.store offset=24
    i32.const -1
    local.set 4
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        i32.load offset=60
        local.get 3
        i32.const 16
        i32.add
        i32.const 2
        local.get 3
        i32.const 12
        i32.add
        call 2
        local.tee 5
        if (result i32)  ;; label = @3
          i32.const 3576
          local.get 5
          i32.store
          i32.const -1
        else
          i32.const 0
        end
        i32.eqz
        if  ;; label = @3
          local.get 3
          i32.load offset=12
          local.tee 4
          i32.const 0
          i32.gt_s
          br_if 1 (;@2;)
        end
        local.get 0
        local.get 0
        i32.load
        local.get 4
        i32.const 48
        i32.and
        i32.const 16
        i32.xor
        i32.or
        i32.store
        br 1 (;@1;)
      end
      local.get 4
      local.get 3
      i32.load offset=20
      local.tee 6
      i32.le_u
      br_if 0 (;@1;)
      local.get 0
      local.get 0
      i32.load offset=44
      local.tee 5
      i32.store offset=4
      local.get 0
      local.get 5
      local.get 4
      local.get 6
      i32.sub
      i32.add
      i32.store offset=8
      local.get 0
      i32.load offset=48
      if  ;; label = @2
        local.get 0
        local.get 5
        i32.const 1
        i32.add
        i32.store offset=4
        local.get 1
        local.get 2
        i32.add
        i32.const 1
        i32.sub
        local.get 5
        i32.load8_u
        i32.store8
      end
      local.get 2
      local.set 4
    end
    local.get 3
    i32.const 32
    i32.add
    global.set 0
    local.get 4)
  (func (;12;) (type 5) (param i32 i64 i32) (result i64)
    (local i32)
    global.get 0
    i32.const 16
    i32.sub
    local.tee 3
    global.set 0
    local.get 0
    i32.load offset=60
    local.get 1
    local.get 2
    i32.const 255
    i32.and
    local.get 3
    i32.const 8
    i32.add
    call 3
    local.tee 0
    if (result i32)  ;; label = @1
      i32.const 3576
      local.get 0
      i32.store
      i32.const -1
    else
      i32.const 0
    end
    local.set 0
    local.get 3
    i64.load offset=8
    local.set 1
    local.get 3
    i32.const 16
    i32.add
    global.set 0
    i64.const -1
    local.get 1
    local.get 0
    select)
  (func (;13;) (type 2) (param i32 i32 i32) (result i32)
    (local i32)
    global.get 0
    i32.const 16
    i32.sub
    local.tee 3
    global.set 0
    local.get 3
    local.get 2
    i32.store offset=12
    local.get 3
    local.get 1
    i32.store offset=8
    local.get 0
    local.get 3
    i32.const 8
    i32.add
    i32.const 1
    local.get 3
    i32.const 4
    i32.add
    call 2
    local.tee 0
    if (result i32)  ;; label = @1
      i32.const 3576
      local.get 0
      i32.store
      i32.const -1
    else
      i32.const 0
    end
    local.set 0
    local.get 3
    i32.load offset=4
    local.set 1
    local.get 3
    i32.const 16
    i32.add
    global.set 0
    i32.const -1
    local.get 1
    local.get 0
    select)
  (func (;14;) (type 6) (result i32)
    i32.const 3576)
  (func (;15;) (type 0) (param i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    global.get 0
    i32.const 16
    i32.sub
    local.tee 11
    global.set 0
    block  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          block  ;; label = @4
            block  ;; label = @5
              block  ;; label = @6
                block  ;; label = @7
                  block  ;; label = @8
                    block  ;; label = @9
                      block  ;; label = @10
                        block  ;; label = @11
                          local.get 0
                          i32.const 244
                          i32.le_u
                          if  ;; label = @12
                            i32.const 3580
                            i32.load
                            local.tee 7
                            i32.const 16
                            local.get 0
                            i32.const 11
                            i32.add
                            i32.const -8
                            i32.and
                            local.get 0
                            i32.const 11
                            i32.lt_u
                            select
                            local.tee 6
                            i32.const 3
                            i32.shr_u
                            local.tee 2
                            i32.shr_u
                            local.tee 1
                            i32.const 3
                            i32.and
                            if  ;; label = @13
                              local.get 1
                              i32.const -1
                              i32.xor
                              i32.const 1
                              i32.and
                              local.get 2
                              i32.add
                              local.tee 4
                              i32.const 3
                              i32.shl
                              local.tee 1
                              i32.const 3628
                              i32.add
                              i32.load
                              local.tee 3
                              i32.const 8
                              i32.add
                              local.set 0
                              block  ;; label = @14
                                local.get 3
                                i32.load offset=8
                                local.tee 2
                                local.get 1
                                i32.const 3620
                                i32.add
                                local.tee 1
                                i32.eq
                                if  ;; label = @15
                                  i32.const 3580
                                  local.get 7
                                  i32.const -2
                                  local.get 4
                                  i32.rotl
                                  i32.and
                                  i32.store
                                  br 1 (;@14;)
                                end
                                local.get 2
                                local.get 1
                                i32.store offset=12
                                local.get 1
                                local.get 2
                                i32.store offset=8
                              end
                              local.get 3
                              local.get 4
                              i32.const 3
                              i32.shl
                              local.tee 1
                              i32.const 3
                              i32.or
                              i32.store offset=4
                              local.get 1
                              local.get 3
                              i32.add
                              local.tee 1
                              local.get 1
                              i32.load offset=4
                              i32.const 1
                              i32.or
                              i32.store offset=4
                              br 12 (;@1;)
                            end
                            local.get 6
                            i32.const 3588
                            i32.load
                            local.tee 10
                            i32.le_u
                            br_if 1 (;@11;)
                            local.get 1
                            if  ;; label = @13
                              block  ;; label = @14
                                i32.const 2
                                local.get 2
                                i32.shl
                                local.tee 0
                                i32.const 0
                                local.get 0
                                i32.sub
                                i32.or
                                local.get 1
                                local.get 2
                                i32.shl
                                i32.and
                                local.tee 0
                                i32.const 0
                                local.get 0
                                i32.sub
                                i32.and
                                i32.const 1
                                i32.sub
                                local.tee 0
                                local.get 0
                                i32.const 12
                                i32.shr_u
                                i32.const 16
                                i32.and
                                local.tee 2
                                i32.shr_u
                                local.tee 1
                                i32.const 5
                                i32.shr_u
                                i32.const 8
                                i32.and
                                local.tee 0
                                local.get 2
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                local.tee 1
                                i32.const 2
                                i32.shr_u
                                i32.const 4
                                i32.and
                                local.tee 0
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                local.tee 1
                                i32.const 1
                                i32.shr_u
                                i32.const 2
                                i32.and
                                local.tee 0
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                local.tee 1
                                i32.const 1
                                i32.shr_u
                                i32.const 1
                                i32.and
                                local.tee 0
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                i32.add
                                local.tee 4
                                i32.const 3
                                i32.shl
                                local.tee 0
                                i32.const 3628
                                i32.add
                                i32.load
                                local.tee 3
                                i32.load offset=8
                                local.tee 1
                                local.get 0
                                i32.const 3620
                                i32.add
                                local.tee 0
                                i32.eq
                                if  ;; label = @15
                                  i32.const 3580
                                  local.get 7
                                  i32.const -2
                                  local.get 4
                                  i32.rotl
                                  i32.and
                                  local.tee 7
                                  i32.store
                                  br 1 (;@14;)
                                end
                                local.get 1
                                local.get 0
                                i32.store offset=12
                                local.get 0
                                local.get 1
                                i32.store offset=8
                              end
                              local.get 3
                              i32.const 8
                              i32.add
                              local.set 0
                              local.get 3
                              local.get 6
                              i32.const 3
                              i32.or
                              i32.store offset=4
                              local.get 3
                              local.get 6
                              i32.add
                              local.tee 2
                              local.get 4
                              i32.const 3
                              i32.shl
                              local.tee 1
                              local.get 6
                              i32.sub
                              local.tee 4
                              i32.const 1
                              i32.or
                              i32.store offset=4
                              local.get 1
                              local.get 3
                              i32.add
                              local.get 4
                              i32.store
                              local.get 10
                              if  ;; label = @14
                                local.get 10
                                i32.const 3
                                i32.shr_u
                                local.tee 1
                                i32.const 3
                                i32.shl
                                i32.const 3620
                                i32.add
                                local.set 5
                                i32.const 3600
                                i32.load
                                local.set 3
                                block (result i32)  ;; label = @15
                                  local.get 7
                                  i32.const 1
                                  local.get 1
                                  i32.shl
                                  local.tee 1
                                  i32.and
                                  i32.eqz
                                  if  ;; label = @16
                                    i32.const 3580
                                    local.get 1
                                    local.get 7
                                    i32.or
                                    i32.store
                                    local.get 5
                                    br 1 (;@15;)
                                  end
                                  local.get 5
                                  i32.load offset=8
                                end
                                local.set 1
                                local.get 5
                                local.get 3
                                i32.store offset=8
                                local.get 1
                                local.get 3
                                i32.store offset=12
                                local.get 3
                                local.get 5
                                i32.store offset=12
                                local.get 3
                                local.get 1
                                i32.store offset=8
                              end
                              i32.const 3600
                              local.get 2
                              i32.store
                              i32.const 3588
                              local.get 4
                              i32.store
                              br 12 (;@1;)
                            end
                            i32.const 3584
                            i32.load
                            local.tee 9
                            i32.eqz
                            br_if 1 (;@11;)
                            local.get 9
                            i32.const 0
                            local.get 9
                            i32.sub
                            i32.and
                            i32.const 1
                            i32.sub
                            local.tee 0
                            local.get 0
                            i32.const 12
                            i32.shr_u
                            i32.const 16
                            i32.and
                            local.tee 2
                            i32.shr_u
                            local.tee 1
                            i32.const 5
                            i32.shr_u
                            i32.const 8
                            i32.and
                            local.tee 0
                            local.get 2
                            i32.or
                            local.get 1
                            local.get 0
                            i32.shr_u
                            local.tee 1
                            i32.const 2
                            i32.shr_u
                            i32.const 4
                            i32.and
                            local.tee 0
                            i32.or
                            local.get 1
                            local.get 0
                            i32.shr_u
                            local.tee 1
                            i32.const 1
                            i32.shr_u
                            i32.const 2
                            i32.and
                            local.tee 0
                            i32.or
                            local.get 1
                            local.get 0
                            i32.shr_u
                            local.tee 1
                            i32.const 1
                            i32.shr_u
                            i32.const 1
                            i32.and
                            local.tee 0
                            i32.or
                            local.get 1
                            local.get 0
                            i32.shr_u
                            i32.add
                            i32.const 2
                            i32.shl
                            i32.const 3884
                            i32.add
                            i32.load
                            local.tee 1
                            i32.load offset=4
                            i32.const -8
                            i32.and
                            local.get 6
                            i32.sub
                            local.set 4
                            local.get 1
                            local.set 2
                            loop  ;; label = @13
                              block  ;; label = @14
                                local.get 2
                                i32.load offset=16
                                local.tee 0
                                i32.eqz
                                if  ;; label = @15
                                  local.get 2
                                  i32.load offset=20
                                  local.tee 0
                                  i32.eqz
                                  br_if 1 (;@14;)
                                end
                                local.get 0
                                i32.load offset=4
                                i32.const -8
                                i32.and
                                local.get 6
                                i32.sub
                                local.tee 2
                                local.get 4
                                local.get 2
                                local.get 4
                                i32.lt_u
                                local.tee 2
                                select
                                local.set 4
                                local.get 0
                                local.get 1
                                local.get 2
                                select
                                local.set 1
                                local.get 0
                                local.set 2
                                br 1 (;@13;)
                              end
                            end
                            local.get 1
                            i32.load offset=24
                            local.set 8
                            local.get 1
                            local.get 1
                            i32.load offset=12
                            local.tee 3
                            i32.ne
                            if  ;; label = @13
                              local.get 1
                              i32.load offset=8
                              local.tee 0
                              i32.const 3596
                              i32.load
                              i32.lt_u
                              drop
                              local.get 0
                              local.get 3
                              i32.store offset=12
                              local.get 3
                              local.get 0
                              i32.store offset=8
                              br 11 (;@2;)
                            end
                            local.get 1
                            i32.const 20
                            i32.add
                            local.tee 2
                            i32.load
                            local.tee 0
                            i32.eqz
                            if  ;; label = @13
                              local.get 1
                              i32.load offset=16
                              local.tee 0
                              i32.eqz
                              br_if 3 (;@10;)
                              local.get 1
                              i32.const 16
                              i32.add
                              local.set 2
                            end
                            loop  ;; label = @13
                              local.get 2
                              local.set 5
                              local.get 0
                              local.tee 3
                              i32.const 20
                              i32.add
                              local.tee 2
                              i32.load
                              local.tee 0
                              br_if 0 (;@13;)
                              local.get 3
                              i32.const 16
                              i32.add
                              local.set 2
                              local.get 3
                              i32.load offset=16
                              local.tee 0
                              br_if 0 (;@13;)
                            end
                            local.get 5
                            i32.const 0
                            i32.store
                            br 10 (;@2;)
                          end
                          i32.const -1
                          local.set 6
                          local.get 0
                          i32.const -65
                          i32.gt_u
                          br_if 0 (;@11;)
                          local.get 0
                          i32.const 11
                          i32.add
                          local.tee 0
                          i32.const -8
                          i32.and
                          local.set 6
                          i32.const 3584
                          i32.load
                          local.tee 9
                          i32.eqz
                          br_if 0 (;@11;)
                          i32.const 0
                          local.get 6
                          i32.sub
                          local.set 4
                          block  ;; label = @12
                            block  ;; label = @13
                              block  ;; label = @14
                                block (result i32)  ;; label = @15
                                  i32.const 0
                                  local.get 6
                                  i32.const 256
                                  i32.lt_u
                                  br_if 0 (;@15;)
                                  drop
                                  i32.const 31
                                  local.get 6
                                  i32.const 16777215
                                  i32.gt_u
                                  br_if 0 (;@15;)
                                  drop
                                  local.get 0
                                  i32.const 8
                                  i32.shr_u
                                  local.tee 0
                                  local.get 0
                                  i32.const 1048320
                                  i32.add
                                  i32.const 16
                                  i32.shr_u
                                  i32.const 8
                                  i32.and
                                  local.tee 2
                                  i32.shl
                                  local.tee 0
                                  local.get 0
                                  i32.const 520192
                                  i32.add
                                  i32.const 16
                                  i32.shr_u
                                  i32.const 4
                                  i32.and
                                  local.tee 1
                                  i32.shl
                                  local.tee 0
                                  local.get 0
                                  i32.const 245760
                                  i32.add
                                  i32.const 16
                                  i32.shr_u
                                  i32.const 2
                                  i32.and
                                  local.tee 0
                                  i32.shl
                                  i32.const 15
                                  i32.shr_u
                                  local.get 1
                                  local.get 2
                                  i32.or
                                  local.get 0
                                  i32.or
                                  i32.sub
                                  local.tee 0
                                  i32.const 1
                                  i32.shl
                                  local.get 6
                                  local.get 0
                                  i32.const 21
                                  i32.add
                                  i32.shr_u
                                  i32.const 1
                                  i32.and
                                  i32.or
                                  i32.const 28
                                  i32.add
                                end
                                local.tee 7
                                i32.const 2
                                i32.shl
                                i32.const 3884
                                i32.add
                                i32.load
                                local.tee 2
                                i32.eqz
                                if  ;; label = @15
                                  i32.const 0
                                  local.set 0
                                  br 1 (;@14;)
                                end
                                i32.const 0
                                local.set 0
                                local.get 6
                                i32.const 0
                                i32.const 25
                                local.get 7
                                i32.const 1
                                i32.shr_u
                                i32.sub
                                local.get 7
                                i32.const 31
                                i32.eq
                                select
                                i32.shl
                                local.set 1
                                loop  ;; label = @15
                                  block  ;; label = @16
                                    local.get 2
                                    i32.load offset=4
                                    i32.const -8
                                    i32.and
                                    local.get 6
                                    i32.sub
                                    local.tee 5
                                    local.get 4
                                    i32.ge_u
                                    br_if 0 (;@16;)
                                    local.get 2
                                    local.set 3
                                    local.get 5
                                    local.tee 4
                                    br_if 0 (;@16;)
                                    i32.const 0
                                    local.set 4
                                    local.get 2
                                    local.set 0
                                    br 3 (;@13;)
                                  end
                                  local.get 0
                                  local.get 2
                                  i32.load offset=20
                                  local.tee 5
                                  local.get 5
                                  local.get 2
                                  local.get 1
                                  i32.const 29
                                  i32.shr_u
                                  i32.const 4
                                  i32.and
                                  i32.add
                                  i32.load offset=16
                                  local.tee 2
                                  i32.eq
                                  select
                                  local.get 0
                                  local.get 5
                                  select
                                  local.set 0
                                  local.get 1
                                  i32.const 1
                                  i32.shl
                                  local.set 1
                                  local.get 2
                                  br_if 0 (;@15;)
                                end
                              end
                              local.get 0
                              local.get 3
                              i32.or
                              i32.eqz
                              if  ;; label = @14
                                i32.const 0
                                local.set 3
                                i32.const 2
                                local.get 7
                                i32.shl
                                local.tee 0
                                i32.const 0
                                local.get 0
                                i32.sub
                                i32.or
                                local.get 9
                                i32.and
                                local.tee 0
                                i32.eqz
                                br_if 3 (;@11;)
                                local.get 0
                                i32.const 0
                                local.get 0
                                i32.sub
                                i32.and
                                i32.const 1
                                i32.sub
                                local.tee 0
                                local.get 0
                                i32.const 12
                                i32.shr_u
                                i32.const 16
                                i32.and
                                local.tee 2
                                i32.shr_u
                                local.tee 1
                                i32.const 5
                                i32.shr_u
                                i32.const 8
                                i32.and
                                local.tee 0
                                local.get 2
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                local.tee 1
                                i32.const 2
                                i32.shr_u
                                i32.const 4
                                i32.and
                                local.tee 0
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                local.tee 1
                                i32.const 1
                                i32.shr_u
                                i32.const 2
                                i32.and
                                local.tee 0
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                local.tee 1
                                i32.const 1
                                i32.shr_u
                                i32.const 1
                                i32.and
                                local.tee 0
                                i32.or
                                local.get 1
                                local.get 0
                                i32.shr_u
                                i32.add
                                i32.const 2
                                i32.shl
                                i32.const 3884
                                i32.add
                                i32.load
                                local.set 0
                              end
                              local.get 0
                              i32.eqz
                              br_if 1 (;@12;)
                            end
                            loop  ;; label = @13
                              local.get 0
                              i32.load offset=4
                              i32.const -8
                              i32.and
                              local.get 6
                              i32.sub
                              local.tee 1
                              local.get 4
                              i32.lt_u
                              local.set 2
                              local.get 1
                              local.get 4
                              local.get 2
                              select
                              local.set 4
                              local.get 0
                              local.get 3
                              local.get 2
                              select
                              local.set 3
                              local.get 0
                              i32.load offset=16
                              local.tee 1
                              if (result i32)  ;; label = @14
                                local.get 1
                              else
                                local.get 0
                                i32.load offset=20
                              end
                              local.tee 0
                              br_if 0 (;@13;)
                            end
                          end
                          local.get 3
                          i32.eqz
                          br_if 0 (;@11;)
                          local.get 4
                          i32.const 3588
                          i32.load
                          local.get 6
                          i32.sub
                          i32.ge_u
                          br_if 0 (;@11;)
                          local.get 3
                          i32.load offset=24
                          local.set 7
                          local.get 3
                          local.get 3
                          i32.load offset=12
                          local.tee 1
                          i32.ne
                          if  ;; label = @12
                            local.get 3
                            i32.load offset=8
                            local.tee 0
                            i32.const 3596
                            i32.load
                            i32.lt_u
                            drop
                            local.get 0
                            local.get 1
                            i32.store offset=12
                            local.get 1
                            local.get 0
                            i32.store offset=8
                            br 9 (;@3;)
                          end
                          local.get 3
                          i32.const 20
                          i32.add
                          local.tee 2
                          i32.load
                          local.tee 0
                          i32.eqz
                          if  ;; label = @12
                            local.get 3
                            i32.load offset=16
                            local.tee 0
                            i32.eqz
                            br_if 3 (;@9;)
                            local.get 3
                            i32.const 16
                            i32.add
                            local.set 2
                          end
                          loop  ;; label = @12
                            local.get 2
                            local.set 5
                            local.get 0
                            local.tee 1
                            i32.const 20
                            i32.add
                            local.tee 2
                            i32.load
                            local.tee 0
                            br_if 0 (;@12;)
                            local.get 1
                            i32.const 16
                            i32.add
                            local.set 2
                            local.get 1
                            i32.load offset=16
                            local.tee 0
                            br_if 0 (;@12;)
                          end
                          local.get 5
                          i32.const 0
                          i32.store
                          br 8 (;@3;)
                        end
                        local.get 6
                        i32.const 3588
                        i32.load
                        local.tee 2
                        i32.le_u
                        if  ;; label = @11
                          i32.const 3600
                          i32.load
                          local.set 4
                          block  ;; label = @12
                            local.get 2
                            local.get 6
                            i32.sub
                            local.tee 1
                            i32.const 16
                            i32.ge_u
                            if  ;; label = @13
                              i32.const 3588
                              local.get 1
                              i32.store
                              i32.const 3600
                              local.get 4
                              local.get 6
                              i32.add
                              local.tee 0
                              i32.store
                              local.get 0
                              local.get 1
                              i32.const 1
                              i32.or
                              i32.store offset=4
                              local.get 2
                              local.get 4
                              i32.add
                              local.get 1
                              i32.store
                              local.get 4
                              local.get 6
                              i32.const 3
                              i32.or
                              i32.store offset=4
                              br 1 (;@12;)
                            end
                            i32.const 3600
                            i32.const 0
                            i32.store
                            i32.const 3588
                            i32.const 0
                            i32.store
                            local.get 4
                            local.get 2
                            i32.const 3
                            i32.or
                            i32.store offset=4
                            local.get 2
                            local.get 4
                            i32.add
                            local.tee 0
                            local.get 0
                            i32.load offset=4
                            i32.const 1
                            i32.or
                            i32.store offset=4
                          end
                          local.get 4
                          i32.const 8
                          i32.add
                          local.set 0
                          br 10 (;@1;)
                        end
                        local.get 6
                        i32.const 3592
                        i32.load
                        local.tee 8
                        i32.lt_u
                        if  ;; label = @11
                          i32.const 3592
                          local.get 8
                          local.get 6
                          i32.sub
                          local.tee 1
                          i32.store
                          i32.const 3604
                          i32.const 3604
                          i32.load
                          local.tee 2
                          local.get 6
                          i32.add
                          local.tee 0
                          i32.store
                          local.get 0
                          local.get 1
                          i32.const 1
                          i32.or
                          i32.store offset=4
                          local.get 2
                          local.get 6
                          i32.const 3
                          i32.or
                          i32.store offset=4
                          local.get 2
                          i32.const 8
                          i32.add
                          local.set 0
                          br 10 (;@1;)
                        end
                        i32.const 0
                        local.set 0
                        local.get 6
                        i32.const 47
                        i32.add
                        local.tee 9
                        block (result i32)  ;; label = @11
                          i32.const 4052
                          i32.load
                          if  ;; label = @12
                            i32.const 4060
                            i32.load
                            br 1 (;@11;)
                          end
                          i32.const 4064
                          i64.const -1
                          i64.store align=4
                          i32.const 4056
                          i64.const 17592186048512
                          i64.store align=4
                          i32.const 4052
                          local.get 11
                          i32.const 12
                          i32.add
                          i32.const -16
                          i32.and
                          i32.const 1431655768
                          i32.xor
                          i32.store
                          i32.const 4072
                          i32.const 0
                          i32.store
                          i32.const 4024
                          i32.const 0
                          i32.store
                          i32.const 4096
                        end
                        local.tee 1
                        i32.add
                        local.tee 7
                        i32.const 0
                        local.get 1
                        i32.sub
                        local.tee 5
                        i32.and
                        local.tee 2
                        local.get 6
                        i32.le_u
                        br_if 9 (;@1;)
                        i32.const 4020
                        i32.load
                        local.tee 3
                        if  ;; label = @11
                          i32.const 4012
                          i32.load
                          local.tee 4
                          local.get 2
                          i32.add
                          local.tee 1
                          local.get 4
                          i32.le_u
                          br_if 10 (;@1;)
                          local.get 1
                          local.get 3
                          i32.gt_u
                          br_if 10 (;@1;)
                        end
                        i32.const 4024
                        i32.load8_u
                        i32.const 4
                        i32.and
                        br_if 4 (;@6;)
                        block  ;; label = @11
                          block  ;; label = @12
                            i32.const 3604
                            i32.load
                            local.tee 4
                            if  ;; label = @13
                              i32.const 4028
                              local.set 0
                              loop  ;; label = @14
                                local.get 4
                                local.get 0
                                i32.load
                                local.tee 1
                                i32.ge_u
                                if  ;; label = @15
                                  local.get 1
                                  local.get 0
                                  i32.load offset=4
                                  i32.add
                                  local.get 4
                                  i32.gt_u
                                  br_if 3 (;@12;)
                                end
                                local.get 0
                                i32.load offset=8
                                local.tee 0
                                br_if 0 (;@14;)
                              end
                            end
                            i32.const 0
                            call 18
                            local.tee 1
                            i32.const -1
                            i32.eq
                            br_if 5 (;@7;)
                            local.get 2
                            local.set 7
                            i32.const 4056
                            i32.load
                            local.tee 4
                            i32.const 1
                            i32.sub
                            local.tee 0
                            local.get 1
                            i32.and
                            if  ;; label = @13
                              local.get 2
                              local.get 1
                              i32.sub
                              local.get 0
                              local.get 1
                              i32.add
                              i32.const 0
                              local.get 4
                              i32.sub
                              i32.and
                              i32.add
                              local.set 7
                            end
                            local.get 6
                            local.get 7
                            i32.ge_u
                            br_if 5 (;@7;)
                            local.get 7
                            i32.const 2147483646
                            i32.gt_u
                            br_if 5 (;@7;)
                            i32.const 4020
                            i32.load
                            local.tee 3
                            if  ;; label = @13
                              i32.const 4012
                              i32.load
                              local.tee 4
                              local.get 7
                              i32.add
                              local.tee 0
                              local.get 4
                              i32.le_u
                              br_if 6 (;@7;)
                              local.get 0
                              local.get 3
                              i32.gt_u
                              br_if 6 (;@7;)
                            end
                            local.get 7
                            call 18
                            local.tee 0
                            local.get 1
                            i32.ne
                            br_if 1 (;@11;)
                            br 7 (;@5;)
                          end
                          local.get 7
                          local.get 8
                          i32.sub
                          local.get 5
                          i32.and
                          local.tee 7
                          i32.const 2147483646
                          i32.gt_u
                          br_if 4 (;@7;)
                          local.get 7
                          call 18
                          local.tee 1
                          local.get 0
                          i32.load
                          local.get 0
                          i32.load offset=4
                          i32.add
                          i32.eq
                          br_if 3 (;@8;)
                          local.get 1
                          local.set 0
                        end
                        block  ;; label = @11
                          local.get 0
                          i32.const -1
                          i32.eq
                          br_if 0 (;@11;)
                          local.get 6
                          i32.const 48
                          i32.add
                          local.get 7
                          i32.le_u
                          br_if 0 (;@11;)
                          i32.const 4060
                          i32.load
                          local.tee 1
                          local.get 9
                          local.get 7
                          i32.sub
                          i32.add
                          i32.const 0
                          local.get 1
                          i32.sub
                          i32.and
                          local.tee 1
                          i32.const 2147483646
                          i32.gt_u
                          if  ;; label = @12
                            local.get 0
                            local.set 1
                            br 7 (;@5;)
                          end
                          local.get 1
                          call 18
                          i32.const -1
                          i32.ne
                          if  ;; label = @12
                            local.get 1
                            local.get 7
                            i32.add
                            local.set 7
                            local.get 0
                            local.set 1
                            br 7 (;@5;)
                          end
                          i32.const 0
                          local.get 7
                          i32.sub
                          call 18
                          drop
                          br 4 (;@7;)
                        end
                        local.get 0
                        local.tee 1
                        i32.const -1
                        i32.ne
                        br_if 5 (;@5;)
                        br 3 (;@7;)
                      end
                      i32.const 0
                      local.set 3
                      br 7 (;@2;)
                    end
                    i32.const 0
                    local.set 1
                    br 5 (;@3;)
                  end
                  local.get 1
                  i32.const -1
                  i32.ne
                  br_if 2 (;@5;)
                end
                i32.const 4024
                i32.const 4024
                i32.load
                i32.const 4
                i32.or
                i32.store
              end
              local.get 2
              i32.const 2147483646
              i32.gt_u
              br_if 1 (;@4;)
              local.get 2
              call 18
              local.set 1
              i32.const 0
              call 18
              local.set 0
              local.get 1
              i32.const -1
              i32.eq
              br_if 1 (;@4;)
              local.get 0
              i32.const -1
              i32.eq
              br_if 1 (;@4;)
              local.get 0
              local.get 1
              i32.le_u
              br_if 1 (;@4;)
              local.get 0
              local.get 1
              i32.sub
              local.tee 7
              local.get 6
              i32.const 40
              i32.add
              i32.le_u
              br_if 1 (;@4;)
            end
            i32.const 4012
            i32.const 4012
            i32.load
            local.get 7
            i32.add
            local.tee 0
            i32.store
            i32.const 4016
            i32.load
            local.get 0
            i32.lt_u
            if  ;; label = @5
              i32.const 4016
              local.get 0
              i32.store
            end
            block  ;; label = @5
              block  ;; label = @6
                block  ;; label = @7
                  i32.const 3604
                  i32.load
                  local.tee 5
                  if  ;; label = @8
                    i32.const 4028
                    local.set 0
                    loop  ;; label = @9
                      local.get 1
                      local.get 0
                      i32.load
                      local.tee 4
                      local.get 0
                      i32.load offset=4
                      local.tee 2
                      i32.add
                      i32.eq
                      br_if 2 (;@7;)
                      local.get 0
                      i32.load offset=8
                      local.tee 0
                      br_if 0 (;@9;)
                    end
                    br 2 (;@6;)
                  end
                  i32.const 3596
                  i32.load
                  local.tee 0
                  i32.const 0
                  local.get 0
                  local.get 1
                  i32.le_u
                  select
                  i32.eqz
                  if  ;; label = @8
                    i32.const 3596
                    local.get 1
                    i32.store
                  end
                  i32.const 0
                  local.set 0
                  i32.const 4032
                  local.get 7
                  i32.store
                  i32.const 4028
                  local.get 1
                  i32.store
                  i32.const 3612
                  i32.const -1
                  i32.store
                  i32.const 3616
                  i32.const 4052
                  i32.load
                  i32.store
                  i32.const 4040
                  i32.const 0
                  i32.store
                  loop  ;; label = @8
                    local.get 0
                    i32.const 3
                    i32.shl
                    local.tee 4
                    i32.const 3628
                    i32.add
                    local.get 4
                    i32.const 3620
                    i32.add
                    local.tee 2
                    i32.store
                    local.get 4
                    i32.const 3632
                    i32.add
                    local.get 2
                    i32.store
                    local.get 0
                    i32.const 1
                    i32.add
                    local.tee 0
                    i32.const 32
                    i32.ne
                    br_if 0 (;@8;)
                  end
                  i32.const 3604
                  local.get 1
                  i32.const -8
                  local.get 1
                  i32.sub
                  i32.const 7
                  i32.and
                  i32.const 0
                  local.get 1
                  i32.const 8
                  i32.add
                  i32.const 7
                  i32.and
                  select
                  local.tee 0
                  i32.add
                  local.tee 2
                  i32.store
                  i32.const 3592
                  local.get 7
                  local.get 0
                  i32.sub
                  i32.const 40
                  i32.sub
                  local.tee 0
                  i32.store
                  local.get 2
                  local.get 0
                  i32.const 1
                  i32.or
                  i32.store offset=4
                  local.get 1
                  local.get 7
                  i32.add
                  i32.const 36
                  i32.sub
                  i32.const 40
                  i32.store
                  i32.const 3608
                  i32.const 4068
                  i32.load
                  i32.store
                  br 2 (;@5;)
                end
                local.get 0
                i32.load8_u offset=12
                i32.const 8
                i32.and
                br_if 0 (;@6;)
                local.get 4
                local.get 5
                i32.gt_u
                br_if 0 (;@6;)
                local.get 1
                local.get 5
                i32.le_u
                br_if 0 (;@6;)
                local.get 0
                local.get 2
                local.get 7
                i32.add
                i32.store offset=4
                i32.const 3604
                local.get 5
                i32.const -8
                local.get 5
                i32.sub
                i32.const 7
                i32.and
                i32.const 0
                local.get 5
                i32.const 8
                i32.add
                i32.const 7
                i32.and
                select
                local.tee 0
                i32.add
                local.tee 2
                i32.store
                i32.const 3592
                i32.const 3592
                i32.load
                local.get 7
                i32.add
                local.tee 1
                local.get 0
                i32.sub
                local.tee 0
                i32.store
                local.get 2
                local.get 0
                i32.const 1
                i32.or
                i32.store offset=4
                local.get 1
                local.get 5
                i32.add
                i32.const 40
                i32.store offset=4
                i32.const 3608
                i32.const 4068
                i32.load
                i32.store
                br 1 (;@5;)
              end
              i32.const 3596
              i32.load
              local.get 1
              i32.gt_u
              if  ;; label = @6
                i32.const 3596
                local.get 1
                i32.store
              end
              local.get 1
              local.get 7
              i32.add
              local.set 3
              i32.const 4028
              local.set 0
              block  ;; label = @6
                block  ;; label = @7
                  block  ;; label = @8
                    block  ;; label = @9
                      block  ;; label = @10
                        block  ;; label = @11
                          loop  ;; label = @12
                            local.get 3
                            local.get 0
                            i32.load
                            i32.ne
                            if  ;; label = @13
                              local.get 0
                              i32.load offset=8
                              local.tee 0
                              br_if 1 (;@12;)
                              br 2 (;@11;)
                            end
                          end
                          local.get 0
                          i32.load8_u offset=12
                          i32.const 8
                          i32.and
                          i32.eqz
                          br_if 1 (;@10;)
                        end
                        i32.const 4028
                        local.set 0
                        loop  ;; label = @11
                          local.get 5
                          local.get 0
                          i32.load
                          local.tee 2
                          i32.ge_u
                          if  ;; label = @12
                            local.get 2
                            local.get 0
                            i32.load offset=4
                            i32.add
                            local.tee 4
                            local.get 5
                            i32.gt_u
                            br_if 3 (;@9;)
                          end
                          local.get 0
                          i32.load offset=8
                          local.set 0
                          br 0 (;@11;)
                        end
                        unreachable
                      end
                      local.get 0
                      local.get 1
                      i32.store
                      local.get 0
                      local.get 0
                      i32.load offset=4
                      local.get 7
                      i32.add
                      i32.store offset=4
                      local.get 1
                      i32.const -8
                      local.get 1
                      i32.sub
                      i32.const 7
                      i32.and
                      i32.const 0
                      local.get 1
                      i32.const 8
                      i32.add
                      i32.const 7
                      i32.and
                      select
                      i32.add
                      local.tee 9
                      local.get 6
                      i32.const 3
                      i32.or
                      i32.store offset=4
                      local.get 3
                      i32.const -8
                      local.get 3
                      i32.sub
                      i32.const 7
                      i32.and
                      i32.const 0
                      local.get 3
                      i32.const 8
                      i32.add
                      i32.const 7
                      i32.and
                      select
                      i32.add
                      local.tee 3
                      local.get 6
                      local.get 9
                      i32.add
                      local.tee 8
                      i32.sub
                      local.set 2
                      local.get 3
                      local.get 5
                      i32.eq
                      if  ;; label = @10
                        i32.const 3604
                        local.get 8
                        i32.store
                        i32.const 3592
                        i32.const 3592
                        i32.load
                        local.get 2
                        i32.add
                        local.tee 0
                        i32.store
                        local.get 8
                        local.get 0
                        i32.const 1
                        i32.or
                        i32.store offset=4
                        br 3 (;@7;)
                      end
                      local.get 3
                      i32.const 3600
                      i32.load
                      i32.eq
                      if  ;; label = @10
                        i32.const 3600
                        local.get 8
                        i32.store
                        i32.const 3588
                        i32.const 3588
                        i32.load
                        local.get 2
                        i32.add
                        local.tee 0
                        i32.store
                        local.get 8
                        local.get 0
                        i32.const 1
                        i32.or
                        i32.store offset=4
                        local.get 0
                        local.get 8
                        i32.add
                        local.get 0
                        i32.store
                        br 3 (;@7;)
                      end
                      local.get 3
                      i32.load offset=4
                      local.tee 0
                      i32.const 3
                      i32.and
                      i32.const 1
                      i32.eq
                      if  ;; label = @10
                        local.get 0
                        i32.const -8
                        i32.and
                        local.set 7
                        block  ;; label = @11
                          local.get 0
                          i32.const 255
                          i32.le_u
                          if  ;; label = @12
                            local.get 3
                            i32.load offset=8
                            local.tee 4
                            local.get 0
                            i32.const 3
                            i32.shr_u
                            local.tee 0
                            i32.const 3
                            i32.shl
                            i32.const 3620
                            i32.add
                            i32.eq
                            drop
                            local.get 4
                            local.get 3
                            i32.load offset=12
                            local.tee 1
                            i32.eq
                            if  ;; label = @13
                              i32.const 3580
                              i32.const 3580
                              i32.load
                              i32.const -2
                              local.get 0
                              i32.rotl
                              i32.and
                              i32.store
                              br 2 (;@11;)
                            end
                            local.get 4
                            local.get 1
                            i32.store offset=12
                            local.get 1
                            local.get 4
                            i32.store offset=8
                            br 1 (;@11;)
                          end
                          local.get 3
                          i32.load offset=24
                          local.set 6
                          block  ;; label = @12
                            local.get 3
                            local.get 3
                            i32.load offset=12
                            local.tee 1
                            i32.ne
                            if  ;; label = @13
                              local.get 3
                              i32.load offset=8
                              local.tee 0
                              local.get 1
                              i32.store offset=12
                              local.get 1
                              local.get 0
                              i32.store offset=8
                              br 1 (;@12;)
                            end
                            block  ;; label = @13
                              local.get 3
                              i32.const 20
                              i32.add
                              local.tee 0
                              i32.load
                              local.tee 4
                              br_if 0 (;@13;)
                              local.get 3
                              i32.const 16
                              i32.add
                              local.tee 0
                              i32.load
                              local.tee 4
                              br_if 0 (;@13;)
                              i32.const 0
                              local.set 1
                              br 1 (;@12;)
                            end
                            loop  ;; label = @13
                              local.get 0
                              local.set 5
                              local.get 4
                              local.tee 1
                              i32.const 20
                              i32.add
                              local.tee 0
                              i32.load
                              local.tee 4
                              br_if 0 (;@13;)
                              local.get 1
                              i32.const 16
                              i32.add
                              local.set 0
                              local.get 1
                              i32.load offset=16
                              local.tee 4
                              br_if 0 (;@13;)
                            end
                            local.get 5
                            i32.const 0
                            i32.store
                          end
                          local.get 6
                          i32.eqz
                          br_if 0 (;@11;)
                          block  ;; label = @12
                            local.get 3
                            local.get 3
                            i32.load offset=28
                            local.tee 4
                            i32.const 2
                            i32.shl
                            i32.const 3884
                            i32.add
                            local.tee 0
                            i32.load
                            i32.eq
                            if  ;; label = @13
                              local.get 0
                              local.get 1
                              i32.store
                              local.get 1
                              br_if 1 (;@12;)
                              i32.const 3584
                              i32.const 3584
                              i32.load
                              i32.const -2
                              local.get 4
                              i32.rotl
                              i32.and
                              i32.store
                              br 2 (;@11;)
                            end
                            local.get 6
                            i32.const 16
                            i32.const 20
                            local.get 6
                            i32.load offset=16
                            local.get 3
                            i32.eq
                            select
                            i32.add
                            local.get 1
                            i32.store
                            local.get 1
                            i32.eqz
                            br_if 1 (;@11;)
                          end
                          local.get 1
                          local.get 6
                          i32.store offset=24
                          local.get 3
                          i32.load offset=16
                          local.tee 0
                          if  ;; label = @12
                            local.get 1
                            local.get 0
                            i32.store offset=16
                            local.get 0
                            local.get 1
                            i32.store offset=24
                          end
                          local.get 3
                          i32.load offset=20
                          local.tee 0
                          i32.eqz
                          br_if 0 (;@11;)
                          local.get 1
                          local.get 0
                          i32.store offset=20
                          local.get 0
                          local.get 1
                          i32.store offset=24
                        end
                        local.get 3
                        local.get 7
                        i32.add
                        local.set 3
                        local.get 2
                        local.get 7
                        i32.add
                        local.set 2
                      end
                      local.get 3
                      local.get 3
                      i32.load offset=4
                      i32.const -2
                      i32.and
                      i32.store offset=4
                      local.get 8
                      local.get 2
                      i32.const 1
                      i32.or
                      i32.store offset=4
                      local.get 2
                      local.get 8
                      i32.add
                      local.get 2
                      i32.store
                      local.get 2
                      i32.const 255
                      i32.le_u
                      if  ;; label = @10
                        local.get 2
                        i32.const 3
                        i32.shr_u
                        local.tee 0
                        i32.const 3
                        i32.shl
                        i32.const 3620
                        i32.add
                        local.set 2
                        block (result i32)  ;; label = @11
                          i32.const 3580
                          i32.load
                          local.tee 1
                          i32.const 1
                          local.get 0
                          i32.shl
                          local.tee 0
                          i32.and
                          i32.eqz
                          if  ;; label = @12
                            i32.const 3580
                            local.get 0
                            local.get 1
                            i32.or
                            i32.store
                            local.get 2
                            br 1 (;@11;)
                          end
                          local.get 2
                          i32.load offset=8
                        end
                        local.set 0
                        local.get 2
                        local.get 8
                        i32.store offset=8
                        local.get 0
                        local.get 8
                        i32.store offset=12
                        local.get 8
                        local.get 2
                        i32.store offset=12
                        local.get 8
                        local.get 0
                        i32.store offset=8
                        br 3 (;@7;)
                      end
                      i32.const 31
                      local.set 0
                      local.get 2
                      i32.const 16777215
                      i32.le_u
                      if  ;; label = @10
                        local.get 2
                        i32.const 8
                        i32.shr_u
                        local.tee 0
                        local.get 0
                        i32.const 1048320
                        i32.add
                        i32.const 16
                        i32.shr_u
                        i32.const 8
                        i32.and
                        local.tee 4
                        i32.shl
                        local.tee 0
                        local.get 0
                        i32.const 520192
                        i32.add
                        i32.const 16
                        i32.shr_u
                        i32.const 4
                        i32.and
                        local.tee 1
                        i32.shl
                        local.tee 0
                        local.get 0
                        i32.const 245760
                        i32.add
                        i32.const 16
                        i32.shr_u
                        i32.const 2
                        i32.and
                        local.tee 0
                        i32.shl
                        i32.const 15
                        i32.shr_u
                        local.get 1
                        local.get 4
                        i32.or
                        local.get 0
                        i32.or
                        i32.sub
                        local.tee 0
                        i32.const 1
                        i32.shl
                        local.get 2
                        local.get 0
                        i32.const 21
                        i32.add
                        i32.shr_u
                        i32.const 1
                        i32.and
                        i32.or
                        i32.const 28
                        i32.add
                        local.set 0
                      end
                      local.get 8
                      local.get 0
                      i32.store offset=28
                      local.get 8
                      i64.const 0
                      i64.store offset=16 align=4
                      local.get 0
                      i32.const 2
                      i32.shl
                      i32.const 3884
                      i32.add
                      local.set 3
                      block  ;; label = @10
                        i32.const 3584
                        i32.load
                        local.tee 4
                        i32.const 1
                        local.get 0
                        i32.shl
                        local.tee 1
                        i32.and
                        i32.eqz
                        if  ;; label = @11
                          i32.const 3584
                          local.get 1
                          local.get 4
                          i32.or
                          i32.store
                          local.get 3
                          local.get 8
                          i32.store
                          local.get 8
                          local.get 3
                          i32.store offset=24
                          br 1 (;@10;)
                        end
                        local.get 2
                        i32.const 0
                        i32.const 25
                        local.get 0
                        i32.const 1
                        i32.shr_u
                        i32.sub
                        local.get 0
                        i32.const 31
                        i32.eq
                        select
                        i32.shl
                        local.set 0
                        local.get 3
                        i32.load
                        local.set 1
                        loop  ;; label = @11
                          local.get 1
                          local.tee 4
                          i32.load offset=4
                          i32.const -8
                          i32.and
                          local.get 2
                          i32.eq
                          br_if 3 (;@8;)
                          local.get 0
                          i32.const 29
                          i32.shr_u
                          local.set 1
                          local.get 0
                          i32.const 1
                          i32.shl
                          local.set 0
                          local.get 4
                          local.get 1
                          i32.const 4
                          i32.and
                          i32.add
                          local.tee 3
                          i32.load offset=16
                          local.tee 1
                          br_if 0 (;@11;)
                        end
                        local.get 3
                        local.get 8
                        i32.store offset=16
                        local.get 8
                        local.get 4
                        i32.store offset=24
                      end
                      local.get 8
                      local.get 8
                      i32.store offset=12
                      local.get 8
                      local.get 8
                      i32.store offset=8
                      br 2 (;@7;)
                    end
                    i32.const 3604
                    local.get 1
                    i32.const -8
                    local.get 1
                    i32.sub
                    i32.const 7
                    i32.and
                    i32.const 0
                    local.get 1
                    i32.const 8
                    i32.add
                    i32.const 7
                    i32.and
                    select
                    local.tee 0
                    i32.add
                    local.tee 2
                    i32.store
                    i32.const 3592
                    local.get 7
                    local.get 0
                    i32.sub
                    i32.const 40
                    i32.sub
                    local.tee 0
                    i32.store
                    local.get 2
                    local.get 0
                    i32.const 1
                    i32.or
                    i32.store offset=4
                    local.get 3
                    i32.const 36
                    i32.sub
                    i32.const 40
                    i32.store
                    i32.const 3608
                    i32.const 4068
                    i32.load
                    i32.store
                    local.get 5
                    local.get 4
                    i32.const 39
                    local.get 4
                    i32.sub
                    i32.const 7
                    i32.and
                    i32.const 0
                    local.get 4
                    i32.const 39
                    i32.sub
                    i32.const 7
                    i32.and
                    select
                    i32.add
                    i32.const 47
                    i32.sub
                    local.tee 0
                    local.get 0
                    local.get 5
                    i32.const 16
                    i32.add
                    i32.lt_u
                    select
                    local.tee 2
                    i32.const 27
                    i32.store offset=4
                    local.get 2
                    i32.const 4036
                    i64.load align=4
                    i64.store offset=16 align=4
                    local.get 2
                    i32.const 4028
                    i64.load align=4
                    i64.store offset=8 align=4
                    i32.const 4036
                    local.get 2
                    i32.const 8
                    i32.add
                    i32.store
                    i32.const 4032
                    local.get 7
                    i32.store
                    i32.const 4028
                    local.get 1
                    i32.store
                    i32.const 4040
                    i32.const 0
                    i32.store
                    local.get 2
                    i32.const 24
                    i32.add
                    local.set 0
                    loop  ;; label = @9
                      local.get 0
                      i32.const 7
                      i32.store offset=4
                      local.get 0
                      i32.const 8
                      i32.add
                      local.set 1
                      local.get 0
                      i32.const 4
                      i32.add
                      local.set 0
                      local.get 1
                      local.get 4
                      i32.lt_u
                      br_if 0 (;@9;)
                    end
                    local.get 2
                    local.get 5
                    i32.eq
                    br_if 3 (;@5;)
                    local.get 2
                    local.get 2
                    i32.load offset=4
                    i32.const -2
                    i32.and
                    i32.store offset=4
                    local.get 5
                    local.get 2
                    local.get 5
                    i32.sub
                    local.tee 3
                    i32.const 1
                    i32.or
                    i32.store offset=4
                    local.get 2
                    local.get 3
                    i32.store
                    local.get 3
                    i32.const 255
                    i32.le_u
                    if  ;; label = @9
                      local.get 3
                      i32.const 3
                      i32.shr_u
                      local.tee 0
                      i32.const 3
                      i32.shl
                      i32.const 3620
                      i32.add
                      local.set 2
                      block (result i32)  ;; label = @10
                        i32.const 3580
                        i32.load
                        local.tee 1
                        i32.const 1
                        local.get 0
                        i32.shl
                        local.tee 0
                        i32.and
                        i32.eqz
                        if  ;; label = @11
                          i32.const 3580
                          local.get 0
                          local.get 1
                          i32.or
                          i32.store
                          local.get 2
                          br 1 (;@10;)
                        end
                        local.get 2
                        i32.load offset=8
                      end
                      local.set 0
                      local.get 2
                      local.get 5
                      i32.store offset=8
                      local.get 0
                      local.get 5
                      i32.store offset=12
                      local.get 5
                      local.get 2
                      i32.store offset=12
                      local.get 5
                      local.get 0
                      i32.store offset=8
                      br 4 (;@5;)
                    end
                    i32.const 31
                    local.set 0
                    local.get 5
                    i64.const 0
                    i64.store offset=16 align=4
                    local.get 3
                    i32.const 16777215
                    i32.le_u
                    if  ;; label = @9
                      local.get 3
                      i32.const 8
                      i32.shr_u
                      local.tee 0
                      local.get 0
                      i32.const 1048320
                      i32.add
                      i32.const 16
                      i32.shr_u
                      i32.const 8
                      i32.and
                      local.tee 2
                      i32.shl
                      local.tee 0
                      local.get 0
                      i32.const 520192
                      i32.add
                      i32.const 16
                      i32.shr_u
                      i32.const 4
                      i32.and
                      local.tee 1
                      i32.shl
                      local.tee 0
                      local.get 0
                      i32.const 245760
                      i32.add
                      i32.const 16
                      i32.shr_u
                      i32.const 2
                      i32.and
                      local.tee 0
                      i32.shl
                      i32.const 15
                      i32.shr_u
                      local.get 1
                      local.get 2
                      i32.or
                      local.get 0
                      i32.or
                      i32.sub
                      local.tee 0
                      i32.const 1
                      i32.shl
                      local.get 3
                      local.get 0
                      i32.const 21
                      i32.add
                      i32.shr_u
                      i32.const 1
                      i32.and
                      i32.or
                      i32.const 28
                      i32.add
                      local.set 0
                    end
                    local.get 5
                    local.get 0
                    i32.store offset=28
                    local.get 0
                    i32.const 2
                    i32.shl
                    i32.const 3884
                    i32.add
                    local.set 4
                    block  ;; label = @9
                      i32.const 3584
                      i32.load
                      local.tee 2
                      i32.const 1
                      local.get 0
                      i32.shl
                      local.tee 1
                      i32.and
                      i32.eqz
                      if  ;; label = @10
                        i32.const 3584
                        local.get 1
                        local.get 2
                        i32.or
                        i32.store
                        local.get 4
                        local.get 5
                        i32.store
                        local.get 5
                        local.get 4
                        i32.store offset=24
                        br 1 (;@9;)
                      end
                      local.get 3
                      i32.const 0
                      i32.const 25
                      local.get 0
                      i32.const 1
                      i32.shr_u
                      i32.sub
                      local.get 0
                      i32.const 31
                      i32.eq
                      select
                      i32.shl
                      local.set 0
                      local.get 4
                      i32.load
                      local.set 1
                      loop  ;; label = @10
                        local.get 1
                        local.tee 2
                        i32.load offset=4
                        i32.const -8
                        i32.and
                        local.get 3
                        i32.eq
                        br_if 4 (;@6;)
                        local.get 0
                        i32.const 29
                        i32.shr_u
                        local.set 1
                        local.get 0
                        i32.const 1
                        i32.shl
                        local.set 0
                        local.get 2
                        local.get 1
                        i32.const 4
                        i32.and
                        i32.add
                        local.tee 4
                        i32.load offset=16
                        local.tee 1
                        br_if 0 (;@10;)
                      end
                      local.get 4
                      local.get 5
                      i32.store offset=16
                      local.get 5
                      local.get 2
                      i32.store offset=24
                    end
                    local.get 5
                    local.get 5
                    i32.store offset=12
                    local.get 5
                    local.get 5
                    i32.store offset=8
                    br 3 (;@5;)
                  end
                  local.get 4
                  i32.load offset=8
                  local.tee 0
                  local.get 8
                  i32.store offset=12
                  local.get 4
                  local.get 8
                  i32.store offset=8
                  local.get 8
                  i32.const 0
                  i32.store offset=24
                  local.get 8
                  local.get 4
                  i32.store offset=12
                  local.get 8
                  local.get 0
                  i32.store offset=8
                end
                local.get 9
                i32.const 8
                i32.add
                local.set 0
                br 5 (;@1;)
              end
              local.get 2
              i32.load offset=8
              local.tee 0
              local.get 5
              i32.store offset=12
              local.get 2
              local.get 5
              i32.store offset=8
              local.get 5
              i32.const 0
              i32.store offset=24
              local.get 5
              local.get 2
              i32.store offset=12
              local.get 5
              local.get 0
              i32.store offset=8
            end
            i32.const 3592
            i32.load
            local.tee 0
            local.get 6
            i32.le_u
            br_if 0 (;@4;)
            i32.const 3592
            local.get 0
            local.get 6
            i32.sub
            local.tee 1
            i32.store
            i32.const 3604
            i32.const 3604
            i32.load
            local.tee 2
            local.get 6
            i32.add
            local.tee 0
            i32.store
            local.get 0
            local.get 1
            i32.const 1
            i32.or
            i32.store offset=4
            local.get 2
            local.get 6
            i32.const 3
            i32.or
            i32.store offset=4
            local.get 2
            i32.const 8
            i32.add
            local.set 0
            br 3 (;@1;)
          end
          i32.const 3576
          i32.const 48
          i32.store
          i32.const 0
          local.set 0
          br 2 (;@1;)
        end
        block  ;; label = @3
          local.get 7
          i32.eqz
          br_if 0 (;@3;)
          block  ;; label = @4
            local.get 3
            i32.load offset=28
            local.tee 2
            i32.const 2
            i32.shl
            i32.const 3884
            i32.add
            local.tee 0
            i32.load
            local.get 3
            i32.eq
            if  ;; label = @5
              local.get 0
              local.get 1
              i32.store
              local.get 1
              br_if 1 (;@4;)
              i32.const 3584
              local.get 9
              i32.const -2
              local.get 2
              i32.rotl
              i32.and
              local.tee 9
              i32.store
              br 2 (;@3;)
            end
            local.get 7
            i32.const 16
            i32.const 20
            local.get 7
            i32.load offset=16
            local.get 3
            i32.eq
            select
            i32.add
            local.get 1
            i32.store
            local.get 1
            i32.eqz
            br_if 1 (;@3;)
          end
          local.get 1
          local.get 7
          i32.store offset=24
          local.get 3
          i32.load offset=16
          local.tee 0
          if  ;; label = @4
            local.get 1
            local.get 0
            i32.store offset=16
            local.get 0
            local.get 1
            i32.store offset=24
          end
          local.get 3
          i32.load offset=20
          local.tee 0
          i32.eqz
          br_if 0 (;@3;)
          local.get 1
          local.get 0
          i32.store offset=20
          local.get 0
          local.get 1
          i32.store offset=24
        end
        block  ;; label = @3
          local.get 4
          i32.const 15
          i32.le_u
          if  ;; label = @4
            local.get 3
            local.get 4
            local.get 6
            i32.add
            local.tee 0
            i32.const 3
            i32.or
            i32.store offset=4
            local.get 0
            local.get 3
            i32.add
            local.tee 0
            local.get 0
            i32.load offset=4
            i32.const 1
            i32.or
            i32.store offset=4
            br 1 (;@3;)
          end
          local.get 3
          local.get 6
          i32.const 3
          i32.or
          i32.store offset=4
          local.get 3
          local.get 6
          i32.add
          local.tee 5
          local.get 4
          i32.const 1
          i32.or
          i32.store offset=4
          local.get 4
          local.get 5
          i32.add
          local.get 4
          i32.store
          local.get 4
          i32.const 255
          i32.le_u
          if  ;; label = @4
            local.get 4
            i32.const 3
            i32.shr_u
            local.tee 0
            i32.const 3
            i32.shl
            i32.const 3620
            i32.add
            local.set 2
            block (result i32)  ;; label = @5
              i32.const 3580
              i32.load
              local.tee 1
              i32.const 1
              local.get 0
              i32.shl
              local.tee 0
              i32.and
              i32.eqz
              if  ;; label = @6
                i32.const 3580
                local.get 0
                local.get 1
                i32.or
                i32.store
                local.get 2
                br 1 (;@5;)
              end
              local.get 2
              i32.load offset=8
            end
            local.set 0
            local.get 2
            local.get 5
            i32.store offset=8
            local.get 0
            local.get 5
            i32.store offset=12
            local.get 5
            local.get 2
            i32.store offset=12
            local.get 5
            local.get 0
            i32.store offset=8
            br 1 (;@3;)
          end
          i32.const 31
          local.set 0
          local.get 4
          i32.const 16777215
          i32.le_u
          if  ;; label = @4
            local.get 4
            i32.const 8
            i32.shr_u
            local.tee 0
            local.get 0
            i32.const 1048320
            i32.add
            i32.const 16
            i32.shr_u
            i32.const 8
            i32.and
            local.tee 2
            i32.shl
            local.tee 0
            local.get 0
            i32.const 520192
            i32.add
            i32.const 16
            i32.shr_u
            i32.const 4
            i32.and
            local.tee 1
            i32.shl
            local.tee 0
            local.get 0
            i32.const 245760
            i32.add
            i32.const 16
            i32.shr_u
            i32.const 2
            i32.and
            local.tee 0
            i32.shl
            i32.const 15
            i32.shr_u
            local.get 1
            local.get 2
            i32.or
            local.get 0
            i32.or
            i32.sub
            local.tee 0
            i32.const 1
            i32.shl
            local.get 4
            local.get 0
            i32.const 21
            i32.add
            i32.shr_u
            i32.const 1
            i32.and
            i32.or
            i32.const 28
            i32.add
            local.set 0
          end
          local.get 5
          local.get 0
          i32.store offset=28
          local.get 5
          i64.const 0
          i64.store offset=16 align=4
          local.get 0
          i32.const 2
          i32.shl
          i32.const 3884
          i32.add
          local.set 1
          block  ;; label = @4
            block  ;; label = @5
              local.get 9
              i32.const 1
              local.get 0
              i32.shl
              local.tee 2
              i32.and
              i32.eqz
              if  ;; label = @6
                i32.const 3584
                local.get 2
                local.get 9
                i32.or
                i32.store
                local.get 1
                local.get 5
                i32.store
                br 1 (;@5;)
              end
              local.get 4
              i32.const 0
              i32.const 25
              local.get 0
              i32.const 1
              i32.shr_u
              i32.sub
              local.get 0
              i32.const 31
              i32.eq
              select
              i32.shl
              local.set 0
              local.get 1
              i32.load
              local.set 6
              loop  ;; label = @6
                local.get 6
                local.tee 1
                i32.load offset=4
                i32.const -8
                i32.and
                local.get 4
                i32.eq
                br_if 2 (;@4;)
                local.get 0
                i32.const 29
                i32.shr_u
                local.set 2
                local.get 0
                i32.const 1
                i32.shl
                local.set 0
                local.get 1
                local.get 2
                i32.const 4
                i32.and
                i32.add
                local.tee 2
                i32.load offset=16
                local.tee 6
                br_if 0 (;@6;)
              end
              local.get 2
              local.get 5
              i32.store offset=16
            end
            local.get 5
            local.get 1
            i32.store offset=24
            local.get 5
            local.get 5
            i32.store offset=12
            local.get 5
            local.get 5
            i32.store offset=8
            br 1 (;@3;)
          end
          local.get 1
          i32.load offset=8
          local.tee 0
          local.get 5
          i32.store offset=12
          local.get 1
          local.get 5
          i32.store offset=8
          local.get 5
          i32.const 0
          i32.store offset=24
          local.get 5
          local.get 1
          i32.store offset=12
          local.get 5
          local.get 0
          i32.store offset=8
        end
        local.get 3
        i32.const 8
        i32.add
        local.set 0
        br 1 (;@1;)
      end
      block  ;; label = @2
        local.get 8
        i32.eqz
        br_if 0 (;@2;)
        block  ;; label = @3
          local.get 1
          i32.load offset=28
          local.tee 2
          i32.const 2
          i32.shl
          i32.const 3884
          i32.add
          local.tee 0
          i32.load
          local.get 1
          i32.eq
          if  ;; label = @4
            local.get 0
            local.get 3
            i32.store
            local.get 3
            br_if 1 (;@3;)
            i32.const 3584
            local.get 9
            i32.const -2
            local.get 2
            i32.rotl
            i32.and
            i32.store
            br 2 (;@2;)
          end
          local.get 8
          i32.const 16
          i32.const 20
          local.get 8
          i32.load offset=16
          local.get 1
          i32.eq
          select
          i32.add
          local.get 3
          i32.store
          local.get 3
          i32.eqz
          br_if 1 (;@2;)
        end
        local.get 3
        local.get 8
        i32.store offset=24
        local.get 1
        i32.load offset=16
        local.tee 0
        if  ;; label = @3
          local.get 3
          local.get 0
          i32.store offset=16
          local.get 0
          local.get 3
          i32.store offset=24
        end
        local.get 1
        i32.load offset=20
        local.tee 0
        i32.eqz
        br_if 0 (;@2;)
        local.get 3
        local.get 0
        i32.store offset=20
        local.get 0
        local.get 3
        i32.store offset=24
      end
      block  ;; label = @2
        local.get 4
        i32.const 15
        i32.le_u
        if  ;; label = @3
          local.get 1
          local.get 4
          local.get 6
          i32.add
          local.tee 0
          i32.const 3
          i32.or
          i32.store offset=4
          local.get 0
          local.get 1
          i32.add
          local.tee 0
          local.get 0
          i32.load offset=4
          i32.const 1
          i32.or
          i32.store offset=4
          br 1 (;@2;)
        end
        local.get 1
        local.get 6
        i32.const 3
        i32.or
        i32.store offset=4
        local.get 1
        local.get 6
        i32.add
        local.tee 2
        local.get 4
        i32.const 1
        i32.or
        i32.store offset=4
        local.get 2
        local.get 4
        i32.add
        local.get 4
        i32.store
        local.get 10
        if  ;; label = @3
          local.get 10
          i32.const 3
          i32.shr_u
          local.tee 0
          i32.const 3
          i32.shl
          i32.const 3620
          i32.add
          local.set 5
          i32.const 3600
          i32.load
          local.set 3
          block (result i32)  ;; label = @4
            i32.const 1
            local.get 0
            i32.shl
            local.tee 0
            local.get 7
            i32.and
            i32.eqz
            if  ;; label = @5
              i32.const 3580
              local.get 0
              local.get 7
              i32.or
              i32.store
              local.get 5
              br 1 (;@4;)
            end
            local.get 5
            i32.load offset=8
          end
          local.set 0
          local.get 5
          local.get 3
          i32.store offset=8
          local.get 0
          local.get 3
          i32.store offset=12
          local.get 3
          local.get 5
          i32.store offset=12
          local.get 3
          local.get 0
          i32.store offset=8
        end
        i32.const 3600
        local.get 2
        i32.store
        i32.const 3588
        local.get 4
        i32.store
      end
      local.get 1
      i32.const 8
      i32.add
      local.set 0
    end
    local.get 11
    i32.const 16
    i32.add
    global.set 0
    local.get 0)
  (func (;16;) (type 1) (param i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    block  ;; label = @1
      local.get 0
      i32.eqz
      br_if 0 (;@1;)
      local.get 0
      i32.const 8
      i32.sub
      local.tee 3
      local.get 0
      i32.const 4
      i32.sub
      i32.load
      local.tee 1
      i32.const -8
      i32.and
      local.tee 0
      i32.add
      local.set 5
      block  ;; label = @2
        local.get 1
        i32.const 1
        i32.and
        br_if 0 (;@2;)
        local.get 1
        i32.const 3
        i32.and
        i32.eqz
        br_if 1 (;@1;)
        local.get 3
        local.get 3
        i32.load
        local.tee 1
        i32.sub
        local.tee 3
        i32.const 3596
        i32.load
        i32.lt_u
        br_if 1 (;@1;)
        local.get 0
        local.get 1
        i32.add
        local.set 0
        local.get 3
        i32.const 3600
        i32.load
        i32.ne
        if  ;; label = @3
          local.get 1
          i32.const 255
          i32.le_u
          if  ;; label = @4
            local.get 3
            i32.load offset=8
            local.tee 2
            local.get 1
            i32.const 3
            i32.shr_u
            local.tee 4
            i32.const 3
            i32.shl
            i32.const 3620
            i32.add
            i32.eq
            drop
            local.get 2
            local.get 3
            i32.load offset=12
            local.tee 1
            i32.eq
            if  ;; label = @5
              i32.const 3580
              i32.const 3580
              i32.load
              i32.const -2
              local.get 4
              i32.rotl
              i32.and
              i32.store
              br 3 (;@2;)
            end
            local.get 2
            local.get 1
            i32.store offset=12
            local.get 1
            local.get 2
            i32.store offset=8
            br 2 (;@2;)
          end
          local.get 3
          i32.load offset=24
          local.set 6
          block  ;; label = @4
            local.get 3
            local.get 3
            i32.load offset=12
            local.tee 1
            i32.ne
            if  ;; label = @5
              local.get 3
              i32.load offset=8
              local.tee 2
              local.get 1
              i32.store offset=12
              local.get 1
              local.get 2
              i32.store offset=8
              br 1 (;@4;)
            end
            block  ;; label = @5
              local.get 3
              i32.const 20
              i32.add
              local.tee 2
              i32.load
              local.tee 4
              br_if 0 (;@5;)
              local.get 3
              i32.const 16
              i32.add
              local.tee 2
              i32.load
              local.tee 4
              br_if 0 (;@5;)
              i32.const 0
              local.set 1
              br 1 (;@4;)
            end
            loop  ;; label = @5
              local.get 2
              local.set 7
              local.get 4
              local.tee 1
              i32.const 20
              i32.add
              local.tee 2
              i32.load
              local.tee 4
              br_if 0 (;@5;)
              local.get 1
              i32.const 16
              i32.add
              local.set 2
              local.get 1
              i32.load offset=16
              local.tee 4
              br_if 0 (;@5;)
            end
            local.get 7
            i32.const 0
            i32.store
          end
          local.get 6
          i32.eqz
          br_if 1 (;@2;)
          block  ;; label = @4
            local.get 3
            local.get 3
            i32.load offset=28
            local.tee 2
            i32.const 2
            i32.shl
            i32.const 3884
            i32.add
            local.tee 4
            i32.load
            i32.eq
            if  ;; label = @5
              local.get 4
              local.get 1
              i32.store
              local.get 1
              br_if 1 (;@4;)
              i32.const 3584
              i32.const 3584
              i32.load
              i32.const -2
              local.get 2
              i32.rotl
              i32.and
              i32.store
              br 3 (;@2;)
            end
            local.get 6
            i32.const 16
            i32.const 20
            local.get 6
            i32.load offset=16
            local.get 3
            i32.eq
            select
            i32.add
            local.get 1
            i32.store
            local.get 1
            i32.eqz
            br_if 2 (;@2;)
          end
          local.get 1
          local.get 6
          i32.store offset=24
          local.get 3
          i32.load offset=16
          local.tee 2
          if  ;; label = @4
            local.get 1
            local.get 2
            i32.store offset=16
            local.get 2
            local.get 1
            i32.store offset=24
          end
          local.get 3
          i32.load offset=20
          local.tee 2
          i32.eqz
          br_if 1 (;@2;)
          local.get 1
          local.get 2
          i32.store offset=20
          local.get 2
          local.get 1
          i32.store offset=24
          br 1 (;@2;)
        end
        local.get 5
        i32.load offset=4
        local.tee 1
        i32.const 3
        i32.and
        i32.const 3
        i32.ne
        br_if 0 (;@2;)
        i32.const 3588
        local.get 0
        i32.store
        local.get 5
        local.get 1
        i32.const -2
        i32.and
        i32.store offset=4
        local.get 3
        local.get 0
        i32.const 1
        i32.or
        i32.store offset=4
        local.get 0
        local.get 3
        i32.add
        local.get 0
        i32.store
        return
      end
      local.get 3
      local.get 5
      i32.ge_u
      br_if 0 (;@1;)
      local.get 5
      i32.load offset=4
      local.tee 1
      i32.const 1
      i32.and
      i32.eqz
      br_if 0 (;@1;)
      block  ;; label = @2
        local.get 1
        i32.const 2
        i32.and
        i32.eqz
        if  ;; label = @3
          local.get 5
          i32.const 3604
          i32.load
          i32.eq
          if  ;; label = @4
            i32.const 3604
            local.get 3
            i32.store
            i32.const 3592
            i32.const 3592
            i32.load
            local.get 0
            i32.add
            local.tee 0
            i32.store
            local.get 3
            local.get 0
            i32.const 1
            i32.or
            i32.store offset=4
            local.get 3
            i32.const 3600
            i32.load
            i32.ne
            br_if 3 (;@1;)
            i32.const 3588
            i32.const 0
            i32.store
            i32.const 3600
            i32.const 0
            i32.store
            return
          end
          local.get 5
          i32.const 3600
          i32.load
          i32.eq
          if  ;; label = @4
            i32.const 3600
            local.get 3
            i32.store
            i32.const 3588
            i32.const 3588
            i32.load
            local.get 0
            i32.add
            local.tee 0
            i32.store
            local.get 3
            local.get 0
            i32.const 1
            i32.or
            i32.store offset=4
            local.get 0
            local.get 3
            i32.add
            local.get 0
            i32.store
            return
          end
          local.get 1
          i32.const -8
          i32.and
          local.get 0
          i32.add
          local.set 0
          block  ;; label = @4
            local.get 1
            i32.const 255
            i32.le_u
            if  ;; label = @5
              local.get 5
              i32.load offset=8
              local.tee 2
              local.get 1
              i32.const 3
              i32.shr_u
              local.tee 4
              i32.const 3
              i32.shl
              i32.const 3620
              i32.add
              i32.eq
              drop
              local.get 2
              local.get 5
              i32.load offset=12
              local.tee 1
              i32.eq
              if  ;; label = @6
                i32.const 3580
                i32.const 3580
                i32.load
                i32.const -2
                local.get 4
                i32.rotl
                i32.and
                i32.store
                br 2 (;@4;)
              end
              local.get 2
              local.get 1
              i32.store offset=12
              local.get 1
              local.get 2
              i32.store offset=8
              br 1 (;@4;)
            end
            local.get 5
            i32.load offset=24
            local.set 6
            block  ;; label = @5
              local.get 5
              local.get 5
              i32.load offset=12
              local.tee 1
              i32.ne
              if  ;; label = @6
                local.get 5
                i32.load offset=8
                local.tee 2
                i32.const 3596
                i32.load
                i32.lt_u
                drop
                local.get 2
                local.get 1
                i32.store offset=12
                local.get 1
                local.get 2
                i32.store offset=8
                br 1 (;@5;)
              end
              block  ;; label = @6
                local.get 5
                i32.const 20
                i32.add
                local.tee 2
                i32.load
                local.tee 4
                br_if 0 (;@6;)
                local.get 5
                i32.const 16
                i32.add
                local.tee 2
                i32.load
                local.tee 4
                br_if 0 (;@6;)
                i32.const 0
                local.set 1
                br 1 (;@5;)
              end
              loop  ;; label = @6
                local.get 2
                local.set 7
                local.get 4
                local.tee 1
                i32.const 20
                i32.add
                local.tee 2
                i32.load
                local.tee 4
                br_if 0 (;@6;)
                local.get 1
                i32.const 16
                i32.add
                local.set 2
                local.get 1
                i32.load offset=16
                local.tee 4
                br_if 0 (;@6;)
              end
              local.get 7
              i32.const 0
              i32.store
            end
            local.get 6
            i32.eqz
            br_if 0 (;@4;)
            block  ;; label = @5
              local.get 5
              local.get 5
              i32.load offset=28
              local.tee 2
              i32.const 2
              i32.shl
              i32.const 3884
              i32.add
              local.tee 4
              i32.load
              i32.eq
              if  ;; label = @6
                local.get 4
                local.get 1
                i32.store
                local.get 1
                br_if 1 (;@5;)
                i32.const 3584
                i32.const 3584
                i32.load
                i32.const -2
                local.get 2
                i32.rotl
                i32.and
                i32.store
                br 2 (;@4;)
              end
              local.get 6
              i32.const 16
              i32.const 20
              local.get 6
              i32.load offset=16
              local.get 5
              i32.eq
              select
              i32.add
              local.get 1
              i32.store
              local.get 1
              i32.eqz
              br_if 1 (;@4;)
            end
            local.get 1
            local.get 6
            i32.store offset=24
            local.get 5
            i32.load offset=16
            local.tee 2
            if  ;; label = @5
              local.get 1
              local.get 2
              i32.store offset=16
              local.get 2
              local.get 1
              i32.store offset=24
            end
            local.get 5
            i32.load offset=20
            local.tee 2
            i32.eqz
            br_if 0 (;@4;)
            local.get 1
            local.get 2
            i32.store offset=20
            local.get 2
            local.get 1
            i32.store offset=24
          end
          local.get 3
          local.get 0
          i32.const 1
          i32.or
          i32.store offset=4
          local.get 0
          local.get 3
          i32.add
          local.get 0
          i32.store
          local.get 3
          i32.const 3600
          i32.load
          i32.ne
          br_if 1 (;@2;)
          i32.const 3588
          local.get 0
          i32.store
          return
        end
        local.get 5
        local.get 1
        i32.const -2
        i32.and
        i32.store offset=4
        local.get 3
        local.get 0
        i32.const 1
        i32.or
        i32.store offset=4
        local.get 0
        local.get 3
        i32.add
        local.get 0
        i32.store
      end
      local.get 0
      i32.const 255
      i32.le_u
      if  ;; label = @2
        local.get 0
        i32.const 3
        i32.shr_u
        local.tee 1
        i32.const 3
        i32.shl
        i32.const 3620
        i32.add
        local.set 0
        block (result i32)  ;; label = @3
          i32.const 3580
          i32.load
          local.tee 2
          i32.const 1
          local.get 1
          i32.shl
          local.tee 1
          i32.and
          i32.eqz
          if  ;; label = @4
            i32.const 3580
            local.get 1
            local.get 2
            i32.or
            i32.store
            local.get 0
            br 1 (;@3;)
          end
          local.get 0
          i32.load offset=8
        end
        local.set 2
        local.get 0
        local.get 3
        i32.store offset=8
        local.get 2
        local.get 3
        i32.store offset=12
        local.get 3
        local.get 0
        i32.store offset=12
        local.get 3
        local.get 2
        i32.store offset=8
        return
      end
      i32.const 31
      local.set 2
      local.get 3
      i64.const 0
      i64.store offset=16 align=4
      local.get 0
      i32.const 16777215
      i32.le_u
      if  ;; label = @2
        local.get 0
        i32.const 8
        i32.shr_u
        local.tee 1
        local.get 1
        i32.const 1048320
        i32.add
        i32.const 16
        i32.shr_u
        i32.const 8
        i32.and
        local.tee 1
        i32.shl
        local.tee 2
        local.get 2
        i32.const 520192
        i32.add
        i32.const 16
        i32.shr_u
        i32.const 4
        i32.and
        local.tee 2
        i32.shl
        local.tee 4
        local.get 4
        i32.const 245760
        i32.add
        i32.const 16
        i32.shr_u
        i32.const 2
        i32.and
        local.tee 4
        i32.shl
        i32.const 15
        i32.shr_u
        local.get 1
        local.get 2
        i32.or
        local.get 4
        i32.or
        i32.sub
        local.tee 1
        i32.const 1
        i32.shl
        local.get 0
        local.get 1
        i32.const 21
        i32.add
        i32.shr_u
        i32.const 1
        i32.and
        i32.or
        i32.const 28
        i32.add
        local.set 2
      end
      local.get 3
      local.get 2
      i32.store offset=28
      local.get 2
      i32.const 2
      i32.shl
      i32.const 3884
      i32.add
      local.set 1
      block  ;; label = @2
        block  ;; label = @3
          block  ;; label = @4
            i32.const 3584
            i32.load
            local.tee 4
            i32.const 1
            local.get 2
            i32.shl
            local.tee 7
            i32.and
            i32.eqz
            if  ;; label = @5
              i32.const 3584
              local.get 4
              local.get 7
              i32.or
              i32.store
              local.get 1
              local.get 3
              i32.store
              local.get 3
              local.get 1
              i32.store offset=24
              br 1 (;@4;)
            end
            local.get 0
            i32.const 0
            i32.const 25
            local.get 2
            i32.const 1
            i32.shr_u
            i32.sub
            local.get 2
            i32.const 31
            i32.eq
            select
            i32.shl
            local.set 2
            local.get 1
            i32.load
            local.set 1
            loop  ;; label = @5
              local.get 1
              local.tee 4
              i32.load offset=4
              i32.const -8
              i32.and
              local.get 0
              i32.eq
              br_if 2 (;@3;)
              local.get 2
              i32.const 29
              i32.shr_u
              local.set 1
              local.get 2
              i32.const 1
              i32.shl
              local.set 2
              local.get 4
              local.get 1
              i32.const 4
              i32.and
              i32.add
              local.tee 7
              i32.const 16
              i32.add
              i32.load
              local.tee 1
              br_if 0 (;@5;)
            end
            local.get 7
            local.get 3
            i32.store offset=16
            local.get 3
            local.get 4
            i32.store offset=24
          end
          local.get 3
          local.get 3
          i32.store offset=12
          local.get 3
          local.get 3
          i32.store offset=8
          br 1 (;@2;)
        end
        local.get 4
        i32.load offset=8
        local.tee 0
        local.get 3
        i32.store offset=12
        local.get 4
        local.get 3
        i32.store offset=8
        local.get 3
        i32.const 0
        i32.store offset=24
        local.get 3
        local.get 4
        i32.store offset=12
        local.get 3
        local.get 0
        i32.store offset=8
      end
      i32.const 3612
      i32.const 3612
      i32.load
      i32.const 1
      i32.sub
      local.tee 0
      i32.const -1
      local.get 0
      select
      i32.store
    end)
  (func (;17;) (type 8) (param i32 i32)
    (local i32 i32 i32 i32 i32 i32)
    local.get 0
    local.get 1
    i32.add
    local.set 5
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        i32.load offset=4
        local.tee 2
        i32.const 1
        i32.and
        br_if 0 (;@2;)
        local.get 2
        i32.const 3
        i32.and
        i32.eqz
        br_if 1 (;@1;)
        local.get 0
        i32.load
        local.tee 2
        local.get 1
        i32.add
        local.set 1
        block  ;; label = @3
          local.get 0
          local.get 2
          i32.sub
          local.tee 0
          i32.const 3600
          i32.load
          i32.ne
          if  ;; label = @4
            local.get 2
            i32.const 255
            i32.le_u
            if  ;; label = @5
              local.get 0
              i32.load offset=8
              local.tee 4
              local.get 2
              i32.const 3
              i32.shr_u
              local.tee 2
              i32.const 3
              i32.shl
              i32.const 3620
              i32.add
              i32.eq
              drop
              local.get 0
              i32.load offset=12
              local.tee 3
              local.get 4
              i32.ne
              br_if 2 (;@3;)
              i32.const 3580
              i32.const 3580
              i32.load
              i32.const -2
              local.get 2
              i32.rotl
              i32.and
              i32.store
              br 3 (;@2;)
            end
            local.get 0
            i32.load offset=24
            local.set 6
            block  ;; label = @5
              local.get 0
              local.get 0
              i32.load offset=12
              local.tee 3
              i32.ne
              if  ;; label = @6
                local.get 0
                i32.load offset=8
                local.tee 2
                i32.const 3596
                i32.load
                i32.lt_u
                drop
                local.get 2
                local.get 3
                i32.store offset=12
                local.get 3
                local.get 2
                i32.store offset=8
                br 1 (;@5;)
              end
              block  ;; label = @6
                local.get 0
                i32.const 20
                i32.add
                local.tee 2
                i32.load
                local.tee 4
                br_if 0 (;@6;)
                local.get 0
                i32.const 16
                i32.add
                local.tee 2
                i32.load
                local.tee 4
                br_if 0 (;@6;)
                i32.const 0
                local.set 3
                br 1 (;@5;)
              end
              loop  ;; label = @6
                local.get 2
                local.set 7
                local.get 4
                local.tee 3
                i32.const 20
                i32.add
                local.tee 2
                i32.load
                local.tee 4
                br_if 0 (;@6;)
                local.get 3
                i32.const 16
                i32.add
                local.set 2
                local.get 3
                i32.load offset=16
                local.tee 4
                br_if 0 (;@6;)
              end
              local.get 7
              i32.const 0
              i32.store
            end
            local.get 6
            i32.eqz
            br_if 2 (;@2;)
            block  ;; label = @5
              local.get 0
              local.get 0
              i32.load offset=28
              local.tee 4
              i32.const 2
              i32.shl
              i32.const 3884
              i32.add
              local.tee 2
              i32.load
              i32.eq
              if  ;; label = @6
                local.get 2
                local.get 3
                i32.store
                local.get 3
                br_if 1 (;@5;)
                i32.const 3584
                i32.const 3584
                i32.load
                i32.const -2
                local.get 4
                i32.rotl
                i32.and
                i32.store
                br 4 (;@2;)
              end
              local.get 6
              i32.const 16
              i32.const 20
              local.get 6
              i32.load offset=16
              local.get 0
              i32.eq
              select
              i32.add
              local.get 3
              i32.store
              local.get 3
              i32.eqz
              br_if 3 (;@2;)
            end
            local.get 3
            local.get 6
            i32.store offset=24
            local.get 0
            i32.load offset=16
            local.tee 2
            if  ;; label = @5
              local.get 3
              local.get 2
              i32.store offset=16
              local.get 2
              local.get 3
              i32.store offset=24
            end
            local.get 0
            i32.load offset=20
            local.tee 2
            i32.eqz
            br_if 2 (;@2;)
            local.get 3
            local.get 2
            i32.store offset=20
            local.get 2
            local.get 3
            i32.store offset=24
            br 2 (;@2;)
          end
          local.get 5
          i32.load offset=4
          local.tee 2
          i32.const 3
          i32.and
          i32.const 3
          i32.ne
          br_if 1 (;@2;)
          i32.const 3588
          local.get 1
          i32.store
          local.get 5
          local.get 2
          i32.const -2
          i32.and
          i32.store offset=4
          local.get 0
          local.get 1
          i32.const 1
          i32.or
          i32.store offset=4
          local.get 5
          local.get 1
          i32.store
          return
        end
        local.get 4
        local.get 3
        i32.store offset=12
        local.get 3
        local.get 4
        i32.store offset=8
      end
      block  ;; label = @2
        local.get 5
        i32.load offset=4
        local.tee 2
        i32.const 2
        i32.and
        i32.eqz
        if  ;; label = @3
          local.get 5
          i32.const 3604
          i32.load
          i32.eq
          if  ;; label = @4
            i32.const 3604
            local.get 0
            i32.store
            i32.const 3592
            i32.const 3592
            i32.load
            local.get 1
            i32.add
            local.tee 1
            i32.store
            local.get 0
            local.get 1
            i32.const 1
            i32.or
            i32.store offset=4
            local.get 0
            i32.const 3600
            i32.load
            i32.ne
            br_if 3 (;@1;)
            i32.const 3588
            i32.const 0
            i32.store
            i32.const 3600
            i32.const 0
            i32.store
            return
          end
          local.get 5
          i32.const 3600
          i32.load
          i32.eq
          if  ;; label = @4
            i32.const 3600
            local.get 0
            i32.store
            i32.const 3588
            i32.const 3588
            i32.load
            local.get 1
            i32.add
            local.tee 1
            i32.store
            local.get 0
            local.get 1
            i32.const 1
            i32.or
            i32.store offset=4
            local.get 0
            local.get 1
            i32.add
            local.get 1
            i32.store
            return
          end
          local.get 2
          i32.const -8
          i32.and
          local.get 1
          i32.add
          local.set 1
          block  ;; label = @4
            local.get 2
            i32.const 255
            i32.le_u
            if  ;; label = @5
              local.get 5
              i32.load offset=8
              local.tee 4
              local.get 2
              i32.const 3
              i32.shr_u
              local.tee 2
              i32.const 3
              i32.shl
              i32.const 3620
              i32.add
              i32.eq
              drop
              local.get 4
              local.get 5
              i32.load offset=12
              local.tee 3
              i32.eq
              if  ;; label = @6
                i32.const 3580
                i32.const 3580
                i32.load
                i32.const -2
                local.get 2
                i32.rotl
                i32.and
                i32.store
                br 2 (;@4;)
              end
              local.get 4
              local.get 3
              i32.store offset=12
              local.get 3
              local.get 4
              i32.store offset=8
              br 1 (;@4;)
            end
            local.get 5
            i32.load offset=24
            local.set 6
            block  ;; label = @5
              local.get 5
              local.get 5
              i32.load offset=12
              local.tee 3
              i32.ne
              if  ;; label = @6
                local.get 5
                i32.load offset=8
                local.tee 2
                i32.const 3596
                i32.load
                i32.lt_u
                drop
                local.get 2
                local.get 3
                i32.store offset=12
                local.get 3
                local.get 2
                i32.store offset=8
                br 1 (;@5;)
              end
              block  ;; label = @6
                local.get 5
                i32.const 20
                i32.add
                local.tee 4
                i32.load
                local.tee 2
                br_if 0 (;@6;)
                local.get 5
                i32.const 16
                i32.add
                local.tee 4
                i32.load
                local.tee 2
                br_if 0 (;@6;)
                i32.const 0
                local.set 3
                br 1 (;@5;)
              end
              loop  ;; label = @6
                local.get 4
                local.set 7
                local.get 2
                local.tee 3
                i32.const 20
                i32.add
                local.tee 4
                i32.load
                local.tee 2
                br_if 0 (;@6;)
                local.get 3
                i32.const 16
                i32.add
                local.set 4
                local.get 3
                i32.load offset=16
                local.tee 2
                br_if 0 (;@6;)
              end
              local.get 7
              i32.const 0
              i32.store
            end
            local.get 6
            i32.eqz
            br_if 0 (;@4;)
            block  ;; label = @5
              local.get 5
              local.get 5
              i32.load offset=28
              local.tee 4
              i32.const 2
              i32.shl
              i32.const 3884
              i32.add
              local.tee 2
              i32.load
              i32.eq
              if  ;; label = @6
                local.get 2
                local.get 3
                i32.store
                local.get 3
                br_if 1 (;@5;)
                i32.const 3584
                i32.const 3584
                i32.load
                i32.const -2
                local.get 4
                i32.rotl
                i32.and
                i32.store
                br 2 (;@4;)
              end
              local.get 6
              i32.const 16
              i32.const 20
              local.get 6
              i32.load offset=16
              local.get 5
              i32.eq
              select
              i32.add
              local.get 3
              i32.store
              local.get 3
              i32.eqz
              br_if 1 (;@4;)
            end
            local.get 3
            local.get 6
            i32.store offset=24
            local.get 5
            i32.load offset=16
            local.tee 2
            if  ;; label = @5
              local.get 3
              local.get 2
              i32.store offset=16
              local.get 2
              local.get 3
              i32.store offset=24
            end
            local.get 5
            i32.load offset=20
            local.tee 2
            i32.eqz
            br_if 0 (;@4;)
            local.get 3
            local.get 2
            i32.store offset=20
            local.get 2
            local.get 3
            i32.store offset=24
          end
          local.get 0
          local.get 1
          i32.const 1
          i32.or
          i32.store offset=4
          local.get 0
          local.get 1
          i32.add
          local.get 1
          i32.store
          local.get 0
          i32.const 3600
          i32.load
          i32.ne
          br_if 1 (;@2;)
          i32.const 3588
          local.get 1
          i32.store
          return
        end
        local.get 5
        local.get 2
        i32.const -2
        i32.and
        i32.store offset=4
        local.get 0
        local.get 1
        i32.const 1
        i32.or
        i32.store offset=4
        local.get 0
        local.get 1
        i32.add
        local.get 1
        i32.store
      end
      local.get 1
      i32.const 255
      i32.le_u
      if  ;; label = @2
        local.get 1
        i32.const 3
        i32.shr_u
        local.tee 2
        i32.const 3
        i32.shl
        i32.const 3620
        i32.add
        local.set 1
        block (result i32)  ;; label = @3
          i32.const 3580
          i32.load
          local.tee 3
          i32.const 1
          local.get 2
          i32.shl
          local.tee 2
          i32.and
          i32.eqz
          if  ;; label = @4
            i32.const 3580
            local.get 2
            local.get 3
            i32.or
            i32.store
            local.get 1
            br 1 (;@3;)
          end
          local.get 1
          i32.load offset=8
        end
        local.set 2
        local.get 1
        local.get 0
        i32.store offset=8
        local.get 2
        local.get 0
        i32.store offset=12
        local.get 0
        local.get 1
        i32.store offset=12
        local.get 0
        local.get 2
        i32.store offset=8
        return
      end
      i32.const 31
      local.set 2
      local.get 0
      i64.const 0
      i64.store offset=16 align=4
      local.get 1
      i32.const 16777215
      i32.le_u
      if  ;; label = @2
        local.get 1
        i32.const 8
        i32.shr_u
        local.tee 2
        local.get 2
        i32.const 1048320
        i32.add
        i32.const 16
        i32.shr_u
        i32.const 8
        i32.and
        local.tee 4
        i32.shl
        local.tee 2
        local.get 2
        i32.const 520192
        i32.add
        i32.const 16
        i32.shr_u
        i32.const 4
        i32.and
        local.tee 3
        i32.shl
        local.tee 2
        local.get 2
        i32.const 245760
        i32.add
        i32.const 16
        i32.shr_u
        i32.const 2
        i32.and
        local.tee 2
        i32.shl
        i32.const 15
        i32.shr_u
        local.get 3
        local.get 4
        i32.or
        local.get 2
        i32.or
        i32.sub
        local.tee 2
        i32.const 1
        i32.shl
        local.get 1
        local.get 2
        i32.const 21
        i32.add
        i32.shr_u
        i32.const 1
        i32.and
        i32.or
        i32.const 28
        i32.add
        local.set 2
      end
      local.get 0
      local.get 2
      i32.store offset=28
      local.get 2
      i32.const 2
      i32.shl
      i32.const 3884
      i32.add
      local.set 7
      block  ;; label = @2
        block  ;; label = @3
          i32.const 3584
          i32.load
          local.tee 4
          i32.const 1
          local.get 2
          i32.shl
          local.tee 3
          i32.and
          i32.eqz
          if  ;; label = @4
            i32.const 3584
            local.get 3
            local.get 4
            i32.or
            i32.store
            local.get 7
            local.get 0
            i32.store
            local.get 0
            local.get 7
            i32.store offset=24
            br 1 (;@3;)
          end
          local.get 1
          i32.const 0
          i32.const 25
          local.get 2
          i32.const 1
          i32.shr_u
          i32.sub
          local.get 2
          i32.const 31
          i32.eq
          select
          i32.shl
          local.set 2
          local.get 7
          i32.load
          local.set 3
          loop  ;; label = @4
            local.get 3
            local.tee 4
            i32.load offset=4
            i32.const -8
            i32.and
            local.get 1
            i32.eq
            br_if 2 (;@2;)
            local.get 2
            i32.const 29
            i32.shr_u
            local.set 3
            local.get 2
            i32.const 1
            i32.shl
            local.set 2
            local.get 4
            local.get 3
            i32.const 4
            i32.and
            i32.add
            local.tee 7
            i32.const 16
            i32.add
            i32.load
            local.tee 3
            br_if 0 (;@4;)
          end
          local.get 7
          local.get 0
          i32.store offset=16
          local.get 0
          local.get 4
          i32.store offset=24
        end
        local.get 0
        local.get 0
        i32.store offset=12
        local.get 0
        local.get 0
        i32.store offset=8
        return
      end
      local.get 4
      i32.load offset=8
      local.tee 1
      local.get 0
      i32.store offset=12
      local.get 4
      local.get 0
      i32.store offset=8
      local.get 0
      i32.const 0
      i32.store offset=24
      local.get 0
      local.get 4
      i32.store offset=12
      local.get 0
      local.get 1
      i32.store offset=8
    end)
  (func (;18;) (type 0) (param i32) (result i32)
    (local i32 i32)
    i32.const 1368
    i32.load
    local.tee 1
    local.get 0
    i32.const 3
    i32.add
    i32.const -4
    i32.and
    local.tee 2
    i32.add
    local.set 0
    block  ;; label = @1
      local.get 2
      i32.const 0
      local.get 0
      local.get 1
      i32.le_u
      select
      br_if 0 (;@1;)
      memory.size
      i32.const 16
      i32.shl
      local.get 0
      i32.lt_u
      br_if 0 (;@1;)
      i32.const 1368
      local.get 0
      i32.store
      local.get 1
      return
    end
    i32.const 3576
    i32.const 48
    i32.store
    i32.const -1)
  (func (;19;) (type 9) (param i32 i32 i32)
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
  (func (;20;) (type 6) (result i32)
    global.get 0)
  (func (;21;) (type 1) (param i32)
    local.get 0
    global.set 0)
  (func (;22;) (type 0) (param i32) (result i32)
    global.get 0
    local.get 0
    i32.sub
    i32.const -16
    i32.and
    local.tee 0
    global.set 0
    local.get 0)
  (table (;0;) 8 8 funcref)
  (memory (;0;) 256 256)
  (global (;0;) (mut i32) (i32.const 5246960))
  (export "memory" (memory 0))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func 6))
  (export "__errno_location" (func 14))
  (export "stackSave" (func 20))
  (export "stackRestore" (func 21))
  (export "stackAlloc" (func 22))
  (elem (;0;) (i32.const 1) func 5 7 9 8 10 11 12)
  (data (;0;) (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a\00\008\04\00\00\c8\04")
  (data (;1;) (i32.const 1073) "\04\00\00\00\00\00\00\05")
  (data (;2;) (i32.const 1092) "\02")
  (data (;3;) (i32.const 1116) "\03\00\00\00\04\00\00\00\e8\05\00\00\00\04")
  (data (;4;) (i32.const 1140) "\01")
  (data (;5;) (i32.const 1155) "\0a\ff\ff\ff\ff")
  (data (;6;) (i32.const 1224) "\09")
  (data (;7;) (i32.const 1236) "\05")
  (data (;8;) (i32.const 1256) "\06\00\00\00\00\00\00\00\07\00\00\00\f8\09\00\00\00\04")
  (data (;9;) (i32.const 1300) "\ff\ff\ff\ff")
  (data (;10;) (i32.const 1368) "\f0\0fP"))
