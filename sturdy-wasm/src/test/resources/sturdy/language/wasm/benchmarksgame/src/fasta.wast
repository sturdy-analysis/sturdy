(module $fasta.wasm
  (type (;0;) (func (param i32 i32 i32 i32) (result i32)))
  (type (;1;) (func (param i32) (result i32)))
  (type (;2;) (func (param i32 i32 i32) (result i32)))
  (type (;3;) (func (param i32)))
  (type (;4;) (func))
  (type (;5;) (func (result i32)))
  (type (;6;) (func (param i32 i32 i32)))
  (import "env" "fwrite" (func $fwrite (type 0)))
  (import "env" "strlen" (func $strlen (type 1)))
  (import "env" "memcpy" (func $memcpy (type 2)))
  (import "env" "exit" (func $exit (type 3)))
  (func $__wasm_call_ctors (type 4))
  (func $_start (type 5) (result i32)
    i32.const 1055
    i32.const 22
    i32.const 1
    i32.const 0
    i32.load
    call $fwrite
    drop
    i32.const 1104
    call $repeat_And_Wrap_String
    i32.const 0
    i32.const 0
    i32.store offset=1568
    i32.const 1078
    i32.const 25
    i32.const 1
    i32.const 0
    i32.load
    call $fwrite
    drop
    i32.const 1392
    i32.const 15
    i32.const 7500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    i32.const 1024
    i32.const 30
    i32.const 1
    i32.const 0
    i32.load
    call $fwrite
    drop
    i32.const 1520
    i32.const 4
    i32.const 12500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    i32.const 0)
  (func $repeat_And_Wrap_String (type 3) (param i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 64
    i32.sub
    local.tee 1
    local.set 2
    local.get 1
    global.set $__stack_pointer
    local.get 1
    local.get 0
    call $strlen
    local.tee 3
    i32.const 75
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee 4
    global.set $__stack_pointer
    block  ;; label = @1
      local.get 3
      i32.const -59
      i32.lt_s
      br_if 0 (;@1;)
      local.get 3
      i32.const 60
      i32.add
      local.tee 5
      i32.const 1
      local.get 5
      i32.const 1
      i32.gt_s
      select
      local.tee 6
      i32.const 1
      i32.and
      local.set 7
      i32.const 0
      local.set 1
      block  ;; label = @2
        local.get 5
        i32.const 2
        i32.lt_s
        br_if 0 (;@2;)
        local.get 6
        i32.const 2147483646
        i32.and
        local.set 6
        i32.const 0
        local.set 1
        loop  ;; label = @3
          local.get 4
          local.get 1
          i32.add
          local.tee 5
          local.get 0
          local.get 1
          local.get 3
          i32.rem_s
          i32.add
          i32.load8_u
          i32.store8
          local.get 5
          i32.const 1
          i32.add
          local.get 0
          local.get 1
          i32.const 1
          i32.add
          local.get 3
          i32.rem_s
          i32.add
          i32.load8_u
          i32.store8
          local.get 1
          i32.const 2
          i32.add
          local.tee 1
          local.get 6
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 7
      i32.eqz
      br_if 0 (;@1;)
      local.get 4
      local.get 1
      i32.add
      local.get 0
      local.get 1
      local.get 3
      i32.rem_s
      i32.add
      i32.load8_u
      i32.store8
    end
    local.get 2
    i32.const 10
    i32.store8 offset=60
    i32.const 5000
    local.set 0
    i32.const 0
    local.set 5
    loop  ;; label = @1
      i32.const 60
      local.set 1
      block  ;; label = @2
        local.get 0
        i32.const 59
        i32.gt_u
        br_if 0 (;@2;)
        local.get 2
        local.get 0
        i32.add
        i32.const 10
        i32.store8
        local.get 0
        local.set 1
      end
      local.get 2
      local.get 4
      local.get 5
      i32.add
      local.get 1
      call $memcpy
      local.tee 6
      local.get 1
      i32.const 1
      i32.add
      i32.const 1
      i32.const 0
      i32.load
      call $fwrite
      drop
      local.get 1
      local.get 5
      i32.add
      local.tee 5
      local.get 3
      i32.const 0
      local.get 5
      local.get 3
      i32.gt_s
      select
      i32.sub
      local.set 5
      local.get 0
      local.get 1
      i32.sub
      local.tee 0
      i32.const 0
      i32.gt_s
      br_if 0 (;@1;)
    end
    local.get 6
    i32.const 64
    i32.add
    global.set $__stack_pointer)
  (func $generate_And_Wrap_Pseudorandom_DNA_Sequence (type 6) (param i32 i32 i32)
    (local i32 i32 i32 i32 i32 i32 f32 i32 i32 f32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 308224
    i32.sub
    local.tee 3
    local.set 4
    local.get 3
    global.set $__stack_pointer
    local.get 3
    local.get 1
    i32.const 2
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee 5
    global.set $__stack_pointer
    block  ;; label = @1
      local.get 1
      i32.const 1
      i32.lt_s
      local.tee 6
      br_if 0 (;@1;)
      local.get 1
      i32.const 1
      i32.and
      local.set 7
      block  ;; label = @2
        block  ;; label = @3
          local.get 1
          i32.const 1
          i32.ne
          br_if 0 (;@3;)
          i32.const 0
          local.set 8
          f32.const 0x0p+0 (;=0;)
          local.set 9
          br 1 (;@2;)
        end
        local.get 0
        i32.const 12
        i32.add
        local.set 3
        local.get 1
        i32.const 2147483646
        i32.and
        local.set 10
        i32.const 0
        local.set 8
        local.get 5
        local.set 11
        f32.const 0x0p+0 (;=0;)
        local.set 9
        loop  ;; label = @3
          block  ;; label = @4
            block  ;; label = @5
              local.get 9
              local.get 3
              i32.const -8
              i32.add
              f32.load
              f32.add
              local.tee 9
              f32.const 0x1.116p+17 (;=139968;)
              f32.mul
              local.tee 12
              f32.const 0x1p+32 (;=4.29497e+09;)
              f32.lt
              local.get 12
              f32.const 0x0p+0 (;=0;)
              f32.ge
              i32.and
              i32.eqz
              br_if 0 (;@5;)
              local.get 12
              i32.trunc_f32_u
              local.set 13
              br 1 (;@4;)
            end
            i32.const 0
            local.set 13
          end
          local.get 11
          local.get 13
          i32.const 1
          i32.add
          i32.store
          block  ;; label = @4
            block  ;; label = @5
              local.get 9
              local.get 3
              f32.load
              f32.add
              local.tee 9
              f32.const 0x1.116p+17 (;=139968;)
              f32.mul
              local.tee 12
              f32.const 0x1p+32 (;=4.29497e+09;)
              f32.lt
              local.get 12
              f32.const 0x0p+0 (;=0;)
              f32.ge
              i32.and
              i32.eqz
              br_if 0 (;@5;)
              local.get 12
              i32.trunc_f32_u
              local.set 13
              br 1 (;@4;)
            end
            i32.const 0
            local.set 13
          end
          local.get 11
          i32.const 4
          i32.add
          local.get 13
          i32.const 1
          i32.add
          i32.store
          local.get 11
          i32.const 8
          i32.add
          local.set 11
          local.get 3
          i32.const 16
          i32.add
          local.set 3
          local.get 10
          local.get 8
          i32.const 2
          i32.add
          local.tee 8
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 7
      i32.eqz
      br_if 0 (;@1;)
      local.get 5
      local.get 8
      i32.const 2
      i32.shl
      i32.add
      local.set 3
      block  ;; label = @2
        block  ;; label = @3
          local.get 9
          local.get 0
          local.get 8
          i32.const 3
          i32.shl
          i32.add
          f32.load offset=4
          f32.add
          f32.const 0x1.116p+17 (;=139968;)
          f32.mul
          local.tee 12
          f32.const 0x1p+32 (;=4.29497e+09;)
          f32.lt
          local.get 12
          f32.const 0x0p+0 (;=0;)
          f32.ge
          i32.and
          i32.eqz
          br_if 0 (;@3;)
          local.get 12
          i32.trunc_f32_u
          local.set 11
          br 1 (;@2;)
        end
        i32.const 0
        local.set 11
      end
      local.get 3
      local.get 11
      i32.const 1
      i32.add
      i32.store
    end
    i32.const 0
    local.get 2
    i32.store offset=1564
    i32.const 0
    i32.const 0
    i32.store offset=1572
    i32.const 0
    i32.const 0
    i32.store offset=1568
    local.get 1
    i32.const 2147483644
    i32.and
    local.set 13
    local.get 1
    i32.const 3
    i32.and
    local.set 14
    local.get 4
    i32.const 4
    i32.or
    local.set 15
    i32.const 0
    local.set 3
    block  ;; label = @1
      loop  ;; label = @2
        i32.const 0
        i32.load offset=1556
        i32.const 1
        i32.gt_s
        local.set 7
        i32.const 0
        i32.load offset=1552
        local.set 11
        loop  ;; label = @3
          i32.const -1
          local.set 10
          block  ;; label = @4
            local.get 3
            br_if 0 (;@4;)
            i32.const 0
            local.get 7
            i32.store offset=1568
            i32.const 0
            local.get 2
            local.get 2
            i32.const 61440
            local.get 2
            i32.const 61440
            i32.lt_s
            select
            local.tee 10
            i32.sub
            local.tee 16
            i32.store offset=1564
            block  ;; label = @5
              local.get 2
              i32.eqz
              br_if 0 (;@5;)
              local.get 4
              local.set 3
              block  ;; label = @6
                block  ;; label = @7
                  local.get 10
                  i32.const 1
                  i32.and
                  br_if 0 (;@7;)
                  local.get 10
                  local.set 8
                  br 1 (;@6;)
                end
                local.get 4
                local.get 11
                i32.const 3877
                i32.mul
                i32.const 29573
                i32.add
                i32.const 139968
                i32.rem_u
                local.tee 11
                i32.store
                local.get 10
                i32.const -1
                i32.add
                local.set 8
                local.get 15
                local.set 3
              end
              block  ;; label = @6
                local.get 2
                i32.const 1
                i32.eq
                br_if 0 (;@6;)
                loop  ;; label = @7
                  local.get 3
                  local.get 11
                  i32.const 3877
                  i32.mul
                  i32.const 29573
                  i32.add
                  i32.const 139968
                  i32.rem_u
                  local.tee 11
                  i32.store
                  local.get 3
                  i32.const 4
                  i32.add
                  local.get 11
                  i32.const 3877
                  i32.mul
                  i32.const 29573
                  i32.add
                  i32.const 139968
                  i32.rem_u
                  local.tee 11
                  i32.store
                  local.get 3
                  i32.const 8
                  i32.add
                  local.set 3
                  local.get 8
                  i32.const -2
                  i32.add
                  local.tee 8
                  br_if 0 (;@7;)
                end
              end
              i32.const 0
              local.get 11
              i32.store offset=1552
            end
            local.get 16
            local.set 2
            local.get 7
            local.set 3
          end
          local.get 10
          i32.const -1
          i32.eq
          br_if 0 (;@3;)
        end
        block  ;; label = @3
          block  ;; label = @4
            block  ;; label = @5
              block  ;; label = @6
                local.get 10
                i32.eqz
                br_if 0 (;@6;)
                block  ;; label = @7
                  local.get 10
                  i32.const 1
                  i32.ge_s
                  br_if 0 (;@7;)
                  local.get 4
                  i32.const 245760
                  i32.add
                  local.set 7
                  br 4 (;@3;)
                end
                local.get 6
                br_if 1 (;@5;)
                local.get 4
                i32.const 245760
                i32.add
                local.set 7
                i32.const 0
                local.set 17
                i32.const 0
                local.set 16
                loop  ;; label = @7
                  local.get 4
                  local.get 17
                  i32.const 2
                  i32.shl
                  i32.add
                  i32.load
                  local.set 11
                  i32.const 0
                  local.set 8
                  i32.const 0
                  local.set 2
                  block  ;; label = @8
                    local.get 1
                    i32.const 4
                    i32.lt_u
                    br_if 0 (;@8;)
                    i32.const 0
                    local.set 8
                    local.get 5
                    local.set 3
                    i32.const 0
                    local.set 2
                    loop  ;; label = @9
                      local.get 8
                      local.get 3
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.get 3
                      i32.const 4
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.get 3
                      i32.const 8
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.get 3
                      i32.const 12
                      i32.add
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.set 8
                      local.get 3
                      i32.const 16
                      i32.add
                      local.set 3
                      local.get 13
                      local.get 2
                      i32.const 4
                      i32.add
                      local.tee 2
                      i32.ne
                      br_if 0 (;@9;)
                    end
                  end
                  block  ;; label = @8
                    local.get 14
                    i32.eqz
                    br_if 0 (;@8;)
                    local.get 5
                    local.get 2
                    i32.const 2
                    i32.shl
                    i32.add
                    local.set 3
                    local.get 14
                    local.set 2
                    loop  ;; label = @9
                      local.get 8
                      local.get 3
                      i32.load
                      local.get 11
                      i32.le_u
                      i32.add
                      local.set 8
                      local.get 3
                      i32.const 4
                      i32.add
                      local.set 3
                      local.get 2
                      i32.const -1
                      i32.add
                      local.tee 2
                      br_if 0 (;@9;)
                    end
                  end
                  local.get 7
                  local.get 0
                  local.get 8
                  i32.const 3
                  i32.shl
                  i32.add
                  i32.load8_u
                  i32.store8
                  block  ;; label = @8
                    block  ;; label = @9
                      local.get 16
                      i32.const 58
                      i32.gt_s
                      br_if 0 (;@9;)
                      local.get 16
                      i32.const 1
                      i32.add
                      local.set 16
                      local.get 7
                      i32.const 1
                      i32.add
                      local.set 7
                      br 1 (;@8;)
                    end
                    local.get 7
                    i32.const 10
                    i32.store8 offset=1
                    local.get 7
                    i32.const 2
                    i32.add
                    local.set 7
                    i32.const 0
                    local.set 16
                  end
                  local.get 17
                  i32.const 1
                  i32.add
                  local.tee 17
                  local.get 10
                  i32.ne
                  br_if 0 (;@7;)
                  br 3 (;@4;)
                end
              end
              local.get 4
              i32.const 308224
              i32.add
              global.set $__stack_pointer
              return
            end
            block  ;; label = @5
              block  ;; label = @6
                local.get 10
                i32.const 1
                i32.ne
                br_if 0 (;@6;)
                local.get 4
                i32.const 245760
                i32.add
                local.set 7
                i32.const 0
                local.set 16
                br 1 (;@5;)
              end
              local.get 10
              i32.const 2147483646
              i32.and
              local.set 8
              local.get 4
              i32.const 245760
              i32.add
              local.set 7
              i32.const 0
              local.set 16
              loop  ;; label = @6
                local.get 7
                local.get 0
                i32.load8_u
                i32.store8
                block  ;; label = @7
                  block  ;; label = @8
                    local.get 16
                    i32.const 59
                    i32.ge_s
                    br_if 0 (;@8;)
                    local.get 16
                    i32.const 1
                    i32.add
                    local.set 11
                    local.get 7
                    i32.const 1
                    i32.add
                    local.set 3
                    br 1 (;@7;)
                  end
                  local.get 7
                  i32.const 10
                  i32.store8 offset=1
                  local.get 7
                  i32.const 2
                  i32.add
                  local.set 3
                  i32.const 0
                  local.set 11
                end
                local.get 3
                local.get 0
                i32.load8_u
                i32.store8
                block  ;; label = @7
                  block  ;; label = @8
                    local.get 11
                    i32.const 59
                    i32.ge_s
                    br_if 0 (;@8;)
                    local.get 11
                    i32.const 1
                    i32.add
                    local.set 16
                    local.get 3
                    i32.const 1
                    i32.add
                    local.set 7
                    br 1 (;@7;)
                  end
                  local.get 3
                  i32.const 10
                  i32.store8 offset=1
                  local.get 3
                  i32.const 2
                  i32.add
                  local.set 7
                  i32.const 0
                  local.set 16
                end
                local.get 8
                i32.const -2
                i32.add
                local.tee 8
                br_if 0 (;@6;)
              end
            end
            local.get 10
            i32.const 1
            i32.and
            i32.eqz
            br_if 0 (;@4;)
            local.get 7
            local.get 0
            i32.load8_u
            i32.store8
            block  ;; label = @5
              local.get 16
              i32.const 59
              i32.ge_s
              br_if 0 (;@5;)
              local.get 16
              i32.const 1
              i32.add
              local.set 16
              local.get 7
              i32.const 1
              i32.add
              local.set 7
              br 1 (;@4;)
            end
            local.get 7
            i32.const 10
            i32.store8 offset=1
            local.get 7
            i32.const 2
            i32.add
            local.set 7
            i32.const 0
            local.set 16
          end
          local.get 16
          i32.eqz
          br_if 0 (;@3;)
          local.get 7
          i32.const 10
          i32.store8
          local.get 7
          i32.const 1
          i32.add
          local.set 7
        end
        i32.const 0
        i32.load offset=1572
        br_if 1 (;@1;)
        local.get 7
        local.get 4
        i32.const 245760
        i32.add
        i32.sub
        local.set 8
        i32.const 0
        local.set 11
        loop  ;; label = @3
          i32.const -1
          local.set 3
          block  ;; label = @4
            local.get 11
            br_if 0 (;@4;)
            i32.const 0
            i32.const 0
            i32.load offset=1560
            i32.const 1
            i32.gt_s
            i32.store offset=1572
            local.get 4
            i32.const 245760
            i32.add
            local.get 8
            i32.const 1
            i32.const 0
            i32.load
            call $fwrite
            local.set 3
            i32.const 0
            i32.load offset=1572
            local.set 11
          end
          local.get 3
          i32.const -1
          i32.eq
          br_if 0 (;@3;)
        end
        block  ;; label = @3
          local.get 3
          i32.eqz
          br_if 0 (;@3;)
          i32.const 0
          i32.load offset=1564
          local.set 2
          i32.const 0
          i32.load offset=1568
          local.set 3
          br 1 (;@2;)
        end
      end
      i32.const 1
      call $exit
      unreachable
    end
    loop  ;; label = @1
      br 0 (;@1;)
    end)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 67120))
  (global (;1;) i32 (i32.const 1568))
  (global (;2;) i32 (i32.const 1564))
  (global (;3;) i32 (i32.const 1572))
  (global (;4;) i32 (i32.const 1556))
  (global (;5;) i32 (i32.const 1552))
  (global (;6;) i32 (i32.const 1560))
  (global (;7;) i32 (i32.const 1024))
  (global (;8;) i32 (i32.const 1576))
  (global (;9;) i32 (i32.const 1584))
  (global (;10;) i32 (i32.const 67120))
  (global (;11;) i32 (i32.const 1024))
  (global (;12;) i32 (i32.const 67120))
  (global (;13;) i32 (i32.const 131072))
  (global (;14;) i32 (i32.const 0))
  (global (;15;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "_start" (func $_start))
  (export "rng_tid" (global 1))
  (export "rng_cnt" (global 2))
  (export "out_tid" (global 3))
  (export "rng_tnum" (global 4))
  (export "seed" (global 5))
  (export "out_tnum" (global 6))
  (export "__dso_handle" (global 7))
  (export "__data_end" (global 8))
  (export "__stack_low" (global 9))
  (export "__stack_high" (global 10))
  (export "__global_base" (global 11))
  (export "__heap_base" (global 12))
  (export "__heap_end" (global 13))
  (export "__memory_base" (global 14))
  (export "__table_base" (global 15))
  (data $.rodata (i32.const 1024) ">THREE Homo sapiens frequency\0a\00>ONE Homo sapiens alu\0a\00>TWO IUB ambiguity codes\0a\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00a\00\00\00q=\8a>c\00\00\00\8f\c2\f5=g\00\00\00\8f\c2\f5=t\00\00\00q=\8a>B\00\00\00\0a\d7\a3<D\00\00\00\0a\d7\a3<H\00\00\00\0a\d7\a3<K\00\00\00\0a\d7\a3<M\00\00\00\0a\d7\a3<N\00\00\00\0a\d7\a3<R\00\00\00\0a\d7\a3<S\00\00\00\0a\d7\a3<V\00\00\00\0a\d7\a3<W\00\00\00\0a\d7\a3<Y\00\00\00\0a\d7\a3<\00\00\00\00\00\00\00\00a\00\00\00\e9\1c\9b>c\00\00\00r\bdJ>g\00\00\00\d7IJ>t\00\00\00r_\9a>")
  (data $.data (i32.const 1552) "*\00\00\00\01\00\00\00\01\00\00\00"))
