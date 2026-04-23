(module $k-nucleotide.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (param i32) (result i32)))
  (type $t3 (func (param i32 i32 i32 i32)))
  (type $t4 (func (param i32 i32 i32) (result i32)))
  (type $t5 (func (param i32 i64 i32) (result i32)))
  (type $t6 (func (result i32)))
  (import "env" "calloc" (func $calloc (type $t0)))
  (import "env" "free" (func $free (type $t1)))
  (import "env" "malloc" (func $malloc (type $t2)))
  (import "env" "realloc" (func $realloc (type $t0)))
  (import "env" "qsort" (func $qsort (type $t3)))
  (import "env" "strlen" (func $strlen (type $t2)))
  (import "env" "fgets" (func $fgets (type $t4)))
  (import "env" "memcmp" (func $memcmp (type $t4)))
  (func $kh_resize_oligonucleotide (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i64) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i64)
    block $B0
      block $B1
        local.get $p0
        i32.load offset=4
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
        i32.trunc_sat_f64_u
        local.tee $l3
        i32.ge_u
        br_if $B1
        block $B2
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
          local.tee $l4
          br_if $B2
          i32.const -1
          return
        end
        block $B3
          local.get $p1
          i32.eqz
          br_if $B3
          local.get $l4
          i32.const 170
          local.get $p1
          memory.fill
        end
        block $B4
          block $B5
            local.get $p0
            i32.load
            local.tee $p1
            local.get $l2
            i32.ge_u
            br_if $B5
            local.get $p0
            i32.load offset=20
            local.get $l2
            i32.const 3
            i32.shl
            call $realloc
            local.tee $p1
            i32.eqz
            br_if $B0
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
            br_if $B0
            local.get $p0
            local.get $p1
            i32.store offset=24
            local.get $p0
            i32.load
            local.tee $p1
            i32.eqz
            br_if $B4
          end
          local.get $l2
          i32.const -1
          i32.add
          local.set $l5
          local.get $p0
          i32.load offset=16
          local.set $l6
          i32.const 0
          local.set $l7
          loop $L6
            block $B7
              local.get $l6
              local.get $l7
              i32.const 2
              i32.shr_u
              i32.const 1073741820
              i32.and
              i32.add
              local.tee $l8
              i32.load
              local.tee $l9
              local.get $l7
              i32.const 1
              i32.shl
              local.tee $l10
              i32.shr_u
              i32.const 3
              i32.and
              br_if $B7
              local.get $p0
              i32.load offset=24
              local.tee $l11
              local.get $l7
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.set $l12
              local.get $l8
              local.get $l9
              i32.const 1
              local.get $l10
              i32.const 30
              i32.and
              i32.shl
              i32.or
              i32.store
              local.get $p0
              i32.load offset=20
              local.tee $l13
              local.get $l7
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.set $l14
              loop $L8
                block $B9
                  block $B10
                    i32.const 2
                    local.get $l5
                    local.get $l14
                    i64.const 7
                    i64.shr_u
                    local.get $l14
                    i64.xor
                    i32.wrap_i64
                    i32.and
                    local.tee $p1
                    i32.const 1
                    i32.shl
                    local.tee $l8
                    i32.shl
                    local.tee $l9
                    local.get $l4
                    local.get $p1
                    i32.const 4
                    i32.shr_u
                    local.tee $l15
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l10
                    i32.load
                    local.tee $l16
                    i32.and
                    i32.eqz
                    br_if $B10
                    local.get $l8
                    i32.const 30
                    i32.and
                    local.set $l8
                    br $B9
                  end
                  i32.const 1
                  local.set $l8
                  loop $L11
                    local.get $p1
                    local.get $l8
                    i32.add
                    local.set $p1
                    local.get $l8
                    i32.const 1
                    i32.add
                    local.set $l8
                    i32.const 2
                    local.get $p1
                    local.get $l5
                    i32.and
                    local.tee $p1
                    i32.const 1
                    i32.shl
                    local.tee $l17
                    i32.shl
                    local.tee $l9
                    local.get $l4
                    local.get $p1
                    i32.const 4
                    i32.shr_u
                    local.tee $l15
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l10
                    i32.load
                    local.tee $l16
                    i32.and
                    i32.eqz
                    br_if $L11
                  end
                  local.get $l17
                  i32.const 30
                  i32.and
                  local.set $l8
                end
                local.get $l10
                local.get $l16
                local.get $l9
                i32.const -1
                i32.xor
                i32.and
                i32.store
                block $B12
                  block $B13
                    local.get $p1
                    local.get $p0
                    i32.load
                    i32.ge_u
                    br_if $B13
                    local.get $l6
                    local.get $l15
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l9
                    i32.load
                    local.get $l8
                    i32.shr_u
                    i32.const 3
                    i32.and
                    i32.eqz
                    br_if $B12
                  end
                  local.get $l11
                  local.get $p1
                  i32.const 2
                  i32.shl
                  i32.add
                  local.get $l12
                  i32.store
                  local.get $l13
                  local.get $p1
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get $l14
                  i64.store
                  local.get $p0
                  i32.load
                  local.set $p1
                  br $B7
                end
                local.get $l11
                local.get $p1
                i32.const 2
                i32.shl
                i32.add
                local.tee $l10
                i32.load
                local.set $l16
                local.get $l10
                local.get $l12
                i32.store
                local.get $l13
                local.get $p1
                i32.const 3
                i32.shl
                i32.add
                local.tee $p1
                i64.load
                local.set $l18
                local.get $p1
                local.get $l14
                i64.store
                local.get $l9
                local.get $l9
                i32.load
                i32.const 1
                local.get $l8
                i32.shl
                i32.or
                i32.store
                local.get $l18
                local.set $l14
                local.get $l16
                local.set $l12
                br $L8
              end
            end
            local.get $l7
            i32.const 1
            i32.add
            local.tee $l7
            local.get $p1
            i32.ne
            br_if $L6
          end
          local.get $p1
          local.get $l2
          i32.le_u
          br_if $B4
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
        local.get $l4
        i32.store offset=16
        local.get $p0
        local.get $l3
        i32.store offset=12
        local.get $p0
        local.get $p0
        i32.load offset=4
        i32.store offset=8
      end
      i32.const 0
      return
    end
    local.get $l4
    call $free
    i32.const -1)
  (func $kh_put_oligonucleotide (type $t5) (param $p0 i32) (param $p1 i64) (param $p2 i32) (result i32)
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
  (func $generate_Frequencies_For_Desired_Length_Oligonucleotides (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
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
          local.get $l8
          i32.load offset=20
          local.get $l9
          i32.add
          i64.load
          local.set $l7
          local.get $l17
          i32.const 0
          i32.store offset=12
          local.get $l17
          local.get $l7
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
  (func $generate_Count_For_Oligonucleotide (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
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
  (table $T0 2 2 funcref)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66624))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (elem $e0 (i32.const 1) func $element_Compare)
  (data $.rodata (i32.const 1024) " \00 \01\03  \02\00GGTATT\00GGTATTTTAATT\00GGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA\00")
  (@custom "name" "\00\12\11k-nucleotide.wasm\01\e8\01\0e\00\06calloc\01\04free\02\06malloc\03\07realloc\04\05qsort\05\06strlen\06\05fgets\07\06memcmp\08\19kh_resize_oligonucleotide\09\16kh_put_oligonucleotide\0a\0felement_Compare\0b8generate_Frequencies_For_Desired_Length_Oligonucleotides\0c\22generate_Count_For_Oligonucleotide\0d\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
