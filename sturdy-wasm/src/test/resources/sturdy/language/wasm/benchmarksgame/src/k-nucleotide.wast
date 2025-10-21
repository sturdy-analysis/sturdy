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
  (data $d0 (i32.const 1024) " \00 \01\03  \02\00GGTATT\00GGTATTTTAATT\00GGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA"))
