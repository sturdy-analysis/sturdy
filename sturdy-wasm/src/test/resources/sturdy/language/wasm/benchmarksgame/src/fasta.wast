(module $fasta.wasm
  (type (;0;) (func (param i32)))
  (type (;1;) (func (param i32 i32 i32) (result i32)))
  (type (;2;) (func))
  (type (;3;) (func (param i32) (result i32)))
  (type (;4;) (func (result i32)))
  (type (;5;) (func (param i32 i32 i32 i32) (result i32)))
  (type (;6;) (func (param i32 i64 i32) (result i64)))
  (type (;7;) (func (param i32 i32 i32)))
  (type (;8;) (func (param i32 i32) (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func $__wasi_proc_exit (type 0)))
  (import "wasi_snapshot_preview1" "fd_write" (func $__wasi_fd_write (type 5)))
  (func $__wasm_call_ctors (type 2)
    nop)
  (func $__original_main (type 4) (result i32)
    (local i32)
    i32.const 1055
    i32.const 22
    i32.const 1
    i32.const 1552
    i32.load
    local.tee 0
    call $fwrite
    drop
    i32.const 1104
    call $repeat_And_Wrap_String
    i32.const 1732
    i32.const 0
    i32.store
    i32.const 1078
    i32.const 25
    i32.const 1
    local.get 0
    call $fwrite
    drop
    i32.const 1392
    i32.const 15
    i32.const 7500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    i32.const 1024
    i32.const 30
    i32.const 1
    local.get 0
    call $fwrite
    drop
    i32.const 1520
    i32.const 4
    i32.const 12500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    i32.const 0)
  (func $repeat_And_Wrap_String (type 0) (param i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const -64
    i32.add
    local.tee 1
    local.set 4
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
    local.tee 6
    global.set $__stack_pointer
    block  ;; label = @1
      local.get 3
      i32.const -59
      i32.lt_s
      br_if 0 (;@1;)
      i32.const 1
      local.get 3
      i32.const 60
      i32.add
      local.tee 2
      local.get 2
      i32.const 1
      i32.le_s
      select
      local.tee 5
      i32.const 1
      i32.and
      local.set 7
      i32.const 0
      local.set 1
      local.get 2
      i32.const 2
      i32.ge_s
      if  ;; label = @2
        local.get 5
        i32.const 2147483646
        i32.and
        local.set 8
        i32.const 0
        local.set 2
        loop  ;; label = @3
          local.get 1
          local.get 6
          i32.add
          local.get 0
          local.get 1
          local.get 3
          i32.rem_s
          i32.add
          i32.load8_u
          i32.store8
          local.get 6
          local.get 1
          i32.const 1
          i32.or
          local.tee 5
          i32.add
          local.get 0
          local.get 5
          local.get 3
          i32.rem_s
          i32.add
          i32.load8_u
          i32.store8
          local.get 1
          i32.const 2
          i32.add
          local.set 1
          local.get 2
          i32.const 2
          i32.add
          local.tee 2
          local.get 8
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 7
      i32.eqz
      br_if 0 (;@1;)
      local.get 1
      local.get 6
      i32.add
      local.get 0
      local.get 1
      local.get 3
      i32.rem_s
      i32.add
      i32.load8_u
      i32.store8
    end
    local.get 4
    i32.const 10
    i32.store8 offset=60
    i32.const 1552
    i32.load
    local.set 5
    i32.const 5000
    local.set 0
    i32.const 0
    local.set 2
    loop  ;; label = @1
      i32.const 60
      local.set 1
      local.get 0
      i32.const 59
      i32.le_u
      if  ;; label = @2
        local.get 0
        local.get 4
        i32.add
        i32.const 10
        i32.store8
        local.get 0
        local.set 1
      end
      local.get 1
      if  ;; label = @2
        local.get 4
        local.get 2
        local.get 6
        i32.add
        local.get 1
        memory.copy
      end
      local.get 4
      local.get 1
      i32.const 1
      i32.add
      i32.const 1
      local.get 5
      call $fwrite
      drop
      local.get 1
      local.get 2
      i32.add
      local.tee 2
      local.get 3
      i32.const 0
      local.get 2
      local.get 3
      i32.gt_s
      select
      i32.sub
      local.set 2
      local.get 0
      local.get 1
      i32.sub
      local.tee 0
      i32.const 0
      i32.gt_s
      br_if 0 (;@1;)
    end
    local.get 4
    i32.const -64
    i32.sub
    global.set $__stack_pointer)
  (func $generate_And_Wrap_Pseudorandom_DNA_Sequence (type 7) (param i32 i32 i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 f32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 308224
    i32.sub
    local.tee 3
    local.set 7
    local.get 3
    global.set $__stack_pointer
    local.get 1
    i32.const 14
    i32.and
    local.set 9
    local.get 1
    i32.const 1
    i32.and
    local.set 14
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
    global.set $__stack_pointer
    i32.const 0
    local.set 3
    loop  ;; label = @1
      local.get 10
      local.get 3
      i32.const 2
      i32.shl
      i32.add
      local.get 11
      local.get 0
      local.get 3
      i32.const 3
      i32.shl
      i32.add
      f32.load offset=4
      f32.add
      local.tee 11
      f32.const 0x1.116p+17 (;=139968;)
      f32.mul
      i32.trunc_sat_f32_u
      i32.const 1
      i32.add
      i32.store
      local.get 10
      local.get 3
      i32.const 1
      i32.or
      local.tee 5
      i32.const 2
      i32.shl
      i32.add
      local.get 11
      local.get 0
      local.get 5
      i32.const 3
      i32.shl
      i32.add
      f32.load offset=4
      f32.add
      local.tee 11
      f32.const 0x1.116p+17 (;=139968;)
      f32.mul
      i32.trunc_sat_f32_u
      i32.const 1
      i32.add
      i32.store
      local.get 3
      i32.const 2
      i32.add
      local.set 3
      local.get 4
      i32.const 2
      i32.add
      local.tee 4
      local.get 9
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 14
    if  ;; label = @1
      local.get 10
      local.get 3
      i32.const 2
      i32.shl
      i32.add
      local.get 11
      local.get 0
      local.get 3
      i32.const 3
      i32.shl
      i32.add
      f32.load offset=4
      f32.add
      f32.const 0x1.116p+17 (;=139968;)
      f32.mul
      i32.trunc_sat_f32_u
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
    i32.const 12
    i32.and
    local.set 14
    local.get 1
    i32.const 3
    i32.and
    local.set 1
    local.get 7
    i32.const 4
    i32.or
    local.set 15
    i32.const 1552
    i32.load
    local.set 16
    i32.const 0
    local.set 3
    block  ;; label = @1
      loop  ;; label = @2
        i32.const 1564
        i32.load
        i32.const 1
        i32.gt_s
        local.set 9
        i32.const 1560
        i32.load
        local.set 4
        loop  ;; label = @3
          i32.const -1
          local.set 6
          local.get 3
          i32.eqz
          if  ;; label = @4
            i32.const 1732
            local.get 9
            i32.store
            i32.const 1728
            local.get 2
            i32.const 61440
            local.get 2
            local.get 2
            i32.const 61440
            i32.ge_s
            select
            local.tee 6
            i32.sub
            local.tee 8
            i32.store
            local.get 2
            if  ;; label = @5
              local.get 7
              local.set 3
              local.get 6
              i32.const 1
              i32.and
              if (result i32)  ;; label = @6
                local.get 7
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
              local.set 5
              local.get 2
              i32.const 1
              i32.ne
              if  ;; label = @6
                loop  ;; label = @7
                  local.get 3
                  local.get 4
                  i32.const 3877
                  i32.mul
                  i32.const 29573
                  i32.add
                  i32.const 139968
                  i32.rem_u
                  local.tee 4
                  i32.store
                  local.get 3
                  local.get 4
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
                  local.get 5
                  i32.const 2
                  i32.sub
                  local.tee 5
                  br_if 0 (;@7;)
                end
              end
              i32.const 1560
              local.get 4
              i32.store
            end
            local.get 9
            local.set 3
            local.get 8
            local.set 2
          end
          local.get 6
          i32.const -1
          i32.eq
          br_if 0 (;@3;)
        end
        block  ;; label = @3
          local.get 6
          if  ;; label = @4
            local.get 7
            i32.const 245760
            i32.add
            local.set 3
            i32.const 0
            local.set 12
            local.get 6
            i32.const 0
            i32.le_s
            br_if 1 (;@3;)
            i32.const 0
            local.set 13
            loop  ;; label = @5
              local.get 3
              local.set 8
              local.get 7
              local.get 12
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.set 4
              i32.const 0
              local.set 2
              i32.const 0
              local.set 3
              i32.const 0
              local.set 9
              loop  ;; label = @6
                local.get 2
                local.get 10
                local.get 3
                i32.const 2
                i32.shl
                i32.add
                local.tee 5
                i32.load
                local.get 4
                i32.le_u
                i32.add
                local.get 5
                i32.load offset=4
                local.get 4
                i32.le_u
                i32.add
                local.get 5
                i32.load offset=8
                local.get 4
                i32.le_u
                i32.add
                local.get 5
                i32.load offset=12
                local.get 4
                i32.le_u
                i32.add
                local.set 2
                local.get 3
                i32.const 4
                i32.add
                local.set 3
                local.get 9
                i32.const 4
                i32.add
                local.tee 9
                local.get 14
                i32.ne
                br_if 0 (;@6;)
              end
              i32.const 0
              local.set 5
              local.get 1
              if  ;; label = @6
                loop  ;; label = @7
                  local.get 2
                  local.get 10
                  local.get 3
                  i32.const 2
                  i32.shl
                  i32.add
                  i32.load
                  local.get 4
                  i32.le_u
                  i32.add
                  local.set 2
                  local.get 3
                  i32.const 1
                  i32.add
                  local.set 3
                  local.get 5
                  i32.const 1
                  i32.add
                  local.tee 5
                  local.get 1
                  i32.ne
                  br_if 0 (;@7;)
                end
              end
              local.get 8
              local.get 0
              local.get 2
              i32.const 3
              i32.shl
              i32.add
              i32.load8_u
              i32.store8
              local.get 13
              i32.const 58
              i32.le_s
              if  ;; label = @6
                local.get 8
                i32.const 1
                i32.add
                local.set 3
                local.get 13
                i32.const 1
                i32.add
                local.set 13
                local.get 12
                i32.const 1
                i32.add
                local.tee 12
                local.get 6
                i32.ne
                br_if 1 (;@5;)
                local.get 13
                i32.eqz
                br_if 3 (;@3;)
                local.get 8
                i32.const 10
                i32.store8 offset=1
                local.get 8
                i32.const 2
                i32.add
                local.set 3
                br 3 (;@3;)
              end
              local.get 8
              i32.const 10
              i32.store8 offset=1
              local.get 8
              i32.const 2
              i32.add
              local.set 3
              i32.const 0
              local.set 13
              local.get 12
              i32.const 1
              i32.add
              local.tee 12
              local.get 6
              i32.ne
              br_if 0 (;@5;)
            end
            br 1 (;@3;)
          end
          local.get 7
          i32.const 308224
          i32.add
          global.set $__stack_pointer
          return
        end
        i32.const 0
        local.set 4
        i32.const 1736
        i32.load
        br_if 1 (;@1;)
        local.get 3
        local.get 7
        i32.const 245760
        i32.add
        i32.sub
        local.set 5
        loop  ;; label = @3
          i32.const -1
          local.set 3
          local.get 4
          i32.eqz
          if  ;; label = @4
            i32.const 1736
            i32.const 1568
            i32.load
            i32.const 1
            i32.gt_s
            i32.store
            local.get 7
            i32.const 245760
            i32.add
            local.get 5
            i32.const 1
            local.get 16
            call $fwrite
            local.set 3
            i32.const 1736
            i32.load
            local.set 4
          end
          local.get 3
          i32.const -1
          i32.eq
          br_if 0 (;@3;)
        end
        local.get 3
        if  ;; label = @3
          i32.const 1728
          i32.load
          local.set 2
          i32.const 1732
          i32.load
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
    end
    unreachable)
  (func $main (type 8) (param i32 i32) (result i32)
    call $__original_main)
  (func $_start (type 2)
    call $__wasm_call_ctors
    call $__original_main
    call $exit
    unreachable)
  (func $dummy (type 2)
    nop)
  (func $libc_exit_fini (type 2)
    call $dummy)
  (func $exit (type 0) (param i32)
    call $dummy
    call $libc_exit_fini
    call $__stdio_exit
    local.get 0
    call $_Exit
    unreachable)
  (func $_Exit (type 0) (param i32)
    local.get 0
    call $__wasi_proc_exit
    unreachable)
  (func $__lockfile (type 3) (param i32) (result i32)
    i32.const 1)
  (func $__unlockfile (type 0) (param i32)
    nop)
  (func $__stdio_exit (type 2)
    (local i32)
    call $__ofl_lock
    i32.load
    local.tee 0
    if  ;; label = @1
      loop  ;; label = @2
        local.get 0
        call $close_file
        local.get 0
        i32.load offset=56
        local.tee 0
        br_if 0 (;@2;)
      end
    end
    i32.const 1740
    i32.load
    call $close_file
    i32.const 1720
    i32.load
    call $close_file
    i32.const 1740
    i32.load
    call $close_file)
  (func $close_file (type 0) (param i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      i32.eqz
      br_if 0 (;@1;)
      local.get 0
      i32.load offset=76
      i32.const 0
      i32.ge_s
      if  ;; label = @2
        local.get 0
        call $__lockfile
        drop
      end
      local.get 0
      i32.load offset=20
      local.get 0
      i32.load offset=28
      i32.ne
      if  ;; label = @2
        local.get 0
        i32.const 0
        i32.const 0
        local.get 0
        i32.load offset=36
        call_indirect (type 1)
        drop
      end
      local.get 0
      i32.load offset=4
      local.tee 1
      local.get 0
      i32.load offset=8
      local.tee 2
      i32.eq
      br_if 0 (;@1;)
      local.get 0
      local.get 1
      local.get 2
      i32.sub
      i64.extend_i32_s
      i32.const 1
      local.get 0
      i32.load offset=40
      call_indirect (type 6)
      drop
    end)
  (func $__towrite (type 3) (param i32) (result i32)
    (local i32)
    local.get 0
    local.get 0
    i32.load offset=72
    local.tee 1
    i32.const 1
    i32.sub
    local.get 1
    i32.or
    i32.store offset=72
    local.get 0
    i32.load
    local.tee 1
    i32.const 8
    i32.and
    if  ;; label = @1
      local.get 0
      local.get 1
      i32.const 32
      i32.or
      i32.store
      i32.const -1
      return
    end
    local.get 0
    i64.const 0
    i64.store offset=4 align=4
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
    i32.const 0)
  (func $__memcpy (type 1) (param i32 i32 i32) (result i32)
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
            i32.const 0
            i32.le_s
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
      local.get 3
      i32.const -4
      i32.and
      local.set 4
      block  ;; label = @2
        local.get 3
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
  (func $__fwritex (type 1) (param i32 i32 i32) (result i32)
    (local i32 i32 i32)
    block  ;; label = @1
      local.get 2
      i32.load offset=16
      local.tee 3
      i32.eqz
      if  ;; label = @2
        local.get 2
        call $__towrite
        br_if 1 (;@1;)
        local.get 2
        i32.load offset=16
        local.set 3
      end
      local.get 1
      local.get 3
      local.get 2
      i32.load offset=20
      local.tee 4
      i32.sub
      i32.gt_u
      if  ;; label = @2
        local.get 2
        local.get 0
        local.get 1
        local.get 2
        i32.load offset=36
        call_indirect (type 1)
        return
      end
      block  ;; label = @2
        block  ;; label = @3
          local.get 2
          i32.load offset=80
          i32.const 0
          i32.lt_s
          br_if 0 (;@3;)
          local.get 1
          i32.eqz
          br_if 0 (;@3;)
          local.get 1
          local.set 3
          loop  ;; label = @4
            local.get 0
            local.get 3
            i32.add
            local.tee 5
            i32.const 1
            i32.sub
            i32.load8_u
            i32.const 10
            i32.ne
            if  ;; label = @5
              local.get 3
              i32.const 1
              i32.sub
              local.tee 3
              br_if 1 (;@4;)
              br 2 (;@3;)
            end
          end
          local.get 2
          local.get 0
          local.get 3
          local.get 2
          i32.load offset=36
          call_indirect (type 1)
          local.tee 4
          local.get 3
          i32.lt_u
          br_if 2 (;@1;)
          local.get 1
          local.get 3
          i32.sub
          local.set 1
          local.get 2
          i32.load offset=20
          local.set 4
          br 1 (;@2;)
        end
        local.get 0
        local.set 5
        i32.const 0
        local.set 3
      end
      local.get 4
      local.get 5
      local.get 1
      call $__memcpy
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
  (func $fwrite (type 5) (param i32 i32 i32 i32) (result i32)
    (local i32 i32)
    local.get 1
    local.get 2
    i32.mul
    local.set 4
    block  ;; label = @1
      local.get 3
      i32.load offset=76
      i32.const 0
      i32.lt_s
      if  ;; label = @2
        local.get 0
        local.get 4
        local.get 3
        call $__fwritex
        local.set 0
        br 1 (;@1;)
      end
      local.get 3
      call $__lockfile
      local.set 5
      local.get 0
      local.get 4
      local.get 3
      call $__fwritex
      local.set 0
      local.get 5
      i32.eqz
      br_if 0 (;@1;)
      local.get 3
      call $__unlockfile
    end
    local.get 0
    local.get 4
    i32.eq
    if  ;; label = @1
      local.get 2
      i32.const 0
      local.get 1
      select
      return
    end
    local.get 0
    local.get 1
    i32.div_u)
  (func $__errno_location (type 4) (result i32)
    i32.const 1744)
  (func $__lock (type 0) (param i32)
    nop)
  (func $__ofl_lock (type 4) (result i32)
    i32.const 1748
    call $__lock
    i32.const 1752)
  (func $__stdio_write (type 1) (param i32 i32 i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee 3
    global.set $__stack_pointer
    local.get 3
    local.get 0
    i32.load offset=28
    local.tee 4
    i32.store offset=16
    local.get 0
    i32.load offset=20
    local.set 5
    local.get 3
    local.get 2
    i32.store offset=28
    local.get 3
    local.get 1
    i32.store offset=24
    local.get 3
    local.get 5
    local.get 4
    i32.sub
    local.tee 1
    i32.store offset=20
    local.get 1
    local.get 2
    i32.add
    local.set 6
    local.get 3
    i32.const 16
    i32.add
    local.set 4
    i32.const 2
    local.set 7
    block (result i32)  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          block  ;; label = @4
            local.get 0
            i32.load offset=60
            local.get 3
            i32.const 16
            i32.add
            i32.const 2
            local.get 3
            i32.const 12
            i32.add
            call $__wasi_fd_write
            call $__wasi_syscall_ret
            if  ;; label = @5
              local.get 4
              local.set 5
              br 1 (;@4;)
            end
            loop  ;; label = @5
              local.get 6
              local.get 3
              i32.load offset=12
              local.tee 1
              i32.eq
              br_if 2 (;@3;)
              local.get 1
              i32.const 0
              i32.lt_s
              if  ;; label = @6
                local.get 4
                local.set 5
                br 4 (;@2;)
              end
              local.get 4
              local.get 1
              local.get 4
              i32.load offset=4
              local.tee 8
              i32.gt_u
              local.tee 9
              i32.const 3
              i32.shl
              i32.add
              local.tee 5
              local.get 1
              local.get 8
              i32.const 0
              local.get 9
              select
              i32.sub
              local.tee 8
              local.get 5
              i32.load
              i32.add
              i32.store
              local.get 4
              i32.const 12
              i32.const 4
              local.get 9
              select
              i32.add
              local.tee 4
              local.get 4
              i32.load
              local.get 8
              i32.sub
              i32.store
              local.get 6
              local.get 1
              i32.sub
              local.set 6
              local.get 0
              i32.load offset=60
              local.get 5
              local.tee 4
              local.get 7
              local.get 9
              i32.sub
              local.tee 7
              local.get 3
              i32.const 12
              i32.add
              call $__wasi_fd_write
              call $__wasi_syscall_ret
              i32.eqz
              br_if 0 (;@5;)
            end
          end
          local.get 6
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
      local.tee 1
      local.get 7
      i32.const 2
      i32.eq
      br_if 0 (;@1;)
      drop
      local.get 2
      local.get 5
      i32.load offset=4
      i32.sub
    end
    local.set 1
    local.get 3
    i32.const 32
    i32.add
    global.set $__stack_pointer
    local.get 1)
  (func $__emscripten_stdout_close (type 3) (param i32) (result i32)
    i32.const 0)
  (func $__emscripten_stdout_seek (type 6) (param i32 i64 i32) (result i64)
    i64.const 0)
  (func $strlen (type 3) (param i32) (result i32)
    (local i32 i32 i32)
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        local.tee 1
        i32.const 3
        i32.and
        i32.eqz
        br_if 0 (;@2;)
        local.get 1
        i32.load8_u
        i32.eqz
        if  ;; label = @3
          i32.const 0
          return
        end
        loop  ;; label = @3
          local.get 1
          i32.const 1
          i32.add
          local.tee 1
          i32.const 3
          i32.and
          i32.eqz
          br_if 1 (;@2;)
          local.get 1
          i32.load8_u
          br_if 0 (;@3;)
        end
        br 1 (;@1;)
      end
      loop  ;; label = @2
        local.get 1
        local.tee 2
        i32.const 4
        i32.add
        local.set 1
        i32.const 16843008
        local.get 2
        i32.load
        local.tee 3
        i32.sub
        local.get 3
        i32.or
        i32.const -2139062144
        i32.and
        i32.const -2139062144
        i32.eq
        br_if 0 (;@2;)
      end
      loop  ;; label = @2
        local.get 2
        local.tee 1
        i32.const 1
        i32.add
        local.set 2
        local.get 1
        i32.load8_u
        br_if 0 (;@2;)
      end
    end
    local.get 1
    local.get 0
    i32.sub)
  (func $__wasi_syscall_ret (type 3) (param i32) (result i32)
    local.get 0
    i32.eqz
    if  ;; label = @1
      i32.const 0
      return
    end
    call $__errno_location
    local.get 0
    i32.store
    i32.const -1)
  (func $_emscripten_stack_restore (type 0) (param i32)
    local.get 0
    global.set $__stack_pointer)
  (func $emscripten_stack_get_current (type 4) (result i32)
    global.get $__stack_pointer)
  (table (;0;) 5 5 funcref)
  (memory (;0;) 258 258)
  (global $__stack_pointer (mut i32) (i32.const 68336))
  (export "memory" (memory 0))
  (export "main" (func $main))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func $_start))
  (export "_emscripten_stack_restore" (func $_emscripten_stack_restore))
  (export "emscripten_stack_get_current" (func $emscripten_stack_get_current))
  (elem (;0;) (i32.const 1) func $__wasm_call_ctors $__emscripten_stdout_close $__stdio_write $__emscripten_stdout_seek)
  (data $.rodata (i32.const 1024) ">THREE Homo sapiens frequency\0a\00>ONE Homo sapiens alu\0a\00>TWO IUB ambiguity codes\0a\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00a\00\00\00q=\8a>c\00\00\00\8f\c2\f5=g\00\00\00\8f\c2\f5=t\00\00\00q=\8a>B\00\00\00\0a\d7\a3<D\00\00\00\0a\d7\a3<H\00\00\00\0a\d7\a3<K\00\00\00\0a\d7\a3<M\00\00\00\0a\d7\a3<N\00\00\00\0a\d7\a3<R\00\00\00\0a\d7\a3<S\00\00\00\0a\d7\a3<V\00\00\00\0a\d7\a3<W\00\00\00\0a\d7\a3<Y\00\00\00\0a\d7\a3<\00\00\00\00\00\00\00\00a\00\00\00\e9\1c\9b>c\00\00\00r\bdJ>g\00\00\00\d7IJ>t\00\00\00r_\9a>(\06")
  (data $.data (i32.const 1560) "*\00\00\00\01\00\00\00\01\00\00\00\00\00\00\00\05")
  (data $.data.1 (i32.const 1588) "\02")
  (data $.data.2 (i32.const 1612) "\03\00\00\00\04\00\00\00\e8\06\00\00\00\04")
  (data $.data.3 (i32.const 1636) "\01")
  (data $.data.4 (i32.const 1652) "\ff\ff\ff\ff\0a")
  (data $.data.5 (i32.const 1720) "(\06"))
