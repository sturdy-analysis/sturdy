(module $k-nucleotide.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32 i32 i32)))
  (type $t3 (func (param i32) (result i32)))
  (type $t4 (func (param i32)))
  (type $t5 (func (result i32)))
  (type $t6 (func))
  (type $t7 (func (param i32 i64 i32) (result i32)))
  (type $t8 (func (param i32 i64) (result i32)))
  (import "env" "fgets" (func $fgets (type $t1)))
  (import "env" "memcmp" (func $memcmp (type $t1)))
  (import "env" "malloc" (func $malloc (type $t3)))
  (import "env" "realloc" (func $realloc (type $t0)))
  (import "env" "free" (func $free (type $t4)))
  (import "env" "strlen" (func $strlen (type $t3)))
  (import "env" "qsort" (func $qsort (type $t2)))
  (import "env" "calloc" (func $calloc (type $t0)))
  (import "env" "memset" (func $memset (type $t1)))
  (func $__wasm_call_ctors (type $t6)
    nop)
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32)
    global.get $__stack_pointer
    i32.const 32784
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    loop $L0
      i32.const 0
      local.set $l1
      local.get $l0
      i32.const 28688
      i32.add
      i32.const 4096
      i32.const 0
      i32.load
      call $fgets
      if $I1
        i32.const 1072
        local.get $l0
        i32.const 28688
        i32.add
        i32.const 6
        call $memcmp
        i32.const 0
        i32.ne
        local.set $l1
      end
      local.get $l1
      br_if $L0
    end
    local.get $l0
    i32.const 1048576
    i32.store offset=28684
    local.get $l0
    i32.const 0
    i32.store offset=28680
    local.get $l0
    local.get $l0
    i32.load offset=28684
    call $malloc
    i32.store offset=28676
    loop $L2
      i32.const 0
      local.set $l2
      local.get $l0
      i32.const 28688
      i32.add
      i32.const 4096
      i32.const 0
      i32.load
      call $fgets
      if $I3
        local.get $l0
        i32.load8_u offset=28688
        i32.const 24
        local.tee $l3
        i32.shl
        local.get $l3
        i32.shr_s
        i32.const 62
        i32.ne
        local.set $l2
      end
      local.get $l2
      if $I4
        local.get $l0
        i32.const 0
        i32.store offset=28672
        loop $L5
          local.get $l0
          i32.load offset=28672
          local.get $l0
          i32.const 28688
          i32.add
          i32.add
          i32.load8_u
          i32.const 24
          local.tee $l4
          i32.shl
          local.get $l4
          i32.shr_s
          if $I6
            local.get $l0
            i32.load offset=28672
            local.get $l0
            i32.const 28688
            i32.add
            i32.add
            i32.load8_u
            i32.const 24
            local.tee $l5
            i32.shl
            local.get $l5
            i32.shr_s
            i32.const 10
            i32.ne
            if $I7
              local.get $l0
              i32.load offset=28672
              local.get $l0
              i32.const 28688
              i32.add
              i32.add
              i32.load8_u
              i32.const 24
              local.tee $l6
              i32.shl
              local.get $l6
              i32.shr_s
              i32.const 7
              i32.and
              i32.load8_u offset=1084
              local.set $l7
              local.get $l0
              i32.load offset=28676
              local.set $l8
              local.get $l0
              local.get $l0
              i32.load offset=28680
              local.tee $l9
              i32.const 1
              i32.add
              i32.store offset=28680
              local.get $l8
              local.get $l9
              i32.add
              local.get $l7
              i32.store8
            end
            local.get $l0
            local.get $l0
            i32.load offset=28672
            i32.const 1
            i32.add
            i32.store offset=28672
            br $L5
          end
        end
        local.get $l0
        i32.load offset=28684
        local.get $l0
        i32.load offset=28680
        i32.sub
        i32.const 4096
        i32.lt_u
        if $I8
          local.get $l0
          i32.load offset=28676
          local.set $l10
          local.get $l0
          local.get $l0
          i32.load offset=28684
          i32.const 1
          i32.shl
          local.tee $l11
          i32.store offset=28684
          local.get $l0
          local.get $l10
          local.get $l11
          call $realloc
          i32.store offset=28676
        end
        br $L2
      end
    end
    local.get $l0
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    call $realloc
    i32.store offset=28676
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    i32.const 1053
    local.get $l0
    i32.const 24576
    i32.add
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    i32.const 1031
    local.get $l0
    i32.const 20480
    i32.add
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    i32.const 1024
    local.get $l0
    i32.const 16384
    i32.add
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    i32.const 1079
    local.get $l0
    i32.const 12288
    i32.add
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    i32.const 1044
    local.get $l0
    i32.const -8192
    i32.sub
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    i32.const 2
    local.get $l0
    i32.const 4096
    i32.add
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get $l0
    i32.load offset=28676
    local.get $l0
    i32.load offset=28680
    i32.const 1
    local.get $l0
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get $l0
    i32.load offset=28676
    call $free
    local.get $l0
    i32.const 32784
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (func $generate_Count_For_Oligonucleotide (type $t2) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    global.get $__stack_pointer
    i32.const 80
    i32.sub
    local.tee $l4
    global.set $__stack_pointer
    local.get $l4
    local.get $p0
    i32.store offset=76
    local.get $l4
    local.get $p1
    i32.store offset=72
    local.get $l4
    local.get $p2
    i32.store offset=68
    local.get $l4
    local.get $p3
    i32.store offset=64
    local.get $l4
    local.get $l4
    i32.load offset=68
    call $strlen
    i32.store offset=60
    local.get $l4
    call $kh_init_oligonucleotide
    i32.store offset=56
    local.get $l4
    i64.const 0
    i64.store offset=48
    local.get $l4
    i64.const 1
    local.get $l4
    i32.load offset=60
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const 1
    i64.sub
    i64.store offset=40
    local.get $l4
    i32.const 0
    i32.store offset=36
    loop $L0
      local.get $l4
      i32.load offset=36
      local.get $l4
      i32.load offset=60
      i32.const 1
      i32.sub
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l4
        local.get $l4
        i32.load offset=76
        local.get $l4
        i32.load offset=36
        i32.add
        i32.load8_u
        i32.const 24
        local.tee $l5
        i32.shl
        local.get $l5
        i32.shr_s
        i64.extend_i32_s
        local.get $l4
        i64.load offset=40
        local.get $l4
        i64.load offset=48
        i64.const 2
        i64.shl
        i64.and
        i64.or
        i64.store offset=48
        local.get $l4
        local.get $l4
        i32.load offset=36
        i32.const 1
        i32.add
        i32.store offset=36
        br $L0
      end
    end
    local.get $l4
    local.get $l4
    i32.load offset=60
    i32.const 1
    i32.sub
    i32.store offset=32
    loop $L2
      local.get $l4
      i32.load offset=32
      local.get $l4
      i32.load offset=72
      i32.ge_s
      i32.eqz
      if $I3
        local.get $l4
        local.get $l4
        i32.load offset=76
        local.get $l4
        i32.load offset=32
        i32.add
        i32.load8_u
        i32.const 24
        local.tee $l6
        i32.shl
        local.get $l6
        i32.shr_s
        i64.extend_i32_s
        local.get $l4
        i64.load offset=40
        local.get $l4
        i64.load offset=48
        i64.const 2
        i64.shl
        i64.and
        i64.or
        i64.store offset=48
        local.get $l4
        local.get $l4
        i32.load offset=56
        local.get $l4
        i64.load offset=48
        local.get $l4
        i32.const 28
        i32.add
        call $kh_put_oligonucleotide
        i32.store offset=24
        block $B4
          local.get $l4
          i32.load offset=28
          if $I5
            local.get $l4
            i32.load offset=56
            i32.load offset=24
            local.get $l4
            i32.load offset=24
            i32.const 2
            i32.shl
            i32.add
            i32.const 1
            i32.store
            br $B4
          end
          local.get $l4
          i32.load offset=56
          i32.load offset=24
          local.get $l4
          i32.load offset=24
          i32.const 2
          i32.shl
          i32.add
          local.tee $l7
          local.get $l7
          i32.load
          i32.const 1
          i32.add
          i32.store
        end
        local.get $l4
        local.get $l4
        i32.load offset=32
        i32.const 1
        i32.add
        i32.store offset=32
        br $L2
      end
    end
    local.get $l4
    i64.const 0
    i64.store offset=48
    local.get $l4
    i32.const 0
    i32.store offset=20
    loop $L6
      local.get $l4
      i32.load offset=20
      local.get $l4
      i32.load offset=60
      i32.ge_s
      i32.eqz
      if $I7
        local.get $l4
        local.get $l4
        i32.load offset=68
        local.get $l4
        i32.load offset=20
        i32.add
        i32.load8_u
        i32.const 24
        local.tee $l8
        i32.shl
        local.get $l8
        i32.shr_s
        i32.const 7
        i32.and
        i32.load8_u offset=1084
        i32.const 24
        local.tee $l9
        i32.shl
        local.get $l9
        i32.shr_s
        i64.extend_i32_s
        local.get $l4
        i64.load offset=48
        i64.const 2
        i64.shl
        i64.or
        i64.store offset=48
        local.get $l4
        local.get $l4
        i32.load offset=20
        i32.const 1
        i32.add
        i32.store offset=20
        br $L6
      end
    end
    local.get $l4
    local.get $l4
    i32.load offset=56
    local.get $l4
    i64.load offset=48
    call $kh_get_oligonucleotide
    i32.store offset=16
    local.get $l4
    local.get $l4
    i32.load offset=16
    local.get $l4
    i32.load offset=56
    i32.load
    i32.eq
    if $I8 (result i32)
      i32.const 0
    else
      local.get $l4
      i32.load offset=56
      i32.load offset=24
      local.get $l4
      i32.load offset=16
      i32.const 2
      i32.shl
      i32.add
      i32.load
    end
    i64.extend_i32_u
    i64.store offset=8
    local.get $l4
    i32.load offset=56
    call $kh_destroy_oligonucleotide
    local.get $l4
    i32.const 80
    i32.add
    global.set $__stack_pointer)
  (func $generate_Frequencies_For_Desired_Length_Oligonucleotides (type $t2) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32)
    global.get $__stack_pointer
    i32.const 128
    i32.sub
    local.tee $l5
    local.set $l4
    local.get $l5
    global.set $__stack_pointer
    local.get $l4
    local.get $p0
    i32.store offset=124
    local.get $l4
    local.get $p1
    i32.store offset=120
    local.get $l4
    local.get $p2
    i32.store offset=116
    local.get $l4
    local.get $p3
    i32.store offset=112
    local.get $l4
    call $kh_init_oligonucleotide
    i32.store offset=108
    local.get $l4
    i64.const 0
    i64.store offset=96
    local.get $l4
    i64.const 1
    local.get $l4
    i32.load offset=116
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const 1
    i64.sub
    i64.store offset=88
    local.get $l4
    i32.const 0
    i32.store offset=84
    loop $L0
      local.get $l4
      i32.load offset=84
      local.get $l4
      i32.load offset=116
      i32.const 1
      i32.sub
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l4
        local.get $l4
        i32.load offset=124
        local.get $l4
        i32.load offset=84
        i32.add
        i32.load8_u
        i32.const 24
        local.tee $l8
        i32.shl
        local.get $l8
        i32.shr_s
        i64.extend_i32_s
        local.get $l4
        i64.load offset=88
        local.get $l4
        i64.load offset=96
        i64.const 2
        i64.shl
        i64.and
        i64.or
        i64.store offset=96
        local.get $l4
        local.get $l4
        i32.load offset=84
        i32.const 1
        i32.add
        i32.store offset=84
        br $L0
      end
    end
    local.get $l4
    local.get $l4
    i32.load offset=116
    i32.const 1
    i32.sub
    i32.store offset=80
    loop $L2
      local.get $l4
      i32.load offset=80
      local.get $l4
      i32.load offset=120
      i32.ge_s
      i32.eqz
      if $I3
        local.get $l4
        local.get $l4
        i32.load offset=124
        local.get $l4
        i32.load offset=80
        i32.add
        i32.load8_u
        i32.const 24
        local.tee $l9
        i32.shl
        local.get $l9
        i32.shr_s
        i64.extend_i32_s
        local.get $l4
        i64.load offset=88
        local.get $l4
        i64.load offset=96
        i64.const 2
        i64.shl
        i64.and
        i64.or
        i64.store offset=96
        local.get $l4
        local.get $l4
        i32.load offset=108
        local.get $l4
        i64.load offset=96
        local.get $l4
        i32.const 76
        i32.add
        call $kh_put_oligonucleotide
        i32.store offset=72
        block $B4
          local.get $l4
          i32.load offset=76
          if $I5
            local.get $l4
            i32.load offset=108
            i32.load offset=24
            local.get $l4
            i32.load offset=72
            i32.const 2
            i32.shl
            i32.add
            i32.const 1
            i32.store
            br $B4
          end
          local.get $l4
          i32.load offset=108
          i32.load offset=24
          local.get $l4
          i32.load offset=72
          i32.const 2
          i32.shl
          i32.add
          local.tee $l10
          local.get $l10
          i32.load
          i32.const 1
          i32.add
          i32.store
        end
        local.get $l4
        local.get $l4
        i32.load offset=80
        i32.const 1
        i32.add
        i32.store offset=80
        br $L2
      end
    end
    local.get $l4
    local.get $l4
    i32.load offset=108
    i32.load offset=4
    i32.store offset=68
    local.get $l4
    i32.const 0
    i32.store offset=64
    local.get $l4
    local.get $l4
    i32.load offset=68
    i32.const 4
    i32.shl
    call $malloc
    i32.store offset=60
    local.get $l4
    i32.const 0
    i32.store offset=52
    loop $L6
      local.get $l4
      i32.load offset=52
      local.get $l4
      i32.load offset=108
      i32.load
      i32.ne
      if $I7
        local.get $l4
        i32.load offset=108
        i32.load offset=16
        local.get $l4
        i32.load offset=52
        i32.const 4
        i32.shr_u
        i32.const 2
        i32.shl
        i32.add
        i32.load
        local.get $l4
        i32.load offset=52
        i32.const 15
        i32.and
        i32.const 1
        i32.shl
        i32.shr_u
        i32.const 3
        i32.and
        i32.eqz
        if $I8
          local.get $l4
          local.get $l4
          i32.load offset=108
          i32.load offset=20
          local.get $l4
          i32.load offset=52
          i32.const 3
          i32.shl
          i32.add
          i64.load
          i64.store offset=96
          local.get $l4
          local.get $l4
          i32.load offset=108
          i32.load offset=24
          local.get $l4
          i32.load offset=52
          i32.const 2
          i32.shl
          i32.add
          i32.load
          i32.store offset=56
          local.get $l4
          i32.load offset=60
          local.set $l11
          local.get $l4
          local.get $l4
          i32.load offset=64
          local.tee $l12
          i32.const 1
          i32.add
          i32.store offset=64
          local.get $l4
          local.get $l4
          i64.load offset=96
          i64.store offset=32
          local.get $l4
          local.get $l4
          i32.load offset=56
          i32.store offset=40
          local.get $l12
          i32.const 4
          i32.shl
          local.get $l11
          i32.add
          local.tee $l13
          local.get $l4
          i64.load offset=32
          i64.store
          i32.const 8
          local.tee $l14
          local.get $l13
          i32.add
          local.get $l4
          i32.const 32
          i32.add
          local.get $l14
          i32.add
          i64.load
          i64.store
        end
        local.get $l4
        local.get $l4
        i32.load offset=52
        i32.const 1
        i32.add
        i32.store offset=52
        br $L6
      end
    end
    local.get $l4
    i32.load offset=108
    call $kh_destroy_oligonucleotide
    local.get $l4
    i32.load offset=60
    local.get $l4
    i32.load offset=68
    i32.const 16
    i32.const 1
    call $qsort
    local.get $l4
    i32.const 0
    i32.store offset=28
    local.get $l4
    i32.const 0
    i32.store offset=24
    loop $L9
      local.get $l4
      i32.load offset=24
      local.get $l4
      i32.load offset=68
      i32.ge_s
      i32.eqz
      if $I10
        local.get $l4
        i32.load offset=116
        local.set $l6
        local.get $l4
        local.get $l5
        i32.store offset=20
        local.get $l5
        local.get $l6
        i32.const 16
        i32.add
        i32.const -16
        i32.and
        i32.sub
        local.tee $l7
        local.tee $l5
        global.set $__stack_pointer
        local.get $l4
        local.get $l6
        i32.const 1
        i32.add
        i32.store offset=16
        local.get $l4
        local.get $l4
        i32.load offset=116
        i32.const 1
        i32.sub
        i32.store offset=12
        loop $L11
          local.get $l4
          i32.load offset=12
          i32.const 0
          i32.lt_s
          i32.eqz
          if $I12
            local.get $l4
            i32.load offset=12
            local.get $l7
            i32.add
            local.get $l4
            i32.load offset=60
            local.get $l4
            i32.load offset=24
            i32.const 4
            i32.shl
            i32.add
            i64.load
            i64.const 3
            i64.and
            i32.wrap_i64
            i32.load8_u offset=1048
            i32.store8
            local.get $l4
            i32.load offset=60
            local.get $l4
            i32.load offset=24
            i32.const 4
            i32.shl
            i32.add
            local.tee $l15
            local.get $l15
            i64.load
            i64.const 2
            i64.shr_u
            i64.store
            local.get $l4
            local.get $l4
            i32.load offset=12
            i32.const 1
            i32.sub
            i32.store offset=12
            br $L11
          end
        end
        local.get $l4
        i32.load offset=116
        local.get $l7
        i32.add
        i32.const 0
        i32.store8
        local.get $l4
        i32.load offset=20
        local.set $l5
        local.get $l4
        local.get $l4
        i32.load offset=24
        i32.const 1
        i32.add
        i32.store offset=24
        br $L9
      end
    end
    local.get $l4
    i32.load offset=60
    call $free
    local.get $l4
    i32.const 128
    i32.add
    global.set $__stack_pointer)
  (func $kh_init_oligonucleotide (type $t5) (result i32)
    i32.const 1
    i32.const 28
    call $calloc)
  (func $kh_put_oligonucleotide (type $t7) (param $p0 i32) (param $p1 i64) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32)
    global.get $__stack_pointer
    i32.const 48
    i32.sub
    local.tee $l3
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=40
    local.get $l3
    local.get $p1
    i64.store offset=32
    local.get $l3
    local.get $p2
    i32.store offset=28
    block $B0
      local.get $l3
      i32.load offset=40
      i32.load offset=8
      local.get $l3
      i32.load offset=40
      i32.load offset=12
      i32.ge_u
      if $I1
        block $B2
          local.get $l3
          i32.load offset=40
          i32.load
          local.get $l3
          i32.load offset=40
          i32.load offset=4
          i32.const 1
          i32.shl
          i32.gt_u
          if $I3
            local.get $l3
            i32.load offset=40
            local.get $l3
            i32.load offset=40
            i32.load
            i32.const 1
            i32.sub
            call $kh_resize_oligonucleotide
            i32.const 0
            i32.lt_s
            if $I4
              local.get $l3
              i32.load offset=28
              i32.const -1
              i32.store
              local.get $l3
              local.get $l3
              i32.load offset=40
              i32.load
              i32.store offset=44
              br $B0
            end
            br $B2
          end
          local.get $l3
          i32.load offset=40
          local.get $l3
          i32.load offset=40
          i32.load
          i32.const 1
          i32.add
          call $kh_resize_oligonucleotide
          i32.const 0
          i32.lt_s
          if $I5
            local.get $l3
            i32.load offset=28
            i32.const -1
            i32.store
            local.get $l3
            local.get $l3
            i32.load offset=40
            i32.load
            i32.store offset=44
            br $B0
          end
        end
      end
      local.get $l3
      local.get $l3
      i32.load offset=40
      i32.load
      i32.const 1
      i32.sub
      i32.store offset=4
      local.get $l3
      i32.const 0
      i32.store
      local.get $l3
      local.get $l3
      i32.load offset=40
      i32.load
      local.tee $l6
      i32.store offset=12
      local.get $l3
      local.get $l6
      i32.store offset=24
      local.get $l3
      local.get $l3
      i64.load offset=32
      local.get $l3
      i64.load offset=32
      i64.const 7
      i64.shr_u
      i64.xor
      i64.store32 offset=20
      local.get $l3
      local.get $l3
      i32.load offset=20
      local.get $l3
      i32.load offset=4
      i32.and
      i32.store offset=16
      block $B6
        local.get $l3
        i32.load offset=40
        i32.load offset=16
        local.get $l3
        i32.load offset=16
        i32.const 4
        i32.shr_u
        i32.const 2
        i32.shl
        i32.add
        i32.load
        local.get $l3
        i32.load offset=16
        i32.const 15
        i32.and
        i32.const 1
        i32.shl
        i32.shr_u
        i32.const 2
        i32.and
        if $I7
          local.get $l3
          local.get $l3
          i32.load offset=16
          i32.store offset=24
          br $B6
        end
        local.get $l3
        local.get $l3
        i32.load offset=16
        i32.store offset=8
        loop $L8
          i32.const 0
          local.set $l4
          local.get $l3
          i32.load offset=40
          i32.load offset=16
          local.get $l3
          i32.load offset=16
          i32.const 4
          i32.shr_u
          i32.const 2
          i32.shl
          i32.add
          i32.load
          local.get $l3
          i32.load offset=16
          i32.const 15
          i32.and
          i32.const 1
          i32.shl
          i32.shr_u
          i32.const 2
          i32.and
          i32.eqz
          if $I9
            i32.const 1
            local.set $l5
            local.get $l3
            i32.load offset=40
            i32.load offset=16
            local.get $l3
            i32.load offset=16
            i32.const 4
            i32.shr_u
            i32.const 2
            i32.shl
            i32.add
            i32.load
            local.get $l3
            i32.load offset=16
            i32.const 15
            i32.and
            i32.const 1
            i32.shl
            i32.shr_u
            i32.const 1
            i32.and
            i32.eqz
            if $I10
              local.get $l3
              i32.load offset=40
              i32.load offset=20
              local.get $l3
              i32.load offset=16
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.get $l3
              i64.load offset=32
              i64.eq
              i32.const -1
              i32.xor
              local.set $l5
            end
            local.get $l5
            local.set $l4
          end
          block $B11
            local.get $l4
            i32.const 1
            i32.and
            i32.eqz
            br_if $B11
            local.get $l3
            i32.load offset=40
            i32.load offset=16
            local.get $l3
            i32.load offset=16
            i32.const 4
            i32.shr_u
            i32.const 2
            i32.shl
            i32.add
            i32.load
            local.get $l3
            i32.load offset=16
            i32.const 15
            i32.and
            i32.const 1
            i32.shl
            i32.shr_u
            i32.const 1
            i32.and
            if $I12
              local.get $l3
              local.get $l3
              i32.load offset=16
              i32.store offset=12
            end
            local.get $l3
            i32.load offset=16
            local.set $l7
            local.get $l3
            local.get $l3
            i32.load
            i32.const 1
            i32.add
            local.tee $l8
            i32.store
            local.get $l3
            local.get $l3
            i32.load offset=4
            local.get $l7
            local.get $l8
            i32.add
            i32.and
            i32.store offset=16
            local.get $l3
            i32.load offset=16
            local.get $l3
            i32.load offset=8
            i32.eq
            if $I13
              local.get $l3
              local.get $l3
              i32.load offset=12
              i32.store offset=24
              br $B11
            end
            br $L8
          end
        end
        local.get $l3
        i32.load offset=24
        local.get $l3
        i32.load offset=40
        i32.load
        i32.eq
        if $I14
          block $B15
            block $B16
              local.get $l3
              i32.load offset=40
              i32.load offset=16
              local.get $l3
              i32.load offset=16
              i32.const 4
              i32.shr_u
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.get $l3
              i32.load offset=16
              i32.const 15
              i32.and
              i32.const 1
              i32.shl
              i32.shr_u
              i32.const 2
              i32.and
              i32.eqz
              br_if $B16
              local.get $l3
              i32.load offset=12
              local.get $l3
              i32.load offset=40
              i32.load
              i32.eq
              br_if $B16
              local.get $l3
              local.get $l3
              i32.load offset=12
              i32.store offset=24
              br $B15
            end
            local.get $l3
            local.get $l3
            i32.load offset=16
            i32.store offset=24
          end
        end
      end
      block $B17
        local.get $l3
        i32.load offset=40
        i32.load offset=16
        local.get $l3
        i32.load offset=24
        i32.const 4
        i32.shr_u
        i32.const 2
        i32.shl
        i32.add
        i32.load
        local.get $l3
        i32.load offset=24
        i32.const 15
        i32.and
        i32.const 1
        i32.shl
        i32.shr_u
        i32.const 2
        i32.and
        if $I18
          local.get $l3
          i32.load offset=40
          i32.load offset=20
          local.get $l3
          i32.load offset=24
          i32.const 3
          i32.shl
          i32.add
          local.get $l3
          i64.load offset=32
          i64.store
          local.get $l3
          i32.load offset=40
          i32.load offset=16
          local.get $l3
          i32.load offset=24
          i32.const 4
          i32.shr_u
          i32.const 2
          i32.shl
          i32.add
          local.tee $l9
          local.get $l9
          i32.load
          i32.const 3
          local.get $l3
          i32.load offset=24
          i32.const 15
          i32.and
          i32.const 1
          i32.shl
          i32.shl
          i32.const -1
          i32.xor
          i32.and
          i32.store
          local.get $l3
          i32.load offset=40
          local.tee $l10
          local.get $l10
          i32.load offset=4
          i32.const 1
          i32.add
          i32.store offset=4
          local.get $l3
          i32.load offset=40
          local.tee $l11
          local.get $l11
          i32.load offset=8
          i32.const 1
          i32.add
          i32.store offset=8
          local.get $l3
          i32.load offset=28
          i32.const 1
          i32.store
          br $B17
        end
        block $B19
          local.get $l3
          i32.load offset=40
          i32.load offset=16
          local.get $l3
          i32.load offset=24
          i32.const 4
          i32.shr_u
          i32.const 2
          i32.shl
          i32.add
          i32.load
          local.get $l3
          i32.load offset=24
          i32.const 15
          i32.and
          i32.const 1
          i32.shl
          i32.shr_u
          i32.const 1
          i32.and
          if $I20
            local.get $l3
            i32.load offset=40
            i32.load offset=20
            local.get $l3
            i32.load offset=24
            i32.const 3
            i32.shl
            i32.add
            local.get $l3
            i64.load offset=32
            i64.store
            local.get $l3
            i32.load offset=40
            i32.load offset=16
            local.get $l3
            i32.load offset=24
            i32.const 4
            i32.shr_u
            i32.const 2
            i32.shl
            i32.add
            local.tee $l12
            local.get $l12
            i32.load
            i32.const 3
            local.get $l3
            i32.load offset=24
            i32.const 15
            i32.and
            i32.const 1
            i32.shl
            i32.shl
            i32.const -1
            i32.xor
            i32.and
            i32.store
            local.get $l3
            i32.load offset=40
            local.tee $l13
            local.get $l13
            i32.load offset=4
            i32.const 1
            i32.add
            i32.store offset=4
            local.get $l3
            i32.load offset=28
            i32.const 2
            i32.store
            br $B19
          end
          local.get $l3
          i32.load offset=28
          i32.const 0
          i32.store
        end
      end
      local.get $l3
      local.get $l3
      i32.load offset=24
      i32.store offset=44
    end
    local.get $l3
    i32.load offset=44
    local.set $l14
    local.get $l3
    i32.const 48
    i32.add
    global.set $__stack_pointer
    local.get $l14)
  (func $kh_get_oligonucleotide (type $t8) (param $p0 i32) (param $p1 i64) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    global.get $__stack_pointer
    i32.const 48
    i32.sub
    local.tee $l2
    local.get $p0
    i32.store offset=40
    local.get $l2
    local.get $p1
    i64.store offset=32
    block $B0
      local.get $l2
      i32.load offset=40
      i32.load
      if $I1
        local.get $l2
        i32.const 0
        i32.store offset=12
        local.get $l2
        local.get $l2
        i32.load offset=40
        i32.load
        i32.const 1
        i32.sub
        i32.store offset=16
        local.get $l2
        local.get $l2
        i64.load offset=32
        local.get $l2
        i64.load offset=32
        i64.const 7
        i64.shr_u
        i64.xor
        i64.store32 offset=28
        local.get $l2
        local.get $l2
        i32.load offset=28
        local.get $l2
        i32.load offset=16
        i32.and
        i32.store offset=24
        local.get $l2
        local.get $l2
        i32.load offset=24
        i32.store offset=20
        loop $L2
          i32.const 0
          local.set $l3
          local.get $l2
          i32.load offset=40
          i32.load offset=16
          local.get $l2
          i32.load offset=24
          i32.const 4
          i32.shr_u
          i32.const 2
          i32.shl
          i32.add
          i32.load
          local.get $l2
          i32.load offset=24
          i32.const 15
          i32.and
          i32.const 1
          i32.shl
          i32.shr_u
          i32.const 2
          i32.and
          i32.eqz
          if $I3
            i32.const 1
            local.set $l4
            local.get $l2
            i32.load offset=40
            i32.load offset=16
            local.get $l2
            i32.load offset=24
            i32.const 4
            i32.shr_u
            i32.const 2
            i32.shl
            i32.add
            i32.load
            local.get $l2
            i32.load offset=24
            i32.const 15
            i32.and
            i32.const 1
            i32.shl
            i32.shr_u
            i32.const 1
            i32.and
            i32.eqz
            if $I4
              local.get $l2
              i32.load offset=40
              i32.load offset=20
              local.get $l2
              i32.load offset=24
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.get $l2
              i64.load offset=32
              i64.eq
              i32.const -1
              i32.xor
              local.set $l4
            end
            local.get $l4
            local.set $l3
          end
          local.get $l3
          i32.const 1
          i32.and
          if $I5
            local.get $l2
            i32.load offset=24
            local.set $l5
            local.get $l2
            local.get $l2
            i32.load offset=12
            i32.const 1
            i32.add
            local.tee $l6
            i32.store offset=12
            local.get $l2
            local.get $l2
            i32.load offset=16
            local.get $l5
            local.get $l6
            i32.add
            i32.and
            i32.store offset=24
            local.get $l2
            i32.load offset=24
            local.get $l2
            i32.load offset=20
            i32.eq
            if $I6
              local.get $l2
              local.get $l2
              i32.load offset=40
              i32.load
              i32.store offset=44
              br $B0
            end
            br $L2
          end
        end
        local.get $l2
        block $B7 (result i32)
          local.get $l2
          i32.load offset=40
          i32.load offset=16
          local.get $l2
          i32.load offset=24
          i32.const 4
          i32.shr_u
          i32.const 2
          i32.shl
          i32.add
          i32.load
          local.get $l2
          i32.load offset=24
          i32.const 15
          i32.and
          i32.const 1
          i32.shl
          i32.shr_u
          i32.const 3
          i32.and
          if $I8
            local.get $l2
            i32.load offset=40
            i32.load
            br $B7
          end
          local.get $l2
          i32.load offset=24
        end
        i32.store offset=44
        br $B0
      end
      local.get $l2
      i32.const 0
      i32.store offset=44
    end
    local.get $l2
    i32.load offset=44)
  (func $kh_destroy_oligonucleotide (type $t4) (param $p0 i32)
    (local $l1 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store offset=12
    local.get $l1
    i32.load offset=12
    if $I0
      local.get $l1
      i32.load offset=12
      i32.load offset=20
      call $free
      local.get $l1
      i32.load offset=12
      i32.load offset=16
      call $free
      local.get $l1
      i32.load offset=12
      i32.load offset=24
      call $free
      local.get $l1
      i32.load offset=12
      call $free
    end
    local.get $l1
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $element_Compare (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l2
    local.get $p0
    i32.store offset=8
    local.get $l2
    local.get $p1
    i32.store offset=4
    block $B0
      local.get $l2
      i32.load offset=8
      i32.load offset=8
      local.get $l2
      i32.load offset=4
      i32.load offset=8
      i32.lt_u
      if $I1
        local.get $l2
        i32.const 1
        i32.store offset=12
        br $B0
      end
      local.get $l2
      i32.load offset=8
      i32.load offset=8
      local.get $l2
      i32.load offset=4
      i32.load offset=8
      i32.gt_u
      if $I2
        local.get $l2
        i32.const -1
        i32.store offset=12
        br $B0
      end
      local.get $l2
      i32.const 1
      i32.const -1
      local.get $l2
      i32.load offset=8
      i64.load
      local.get $l2
      i32.load offset=4
      i64.load
      i64.gt_u
      select
      i32.store offset=12
    end
    local.get $l2
    i32.load offset=12)
  (func $kh_resize_oligonucleotide (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 f64) (local $l4 f64) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    global.get $__stack_pointer
    i32.const 80
    i32.sub
    local.tee $l2
    global.set $__stack_pointer
    local.get $l2
    local.get $p0
    i32.store offset=72
    local.get $l2
    local.get $p1
    i32.store offset=68
    local.get $l2
    i32.const 0
    i32.store offset=64
    local.get $l2
    i32.const 1
    i32.store offset=60
    local.get $l2
    local.get $l2
    i32.load offset=68
    i32.const 1
    i32.sub
    i32.store offset=68
    local.get $l2
    local.get $l2
    i32.load offset=68
    local.get $l2
    i32.load offset=68
    i32.const 1
    i32.shr_u
    i32.or
    i32.store offset=68
    local.get $l2
    local.get $l2
    i32.load offset=68
    local.get $l2
    i32.load offset=68
    i32.const 2
    i32.shr_u
    i32.or
    i32.store offset=68
    local.get $l2
    local.get $l2
    i32.load offset=68
    local.get $l2
    i32.load offset=68
    i32.const 4
    i32.shr_u
    i32.or
    i32.store offset=68
    local.get $l2
    local.get $l2
    i32.load offset=68
    local.get $l2
    i32.load offset=68
    i32.const 8
    i32.shr_u
    i32.or
    i32.store offset=68
    local.get $l2
    local.get $l2
    i32.load offset=68
    local.get $l2
    i32.load offset=68
    i32.const 16
    i32.shr_u
    i32.or
    i32.store offset=68
    local.get $l2
    local.get $l2
    i32.load offset=68
    i32.const 1
    i32.add
    i32.store offset=68
    local.get $l2
    i32.load offset=68
    i32.const 4
    i32.lt_u
    if $I0
      local.get $l2
      i32.const 4
      i32.store offset=68
    end
    block $B1
      block $B2
        block $B3 (result i32)
          local.get $l2
          i32.load offset=68
          f64.convert_i32_u
          f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)
          f64.mul
          f64.const 0x1p-1 (;=0.5;)
          f64.add
          local.tee $l3
          f64.const 0x1p+32 (;=4.29497e+09;)
          f64.lt
          local.get $l3
          f64.const 0x0p+0 (;=0;)
          f64.ge
          i32.and
          if $I4
            local.get $l3
            i32.trunc_f64_u
            br $B3
          end
          i32.const 0
        end
        local.get $l2
        i32.load offset=72
        i32.load offset=4
        i32.le_u
        if $I5
          local.get $l2
          i32.const 0
          i32.store offset=60
          br $B2
        end
        local.get $l2
        local.get $l2
        i32.load offset=68
        i32.const 16
        i32.lt_u
        if $I6 (result i32)
          i32.const 1
        else
          local.get $l2
          i32.load offset=68
          i32.const 4
          i32.shr_u
        end
        i32.const 2
        i32.shl
        call $malloc
        i32.store offset=64
        local.get $l2
        i32.load offset=64
        i32.eqz
        if $I7
          local.get $l2
          i32.const -1
          i32.store offset=76
          br $B1
        end
        local.get $l2
        i32.load offset=64
        i32.const 170
        local.get $l2
        i32.load offset=68
        i32.const 16
        i32.lt_u
        if $I8 (result i32)
          i32.const 1
        else
          local.get $l2
          i32.load offset=68
          i32.const 4
          i32.shr_u
        end
        i32.const 2
        i32.shl
        call $memset
        drop
        local.get $l2
        i32.load offset=72
        i32.load
        local.get $l2
        i32.load offset=68
        i32.lt_u
        if $I9
          local.get $l2
          local.get $l2
          i32.load offset=72
          i32.load offset=20
          local.get $l2
          i32.load offset=68
          i32.const 3
          i32.shl
          call $realloc
          i32.store offset=56
          local.get $l2
          i32.load offset=56
          i32.eqz
          if $I10
            local.get $l2
            i32.load offset=64
            call $free
            local.get $l2
            i32.const -1
            i32.store offset=76
            br $B1
          end
          local.get $l2
          i32.load offset=72
          local.get $l2
          i32.load offset=56
          i32.store offset=20
          local.get $l2
          local.get $l2
          i32.load offset=72
          i32.load offset=24
          local.get $l2
          i32.load offset=68
          i32.const 2
          i32.shl
          call $realloc
          i32.store offset=52
          local.get $l2
          i32.load offset=52
          i32.eqz
          if $I11
            local.get $l2
            i32.load offset=64
            call $free
            local.get $l2
            i32.const -1
            i32.store offset=76
            br $B1
          end
          local.get $l2
          i32.load offset=72
          local.get $l2
          i32.load offset=52
          i32.store offset=24
        end
      end
      local.get $l2
      i32.load offset=60
      if $I12
        local.get $l2
        i32.const 0
        i32.store offset=60
        loop $L13
          local.get $l2
          i32.load offset=60
          local.get $l2
          i32.load offset=72
          i32.load
          i32.ne
          if $I14
            local.get $l2
            i32.load offset=72
            i32.load offset=16
            local.get $l2
            i32.load offset=60
            i32.const 4
            i32.shr_u
            i32.const 2
            i32.shl
            i32.add
            i32.load
            local.get $l2
            i32.load offset=60
            i32.const 15
            i32.and
            i32.const 1
            i32.shl
            i32.shr_u
            i32.const 3
            i32.and
            i32.eqz
            if $I15
              local.get $l2
              local.get $l2
              i32.load offset=72
              i32.load offset=20
              local.get $l2
              i32.load offset=60
              i32.const 3
              i32.shl
              i32.add
              i64.load
              i64.store offset=40
              local.get $l2
              local.get $l2
              i32.load offset=68
              i32.const 1
              i32.sub
              i32.store offset=32
              local.get $l2
              local.get $l2
              i32.load offset=72
              i32.load offset=24
              local.get $l2
              i32.load offset=60
              i32.const 2
              i32.shl
              i32.add
              i32.load
              i32.store offset=36
              local.get $l2
              i32.load offset=72
              i32.load offset=16
              local.get $l2
              i32.load offset=60
              i32.const 4
              i32.shr_u
              i32.const 2
              i32.shl
              i32.add
              local.tee $l5
              local.get $l5
              i32.load
              i32.const 1
              local.get $l2
              i32.load offset=60
              i32.const 15
              i32.and
              i32.const 1
              i32.shl
              i32.shl
              i32.or
              i32.store
              loop $L16
                local.get $l2
                i32.const 0
                i32.store offset=20
                local.get $l2
                local.get $l2
                i64.load offset=40
                local.get $l2
                i64.load offset=40
                i64.const 7
                i64.shr_u
                i64.xor
                i64.store32 offset=28
                local.get $l2
                local.get $l2
                i32.load offset=28
                local.get $l2
                i32.load offset=32
                i32.and
                i32.store offset=24
                loop $L17
                  local.get $l2
                  i32.load offset=64
                  local.get $l2
                  i32.load offset=24
                  i32.const 4
                  i32.shr_u
                  i32.const 2
                  i32.shl
                  i32.add
                  i32.load
                  local.get $l2
                  i32.load offset=24
                  i32.const 15
                  i32.and
                  i32.const 1
                  i32.shl
                  i32.shr_u
                  i32.const 2
                  i32.and
                  i32.const 0
                  i32.ne
                  i32.const -1
                  i32.xor
                  i32.const 1
                  i32.and
                  if $I18
                    local.get $l2
                    i32.load offset=24
                    local.set $l6
                    local.get $l2
                    local.get $l2
                    i32.load offset=20
                    i32.const 1
                    i32.add
                    local.tee $l7
                    i32.store offset=20
                    local.get $l2
                    local.get $l2
                    i32.load offset=32
                    local.get $l6
                    local.get $l7
                    i32.add
                    i32.and
                    i32.store offset=24
                    br $L17
                  end
                end
                local.get $l2
                i32.load offset=64
                local.get $l2
                i32.load offset=24
                i32.const 4
                i32.shr_u
                i32.const 2
                i32.shl
                i32.add
                local.tee $l8
                local.get $l8
                i32.load
                i32.const 2
                local.get $l2
                i32.load offset=24
                i32.const 15
                i32.and
                i32.const 1
                i32.shl
                i32.shl
                i32.const -1
                i32.xor
                i32.and
                i32.store
                block $B19
                  local.get $l2
                  i32.load offset=24
                  local.get $l2
                  i32.load offset=72
                  i32.load
                  i32.ge_u
                  br_if $B19
                  local.get $l2
                  i32.load offset=72
                  i32.load offset=16
                  local.get $l2
                  i32.load offset=24
                  i32.const 4
                  i32.shr_u
                  i32.const 2
                  i32.shl
                  i32.add
                  i32.load
                  local.get $l2
                  i32.load offset=24
                  i32.const 15
                  i32.and
                  i32.const 1
                  i32.shl
                  i32.shr_u
                  i32.const 3
                  i32.and
                  br_if $B19
                  local.get $l2
                  local.get $l2
                  i32.load offset=72
                  i32.load offset=20
                  local.get $l2
                  i32.load offset=24
                  i32.const 3
                  i32.shl
                  i32.add
                  i64.load
                  i64.store offset=8
                  local.get $l2
                  i32.load offset=72
                  i32.load offset=20
                  local.get $l2
                  i32.load offset=24
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get $l2
                  i64.load offset=40
                  i64.store
                  local.get $l2
                  local.get $l2
                  i64.load offset=8
                  i64.store offset=40
                  local.get $l2
                  local.get $l2
                  i32.load offset=72
                  i32.load offset=24
                  local.get $l2
                  i32.load offset=24
                  i32.const 2
                  i32.shl
                  i32.add
                  i32.load
                  i32.store offset=4
                  local.get $l2
                  i32.load offset=72
                  i32.load offset=24
                  local.get $l2
                  i32.load offset=24
                  i32.const 2
                  i32.shl
                  i32.add
                  local.get $l2
                  i32.load offset=36
                  i32.store
                  local.get $l2
                  local.get $l2
                  i32.load offset=4
                  i32.store offset=36
                  local.get $l2
                  i32.load offset=72
                  i32.load offset=16
                  local.get $l2
                  i32.load offset=24
                  i32.const 4
                  i32.shr_u
                  i32.const 2
                  i32.shl
                  i32.add
                  local.tee $l9
                  local.get $l9
                  i32.load
                  i32.const 1
                  local.get $l2
                  i32.load offset=24
                  i32.const 15
                  i32.and
                  i32.const 1
                  i32.shl
                  i32.shl
                  i32.or
                  i32.store
                  br $L16
                end
              end
              local.get $l2
              i32.load offset=72
              i32.load offset=20
              local.get $l2
              i32.load offset=24
              i32.const 3
              i32.shl
              i32.add
              local.get $l2
              i64.load offset=40
              i64.store
              local.get $l2
              i32.load offset=72
              i32.load offset=24
              local.get $l2
              i32.load offset=24
              i32.const 2
              i32.shl
              i32.add
              local.get $l2
              i32.load offset=36
              i32.store
            end
            local.get $l2
            local.get $l2
            i32.load offset=60
            i32.const 1
            i32.add
            i32.store offset=60
            br $L13
          end
        end
        local.get $l2
        i32.load offset=72
        i32.load
        local.get $l2
        i32.load offset=68
        i32.gt_u
        if $I20
          local.get $l2
          i32.load offset=72
          i32.load offset=20
          local.get $l2
          i32.load offset=68
          i32.const 3
          i32.shl
          call $realloc
          local.set $l10
          local.get $l2
          i32.load offset=72
          local.get $l10
          i32.store offset=20
          local.get $l2
          i32.load offset=72
          i32.load offset=24
          local.get $l2
          i32.load offset=68
          i32.const 2
          i32.shl
          call $realloc
          local.set $l11
          local.get $l2
          i32.load offset=72
          local.get $l11
          i32.store offset=24
        end
        local.get $l2
        i32.load offset=72
        i32.load offset=16
        call $free
        local.get $l2
        i32.load offset=72
        local.get $l2
        i32.load offset=64
        i32.store offset=16
        local.get $l2
        i32.load offset=72
        local.get $l2
        i32.load offset=68
        i32.store
        local.get $l2
        i32.load offset=72
        local.tee $l12
        local.get $l12
        i32.load offset=4
        i32.store offset=8
        local.get $l2
        i32.load offset=72
        block $B21 (result i32)
          local.get $l2
          i32.load offset=72
          i32.load
          f64.convert_i32_u
          f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)
          f64.mul
          f64.const 0x1p-1 (;=0.5;)
          f64.add
          local.tee $l4
          f64.const 0x1p+32 (;=4.29497e+09;)
          f64.lt
          local.get $l4
          f64.const 0x0p+0 (;=0;)
          f64.ge
          i32.and
          if $I22
            local.get $l4
            i32.trunc_f64_u
            br $B21
          end
          i32.const 0
        end
        i32.store offset=12
      end
      local.get $l2
      i32.const 0
      i32.store offset=76
    end
    local.get $l2
    i32.load offset=76
    local.set $l13
    local.get $l2
    i32.const 80
    i32.add
    global.set $__stack_pointer
    local.get $l13)
  (table $__indirect_function_table 2 2 funcref)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66640))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1093))
  (global $__stack_low i32 (i32.const 1104))
  (global $__stack_high i32 (i32.const 66640))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66640))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "_start" (func $_start))
  (export "__indirect_function_table" (table $__indirect_function_table))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (elem $e0 (i32.const 1) func $element_Compare)
  (data $.rodata (i32.const 1024) "GGTATT\00GGTATTTTAATT\00GGT\00ACGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA\00 \00 \01\03  \02"))
