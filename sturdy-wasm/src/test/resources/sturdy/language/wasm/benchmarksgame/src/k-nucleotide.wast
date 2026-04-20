(module
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (param i32 i32 i32) (result i32)))
  (type $t3 (func (param i32 i32 i32 i32)))
  (type $t4 (func (param i32) (result i32)))
  (type $t5 (func (result i32)))
  (type $t6 (func))
  (type $t7 (func (param i32 i64) (result i32)))
  (type $t8 (func (param i32 i64 i32) (result i32)))
  (type $t9 (func (param i32 i32)))
  (import "env" "calloc" (func $env.calloc (type $t0)))
  (import "env" "free" (func $env.free (type $t1)))
  (import "env" "memset" (func $env.memset (type $t2)))
  (import "env" "malloc" (func $env.malloc (type $t4)))
  (import "env" "realloc" (func $env.realloc (type $t0)))
  (import "env" "qsort" (func $env.qsort (type $t3)))
  (import "env" "strlen" (func $env.strlen (type $t4)))
  (import "env" "fgets" (func $env.fgets (type $t2)))
  (import "env" "memcmp" (func $env.memcmp (type $t2)))
  (func $__wasm_call_ctors (type $t6))
  (func $kh_init_oligonucleotide (type $t5) (result i32)
    i32.const 1
    i32.const 28
    call $env.calloc)
  (func $kh_destroy_oligonucleotide (type $t1) (param $p0 i32)
    local.get $p0
    if $I0
      local.get $p0
      i32.load offset=20
      call $env.free
      local.get $p0
      i32.load offset=16
      call $env.free
      local.get $p0
      i32.load offset=24
      call $env.free
      local.get $p0
      call $env.free
    end)
  (func $kh_clear_oligonucleotide (type $t1) (param $p0 i32)
    (local $l1 i32)
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
      local.tee $l1
      i32.const 2
      i32.shr_u
      i32.const 1073741820
      i32.and
      local.get $l1
      i32.const 16
      i32.lt_u
      select
      call $env.memset
      drop
      local.get $p0
      i64.const 0
      i64.store offset=4 align=4
    end)
  (func $kh_get_oligonucleotide (type $t7) (param $p0 i32) (param $p1 i64) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32)
    local.get $p0
    i32.load
    local.tee $l3
    i32.eqz
    if $I0
      i32.const 0
      return
    end
    local.get $p0
    i32.load offset=16
    local.set $l5
    i32.const 1
    local.set $l4
    local.get $l3
    i32.const 1
    i32.sub
    local.tee $l6
    local.get $p1
    i64.const 7
    i64.shr_u
    local.get $p1
    i64.xor
    i32.wrap_i64
    i32.and
    local.tee $l7
    local.set $l2
    block $B1
      loop $L2
        block $B3
          local.get $l5
          local.get $l2
          i32.const 2
          i32.shr_u
          i32.const 1073741820
          i32.and
          i32.add
          i32.load
          local.tee $l8
          local.get $l2
          i32.const 1
          i32.shl
          local.tee $l9
          i32.shr_u
          local.tee $l10
          i32.const 2
          i32.and
          br_if $B3
          local.get $l10
          i32.const 1
          i32.and
          i32.eqz
          if $I4
            local.get $p0
            i32.load offset=20
            local.get $l2
            i32.const 3
            i32.shl
            i32.add
            i64.load
            local.get $p1
            i64.eq
            br_if $B3
          end
          local.get $l2
          local.get $l4
          i32.add
          local.get $l4
          i32.const 1
          i32.add
          local.set $l4
          local.get $l6
          i32.and
          local.tee $l2
          local.get $l7
          i32.ne
          br_if $L2
          br $B1
        end
      end
      local.get $l3
      local.get $l2
      local.get $l8
      local.get $l9
      i32.const 30
      i32.and
      i32.shr_u
      i32.const 3
      i32.and
      select
      local.set $l3
    end
    local.get $l3)
  (func $kh_resize_oligonucleotide (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i64) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 f64) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i64)
    block $B0
      block $B1 (result i32)
        i32.const 4
        local.get $p1
        i32.const 1
        i32.sub
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
        local.get $p1
        i32.const 4
        i32.le_u
        select
        local.tee $l3
        f64.convert_i32_u
        f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)
        f64.mul
        f64.const 0x1p-1 (;=0.5;)
        f64.add
        local.tee $l12
        f64.const 0x1p+32 (;=4.29497e+09;)
        f64.lt
        local.get $l12
        f64.const 0x0p+0 (;=0;)
        f64.ge
        i32.and
        if $I2
          local.get $l12
          i32.trunc_f64_u
          br $B1
        end
        i32.const 0
      end
      local.tee $l17
      local.get $p0
      i32.load offset=4
      i32.gt_u
      if $I3
        i32.const 4
        local.get $l3
        i32.const 2
        i32.shr_u
        i32.const 1073741820
        i32.and
        local.get $p1
        i32.const 16
        i32.lt_u
        select
        local.tee $p1
        call $env.malloc
        local.tee $l6
        i32.eqz
        if $I4
          i32.const -1
          return
        end
        local.get $l6
        i32.const 170
        local.get $p1
        call $env.memset
        local.set $l9
        block $B5
          local.get $l3
          local.get $p0
          i32.load
          local.tee $p1
          i32.gt_u
          if $I6
            local.get $p0
            i32.load offset=20
            local.get $l3
            i32.const 3
            i32.shl
            call $env.realloc
            local.tee $p1
            i32.eqz
            br_if $B0
            local.get $p0
            local.get $p1
            i32.store offset=20
            local.get $p0
            i32.load offset=24
            local.get $l3
            i32.const 2
            i32.shl
            call $env.realloc
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
            br_if $B5
          end
          local.get $l3
          i32.const 1
          i32.sub
          local.set $l13
          local.get $p0
          i32.load offset=16
          local.set $l14
          loop $L7
            block $B8
              local.get $l14
              local.get $l4
              i32.const 2
              i32.shr_u
              i32.const 1073741820
              i32.and
              i32.add
              local.tee $l5
              i32.load
              local.tee $l2
              local.get $l4
              i32.const 1
              i32.shl
              local.tee $l10
              i32.shr_u
              i32.const 3
              i32.and
              br_if $B8
              local.get $p0
              i32.load offset=24
              local.tee $l15
              local.get $l4
              i32.const 2
              i32.shl
              i32.add
              i32.load
              local.set $l6
              local.get $l5
              local.get $l2
              i32.const 1
              local.get $l10
              i32.const 30
              i32.and
              i32.shl
              i32.or
              i32.store
              local.get $p0
              i32.load offset=20
              local.tee $l10
              local.get $l4
              i32.const 3
              i32.shl
              i32.add
              i64.load
              local.set $l7
              loop $L9
                block $B10 (result i32)
                  i32.const 2
                  local.get $l13
                  local.get $l7
                  i64.const 7
                  i64.shr_u
                  local.get $l7
                  i64.xor
                  i32.wrap_i64
                  i32.and
                  local.tee $p1
                  i32.const 1
                  i32.shl
                  local.tee $l2
                  i32.shl
                  local.tee $l5
                  local.get $l9
                  local.get $p1
                  i32.const 4
                  i32.shr_u
                  local.tee $l8
                  i32.const 2
                  i32.shl
                  i32.add
                  local.tee $l11
                  i32.load
                  local.tee $l16
                  i32.and
                  if $I11
                    local.get $l2
                    i32.const 30
                    i32.and
                    br $B10
                  end
                  i32.const 1
                  local.set $l2
                  loop $L12
                    local.get $p1
                    local.get $l2
                    i32.add
                    local.set $p1
                    local.get $l2
                    i32.const 1
                    i32.add
                    local.set $l2
                    i32.const 2
                    local.get $p1
                    local.get $l13
                    i32.and
                    local.tee $p1
                    i32.const 1
                    i32.shl
                    local.tee $l18
                    i32.shl
                    local.tee $l5
                    local.get $l9
                    local.get $p1
                    i32.const 4
                    i32.shr_u
                    local.tee $l8
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l11
                    i32.load
                    local.tee $l16
                    i32.and
                    i32.eqz
                    br_if $L12
                  end
                  local.get $l18
                  i32.const 30
                  i32.and
                end
                local.set $l2
                local.get $l11
                local.get $l16
                local.get $l5
                i32.const -1
                i32.xor
                i32.and
                i32.store
                block $B13
                  local.get $p0
                  i32.load
                  local.get $p1
                  i32.gt_u
                  if $I14
                    local.get $l14
                    local.get $l8
                    i32.const 2
                    i32.shl
                    i32.add
                    local.tee $l8
                    i32.load
                    local.get $l2
                    i32.shr_u
                    i32.const 3
                    i32.and
                    i32.eqz
                    br_if $B13
                  end
                  local.get $l15
                  local.get $p1
                  i32.const 2
                  i32.shl
                  i32.add
                  local.get $l6
                  i32.store
                  local.get $l10
                  local.get $p1
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get $l7
                  i64.store
                  local.get $p0
                  i32.load
                  local.set $p1
                  br $B8
                end
                local.get $l15
                local.get $p1
                i32.const 2
                i32.shl
                i32.add
                local.tee $l11
                i32.load
                local.get $l11
                local.get $l6
                i32.store
                local.get $l10
                local.get $p1
                i32.const 3
                i32.shl
                i32.add
                local.tee $p1
                i64.load
                local.get $p1
                local.get $l7
                i64.store
                local.get $l8
                local.get $l8
                i32.load
                i32.const 1
                local.get $l2
                i32.shl
                i32.or
                i32.store
                local.set $l7
                local.set $l6
                br $L9
              end
              unreachable
            end
            local.get $l4
            i32.const 1
            i32.add
            local.tee $l4
            local.get $p1
            i32.ne
            br_if $L7
          end
          local.get $p1
          local.get $l3
          i32.le_u
          br_if $B5
          local.get $p0
          local.get $p0
          i32.load offset=20
          local.get $l3
          i32.const 3
          i32.shl
          call $env.realloc
          i32.store offset=20
          local.get $p0
          local.get $p0
          i32.load offset=24
          local.get $l3
          i32.const 2
          i32.shl
          call $env.realloc
          i32.store offset=24
        end
        local.get $p0
        i32.load offset=16
        call $env.free
        local.get $p0
        local.get $l3
        i32.store
        local.get $p0
        local.get $l9
        i32.store offset=16
        local.get $p0
        local.get $l17
        i32.store offset=12
        local.get $p0
        local.get $p0
        i32.load offset=4
        i32.store offset=8
      end
      i32.const 0
      return
    end
    local.get $l9
    call $env.free
    i32.const -1)
  (func $kh_put_oligonucleotide (type $t8) (param $p0 i32) (param $p1 i64) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    block $B0
      block $B1
        local.get $p0
        i32.load offset=8
        local.get $p0
        i32.load offset=12
        i32.lt_u
        br_if $B1
        local.get $p0
        i32.load
        local.tee $l3
        local.get $p0
        i32.load offset=4
        i32.const 1
        i32.shl
        i32.gt_u
        if $I2
          local.get $p0
          local.get $l3
          i32.const 1
          i32.sub
          call $kh_resize_oligonucleotide
          i32.const 0
          i32.ge_s
          br_if $B1
          br $B0
        end
        local.get $p0
        local.get $l3
        i32.const 1
        i32.add
        call $kh_resize_oligonucleotide
        i32.const 0
        i32.ge_s
        br_if $B1
        br $B0
      end
      block $B3
        local.get $p0
        i32.load offset=16
        local.tee $l9
        local.get $p0
        i32.load
        local.tee $l7
        i32.const 1
        i32.sub
        local.tee $l12
        local.get $p1
        i64.const 7
        i64.shr_u
        local.get $p1
        i64.xor
        i32.wrap_i64
        i32.and
        local.tee $l3
        i32.const 2
        i32.shr_u
        i32.const 1073741820
        i32.and
        i32.add
        i32.load
        local.get $l3
        i32.const 1
        i32.shl
        i32.shr_u
        i32.const 2
        i32.and
        if $I4
          local.get $l3
          local.set $l4
          br $B3
        end
        i32.const 1
        local.set $l6
        local.get $l3
        local.set $l5
        local.get $l7
        local.set $l4
        block $B5 (result i32)
          block $B6
            loop $L7
              local.get $l5
              i32.const 1
              i32.shl
              local.tee $l8
              i32.const 30
              i32.and
              local.set $l10
              local.get $l9
              local.get $l5
              i32.const 2
              i32.shr_u
              i32.const 1073741820
              i32.and
              i32.add
              i32.load
              local.tee $l11
              local.get $l8
              i32.shr_u
              local.tee $l8
              i32.const 2
              i32.and
              br_if $B6
              local.get $l8
              i32.const 1
              i32.and
              i32.eqz
              if $I8
                local.get $p0
                i32.load offset=20
                local.get $l5
                i32.const 3
                i32.shl
                i32.add
                i64.load
                local.get $p1
                i64.eq
                br_if $B6
              end
              local.get $l5
              local.get $l4
              local.get $l11
              local.get $l10
              i32.shr_u
              i32.const 1
              i32.and
              select
              local.set $l4
              local.get $l5
              local.get $l6
              i32.add
              local.get $l6
              i32.const 1
              i32.add
              local.set $l6
              local.get $l12
              i32.and
              local.tee $l5
              local.get $l3
              i32.ne
              br_if $L7
            end
            i32.const 1
            local.get $l4
            local.get $l7
            local.tee $l6
            i32.eq
            br_if $B5
            drop
            br $B3
          end
          local.get $l4
          local.set $l6
          local.get $l5
          local.set $l3
          local.get $l11
          local.get $l10
          i32.shr_u
          i32.const 2
          i32.and
          i32.eqz
        end
        local.set $l8
        local.get $l3
        local.get $l3
        local.get $l6
        local.get $l6
        local.get $l7
        i32.eq
        select
        local.get $l8
        select
        local.set $l4
      end
      local.get $l4
      i32.const 1
      i32.shl
      local.tee $l3
      i32.const 30
      i32.and
      local.set $l6
      local.get $l9
      local.get $l4
      i32.const 2
      i32.shr_u
      i32.const 1073741820
      i32.and
      i32.add
      local.tee $l5
      i32.load
      local.tee $l7
      local.get $l3
      i32.shr_u
      local.tee $l3
      i32.const 2
      i32.and
      if $I9
        local.get $l5
        local.get $l7
        i32.const 3
        local.get $l6
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
        local.get $l4
        i32.const 3
        i32.shl
        i32.add
        local.get $p1
        i64.store
        local.get $p2
        i32.const 1
        i32.store
        local.get $l4
        return
      end
      local.get $l3
      i32.const 1
      i32.and
      if $I10
        local.get $l5
        local.get $l7
        i32.const 3
        local.get $l6
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
        local.get $l4
        i32.const 3
        i32.shl
        i32.add
        local.get $p1
        i64.store
        local.get $p2
        i32.const 2
        i32.store
        local.get $l4
        return
      end
      local.get $p2
      i32.const 0
      i32.store
      local.get $l4
      return
    end
    local.get $p2
    i32.const -1
    i32.store
    local.get $p0
    i32.load)
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
      i32.const 1
      i32.sub
      i32.store offset=4
    end)
  (func $element_Compare (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    local.get $p0
    i32.load offset=8
    local.tee $l2
    local.get $p1
    i32.load offset=8
    local.tee $l3
    i32.lt_u
    if $I0
      i32.const 1
      return
    end
    local.get $l2
    local.get $l3
    i32.gt_u
    if $I1
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
    (local $l4 i64) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i64) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32)
    global.get $g0
    i32.const 16
    i32.sub
    local.tee $l14
    global.set $g0
    local.get $p2
    i32.const 1
    i32.sub
    local.set $l10
    i64.const -1
    local.get $p2
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const -1
    i64.xor
    local.set $l9
    i32.const 1
    i32.const 28
    call $env.calloc
    local.set $l6
    block $B0
      local.get $p2
      i32.const 2
      i32.lt_s
      br_if $B0
      local.get $l10
      i32.const 3
      i32.and
      local.set $l7
      block $B1
        local.get $p2
        i32.const 2
        i32.sub
        i32.const 3
        i32.lt_u
        if $I2
          br $B1
        end
        local.get $l10
        i32.const -4
        i32.and
        local.set $l8
        loop $L3
          local.get $p0
          local.get $l5
          i32.add
          local.tee $p3
          i64.load8_s
          local.get $l4
          i64.const 2
          i64.shl
          local.get $l9
          i64.and
          i64.or
          i64.const 2
          i64.shl
          local.get $l9
          i64.and
          local.get $p3
          i32.const 1
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l9
          i64.and
          local.get $p3
          i32.const 2
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l9
          i64.and
          local.get $p3
          i32.const 3
          i32.add
          i64.load8_s
          i64.or
          local.set $l4
          local.get $l8
          local.get $l5
          i32.const 4
          i32.add
          local.tee $l5
          i32.ne
          br_if $L3
        end
      end
      local.get $l7
      i32.eqz
      br_if $B0
      local.get $p0
      local.get $l5
      i32.add
      local.set $p3
      loop $L4
        local.get $p3
        i64.load8_s
        local.get $l4
        i64.const 2
        i64.shl
        local.get $l9
        i64.and
        i64.or
        local.set $l4
        local.get $p3
        i32.const 1
        i32.add
        local.set $p3
        local.get $l7
        i32.const 1
        i32.sub
        local.tee $l7
        br_if $L4
      end
    end
    block $B5
      local.get $p1
      local.get $p2
      i32.lt_s
      if $I6
        i32.const 0
        local.set $p1
        i32.const 0
        call $env.malloc
        local.set $l11
        i32.const 0
        local.set $l8
        local.get $l6
        i32.load offset=16
        local.set $l12
        br $B5
      end
      local.get $p0
      local.get $l10
      i32.add
      local.set $p3
      local.get $p1
      local.get $p2
      i32.sub
      i32.const 1
      i32.add
      local.set $p0
      loop $L7
        local.get $l6
        local.get $p3
        i64.load8_s
        local.get $l4
        i64.const 2
        i64.shl
        local.get $l9
        i64.and
        i64.or
        local.tee $l4
        local.get $l14
        i32.const 12
        i32.add
        call $kh_put_oligonucleotide
        local.set $p1
        i32.const 1
        local.set $l7
        local.get $l6
        i32.load offset=24
        local.tee $l8
        local.get $p1
        i32.const 2
        i32.shl
        i32.add
        local.tee $p1
        local.get $l14
        i32.load offset=12
        if $I8 (result i32)
          i32.const 1
        else
          local.get $p1
          i32.load
          i32.const 1
          i32.add
        end
        i32.store
        local.get $p3
        i32.const 1
        i32.add
        local.set $p3
        local.get $p0
        i32.const 1
        i32.sub
        local.tee $p0
        br_if $L7
      end
      local.get $l6
      i32.load
      local.set $l15
      local.get $l6
      i32.load offset=4
      local.tee $p1
      i32.const 4
      i32.shl
      call $env.malloc
      local.set $l11
      local.get $l15
      i32.eqz
      if $I9
        local.get $l6
        i32.load offset=16
        local.set $l12
        br $B5
      end
      local.get $l6
      i32.load offset=16
      local.set $l12
      i32.const 0
      local.set $p3
      i32.const 0
      local.set $l7
      local.get $l8
      local.set $p0
      i32.const 0
      local.set $l5
      loop $L10
        local.get $l12
        local.get $l5
        i32.const 2
        i32.shr_u
        i32.const 1073741820
        i32.and
        i32.add
        i32.load
        local.get $p3
        i32.const 30
        i32.and
        i32.shr_u
        i32.const 3
        i32.and
        i32.eqz
        if $I11
          local.get $l11
          local.get $l13
          i32.const 4
          i32.shl
          i32.add
          local.tee $l16
          local.get $p0
          i32.load
          i32.store offset=8
          local.get $l16
          local.get $l6
          i32.load offset=20
          local.get $l7
          i32.add
          i64.load
          i64.store
          local.get $l13
          i32.const 1
          i32.add
          local.set $l13
        end
        local.get $p3
        i32.const 2
        i32.add
        local.set $p3
        local.get $l7
        i32.const 8
        i32.add
        local.set $l7
        local.get $p0
        i32.const 4
        i32.add
        local.set $p0
        local.get $l15
        local.get $l5
        i32.const 1
        i32.add
        local.tee $l5
        i32.ne
        br_if $L10
      end
    end
    local.get $l6
    i32.load offset=20
    call $env.free
    local.get $l12
    call $env.free
    local.get $l8
    call $env.free
    local.get $l6
    call $env.free
    local.get $l11
    local.get $p1
    i32.const 16
    i32.const 1
    call $env.qsort
    block $B12
      local.get $p1
      i32.const 0
      i32.le_s
      br_if $B12
      local.get $p2
      i32.const 0
      i32.le_s
      br_if $B12
      local.get $p2
      i32.const 7
      i32.and
      local.set $p0
      i32.const 0
      local.set $l5
      local.get $p2
      i32.const 8
      i32.lt_u
      local.set $l8
      loop $L13
        local.get $l11
        local.get $l5
        i32.const 4
        i32.shl
        i32.add
        local.tee $l6
        i64.load
        local.set $l4
        block $B14
          local.get $p0
          i32.eqz
          if $I15
            local.get $l10
            local.set $p3
            br $B14
          end
          local.get $p0
          local.set $p2
          local.get $l10
          local.set $p3
          loop $L16
            local.get $p3
            i32.const 1
            i32.sub
            local.set $p3
            local.get $l4
            i64.const 2
            i64.shr_u
            local.set $l4
            local.get $p2
            i32.const 1
            i32.sub
            local.tee $p2
            br_if $L16
          end
        end
        local.get $l8
        i32.eqz
        if $I17
          local.get $p3
          i32.const 8
          i32.add
          local.set $p3
          loop $L18
            local.get $l4
            i64.const 16
            i64.shr_u
            local.set $l4
            local.get $p3
            i32.const 8
            i32.sub
            local.tee $p3
            i32.const 7
            i32.gt_s
            br_if $L18
          end
        end
        local.get $l6
        local.get $l4
        i64.store
        local.get $l5
        i32.const 1
        i32.add
        local.tee $l5
        local.get $p1
        i32.ne
        br_if $L13
      end
    end
    local.get $l11
    call $env.free
    local.get $l14
    i32.const 16
    i32.add
    global.set $g0)
  (func $generate_Count_For_Oligonucleotide (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i64) (local $l5 i64) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32)
    global.get $g0
    i32.const 16
    i32.sub
    local.tee $l9
    global.set $g0
    local.get $p2
    call $env.strlen
    local.tee $l7
    i32.const 1
    i32.sub
    local.set $l10
    i64.const -1
    local.get $l7
    i32.const 1
    i32.shl
    i64.extend_i32_u
    i64.shl
    i64.const -1
    i64.xor
    local.set $l5
    i32.const 1
    i32.const 28
    call $env.calloc
    local.set $l8
    block $B0
      local.get $l7
      i32.const 2
      i32.lt_s
      br_if $B0
      local.get $l10
      i32.const 3
      i32.and
      local.set $p3
      block $B1
        local.get $l7
        i32.const 2
        i32.sub
        i32.const 3
        i32.lt_u
        if $I2
          i32.const 0
          local.set $p2
          br $B1
        end
        local.get $l10
        i32.const -4
        i32.and
        local.set $l11
        i32.const 0
        local.set $p2
        loop $L3
          local.get $p0
          local.get $p2
          i32.add
          local.tee $l6
          i64.load8_s
          local.get $l4
          i64.const 2
          i64.shl
          local.get $l5
          i64.and
          i64.or
          i64.const 2
          i64.shl
          local.get $l5
          i64.and
          local.get $l6
          i32.const 1
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l5
          i64.and
          local.get $l6
          i32.const 2
          i32.add
          i64.load8_s
          i64.or
          i64.const 2
          i64.shl
          local.get $l5
          i64.and
          local.get $l6
          i32.const 3
          i32.add
          i64.load8_s
          i64.or
          local.set $l4
          local.get $l11
          local.get $p2
          i32.const 4
          i32.add
          local.tee $p2
          i32.ne
          br_if $L3
        end
      end
      local.get $p3
      i32.eqz
      br_if $B0
      local.get $p0
      local.get $p2
      i32.add
      local.set $p2
      loop $L4
        local.get $p2
        i64.load8_s
        local.get $l4
        i64.const 2
        i64.shl
        local.get $l5
        i64.and
        i64.or
        local.set $l4
        local.get $p2
        i32.const 1
        i32.add
        local.set $p2
        local.get $p3
        i32.const 1
        i32.sub
        local.tee $p3
        br_if $L4
      end
    end
    i32.const 0
    local.set $l6
    local.get $p1
    local.get $l7
    i32.ge_s
    if $I5
      local.get $p0
      local.get $l10
      i32.add
      local.set $p2
      local.get $p1
      local.get $l7
      i32.sub
      i32.const 1
      i32.add
      local.set $p0
      loop $L6
        local.get $l8
        local.get $p2
        i64.load8_s
        local.get $l4
        i64.const 2
        i64.shl
        local.get $l5
        i64.and
        i64.or
        local.tee $l4
        local.get $l9
        i32.const 12
        i32.add
        call $kh_put_oligonucleotide
        local.set $p1
        i32.const 1
        local.set $p3
        local.get $l8
        i32.load offset=24
        local.tee $l6
        local.get $p1
        i32.const 2
        i32.shl
        i32.add
        local.tee $p1
        local.get $l9
        i32.load offset=12
        if $I7 (result i32)
          i32.const 1
        else
          local.get $p1
          i32.load
          i32.const 1
          i32.add
        end
        i32.store
        local.get $p2
        i32.const 1
        i32.add
        local.set $p2
        local.get $p0
        i32.const 1
        i32.sub
        local.tee $p0
        br_if $L6
      end
    end
    local.get $l8
    i32.load offset=20
    call $env.free
    local.get $l8
    i32.load offset=16
    call $env.free
    local.get $l6
    call $env.free
    local.get $l8
    call $env.free
    local.get $l9
    i32.const 16
    i32.add
    global.set $g0)
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    global.get $g0
    i32.const 4096
    i32.sub
    local.tee $l2
    global.set $g0
    loop $L0
      local.get $l2
      i32.const 4096
      i32.const 0
      i32.load
      call $env.fgets
      if $I1
        i32.const 1076
        local.get $l2
        i32.const 6
        call $env.memcmp
        br_if $L0
      end
    end
    i32.const 1048576
    call $env.malloc
    local.set $l3
    block $B2
      local.get $l2
      i32.const 4096
      i32.const 0
      i32.load
      call $env.fgets
      i32.eqz
      if $I3
        br $B2
      end
      local.get $l2
      i32.load8_u
      local.tee $l4
      i32.const 62
      i32.eq
      if $I4
        br $B2
      end
      local.get $l2
      i32.const 1
      i32.or
      local.set $l6
      i32.const 1048576
      local.set $l5
      loop $L5
        local.get $l6
        local.set $l0
        loop $L6
          block $B7
            block $B8
              block $B9
                local.get $l4
                br_table $B9 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B7 $B8
              end
              local.get $l5
              local.get $l1
              i32.sub
              i32.const 4096
              i32.lt_u
              if $I10
                local.get $l3
                local.get $l5
                i32.const 1
                i32.shl
                local.tee $l5
                call $env.realloc
                local.set $l3
              end
              local.get $l2
              i32.const 4096
              i32.const 0
              i32.load
              call $env.fgets
              i32.eqz
              br_if $B2
              local.get $l2
              i32.load8_u
              local.tee $l4
              i32.const 62
              i32.ne
              br_if $L5
              br $B2
            end
            local.get $l1
            local.get $l3
            i32.add
            local.get $l4
            i32.const 7
            i32.and
            i32.const 1024
            i32.add
            i32.load8_u
            i32.store8
            local.get $l1
            i32.const 1
            i32.add
            local.set $l1
          end
          local.get $l0
          i32.load8_u
          local.set $l4
          local.get $l0
          i32.const 1
          i32.add
          local.set $l0
          br $L6
        end
        unreachable
      end
      unreachable
    end
    local.get $l3
    local.get $l1
    call $env.realloc
    local.tee $l0
    local.get $l1
    i32.const 1057
    local.get $l0
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    local.get $l1
    i32.const 1040
    local.get $l0
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    local.get $l1
    i32.const 1033
    local.get $l0
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    local.get $l1
    i32.const 1083
    local.get $l0
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    local.get $l1
    i32.const 1053
    local.get $l0
    call $generate_Count_For_Oligonucleotide
    local.get $l0
    local.get $l1
    i32.const 2
    local.get $l0
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get $l0
    local.get $l1
    i32.const 1
    local.get $l0
    call $generate_Frequencies_For_Desired_Length_Oligonucleotides
    local.get $l0
    call $env.free
    local.get $l2
    i32.const 4096
    i32.add
    global.set $g0
    i32.const 0)
  (table $__indirect_function_table 2 2 funcref)
  (memory $memory 2)
  (global $g0 (mut i32) (i32.const 66624))
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
  (data $d0 (i32.const 1024) " \00 \01\03  \02\00GGTATT\00GGTATTTTAATT\00GGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA")
  (@custom ".debug_loc" "\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\001\9f\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\09\9f\01\00\00\00\01\00\00\00\02\001\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\0d\9f\01\00\00\00\01\00\00\00\04\00\ed\00\11\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\11\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\13\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\01\00\00\00\01\00\00\00\04\00\ed\00\08\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\08\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\0b\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0a\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\0c\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\08\9f\01\00\00\00\01\00\00\00\04\00\ed\00\08\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\08\9f\01\00\00\00\01\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\09\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\0b\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\05\00\10\80\80@\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\000\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13:\0b;\0b\1c\0f\00\00\03&\00I\13\00\00\04$\00\03\0e>\0b\0b\0b\00\00\054\00I\13:\0b;\0b\00\00\06\01\01I\13\00\00\07!\00I\137\0b\00\00\08$\00\03\0e\0b\0b>\0b\00\00\094\00I\13:\0b;\0b\02\18\00\00\0a\0f\00I\13\00\00\0b\16\00I\13\03\0e:\0b;\0b\00\00\0c\13\01\03\0e\0b\0b:\0b;\0b\00\00\0d\0d\00\03\0eI\13:\0b;\0b8\0b\00\00\0e\0f\00\00\00\0f\15\01I\13'\19\00\00\10\05\00I\13\00\00\11&\00\00\00\12.\01\11\01\12\06@\18\97B\191\13\00\00\13\89\82\01\001\13\11\01\00\00\14.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\15\05\00\02\181\13\00\00\16.\01\03\0e:\0b;\0b'\19<\19?\19\00\00\17.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\18\05\00\02\18\03\0e:\0b;\0bI\13\00\00\19.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19I\13?\19\00\00\1a\0b\01\11\01\12\06\00\00\1b4\00\02\17\03\0e:\0b;\0bI\13\00\00\1c\05\00\03\0e:\0b;\0bI\13\00\00\1d\05\00\02\17\03\0e:\0b;\0bI\13\00\00\1e\0b\01U\17\00\00\1f4\00\03\0e:\0b;\0bI\13\00\00 .\00\03\0e:\0b;\0b'\19I\13?\19 \0b\00\00!.\01\03\0e:\0b;\0b'\19?\19 \0b\00\00\22\1d\001\13\11\01\12\06X\0bY\0bW\0b\00\00#4\00\02\18\03\0e:\0b;\0bI\13\00\00$\1d\001\13U\17X\0bY\0bW\0b\00\00%4\00\1c\0d\03\0e:\0b;\0bI\13\00\00&4\00\03\0eI\134\19\00\00'.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00(\13\00\03\0e<\19\00\00)\13\01\0b\0b:\0b;\0b\00\00*!\00I\137\13\00\00+!\00I\137\05\00\00\00")
  (@custom ".debug_info" "\ad\0b\00\00\04\00\00\00\00\00\04\01E\04\00\00\1d\00\09\04\00\00\00\00\00\00\94\03\00\00\00\00\00\00\c0\00\00\00\02,\04\00\00:\00\00\00\01\be\a4\e1\f5\d1\f0\fa\a8\f4?\03?\00\00\00\04v\02\00\00\04\08\05M\00\00\00\02s\06Y\00\00\00\07`\00\00\00\05\00\04\b5\01\00\00\06\01\08\18\04\00\00\08\07\09t\00\00\00\02\a9\05\03\00\04\00\00\06Y\00\00\00\07`\00\00\00\09\00\09\8d\00\00\00\02\b8\05\034\04\00\00\06Y\00\00\00\07`\00\00\00\07\00\09\a6\00\00\00\02\d8\05\03!\04\00\00\06Y\00\00\00\07`\00\00\00\13\00\09\bf\00\00\00\02\db\05\03\10\04\00\00\06Y\00\00\00\07`\00\00\00\0d\00\09\8d\00\00\00\02\de\05\03\09\04\00\00\09M\00\00\00\02\e1\05\03;\04\00\00\09\f2\00\00\00\02\e4\05\03\1d\04\00\00\06Y\00\00\00\07`\00\00\00\04\00\0a\03\01\00\00\0b\0e\01\00\00\c9\00\00\00\02\14\0c\8b\01\00\00\1c\02\14\0d\0b\01\00\00k\01\00\00\02\14\00\0dB\02\00\00k\01\00\00\02\14\04\0d\89\03\00\00k\01\00\00\02\14\08\0dj\03\00\00k\01\00\00\02\14\0c\0d(\01\00\00\88\01\00\00\02\14\10\0d\02\01\00\00\8d\01\00\00\02\14\14\0d\1f\01\00\00\af\01\00\00\02\14\18\00\0bv\01\00\00\8c\00\00\00\01\a0\0b\81\01\00\00\f4\00\00\00\01\85\04L\00\00\00\07\04\0av\01\00\00\0a\92\01\00\00\0b\9d\01\00\00\e0\00\00\00\03A\0b\a8\01\00\00\de\00\00\00\03\1d\04!\02\00\00\07\08\0a\b4\01\00\00\0b\bf\01\00\00\eb\00\00\00\03@\0b\81\01\00\00\e9\00\00\00\03\17\0e\0a\d0\01\00\00\0f\e0\01\00\00\10\e7\01\00\00\10\e7\01\00\00\00\04U\00\00\00\05\04\0a\ec\01\00\00\11\12\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\f6\05\00\00\13\0c\02\00\00\00\00\00\00\00\14\02\04\00\00\03\cc\ca\01\00\00\10\22\02\00\00\10\22\02\00\00\00\0b-\02\00\00\b6\00\00\00\03\9c\044\02\00\00\07\04\12\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\02\06\00\00\15\04\ed\00\00\9f\0a\06\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\00\16\88\02\00\00\03\ce\10\ca\01\00\00\00\17\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\fd\02\00\00\02\14\18\04\ed\00\00\9f\1f\02\00\00\02\14\fe\00\00\00\00\19\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\e6\02\00\00\02\14k\01\00\00\18\04\ed\00\00\9f\1f\02\00\00\02\14\e1\0a\00\00\18\04\ed\00\01\9f\18\00\00\00\02\14\92\01\00\00\1a\00\00\00\00\00\00\00\00\1b\00\00\00\00\c5\01\00\00\02\14k\01\00\00\1b\1c\00\00\00\e9\01\00\00\02\14k\01\00\00\1bH\00\00\00\ec\01\00\00\02\14k\01\00\00\1bf\00\00\00\f2\01\00\00\02\14k\01\00\00\1b\ae\00\00\004\00\00\00\02\14k\01\00\00\00\00\19\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f-\03\00\00\02\14\e0\01\00\00\1c\1f\02\00\00\02\14\fe\00\00\00\1d\da\00\00\00\07\01\00\00\02\14k\01\00\00\1b\bc\01\00\00$\01\00\00\02\14\88\01\00\00\1b\02\02\00\00\ee\01\00\00\02\14k\01\00\00\1e\00\00\00\00\1bR\02\00\00\fe\00\00\00\02\14\8d\01\00\00\1a\00\00\00\00\00\00\00\00\1b~\02\00\00\1b\01\00\00\02\14\af\01\00\00\00\00\1e\18\00\00\00\1b\aa\02\00\00\e1\01\00\00\02\14\b4\01\00\00\1f\18\00\00\00\02\14\92\01\00\00\1f\e5\01\00\00\02\14k\01\00\00\1e8\00\00\00\1b\d6\02\00\00\c5\01\00\00\02\14k\01\00\00\1b\f2\02\00\00\ec\01\00\00\02\14k\01\00\00\1b\10\03\00\00\f2\01\00\00\02\14k\01\00\00\1a\00\00\00\00\00\00\00\00\1bX\03\00\00\ba\01\00\00\02\14\b4\01\00\00\00\1a\00\00\00\00\00\00\00\00\1bv\03\00\00\ba\01\00\00\02\14\92\01\00\00\00\00\00\13\8f\04\00\00\00\00\00\00\13\a0\04\00\00\00\00\00\00\13\a0\04\00\00\00\00\00\00\13\a0\04\00\00\00\00\00\00\13\a0\04\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\00\14\f3\03\00\00\03\cb\ca\01\00\00\10\22\02\00\00\00\14\fa\03\00\00\03\cd\ca\01\00\00\10\ca\01\00\00\10-\02\00\00\00\19\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\b7\02\00\00\02\14k\01\00\00\18\04\ed\00\00\9f\1f\02\00\00\02\14\fe\00\00\00\18\04\ed\00\01\9f\18\00\00\00\02\14\92\01\00\00\18\04\ed\00\02\9f|\00\00\00\02\14\eb\0a\00\00\1b\16\04\00\00+\00\00\00\02\14k\01\00\00\1a\00\00\00\00\00\00\00\00\1b\94\03\00\00\e9\01\00\00\02\14k\01\00\00\1b\c0\03\00\00\c5\01\00\00\02\14k\01\00\00\1b\dc\03\00\00a\02\00\00\02\14k\01\00\00\1bP\04\00\00\ec\01\00\00\02\14k\01\00\00\1bn\04\00\00\f2\01\00\00\02\14k\01\00\00\1f4\00\00\00\02\14k\01\00\00\00\13>\03\00\00\00\00\00\00\13>\03\00\00\00\00\00\00\00\17\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\16\03\00\00\02\14\18\04\ed\00\00\9f\1f\02\00\00\02\14\fe\00\00\00\1d\b6\04\00\00+\00\00\00\02\14k\01\00\00\00\19\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9ff\02\00\00\02/\e0\01\00\00\18\04\ed\00\00\9fo\00\00\00\02/\f0\0a\00\00\18\04\ed\00\01\9fa\00\00\00\020\f0\0a\00\00\00 \ce\02\00\00\02\14\fe\00\00\00\01!\9c\02\00\00\02\14\01\1c\1f\02\00\00\02\14\fe\00\00\00\00\17\00\00\00\00\00\00\00\00\04\ed\00\04\9fR\01\00\00\02>\1d\b8\05\00\00\8d\02\00\00\02?G\0b\00\00\1d\9a\05\00\00\f4\01\00\00\02?'\0b\00\00\18\04\ed\00\02\9f.\01\00\00\02@'\0b\00\00\1c-\00\00\00\02@a\0b\00\00\1b\0c\05\00\00\18\00\00\00\02D\92\01\00\00\1b|\05\00\00\e9\01\00\00\02EB\0b\00\00\1b\d6\05\00\00}\02\00\00\02B\fe\00\00\00\1b\f4\05\00\00G\02\00\00\02a,\0b\00\00\1b,\06\00\00\f2\01\00\00\02a,\0b\00\00\1bT\06\00\00\1c\00\00\00\02bL\0b\00\00\1b\e2\06\00\00[\02\00\00\02c\b4\01\00\00\22\f6\05\00\00\00\00\00\00\00\00\00\00\02B(\1a\00\00\00\00\00\00\00\00\1b\d4\04\00\00\f2\01\00\00\02I,\0b\00\00\00\1eP\00\00\00\1f\f2\01\00\00\02O,\0b\00\00\1a\00\00\00\00\00\00\00\00#\02\91\0cv\03\00\00\02T\e0\01\00\00\1b\c4\06\00\00\ec\01\00\00\02UQ\0b\00\00\00\00$\02\06\00\00h\00\00\00\02g\02\1e\88\00\00\00\1b\80\06\00\00\f0\01\00\00\02dk\01\00\00\00\1a\00\00\00\00\00\00\00\00%\00\ca\01\00\00\02n,\0b\00\00\1b\00\07\00\00\f2\01\00\00\02n,\0b\00\00\1a\00\00\00\00\00\00\00\00&Z\04\00\00\81\01\00\00\1f7\03\00\00\02qf\0b\00\00\1a\00\00\00\00\00\00\00\00\1b8\07\00\00\ee\01\00\00\02r,\0b\00\00\00\00\00\13\0c\02\00\00\00\00\00\00\13\8f\04\00\00\00\00\00\00\13\b6\04\00\00\00\00\00\00\13\8f\04\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13\05\08\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\00\169\00\00\00\03\f8\10\ca\01\00\00\10\22\02\00\00\10\22\02\00\00\10!\08\00\00\00\0b\cb\01\00\00\a6\00\00\00\03\f7\17\00\00\00\00\00\00\00\00\04\ed\00\04\9fG\03\00\00\02\85\1d\82\08\00\00\8d\02\00\00\02\86G\0b\00\00\1dd\08\00\00\f4\01\00\00\02\86'\0b\00\00\1dV\07\00\007\03\00\00\02\87G\0b\00\00\1c-\00\00\00\02\87a\0b\00\00\1bt\07\00\00\0a\02\00\00\02\88'\0b\00\00\1b\d8\07\00\00\18\00\00\00\02\8c\92\01\00\00\1bF\08\00\00\e9\01\00\00\02\8dB\0b\00\00\1b\a0\08\00\00}\02\00\00\02\8au\0b\00\00\1f\ec\01\00\00\02\acV\0b\00\00\1fF\00\00\00\02\adz\0b\00\00\22\f6\05\00\00\00\00\00\00\00\00\00\00\02\8a.\1a\00\00\00\00\00\00\00\00\1b\a0\07\00\00\f2\01\00\00\02\91,\0b\00\00\00\1a\00\00\00\00\00\00\00\00\1f\f2\01\00\00\02\96,\0b\00\00\1a\00\00\00\00\00\00\00\00#\02\91\0cv\03\00\00\02\9a\e0\01\00\00\1b\be\08\00\00\ec\01\00\00\02\9bQ\0b\00\00\00\00\22\02\06\00\00\00\00\00\00\00\00\00\00\02\b0\02\13\7f\09\00\00\00\00\00\00\13\0c\02\00\00\00\00\00\00\13\b6\04\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\00\14\da\01\00\00\03\db-\02\00\00\10\90\09\00\00\00\0a\95\09\00\00\03Y\00\00\00'\00\00\00\00\00\00\00\00\04\ed\00\00\9f?\00\00\00\02\b4\e0\01\00\00#\02\91\00\a0\01\00\00\02\b5\90\0b\00\00\1b\dc\08\00\00\00\00\00\00\02\bd,\0b\00\00\1b\17\09\00\00\f4\01\00\00\02\be,\0b\00\00\1b3\09\00\00\8d\02\00\00\02\bf\ac\0a\00\00\1f\a7\01\00\00\02\d1\9d\0b\00\00\1e\a8\00\00\00\1f\f2\01\00\00\02\c3,\0b\00\00\00\13\91\0a\00\00\00\00\00\00\13\c6\0a\00\00\00\00\00\00\13\8f\04\00\00\00\00\00\00\13\91\0a\00\00\00\00\00\00\13\a0\04\00\00\00\00\00\00\13\91\0a\00\00\00\00\00\00\13\a0\04\00\00\00\00\00\00\13,\08\00\00\00\00\00\00\13,\08\00\00\00\00\00\00\13,\08\00\00\00\00\00\00\13,\08\00\00\00\00\00\00\13,\08\00\00\00\00\00\00\13\16\06\00\00\00\00\00\00\13\16\06\00\00\00\00\00\00\13x\02\00\00\00\00\00\00\00\14\15\01\00\00\03\ed\ac\0a\00\00\10\ac\0a\00\00\10\e0\01\00\00\10\b1\0a\00\00\00\0aY\00\00\00\0a\b6\0a\00\00\0b\c1\0a\00\00@\04\00\00\03\e8(<\04\00\00\14\be\01\00\00\03\da\e0\01\00\00\10\e7\01\00\00\10\e7\01\00\00\10\22\02\00\00\00\0a\e6\0a\00\00\03\03\01\00\00\0a\e0\01\00\00\03\f5\0a\00\00\0a\fa\0a\00\00\03\ff\0a\00\00\0b\0a\0b\00\00Y\00\00\00\02\1d)\10\02\1a\0d\18\00\00\00\92\01\00\00\02\1b\00\0d[\02\00\00\b4\01\00\00\02\1c\08\00\03,\0b\00\00\0b7\0b\00\00\bd\00\00\00\02\18\0b\e0\01\00\00\94\00\00\00\03p\03\92\01\00\00\03\90\09\00\00\0a\ff\0a\00\00\03V\0b\00\00\0bk\01\00\00\9d\00\00\00\01\a1\03\ac\0a\00\00\06Y\00\00\00*`\00\00\00{\07\00\00\00\03\fe\00\00\00\0b\85\0b\00\00\82\00\00\00\03x\0b\a8\01\00\00\80\00\00\00\036\06Y\00\00\00+`\00\00\00\00\10\00\06Y\00\00\00\07`\00\00\00\07+`\00\00\00\00\10\00\00")
  (@custom ".debug_ranges" "\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "polynucleotide_Capacity\00key\00elements_Array\00x\00output\00last\00qsort\00_start\00count\00unsigned int\00element\00right_Element\00left_Element\00ret\00__uintmax_t\00khint_t\00intptr_t\00khiter_t\00comparison_fn_t\00size_t\00intnative_t\00kh_oligonucleotide_t\00__uint64_t\00__uint32_t\00khint32_t\00new_keys\00new_n_buckets\00fgets\00new_vals\00new_flags\00desired_Length_For_Oligonucleotides\00generate_Frequencies_For_Desired_Length_Oligonucleotides\00kh_oligonucleotide_s\00buffer\00output_Buffer\00char\00tmp\00memcmp\00step\00output_Position\00strlen\00val\00new_mask\00j\00__i\00polynucleotide_Length\00oligonucleotide_Length\00unsigned long long\00unsigned long\00size\00elements_Array_Size\00value\00site\00element_Compare\00double\00hash_Table\00free\00polynucleotide\00kh_destroy_oligonucleotide\00kh_put_oligonucleotide\00kh_init_oligonucleotide\00kh_get_oligonucleotide\00kh_clear_oligonucleotide\00kh_del_oligonucleotide\00kh_resize_oligonucleotide\00generate_Count_For_Oligonucleotide\00upper_bound\00element_Was_Unused\00n_occupied\00/home/sven/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00malloc\00realloc\00calloc\00k-nucleotide.c\00__ARRAY_SIZE_TYPE__\00__ac_HASH_UPPER\00_IO_FILE\00clang version 19.1.7\00__vla_expr0\00")
  (@custom ".debug_line" "M\00\00\00\04\00G\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01.\00./../..\00\00khash.h\00\01\00\00k-nucleotide.c\00\00\00\00stdlib.h\00\02\00\00\00")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0619.1.7")
  (@custom "target_features" "\04+\0fmutable-globals+\08sign-ext+\0freference-types+\0amultivalue"))
