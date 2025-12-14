(module $k-nucleotide.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (param i32 i32 i32) (result i32)))
  (type $t3 (func (param i32) (result i32)))
  (type $t4 (func (param i32 i32 i32 i32)))
  (type $t5 (func))
  (type $t6 (func (result i32)))
  (type $t7 (func (param i32 i64) (result i32)))
  (type $t8 (func (param i32 i64 i32) (result i32)))
  (type $t9 (func (param i32 i32)))
  (import "env" "calloc" (func $calloc (type $t0)))
  (import "env" "free" (func $free (type $t1)))
  (import "env" "memset" (func $memset (type $t2)))
  (import "env" "malloc" (func $malloc (type $t3)))
  (import "env" "realloc" (func $realloc (type $t0)))
  (import "env" "qsort" (func $qsort (type $t4)))
  (import "env" "strlen" (func $strlen (type $t3)))
  (import "env" "fgets" (func $fgets (type $t2)))
  (import "env" "memcmp" (func $memcmp (type $t2)))
  (func $__wasm_call_ctors (type $t5))
  (func $kh_init_oligonucleotide (type $t6) (result i32)
    i32.const 1
    i32.const 28
    call $calloc)
  (func $kh_destroy_oligonucleotide (type $t1) (param $p0 i32)
    block $B0
      local.get $p0
      i32.eqz
      br_if $B0
      local.get $p0
      i32.load offset=20
      call $free
      local.get $p0
      i32.load offset=16
      call $free
      local.get $p0
      i32.load offset=24
      call $free
      local.get $p0
      call $free
    end)
  (func $kh_clear_oligonucleotide (type $t1) (param $p0 i32)
    (local $l1 i32) (local $l2 i32)
    block $B0
      local.get $p0
      i32.eqz
      br_if $B0
      local.get $p0
      i32.load offset=16
      local.tee $l1
      i32.eqz
      br_if $B0
      local.get $l1
      i32.const 170
      i32.const 4
      local.get $p0
      i32.load
      local.tee $l2
      i32.const 2
      i32.shr_u
      i32.const 1073741820
      i32.and
      local.get $l2
      i32.const 16
      i32.lt_u
      select
      call $memset
      drop
      local.get $p0
      i64.const 0
      i64.store offset=4 align=4
    end)
  (func $kh_get_oligonucleotide (type $t7) (param $p0 i32) (param $p1 i64) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32)
    block $B0
      local.get $p0
      i32.load
      local.tee $l2
      br_if $B0
      i32.const 0
      return
    end
    local.get $p0
    i32.load offset=16
    local.set $l3
    i32.const 1
    local.set $l4
    local.get $l2
    i32.const -1
    i32.add
    local.tee $l5
    local.get $p1
    i64.const 7
    i64.shr_u
    local.get $p1
    i64.xor
    i32.wrap_i64
    i32.and
    local.tee $l6
    local.set $l7
    block $B1
      block $B2
        loop $L3
          local.get $l3
          local.get $l7
          i32.const 2
          i32.shr_u
          i32.const 1073741820
          i32.and
          i32.add
          i32.load
          local.tee $l8
          local.get $l7
          i32.const 1
          i32.shl
          local.tee $l9
          i32.shr_u
          local.tee $l10
          i32.const 2
          i32.and
          br_if $B2
          block $B4
            local.get $l10
            i32.const 1
            i32.and
            br_if $B4
            local.get $p0
            i32.load offset=20
            local.get $l7
            i32.const 3
            i32.shl
            i32.add
            i64.load
            local.get $p1
            i64.eq
            br_if $B2
          end
          local.get $l7
          local.get $l4
          i32.add
          local.set $l7
          local.get $l4
          i32.const 1
          i32.add
          local.set $l4
          local.get $l7
          local.get $l5
          i32.and
          local.tee $l7
          local.get $l6
          i32.ne
          br_if $L3
          br $B1
        end
      end
      local.get $l2
      local.get $l7
      local.get $l8
      local.get $l9
      i32.const 30
      i32.and
      i32.shr_u
      i32.const 3
      i32.and
      select
      local.set $l2
    end
    local.get $l2)
  (func $kh_resize_oligonucleotide (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 f64) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i64) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i64)
    block $B0
      block $B1
        local.get $p1
        i32.const -1
        i32.add
        local.tee $p1
        i32.const 1
        i32.shr_u
        local.get $p1
        i32.or
        local.tee $p1
        i32.const 2
        i32.shr_u
        local.get $p1
        i32.or
        local.tee $p1
        i32.const 4
        i32.shr_u
        local.get $p1
        i32.or
        local.tee $p1
        i32.const 8
        i32.shr_u
        local.get $p1
        i32.or
        local.tee $p1
        i32.const 16
        i32.shr_u
        local.get $p1
        i32.or
        i32.const 1
        i32.add
        local.tee $p1
        i32.const 4
        local.get $p1
        i32.const 4
        i32.gt_u
        select
        local.tee $l2
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
        i32.eqz
        br_if $B1
        local.get $l3
        i32.trunc_f64_u
        local.set $l4
        br $B0
      end
      i32.const 0
      local.set $l4
    end
    block $B2
      block $B3
        local.get $p0
        i32.load offset=4
        local.get $l4
        i32.ge_u
        br_if $B3
        block $B4
          i32.const 4
          local.get $l2
          i32.const 2
          i32.shr_u
          i32.const 1073741820
          i32.and
          local.get $p1
          i32.const 16
          i32.lt_u
          select
          local.tee $p1
          call $malloc
          local.tee $l5
          br_if $B4
          i32.const -1
          return
        end
        local.get $l5
        i32.const 170
        local.get $p1
        call $memset
        local.set $l6
        block $B5
          block $B6
            local.get $p0
            i32.load
            local.tee $p1
            local.get $l2
            i32.ge_u
            br_if $B6
            local.get $p0
            i32.load offset=20
            local.get $l2
            i32.const 3
            i32.shl
            call $realloc
            local.tee $p1
            i32.eqz
            br_if $B2
            local.get $p0
            local.get $p1
            i32.store offset=20
            local.get $p0
            i32.load offset=24
            local.get $l2
            i32.const 2
            i32.shl
            call $realloc
            local.tee $p1
            i32.eqz
            br_if $B2
            local.get $p0
            local.get $p1
            i32.store offset=24
            local.get $p0
            i32.load
            local.tee $p1
            i32.eqz
            br_if $B5
          end
          local.get $l2
          i32.const -1
          i32.add
          local.set $l7
          local.get $p0
          i32.load offset=16
          local.set $l8
          i32.const 0
          local.set $l9
          loop $L7
            block $B8
              local.get $l8
              local.get $l9
              i32.const 2
              i32.shr_u
              i32.const 1073741820
              i32.and
              i32.add
              local.tee $l5
              i32.load
              local.tee $l10
              local.get $l9
              i32.const 1
              i32.shl
              local.tee $l11
              i32.shr_u
              i32.const 3
              i32.and
              br_if $B8
              local.get $p0
              i32.load offset=24
              local.tee $l12
              local.get $l9
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.set $l13
              local.get $l5
              local.get $l10
              i32.const 1
              local.get $l11
              i32.const 30
              i32.and
              i32.shl
              i32.or
              i32.store
              local.get $p0
              i32.load offset=20
              local.tee $l14
              local.get $l9
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.set $l15
              loop $L9
                block $B10
                  block $B11
                    i32.const 2
                    local.get $l7
                    local.get $l15
                    i64.const 7
                    i64.shr_u
                    local.get $l15
                    i64.xor
                    i32.wrap_i64
                    i32.and
                    local.tee $p1
                    i32.const 1
                    i32.shl
                    local.tee $l5
                    i32.shl
                    local.tee $l10
                    local.get $l6
                    local.get $p1
                    i32.const 4
                    i32.shr_u
                    local.tee $l16
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l11
                    i32.load
                    local.tee $l17
                    i32.and
                    i32.eqz
                    br_if $B11
                    local.get $l5
                    i32.const 30
                    i32.and
                    local.set $l5
                    br $B10
                  end
                  i32.const 1
                  local.set $l5
                  loop $L12
                    local.get $p1
                    local.get $l5
                    i32.add
                    local.set $p1
                    local.get $l5
                    i32.const 1
                    i32.add
                    local.set $l5
                    i32.const 2
                    local.get $p1
                    local.get $l7
                    i32.and
                    local.tee $p1
                    i32.const 1
                    i32.shl
                    local.tee $l18
                    i32.shl
                    local.tee $l10
                    local.get $l6
                    local.get $p1
                    i32.const 4
                    i32.shr_u
                    local.tee $l16
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l11
                    i32.load
                    local.tee $l17
                    i32.and
                    i32.eqz
                    br_if $L12
                  end
                  local.get $l18
                  i32.const 30
                  i32.and
                  local.set $l5
                end
                local.get $l11
                local.get $l17
                local.get $l10
                i32.const -1
                i32.xor
                i32.and
                i32.store
                block $B13
                  block $B14
                    local.get $p1
                    local.get $p0
                    i32.load
                    i32.ge_u
                    br_if $B14
                    local.get $l8
                    local.get $l16
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l10
                    i32.load
                    local.get $l5
                    i32.shr_u
                    i32.const 3
                    i32.and
                    i32.eqz
                    br_if $B13
                  end
                  local.get $l12
                  local.get $p1
                  i32.const 2
                  i32.shl
                  i32.add
                  local.get $l13
                  i32.store
                  local.get $l14
                  local.get $p1
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get $l15
                  i64.store
                  local.get $p0
                  i32.load
                  local.set $p1
                  br $B8
                end
                local.get $l12
                local.get $p1
                i32.const 2
                i32.shl
                i32.add
                local.tee $l11
                i32.load
                local.set $l17
                local.get $l11
                local.get $l13
                i32.store
                local.get $l14
                local.get $p1
                i32.const 3
                i32.shl
                i32.add
                local.tee $p1
                i64.load
                local.set $l19
                local.get $p1
                local.get $l15
                i64.store
                local.get $l10
                local.get $l10
                i32.load
                i32.const 1
                local.get $l5
                i32.shl
                i32.or
                i32.store
                local.get $l19
                local.set $l15
                local.get $l17
                local.set $l13
                br $L9
              end
            end
            local.get $l9
            i32.const 1
            i32.add
            local.tee $l9
            local.get $p1
            i32.ne
            br_if $L7
          end
          local.get $p1
          local.get $l2
          i32.le_u
          br_if $B5
          local.get $p0
          local.get $p0
          i32.load offset=20
          local.get $l2
          i32.const 3
          i32.shl
          call $realloc
          i32.store offset=20
          local.get $p0
          local.get $p0
          i32.load offset=24
          local.get $l2
          i32.const 2
          i32.shl
          call $realloc
          i32.store offset=24
        end
        local.get $p0
        i32.load offset=16
        call $free
        local.get $p0
        local.get $l2
        i32.store
        local.get $p0
        local.get $l6
        i32.store offset=16
        local.get $p0
        local.get $l4
        i32.store offset=12
        local.get $p0
        local.get $p0
        i32.load offset=4
        i32.store offset=8
      end
      i32.const 0
      return
    end
    local.get $l6
    call $free
    i32.const -1)
  (func $kh_put_oligonucleotide (type $t8) (param $p0 i32) (param $p1 i64) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    block $B0
      local.get $p0
      i32.load offset=8
      local.get $p0
      i32.load offset=12
      i32.lt_u
      br_if $B0
      block $B1
        local.get $p0
        i32.load
        local.tee $l3
        local.get $p0
        i32.load offset=4
        i32.const 1
        i32.shl
        i32.le_u
        br_if $B1
        local.get $p0
        local.get $l3
        i32.const -1
        i32.add
        call $kh_resize_oligonucleotide
        i32.const -1
        i32.gt_s
        br_if $B0
        local.get $p2
        i32.const -1
        i32.store
        local.get $p0
        i32.load
        return
      end
      local.get $p0
      local.get $l3
      i32.const 1
      i32.add
      call $kh_resize_oligonucleotide
      i32.const -1
      i32.gt_s
      br_if $B0
      local.get $p2
      i32.const -1
      i32.store
      local.get $p0
      i32.load
      return
    end
    block $B2
      block $B3
        local.get $p0
        i32.load offset=16
        local.tee $l4
        local.get $p0
        i32.load
        local.tee $l5
        i32.const -1
        i32.add
        local.tee $l6
        local.get $p1
        i64.const 7
        i64.shr_u
        local.get $p1
        i64.xor
        i32.wrap_i64
        i32.and
        local.tee $l7
        i32.const 2
        i32.shr_u
        i32.const 1073741820
        i32.and
        i32.add
        i32.load
        local.get $l7
        i32.const 1
        i32.shl
        i32.shr_u
        i32.const 2
        i32.and
        i32.eqz
        br_if $B3
        local.get $l7
        local.set $l8
        br $B2
      end
      i32.const 1
      local.set $l9
      local.get $l7
      local.set $l3
      local.get $l5
      local.set $l8
      block $B4
        block $B5
          loop $L6
            local.get $l3
            i32.const 1
            i32.shl
            local.tee $l10
            i32.const 30
            i32.and
            local.set $l11
            local.get $l4
            local.get $l3
            i32.const 2
            i32.shr_u
            i32.const 1073741820
            i32.and
            i32.add
            i32.load
            local.tee $l12
            local.get $l10
            i32.shr_u
            local.tee $l10
            i32.const 2
            i32.and
            br_if $B5
            block $B7
              local.get $l10
              i32.const 1
              i32.and
              br_if $B7
              local.get $p0
              i32.load offset=20
              local.get $l3
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.get $p1
              i64.eq
              br_if $B5
            end
            local.get $l3
            local.get $l8
            local.get $l12
            local.get $l11
            i32.shr_u
            i32.const 1
            i32.and
            select
            local.set $l8
            local.get $l3
            local.get $l9
            i32.add
            local.set $l3
            local.get $l9
            i32.const 1
            i32.add
            local.set $l9
            local.get $l3
            local.get $l6
            i32.and
            local.tee $l3
            local.get $l7
            i32.ne
            br_if $L6
          end
          i32.const 1
          local.set $l10
          local.get $l5
          local.set $l9
          local.get $l8
          local.get $l5
          i32.eq
          br_if $B4
          br $B2
        end
        local.get $l12
        local.get $l11
        i32.shr_u
        i32.const 2
        i32.and
        i32.eqz
        local.set $l10
        local.get $l8
        local.set $l9
        local.get $l3
        local.set $l7
      end
      local.get $l7
      local.get $l7
      local.get $l9
      local.get $l9
      local.get $l5
      i32.eq
      select
      local.get $l10
      select
      local.set $l8
    end
    local.get $l8
    i32.const 1
    i32.shl
    local.tee $l3
    i32.const 30
    i32.and
    local.set $l9
    block $B8
      local.get $l4
      local.get $l8
      i32.const 2
      i32.shr_u
      i32.const 1073741820
      i32.and
      i32.add
      local.tee $l10
      i32.load
      local.tee $l11
      local.get $l3
      i32.shr_u
      local.tee $l3
      i32.const 2
      i32.and
      i32.eqz
      br_if $B8
      local.get $l10
      local.get $l11
      i32.const 3
      local.get $l9
      i32.shl
      i32.const -1
      i32.xor
      i32.and
      i32.store
      local.get $p0
      local.get $p0
      i32.load offset=8
      i32.const 1
      i32.add
      i32.store offset=8
      local.get $p0
      local.get $p0
      i32.load offset=4
      i32.const 1
      i32.add
      i32.store offset=4
      local.get $p0
      i32.load offset=20
      local.get $l8
      i32.const 3
      i32.shl
      i32.add
      local.get $p1
      i64.store
      local.get $p2
      i32.const 1
      i32.store
      local.get $l8
      return
    end
    block $B9
      local.get $l3
      i32.const 1
      i32.and
      i32.eqz
      br_if $B9
      local.get $l10
      local.get $l11
      i32.const 3
      local.get $l9
      i32.shl
      i32.const -1
      i32.xor
      i32.and
      i32.store
      local.get $p0
      local.get $p0
      i32.load offset=4
      i32.const 1
      i32.add
      i32.store offset=4
      local.get $p0
      i32.load offset=20
      local.get $l8
      i32.const 3
      i32.shl
      i32.add
      local.get $p1
      i64.store
      local.get $p2
      i32.const 2
      i32.store
      local.get $l8
      return
    end
    local.get $p2
    i32.const 0
    i32.store
    local.get $l8)
  (func $kh_del_oligonucleotide (type $t9) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32)
    block $B0
      local.get $p0
      i32.load
      local.get $p1
      i32.eq
      br_if $B0
      local.get $p0
      i32.load offset=16
      local.get $p1
      i32.const 2
      i32.shr_u
      i32.const 1073741820
      i32.and
      i32.add
      local.tee $l2
      i32.load
      local.tee $l3
      local.get $p1
      i32.const 1
      i32.shl
      local.tee $p1
      i32.shr_u
      i32.const 3
      i32.and
      br_if $B0
      local.get $l2
      local.get $l3
      i32.const 1
      local.get $p1
      i32.const 30
      i32.and
      i32.shl
      i32.or
      i32.store
      local.get $p0
      local.get $p0
      i32.load offset=4
      i32.const -1
      i32.add
      i32.store offset=4
    end)
  (func $element_Compare (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    block $B0
      local.get $p0
      i32.load offset=8
      local.tee $l2
      local.get $p1
      i32.load offset=8
      local.tee $l3
      i32.ge_u
      br_if $B0
      i32.const 1
      return
    end
    block $B1
      local.get $l2
      local.get $l3
      i32.le_u
      br_if $B1
      i32.const -1
      return
    end
    i32.const 1
    i32.const -1
    local.get $p0
    i64.load
    local.get $p1
    i64.load
    i64.gt_u
    select)
  (func $generate_Frequencies_For_Desired_Length_Oligonucleotides (type $t4) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i64) (local $l7 i64) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l4
    global.set $__stack_pointer
    local.get $p2
    i32.const -1
    i32.add
    local.set $l5
    i64.const -1
    local.get $p2
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const -1
    i64.xor
    local.set $l6
    i64.const 0
    local.set $l7
    i32.const 1
    i32.const 28
    call $calloc
    local.set $l8
    block $B0
      local.get $p2
      i32.const 2
      i32.lt_s
      br_if $B0
      local.get $l5
      i32.const 3
      i32.and
      local.set $l9
      block $B1
        block $B2
          local.get $p2
          i32.const -2
          i32.add
          i32.const 3
          i32.ge_u
          br_if $B2
          i32.const 0
          local.set $l10
          i64.const 0
          local.set $l7
          br $B1
        end
        local.get $l5
        i32.const -4
        i32.and
        local.set $l11
        i32.const 0
        local.set $l10
        i64.const 0
        local.set $l7
        loop $L3
          local.get $l7
          i64.const 2
          i64.shl
          local.get $l6
          i64.and
          local.get $p0
          local.get $l10
          i32.add
          local.tee $l12
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l6
          i64.and
          local.get $l12
          i32.const 1
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l6
          i64.and
          local.get $l12
          i32.const 2
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l6
          i64.and
          local.get $l12
          i32.const 3
          i32.add
          i64.load8_s
          i64.or
          local.set $l7
          local.get $l11
          local.get $l10
          i32.const 4
          i32.add
          local.tee $l10
          i32.ne
          br_if $L3
        end
      end
      local.get $l9
      i32.eqz
      br_if $B0
      local.get $p0
      local.get $l10
      i32.add
      local.set $l12
      loop $L4
        local.get $l7
        i64.const 2
        i64.shl
        local.get $l6
        i64.and
        local.get $l12
        i64.load8_s
        i64.or
        local.set $l7
        local.get $l12
        i32.const 1
        i32.add
        local.set $l12
        local.get $l9
        i32.const -1
        i32.add
        local.tee $l9
        br_if $L4
      end
    end
    block $B5
      block $B6
        local.get $p2
        local.get $p1
        i32.le_s
        br_if $B6
        i32.const 0
        local.set $p1
        i32.const 0
        call $malloc
        local.set $l11
        i32.const 0
        local.set $l13
        local.get $l8
        i32.load offset=16
        local.set $l14
        br $B5
      end
      local.get $p0
      local.get $l5
      i32.add
      local.set $l12
      local.get $p1
      local.get $p2
      i32.sub
      i32.const 1
      i32.add
      local.set $p0
      loop $L7
        local.get $l8
        local.get $l7
        i64.const 2
        i64.shl
        local.get $l6
        i64.and
        local.get $l12
        i64.load8_s
        i64.or
        local.tee $l7
        local.get $l4
        i32.const 12
        i32.add
        call $kh_put_oligonucleotide
        local.set $l10
        local.get $l8
        i32.load offset=24
        local.tee $l13
        local.get $l10
        i32.const 2
        i32.shl
        i32.add
        local.set $l10
        i32.const 1
        local.set $l9
        block $B8
          local.get $l4
          i32.load offset=12
          br_if $B8
          local.get $l10
          i32.load
          i32.const 1
          i32.add
          local.set $l9
        end
        local.get $l10
        local.get $l9
        i32.store
        local.get $l12
        i32.const 1
        i32.add
        local.set $l12
        local.get $p0
        i32.const -1
        i32.add
        local.tee $p0
        br_if $L7
      end
      local.get $l8
      i32.load
      local.set $l15
      local.get $l8
      i32.load offset=4
      local.tee $p1
      i32.const 4
      i32.shl
      call $malloc
      local.set $l11
      block $B9
        local.get $l15
        br_if $B9
        local.get $l8
        i32.load offset=16
        local.set $l14
        br $B5
      end
      local.get $l8
      i32.load offset=16
      local.set $l14
      i32.const 0
      local.set $l12
      i32.const 0
      local.set $l9
      local.get $l13
      local.set $p0
      i32.const 0
      local.set $l16
      i32.const 0
      local.set $l10
      loop $L10
        block $B11
          local.get $l14
          local.get $l10
          i32.const 2
          i32.shr_u
          i32.const 1073741820
          i32.and
          i32.add
          i32.load
          local.get $l12
          i32.const 30
          i32.and
          i32.shr_u
          i32.const 3
          i32.and
          br_if $B11
          local.get $l11
          local.get $l16
          i32.const 4
          i32.shl
          i32.add
          local.tee $l17
          local.get $p0
          i32.load
          i32.store offset=8
          local.get $l17
          local.get $l8
          i32.load offset=20
          local.get $l9
          i32.add
          i64.load
          i64.store
          local.get $l16
          i32.const 1
          i32.add
          local.set $l16
        end
        local.get $l12
        i32.const 2
        i32.add
        local.set $l12
        local.get $l9
        i32.const 8
        i32.add
        local.set $l9
        local.get $p0
        i32.const 4
        i32.add
        local.set $p0
        local.get $l15
        local.get $l10
        i32.const 1
        i32.add
        local.tee $l10
        i32.ne
        br_if $L10
      end
    end
    local.get $l8
    i32.load offset=20
    call $free
    local.get $l14
    call $free
    local.get $l13
    call $free
    local.get $l8
    call $free
    local.get $l11
    local.get $p1
    i32.const 16
    i32.const 1
    call $qsort
    block $B12
      local.get $p1
      i32.const 1
      i32.lt_s
      br_if $B12
      local.get $p2
      i32.const 1
      i32.lt_s
      br_if $B12
      local.get $p2
      i32.const 7
      i32.and
      local.set $p0
      i32.const 0
      local.set $l10
      local.get $p2
      i32.const 8
      i32.lt_u
      local.set $l13
      loop $L13
        local.get $l11
        local.get $l10
        i32.const 4
        i32.shl
        i32.add
        local.tee $l9
        i64.load
        local.set $l7
        block $B14
          block $B15
            local.get $p0
            br_if $B15
            local.get $l5
            local.set $l12
            br $B14
          end
          local.get $p0
          local.set $l8
          local.get $l5
          local.set $l12
          loop $L16
            local.get $l12
            i32.const -1
            i32.add
            local.set $l12
            local.get $l7
            i64.const 2
            i64.shr_u
            local.set $l7
            local.get $l8
            i32.const -1
            i32.add
            local.tee $l8
            br_if $L16
          end
        end
        block $B17
          local.get $l13
          br_if $B17
          local.get $l12
          i32.const 8
          i32.add
          local.set $l12
          loop $L18
            local.get $l7
            i64.const 16
            i64.shr_u
            local.set $l7
            local.get $l12
            i32.const -8
            i32.add
            local.tee $l12
            i32.const 7
            i32.gt_s
            br_if $L18
          end
        end
        local.get $l9
        local.get $l7
        i64.store
        local.get $l10
        i32.const 1
        i32.add
        local.tee $l10
        local.get $p1
        i32.ne
        br_if $L13
      end
    end
    local.get $l11
    call $free
    local.get $l4
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $generate_Count_For_Oligonucleotide (type $t4) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i64) (local $l8 i64) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l4
    global.set $__stack_pointer
    local.get $p2
    call $strlen
    local.tee $l5
    i32.const -1
    i32.add
    local.set $l6
    i64.const -1
    local.get $l5
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const -1
    i64.xor
    local.set $l7
    i64.const 0
    local.set $l8
    i32.const 1
    i32.const 28
    call $calloc
    local.set $l9
    block $B0
      local.get $l5
      i32.const 2
      i32.lt_s
      br_if $B0
      local.get $l6
      i32.const 3
      i32.and
      local.set $l10
      block $B1
        block $B2
          local.get $l5
          i32.const -2
          i32.add
          i32.const 3
          i32.ge_u
          br_if $B2
          i32.const 0
          local.set $l11
          i64.const 0
          local.set $l8
          br $B1
        end
        local.get $l6
        i32.const -4
        i32.and
        local.set $l12
        i32.const 0
        local.set $l11
        i64.const 0
        local.set $l8
        loop $L3
          local.get $l8
          i64.const 2
          i64.shl
          local.get $l7
          i64.and
          local.get $p0
          local.get $l11
          i32.add
          local.tee $p2
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l7
          i64.and
          local.get $p2
          i32.const 1
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l7
          i64.and
          local.get $p2
          i32.const 2
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l7
          i64.and
          local.get $p2
          i32.const 3
          i32.add
          i64.load8_s
          i64.or
          local.set $l8
          local.get $l12
          local.get $l11
          i32.const 4
          i32.add
          local.tee $l11
          i32.ne
          br_if $L3
        end
      end
      local.get $l10
      i32.eqz
      br_if $B0
      local.get $p0
      local.get $l11
      i32.add
      local.set $p2
      loop $L4
        local.get $l8
        i64.const 2
        i64.shl
        local.get $l7
        i64.and
        local.get $p2
        i64.load8_s
        i64.or
        local.set $l8
        local.get $p2
        i32.const 1
        i32.add
        local.set $p2
        local.get $l10
        i32.const -1
        i32.add
        local.tee $l10
        br_if $L4
      end
    end
    i32.const 0
    local.set $l12
    block $B5
      local.get $l5
      local.get $p1
      i32.gt_s
      br_if $B5
      local.get $p0
      local.get $l6
      i32.add
      local.set $p2
      local.get $p1
      local.get $l5
      i32.sub
      i32.const 1
      i32.add
      local.set $p0
      loop $L6
        local.get $l9
        local.get $l8
        i64.const 2
        i64.shl
        local.get $l7
        i64.and
        local.get $p2
        i64.load8_s
        i64.or
        local.tee $l8
        local.get $l4
        i32.const 12
        i32.add
        call $kh_put_oligonucleotide
        local.set $l11
        local.get $l9
        i32.load offset=24
        local.tee $l12
        local.get $l11
        i32.const 2
        i32.shl
        i32.add
        local.set $l11
        i32.const 1
        local.set $l10
        block $B7
          local.get $l4
          i32.load offset=12
          br_if $B7
          local.get $l11
          i32.load
          i32.const 1
          i32.add
          local.set $l10
        end
        local.get $l11
        local.get $l10
        i32.store
        local.get $p2
        i32.const 1
        i32.add
        local.set $p2
        local.get $p0
        i32.const -1
        i32.add
        local.tee $p0
        br_if $L6
      end
    end
    local.get $l9
    i32.load offset=20
    call $free
    local.get $l9
    i32.load offset=16
    call $free
    local.get $l12
    call $free
    local.get $l9
    call $free
    local.get $l4
    i32.const 16
    i32.add
    global.set $__stack_pointer)
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    global.get $__stack_pointer
    i32.const 4096
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    block $B0
      loop $L1
        local.get $l0
        i32.const 4096
        i32.const 0
        i32.load
        call $fgets
        i32.eqz
        br_if $B0
        i32.const 1076
        local.get $l0
        i32.const 6
        call $memcmp
        br_if $L1
      end
    end
    i32.const 1048576
    call $malloc
    local.set $l1
    block $B2
      block $B3
        local.get $l0
        i32.const 4096
        i32.const 0
        i32.load
        call $fgets
        br_if $B3
        i32.const 0
        local.set $l2
        br $B2
      end
      block $B4
        local.get $l0
        i32.load8_u
        local.tee $l3
        i32.const 255
        i32.and
        i32.const 62
        i32.ne
        br_if $B4
        i32.const 0
        local.set $l2
        br $B2
      end
      local.get $l0
      i32.const 1
      i32.or
      local.set $l4
      i32.const 1048576
      local.set $l5
      i32.const 0
      local.set $l2
      loop $L5
        local.get $l4
        local.set $l6
        loop $L6
          block $B7
            block $B8
              block $B9
                local.get $l3
                i32.const 255
                i32.and
                br_table $B9 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B7 $B8
              end
              block $B10
                local.get $l5
                local.get $l2
                i32.sub
                i32.const 4096
                i32.ge_u
                br_if $B10
                local.get $l1
                local.get $l5
                i32.const 1
                i32.shl
                local.tee $l5
                call $realloc
                local.set $l1
              end
              local.get $l0
              i32.const 4096
              i32.const 0
              i32.load
              call $fgets
              i32.eqz
              br_if $B2
              local.get $l0
              i32.load8_u
              local.tee $l3
              i32.const 255
              i32.and
              i32.const 62
              i32.ne
              br_if $L5
              br $B2
            end
            local.get $l1
            local.get $l2
            i32.add
            local.get $l3
            i32.const 7
            i32.and
            i32.const 1024
            i32.add
            i32.load8_u
            i32.store8
            local.get $l2
            i32.const 1
            i32.add
            local.set $l2
          end
          local.get $l6
          i32.load8_u
          local.set $l3
          local.get $l6
          i32.const 1
          i32.add
          local.set $l6
          br $L6
        end
      end
    end
    local.get $l1
    local.get $l2
    call $realloc
    local.tee $l6
    local.get $l2
    i32.const 1057
    local.get $l6
    call $generate_Count_For_Oligonucleotide
    local.get $l6
    local.get $l2
    i32.const 1040
    local.get $l6
    call $generate_Count_For_Oligonucleotide
    local.get $l6
    local.get $l2
    i32.const 1033
    local.get $l6
    call $generate_Count_For_Oligonucleotide
    local.get $l6
    local.get $l2
    i32.const 1083
    local.get $l6
    call $generate_Count_For_Oligonucleotide
    local.get $l6
    local.get $l2
    i32.const 1053
    local.get $l6
    call $generate_Count_For_Oligonucleotide
    local.get $l6
    local.get $l2
    i32.const 2
    local.get $l6
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get $l6
    local.get $l2
    i32.const 1
    local.get $l6
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get $l6
    call $free
    local.get $l0
    i32.const 4096
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (table $__indirect_function_table 2 2 funcref)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66624))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1088))
  (global $__stack_low i32 (i32.const 1088))
  (global $__stack_high i32 (i32.const 66624))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66624))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "kh_init_oligonucleotide" (func $kh_init_oligonucleotide))
  (export "kh_destroy_oligonucleotide" (func $kh_destroy_oligonucleotide))
  (export "kh_clear_oligonucleotide" (func $kh_clear_oligonucleotide))
  (export "kh_get_oligonucleotide" (func $kh_get_oligonucleotide))
  (export "kh_resize_oligonucleotide" (func $kh_resize_oligonucleotide))
  (export "kh_put_oligonucleotide" (func $kh_put_oligonucleotide))
  (export "kh_del_oligonucleotide" (func $kh_del_oligonucleotide))
  (export "element_Compare" (func $element_Compare))
  (export "generate_Frequencies_For_Desired_Length_Oligonucleotides" (func $generate_Frequencies_For_Desired_Length_Oligonucleotides))
  (export "generate_Count_For_Oligonucleotide" (func $generate_Count_For_Oligonucleotide))
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
  (data $.rodata (i32.const 1024) " \00 \01\03  \02\00GGTATT\00GGTATTTTAATT\00GGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA\00")
  (@custom ".debug_loc" "\ff\ff\ff\ff\8a\00\00\00\12\00\00\002\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\8a\00\00\00\22\00\00\00$\00\00\00\04\00\ed\02\00\9f$\00\00\00\a8\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\8a\00\00\00-\00\00\00.\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\8a\00\00\00.\00\00\000\00\00\00\04\00\ed\02\00\9f0\00\00\002\00\00\00\04\00\ed\00\06\9f\8b\00\00\00\8d\00\00\00\04\00\ed\02\00\9f\8d\00\00\00\94\00\00\00\04\00\ed\00\07\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\8a\00\00\00.\00\00\000\00\00\00\04\00\ed\02\00\9f0\00\00\00\a8\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\00\00\00\00\16\00\00\00\04\00\ed\00\01\9f\16\00\00\00\18\00\00\00\04\00\ed\02\00\9f\18\00\00\00\1e\00\00\00\04\00\ed\00\01\9f\1e\00\00\00 \00\00\00\04\00\ed\02\00\9f \00\00\00&\00\00\00\04\00\ed\00\01\9f&\00\00\00(\00\00\00\04\00\ed\02\00\9f(\00\00\00.\00\00\00\04\00\ed\00\01\9f.\00\00\000\00\00\00\04\00\ed\02\00\9f0\00\00\006\00\00\00\04\00\ed\00\01\9f6\00\00\008\00\00\00\04\00\ed\02\00\9f8\00\00\00>\00\00\00\04\00\ed\00\01\9f>\00\00\00C\00\00\00\04\00\ed\02\00\9fC\00\00\00K\00\00\00\04\00\ed\00\01\9fK\00\00\00M\00\00\00\04\00\ed\02\00\9fM\00\00\00+\03\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\00\00\00\00\b7\00\00\00\02\000\9f\b7\00\00\00\b9\00\00\00\04\00\ed\02\00\9f\b9\00\00\00\16\01\00\00\04\00\ed\00\05\9f\1f\03\00\00+\03\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\00\00\00\00\16\01\00\00\02\001\9f\16\01\00\00 \01\00\00\02\000\9f\b7\02\00\00\b9\02\00\00\04\00\ed\02\00\9f\b9\02\00\00\f0\02\00\00\04\00\ed\00\09\9f\1f\03\00\00+\03\00\00\02\001\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\ee\00\00\00\f0\00\00\00\04\00\ed\02\00\9f\f0\00\00\00\0f\01\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\0a\01\00\00\0c\01\00\00\04\00\ed\02\00\9f\0c\01\00\00 \01\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00l\01\00\00\8e\01\00\00\04\00\ed\00\0d\9f\96\02\00\00\b0\02\00\00\04\00\ed\00\11\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\90\01\00\00\d2\01\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\a1\01\00\00\a2\01\00\00\04\00\ed\02\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\a2\01\00\00\a4\01\00\00\04\00\ed\02\01\9f\a4\01\00\00\d4\01\00\00\04\00\ed\00\01\9f\e9\01\00\00\eb\01\00\00\04\00\ed\02\01\9f\eb\01\00\00\b0\02\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00y\02\00\00\b0\02\00\00\04\00\ed\00\11\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff8\01\00\00\8f\02\00\00\b0\02\00\00\04\00\ed\00\13\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffe\04\00\00{\00\00\00}\00\00\00\04\00\ed\02\01\9f}\00\00\00d\01\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffe\04\00\00f\00\00\00\b6\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffe\04\00\00v\00\00\00x\00\00\00\04\00\ed\02\01\9fx\00\00\00\b6\00\00\00\04\00\ed\00\05\9f\12\01\00\00-\01\00\00\04\00\ed\00\08\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffe\04\00\00v\00\00\00x\00\00\00\04\00\ed\02\01\9fx\00\00\00-\01\00\00\04\00\ed\00\05\9f>\01\00\00R\01\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffe\04\00\00\86\00\00\00\87\00\00\00\04\00\ed\02\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffe\04\00\00\87\00\00\00\89\00\00\00\04\00\ed\02\01\9f\89\00\00\00\b6\00\00\00\04\00\ed\00\07\9f%\01\00\00'\01\00\00\04\00\ed\02\00\9f'\01\00\00-\01\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\8b\06\00\00\00\00\00\005\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\18\00\00\00u\00\00\00\02\000\9f\c0\00\00\00\c2\00\00\00\04\00\ed\02\01\9f\c2\00\00\00\c5\00\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\1f\00\00\00u\00\00\00\02\000\9f\a8\00\00\00\ab\00\00\00\04\00\ed\02\00\9f\b9\00\00\00\c5\00\00\00\04\00\ed\00\07\9f\e5\00\00\00\f5\00\00\00\04\00\ed\00\07\9fA\01\00\00C\01\00\00\04\00\ed\02\01\9fC\01\00\00\85\01\00\00\04\00\ed\00\07\9f\13\02\00\00\16\02\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00-\00\00\00(\03\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\00\00\00\00\ae\01\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\00\00\00\00/\01\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00=\00\00\00\98\02\00\00\04\00\ed\00\08\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\02\01\00\00\1d\01\00\00\02\000\9f\9b\01\00\00\9d\01\00\00\04\00\ed\02\00\9f\9d\01\00\00?\02\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\02\01\00\00\1d\01\00\00\02\000\9f\8f\01\00\00\d3\01\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\10\01\00\00\1d\01\00\00\04\00\ed\00\0b\9f\a8\01\00\00(\03\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\10\01\00\00\1d\01\00\00\02\000\9f\a8\01\00\00\d3\01\00\00\02\000\9f:\02\00\00<\02\00\00\04\00\ed\02\01\9f<\02\00\00?\02\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00P\01\00\00l\01\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\03\02\00\00\06\02\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00v\02\00\00\98\02\00\00\02\000\9f\0b\03\00\00\0d\03\00\00\04\00\ed\02\00\9f\0d\03\00\00\12\03\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\1a\07\00\00\c9\02\00\00\d9\02\00\00\04\00\ed\00\0c\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00\00\00\00\00}\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00 \00\00\00\22\00\00\00\04\00\ed\02\00\9f\22\00\00\00\b0\01\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00\18\00\00\00}\00\00\00\02\000\9f\c8\00\00\00\ca\00\00\00\04\00\ed\02\01\9f\ca\00\00\00\cd\00\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00'\00\00\00}\00\00\00\02\000\9f\b0\00\00\00\b3\00\00\00\04\00\ed\02\00\9f\c1\00\00\00\cd\00\00\00\04\00\ed\00\08\9f\ed\00\00\00\fd\00\00\00\04\00\ed\00\08\9f/\01\00\001\01\00\00\04\00\ed\02\01\9f1\01\00\00s\01\00\00\04\00\ed\00\08\9f~\01\00\00\b0\01\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\005\00\00\00\b0\01\00\00\04\00\ed\00\07\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00\00\00\00\00\b0\01\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00\00\00\00\00\1d\01\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00E\00\00\00\b0\01\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffD\0a\00\00>\01\00\00Z\01\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f6\0b\00\00D\00\00\00\9d\00\00\00\05\00\10\80\80@\9f\d4\00\00\00\d6\00\00\00\04\00\ed\02\01\9f\d6\00\00\00\de\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f6\0b\00\00D\00\00\00\9d\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f6\0b\00\00Q\00\00\00\9d\00\00\00\04\00\ed\00\01\9fE\01\00\00G\01\00\00\04\00\ed\02\00\9fG\01\00\00\d2\01\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13:\0b;\0b\1c\0f\00\00\03&\00I\13\00\00\04$\00\03\0e>\0b\0b\0b\00\00\054\00I\13:\0b;\0b\00\00\06\01\01I\13\00\00\07!\00I\137\0b\00\00\08$\00\03\0e\0b\0b>\0b\00\00\094\00I\13:\0b;\0b\02\18\00\00\0a\0f\00I\13\00\00\0b\16\00I\13\03\0e:\0b;\0b\00\00\0c\13\01\03\0e\0b\0b:\0b;\0b\00\00\0d\0d\00\03\0eI\13:\0b;\0b8\0b\00\00\0e\0f\00\00\00\0f\15\01I\13'\19\00\00\10\05\00I\13\00\00\11&\00\00\00\12.\01\11\01\12\06@\18\97B\191\13\00\00\13\89\82\01\001\13\11\01\00\00\14.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\15\05\00\02\181\13\00\00\16.\01\03\0e:\0b;\0b'\19<\19?\19\00\00\17.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\18\05\00\02\18\03\0e:\0b;\0bI\13\00\00\19.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19I\13?\19\00\00\1a\0b\01\11\01\12\06\00\00\1b4\00\02\17\03\0e:\0b;\0bI\13\00\00\1c\05\00\03\0e:\0b;\0bI\13\00\00\1d\05\00\02\17\03\0e:\0b;\0bI\13\00\00\1e\0b\01U\17\00\00\1f4\00\03\0e:\0b;\0bI\13\00\00 .\00\03\0e:\0b;\0b'\19I\13?\19 \0b\00\00!.\01\03\0e:\0b;\0b'\19?\19 \0b\00\00\22\1d\001\13\11\01\12\06X\0bY\0bW\0b\00\00#4\00\02\18\03\0e:\0b;\0bI\13\00\00$\1d\001\13U\17X\0bY\0bW\0b\00\00%4\00\1c\0d\03\0e:\0b;\0bI\13\00\00&4\00\03\0eI\134\19\00\00'.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00(\13\00\03\0e<\19\00\00)\13\01\0b\0b:\0b;\0b\00\00*!\00I\137\13\00\00+!\00I\137\05\00\00\00")
  (@custom ".debug_info" "\ad\0b\00\00\04\00\00\00\00\00\04\01\5c\04\00\00\1d\00\14\04\00\00\00\00\00\00\94\03\00\00\00\00\00\00\c0\00\00\00\027\04\00\00:\00\00\00\01\be\a4\e1\f5\d1\f0\fa\a8\f4?\03?\00\00\00\04v\02\00\00\04\08\05M\00\00\00\02s\06Y\00\00\00\07`\00\00\00\05\00\04\b5\01\00\00\06\01\08#\04\00\00\08\07\09t\00\00\00\02\a9\05\03\00\04\00\00\06Y\00\00\00\07`\00\00\00\09\00\09\8d\00\00\00\02\b8\05\034\04\00\00\06Y\00\00\00\07`\00\00\00\07\00\09\a6\00\00\00\02\d8\05\03!\04\00\00\06Y\00\00\00\07`\00\00\00\13\00\09\bf\00\00\00\02\db\05\03\10\04\00\00\06Y\00\00\00\07`\00\00\00\0d\00\09\8d\00\00\00\02\de\05\03\09\04\00\00\09M\00\00\00\02\e1\05\03;\04\00\00\09\f2\00\00\00\02\e4\05\03\1d\04\00\00\06Y\00\00\00\07`\00\00\00\04\00\0a\03\01\00\00\0b\0e\01\00\00\c9\00\00\00\02\14\0c\8b\01\00\00\1c\02\14\0d\0b\01\00\00k\01\00\00\02\14\00\0dB\02\00\00k\01\00\00\02\14\04\0d\89\03\00\00k\01\00\00\02\14\08\0dj\03\00\00k\01\00\00\02\14\0c\0d(\01\00\00\88\01\00\00\02\14\10\0d\02\01\00\00\8d\01\00\00\02\14\14\0d\1f\01\00\00\af\01\00\00\02\14\18\00\0bv\01\00\00\8c\00\00\00\01\a0\0b\81\01\00\00\f4\00\00\00\01\85\04L\00\00\00\07\04\0av\01\00\00\0a\92\01\00\00\0b\9d\01\00\00\e0\00\00\00\03A\0b\a8\01\00\00\de\00\00\00\03\1d\04!\02\00\00\07\08\0a\b4\01\00\00\0b\bf\01\00\00\eb\00\00\00\03@\0b\81\01\00\00\e9\00\00\00\03\17\0e\0a\d0\01\00\00\0f\e0\01\00\00\10\e7\01\00\00\10\e7\01\00\00\00\04U\00\00\00\05\04\0a\ec\01\00\00\11\12\05\00\00\00\0c\00\00\00\07\ed\03\00\00\00\00\9f\f6\05\00\00\13\0c\02\00\00\10\00\00\00\00\14\0d\04\00\00\03\cc\ca\01\00\00\10\22\02\00\00\10\22\02\00\00\00\0b-\02\00\00\b6\00\00\00\03\9c\044\02\00\00\07\04\12\12\00\00\003\00\00\00\07\ed\03\00\00\00\00\9f\02\06\00\00\15\04\ed\00\00\9f\0a\06\00\00\13x\02\00\00%\00\00\00\13x\02\00\000\00\00\00\13x\02\00\00;\00\00\00\13x\02\00\00C\00\00\00\00\16\88\02\00\00\03\ce\10\ca\01\00\00\00\17F\00\00\00B\00\00\00\07\ed\03\00\00\00\00\9f\fd\02\00\00\02\14\18\04\ed\00\00\9f\1f\02\00\00\02\14\fe\00\00\00\00\19\8a\00\00\00\ac\00\00\00\07\ed\03\00\00\00\00\9f\e6\02\00\00\02\14k\01\00\00\18\04\ed\00\00\9f\1f\02\00\00\02\14\e1\0a\00\00\18\04\ed\00\01\9f\18\00\00\00\02\14\92\01\00\00\1a\ab\00\00\00\87\00\00\00\1b\00\00\00\00\c5\01\00\00\02\14k\01\00\00\1b\1c\00\00\00\e9\01\00\00\02\14k\01\00\00\1bH\00\00\00\ec\01\00\00\02\14k\01\00\00\1bf\00\00\00\f2\01\00\00\02\14k\01\00\00\1b\ae\00\00\004\00\00\00\02\14k\01\00\00\00\00\198\01\00\00+\03\00\00\07\ed\03\00\00\00\00\9f-\03\00\00\02\14\e0\01\00\00\1c\1f\02\00\00\02\14\fe\00\00\00\1d\da\00\00\00\07\01\00\00\02\14k\01\00\00\1b\bc\01\00\00$\01\00\00\02\14\88\01\00\00\1b\02\02\00\00\ee\01\00\00\02\14k\01\00\00\1e\00\00\00\00\1bR\02\00\00\fe\00\00\00\02\14\8d\01\00\00\1a2\02\00\00\1c\00\00\00\1b~\02\00\00\1b\01\00\00\02\14\af\01\00\00\00\00\1e\18\00\00\00\1b\aa\02\00\00\e1\01\00\00\02\14\b4\01\00\00\1f\18\00\00\00\02\14\92\01\00\00\1f\e5\01\00\00\02\14k\01\00\00\1e8\00\00\00\1b\d6\02\00\00\c5\01\00\00\02\14k\01\00\00\1b\f2\02\00\00\ec\01\00\00\02\14k\01\00\00\1b\10\03\00\00\f2\01\00\00\02\14k\01\00\00\1a\a8\03\00\00\16\00\00\00\1bX\03\00\00\ba\01\00\00\02\14\b4\01\00\00\00\1a\be\03\00\00\10\00\00\00\1bv\03\00\00\ba\01\00\00\02\14\92\01\00\00\00\00\00\13\8f\04\00\00\ef\01\00\00\13\a0\04\00\00&\02\00\00\13\a0\04\00\00B\02\00\00\13\a0\04\00\00\10\04\00\00\13\a0\04\00\00%\04\00\00\13x\02\00\004\04\00\00\13x\02\00\00`\04\00\00\00\14\fe\03\00\00\03\cb\ca\01\00\00\10\22\02\00\00\00\14\05\04\00\00\03\cd\ca\01\00\00\10\ca\01\00\00\10-\02\00\00\00\19e\04\00\00%\02\00\00\07\ed\03\00\00\00\00\9f\b7\02\00\00\02\14k\01\00\00\18\04\ed\00\00\9f\1f\02\00\00\02\14\fe\00\00\00\18\04\ed\00\01\9f\18\00\00\00\02\14\92\01\00\00\18\04\ed\00\02\9f|\00\00\00\02\14\eb\0a\00\00\1b\16\04\00\00+\00\00\00\02\14k\01\00\00\1a\cb\04\00\00\fe\00\00\00\1b\94\03\00\00\e9\01\00\00\02\14k\01\00\00\1b\c0\03\00\00\c5\01\00\00\02\14k\01\00\00\1b\dc\03\00\00a\02\00\00\02\14k\01\00\00\1bP\04\00\00\ec\01\00\00\02\14k\01\00\00\1bn\04\00\00\f2\01\00\00\02\14k\01\00\00\1f4\00\00\00\02\14k\01\00\00\00\13>\03\00\00\98\04\00\00\13>\03\00\00\b8\04\00\00\00\17\8b\06\00\00T\00\00\00\07\ed\03\00\00\00\00\9f\16\03\00\00\02\14\18\04\ed\00\00\9f\1f\02\00\00\02\14\fe\00\00\00\1d\b6\04\00\00+\00\00\00\02\14k\01\00\00\00\19\e0\06\00\008\00\00\00\07\ed\03\00\00\00\00\9ff\02\00\00\02/\e0\01\00\00\18\04\ed\00\00\9fo\00\00\00\02/\f0\0a\00\00\18\04\ed\00\01\9fa\00\00\00\020\f0\0a\00\00\00 \ce\02\00\00\02\14\fe\00\00\00\01!\9c\02\00\00\02\14\01\1c\1f\02\00\00\02\14\fe\00\00\00\00\17\1a\07\00\00(\03\00\00\04\ed\00\04\9fR\01\00\00\02>\1d\b8\05\00\00\8d\02\00\00\02?G\0b\00\00\1d\9a\05\00\00\f4\01\00\00\02?'\0b\00\00\18\04\ed\00\02\9f.\01\00\00\02@'\0b\00\00\1c-\00\00\00\02@a\0b\00\00\1b\0c\05\00\00\18\00\00\00\02D\92\01\00\00\1b|\05\00\00\e9\01\00\00\02EB\0b\00\00\1b\d6\05\00\00}\02\00\00\02B\fe\00\00\00\1b\f4\05\00\00G\02\00\00\02a,\0b\00\00\1b,\06\00\00\f2\01\00\00\02a,\0b\00\00\1bT\06\00\00\1c\00\00\00\02bL\0b\00\00\1b\e2\06\00\00[\02\00\00\02c\b4\01\00\00\22\f6\05\00\00O\07\00\00\0e\00\00\00\02B(\1a]\07\00\00\b4\00\00\00\1b\d4\04\00\00\f2\01\00\00\02I,\0b\00\00\00\1eP\00\00\00\1f\f2\01\00\00\02O,\0b\00\00\1aQ\08\00\00K\00\00\00#\02\91\0cv\03\00\00\02T\e0\01\00\00\1b\c4\06\00\00\ec\01\00\00\02UQ\0b\00\00\00\00$\02\06\00\00h\00\00\00\02g\02\1e\88\00\00\00\1b\80\06\00\00\f0\01\00\00\02dk\01\00\00\00\1a\96\09\00\00\98\00\00\00%\00\ca\01\00\00\02n,\0b\00\00\1b\00\07\00\00\f2\01\00\00\02n,\0b\00\00\1a\c3\09\00\00a\00\00\00&P\04\00\00\81\01\00\00\1f7\03\00\00\02qf\0b\00\00\1a\c3\09\00\00a\00\00\00\1b8\07\00\00\ee\01\00\00\02r,\0b\00\00\00\00\00\13\0c\02\00\00U\07\00\00\13\8f\04\00\00(\08\00\00\13\b6\04\00\00h\08\00\00\13\8f\04\00\00\c0\08\00\00\13x\02\00\00f\09\00\00\13x\02\00\00n\09\00\00\13x\02\00\00v\09\00\00\13x\02\00\00~\09\00\00\13\05\08\00\00\90\09\00\00\13x\02\00\006\0a\00\00\00\169\00\00\00\03\fa\10\ca\01\00\00\10\22\02\00\00\10\22\02\00\00\10!\08\00\00\00\0b\cb\01\00\00\a6\00\00\00\03\f9\17D\0a\00\00\b0\01\00\00\04\ed\00\04\9fG\03\00\00\02\85\1d\82\08\00\00\8d\02\00\00\02\86G\0b\00\00\1dd\08\00\00\f4\01\00\00\02\86'\0b\00\00\1dV\07\00\007\03\00\00\02\87G\0b\00\00\1c-\00\00\00\02\87a\0b\00\00\1bt\07\00\00\0a\02\00\00\02\88'\0b\00\00\1b\d8\07\00\00\18\00\00\00\02\8c\92\01\00\00\1bF\08\00\00\e9\01\00\00\02\8dB\0b\00\00\1b\a0\08\00\00}\02\00\00\02\8au\0b\00\00\1f\ec\01\00\00\02\acV\0b\00\00\1fF\00\00\00\02\adz\0b\00\00\22\f6\05\00\00\81\0a\00\00\0e\00\00\00\02\8a.\1a\8f\0a\00\00\b8\00\00\00\1b\a0\07\00\00\f2\01\00\00\02\91,\0b\00\00\00\1aG\0b\00\00y\00\00\00\1f\f2\01\00\00\02\96,\0b\00\00\1ai\0b\00\00K\00\00\00#\02\91\0cv\03\00\00\02\9a\e0\01\00\00\1b\be\08\00\00\ec\01\00\00\02\9bQ\0b\00\00\00\00\22\02\06\00\00\c2\0b\00\00&\00\00\00\02\b0\02\13\7f\09\00\00d\0a\00\00\13\0c\02\00\00\87\0a\00\00\13\b6\04\00\00\80\0b\00\00\13x\02\00\00\cd\0b\00\00\13x\02\00\00\d8\0b\00\00\13x\02\00\00\e0\0b\00\00\13x\02\00\00\e8\0b\00\00\00\14\da\01\00\00\03\db-\02\00\00\10\90\09\00\00\00\0a\95\09\00\00\03Y\00\00\00'\f6\0b\00\00\d2\01\00\00\04\ed\00\00\9f?\00\00\00\02\b4\e0\01\00\00#\02\91\00\a0\01\00\00\02\b5\90\0b\00\00\1b\dc\08\00\00\00\00\00\00\02\bd,\0b\00\00\1b\17\09\00\00\f4\01\00\00\02\be,\0b\00\00\1b3\09\00\00\8d\02\00\00\02\bf\ac\0a\00\00\1f\a7\01\00\00\02\d1\9d\0b\00\00\1e\a8\00\00\00\1f\f2\01\00\00\02\c3,\0b\00\00\00\13\91\0a\00\00#\0c\00\00\13\c6\0a\00\006\0c\00\00\13\8f\04\00\00E\0c\00\00\13\91\0a\00\00_\0c\00\00\13\a0\04\00\00\d2\0c\00\00\13\91\0a\00\00\e9\0c\00\00\13\a0\04\00\00;\0d\00\00\13,\08\00\00M\0d\00\00\13,\08\00\00_\0d\00\00\13,\08\00\00q\0d\00\00\13,\08\00\00\83\0d\00\00\13,\08\00\00\95\0d\00\00\13\16\06\00\00\a3\0d\00\00\13\16\06\00\00\b1\0d\00\00\13x\02\00\00\b9\0d\00\00\00\14\15\01\00\00\03\ef\ac\0a\00\00\10\ac\0a\00\00\10\e0\01\00\00\10\b1\0a\00\00\00\0aY\00\00\00\0a\b6\0a\00\00\0b\c1\0a\00\00K\04\00\00\03\ea(G\04\00\00\14\be\01\00\00\03\da\e0\01\00\00\10\e7\01\00\00\10\e7\01\00\00\10\22\02\00\00\00\0a\e6\0a\00\00\03\03\01\00\00\0a\e0\01\00\00\03\f5\0a\00\00\0a\fa\0a\00\00\03\ff\0a\00\00\0b\0a\0b\00\00Y\00\00\00\02\1d)\10\02\1a\0d\18\00\00\00\92\01\00\00\02\1b\00\0d[\02\00\00\b4\01\00\00\02\1c\08\00\03,\0b\00\00\0b7\0b\00\00\bd\00\00\00\02\18\0b\e0\01\00\00\94\00\00\00\03p\03\92\01\00\00\03\90\09\00\00\0a\ff\0a\00\00\03V\0b\00\00\0bk\01\00\00\9d\00\00\00\01\a1\03\ac\0a\00\00\06Y\00\00\00*`\00\00\00{\07\00\00\00\03\fe\00\00\00\0b\85\0b\00\00\82\00\00\00\03x\0b\a8\01\00\00\80\00\00\00\036\06Y\00\00\00+`\00\00\00\00\10\00\06Y\00\00\00\07`\00\00\00\07+`\00\00\00\00\10\00\00")
  (@custom ".debug_ranges" "\16\02\00\00N\02\00\00X\04\00\00b\04\00\00\00\00\00\00\00\00\00\00\92\02\00\00\ae\02\00\00\af\02\00\00\98\03\00\00\a8\03\00\00\e8\03\00\00\00\00\00\00\00\00\00\00\d4\02\00\00\98\03\00\00\a8\03\00\00\e8\03\00\00\00\00\00\00\00\00\00\00\11\08\00\00\1c\08\00\008\08\00\00\b0\08\00\00\00\00\00\00\00\00\00\00.\08\00\005\08\00\00\c8\08\00\00\cf\08\00\00[\09\00\00\84\09\00\00\00\00\00\00\00\00\00\005\08\00\007\08\00\00\c2\08\00\00\c8\08\00\00\cf\08\00\00[\09\00\00\00\00\00\00\00\00\00\00\a6\0c\00\00\b5\0c\00\00\ff\0c\00\00.\0d\00\00\00\00\00\00\00\00\00\00\05\00\00\00\11\00\00\00\12\00\00\00E\00\00\00F\00\00\00\88\00\00\00\8a\00\00\006\01\00\008\01\00\00c\04\00\00e\04\00\00\8a\06\00\00\8b\06\00\00\df\06\00\00\e0\06\00\00\18\07\00\00\1a\07\00\00B\0a\00\00D\0a\00\00\f4\0b\00\00\f6\0b\00\00\c8\0d\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "polynucleotide_Capacity\00key\00elements_Array\00x\00output\00last\00qsort\00_start\00count\00unsigned int\00element\00right_Element\00left_Element\00ret\00__uintmax_t\00khint_t\00intptr_t\00khiter_t\00comparison_fn_t\00size_t\00intnative_t\00kh_oligonucleotide_t\00__uint64_t\00__uint32_t\00khint32_t\00new_keys\00new_n_buckets\00fgets\00new_vals\00new_flags\00desired_Length_For_Oligonucleotides\00generate_Frequencies_For_Desired_Length_Oligonucleotides\00kh_oligonucleotide_s\00buffer\00output_Buffer\00char\00tmp\00memcmp\00step\00output_Position\00strlen\00val\00new_mask\00j\00__i\00polynucleotide_Length\00oligonucleotide_Length\00unsigned long long\00unsigned long\00size\00elements_Array_Size\00value\00site\00element_Compare\00double\00hash_Table\00free\00polynucleotide\00kh_destroy_oligonucleotide\00kh_put_oligonucleotide\00kh_init_oligonucleotide\00kh_get_oligonucleotide\00kh_clear_oligonucleotide\00kh_del_oligonucleotide\00kh_resize_oligonucleotide\00generate_Count_For_Oligonucleotide\00upper_bound\00element_Was_Unused\00n_occupied\00/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00malloc\00realloc\00calloc\00k-nucleotide.c\00__ARRAY_SIZE_TYPE__\00__ac_HASH_UPPER\00_IO_FILE\00__vla_expr0\00clang version 19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)\00")
  (@custom ".debug_line" "i\07\00\00\04\00G\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01.\00./../..\00\00khash.h\00\01\00\00k-nucleotide.c\00\00\00\00stdlib.h\00\02\00\00\00\04\02\00\05\02\05\00\00\00\03\13\01\05\01\0aX\02\07\00\01\01\04\02\05\01\0a\00\05\02\13\00\00\00\03\13\01\06t\03l\02)\01\03\14 \02\01\00\01\01\04\02\05\01\0a\00\05\02I\00\00\00\03\13\01\06\ba\03lX\03\14t\03l\02%\01\03\14 \02\01\00\01\01\04\02\05\01\0a\00\05\02\8d\00\00\00\03\13\01\06t\03lJ\03\14.\03l \03\14\f2\03l \03\14f\03lf\03\14\f2\03l\08\ba\03\14f\03l\08X\03\14 \03lt\03\14J\82XJ\03l\08<\03\14 \02\03\00\01\01\04\02\00\05\028\01\00\00\03\13\01\05\01\0a\08J\06\03l \03\14J\03lJ\03\14J\03lJ\03\14J\03lJ\03\14J\03lJ\03\14J\03lJ\03\14.\03l \03\14\82.\03l\02:\01\03\14 \03lJ\03\14 \ba\03l.\03\14\82\03l\08\90\03\14.\03l \03\14f\9e\d6.\08\ac\08\acX\03lX\03\14\08\ba\08\9e.\03l\08 \03\14\9e \03l\08f\03\14\d6\03lf\03\14J\03l\08\c8\03\14J\03lX\03\14X\03l\08f\03\14J\03l\02&\01\03\14\90X\03l\d6\03\14f\c8\03l<\03\14t\c8t\03l\90\03\14t\08X\f2\03l\08\90\03\14ff<X.\08J\03l\08J\03\14 \03l\02*\01\03\14<\03l \03\14 \03l\82\03\14.\02\01\00\01\01\04\02\05\01\0a\00\05\02h\04\00\00\03\13\01\06\c8.\08 \03l.\03\14f\03lt\03\14. \03l.\03\14J\82\03l \03\14t\03lt\03\14. \03l.\03\14J\82\03l \03\14 \90\03lt\03\14J\03l \03\14f\03lf\03\14J\08J\03l<\03\14J\03l.\03\14\08f\03l\02&\01\03\14f\03l\08X\03\14 \ba\03l\9e\03\14J\82X\03l<\03\14\82X\03lJ\03\14 \03l\08.\03\14 \03l\08\12\03\14X\02(\12\03l<\03\14f\02;\12\03l<\03\14t \03l<\03\14f\02.\12\03l<\03\14X<\02\03\00\01\01\04\02\05\01\0a\00\05\02\8e\06\00\00\03\13\01\06\020\12\03l.\03\14\9e \03l\08 \03\14 \02\01\00\01\01\04\02\05\13\0a\00\05\02\e3\06\00\00\032\01\05*\06t\05\19t\05\05<\03M.\05\01\06\039.\06\03G \05\19\06\034 \05\05\06t\03L.\05\01\06\039.\06\03G \05\17\06\038X\05,\06X\05\1bX\05\09 \05\01\06!\02\01\00\01\01\04\02\00\05\02\1a\07\00\00\03?\01\05%\0a\02%\17\05\22\06 \05J \03\bb\7ff\05\01\06\03\14\82\06\03l\82\05\18\06\03\c9\00f\05\02\06 \03\b7\7f.\03\c9\00J\03\b7\7f\08\12\03\c9\00\e4\03\b7\7f\ac\05\0b\06\03\ca\00f\05\0f\06 \05\19<\05\17\9e\05\0b<\05\0f \05\19t\05\17J\05\0b<\05\0f \05\19t\05\17J\03\b6\7f \05\0b\03\ca\00.\05\0f \05\19t\05\17J\03\b6\7f<\05A\06\03\c9\00f\05\02\06 \03\b7\7fX\03\c9\00.\03\b7\7f\ba\05\0b\06\03\ca\00f\05\0f\06 \05\19<\05\17X\03\b6\7f<\05\02\06\03\c9\00J\05\07\dd\05\02\8f\06\03\b1\7f.\05\1b\06\03\e2\00f\06\03\9e\7f\82\05\01\06\03\14J\05\02\03\d0\00t\06\03\9c\7f.\06\03\cf\00 \06\03\b1\7f\08\12\05\0b\06\03\d2\00\82\05\0f\06 \05\19<\05\17X\05\14\06#\05\00\06\03\ab\7f\e4\05\06\06\03\da\00\08.\06t\05\1b\061\06\03\a3\7f\9e\05\07\06\03\d0\00\ba\06\03\b0\7f<\03\d0\00J\05\02\06\1f\06\03\b1\7fX\05\22\06\03\e1\00t\06\03\9f\7fX\055\06\03\e2\00J\05\1b\06 \05\02\06\84\05\01\03\b0\7ff\05\02\03\d0\00t\06\03\9c\7f.\03\e4\00\02&\01\08J\03\9c\7f.\03\e4\00f\03\9c\7f\08\ac\03\e4\00J\03\9c\7f<\03\e4\00X\08\9e\05\01\06\03\b0\7fJ\06\03l\02#\01\05\02\06\03\ea\00f\06\03\96\7f\ba\05+\06\03\ee\00f\05\02\06 \03\92\7f.\03\ee\00J\03\92\7f<\05\03\06\03\f2\00\02#\01\06\03\8e\7f\82\03\f2\00J\03\8e\7f.\05C\03\f2\00\e4\03\8e\7f<\05\19\06\03\f4\00J\05\03r\06\03\8e\7f\c8\03\f2\00J\03\8e\7f<\05\19\06\03\f4\00f\06\03\8c\7f<\05=\06\03\f2\00J\05\03\06f\05\19\06L\06\03\8c\7ft\05B\06\03\ee\00J\05+\06 \05\02X\06\03\11J\05\01\83\02\0c\00\01\01\04\02\00\05\02D\0a\00\00\03\86\01\01\05+\0a\08u\06\03\f8~\82\05%\06\03\8d\01\c8\05\22\06 \05= \03\f3~f\05\01\06\03\14\82\06\03l\82\05\18\06\03\91\01f\05\02\06 \03\ef~.\03\91\01J\03\ef~\08\12\03\91\01\e4\03\ef~\ac\05\0b\06\03\92\01f\05\0f\06 \05\19<\05\17\9e\05\0b<\05\0f \05\19t\05\17J\05\0b<\05\0f \05\19t\05\17J\03\ee~ \05\0b\03\92\01.\05\0f \05\19t\05\17J\03\ee~<\054\06\03\91\01f\05\02\06 \03\ef~X\03\91\01.\03\ef~\ba\05\0b\06\03\92\01f\05\0f\06 \05\19<\05\17X\03\ee~<\05\02\06\03\91\01J\06\03\ef~\d6\05/\06\03\96\01J\05\02\06t\03\ea~\08.\05\0b\06\03\98\01\82\05\0f\06 \05\19<\05\17X\05\14\06#\05\00\06\03\e5~\e4\05\06\06\03\a0\01\08.\05\1b\93\06\03\dd~\9e\05/\06\03\96\01\ba\06\03\ea~<\03\96\01J\03\ea~X\05\01\06\03\14.\03\9d\01\02&\01\02\0c\00\01\01\04\02\00\05\02\f6\0b\00\00\03\b3\01\01\05&\0a\08\e8\05\08\06t\05-f\03\c8~<\050\03\b8\01\9e\05\02f\03\c8~J\05\18\06\03\bf\01X\06\03\c1~\82\05&\06\03\c2\01\90\05\08\06\90\05\02f\03\be~.\03\c2\01J\03\be~.\03\c2\01\f2\03\be~<\03\c2\01J\03\be~.\03\c2\01X\03\be~\d6\05\03\06\03\c3\01\08.\06\03\bd~\e4\05\1d\06\03\ca\01 \053\06\9e\05\06 \03\b6~.\05B\06\03\cb\01f\05\13\06 \03\b5~\9e\05&\06\03\c2\01\82\05\08\06t\03\be~f\05\02\03\c2\01\f2\03\be~X\05\05\06\03\c5\01 \05\08\91\05\07\06\ac\05)\06s\06\03\bb~<\05\18\06\03\c3\01 \05\03\06\ac\03\bd~X\05\11\06\03\cf\01<\05\03\a6\08#\08#\08#\08#\06\03\9d~\08 \06\03\e7\01f\06\03\99~\82\06\03\ea\01f\05\02\89\84\02\0f\00\01\01")
  (@custom "name" "\00\12\11k-nucleotide.wasm\01\82\03\15\00\06calloc\01\04free\02\06memset\03\06malloc\04\07realloc\05\05qsort\06\06strlen\07\05fgets\08\06memcmp\09\11__wasm_call_ctors\0a\17kh_init_oligonucleotide\0b\1akh_destroy_oligonucleotide\0c\18kh_clear_oligonucleotide\0d\16kh_get_oligonucleotide\0e\19kh_resize_oligonucleotide\0f\16kh_put_oligonucleotide\10\16kh_del_oligonucleotide\11\0felement_Compare\128generate_Frequencies_For_Desired_Length_Oligonucleotides\13\22generate_Count_For_Oligonucleotide\14\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clangZ19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)")
  (@custom "target_features" "\04+\0amultivalue+\0fmutable-globals+\0freference-types+\08sign-ext"))
