(module $k-nucleotide.wasm
  (type (;0;) (func (param i32 i32 i32) (result i32)))
  (type (;1;) (func (param i32) (result i32)))
  (type (;2;) (func (param i32)))
  (type (;3;) (func (param i32 i32) (result i32)))
  (type (;4;) (func))
  (type (;5;) (func (result i32)))
  (type (;6;) (func (param i32 i32 i32)))
  (type (;7;) (func (param i32 i64 i32) (result i64)))
  (type (;8;) (func (param i32 i32)))
  (type (;9;) (func (param i32 i32 i32 i32) (result i32)))
  (type (;10;) (func (param i32 i64 i32 i32) (result i32)))
  (type (;11;) (func (param i32 i64 i32) (result i32)))
  (type (;12;) (func (param i32 i32 i32 i32 i32)))
  (type (;13;) (func (param i32 i32 i32 i32 i32 i32)))
  (type (;14;) (func (param i32 i32 i32 i32 i32 i32 i32 i32)))
  (type (;15;) (func (param i32 i32 i32 i32)))
  (import "env" "malloc" (func $malloc (type 1)))
  (import "env" "realloc" (func $realloc (type 3)))
  (import "env" "free" (func $free (type 2)))
  (import "env" "calloc" (func $calloc (type 3)))
  (import "wasi_snapshot_preview1" "proc_exit" (func $__wasi_proc_exit (type 2)))
  (import "wasi_snapshot_preview1" "fd_close" (func $__wasi_fd_close (type 1)))
  (import "wasi_snapshot_preview1" "fd_read" (func $__wasi_fd_read (type 9)))
  (import "wasi_snapshot_preview1" "fd_seek" (func $__wasi_fd_seek (type 10)))
  (func $__wasm_call_ctors (type 4)
    nop)
  (func $__original_main (type 5) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 4096
    i32.sub
    local.tee 2
    global.set $__stack_pointer
    i32.const 1088
    i32.load
    local.set 4
    loop  ;; label = @1
      local.get 2
      i32.const 4096
      local.get 4
      call $fgets
      if  ;; label = @2
        i32.const 1067
        local.get 2
        i32.const 6
        call $memcmp
        br_if 1 (;@1;)
      end
    end
    i32.const 1048576
    local.set 5
    i32.const 1048576
    call $malloc
    local.set 3
    block  ;; label = @1
      local.get 2
      i32.const 4096
      local.get 4
      call $fgets
      i32.eqz
      if  ;; label = @2
        br 1 (;@1;)
      end
      local.get 2
      i32.load8_u
      local.tee 1
      i32.const 62
      i32.eq
      if  ;; label = @2
        br 1 (;@1;)
      end
      loop  ;; label = @2
        i32.const 0
        local.set 6
        loop  ;; label = @3
          block  ;; label = @4
            block  ;; label = @5
              block  ;; label = @6
                local.get 1
                i32.const 255
                i32.and
                br_table 0 (;@6;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 2 (;@4;) 1 (;@5;)
              end
              local.get 5
              local.get 0
              i32.sub
              i32.const 4096
              i32.lt_u
              if  ;; label = @6
                local.get 3
                local.get 5
                i32.const 1
                i32.shl
                local.tee 5
                call $realloc
                local.set 3
              end
              local.get 2
              i32.const 4096
              local.get 4
              call $fgets
              i32.eqz
              br_if 4 (;@1;)
              local.get 2
              i32.load8_u
              local.tee 1
              i32.const 62
              i32.ne
              br_if 3 (;@2;)
              br 4 (;@1;)
            end
            local.get 0
            local.get 3
            i32.add
            local.get 1
            i32.const 7
            i32.and
            i32.const 1079
            i32.add
            i32.load8_u
            i32.store8
            local.get 0
            i32.const 1
            i32.add
            local.set 0
          end
          local.get 2
          local.get 6
          i32.const 1
          i32.add
          local.tee 6
          i32.add
          i32.load8_u
          local.set 1
          br 0 (;@3;)
        end
        unreachable
      end
      unreachable
    end
    local.get 3
    local.get 0
    call $realloc
    local.tee 1
    local.get 0
    i32.const 1048
    call $generate_Count_For_Oligonucleotide
    local.get 1
    local.get 0
    i32.const 1031
    call $generate_Count_For_Oligonucleotide
    local.get 1
    local.get 0
    i32.const 1024
    call $generate_Count_For_Oligonucleotide
    local.get 1
    local.get 0
    i32.const 1074
    call $generate_Count_For_Oligonucleotide
    local.get 1
    local.get 0
    i32.const 1044
    call $generate_Count_For_Oligonucleotide
    local.get 1
    local.get 0
    i32.const 2
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get 1
    local.get 0
    i32.const 1
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get 1
    call $free
    local.get 2
    i32.const 4096
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (func $generate_Count_For_Oligonucleotide (type 6) (param i32 i32 i32)
    (local i64 i64 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 10
    global.set $__stack_pointer
    local.get 2
    call $strlen
    local.tee 9
    i32.const 1
    i32.sub
    local.set 6
    i64.const -1
    local.get 9
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const -1
    i64.xor
    local.set 4
    i32.const 1
    i32.const 28
    call $calloc
    local.set 7
    block  ;; label = @1
      local.get 9
      i32.const 2
      i32.lt_s
      br_if 0 (;@1;)
      local.get 6
      i32.const 3
      i32.and
      local.set 11
      block  ;; label = @2
        local.get 9
        i32.const 2
        i32.sub
        i32.const 3
        i32.lt_u
        if  ;; label = @3
          i32.const 0
          local.set 2
          br 1 (;@2;)
        end
        local.get 6
        i32.const -4
        i32.and
        local.set 13
        i32.const 0
        local.set 2
        loop  ;; label = @3
          local.get 0
          local.get 2
          i32.add
          local.tee 5
          i64.load8_s
          local.get 3
          i64.const 2
          i64.shl
          local.get 4
          i64.and
          i64.or
          i64.const 2
          i64.shl
          local.get 4
          i64.and
          local.get 5
          i64.load8_s offset=1
          i64.or
          i64.const 2
          i64.shl
          local.get 4
          i64.and
          local.get 5
          i64.load8_s offset=2
          i64.or
          i64.const 2
          i64.shl
          local.get 4
          i64.and
          local.get 5
          i64.load8_s offset=3
          i64.or
          local.set 3
          local.get 2
          i32.const 4
          i32.add
          local.set 2
          local.get 12
          i32.const 4
          i32.add
          local.tee 12
          local.get 13
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 11
      i32.eqz
      br_if 0 (;@1;)
      loop  ;; label = @2
        local.get 0
        local.get 2
        i32.add
        i64.load8_s
        local.get 3
        i64.const 2
        i64.shl
        local.get 4
        i64.and
        i64.or
        local.set 3
        local.get 2
        i32.const 1
        i32.add
        local.set 2
        local.get 8
        i32.const 1
        i32.add
        local.tee 8
        local.get 11
        i32.ne
        br_if 0 (;@2;)
      end
    end
    i32.const 0
    local.set 8
    local.get 1
    local.get 9
    i32.ge_s
    if  ;; label = @1
      loop  ;; label = @2
        local.get 7
        local.get 0
        local.get 6
        i32.add
        i64.load8_s
        local.get 3
        i64.const 2
        i64.shl
        local.get 4
        i64.and
        i64.or
        local.tee 3
        local.get 10
        i32.const 12
        i32.add
        call $kh_put_oligonucleotide
        local.set 2
        i32.const 1
        local.set 5
        local.get 7
        i32.load offset=24
        local.tee 8
        local.get 2
        i32.const 2
        i32.shl
        i32.add
        local.tee 2
        local.get 10
        i32.load offset=12
        i32.eqz
        if  ;; label = @3
          local.get 2
          i32.load
          i32.const 1
          i32.add
          local.set 5
        end
        local.get 5
        i32.store
        local.get 6
        i32.const 1
        i32.add
        local.tee 6
        local.get 1
        i32.ne
        br_if 0 (;@2;)
      end
    end
    local.get 7
    i32.load offset=20
    call $free
    local.get 7
    i32.load offset=16
    call $free
    local.get 8
    call $free
    local.get 7
    call $free
    local.get 10
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $generate_Frequencies_For_Desired_Length_Oligonucleotides (type 6) (param i32 i32 i32)
    (local i32 i32 i32 i32 i64 i32 i32 i32 i32 i64 i32 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 10
    global.set $__stack_pointer
    i32.const 1
    i32.const 28
    call $calloc
    local.set 4
    local.get 2
    i32.const 1
    i32.sub
    local.tee 3
    if (result i64)  ;; label = @1
      local.get 0
      i64.load8_u
    else
      i64.const 0
    end
    local.set 7
    block  ;; label = @1
      block (result i32)  ;; label = @2
        local.get 1
        local.get 2
        i32.ge_s
        if  ;; label = @3
          i64.const -1
          local.get 2
          i32.const 1
          i32.shl
          i64.extend_i32_u
          i64.shl
          i64.const -1
          i64.xor
          local.set 12
          loop  ;; label = @4
            local.get 4
            local.get 0
            local.get 3
            i32.add
            i64.load8_s
            local.get 7
            i64.const 2
            i64.shl
            local.get 12
            i64.and
            i64.or
            local.tee 7
            local.get 10
            i32.const 12
            i32.add
            call $kh_put_oligonucleotide
            local.set 5
            i32.const 1
            local.set 6
            local.get 4
            i32.load offset=24
            local.tee 9
            local.get 5
            i32.const 2
            i32.shl
            i32.add
            local.tee 5
            local.get 10
            i32.load offset=12
            i32.eqz
            if  ;; label = @5
              local.get 5
              i32.load
              i32.const 1
              i32.add
              local.set 6
            end
            local.get 6
            i32.store
            local.get 3
            i32.const 1
            i32.add
            local.tee 3
            local.get 1
            i32.ne
            br_if 0 (;@4;)
          end
          local.get 4
          i32.load
          local.set 11
          local.get 4
          i32.load offset=4
          local.tee 8
          i32.const 4
          i32.shl
          call $malloc
          local.tee 1
          local.get 11
          i32.eqz
          br_if 1 (;@2;)
          drop
          local.get 4
          i32.load offset=16
          local.set 5
          i32.const 0
          local.set 6
          i32.const 0
          local.set 3
          loop  ;; label = @4
            local.get 5
            local.get 3
            i32.const 2
            i32.shr_u
            i32.const 1073741820
            i32.and
            i32.add
            i32.load
            local.get 3
            i32.const 1
            i32.shl
            i32.shr_u
            i32.const 3
            i32.and
            i32.eqz
            if  ;; label = @5
              local.get 4
              i32.load offset=20
              local.get 3
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.set 7
              local.get 9
              local.get 3
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.set 13
              local.get 1
              local.get 6
              i32.const 4
              i32.shl
              i32.add
              local.tee 0
              i32.const 0
              i32.store offset=12
              local.get 0
              local.get 13
              i32.store offset=8
              local.get 0
              local.get 7
              i64.store
              local.get 6
              i32.const 1
              i32.add
              local.set 6
            end
            local.get 3
            i32.const 1
            i32.add
            local.tee 3
            local.get 11
            i32.ne
            br_if 0 (;@4;)
          end
          br 2 (;@1;)
        end
        i32.const 0
        call $malloc
      end
      local.set 1
      local.get 4
      i32.load offset=16
      local.set 5
    end
    local.get 4
    i32.load offset=20
    call $free
    local.get 5
    call $free
    local.get 9
    call $free
    local.get 4
    call $free
    local.get 1
    local.get 8
    i32.const 16
    i32.const 1
    call $qsort
    block  ;; label = @1
      local.get 8
      i32.const 0
      i32.le_s
      br_if 0 (;@1;)
      local.get 8
      i32.const 1
      i32.and
      local.set 0
      i32.const 0
      local.set 3
      local.get 8
      i32.const 1
      i32.ne
      if  ;; label = @2
        local.get 8
        i32.const 2147483646
        i32.and
        local.set 9
        local.get 2
        i32.const 1
        i32.gt_u
        local.set 5
        i32.const 0
        local.set 6
        loop  ;; label = @3
          local.get 1
          local.get 3
          i32.const 4
          i32.shl
          i32.add
          local.tee 4
          local.get 4
          i64.load
          local.tee 7
          i64.const 4
          i64.shr_u
          local.get 7
          i64.const 2
          i64.shr_u
          local.get 5
          select
          i64.store
          local.get 4
          local.tee 14
          i32.const 16
          i32.add
          local.tee 4
          local.get 14
          i64.load offset=16
          local.tee 7
          i64.const 4
          i64.shr_u
          local.get 7
          i64.const 2
          i64.shr_u
          local.get 5
          select
          i64.store
          local.get 3
          i32.const 2
          i32.add
          local.set 3
          local.get 6
          i32.const 2
          i32.add
          local.tee 6
          local.get 9
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 0
      i32.eqz
      br_if 0 (;@1;)
      local.get 1
      local.get 3
      i32.const 4
      i32.shl
      i32.add
      local.tee 3
      local.get 3
      i64.load
      i64.const 4
      i64.const 2
      local.get 2
      i32.const 1
      i32.gt_u
      select
      i64.shr_u
      i64.store
    end
    local.get 1
    call $free
    local.get 10
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $kh_put_oligonucleotide (type 11) (param i32 i64 i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        i32.load offset=8
        local.get 0
        i32.load offset=12
        i32.lt_u
        br_if 0 (;@2;)
        local.get 0
        i32.load
        local.tee 3
        local.get 0
        i32.load offset=4
        i32.const 1
        i32.shl
        i32.gt_u
        if  ;; label = @3
          local.get 0
          local.get 3
          i32.const 1
          i32.sub
          call $kh_resize_oligonucleotide
          i32.const 0
          i32.ge_s
          br_if 1 (;@2;)
          br 2 (;@1;)
        end
        local.get 0
        local.get 3
        i32.const 1
        i32.add
        call $kh_resize_oligonucleotide
        i32.const 0
        i32.ge_s
        br_if 0 (;@2;)
        br 1 (;@1;)
      end
      block  ;; label = @2
        local.get 0
        i32.load offset=16
        local.tee 10
        local.get 0
        i32.load
        local.tee 9
        i32.const 1
        i32.sub
        local.tee 12
        local.get 1
        i64.const 7
        i64.shr_u
        local.get 1
        i64.xor
        i32.wrap_i64
        i32.and
        local.tee 7
        i32.const 2
        i32.shr_u
        i32.const 1073741820
        i32.and
        i32.add
        i32.load
        local.get 7
        i32.const 1
        i32.shl
        i32.shr_u
        i32.const 2
        i32.and
        if  ;; label = @3
          local.get 7
          local.set 4
          br 1 (;@2;)
        end
        local.get 7
        local.set 3
        local.get 9
        local.set 4
        block (result i32)  ;; label = @3
          block  ;; label = @4
            loop  ;; label = @5
              local.get 3
              i32.const 1
              i32.shl
              local.tee 5
              i32.const 30
              i32.and
              local.set 6
              local.get 10
              local.get 3
              i32.const 2
              i32.shr_u
              i32.const 1073741820
              i32.and
              i32.add
              i32.load
              local.tee 8
              local.get 5
              i32.shr_u
              local.tee 5
              i32.const 2
              i32.and
              br_if 1 (;@4;)
              local.get 5
              i32.const 1
              i32.and
              i32.eqz
              if  ;; label = @6
                local.get 0
                i32.load offset=20
                local.get 3
                i32.const 3
                i32.shl
                i32.add
                i64.load
                local.get 1
                i64.eq
                br_if 2 (;@4;)
              end
              local.get 3
              local.get 4
              local.get 8
              local.get 6
              i32.shr_u
              i32.const 1
              i32.and
              select
              local.set 4
              local.get 11
              i32.const 1
              i32.add
              local.tee 11
              local.get 3
              i32.add
              local.get 12
              i32.and
              local.tee 3
              local.get 7
              i32.ne
              br_if 0 (;@5;)
            end
            i32.const 1
            local.tee 6
            local.get 4
            local.get 9
            local.tee 5
            i32.eq
            br_if 1 (;@3;)
            drop
            br 2 (;@2;)
          end
          local.get 4
          local.set 5
          local.get 3
          local.set 7
          local.get 8
          local.get 6
          i32.shr_u
          i32.const 2
          i32.and
          i32.eqz
        end
        local.set 6
        local.get 7
        local.get 7
        local.get 5
        local.get 5
        local.get 9
        i32.eq
        select
        local.get 6
        select
        local.set 4
      end
      local.get 4
      i32.const 1
      i32.shl
      local.tee 3
      i32.const 30
      i32.and
      local.set 5
      local.get 10
      local.get 4
      i32.const 2
      i32.shr_u
      i32.const 1073741820
      i32.and
      i32.add
      local.tee 6
      i32.load
      local.tee 8
      local.get 3
      i32.shr_u
      local.tee 3
      i32.const 2
      i32.and
      if  ;; label = @2
        local.get 0
        i32.load offset=20
        local.get 4
        i32.const 3
        i32.shl
        i32.add
        local.get 1
        i64.store
        local.get 6
        local.get 8
        i32.const 3
        local.get 5
        i32.shl
        i32.const -1
        i32.xor
        i32.and
        i32.store
        local.get 0
        local.get 0
        i32.load offset=4
        i32.const 1
        i32.add
        i32.store offset=4
        local.get 0
        local.get 0
        i32.load offset=8
        i32.const 1
        i32.add
        i32.store offset=8
        local.get 2
        i32.const 1
        i32.store
        local.get 4
        return
      end
      local.get 3
      i32.const 1
      i32.and
      if  ;; label = @2
        local.get 0
        i32.load offset=20
        local.get 4
        i32.const 3
        i32.shl
        i32.add
        local.get 1
        i64.store
        local.get 6
        local.get 8
        i32.const 3
        local.get 5
        i32.shl
        i32.const -1
        i32.xor
        i32.and
        i32.store
        local.get 0
        local.get 0
        i32.load offset=4
        i32.const 1
        i32.add
        i32.store offset=4
        local.get 2
        i32.const 2
        i32.store
        local.get 4
        return
      end
      local.get 2
      i32.const 0
      i32.store
      local.get 4
      return
    end
    local.get 2
    i32.const -1
    i32.store
    local.get 0
    i32.load)
  (func $element_Compare (type 3) (param i32 i32) (result i32)
    (local i32 i32)
    local.get 0
    i32.load offset=8
    local.tee 2
    local.get 1
    i32.load offset=8
    local.tee 3
    i32.lt_u
    if  ;; label = @1
      i32.const 1
      return
    end
    local.get 2
    local.get 3
    i32.gt_u
    if  ;; label = @1
      i32.const -1
      return
    end
    i32.const 1
    i32.const -1
    local.get 0
    i64.load
    local.get 1
    i64.load
    i64.gt_u
    select)
  (func $kh_resize_oligonucleotide (type 3) (param i32 i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i64 i32 i32 i32 i32 i32 i32 i32 i32 i32 i64)
    block  ;; label = @1
      i32.const 4
      local.get 1
      i32.const 1
      i32.sub
      local.tee 1
      i32.const 1
      i32.shr_u
      local.get 1
      i32.or
      local.tee 1
      i32.const 2
      i32.shr_u
      local.get 1
      i32.or
      local.tee 1
      i32.const 4
      i32.shr_u
      local.get 1
      i32.or
      local.tee 1
      i32.const 8
      i32.shr_u
      local.get 1
      i32.or
      local.tee 1
      i32.const 16
      i32.shr_u
      local.get 1
      i32.or
      i32.const 1
      i32.add
      local.tee 1
      local.get 1
      i32.const 4
      i32.le_u
      select
      local.tee 2
      f64.convert_i32_u
      f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)
      f64.mul
      f64.const 0x1p-1 (;=0.5;)
      f64.add
      i32.trunc_sat_f64_u
      local.tee 16
      local.get 0
      i32.load offset=4
      i32.gt_u
      if  ;; label = @2
        i32.const 4
        local.get 2
        i32.const 2
        i32.shr_u
        i32.const 1073741820
        i32.and
        local.get 1
        i32.const 16
        i32.lt_u
        select
        local.tee 1
        call $malloc
        local.tee 7
        i32.eqz
        if  ;; label = @3
          i32.const -1
          return
        end
        local.get 1
        if  ;; label = @3
          local.get 7
          i32.const 170
          local.get 1
          memory.fill
        end
        block  ;; label = @3
          local.get 2
          local.get 0
          i32.load
          local.tee 1
          i32.gt_u
          if  ;; label = @4
            local.get 0
            i32.load offset=20
            local.get 2
            i32.const 3
            i32.shl
            call $realloc
            local.tee 1
            i32.eqz
            br_if 3 (;@1;)
            local.get 0
            local.get 1
            i32.store offset=20
            local.get 0
            i32.load offset=24
            local.get 2
            i32.const 2
            i32.shl
            call $realloc
            local.tee 1
            i32.eqz
            br_if 3 (;@1;)
            local.get 0
            local.get 1
            i32.store offset=24
            local.get 0
            i32.load
            local.tee 1
            i32.eqz
            br_if 1 (;@3;)
          end
          local.get 2
          i32.const 1
          i32.sub
          local.set 10
          local.get 0
          i32.load offset=16
          local.set 11
          loop  ;; label = @4
            block  ;; label = @5
              local.get 11
              local.get 6
              i32.const 2
              i32.shr_u
              i32.const 1073741820
              i32.and
              i32.add
              local.tee 3
              i32.load
              local.tee 5
              local.get 6
              i32.const 1
              i32.shl
              local.tee 4
              i32.shr_u
              i32.const 3
              i32.and
              br_if 0 (;@5;)
              local.get 0
              i32.load offset=24
              local.tee 12
              local.get 6
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.set 9
              local.get 0
              i32.load offset=20
              local.tee 13
              local.get 6
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.set 8
              local.get 3
              local.get 5
              i32.const 1
              local.get 4
              i32.const 30
              i32.and
              i32.shl
              i32.or
              i32.store
              loop  ;; label = @6
                block (result i32)  ;; label = @7
                  i32.const 2
                  local.get 10
                  local.get 8
                  i64.const 7
                  i64.shr_u
                  local.get 8
                  i64.xor
                  i32.wrap_i64
                  i32.and
                  local.tee 1
                  i32.const 1
                  i32.shl
                  local.tee 3
                  i32.shl
                  local.tee 5
                  local.get 7
                  local.get 1
                  i32.const 4
                  i32.shr_u
                  local.tee 14
                  i32.const 2
                  i32.shl
                  i32.add
                  local.tee 4
                  i32.load
                  local.tee 15
                  i32.and
                  if  ;; label = @8
                    local.get 3
                    i32.const 30
                    i32.and
                    br 1 (;@7;)
                  end
                  i32.const 0
                  local.set 3
                  loop  ;; label = @8
                    i32.const 2
                    local.get 3
                    i32.const 1
                    i32.add
                    local.tee 3
                    local.get 1
                    i32.add
                    local.get 10
                    i32.and
                    local.tee 1
                    i32.const 1
                    i32.shl
                    local.tee 17
                    i32.shl
                    local.tee 5
                    local.get 7
                    local.get 1
                    i32.const 4
                    i32.shr_u
                    local.tee 14
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee 4
                    i32.load
                    local.tee 15
                    i32.and
                    i32.eqz
                    br_if 0 (;@8;)
                  end
                  local.get 17
                  i32.const 30
                  i32.and
                end
                local.set 3
                local.get 4
                local.get 15
                local.get 5
                i32.const -1
                i32.xor
                i32.and
                i32.store
                block  ;; label = @7
                  local.get 0
                  i32.load
                  local.get 1
                  i32.gt_u
                  if  ;; label = @8
                    local.get 11
                    local.get 14
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee 5
                    i32.load
                    local.get 3
                    i32.shr_u
                    i32.const 3
                    i32.and
                    i32.eqz
                    br_if 1 (;@7;)
                  end
                  local.get 13
                  local.get 1
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get 8
                  i64.store
                  local.get 12
                  local.get 1
                  i32.const 2
                  i32.shl
                  i32.add
                  local.get 9
                  i32.store
                  local.get 0
                  i32.load
                  local.set 1
                  br 2 (;@5;)
                end
                local.get 13
                local.get 1
                i32.const 3
                i32.shl
                i32.add
                local.tee 4
                i64.load
                local.set 18
                local.get 4
                local.get 8
                i64.store
                local.get 12
                local.get 1
                i32.const 2
                i32.shl
                i32.add
                local.tee 1
                i32.load
                local.set 4
                local.get 1
                local.get 9
                i32.store
                local.get 5
                local.get 5
                i32.load
                i32.const 1
                local.get 3
                i32.shl
                i32.or
                i32.store
                local.get 18
                local.set 8
                local.get 4
                local.set 9
                br 0 (;@6;)
              end
              unreachable
            end
            local.get 6
            i32.const 1
            i32.add
            local.tee 6
            local.get 1
            i32.ne
            br_if 0 (;@4;)
          end
          local.get 1
          local.get 2
          i32.le_u
          br_if 0 (;@3;)
          local.get 0
          local.get 0
          i32.load offset=20
          local.get 2
          i32.const 3
          i32.shl
          call $realloc
          i32.store offset=20
          local.get 0
          local.get 0
          i32.load offset=24
          local.get 2
          i32.const 2
          i32.shl
          call $realloc
          i32.store offset=24
        end
        local.get 0
        i32.load offset=16
        call $free
        local.get 0
        local.get 2
        i32.store
        local.get 0
        local.get 7
        i32.store offset=16
        local.get 0
        local.get 16
        i32.store offset=12
        local.get 0
        local.get 0
        i32.load offset=4
        i32.store offset=8
      end
      i32.const 0
      return
    end
    local.get 7
    call $free
    i32.const -1)
  (func $main (type 3) (param i32 i32) (result i32)
    call $__original_main)
  (func $_start (type 4)
    call $__wasm_call_ctors
    call $__original_main
    call $exit
    unreachable)
  (func $dummy (type 4)
    nop)
  (func $libc_exit_fini (type 4)
    call $dummy)
  (func $exit (type 2) (param i32)
    call $dummy
    call $libc_exit_fini
    call $__stdio_exit
    local.get 0
    call $_Exit
    unreachable)
  (func $_Exit (type 2) (param i32)
    local.get 0
    call $__wasi_proc_exit
    unreachable)
  (func $__lockfile (type 1) (param i32) (result i32)
    i32.const 1)
  (func $__unlockfile (type 2) (param i32)
    nop)
  (func $__memcpy (type 0) (param i32 i32 i32) (result i32)
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
  (func $__stdio_exit (type 4)
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
    i32.const 1240
    i32.load
    call $close_file
    i32.const 1248
    i32.load
    call $close_file
    i32.const 1248
    i32.load
    call $close_file)
  (func $close_file (type 2) (param i32)
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
        call_indirect (type 0)
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
      call_indirect (type 7)
      drop
    end)
  (func $__toread (type 1) (param i32) (result i32)
    (local i32 i32)
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
    i32.load offset=20
    local.get 0
    i32.load offset=28
    i32.ne
    if  ;; label = @1
      local.get 0
      i32.const 0
      i32.const 0
      local.get 0
      i32.load offset=36
      call_indirect (type 0)
      drop
    end
    local.get 0
    i32.const 0
    i32.store offset=28
    local.get 0
    i64.const 0
    i64.store offset=16
    local.get 0
    i32.load
    local.tee 1
    i32.const 4
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
    local.get 0
    i32.load offset=44
    local.get 0
    i32.load offset=48
    i32.add
    local.tee 2
    i32.store offset=8
    local.get 0
    local.get 2
    i32.store offset=4
    local.get 1
    i32.const 27
    i32.shl
    i32.const 31
    i32.shr_s)
  (func $__uflow (type 1) (param i32) (result i32)
    (local i32 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 1
    global.set $__stack_pointer
    i32.const -1
    local.set 2
    block  ;; label = @1
      local.get 0
      call $__toread
      br_if 0 (;@1;)
      local.get 0
      local.get 1
      i32.const 15
      i32.add
      i32.const 1
      local.get 0
      i32.load offset=32
      call_indirect (type 0)
      i32.const 1
      i32.ne
      br_if 0 (;@1;)
      local.get 1
      i32.load8_u offset=15
      local.set 2
    end
    local.get 1
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get 2)
  (func $fgets (type 0) (param i32 i32 i32) (result i32)
    (local i32 i32 i32 i32 i32)
    block  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          block  ;; label = @4
            local.get 2
            i32.load offset=76
            i32.const 0
            i32.ge_s
            if  ;; label = @5
              local.get 2
              call $__lockfile
              local.set 3
              local.get 1
              i32.const 2
              i32.lt_s
              br_if 1 (;@4;)
              local.get 3
              i32.eqz
              local.set 6
              br 3 (;@2;)
            end
            i32.const 1
            local.set 6
            local.get 1
            i32.const 1
            i32.gt_s
            br_if 2 (;@2;)
            local.get 2
            local.get 2
            i32.load offset=72
            local.tee 3
            i32.const 1
            i32.sub
            local.get 3
            i32.or
            i32.store offset=72
            br 1 (;@3;)
          end
          local.get 2
          local.get 2
          i32.load offset=72
          local.tee 4
          i32.const 1
          i32.sub
          local.get 4
          i32.or
          i32.store offset=72
          local.get 3
          i32.eqz
          br_if 0 (;@3;)
          local.get 2
          call $__unlockfile
        end
        i32.const 0
        local.set 3
        local.get 1
        i32.const 1
        i32.ne
        br_if 1 (;@1;)
        local.get 0
        i32.const 0
        i32.store8
        local.get 0
        return
      end
      local.get 1
      i32.const 1
      i32.sub
      local.set 3
      local.get 0
      local.set 1
      block  ;; label = @2
        loop  ;; label = @3
          block  ;; label = @4
            block  ;; label = @5
              block  ;; label = @6
                local.get 2
                i32.load offset=4
                local.tee 4
                local.get 2
                i32.load offset=8
                local.tee 5
                i32.eq
                br_if 0 (;@6;)
                block (result i32)  ;; label = @7
                  local.get 4
                  i32.const 10
                  local.get 5
                  local.get 4
                  i32.sub
                  call $memchr
                  local.tee 7
                  if  ;; label = @8
                    local.get 7
                    local.get 2
                    i32.load offset=4
                    local.tee 5
                    i32.sub
                    i32.const 1
                    i32.add
                    br 1 (;@7;)
                  end
                  local.get 2
                  i32.load offset=8
                  local.get 2
                  i32.load offset=4
                  local.tee 5
                  i32.sub
                end
                local.set 4
                local.get 1
                local.get 5
                local.get 4
                local.get 3
                local.get 3
                local.get 4
                i32.gt_u
                select
                local.tee 4
                call $__memcpy
                drop
                local.get 2
                local.get 2
                i32.load offset=4
                local.get 4
                i32.add
                local.tee 5
                i32.store offset=4
                local.get 1
                local.get 4
                i32.add
                local.set 1
                local.get 7
                br_if 2 (;@4;)
                local.get 3
                local.get 4
                i32.sub
                local.tee 3
                i32.eqz
                br_if 2 (;@4;)
                local.get 5
                local.get 2
                i32.load offset=8
                i32.eq
                br_if 0 (;@6;)
                local.get 2
                local.get 5
                i32.const 1
                i32.add
                i32.store offset=4
                local.get 5
                i32.load8_u
                local.set 4
                br 1 (;@5;)
              end
              local.get 2
              call $__uflow
              local.tee 4
              i32.const 0
              i32.ge_s
              br_if 0 (;@5;)
              i32.const 0
              local.set 3
              local.get 0
              local.get 1
              i32.eq
              br_if 3 (;@2;)
              local.get 2
              i32.load8_u
              i32.const 16
              i32.and
              br_if 1 (;@4;)
              br 3 (;@2;)
            end
            local.get 1
            local.get 4
            i32.store8
            local.get 1
            i32.const 1
            i32.add
            local.set 1
            local.get 4
            i32.const 255
            i32.and
            i32.const 10
            i32.eq
            br_if 0 (;@4;)
            local.get 3
            i32.const 1
            i32.sub
            local.tee 3
            br_if 1 (;@3;)
          end
        end
        local.get 0
        i32.eqz
        if  ;; label = @3
          i32.const 0
          local.set 3
          br 1 (;@2;)
        end
        local.get 1
        i32.const 0
        i32.store8
        local.get 0
        local.set 3
      end
      local.get 6
      br_if 0 (;@1;)
      local.get 2
      call $__unlockfile
    end
    local.get 3)
  (func $memchr (type 0) (param i32 i32 i32) (result i32)
    (local i32 i32)
    local.get 2
    i32.const 0
    i32.ne
    local.set 3
    block  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          local.get 0
          i32.const 3
          i32.and
          i32.eqz
          br_if 0 (;@3;)
          local.get 2
          i32.eqz
          br_if 0 (;@3;)
          local.get 1
          i32.const 255
          i32.and
          local.set 4
          loop  ;; label = @4
            local.get 0
            i32.load8_u
            local.get 4
            i32.eq
            br_if 2 (;@2;)
            local.get 2
            i32.const 1
            i32.sub
            local.tee 2
            i32.const 0
            i32.ne
            local.set 3
            local.get 0
            i32.const 1
            i32.add
            local.tee 0
            i32.const 3
            i32.and
            i32.eqz
            br_if 1 (;@3;)
            local.get 2
            br_if 0 (;@4;)
          end
        end
        local.get 3
        i32.eqz
        br_if 1 (;@1;)
        block  ;; label = @3
          local.get 0
          i32.load8_u
          local.get 1
          i32.const 255
          i32.and
          i32.eq
          br_if 0 (;@3;)
          local.get 2
          i32.const 4
          i32.lt_u
          br_if 0 (;@3;)
          local.get 1
          i32.const 255
          i32.and
          i32.const 16843009
          i32.mul
          local.set 4
          loop  ;; label = @4
            i32.const 16843008
            local.get 0
            i32.load
            local.get 4
            i32.xor
            local.tee 3
            i32.sub
            local.get 3
            i32.or
            i32.const -2139062144
            i32.and
            i32.const -2139062144
            i32.ne
            br_if 2 (;@2;)
            local.get 0
            i32.const 4
            i32.add
            local.set 0
            local.get 2
            i32.const 4
            i32.sub
            local.tee 2
            i32.const 3
            i32.gt_u
            br_if 0 (;@4;)
          end
        end
        local.get 2
        i32.eqz
        br_if 1 (;@1;)
      end
      local.get 1
      i32.const 255
      i32.and
      local.set 3
      loop  ;; label = @2
        local.get 3
        local.get 0
        i32.load8_u
        i32.eq
        if  ;; label = @3
          local.get 0
          return
        end
        local.get 0
        i32.const 1
        i32.add
        local.set 0
        local.get 2
        i32.const 1
        i32.sub
        local.tee 2
        br_if 0 (;@2;)
      end
    end
    i32.const 0)
  (func $memcmp (type 0) (param i32 i32 i32) (result i32)
    (local i32 i32)
    block  ;; label = @1
      block  ;; label = @2
        local.get 2
        i32.const 4
        i32.ge_u
        if  ;; label = @3
          local.get 0
          local.get 1
          i32.or
          i32.const 3
          i32.and
          br_if 1 (;@2;)
          loop  ;; label = @4
            local.get 0
            i32.load
            local.get 1
            i32.load
            i32.ne
            br_if 2 (;@2;)
            local.get 1
            i32.const 4
            i32.add
            local.set 1
            local.get 0
            i32.const 4
            i32.add
            local.set 0
            local.get 2
            i32.const 4
            i32.sub
            local.tee 2
            i32.const 3
            i32.gt_u
            br_if 0 (;@4;)
          end
        end
        local.get 2
        i32.eqz
        br_if 1 (;@1;)
      end
      loop  ;; label = @2
        local.get 0
        i32.load8_u
        local.tee 3
        local.get 1
        i32.load8_u
        local.tee 4
        i32.eq
        if  ;; label = @3
          local.get 1
          i32.const 1
          i32.add
          local.set 1
          local.get 0
          i32.const 1
          i32.add
          local.set 0
          local.get 2
          i32.const 1
          i32.sub
          local.tee 2
          br_if 1 (;@2;)
          br 2 (;@1;)
        end
      end
      local.get 3
      local.get 4
      i32.sub
      return
    end
    i32.const 0)
  (func $__errno_location (type 5) (result i32)
    i32.const 1252)
  (func $__lock (type 2) (param i32)
    nop)
  (func $__ofl_lock (type 5) (result i32)
    i32.const 1256
    call $__lock
    i32.const 1260)
  (func $__qsort_r (type 12) (param i32 i32 i32 i32 i32)
    (local i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 208
    i32.sub
    local.tee 5
    global.set $__stack_pointer
    local.get 5
    i64.const 1
    i64.store offset=8
    block  ;; label = @1
      local.get 1
      local.get 2
      i32.mul
      local.tee 9
      i32.eqz
      br_if 0 (;@1;)
      local.get 5
      local.get 2
      i32.store offset=16
      local.get 5
      local.get 2
      i32.store offset=20
      i32.const 0
      local.get 2
      i32.sub
      local.set 8
      local.get 2
      local.tee 1
      local.set 7
      i32.const 2
      local.set 6
      loop  ;; label = @2
        local.get 5
        i32.const 16
        i32.add
        local.get 6
        i32.const 2
        i32.shl
        i32.add
        local.get 2
        local.get 7
        i32.add
        local.get 1
        local.tee 7
        i32.add
        local.tee 1
        i32.store
        local.get 6
        i32.const 1
        i32.add
        local.set 6
        local.get 1
        local.get 9
        i32.lt_u
        br_if 0 (;@2;)
      end
      block  ;; label = @2
        local.get 0
        local.get 0
        local.get 9
        i32.add
        local.get 8
        i32.add
        local.tee 7
        i32.ge_u
        if  ;; label = @3
          i32.const 1
          local.set 1
          br 1 (;@2;)
        end
        i32.const 1
        local.set 6
        i32.const 1
        local.set 1
        loop  ;; label = @3
          block (result i32)  ;; label = @4
            local.get 6
            i32.const 3
            i32.and
            i32.const 3
            i32.eq
            if  ;; label = @5
              local.get 0
              local.get 2
              local.get 3
              local.get 4
              local.get 1
              local.get 5
              i32.const 16
              i32.add
              call $sift
              local.get 5
              i32.const 8
              i32.add
              i32.const 2
              call $shr
              local.get 1
              i32.const 2
              i32.add
              br 1 (;@4;)
            end
            block  ;; label = @5
              local.get 5
              i32.const 16
              i32.add
              local.get 1
              i32.const 1
              i32.sub
              local.tee 6
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.get 7
              local.get 0
              i32.sub
              i32.ge_u
              if  ;; label = @6
                local.get 0
                local.get 2
                local.get 3
                local.get 4
                local.get 5
                i32.const 8
                i32.add
                local.get 1
                i32.const 0
                local.get 5
                i32.const 16
                i32.add
                call $trinkle
                br 1 (;@5;)
              end
              local.get 0
              local.get 2
              local.get 3
              local.get 4
              local.get 1
              local.get 5
              i32.const 16
              i32.add
              call $sift
            end
            local.get 1
            i32.const 1
            i32.eq
            if  ;; label = @5
              local.get 5
              i32.const 8
              i32.add
              i32.const 1
              call $shl
              i32.const 0
              br 1 (;@4;)
            end
            local.get 5
            i32.const 8
            i32.add
            local.get 6
            call $shl
            i32.const 1
          end
          local.set 1
          local.get 5
          local.get 5
          i32.load offset=8
          i32.const 1
          i32.or
          local.tee 6
          i32.store offset=8
          local.get 0
          local.get 2
          i32.add
          local.tee 0
          local.get 7
          i32.lt_u
          br_if 0 (;@3;)
        end
      end
      local.get 0
      local.get 2
      local.get 3
      local.get 4
      local.get 5
      i32.const 8
      i32.add
      local.get 1
      i32.const 0
      local.get 5
      i32.const 16
      i32.add
      call $trinkle
      block  ;; label = @2
        local.get 1
        i32.const 1
        i32.ne
        br_if 0 (;@2;)
        local.get 5
        i32.load offset=8
        i32.const 1
        i32.ne
        br_if 0 (;@2;)
        local.get 5
        i32.load offset=12
        i32.eqz
        br_if 1 (;@1;)
      end
      loop  ;; label = @2
        block (result i32)  ;; label = @3
          local.get 1
          i32.const 1
          i32.le_s
          if  ;; label = @4
            local.get 5
            i32.const 8
            i32.add
            local.get 5
            i32.const 8
            i32.add
            call $pntz
            local.tee 6
            call $shr
            local.get 1
            local.get 6
            i32.add
            br 1 (;@3;)
          end
          local.get 5
          i32.const 8
          i32.add
          i32.const 2
          call $shl
          local.get 5
          local.get 5
          i32.load offset=8
          i32.const 7
          i32.xor
          i32.store offset=8
          local.get 5
          i32.const 8
          i32.add
          i32.const 1
          call $shr
          local.get 0
          local.get 8
          i32.add
          local.tee 7
          local.get 5
          i32.const 16
          i32.add
          local.get 1
          i32.const 2
          i32.sub
          local.tee 6
          i32.const 2
          i32.shl
          i32.add
          i32.load
          i32.sub
          local.get 2
          local.get 3
          local.get 4
          local.get 5
          i32.const 8
          i32.add
          local.get 1
          i32.const 1
          i32.sub
          i32.const 1
          local.get 5
          i32.const 16
          i32.add
          call $trinkle
          local.get 5
          i32.const 8
          i32.add
          i32.const 1
          call $shl
          local.get 5
          local.get 5
          i32.load offset=8
          i32.const 1
          i32.or
          i32.store offset=8
          local.get 7
          local.get 2
          local.get 3
          local.get 4
          local.get 5
          i32.const 8
          i32.add
          local.get 6
          i32.const 1
          local.get 5
          i32.const 16
          i32.add
          call $trinkle
          local.get 6
        end
        local.set 1
        local.get 0
        local.get 8
        i32.add
        local.set 0
        local.get 1
        i32.const 1
        i32.ne
        br_if 0 (;@2;)
        local.get 5
        i32.load offset=8
        i32.const 1
        i32.ne
        br_if 0 (;@2;)
        local.get 5
        i32.load offset=12
        br_if 0 (;@2;)
      end
    end
    local.get 5
    i32.const 208
    i32.add
    global.set $__stack_pointer)
  (func $sift (type 13) (param i32 i32 i32 i32 i32 i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 240
    i32.sub
    local.tee 8
    global.set $__stack_pointer
    local.get 8
    local.get 0
    i32.store
    i32.const 1
    local.set 7
    block  ;; label = @1
      local.get 4
      i32.const 2
      i32.lt_s
      br_if 0 (;@1;)
      i32.const 0
      local.get 1
      i32.sub
      local.set 10
      local.get 0
      local.set 6
      loop  ;; label = @2
        local.get 0
        local.get 6
        local.get 10
        i32.add
        local.tee 6
        local.get 5
        local.get 4
        i32.const 2
        i32.sub
        local.tee 11
        i32.const 2
        i32.shl
        i32.add
        i32.load
        i32.sub
        local.tee 9
        local.get 3
        local.get 2
        call_indirect (type 0)
        i32.const 0
        i32.ge_s
        if  ;; label = @3
          local.get 0
          local.get 6
          local.get 3
          local.get 2
          call_indirect (type 0)
          i32.const 0
          i32.ge_s
          br_if 2 (;@1;)
        end
        local.get 8
        local.get 7
        i32.const 2
        i32.shl
        i32.add
        local.get 9
        local.get 6
        local.get 9
        local.get 6
        local.get 3
        local.get 2
        call_indirect (type 0)
        i32.const 0
        i32.ge_s
        local.tee 12
        select
        local.tee 6
        i32.store
        local.get 7
        i32.const 1
        i32.add
        local.set 7
        local.get 4
        i32.const 1
        i32.sub
        local.get 11
        local.get 12
        select
        local.tee 4
        i32.const 1
        i32.gt_s
        br_if 0 (;@2;)
      end
    end
    local.get 1
    local.get 8
    local.get 7
    call $cycle
    local.get 8
    i32.const 240
    i32.add
    global.set $__stack_pointer)
  (func $shr (type 8) (param i32 i32)
    (local i32 i32 i32)
    local.get 0
    i32.load offset=4
    local.set 2
    local.get 0
    block (result i32)  ;; label = @1
      local.get 1
      i32.const 31
      i32.le_u
      if  ;; label = @2
        local.get 0
        i32.load
        local.set 3
        local.get 2
        br 1 (;@1;)
      end
      local.get 1
      i32.const 32
      i32.sub
      local.set 1
      local.get 2
      local.set 3
      i32.const 0
    end
    local.tee 4
    local.get 1
    i32.shr_u
    i32.store offset=4
    local.get 0
    local.get 4
    i32.const 32
    local.get 1
    i32.sub
    i32.shl
    local.get 3
    local.get 1
    i32.shr_u
    i32.or
    i32.store)
  (func $trinkle (type 14) (param i32 i32 i32 i32 i32 i32 i32 i32)
    (local i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 240
    i32.sub
    local.tee 9
    global.set $__stack_pointer
    local.get 9
    local.get 4
    i32.load
    local.tee 8
    i32.store offset=232
    local.get 4
    i32.load offset=4
    local.set 4
    local.get 9
    local.get 0
    i32.store
    local.get 9
    local.get 4
    i32.store offset=236
    i32.const 0
    local.get 1
    i32.sub
    local.set 13
    local.get 6
    i32.eqz
    local.set 10
    block  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          block  ;; label = @4
            local.get 8
            i32.const 1
            i32.ne
            if  ;; label = @5
              local.get 0
              local.set 8
              i32.const 1
              local.set 6
              br 1 (;@4;)
            end
            local.get 0
            local.set 8
            i32.const 1
            local.set 6
            local.get 4
            br_if 0 (;@4;)
            br 1 (;@3;)
          end
          loop  ;; label = @4
            local.get 8
            local.get 7
            local.get 5
            i32.const 2
            i32.shl
            i32.add
            local.tee 11
            i32.load
            i32.sub
            local.tee 4
            local.get 0
            local.get 3
            local.get 2
            call_indirect (type 0)
            i32.const 0
            i32.le_s
            if  ;; label = @5
              br 2 (;@3;)
            end
            local.get 10
            i32.const -1
            i32.xor
            local.set 12
            i32.const 1
            local.set 10
            block  ;; label = @5
              local.get 12
              local.get 5
              i32.const 2
              i32.lt_s
              i32.or
              i32.const 1
              i32.and
              i32.eqz
              if  ;; label = @6
                local.get 11
                i32.const 8
                i32.sub
                i32.load
                local.set 12
                local.get 8
                local.get 13
                i32.add
                local.tee 11
                local.get 4
                local.get 3
                local.get 2
                call_indirect (type 0)
                i32.const 0
                i32.ge_s
                br_if 1 (;@5;)
                local.get 11
                local.get 12
                i32.sub
                local.get 4
                local.get 3
                local.get 2
                call_indirect (type 0)
                i32.const 0
                i32.ge_s
                br_if 1 (;@5;)
              end
              local.get 9
              local.get 6
              i32.const 2
              i32.shl
              i32.add
              local.get 4
              i32.store
              local.get 9
              i32.const 232
              i32.add
              local.get 9
              i32.const 232
              i32.add
              call $pntz
              local.tee 8
              call $shr
              local.get 6
              i32.const 1
              i32.add
              local.set 6
              local.get 5
              local.get 8
              i32.add
              local.set 5
              local.get 4
              local.set 8
              local.get 9
              i32.load offset=232
              i32.const 1
              i32.ne
              br_if 1 (;@4;)
              local.get 9
              i32.load offset=236
              br_if 1 (;@4;)
              br 3 (;@2;)
            end
          end
          local.get 8
          local.set 4
          br 1 (;@2;)
        end
        local.get 8
        local.set 4
        local.get 10
        i32.eqz
        br_if 1 (;@1;)
      end
      local.get 1
      local.get 9
      local.get 6
      call $cycle
      local.get 4
      local.get 1
      local.get 2
      local.get 3
      local.get 5
      local.get 7
      call $sift
    end
    local.get 9
    i32.const 240
    i32.add
    global.set $__stack_pointer)
  (func $shl (type 8) (param i32 i32)
    (local i32 i32)
    block (result i32)  ;; label = @1
      local.get 1
      i32.const 31
      i32.le_u
      if  ;; label = @2
        local.get 0
        i32.load
        local.set 2
        local.get 0
        i32.const 4
        i32.add
        br 1 (;@1;)
      end
      local.get 1
      i32.const 32
      i32.sub
      local.set 1
      local.get 0
    end
    local.tee 3
    i32.load
    local.set 3
    local.get 0
    local.get 2
    local.get 1
    i32.shl
    i32.store
    local.get 0
    local.get 3
    local.get 1
    i32.shl
    local.get 2
    i32.const 32
    local.get 1
    i32.sub
    i32.shr_u
    i32.or
    i32.store offset=4)
  (func $pntz (type 1) (param i32) (result i32)
    (local i32)
    local.get 0
    i32.load
    i32.const 1
    i32.sub
    call $__builtin_ctz
    local.tee 1
    i32.eqz
    if  ;; label = @1
      local.get 0
      i32.load offset=4
      call $__builtin_ctz
      local.tee 0
      i32.const 32
      i32.add
      i32.const 0
      local.get 0
      select
      local.set 1
    end
    local.get 1)
  (func $cycle (type 6) (param i32 i32 i32)
    (local i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 256
    i32.sub
    local.tee 5
    global.set $__stack_pointer
    block  ;; label = @1
      local.get 2
      i32.const 2
      i32.lt_s
      br_if 0 (;@1;)
      local.get 1
      local.get 2
      i32.const 2
      i32.shl
      i32.add
      local.tee 7
      local.get 5
      i32.store
      local.get 0
      i32.eqz
      br_if 0 (;@1;)
      loop  ;; label = @2
        local.get 7
        i32.load
        local.get 1
        i32.load
        i32.const 256
        local.get 0
        local.get 0
        i32.const 256
        i32.ge_u
        select
        local.tee 4
        call $__memcpy
        drop
        i32.const 0
        local.set 3
        loop  ;; label = @3
          local.get 1
          local.get 3
          i32.const 2
          i32.shl
          i32.add
          local.tee 6
          i32.load
          local.get 1
          local.get 3
          i32.const 1
          i32.add
          local.tee 3
          i32.const 2
          i32.shl
          i32.add
          i32.load
          local.get 4
          call $__memcpy
          drop
          local.get 6
          local.get 6
          i32.load
          local.get 4
          i32.add
          i32.store
          local.get 2
          local.get 3
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 0
        local.get 4
        i32.sub
        local.tee 0
        br_if 0 (;@2;)
      end
    end
    local.get 5
    i32.const 256
    i32.add
    global.set $__stack_pointer)
  (func $__builtin_ctz (type 1) (param i32) (result i32)
    local.get 0
    call $a_ctz_32)
  (func $a_ctz_32 (type 1) (param i32) (result i32)
    local.get 0
    i32.ctz
    i32.const 0
    local.get 0
    select)
  (func $qsort (type 15) (param i32 i32 i32 i32)
    local.get 0
    local.get 1
    local.get 2
    i32.const 3
    local.get 3
    call $__qsort_r)
  (func $wrapper_cmp (type 0) (param i32 i32 i32) (result i32)
    local.get 0
    local.get 1
    local.get 2
    call_indirect (type 3))
  (func $dummy_45 (type 1) (param i32) (result i32)
    local.get 0)
  (func $__stdio_close (type 1) (param i32) (result i32)
    local.get 0
    i32.load offset=60
    call $dummy_45
    call $__wasi_fd_close
    call $__wasi_syscall_ret)
  (func $__stdio_read (type 0) (param i32 i32 i32) (result i32)
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
  (func $__lseek (type 7) (param i32 i64 i32) (result i64)
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
  (func $__stdio_seek (type 7) (param i32 i64 i32) (result i64)
    local.get 0
    i32.load offset=60
    local.get 1
    local.get 2
    call $__lseek)
  (func $strlen (type 1) (param i32) (result i32)
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
  (func $__wasi_syscall_ret (type 1) (param i32) (result i32)
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
  (func $_emscripten_stack_restore (type 2) (param i32)
    local.get 0
    global.set $__stack_pointer)
  (func $emscripten_stack_get_current (type 5) (result i32)
    global.get $__stack_pointer)
  (table (;0;) 7 7 funcref)
  (memory (;0;) 258 258)
  (global $__stack_pointer (mut i32) (i32.const 67840))
  (export "memory" (memory 0))
  (export "main" (func $main))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func $_start))
  (export "_emscripten_stack_restore" (func $_emscripten_stack_restore))
  (export "emscripten_stack_get_current" (func $emscripten_stack_get_current))
  (elem (;0;) (i32.const 1) func $element_Compare $__wasm_call_ctors $wrapper_cmp $__stdio_close $__stdio_read $__stdio_seek)
  (data $.rodata (i32.const 1024) "GGTATT\00GGTATTTTAATT\00GGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA\00 \00 \01\03  \02\00H\04")
  (data $.data (i32.const 1096) "\09")
  (data $.data.1 (i32.const 1108) "\04")
  (data $.data.2 (i32.const 1128) "\05\00\00\00\00\00\00\00\06\00\00\00\f8\04\00\00\00\04")
  (data $.data.3 (i32.const 1172) "\ff\ff\ff\ff")
  (data $.data.4 (i32.const 1240) "H\04"))
