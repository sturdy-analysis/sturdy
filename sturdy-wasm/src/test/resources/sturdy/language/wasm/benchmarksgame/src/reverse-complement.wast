(module $reverse-complement.wasm
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func (param i32)))
  (type (;2;) (func (param i32 i32 i32) (result i32)))
  (type (;3;) (func))
  (type (;4;) (func (result i32)))
  (type (;5;) (func (param i32 i64 i32) (result i64)))
  (type (;6;) (func (param i32 i32) (result i32)))
  (type (;7;) (func (param i32 i32 i32 i32) (result i32)))
  (type (;8;) (func (param i32 i64 i32 i32) (result i32)))
  (import "env" "malloc" (func $malloc (type 0)))
  (import "env" "realloc" (func $realloc (type 6)))
  (import "env" "free" (func $free (type 1)))
  (import "wasi_snapshot_preview1" "proc_exit" (func $__wasi_proc_exit (type 1)))
  (import "wasi_snapshot_preview1" "fd_read" (func $__wasi_fd_read (type 7)))
  (import "wasi_snapshot_preview1" "fd_close" (func $__wasi_fd_close (type 0)))
  (import "wasi_snapshot_preview1" "fd_seek" (func $__wasi_fd_seek (type 8)))
  (import "wasi_snapshot_preview1" "fd_write" (func $__wasi_fd_write (type 7)))
  (func $__wasm_call_ctors (type 3)
    nop)
  (func $__original_main (type 4) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    i32.const 1072
    i32.load
    local.tee 1
    i32.load8_u
    local.tee 0
    if  ;; label = @1
      loop  ;; label = @2
        local.get 0
        i32.extend8_s
        call $toupper
        i32.const 1376
        i32.add
        local.get 1
        i32.load8_u offset=1
        local.tee 0
        i32.store8
        local.get 1
        i32.load8_s
        call $tolower
        i32.const 1376
        i32.add
        local.get 0
        i32.store8
        local.get 1
        i32.load8_u offset=2
        local.set 0
        local.get 1
        i32.const 2
        i32.add
        local.set 1
        local.get 0
        br_if 0 (;@2;)
      end
    end
    i32.const 8192
    local.set 1
    i32.const 8192
    call $malloc
    local.set 3
    i32.const 7936
    local.set 0
    block  ;; label = @1
      i32.const 1060
      i32.load
      call $fileno
      local.tee 5
      local.get 3
      i32.const 7936
      call $read
      local.tee 4
      i32.eqz
      if  ;; label = @2
        local.get 3
        local.set 2
        br 1 (;@1;)
      end
      loop  ;; label = @2
        local.get 0
        local.get 4
        local.get 6
        i32.add
        local.tee 6
        i32.le_u
        if  ;; label = @3
          local.get 3
          local.get 1
          i32.const -1048576
          i32.sub
          local.get 1
          i32.const 1
          i32.shl
          local.get 1
          i32.const 1048575
          i32.gt_u
          select
          local.tee 1
          call $realloc
          local.set 3
        end
        local.get 5
        local.get 3
        local.get 6
        i32.add
        local.tee 2
        local.get 1
        i32.const 256
        i32.sub
        local.tee 0
        local.get 6
        i32.sub
        call $read
        local.tee 4
        br_if 0 (;@2;)
      end
    end
    local.get 2
    i32.const 62
    i32.store8
    local.get 2
    i32.const 1
    i32.sub
    local.set 4
    loop  ;; label = @1
      local.get 4
      local.set 5
      loop  ;; label = @2
        local.get 4
        local.tee 1
        i32.const 1
        i32.sub
        local.set 4
        local.get 1
        i32.load8_u
        i32.const 62
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 1
      i32.const 1
      i32.add
      local.set 0
      loop  ;; label = @2
        local.get 0
        i32.load8_u
        local.set 2
        local.get 0
        i32.const 1
        i32.add
        local.tee 1
        local.set 0
        local.get 2
        i32.const 10
        i32.ne
        br_if 0 (;@2;)
      end
      block  ;; label = @2
        local.get 5
        local.get 1
        i32.sub
        i32.const 61
        i32.rem_u
        local.tee 2
        i32.const 60
        i32.eq
        br_if 0 (;@2;)
        local.get 1
        local.get 2
        i32.add
        local.tee 0
        local.get 5
        i32.ge_u
        br_if 0 (;@2;)
        i32.const 60
        local.get 2
        i32.sub
        local.set 2
        loop  ;; label = @3
          local.get 0
          i32.const 1
          i32.add
          local.get 0
          local.get 2
          call $memmove
          drop
          local.get 0
          i32.const 10
          i32.store8
          local.get 0
          i32.const 61
          i32.add
          local.tee 0
          local.get 5
          i32.lt_u
          br_if 0 (;@3;)
        end
      end
      local.get 5
      i32.const 1
      i32.sub
      local.tee 0
      local.get 1
      i32.ge_u
      if  ;; label = @2
        loop  ;; label = @3
          local.get 1
          i32.load8_s
          i32.const 1376
          i32.add
          i32.load8_u
          local.set 2
          local.get 1
          local.get 0
          i32.load8_s
          i32.const 1376
          i32.add
          i32.load8_u
          i32.store8
          local.get 0
          local.get 2
          i32.store8
          local.get 1
          i32.const 1
          i32.add
          local.tee 1
          local.get 0
          i32.const 1
          i32.sub
          local.tee 0
          i32.le_u
          br_if 0 (;@3;)
        end
      end
      local.get 3
      local.get 4
      i32.le_u
      br_if 0 (;@1;)
    end
    i32.const 1064
    i32.load
    call $fileno
    local.get 3
    local.get 6
    call $write
    drop
    local.get 3
    call $free
    i32.const 0)
  (func $main (type 6) (param i32 i32) (result i32)
    call $__original_main)
  (func $_start (type 3)
    call $__wasm_call_ctors
    call $__original_main
    call $exit
    unreachable)
  (func $dummy (type 3)
    nop)
  (func $libc_exit_fini (type 3)
    call $dummy)
  (func $exit (type 1) (param i32)
    call $dummy
    call $libc_exit_fini
    call $dummy
    local.get 0
    call $_Exit
    unreachable)
  (func $_Exit (type 1) (param i32)
    local.get 0
    call $__wasi_proc_exit
    unreachable)
  (func $__memcpy (type 2) (param i32 i32 i32) (result i32)
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
  (func $memmove (type 2) (param i32 i32 i32) (result i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      local.get 1
      i32.eq
      br_if 0 (;@1;)
      local.get 1
      local.get 0
      local.get 2
      i32.add
      local.tee 3
      i32.sub
      i32.const 0
      local.get 2
      i32.const 1
      i32.shl
      i32.sub
      i32.le_u
      if  ;; label = @2
        local.get 0
        local.get 1
        local.get 2
        call $__memcpy
        return
      end
      local.get 0
      local.get 1
      i32.xor
      i32.const 3
      i32.and
      local.set 4
      block  ;; label = @2
        block  ;; label = @3
          local.get 0
          local.get 1
          i32.lt_u
          if  ;; label = @4
            local.get 4
            if  ;; label = @5
              local.get 0
              local.set 3
              br 3 (;@2;)
            end
            local.get 0
            i32.const 3
            i32.and
            i32.eqz
            if  ;; label = @5
              local.get 0
              local.set 3
              br 2 (;@3;)
            end
            local.get 0
            local.set 3
            loop  ;; label = @5
              local.get 2
              i32.eqz
              br_if 4 (;@1;)
              local.get 3
              local.get 1
              i32.load8_u
              i32.store8
              local.get 1
              i32.const 1
              i32.add
              local.set 1
              local.get 2
              i32.const 1
              i32.sub
              local.set 2
              local.get 3
              i32.const 1
              i32.add
              local.tee 3
              i32.const 3
              i32.and
              br_if 0 (;@5;)
            end
            br 1 (;@3;)
          end
          block  ;; label = @4
            local.get 4
            br_if 0 (;@4;)
            local.get 3
            i32.const 3
            i32.and
            if  ;; label = @5
              loop  ;; label = @6
                local.get 2
                i32.eqz
                br_if 5 (;@1;)
                local.get 0
                local.get 2
                i32.const 1
                i32.sub
                local.tee 2
                i32.add
                local.tee 3
                local.get 1
                local.get 2
                i32.add
                i32.load8_u
                i32.store8
                local.get 3
                i32.const 3
                i32.and
                br_if 0 (;@6;)
              end
            end
            local.get 2
            i32.const 3
            i32.le_u
            br_if 0 (;@4;)
            loop  ;; label = @5
              local.get 0
              local.get 2
              i32.const 4
              i32.sub
              local.tee 2
              i32.add
              local.get 1
              local.get 2
              i32.add
              i32.load
              i32.store
              local.get 2
              i32.const 3
              i32.gt_u
              br_if 0 (;@5;)
            end
          end
          local.get 2
          i32.eqz
          br_if 2 (;@1;)
          loop  ;; label = @4
            local.get 0
            local.get 2
            i32.const 1
            i32.sub
            local.tee 2
            i32.add
            local.get 1
            local.get 2
            i32.add
            i32.load8_u
            i32.store8
            local.get 2
            br_if 0 (;@4;)
          end
          br 2 (;@1;)
        end
        local.get 2
        i32.const 3
        i32.le_u
        br_if 0 (;@2;)
        loop  ;; label = @3
          local.get 3
          local.get 1
          i32.load
          i32.store
          local.get 1
          i32.const 4
          i32.add
          local.set 1
          local.get 3
          i32.const 4
          i32.add
          local.set 3
          local.get 2
          i32.const 4
          i32.sub
          local.tee 2
          i32.const 3
          i32.gt_u
          br_if 0 (;@3;)
        end
      end
      local.get 2
      i32.eqz
      br_if 0 (;@1;)
      loop  ;; label = @2
        local.get 3
        local.get 1
        i32.load8_u
        i32.store8
        local.get 3
        i32.const 1
        i32.add
        local.set 3
        local.get 1
        i32.const 1
        i32.add
        local.set 1
        local.get 2
        i32.const 1
        i32.sub
        local.tee 2
        br_if 0 (;@2;)
      end
    end
    local.get 0)
  (func $__lockfile (type 0) (param i32) (result i32)
    i32.const 1)
  (func $__unlockfile (type 1) (param i32)
    nop)
  (func $__errno_location (type 4) (result i32)
    i32.const 1504)
  (func $fileno (type 0) (param i32) (result i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      i32.load offset=76
      i32.const 0
      i32.lt_s
      if  ;; label = @2
        local.get 0
        i32.load offset=60
        local.set 1
        br 1 (;@1;)
      end
      local.get 0
      call $__lockfile
      local.set 2
      local.get 0
      i32.load offset=60
      local.set 1
      local.get 2
      i32.eqz
      br_if 0 (;@1;)
      local.get 0
      call $__unlockfile
    end
    local.get 1
    i32.const 0
    i32.lt_s
    if  ;; label = @1
      call $__errno_location
      i32.const 8
      i32.store
      i32.const -1
      local.set 1
    end
    local.get 1)
  (func $read (type 2) (param i32 i32 i32) (result i32)
    (local i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 3
    global.set $__stack_pointer
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
    call $__wasi_fd_read
    call $__wasi_syscall_ret
    local.set 2
    local.get 3
    i32.load offset=4
    local.set 1
    local.get 3
    i32.const 16
    i32.add
    global.set $__stack_pointer
    i32.const -1
    local.get 1
    local.get 2
    select)
  (func $dummy_23 (type 0) (param i32) (result i32)
    local.get 0)
  (func $__stdio_close (type 0) (param i32) (result i32)
    local.get 0
    i32.load offset=60
    call $dummy_23
    call $__wasi_fd_close
    call $__wasi_syscall_ret)
  (func $__stdio_read (type 2) (param i32 i32 i32) (result i32)
    (local i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee 3
    global.set $__stack_pointer
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
    local.set 6
    local.get 3
    local.get 4
    i32.store offset=28
    local.get 3
    local.get 6
    i32.store offset=24
    i32.const 32
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
        call $__wasi_fd_read
        call $__wasi_syscall_ret
        i32.eqz
        if  ;; label = @3
          local.get 3
          i32.load offset=12
          local.tee 4
          i32.const 0
          i32.gt_s
          br_if 1 (;@2;)
          i32.const 32
          i32.const 16
          local.get 4
          select
          local.set 4
        end
        local.get 0
        local.get 0
        i32.load
        local.get 4
        i32.or
        i32.store
        br 1 (;@1;)
      end
      local.get 4
      local.set 5
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
      local.set 5
    end
    local.get 3
    i32.const 32
    i32.add
    global.set $__stack_pointer
    local.get 5)
  (func $__lseek (type 5) (param i32 i64 i32) (result i64)
    (local i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 3
    global.set $__stack_pointer
    local.get 0
    local.get 1
    local.get 2
    i32.const 255
    i32.and
    local.get 3
    i32.const 8
    i32.add
    call $__wasi_fd_seek
    call $__wasi_syscall_ret
    local.set 2
    local.get 3
    i64.load offset=8
    local.set 1
    local.get 3
    i32.const 16
    i32.add
    global.set $__stack_pointer
    i64.const -1
    local.get 1
    local.get 2
    select)
  (func $__stdio_seek (type 5) (param i32 i64 i32) (result i64)
    local.get 0
    i32.load offset=60
    local.get 1
    local.get 2
    call $__lseek)
  (func $__stdio_write (type 2) (param i32 i32 i32) (result i32)
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
  (func $__emscripten_stdout_close (type 0) (param i32) (result i32)
    i32.const 0)
  (func $__emscripten_stdout_seek (type 5) (param i32 i64 i32) (result i64)
    i64.const 0)
  (func $tolower (type 0) (param i32) (result i32)
    local.get 0
    i32.const 32
    i32.or
    local.get 0
    local.get 0
    i32.const 65
    i32.sub
    i32.const 26
    i32.lt_u
    select)
  (func $toupper (type 0) (param i32) (result i32)
    local.get 0
    i32.const 95
    i32.and
    local.get 0
    local.get 0
    i32.const 97
    i32.sub
    i32.const 26
    i32.lt_u
    select)
  (func $__wasi_syscall_ret (type 0) (param i32) (result i32)
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
  (func $write (type 2) (param i32 i32 i32) (result i32)
    (local i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 3
    global.set $__stack_pointer
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
    call $__wasi_fd_write
    call $__wasi_syscall_ret
    local.set 2
    local.get 3
    i32.load offset=4
    local.set 1
    local.get 3
    i32.const 16
    i32.add
    global.set $__stack_pointer
    i32.const -1
    local.get 1
    local.get 2
    select)
  (func $_emscripten_stack_restore (type 1) (param i32)
    local.get 0
    global.set $__stack_pointer)
  (func $emscripten_stack_get_current (type 4) (result i32)
    global.get $__stack_pointer)
  (table (;0;) 8 8 funcref)
  (memory (;0;) 258 258)
  (global $__stack_pointer (mut i32) (i32.const 69136))
  (export "memory" (memory 0))
  (export "main" (func $main))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func $_start))
  (export "_emscripten_stack_restore" (func $_emscripten_stack_restore))
  (export "emscripten_stack_get_current" (func $emscripten_stack_get_current))
  (elem (;0;) (i32.const 1) func $__wasm_call_ctors $__stdio_close $__stdio_read $__stdio_seek $__emscripten_stdout_close $__stdio_write $__emscripten_stdout_seek)
  (data $.rodata (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a\00\008\04\00\00\c8\04")
  (data $.data (i32.const 1073) "\04\00\00\00\00\00\00\09")
  (data $.data.1 (i32.const 1092) "\02")
  (data $.data.2 (i32.const 1112) "\03\00\00\00\00\00\00\00\04\00\00\00\f8\05\00\00\00\04")
  (data $.data.3 (i32.const 1156) "\ff\ff\ff\ff")
  (data $.data.4 (i32.const 1224) "\05")
  (data $.data.5 (i32.const 1236) "\05")
  (data $.data.6 (i32.const 1260) "\06\00\00\00\07\00\00\00\08\0a\00\00\00\04")
  (data $.data.7 (i32.const 1284) "\01")
  (data $.data.8 (i32.const 1300) "\ff\ff\ff\ff\0a"))
