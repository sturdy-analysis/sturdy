(module $k-nucleotide.wasm
  (type (;0;) (func (param i32 i32 i32) (result i32)))
  (type (;1;) (func (param i32) (result i32)))
  (type (;2;) (func (param i32 i32) (result i32)))
  (type (;3;) (func (param i32)))
  (type (;4;) (func (param i32 i32 i32 i32)))
  (type (;5;) (func))
  (type (;6;) (func (result i32)))
  (type (;7;) (func (param i32 i32 i32)))
  (type (;8;) (func (param i32 i64 i32) (result i32)))
  (import "env" "fgets" (func $fgets (type 0)))
  (import "env" "memcmp" (func $memcmp (type 0)))
  (import "env" "malloc" (func $malloc (type 1)))
  (import "env" "realloc" (func $realloc (type 2)))
  (import "env" "free" (func $free (type 3)))
  (import "env" "strlen" (func $strlen (type 1)))
  (import "env" "calloc" (func $calloc (type 2)))
  (import "env" "qsort" (func $qsort (type 4)))
  (import "env" "memset" (func $memset (type 0)))
  (func $__wasm_call_ctors (type 5))
  (func $_start (type 6) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 4096
    i32.sub
    local.tee 0
    global.set $__stack_pointer
    block  ;; label = @1
      loop  ;; label = @2
        local.get 0
        i32.const 4096
        i32.const 0
        i32.load
        call $fgets
        i32.eqz
        br_if 1 (;@1;)
        i32.const 1067
        local.get 0
        i32.const 6
        call $memcmp
        br_if 0 (;@2;)
      end
    end
    i32.const 1048576
    call $malloc
    local.set 1
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        i32.const 4096
        i32.const 0
        i32.load
        call $fgets
        br_if 0 (;@2;)
        i32.const 0
        local.set 2
        br 1 (;@1;)
      end
      block  ;; label = @2
        local.get 0
        i32.load8_u
        local.tee 3
        i32.const 255
        i32.and
        i32.const 62
        i32.ne
        br_if 0 (;@2;)
        i32.const 0
        local.set 2
        br 1 (;@1;)
      end
      local.get 0
      i32.const 1
      i32.or
      local.set 4
      i32.const 1048576
      local.set 5
      i32.const 0
      local.set 2
      loop  ;; label = @2
        local.get 4
        local.set 6
        loop  ;; label = @3
          block  ;; label = @4
            block  ;; label = @5
              block  ;; label = @6
                local.get 3
                i32.const 255
                i32.and
                br_table 0 (;@6;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 1 (;@5;) 2 (;@4;) 1 (;@5;)
              end
              block  ;; label = @6
                local.get 5
                local.get 2
                i32.sub
                i32.const 4096
                i32.ge_u
                br_if 0 (;@6;)
                local.get 1
                local.get 5
                i32.const 1
                i32.shl
                local.tee 5
                call $realloc
                local.set 1
              end
              local.get 0
              i32.const 4096
              i32.const 0
              i32.load
              call $fgets
              i32.eqz
              br_if 4 (;@1;)
              local.get 0
              i32.load8_u
              local.tee 3
              i32.const 255
              i32.and
              i32.const 62
              i32.ne
              br_if 3 (;@2;)
              br 4 (;@1;)
            end
            local.get 1
            local.get 2
            i32.add
            local.get 3
            i32.const 7
            i32.and
            i32.const 1079
            i32.add
            i32.load8_u
            i32.store8
            local.get 2
            i32.const 1
            i32.add
            local.set 2
          end
          local.get 6
          i32.load8_u
          local.set 3
          local.get 6
          i32.const 1
          i32.add
          local.set 6
          br 0 (;@3;)
        end
      end
    end
    local.get 1
    local.get 2
    call $realloc
    local.tee 6
    local.get 2
    i32.const 1048
    call $generate_Count_For_Oligonucleotide
    local.get 6
    local.get 2
    i32.const 1031
    call $generate_Count_For_Oligonucleotide
    local.get 6
    local.get 2
    i32.const 1024
    call $generate_Count_For_Oligonucleotide
    local.get 6
    local.get 2
    i32.const 1074
    call $generate_Count_For_Oligonucleotide
    local.get 6
    local.get 2
    i32.const 1044
    call $generate_Count_For_Oligonucleotide
    local.get 6
    local.get 2
    i32.const 2
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get 6
    local.get 2
    i32.const 1
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get 6
    call $free
    local.get 0
    i32.const 4096
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (func $generate_Count_For_Oligonucleotide (type 7) (param i32 i32 i32)
    (local i32 i32 i32 i64 i64 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 3
    global.set $__stack_pointer
    local.get 2
    call $strlen
    local.tee 4
    i32.const -1
    i32.add
    local.set 5
    i64.const -1
    local.get 4
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const -1
    i64.xor
    local.set 6
    i64.const 0
    local.set 7
    i32.const 1
    i32.const 28
    call $calloc
    local.set 8
    block  ;; label = @1
      local.get 4
      i32.const 2
      i32.lt_s
      br_if 0 (;@1;)
      local.get 5
      i32.const 3
      i32.and
      local.set 9
      block  ;; label = @2
        block  ;; label = @3
          local.get 4
          i32.const -2
          i32.add
          i32.const 3
          i32.ge_u
          br_if 0 (;@3;)
          i32.const 0
          local.set 10
          i64.const 0
          local.set 7
          br 1 (;@2;)
        end
        local.get 5
        i32.const -4
        i32.and
        local.set 11
        i32.const 0
        local.set 10
        i64.const 0
        local.set 7
        loop  ;; label = @3
          local.get 7
          i64.const 2
          i64.shl
          local.get 6
          i64.and
          local.get 0
          local.get 10
          i32.add
          local.tee 2
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get 6
          i64.and
          local.get 2
          i32.const 1
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get 6
          i64.and
          local.get 2
          i32.const 2
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get 6
          i64.and
          local.get 2
          i32.const 3
          i32.add
          i64.load8_s
          i64.or
          local.set 7
          local.get 11
          local.get 10
          i32.const 4
          i32.add
          local.tee 10
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 9
      i32.eqz
      br_if 0 (;@1;)
      local.get 0
      local.get 10
      i32.add
      local.set 2
      loop  ;; label = @2
        local.get 7
        i64.const 2
        i64.shl
        local.get 6
        i64.and
        local.get 2
        i64.load8_s
        i64.or
        local.set 7
        local.get 2
        i32.const 1
        i32.add
        local.set 2
        local.get 9
        i32.const -1
        i32.add
        local.tee 9
        br_if 0 (;@2;)
      end
    end
    i32.const 0
    local.set 11
    block  ;; label = @1
      local.get 4
      local.get 1
      i32.gt_s
      br_if 0 (;@1;)
      local.get 0
      local.get 5
      i32.add
      local.set 2
      local.get 1
      local.get 4
      i32.sub
      i32.const 1
      i32.add
      local.set 0
      loop  ;; label = @2
        local.get 8
        local.get 7
        i64.const 2
        i64.shl
        local.get 6
        i64.and
        local.get 2
        i64.load8_s
        i64.or
        local.tee 7
        local.get 3
        i32.const 12
        i32.add
        call $kh_put_oligonucleotide
        local.set 10
        local.get 8
        i32.load offset=24
        local.tee 11
        local.get 10
        i32.const 2
        i32.shl
        i32.add
        local.set 10
        i32.const 1
        local.set 9
        block  ;; label = @3
          local.get 3
          i32.load offset=12
          br_if 0 (;@3;)
          local.get 10
          i32.load
          i32.const 1
          i32.add
          local.set 9
        end
        local.get 10
        local.get 9
        i32.store
        local.get 2
        i32.const 1
        i32.add
        local.set 2
        local.get 0
        i32.const -1
        i32.add
        local.tee 0
        br_if 0 (;@2;)
      end
    end
    local.get 8
    i32.load offset=20
    call $free
    local.get 8
    i32.load offset=16
    call $free
    local.get 11
    call $free
    local.get 8
    call $free
    local.get 3
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $generate_Frequencies_For_Desired_Length_Oligonucleotides (type 7) (param i32 i32 i32)
    (local i32 i64 i32 i32 i32 i32 i32 i32 i64 i32 i32 i32 i32 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee 3
    global.set $__stack_pointer
    i64.const 0
    local.set 4
    i32.const 1
    i32.const 28
    call $calloc
    local.set 5
    block  ;; label = @1
      local.get 2
      i32.const 2
      i32.lt_s
      br_if 0 (;@1;)
      block  ;; label = @2
        local.get 2
        i32.const 2
        i32.eq
        br_if 0 (;@2;)
        loop  ;; label = @3
          br 0 (;@3;)
        end
      end
      local.get 0
      i64.load8_s
      local.set 4
    end
    local.get 2
    i32.const -1
    i32.add
    local.set 6
    block  ;; label = @1
      block  ;; label = @2
        local.get 2
        local.get 1
        i32.le_s
        br_if 0 (;@2;)
        i32.const 0
        local.set 7
        i32.const 0
        call $malloc
        local.set 8
        i32.const 0
        local.set 9
        local.get 5
        i32.load offset=16
        local.set 10
        br 1 (;@1;)
      end
      i64.const -1
      local.get 2
      i32.const 1
      i32.shl
      i64.extend_i32_u
      i64.shl
      i64.const -1
      i64.xor
      local.set 11
      local.get 0
      local.get 6
      i32.add
      local.set 0
      local.get 1
      local.get 2
      i32.sub
      i32.const 1
      i32.add
      local.set 12
      loop  ;; label = @2
        local.get 5
        local.get 4
        i64.const 2
        i64.shl
        local.get 11
        i64.and
        local.get 0
        i64.load8_s
        i64.or
        local.tee 4
        local.get 3
        i32.const 12
        i32.add
        call $kh_put_oligonucleotide
        local.set 1
        local.get 5
        i32.load offset=24
        local.tee 9
        local.get 1
        i32.const 2
        i32.shl
        i32.add
        local.set 1
        i32.const 1
        local.set 13
        block  ;; label = @3
          local.get 3
          i32.load offset=12
          br_if 0 (;@3;)
          local.get 1
          i32.load
          i32.const 1
          i32.add
          local.set 13
        end
        local.get 1
        local.get 13
        i32.store
        local.get 0
        i32.const 1
        i32.add
        local.set 0
        local.get 12
        i32.const -1
        i32.add
        local.tee 12
        br_if 0 (;@2;)
      end
      local.get 5
      i32.load
      local.set 14
      local.get 5
      i32.load offset=4
      local.tee 7
      i32.const 4
      i32.shl
      call $malloc
      local.set 8
      block  ;; label = @2
        local.get 14
        br_if 0 (;@2;)
        local.get 5
        i32.load offset=16
        local.set 10
        br 1 (;@1;)
      end
      local.get 5
      i32.load offset=16
      local.set 10
      i32.const 0
      local.set 0
      i32.const 0
      local.set 13
      local.get 9
      local.set 12
      i32.const 0
      local.set 15
      i32.const 0
      local.set 1
      loop  ;; label = @2
        block  ;; label = @3
          local.get 10
          local.get 1
          i32.const 2
          i32.shr_u
          i32.const 1073741820
          i32.and
          i32.add
          i32.load
          local.get 0
          i32.const 30
          i32.and
          i32.shr_u
          i32.const 3
          i32.and
          br_if 0 (;@3;)
          local.get 8
          local.get 15
          i32.const 4
          i32.shl
          i32.add
          local.tee 16
          local.get 12
          i32.load
          i32.store offset=8
          local.get 16
          local.get 5
          i32.load offset=20
          local.get 13
          i32.add
          i64.load
          i64.store
          local.get 15
          i32.const 1
          i32.add
          local.set 15
        end
        local.get 0
        i32.const 2
        i32.add
        local.set 0
        local.get 13
        i32.const 8
        i32.add
        local.set 13
        local.get 12
        i32.const 4
        i32.add
        local.set 12
        local.get 14
        local.get 1
        i32.const 1
        i32.add
        local.tee 1
        i32.ne
        br_if 0 (;@2;)
      end
    end
    local.get 5
    i32.load offset=20
    call $free
    local.get 10
    call $free
    local.get 9
    call $free
    local.get 5
    call $free
    local.get 8
    local.get 7
    i32.const 16
    i32.const 1
    call $qsort
    block  ;; label = @1
      local.get 7
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      local.get 2
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      local.get 2
      i32.const 7
      i32.and
      local.set 12
      i32.const 0
      local.set 1
      local.get 2
      i32.const 8
      i32.lt_u
      local.set 9
      loop  ;; label = @2
        local.get 8
        local.get 1
        i32.const 4
        i32.shl
        i32.add
        local.tee 13
        i64.load
        local.set 4
        block  ;; label = @3
          block  ;; label = @4
            local.get 12
            br_if 0 (;@4;)
            local.get 6
            local.set 0
            br 1 (;@3;)
          end
          local.get 12
          local.set 5
          local.get 6
          local.set 0
          loop  ;; label = @4
            local.get 0
            i32.const -1
            i32.add
            local.set 0
            local.get 4
            i64.const 2
            i64.shr_u
            local.set 4
            local.get 5
            i32.const -1
            i32.add
            local.tee 5
            br_if 0 (;@4;)
          end
        end
        block  ;; label = @3
          local.get 9
          br_if 0 (;@3;)
          local.get 0
          i32.const 8
          i32.add
          local.set 0
          loop  ;; label = @4
            local.get 4
            i64.const 16
            i64.shr_u
            local.set 4
            local.get 0
            i32.const -8
            i32.add
            local.tee 0
            i32.const 7
            i32.gt_s
            br_if 0 (;@4;)
          end
        end
        local.get 13
        local.get 4
        i64.store
        local.get 1
        i32.const 1
        i32.add
        local.tee 1
        local.get 7
        i32.ne
        br_if 0 (;@2;)
      end
    end
    local.get 8
    call $free
    local.get 3
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $kh_put_oligonucleotide (type 8) (param i32 i64 i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    block  ;; label = @1
      local.get 0
      i32.load offset=8
      local.get 0
      i32.load offset=12
      i32.lt_u
      br_if 0 (;@1;)
      block  ;; label = @2
        local.get 0
        i32.load
        local.tee 3
        local.get 0
        i32.load offset=4
        i32.const 1
        i32.shl
        i32.le_u
        br_if 0 (;@2;)
        local.get 0
        local.get 3
        i32.const -1
        i32.add
        call $kh_resize_oligonucleotide
        i32.const -1
        i32.gt_s
        br_if 1 (;@1;)
        local.get 2
        i32.const -1
        i32.store
        local.get 0
        i32.load
        return
      end
      local.get 0
      local.get 3
      i32.const 1
      i32.add
      call $kh_resize_oligonucleotide
      i32.const -1
      i32.gt_s
      br_if 0 (;@1;)
      local.get 2
      i32.const -1
      i32.store
      local.get 0
      i32.load
      return
    end
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        i32.load offset=16
        local.tee 4
        local.get 0
        i32.load
        local.tee 5
        i32.const -1
        i32.add
        local.tee 6
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
        i32.eqz
        br_if 0 (;@2;)
        local.get 7
        local.set 8
        br 1 (;@1;)
      end
      i32.const 1
      local.set 9
      local.get 7
      local.set 3
      local.get 5
      local.set 8
      block  ;; label = @2
        block  ;; label = @3
          loop  ;; label = @4
            local.get 3
            i32.const 1
            i32.shl
            local.tee 10
            i32.const 30
            i32.and
            local.set 11
            local.get 4
            local.get 3
            i32.const 2
            i32.shr_u
            i32.const 1073741820
            i32.and
            i32.add
            i32.load
            local.tee 12
            local.get 10
            i32.shr_u
            local.tee 10
            i32.const 2
            i32.and
            br_if 1 (;@3;)
            block  ;; label = @5
              local.get 10
              i32.const 1
              i32.and
              br_if 0 (;@5;)
              local.get 0
              i32.load offset=20
              local.get 3
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.get 1
              i64.eq
              br_if 2 (;@3;)
            end
            local.get 3
            local.get 8
            local.get 12
            local.get 11
            i32.shr_u
            i32.const 1
            i32.and
            select
            local.set 8
            local.get 3
            local.get 9
            i32.add
            local.set 3
            local.get 9
            i32.const 1
            i32.add
            local.set 9
            local.get 3
            local.get 6
            i32.and
            local.tee 3
            local.get 7
            i32.ne
            br_if 0 (;@4;)
          end
          i32.const 1
          local.set 10
          local.get 5
          local.set 9
          local.get 8
          local.get 5
          i32.eq
          br_if 1 (;@2;)
          br 2 (;@1;)
        end
        local.get 12
        local.get 11
        i32.shr_u
        i32.const 2
        i32.and
        i32.eqz
        local.set 10
        local.get 8
        local.set 9
        local.get 3
        local.set 7
      end
      local.get 7
      local.get 7
      local.get 9
      local.get 9
      local.get 5
      i32.eq
      select
      local.get 10
      select
      local.set 8
    end
    local.get 8
    i32.const 1
    i32.shl
    local.tee 3
    i32.const 30
    i32.and
    local.set 9
    block  ;; label = @1
      local.get 4
      local.get 8
      i32.const 2
      i32.shr_u
      i32.const 1073741820
      i32.and
      i32.add
      local.tee 10
      i32.load
      local.tee 11
      local.get 3
      i32.shr_u
      local.tee 3
      i32.const 2
      i32.and
      i32.eqz
      br_if 0 (;@1;)
      local.get 10
      local.get 11
      i32.const 3
      local.get 9
      i32.shl
      i32.const -1
      i32.xor
      i32.and
      i32.store
      local.get 0
      local.get 0
      i32.load offset=8
      i32.const 1
      i32.add
      i32.store offset=8
      local.get 0
      local.get 0
      i32.load offset=4
      i32.const 1
      i32.add
      i32.store offset=4
      local.get 0
      i32.load offset=20
      local.get 8
      i32.const 3
      i32.shl
      i32.add
      local.get 1
      i64.store
      local.get 2
      i32.const 1
      i32.store
      local.get 8
      return
    end
    block  ;; label = @1
      local.get 3
      i32.const 1
      i32.and
      i32.eqz
      br_if 0 (;@1;)
      local.get 10
      local.get 11
      i32.const 3
      local.get 9
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
      i32.load offset=20
      local.get 8
      i32.const 3
      i32.shl
      i32.add
      local.get 1
      i64.store
      local.get 2
      i32.const 2
      i32.store
      local.get 8
      return
    end
    local.get 2
    i32.const 0
    i32.store
    local.get 8)
  (func $element_Compare (type 2) (param i32 i32) (result i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      i32.load offset=8
      local.tee 2
      local.get 1
      i32.load offset=8
      local.tee 3
      i32.ge_u
      br_if 0 (;@1;)
      i32.const 1
      return
    end
    block  ;; label = @1
      local.get 2
      local.get 3
      i32.le_u
      br_if 0 (;@1;)
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
  (func $kh_resize_oligonucleotide (type 2) (param i32 i32) (result i32)
    (local i32 f64 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i64 i32 i32 i32 i64)
    block  ;; label = @1
      block  ;; label = @2
        local.get 1
        i32.const -1
        i32.add
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
        i32.const 4
        local.get 1
        i32.const 4
        i32.gt_u
        select
        local.tee 2
        f64.convert_i32_u
        f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)
        f64.mul
        f64.const 0x1p-1 (;=0.5;)
        f64.add
        local.tee 3
        f64.const 0x1p+32 (;=4.29497e+09;)
        f64.lt
        local.get 3
        f64.const 0x0p+0 (;=0;)
        f64.ge
        i32.and
        i32.eqz
        br_if 0 (;@2;)
        local.get 3
        i32.trunc_f64_u
        local.set 4
        br 1 (;@1;)
      end
      i32.const 0
      local.set 4
    end
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        i32.load offset=4
        local.get 4
        i32.ge_u
        br_if 0 (;@2;)
        block  ;; label = @3
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
          local.tee 5
          br_if 0 (;@3;)
          i32.const -1
          return
        end
        local.get 5
        i32.const 170
        local.get 1
        call $memset
        local.set 6
        block  ;; label = @3
          block  ;; label = @4
            local.get 0
            i32.load
            local.tee 1
            local.get 2
            i32.ge_u
            br_if 0 (;@4;)
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
          i32.const -1
          i32.add
          local.set 7
          local.get 0
          i32.load offset=16
          local.set 8
          i32.const 0
          local.set 9
          loop  ;; label = @4
            block  ;; label = @5
              local.get 8
              local.get 9
              i32.const 2
              i32.shr_u
              i32.const 1073741820
              i32.and
              i32.add
              local.tee 5
              i32.load
              local.tee 10
              local.get 9
              i32.const 1
              i32.shl
              local.tee 11
              i32.shr_u
              i32.const 3
              i32.and
              br_if 0 (;@5;)
              local.get 0
              i32.load offset=24
              local.tee 12
              local.get 9
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.set 13
              local.get 5
              local.get 10
              i32.const 1
              local.get 11
              i32.const 30
              i32.and
              i32.shl
              i32.or
              i32.store
              local.get 0
              i32.load offset=20
              local.tee 14
              local.get 9
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.set 15
              loop  ;; label = @6
                block  ;; label = @7
                  block  ;; label = @8
                    i32.const 2
                    local.get 7
                    local.get 15
                    i64.const 7
                    i64.shr_u
                    local.get 15
                    i64.xor
                    i32.wrap_i64
                    i32.and
                    local.tee 1
                    i32.const 1
                    i32.shl
                    local.tee 5
                    i32.shl
                    local.tee 10
                    local.get 6
                    local.get 1
                    i32.const 4
                    i32.shr_u
                    local.tee 16
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee 11
                    i32.load
                    local.tee 17
                    i32.and
                    i32.eqz
                    br_if 0 (;@8;)
                    local.get 5
                    i32.const 30
                    i32.and
                    local.set 5
                    br 1 (;@7;)
                  end
                  i32.const 1
                  local.set 5
                  loop  ;; label = @8
                    local.get 1
                    local.get 5
                    i32.add
                    local.set 1
                    local.get 5
                    i32.const 1
                    i32.add
                    local.set 5
                    i32.const 2
                    local.get 1
                    local.get 7
                    i32.and
                    local.tee 1
                    i32.const 1
                    i32.shl
                    local.tee 18
                    i32.shl
                    local.tee 10
                    local.get 6
                    local.get 1
                    i32.const 4
                    i32.shr_u
                    local.tee 16
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee 11
                    i32.load
                    local.tee 17
                    i32.and
                    i32.eqz
                    br_if 0 (;@8;)
                  end
                  local.get 18
                  i32.const 30
                  i32.and
                  local.set 5
                end
                local.get 11
                local.get 17
                local.get 10
                i32.const -1
                i32.xor
                i32.and
                i32.store
                block  ;; label = @7
                  block  ;; label = @8
                    local.get 1
                    local.get 0
                    i32.load
                    i32.ge_u
                    br_if 0 (;@8;)
                    local.get 8
                    local.get 16
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee 10
                    i32.load
                    local.get 5
                    i32.shr_u
                    i32.const 3
                    i32.and
                    i32.eqz
                    br_if 1 (;@7;)
                  end
                  local.get 12
                  local.get 1
                  i32.const 2
                  i32.shl
                  i32.add
                  local.get 13
                  i32.store
                  local.get 14
                  local.get 1
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get 15
                  i64.store
                  local.get 0
                  i32.load
                  local.set 1
                  br 2 (;@5;)
                end
                local.get 12
                local.get 1
                i32.const 2
                i32.shl
                i32.add
                local.tee 11
                i32.load
                local.set 17
                local.get 11
                local.get 13
                i32.store
                local.get 14
                local.get 1
                i32.const 3
                i32.shl
                i32.add
                local.tee 1
                i64.load
                local.set 19
                local.get 1
                local.get 15
                i64.store
                local.get 10
                local.get 10
                i32.load
                i32.const 1
                local.get 5
                i32.shl
                i32.or
                i32.store
                local.get 19
                local.set 15
                local.get 17
                local.set 13
                br 0 (;@6;)
              end
            end
            local.get 9
            i32.const 1
            i32.add
            local.tee 9
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
        local.get 6
        i32.store offset=16
        local.get 0
        local.get 4
        i32.store offset=12
        local.get 0
        local.get 0
        i32.load offset=4
        i32.store offset=8
      end
      i32.const 0
      return
    end
    local.get 6
    call $free
    i32.const -1)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 66624))
  (global (;1;) i32 (i32.const 1024))
  (global (;2;) i32 (i32.const 1088))
  (global (;3;) i32 (i32.const 1088))
  (global (;4;) i32 (i32.const 66624))
  (global (;5;) i32 (i32.const 1024))
  (global (;6;) i32 (i32.const 66624))
  (global (;7;) i32 (i32.const 131072))
  (global (;8;) i32 (i32.const 0))
  (global (;9;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "_start" (func $_start))
  (export "__indirect_function_table" (table 0))
  (export "__dso_handle" (global 1))
  (export "__data_end" (global 2))
  (export "__stack_low" (global 3))
  (export "__stack_high" (global 4))
  (export "__global_base" (global 5))
  (export "__heap_base" (global 6))
  (export "__heap_end" (global 7))
  (export "__memory_base" (global 8))
  (export "__table_base" (global 9))
  (elem (;0;) (i32.const 1) func $element_Compare)
  (data $.rodata (i32.const 1024) "GGTATT\00GGTATTTTAATT\00GGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA\00 \00 \01\03  \02\00"))
