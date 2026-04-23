(module $pidigits.wasm
  (type $t0 (func (param i32 i32 i32) (result i32)))
  (type $t1 (func))
  (type $t2 (func (param i32)))
  (type $t3 (func (param i32) (result i32)))
  (type $t4 (func (param i32 i32) (result i32)))
  (type $t5 (func (result i32)))
  (type $t6 (func (param i32 i32 i32 i32 i32) (result i32)))
  (type $t7 (func (param i32 i32)))
  (type $t8 (func (param i32 i32 i32)))
  (type $t9 (func (param i32 i32 i32 i32) (result i32)))
  (type $t10 (func (param i32 i32 i32 i32)))
  (type $t11 (func (param i32 i32 i32 i32 i32 i32 i32)))
  (type $t12 (func (param i32 i32 i32 i32 i32 i32)))
  (type $t13 (func (param i32 i32 i32 i32 i32)))
  (import "env" "fprintf" (func $fprintf (type $t0)))
  (import "env" "abort" (func $abort (type $t1)))
  (import "env" "assert" (func $assert (type $t2)))
  (import "env" "malloc" (func $malloc (type $t3)))
  (import "env" "realloc" (func $realloc (type $t4)))
  (import "env" "free" (func $free (type $t2)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t5)))
  (import "env" "putchar" (func $putchar (type $t3)))
  (import "env" "printf" (func $printf (type $t4)))
  (func $gmp_die (type $t2) (param $p0 i32)
    (local $l1 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store
    i32.const 0
    i32.load
    i32.const 1150
    local.get $l1
    call $fprintf
    drop
    call $abort
    unreachable)
  (func $mpn_mul (type $t6) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    local.get $p2
    local.get $p4
    i32.ge_s
    call $assert
    i32.const 0
    local.set $l5
    local.get $p4
    i32.const 0
    i32.gt_s
    call $assert
    local.get $p0
    local.get $p2
    i32.const 2
    i32.shl
    local.tee $l6
    i32.add
    local.tee $l7
    local.get $p4
    i32.const 2
    i32.shl
    local.tee $l8
    i32.add
    local.tee $l9
    local.get $p1
    i32.le_u
    local.get $p1
    local.get $l6
    i32.add
    local.get $p0
    i32.le_u
    i32.or
    call $assert
    local.get $l9
    local.get $p3
    i32.le_u
    local.get $p3
    local.get $l8
    i32.add
    local.get $p0
    i32.le_u
    i32.or
    call $assert
    local.get $p3
    i32.load
    local.set $l6
    local.get $p2
    i32.const 0
    i32.gt_s
    local.tee $l10
    call $assert
    local.get $l6
    i32.const 16
    i32.shr_u
    local.set $l11
    local.get $l6
    i32.const 65535
    i32.and
    local.set $l12
    local.get $p2
    local.set $l13
    local.get $p1
    local.set $l6
    local.get $p0
    local.set $l8
    loop $L0
      local.get $l6
      i32.load
      local.set $l9
      i32.const 1
      call $assert
      local.get $l8
      local.get $l9
      i32.const 65535
      i32.and
      local.tee $l14
      local.get $l12
      i32.mul
      local.tee $l15
      i32.const 65535
      i32.and
      local.get $l5
      i32.add
      local.get $l9
      i32.const 16
      i32.shr_u
      local.tee $l16
      local.get $l12
      i32.mul
      local.tee $l17
      local.get $l14
      local.get $l11
      i32.mul
      i32.add
      local.get $l15
      i32.const 16
      i32.shr_u
      i32.add
      local.tee $l9
      i32.const 16
      i32.shl
      i32.add
      local.tee $l14
      i32.store
      local.get $l16
      local.get $l11
      i32.mul
      local.tee $l15
      i32.const 65536
      i32.add
      local.get $l15
      local.get $l9
      local.get $l17
      i32.lt_u
      select
      local.get $l9
      i32.const 16
      i32.shr_u
      i32.add
      local.get $l14
      local.get $l5
      i32.lt_u
      i32.add
      local.set $l5
      local.get $l8
      i32.const 4
      i32.add
      local.set $l8
      local.get $l6
      i32.const 4
      i32.add
      local.set $l6
      local.get $l13
      i32.const -1
      i32.add
      local.tee $l13
      br_if $L0
    end
    local.get $l7
    local.get $l5
    i32.store
    block $B1
      local.get $p4
      i32.const 2
      i32.lt_s
      br_if $B1
      loop $L2
        local.get $p3
        i32.load offset=4
        local.set $l6
        local.get $l10
        call $assert
        local.get $p3
        i32.const 4
        i32.add
        local.set $p3
        local.get $p0
        i32.const 4
        i32.add
        local.set $l7
        local.get $l6
        i32.const 16
        i32.shr_u
        local.set $l11
        local.get $l6
        i32.const 65535
        i32.and
        local.set $l12
        i32.const 0
        local.set $l6
        i32.const 0
        local.set $l5
        local.get $p2
        local.set $l13
        loop $L3
          local.get $p1
          local.get $l6
          i32.add
          i32.load
          local.set $l8
          i32.const 1
          call $assert
          local.get $p0
          local.get $l6
          i32.add
          i32.const 4
          i32.add
          local.tee $l14
          local.get $l8
          i32.const 65535
          i32.and
          local.tee $l9
          local.get $l12
          i32.mul
          local.tee $l15
          i32.const 65535
          i32.and
          local.get $l5
          i32.add
          local.get $l8
          i32.const 16
          i32.shr_u
          local.tee $l16
          local.get $l12
          i32.mul
          local.tee $l17
          local.get $l9
          local.get $l11
          i32.mul
          i32.add
          local.get $l15
          i32.const 16
          i32.shr_u
          i32.add
          local.tee $l8
          i32.const 16
          i32.shl
          i32.add
          local.tee $l9
          local.get $l14
          i32.load
          i32.add
          local.tee $l14
          i32.store
          local.get $l16
          local.get $l11
          i32.mul
          local.tee $l15
          i32.const 65536
          i32.add
          local.get $l15
          local.get $l8
          local.get $l17
          i32.lt_u
          select
          local.get $l8
          i32.const 16
          i32.shr_u
          i32.add
          local.get $l9
          local.get $l5
          i32.lt_u
          i32.add
          local.get $l14
          local.get $l9
          i32.lt_u
          i32.add
          local.set $l5
          local.get $l6
          i32.const 4
          i32.add
          local.set $l6
          local.get $l13
          i32.const -1
          i32.add
          local.tee $l13
          br_if $L3
        end
        local.get $l7
        local.get $p2
        i32.const 2
        i32.shl
        i32.add
        local.get $l5
        i32.store
        local.get $p4
        i32.const 2
        i32.gt_s
        local.set $l6
        local.get $p4
        i32.const -1
        i32.add
        local.set $p4
        local.get $l7
        local.set $p0
        local.get $l6
        br_if $L2
      end
    end
    local.get $l5)
  (func $mpn_invert_3by2 (type $t4) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    i32.const 1
    call $assert
    block $B0
      local.get $p0
      i32.const -1
      i32.xor
      local.tee $l2
      local.get $l2
      local.get $p0
      i32.const 16
      i32.shr_u
      local.tee $l3
      i32.div_u
      local.tee $l2
      local.get $l3
      i32.mul
      i32.sub
      i32.const 16
      i32.shl
      i32.const 65535
      i32.or
      local.tee $l3
      local.get $l2
      local.get $p0
      i32.const 65535
      i32.and
      i32.mul
      local.tee $l4
      i32.ge_u
      br_if $B0
      local.get $l2
      i32.const -1
      i32.add
      local.set $l5
      block $B1
        local.get $l3
        local.get $p0
        i32.add
        local.tee $l3
        local.get $p0
        i32.ge_u
        br_if $B1
        local.get $l5
        local.set $l2
        br $B0
      end
      block $B2
        local.get $l3
        local.get $l4
        i32.lt_u
        br_if $B2
        local.get $l5
        local.set $l2
        br $B0
      end
      local.get $l3
      local.get $p0
      i32.add
      local.set $l3
      local.get $l2
      i32.const -2
      i32.add
      local.set $l2
    end
    local.get $l3
    local.get $l4
    i32.sub
    local.tee $l3
    i32.const 16
    i32.shr_u
    local.get $l2
    i32.mul
    local.get $l3
    i32.add
    local.tee $l4
    i32.const 16
    i32.shr_u
    local.tee $l5
    local.get $p0
    local.get $l5
    i32.const -1
    i32.xor
    i32.mul
    local.get $l3
    i32.const 16
    i32.shl
    i32.add
    i32.const 65535
    i32.add
    local.tee $l3
    local.get $l4
    i32.const 16
    i32.shl
    i32.lt_u
    local.tee $l4
    i32.add
    local.get $l2
    i32.const 16
    i32.shl
    i32.add
    i32.const 0
    local.get $p0
    local.get $l4
    select
    local.get $l3
    i32.add
    local.tee $l3
    local.get $p0
    i32.ge_u
    local.tee $l4
    i32.add
    local.set $l2
    block $B3
      local.get $p1
      i32.eqz
      br_if $B3
      block $B4
        local.get $p1
        local.get $l3
        i32.const -1
        i32.xor
        i32.add
        local.get $p0
        i32.const 0
        local.get $l4
        select
        i32.add
        local.tee $l3
        local.get $p1
        i32.ge_u
        br_if $B4
        i32.const -1
        i32.const -2
        local.get $l3
        local.get $p0
        i32.lt_u
        local.tee $l4
        select
        local.get $l2
        i32.add
        local.set $l2
        local.get $l3
        local.get $p0
        i32.const 0
        local.get $p0
        local.get $l4
        select
        i32.add
        i32.sub
        local.set $l3
      end
      i32.const 1
      call $assert
      local.get $l2
      i32.const 16
      i32.shr_u
      local.tee $l4
      local.get $p1
      i32.const 16
      i32.shr_u
      local.tee $l5
      i32.mul
      local.tee $l6
      i32.const 65536
      i32.add
      local.get $l6
      local.get $l2
      i32.const 65535
      i32.and
      local.tee $l7
      local.get $l5
      i32.mul
      local.tee $l5
      local.get $l4
      local.get $p1
      i32.const 65535
      i32.and
      local.tee $l8
      i32.mul
      i32.add
      local.get $l7
      local.get $l8
      i32.mul
      local.tee $l7
      i32.const 16
      i32.shr_u
      i32.add
      local.tee $l4
      local.get $l5
      i32.lt_u
      select
      local.get $l4
      i32.const 16
      i32.shr_u
      i32.add
      local.tee $l5
      local.get $l3
      i32.add
      local.tee $l3
      local.get $l5
      i32.ge_u
      br_if $B3
      local.get $l3
      local.get $p0
      i32.gt_u
      local.get $l4
      i32.const 16
      i32.shl
      local.get $l7
      i32.const 65535
      i32.and
      i32.or
      local.get $p1
      i32.gt_u
      local.get $l3
      local.get $p0
      i32.eq
      i32.and
      i32.or
      i32.const -1
      i32.xor
      local.get $l2
      i32.add
      local.set $l2
    end
    local.get $l2)
  (func $mpn_div_qr_1_invert (type $t7) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    local.get $p1
    i32.const 0
    i32.ne
    call $assert
    i32.const 0
    local.set $l2
    local.get $p1
    local.set $l3
    block $B0
      local.get $p1
      i32.const 16777215
      i32.gt_u
      br_if $B0
      i32.const 0
      local.set $l2
      local.get $p1
      local.set $l4
      loop $L1
        local.get $l2
        i32.const 8
        i32.add
        local.set $l2
        local.get $l4
        i32.const 65536
        i32.lt_u
        local.set $l5
        local.get $l4
        i32.const 8
        i32.shl
        local.tee $l3
        local.set $l4
        local.get $l5
        br_if $L1
      end
    end
    block $B2
      local.get $l3
      i32.const 0
      i32.lt_s
      br_if $B2
      loop $L3
        local.get $l2
        i32.const 1
        i32.add
        local.set $l2
        local.get $l3
        i32.const 1
        i32.shl
        local.tee $l3
        i32.const -1
        i32.gt_s
        br_if $L3
      end
    end
    local.get $p0
    local.get $l2
    i32.store
    local.get $p0
    local.get $p1
    local.get $l2
    i32.shl
    local.tee $l2
    i32.store offset=4
    i32.const 1
    call $assert
    block $B4
      local.get $l2
      i32.const -1
      i32.xor
      local.tee $l3
      local.get $l3
      local.get $l2
      i32.const 16
      i32.shr_u
      local.tee $l4
      i32.div_u
      local.tee $l3
      local.get $l4
      i32.mul
      i32.sub
      i32.const 16
      i32.shl
      i32.const 65535
      i32.or
      local.tee $l4
      local.get $l3
      local.get $l2
      i32.const 65535
      i32.and
      i32.mul
      local.tee $l5
      i32.ge_u
      br_if $B4
      local.get $l3
      i32.const -1
      i32.add
      local.set $p1
      block $B5
        local.get $l4
        local.get $l2
        i32.add
        local.tee $l4
        local.get $l2
        i32.ge_u
        br_if $B5
        local.get $p1
        local.set $l3
        br $B4
      end
      block $B6
        local.get $l4
        local.get $l5
        i32.lt_u
        br_if $B6
        local.get $p1
        local.set $l3
        br $B4
      end
      local.get $l4
      local.get $l2
      i32.add
      local.set $l4
      local.get $l3
      i32.const -2
      i32.add
      local.set $l3
    end
    local.get $p0
    local.get $l4
    local.get $l5
    i32.sub
    local.tee $l4
    i32.const 16
    i32.shr_u
    local.get $l3
    i32.mul
    local.get $l4
    i32.add
    local.tee $l5
    i32.const 16
    i32.shr_u
    local.tee $p1
    local.get $l3
    i32.const 16
    i32.shl
    i32.or
    local.get $l2
    local.get $p1
    i32.const -1
    i32.xor
    i32.mul
    local.get $l4
    i32.const 16
    i32.shl
    i32.add
    i32.const 65535
    i32.add
    local.tee $l3
    local.get $l5
    i32.const 16
    i32.shl
    i32.lt_u
    local.tee $l4
    i32.add
    i32.const 0
    local.get $l2
    local.get $l4
    select
    local.get $l3
    i32.add
    local.get $l2
    i32.ge_u
    i32.add
    i32.store offset=12)
  (func $mpn_div_qr_invert (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    i32.const 0
    local.set $l3
    local.get $p2
    i32.const 0
    i32.gt_s
    call $assert
    block $B0
      block $B1
        block $B2
          local.get $p2
          i32.const -1
          i32.add
          br_table $B2 $B1 $B0
        end
        local.get $p0
        local.get $p1
        i32.load
        call $mpn_div_qr_1_invert
        return
      end
      local.get $p1
      i32.load
      local.set $l4
      local.get $p1
      i32.load offset=4
      local.tee $l5
      i32.const 0
      i32.ne
      call $assert
      local.get $l5
      local.set $p2
      block $B3
        local.get $l5
        i32.const 16777215
        i32.gt_u
        br_if $B3
        i32.const 0
        local.set $l3
        local.get $l5
        local.set $p1
        loop $L4
          local.get $l3
          i32.const 8
          i32.add
          local.set $l3
          local.get $p1
          i32.const 65536
          i32.lt_u
          local.set $l6
          local.get $p1
          i32.const 8
          i32.shl
          local.tee $p2
          local.set $p1
          local.get $l6
          br_if $L4
        end
      end
      block $B5
        local.get $p2
        i32.const 0
        i32.lt_s
        br_if $B5
        loop $L6
          local.get $l3
          i32.const 1
          i32.add
          local.set $l3
          local.get $p2
          i32.const 1
          i32.shl
          local.tee $p2
          i32.const -1
          i32.gt_s
          br_if $L6
        end
      end
      local.get $p0
      local.get $l3
      i32.store
      local.get $p0
      local.get $l4
      local.get $l3
      i32.shl
      local.tee $p2
      i32.store offset=8
      local.get $p0
      local.get $l5
      local.get $l3
      i32.shl
      local.get $l4
      i32.const 1
      i32.shr_u
      local.get $l3
      i32.const -1
      i32.xor
      i32.shr_u
      i32.or
      local.tee $l3
      i32.store offset=4
      local.get $p0
      local.get $l3
      local.get $p2
      call $mpn_invert_3by2
      i32.store offset=12
      return
    end
    local.get $p1
    local.get $p2
    i32.const 2
    i32.shl
    i32.add
    local.tee $l7
    i32.const -8
    i32.add
    i32.load
    local.set $l4
    local.get $l7
    i32.const -4
    i32.add
    i32.load
    local.tee $l5
    i32.const 0
    i32.ne
    call $assert
    local.get $l5
    local.set $p2
    i32.const 0
    local.set $l3
    block $B7
      local.get $l5
      i32.const 16777215
      i32.gt_u
      br_if $B7
      i32.const 0
      local.set $l3
      local.get $l5
      local.set $p1
      loop $L8
        local.get $l3
        i32.const 8
        i32.add
        local.set $l3
        local.get $p1
        i32.const 65536
        i32.lt_u
        local.set $l6
        local.get $p1
        i32.const 8
        i32.shl
        local.tee $p2
        local.set $p1
        local.get $l6
        br_if $L8
      end
    end
    block $B9
      local.get $p2
      i32.const 0
      i32.lt_s
      br_if $B9
      loop $L10
        local.get $l3
        i32.const 1
        i32.add
        local.set $l3
        local.get $p2
        i32.const 1
        i32.shl
        local.tee $p2
        i32.const -1
        i32.gt_s
        br_if $L10
      end
    end
    local.get $p0
    local.get $l3
    i32.store
    block $B11
      block $B12
        local.get $l3
        br_if $B12
        local.get $l4
        local.set $p2
        br $B11
      end
      local.get $l7
      i32.const -12
      i32.add
      i32.load
      i32.const 32
      local.get $l3
      i32.sub
      i32.shr_u
      local.get $l4
      local.get $l3
      i32.shl
      i32.or
      local.set $p2
    end
    local.get $p0
    local.get $p2
    i32.store offset=8
    local.get $p0
    local.get $l5
    local.get $l3
    i32.shl
    local.get $l4
    i32.const 1
    i32.shr_u
    local.get $l3
    i32.const -1
    i32.xor
    i32.shr_u
    i32.or
    local.tee $l3
    i32.store offset=4
    local.get $p0
    local.get $l3
    local.get $p2
    call $mpn_invert_3by2
    i32.store offset=12)
  (func $mpn_div_qr_1_preinv (type $t9) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    block $B0
      block $B1
        block $B2
          local.get $p3
          i32.load
          local.tee $l4
          br_if $B2
          i32.const 0
          local.set $l5
          i32.const 1
          local.set $l6
          i32.const 0
          local.set $l7
          br $B1
        end
        i32.const 1
        local.set $l6
        local.get $p0
        local.set $l5
        block $B3
          local.get $p0
          br_if $B3
          local.get $p2
          i32.const 2
          i32.shl
          local.tee $l7
          i32.const 0
          i32.ne
          call $assert
          local.get $l7
          call $malloc
          local.tee $l5
          i32.eqz
          br_if $B0
          local.get $p2
          i32.eqz
          local.set $l6
          local.get $p3
          i32.load
          local.set $l4
        end
        local.get $p2
        i32.const 0
        i32.gt_s
        call $assert
        local.get $l4
        i32.const 0
        i32.ne
        call $assert
        local.get $l4
        i32.const 32
        i32.lt_u
        call $assert
        i32.const 32
        local.get $l4
        i32.sub
        local.set $l8
        local.get $l5
        local.get $p2
        i32.const 2
        i32.shl
        local.tee $l9
        i32.add
        local.set $l7
        local.get $p1
        local.get $l9
        i32.add
        local.tee $l10
        i32.const -4
        i32.add
        local.tee $l9
        i32.load
        local.tee $l11
        local.get $l4
        i32.shl
        local.set $l12
        block $B4
          local.get $p2
          i32.const -1
          i32.add
          local.tee $p1
          i32.eqz
          br_if $B4
          block $B5
            local.get $p1
            i32.const 1
            i32.and
            i32.eqz
            br_if $B5
            local.get $l7
            i32.const -4
            i32.add
            local.tee $l7
            local.get $l10
            i32.const -8
            i32.add
            local.tee $l9
            i32.load
            local.tee $l10
            local.get $l8
            i32.shr_u
            local.get $l12
            i32.or
            i32.store
            local.get $p2
            i32.const -2
            i32.add
            local.set $p1
            local.get $l10
            local.get $l4
            i32.shl
            local.set $l12
          end
          local.get $p2
          i32.const 2
          i32.eq
          br_if $B4
          local.get $l9
          i32.const -8
          i32.add
          local.set $l9
          local.get $l7
          i32.const -8
          i32.add
          local.set $l7
          loop $L6
            local.get $l7
            i32.const 4
            i32.add
            local.get $l9
            i32.const 4
            i32.add
            i32.load
            local.tee $l10
            local.get $l8
            i32.shr_u
            local.get $l12
            i32.or
            i32.store
            local.get $l7
            local.get $l9
            i32.load
            local.tee $l12
            local.get $l8
            i32.shr_u
            local.get $l10
            local.get $l4
            i32.shl
            i32.or
            i32.store
            local.get $l9
            i32.const -8
            i32.add
            local.set $l9
            local.get $l7
            i32.const -8
            i32.add
            local.set $l7
            local.get $l12
            local.get $l4
            i32.shl
            local.set $l12
            local.get $p1
            i32.const -2
            i32.add
            local.tee $p1
            br_if $L6
          end
          local.get $l7
          i32.const 8
          i32.add
          local.set $l7
        end
        local.get $l7
        i32.const -4
        i32.add
        local.get $l12
        i32.store
        local.get $l11
        local.get $l8
        i32.shr_u
        local.set $l7
        local.get $l5
        local.set $p1
      end
      block $B7
        local.get $p2
        i32.const 1
        i32.lt_s
        br_if $B7
        local.get $p3
        i32.load offset=4
        local.set $l9
        local.get $p3
        i32.load offset=12
        local.tee $l12
        i32.const 16
        i32.shr_u
        local.set $l4
        local.get $l12
        i32.const 65535
        i32.and
        local.set $l12
        block $B8
          local.get $p0
          i32.eqz
          br_if $B8
          local.get $p1
          local.get $p2
          i32.const 2
          i32.shl
          i32.const -4
          i32.add
          local.tee $l10
          i32.add
          local.set $l8
          local.get $p0
          local.get $l10
          i32.add
          local.set $p1
          loop $L9
            i32.const 1
            call $assert
            local.get $p1
            local.get $l7
            local.get $l7
            i32.const 16
            i32.shr_u
            local.tee $p0
            local.get $l12
            i32.mul
            local.tee $l11
            local.get $l7
            i32.const 65535
            i32.and
            local.tee $l10
            local.get $l4
            i32.mul
            i32.add
            local.get $l10
            local.get $l12
            i32.mul
            local.tee $l13
            i32.const 16
            i32.shr_u
            i32.add
            local.tee $l10
            i32.const 16
            i32.shr_u
            i32.add
            local.get $p0
            local.get $l4
            i32.mul
            local.tee $l7
            i32.const 65536
            i32.add
            local.get $l7
            local.get $l10
            local.get $l11
            i32.lt_u
            select
            i32.add
            local.get $l8
            i32.load
            local.tee $l7
            local.get $l10
            i32.const 16
            i32.shl
            local.get $l13
            i32.const 65535
            i32.and
            i32.or
            i32.add
            local.tee $l10
            local.get $l7
            i32.lt_u
            i32.add
            i32.const 1
            i32.add
            local.tee $p0
            local.get $l7
            local.get $p0
            local.get $l9
            i32.mul
            i32.sub
            local.tee $l7
            local.get $l10
            i32.gt_u
            local.tee $l10
            i32.sub
            local.get $l9
            i32.const 0
            local.get $l10
            select
            local.get $l7
            i32.add
            local.tee $l7
            local.get $l9
            i32.ge_u
            local.tee $l10
            i32.add
            i32.store
            local.get $l7
            local.get $l9
            i32.const 0
            local.get $l10
            select
            i32.sub
            local.set $l7
            local.get $l8
            i32.const -4
            i32.add
            local.set $l8
            local.get $p1
            i32.const -4
            i32.add
            local.set $p1
            local.get $p2
            i32.const -1
            i32.add
            local.tee $p2
            br_if $L9
            br $B7
          end
        end
        local.get $p2
        i32.const 2
        i32.shl
        local.get $p1
        i32.add
        i32.const -4
        i32.add
        local.set $l8
        loop $L10
          i32.const 1
          call $assert
          local.get $l9
          i32.const 0
          local.get $l8
          i32.load
          local.tee $p1
          local.get $l7
          local.get $l7
          i32.const 16
          i32.shr_u
          local.tee $p0
          local.get $l12
          i32.mul
          local.tee $l11
          local.get $l7
          i32.const 65535
          i32.and
          local.tee $l10
          local.get $l4
          i32.mul
          i32.add
          local.get $l10
          local.get $l12
          i32.mul
          local.tee $l13
          i32.const 16
          i32.shr_u
          i32.add
          local.tee $l10
          i32.const 16
          i32.shr_u
          i32.add
          local.get $p0
          local.get $l4
          i32.mul
          local.tee $l7
          i32.const 65536
          i32.add
          local.get $l7
          local.get $l10
          local.get $l11
          i32.lt_u
          select
          i32.add
          local.get $p1
          local.get $l10
          i32.const 16
          i32.shl
          local.get $l13
          i32.const 65535
          i32.and
          i32.or
          i32.add
          local.tee $l7
          local.get $p1
          i32.lt_u
          i32.add
          i32.const 1
          i32.add
          local.get $l9
          i32.mul
          i32.sub
          local.tee $p1
          local.get $l7
          i32.gt_u
          select
          local.get $p1
          i32.add
          local.tee $l7
          i32.const 0
          local.get $l9
          local.get $l7
          local.get $l9
          i32.lt_u
          select
          i32.sub
          local.set $l7
          local.get $l8
          i32.const -4
          i32.add
          local.set $l8
          local.get $p2
          i32.const -1
          i32.add
          local.tee $p2
          br_if $L10
        end
      end
      block $B11
        local.get $l6
        br_if $B11
        local.get $l5
        call $free
      end
      local.get $l7
      local.get $p3
      i32.load
      i32.shr_u
      return
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $mpn_div_qr_2_preinv (type $t10) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    local.get $p2
    i32.const 1
    i32.gt_s
    call $assert
    local.get $p3
    i32.load offset=12
    local.set $l4
    local.get $p3
    i32.load offset=8
    local.set $l5
    local.get $p3
    i32.load offset=4
    local.set $l6
    i32.const 0
    local.set $l7
    block $B0
      local.get $p3
      i32.load
      local.tee $l8
      i32.eqz
      br_if $B0
      local.get $p2
      i32.const 0
      i32.gt_s
      call $assert
      i32.const 1
      call $assert
      local.get $l8
      i32.const 32
      i32.lt_u
      call $assert
      i32.const 32
      local.get $l8
      i32.sub
      local.set $l9
      local.get $p1
      local.get $p2
      i32.const 2
      i32.shl
      i32.add
      local.tee $l7
      i32.const -4
      i32.add
      local.tee $l10
      i32.load
      local.tee $l11
      local.get $l8
      i32.shl
      local.set $l12
      block $B1
        block $B2
          local.get $p2
          i32.const -1
          i32.add
          local.tee $l13
          br_if $B2
          local.get $l7
          local.set $p3
          br $B1
        end
        block $B3
          block $B4
            local.get $l13
            i32.const 1
            i32.and
            br_if $B4
            br $B3
          end
          local.get $l7
          i32.const -4
          i32.add
          local.tee $p3
          local.get $l7
          i32.const -8
          i32.add
          local.tee $l10
          i32.load
          local.tee $l7
          local.get $l9
          i32.shr_u
          local.get $l12
          i32.or
          i32.store
          local.get $p2
          i32.const -2
          i32.add
          local.set $l13
          local.get $l7
          local.get $l8
          i32.shl
          local.set $l12
          local.get $p3
          local.set $l7
        end
        local.get $p2
        i32.const 2
        i32.eq
        br_if $B1
        local.get $l10
        i32.const -8
        i32.add
        local.set $p3
        local.get $l7
        i32.const -8
        i32.add
        local.set $l7
        loop $L5
          local.get $l7
          i32.const 4
          i32.add
          local.get $p3
          i32.const 4
          i32.add
          i32.load
          local.tee $l10
          local.get $l9
          i32.shr_u
          local.get $l12
          i32.or
          i32.store
          local.get $l7
          local.get $p3
          i32.load
          local.tee $l12
          local.get $l9
          i32.shr_u
          local.get $l10
          local.get $l8
          i32.shl
          i32.or
          i32.store
          local.get $p3
          i32.const -8
          i32.add
          local.set $p3
          local.get $l7
          i32.const -8
          i32.add
          local.set $l7
          local.get $l12
          local.get $l8
          i32.shl
          local.set $l12
          local.get $l13
          i32.const -2
          i32.add
          local.tee $l13
          br_if $L5
        end
        local.get $l7
        i32.const 8
        i32.add
        local.set $p3
      end
      local.get $p3
      i32.const -4
      i32.add
      local.get $l12
      i32.store
      local.get $l11
      local.get $l9
      i32.shr_u
      local.set $l7
    end
    local.get $p2
    i32.const -1
    i32.add
    local.set $l10
    local.get $l5
    i32.const 16
    i32.shr_u
    local.set $l11
    local.get $l5
    i32.const 65535
    i32.and
    local.set $l14
    local.get $l4
    i32.const 16
    i32.shr_u
    local.set $l15
    local.get $l4
    i32.const 65535
    i32.and
    local.set $l4
    local.get $p2
    i32.const 2
    i32.shl
    local.tee $l12
    i32.const -8
    i32.add
    local.set $p3
    local.get $p1
    local.get $l12
    i32.add
    i32.const -4
    i32.add
    i32.load
    local.set $l12
    loop $L6
      local.get $p1
      local.get $p3
      i32.add
      i32.load
      local.set $l9
      i32.const 1
      call $assert
      i32.const 1
      call $assert
      local.get $l7
      i32.const 16
      i32.shr_u
      local.tee $p2
      local.get $l4
      i32.mul
      local.tee $l16
      local.get $l7
      i32.const 65535
      i32.and
      local.tee $l13
      local.get $l15
      i32.mul
      i32.add
      local.get $l13
      local.get $l4
      i32.mul
      local.tee $l17
      i32.const 16
      i32.shr_u
      i32.add
      local.tee $l13
      i32.const 16
      i32.shr_u
      local.get $l7
      i32.add
      local.get $p2
      local.get $l15
      i32.mul
      local.tee $l7
      i32.const 65536
      i32.add
      local.get $l7
      local.get $l13
      local.get $l16
      i32.lt_u
      select
      i32.add
      local.get $l13
      i32.const 16
      i32.shl
      local.get $l17
      i32.const 65535
      i32.and
      i32.or
      local.tee $l7
      local.get $l12
      i32.add
      local.tee $l13
      local.get $l7
      i32.lt_u
      i32.add
      local.tee $l7
      local.get $l12
      local.get $l9
      local.get $l5
      i32.lt_u
      i32.sub
      local.get $l6
      local.get $l7
      i32.const -1
      i32.xor
      i32.mul
      i32.add
      local.get $l7
      i32.const 65535
      i32.and
      local.tee $l12
      local.get $l11
      i32.mul
      local.tee $p2
      local.get $l7
      i32.const 16
      i32.shr_u
      local.tee $l16
      local.get $l14
      i32.mul
      i32.add
      local.get $l12
      local.get $l14
      i32.mul
      local.tee $l12
      i32.const 16
      i32.shr_u
      i32.add
      local.tee $l7
      i32.const 16
      i32.shr_u
      local.get $l16
      local.get $l11
      i32.mul
      local.tee $l16
      i32.const 65536
      i32.add
      local.get $l16
      local.get $l7
      local.get $p2
      i32.lt_u
      select
      i32.add
      i32.sub
      local.get $l9
      local.get $l5
      i32.sub
      local.tee $l9
      local.get $l7
      i32.const 16
      i32.shl
      local.get $l12
      i32.const 65535
      i32.and
      i32.or
      local.tee $l12
      i32.lt_u
      i32.sub
      local.tee $p2
      local.get $l13
      i32.ge_u
      local.tee $l7
      i32.sub
      i32.const 1
      i32.add
      local.set $l13
      block $B7
        block $B8
          local.get $l6
          i32.const 0
          local.get $l7
          select
          local.get $p2
          i32.add
          local.get $l5
          i32.const 0
          local.get $l7
          select
          local.tee $l7
          local.get $l9
          local.get $l12
          i32.sub
          i32.add
          local.tee $l9
          local.get $l7
          i32.lt_u
          i32.add
          local.tee $l7
          local.get $l6
          i32.ge_u
          br_if $B8
          local.get $l9
          local.set $l12
          br $B7
        end
        block $B9
          local.get $l7
          local.get $l6
          i32.gt_u
          br_if $B9
          local.get $l9
          local.get $l5
          i32.ge_u
          br_if $B9
          local.get $l9
          local.set $l12
          br $B7
        end
        local.get $l9
        local.get $l5
        i32.sub
        local.set $l12
        local.get $l13
        i32.const 1
        i32.add
        local.set $l13
        i32.const -1
        i32.const 0
        local.get $l9
        local.get $l5
        i32.lt_u
        select
        local.get $l6
        i32.sub
        local.get $l7
        i32.add
        local.set $l7
      end
      block $B10
        local.get $p0
        i32.eqz
        br_if $B10
        local.get $p0
        local.get $p3
        i32.add
        local.get $l13
        i32.store
      end
      local.get $p3
      i32.const -4
      i32.add
      local.set $p3
      local.get $l10
      i32.const -1
      i32.add
      local.tee $l10
      i32.const 0
      i32.gt_s
      br_if $L6
    end
    local.get $l7
    local.set $l5
    block $B11
      local.get $l8
      i32.eqz
      br_if $B11
      local.get $l12
      i32.const 32
      local.get $l8
      i32.sub
      i32.shl
      i32.eqz
      call $assert
      local.get $l7
      local.get $l8
      i32.shr_u
      local.set $l5
    end
    local.get $p1
    local.get $l5
    i32.store offset=4
    local.get $p1
    local.get $l7
    i32.const 1
    i32.shl
    local.get $l8
    i32.const -1
    i32.xor
    i32.shl
    local.get $l12
    local.get $l8
    i32.shr_u
    i32.or
    i32.store)
  (func $mpn_div_qr_pi1 (type $t11) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (param $p5 i32) (param $p6 i32)
    (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i32) (local $l20 i32) (local $l21 i32) (local $l22 i32) (local $l23 i32) (local $l24 i32) (local $l25 i32) (local $l26 i32) (local $l27 i32) (local $l28 i32) (local $l29 i32) (local $l30 i32) (local $l31 i32) (local $l32 i32) (local $l33 i32) (local $l34 i32) (local $l35 i32) (local $l36 i32)
    local.get $p5
    i32.const 2
    i32.gt_s
    local.tee $l7
    call $assert
    local.get $p2
    local.get $p5
    i32.ge_s
    call $assert
    local.get $p4
    local.get $p5
    i32.const -2
    i32.add
    local.tee $l8
    i32.const 2
    i32.shl
    local.tee $l9
    i32.add
    i32.load
    local.set $l10
    local.get $p4
    local.get $p5
    i32.const -1
    i32.add
    local.tee $l11
    i32.const 2
    i32.shl
    local.tee $l12
    i32.add
    i32.load
    local.tee $l13
    i32.const 31
    i32.shr_u
    call $assert
    local.get $p1
    local.get $p2
    i32.const 2
    i32.shl
    local.get $p5
    i32.const 2
    i32.shl
    i32.sub
    i32.add
    local.set $l14
    local.get $p5
    i32.const 0
    i32.gt_s
    local.set $l15
    local.get $p6
    i32.const 16
    i32.shr_u
    local.set $l16
    local.get $p6
    i32.const 65535
    i32.and
    local.set $l17
    local.get $p2
    local.get $p5
    i32.sub
    local.set $l18
    local.get $l11
    i32.const 1
    i32.and
    local.set $l19
    i32.const 0
    local.get $l11
    i32.const -2
    i32.and
    i32.sub
    local.set $l20
    local.get $p1
    local.get $l9
    i32.add
    local.set $l21
    local.get $p1
    local.get $l12
    i32.add
    local.set $l22
    local.get $l10
    i32.const 16
    i32.shr_u
    local.set $l23
    local.get $l10
    i32.const 65535
    i32.and
    local.set $l24
    loop $L0
      local.get $l22
      local.get $l18
      i32.const 2
      i32.shl
      local.tee $l25
      i32.add
      local.tee $l26
      i32.load
      local.set $p2
      block $B1
        block $B2
          local.get $p3
          local.get $l13
          i32.ne
          br_if $B2
          local.get $p2
          local.get $l10
          i32.ne
          br_if $B2
          local.get $l15
          call $assert
          i32.const 0
          local.set $p2
          i32.const 0
          local.set $l11
          local.get $p5
          local.set $l27
          loop $L3
            local.get $p4
            local.get $p2
            i32.add
            i32.load
            local.set $p6
            i32.const 1
            call $assert
            local.get $l14
            local.get $p2
            i32.add
            local.tee $l9
            local.get $l9
            i32.load
            local.tee $l28
            local.get $p6
            i32.const 65535
            i32.and
            i32.const 65535
            i32.mul
            local.tee $l9
            i32.const 65535
            i32.and
            local.get $l11
            i32.add
            local.get $p6
            i32.const 16
            i32.shr_u
            i32.const 65535
            i32.mul
            local.tee $p6
            local.get $l9
            i32.add
            local.get $l9
            i32.const 16
            i32.shr_u
            i32.add
            local.tee $l9
            i32.const 16
            i32.shl
            i32.add
            local.tee $l12
            i32.sub
            i32.store
            local.get $p6
            i32.const 65536
            i32.add
            local.get $p6
            local.get $l9
            local.get $p6
            i32.lt_u
            select
            local.get $l9
            i32.const 16
            i32.shr_u
            i32.add
            local.get $l12
            local.get $l11
            i32.lt_u
            i32.add
            local.get $l28
            local.get $l12
            i32.lt_u
            i32.add
            local.set $l11
            local.get $p2
            i32.const 4
            i32.add
            local.set $p2
            local.get $l27
            i32.const -1
            i32.add
            local.tee $l27
            br_if $L3
          end
          local.get $l26
          i32.load
          local.set $p3
          i32.const -1
          local.set $l29
          br $B1
        end
        i32.const 1
        call $assert
        local.get $l21
        local.get $l25
        i32.add
        local.tee $l30
        i32.load
        local.set $l11
        i32.const 1
        call $assert
        local.get $p3
        i32.const 16
        i32.shr_u
        local.tee $l9
        local.get $l17
        i32.mul
        local.tee $l12
        local.get $p3
        i32.const 65535
        i32.and
        local.tee $p6
        local.get $l16
        i32.mul
        i32.add
        local.get $p6
        local.get $l17
        i32.mul
        local.tee $l27
        i32.const 16
        i32.shr_u
        i32.add
        local.tee $p6
        i32.const 16
        i32.shr_u
        local.get $p3
        i32.add
        local.get $l9
        local.get $l16
        i32.mul
        local.tee $l9
        i32.const 65536
        i32.add
        local.get $l9
        local.get $p6
        local.get $l12
        i32.lt_u
        select
        i32.add
        local.get $p6
        i32.const 16
        i32.shl
        local.get $l27
        i32.const 65535
        i32.and
        i32.or
        local.tee $p6
        local.get $p2
        i32.add
        local.tee $l9
        local.get $p6
        i32.lt_u
        i32.add
        local.tee $p6
        local.get $l13
        local.get $p6
        i32.const -1
        i32.xor
        i32.mul
        local.get $p2
        i32.add
        local.get $p6
        i32.const 65535
        i32.and
        local.tee $p2
        local.get $l23
        i32.mul
        local.tee $l12
        local.get $p6
        i32.const 16
        i32.shr_u
        local.tee $p6
        local.get $l24
        i32.mul
        i32.add
        local.get $p2
        local.get $l24
        i32.mul
        local.tee $l27
        i32.const 16
        i32.shr_u
        i32.add
        local.tee $p2
        i32.const 16
        i32.shr_u
        local.get $p6
        local.get $l23
        i32.mul
        local.tee $p6
        i32.const 65536
        i32.add
        local.get $p6
        local.get $p2
        local.get $l12
        i32.lt_u
        select
        i32.add
        i32.sub
        local.get $l11
        local.get $l10
        i32.lt_u
        i32.sub
        local.get $l11
        local.get $l10
        i32.sub
        local.tee $p6
        local.get $p2
        i32.const 16
        i32.shl
        local.get $l27
        i32.const 65535
        i32.and
        i32.or
        local.tee $l11
        i32.lt_u
        i32.sub
        local.tee $l12
        local.get $l9
        i32.ge_u
        local.tee $p2
        i32.sub
        i32.const 1
        i32.add
        local.set $l29
        block $B4
          block $B5
            local.get $l13
            i32.const 0
            local.get $p2
            select
            local.get $l12
            i32.add
            local.get $l10
            i32.const 0
            local.get $p2
            select
            local.tee $l9
            local.get $p6
            local.get $l11
            i32.sub
            i32.add
            local.tee $p2
            local.get $l9
            i32.lt_u
            i32.add
            local.tee $l31
            local.get $l13
            i32.ge_u
            br_if $B5
            local.get $p2
            local.set $l32
            br $B4
          end
          block $B6
            local.get $l31
            local.get $l13
            i32.gt_u
            br_if $B6
            local.get $p2
            local.get $l10
            i32.ge_u
            br_if $B6
            local.get $p2
            local.set $l32
            br $B4
          end
          local.get $p2
          local.get $l10
          i32.sub
          local.set $l32
          local.get $l29
          i32.const 1
          i32.add
          local.set $l29
          i32.const -1
          i32.const 0
          local.get $p2
          local.get $l10
          i32.lt_u
          select
          local.get $l13
          i32.sub
          local.get $l31
          i32.add
          local.set $l31
        end
        local.get $l7
        call $assert
        local.get $l29
        i32.const 16
        i32.shr_u
        local.set $l27
        local.get $l29
        i32.const 65535
        i32.and
        local.set $l28
        i32.const 0
        local.set $p6
        local.get $l8
        local.set $p3
        local.get $p4
        local.set $l11
        local.get $p1
        local.get $l25
        i32.add
        local.tee $l33
        local.set $p2
        loop $L7
          local.get $l11
          i32.load
          local.set $l9
          i32.const 1
          call $assert
          local.get $p2
          local.get $p2
          i32.load
          local.tee $l26
          local.get $l9
          i32.const 65535
          i32.and
          local.tee $l12
          local.get $l28
          i32.mul
          local.tee $l34
          i32.const 65535
          i32.and
          local.get $p6
          i32.add
          local.get $l9
          i32.const 16
          i32.shr_u
          local.tee $l35
          local.get $l28
          i32.mul
          local.tee $l36
          local.get $l12
          local.get $l27
          i32.mul
          i32.add
          local.get $l34
          i32.const 16
          i32.shr_u
          i32.add
          local.tee $l9
          i32.const 16
          i32.shl
          i32.add
          local.tee $l12
          i32.sub
          i32.store
          local.get $l35
          local.get $l27
          i32.mul
          local.tee $l34
          i32.const 65536
          i32.add
          local.get $l34
          local.get $l9
          local.get $l36
          i32.lt_u
          select
          local.get $l9
          i32.const 16
          i32.shr_u
          i32.add
          local.get $l12
          local.get $p6
          i32.lt_u
          i32.add
          local.get $l26
          local.get $l12
          i32.lt_u
          i32.add
          local.set $p6
          local.get $p2
          i32.const 4
          i32.add
          local.set $p2
          local.get $l11
          i32.const 4
          i32.add
          local.set $l11
          local.get $p3
          i32.const -1
          i32.add
          local.tee $p3
          br_if $L7
        end
        local.get $l30
        local.get $l32
        local.get $p6
        i32.sub
        i32.store
        local.get $l31
        local.get $l32
        local.get $p6
        i32.lt_u
        local.tee $p2
        i32.sub
        local.set $p3
        local.get $l31
        local.get $p2
        i32.ge_u
        br_if $B1
        i32.const 0
        local.set $l11
        block $B8
          local.get $p5
          i32.const 2
          i32.lt_s
          br_if $B8
          block $B9
            block $B10
              local.get $l8
              br_if $B10
              i32.const 0
              local.set $p2
              i32.const 0
              local.set $l11
              br $B9
            end
            i32.const 0
            local.set $l9
            local.get $l14
            local.set $p2
            local.get $p4
            local.set $p6
            i32.const 0
            local.set $l11
            loop $L11
              local.get $p2
              local.get $p2
              i32.load
              local.tee $l12
              local.get $l11
              i32.add
              local.tee $l11
              local.get $p6
              i32.load
              i32.add
              local.tee $l27
              i32.store
              local.get $p2
              i32.const 4
              i32.add
              local.tee $l28
              local.get $l28
              i32.load
              local.tee $l28
              local.get $l27
              local.get $l11
              i32.lt_u
              local.get $l11
              local.get $l12
              i32.lt_u
              i32.add
              i32.add
              local.tee $l11
              local.get $p6
              i32.const 4
              i32.add
              i32.load
              i32.add
              local.tee $l12
              i32.store
              local.get $l12
              local.get $l11
              i32.lt_u
              local.get $l11
              local.get $l28
              i32.lt_u
              i32.add
              local.set $l11
              local.get $p2
              i32.const 8
              i32.add
              local.set $p2
              local.get $p6
              i32.const 8
              i32.add
              local.set $p6
              local.get $l20
              local.get $l9
              i32.const -2
              i32.add
              local.tee $l9
              i32.ne
              br_if $L11
            end
            i32.const 0
            local.get $l9
            i32.sub
            local.set $p2
          end
          local.get $l19
          i32.eqz
          br_if $B8
          local.get $l33
          local.get $p2
          i32.const 2
          i32.shl
          local.tee $p6
          i32.add
          local.tee $p2
          local.get $p2
          i32.load
          local.tee $l9
          local.get $l11
          i32.add
          local.tee $p2
          local.get $p4
          local.get $p6
          i32.add
          i32.load
          i32.add
          local.tee $p6
          i32.store
          local.get $p6
          local.get $p2
          i32.lt_u
          local.get $p2
          local.get $l9
          i32.lt_u
          i32.add
          local.set $l11
        end
        local.get $l29
        i32.const -1
        i32.add
        local.set $l29
        local.get $p3
        local.get $l13
        i32.add
        local.get $l11
        i32.add
        local.set $p3
      end
      block $B12
        local.get $p0
        i32.eqz
        br_if $B12
        local.get $p0
        local.get $l25
        i32.add
        local.get $l29
        i32.store
      end
      local.get $l14
      i32.const -4
      i32.add
      local.set $l14
      local.get $l18
      i32.const 0
      i32.gt_s
      local.set $p2
      local.get $l18
      i32.const -1
      i32.add
      local.set $l18
      local.get $p2
      br_if $L0
    end
    local.get $l22
    local.get $p3
    i32.store)
  (func $mpn_div_qr_preinv (type $t12) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (param $p5 i32)
    (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32)
    local.get $p4
    i32.const 0
    i32.gt_s
    local.tee $l6
    call $assert
    local.get $p2
    local.get $p4
    i32.ge_s
    call $assert
    block $B0
      block $B1
        block $B2
          local.get $p4
          i32.const -1
          i32.add
          br_table $B2 $B1 $B0
        end
        local.get $p1
        local.get $p0
        local.get $p1
        local.get $p2
        local.get $p5
        call $mpn_div_qr_1_preinv
        i32.store
        return
      end
      local.get $p0
      local.get $p1
      local.get $p2
      local.get $p5
      call $mpn_div_qr_2_preinv
      return
    end
    local.get $p5
    i32.load offset=4
    local.get $p3
    local.get $p4
    i32.const 2
    i32.shl
    i32.add
    local.tee $l7
    i32.const -4
    i32.add
    i32.load
    i32.eq
    call $assert
    local.get $p5
    i32.load offset=8
    local.get $l7
    i32.const -8
    i32.add
    i32.load
    i32.eq
    call $assert
    local.get $p5
    i32.load offset=4
    i32.const 31
    i32.shr_u
    call $assert
    block $B3
      local.get $p5
      i32.load
      local.tee $l7
      i32.eqz
      br_if $B3
      local.get $p2
      i32.const 0
      i32.gt_s
      call $assert
      i32.const 1
      call $assert
      local.get $l7
      i32.const 32
      i32.lt_u
      local.tee $l8
      call $assert
      i32.const 32
      local.get $l7
      i32.sub
      local.set $l9
      local.get $p1
      local.get $p2
      i32.const 2
      i32.shl
      i32.add
      local.tee $l10
      i32.const -4
      i32.add
      local.tee $l11
      i32.load
      local.tee $l12
      local.get $l7
      i32.shl
      local.set $l13
      block $B4
        block $B5
          local.get $p2
          i32.const -1
          i32.add
          local.tee $l14
          br_if $B5
          local.get $l10
          local.set $l15
          br $B4
        end
        block $B6
          block $B7
            local.get $l14
            i32.const 1
            i32.and
            br_if $B7
            br $B6
          end
          local.get $l10
          i32.const -4
          i32.add
          local.tee $l15
          local.get $l10
          i32.const -8
          i32.add
          local.tee $l11
          i32.load
          local.tee $l10
          local.get $l9
          i32.shr_u
          local.get $l13
          i32.or
          i32.store
          local.get $p2
          i32.const -2
          i32.add
          local.set $l14
          local.get $l10
          local.get $l7
          i32.shl
          local.set $l13
          local.get $l15
          local.set $l10
        end
        local.get $p2
        i32.const 2
        i32.eq
        br_if $B4
        local.get $l11
        i32.const -8
        i32.add
        local.set $l15
        local.get $l10
        i32.const -8
        i32.add
        local.set $l10
        loop $L8
          local.get $l10
          i32.const 4
          i32.add
          local.get $l15
          i32.const 4
          i32.add
          i32.load
          local.tee $l11
          local.get $l9
          i32.shr_u
          local.get $l13
          i32.or
          i32.store
          local.get $l10
          local.get $l15
          i32.load
          local.tee $l13
          local.get $l9
          i32.shr_u
          local.get $l11
          local.get $l7
          i32.shl
          i32.or
          i32.store
          local.get $l15
          i32.const -8
          i32.add
          local.set $l15
          local.get $l10
          i32.const -8
          i32.add
          local.set $l10
          local.get $l13
          local.get $l7
          i32.shl
          local.set $l13
          local.get $l14
          i32.const -2
          i32.add
          local.tee $l14
          br_if $L8
        end
        local.get $l10
        i32.const 8
        i32.add
        local.set $l15
      end
      local.get $l15
      i32.const -4
      i32.add
      local.get $l13
      i32.store
      local.get $p0
      local.get $p1
      local.get $p2
      local.get $l12
      local.get $l9
      i32.shr_u
      local.get $p3
      local.get $p4
      local.get $p5
      i32.load offset=12
      call $mpn_div_qr_pi1
      local.get $l6
      call $assert
      i32.const 1
      call $assert
      local.get $l8
      call $assert
      local.get $p4
      i32.const -2
      i32.add
      local.set $l14
      local.get $p1
      i32.load
      local.tee $l11
      local.get $l7
      i32.shr_u
      local.set $l10
      block $B9
        local.get $p4
        i32.const -1
        i32.add
        local.tee $l15
        i32.const 3
        i32.and
        local.tee $p2
        i32.eqz
        br_if $B9
        local.get $p1
        local.set $p4
        loop $L10
          local.get $p4
          local.get $p4
          i32.const 4
          i32.add
          local.tee $p1
          i32.load
          local.tee $l13
          local.get $l9
          i32.shl
          local.get $l10
          i32.add
          i32.store
          local.get $l15
          i32.const -1
          i32.add
          local.set $l15
          local.get $l13
          local.get $l7
          i32.shr_u
          local.set $l10
          local.get $p1
          local.set $p4
          local.get $p2
          i32.const -1
          i32.add
          local.tee $p2
          br_if $L10
        end
      end
      block $B11
        local.get $l14
        i32.const 3
        i32.lt_u
        br_if $B11
        loop $L12
          local.get $p1
          local.get $p1
          i32.const 4
          i32.add
          local.tee $p4
          i32.load
          local.tee $p2
          local.get $l9
          i32.shl
          local.get $l10
          i32.add
          i32.store
          local.get $p1
          i32.const 8
          i32.add
          local.tee $l10
          local.get $p1
          i32.const 12
          i32.add
          local.tee $l13
          i32.load
          local.tee $l14
          local.get $l9
          i32.shl
          local.get $l10
          i32.load
          local.tee $l10
          local.get $l7
          i32.shr_u
          i32.or
          i32.store
          local.get $p4
          local.get $l10
          local.get $l9
          i32.shl
          local.get $p2
          local.get $l7
          i32.shr_u
          i32.or
          i32.store
          local.get $l13
          local.get $p1
          i32.const 16
          i32.add
          local.tee $p1
          i32.load
          local.tee $l10
          local.get $l9
          i32.shl
          local.get $l14
          local.get $l7
          i32.shr_u
          i32.or
          i32.store
          local.get $l10
          local.get $l7
          i32.shr_u
          local.set $l10
          local.get $l15
          i32.const -4
          i32.add
          local.tee $l15
          br_if $L12
        end
      end
      local.get $p1
      local.get $l10
      i32.store
      local.get $l11
      local.get $l9
      i32.shl
      i32.eqz
      call $assert
      return
    end
    local.get $p0
    local.get $p1
    local.get $p2
    i32.const 0
    local.get $p3
    local.get $p4
    local.get $p5
    i32.load offset=12
    call $mpn_div_qr_pi1)
  (func $mpn_div_qr (type $t13) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l5
    global.set $__stack_pointer
    local.get $p4
    i32.const 0
    i32.gt_s
    local.tee $l6
    call $assert
    local.get $p2
    local.get $p4
    i32.ge_s
    call $assert
    local.get $l5
    local.get $p3
    local.get $p4
    call $mpn_div_qr_invert
    block $B0
      block $B1
        block $B2
          block $B3
            local.get $p4
            i32.const 3
            i32.lt_s
            br_if $B3
            local.get $l5
            i32.load
            local.tee $l7
            br_if $B2
          end
          local.get $p0
          local.get $p1
          local.get $p2
          local.get $p3
          local.get $p4
          local.get $l5
          call $mpn_div_qr_preinv
          br $B1
        end
        local.get $p4
        i32.const 2
        i32.shl
        local.tee $l8
        i32.const 0
        i32.ne
        call $assert
        local.get $l8
        call $malloc
        local.tee $l9
        i32.eqz
        br_if $B0
        local.get $l6
        call $assert
        i32.const 1
        call $assert
        local.get $l7
        i32.const 32
        i32.lt_u
        call $assert
        i32.const 32
        local.get $l7
        i32.sub
        local.set $l10
        local.get $l9
        local.get $l8
        i32.add
        local.set $l11
        local.get $p3
        local.get $l8
        i32.add
        local.tee $l8
        i32.const -4
        i32.add
        local.tee $p3
        i32.load
        local.tee $l12
        local.get $l7
        i32.shl
        local.set $l6
        block $B4
          local.get $p4
          i32.const -1
          i32.add
          local.tee $l13
          i32.const 1
          i32.and
          i32.eqz
          br_if $B4
          local.get $l11
          i32.const -4
          i32.add
          local.tee $l11
          local.get $l8
          i32.const -8
          i32.add
          local.tee $p3
          i32.load
          local.tee $l8
          local.get $l10
          i32.shr_u
          local.get $l6
          i32.or
          i32.store
          local.get $p4
          i32.const -2
          i32.add
          local.set $l13
          local.get $l8
          local.get $l7
          i32.shl
          local.set $l6
        end
        local.get $p3
        i32.const -8
        i32.add
        local.set $l8
        local.get $l11
        i32.const -8
        i32.add
        local.set $p3
        loop $L5
          local.get $p3
          i32.const 4
          i32.add
          local.get $l8
          i32.const 4
          i32.add
          i32.load
          local.tee $l11
          local.get $l10
          i32.shr_u
          local.get $l6
          i32.or
          i32.store
          local.get $p3
          local.get $l8
          i32.load
          local.tee $l6
          local.get $l10
          i32.shr_u
          local.get $l11
          local.get $l7
          i32.shl
          i32.or
          i32.store
          local.get $l8
          i32.const -8
          i32.add
          local.set $l8
          local.get $p3
          i32.const -8
          i32.add
          local.set $p3
          local.get $l6
          local.get $l7
          i32.shl
          local.set $l6
          local.get $l13
          i32.const -2
          i32.add
          local.tee $l13
          br_if $L5
        end
        local.get $p3
        i32.const 4
        i32.add
        local.get $l6
        i32.store
        local.get $l12
        local.get $l10
        i32.shr_u
        i32.eqz
        call $assert
        local.get $p0
        local.get $p1
        local.get $p2
        local.get $l9
        local.get $p4
        local.get $l5
        call $mpn_div_qr_preinv
        local.get $l9
        call $free
      end
      local.get $l5
      i32.const 16
      i32.add
      global.set $__stack_pointer
      return
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $mpz_realloc (type $t4) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    local.get $p1
    i32.const 1
    local.get $p1
    i32.const 1
    i32.gt_s
    select
    local.set $p1
    block $B0
      block $B1
        block $B2
          local.get $p0
          i32.load
          i32.eqz
          br_if $B2
          local.get $p0
          i32.load offset=8
          local.set $l2
          i32.const 1
          call $assert
          local.get $l2
          local.get $p1
          i32.const 2
          i32.shl
          call $realloc
          local.tee $l2
          br_if $B1
          i32.const 1052
          call $gmp_die
          unreachable
        end
        local.get $p1
        i32.const 2
        i32.shl
        local.tee $l2
        i32.const 0
        i32.ne
        call $assert
        local.get $l2
        call $malloc
        local.tee $l2
        i32.eqz
        br_if $B0
      end
      local.get $p0
      local.get $l2
      i32.store offset=8
      local.get $p0
      local.get $p1
      i32.store
      block $B3
        local.get $p0
        i32.load offset=4
        local.tee $l3
        local.get $l3
        i32.const 31
        i32.shr_s
        local.tee $l3
        i32.xor
        local.get $l3
        i32.sub
        local.get $p1
        i32.le_u
        br_if $B3
        local.get $p0
        i32.const 0
        i32.store offset=4
      end
      local.get $l2
      return
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $mpz_add_ui (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l3
    global.set $__stack_pointer
    i32.const 1156
    local.set $l4
    local.get $l3
    i32.const 1156
    i32.store offset=12
    local.get $l3
    i32.const 0
    i32.store offset=4
    block $B0
      block $B1
        block $B2
          local.get $p2
          i32.eqz
          br_if $B2
          local.get $l3
          i32.const 1
          i32.store offset=8
          i32.const 1
          call $assert
          i32.const 4
          call $malloc
          local.tee $l4
          i32.eqz
          br_if $B0
          local.get $l4
          local.get $p2
          i32.store
          local.get $l3
          i32.const 1
          i32.store offset=4
          local.get $l3
          local.get $l4
          i32.store offset=12
          br $B1
        end
        local.get $l3
        i32.const 0
        i32.store offset=8
      end
      block $B3
        block $B4
          local.get $p1
          i32.load offset=4
          i32.const 0
          i32.lt_s
          br_if $B4
          local.get $p0
          local.get $p1
          local.get $l3
          i32.const 4
          i32.add
          call $mpz_abs_add
          local.set $l5
          br $B3
        end
        local.get $p0
        local.get $p1
        local.get $l3
        i32.const 4
        i32.add
        call $mpz_abs_sub
        local.set $l5
      end
      local.get $p0
      i32.const 0
      local.get $l5
      i32.sub
      local.get $l5
      local.get $p1
      i32.load offset=4
      i32.const 0
      i32.lt_s
      select
      i32.store offset=4
      block $B5
        local.get $p2
        i32.eqz
        br_if $B5
        local.get $l4
        call $free
      end
      local.get $l3
      i32.const 16
      i32.add
      global.set $__stack_pointer
      return
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $mpz_abs_add (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32)
    block $B0
      block $B1
        local.get $p1
        i32.load offset=4
        local.tee $l3
        local.get $l3
        i32.const 31
        i32.shr_s
        local.tee $l3
        i32.xor
        local.get $l3
        i32.sub
        local.tee $l3
        local.get $p2
        i32.load offset=4
        local.tee $l4
        local.get $l4
        i32.const 31
        i32.shr_s
        local.tee $l4
        i32.xor
        local.get $l4
        i32.sub
        local.tee $l5
        i32.lt_u
        br_if $B1
        local.get $l5
        local.set $l6
        local.get $l3
        local.set $l5
        local.get $p2
        local.set $l3
        local.get $p1
        local.set $p2
        br $B0
      end
      local.get $l3
      local.set $l6
      local.get $p1
      local.set $l3
    end
    block $B2
      block $B3
        local.get $l5
        local.get $p0
        i32.load
        i32.lt_s
        br_if $B3
        local.get $p0
        local.get $l5
        i32.const 1
        i32.add
        call $mpz_realloc
        local.set $l7
        br $B2
      end
      local.get $p0
      i32.load offset=8
      local.set $l7
    end
    local.get $l3
    i32.load offset=8
    local.set $l8
    local.get $p2
    i32.load offset=8
    local.set $l9
    local.get $l5
    local.get $l6
    i32.ge_u
    call $assert
    block $B4
      block $B5
        local.get $l6
        br_if $B5
        i32.const 0
        local.set $l3
        br $B4
      end
      local.get $l6
      i32.const 1
      i32.and
      local.set $l10
      block $B6
        block $B7
          local.get $l6
          i32.const 1
          i32.ne
          br_if $B7
          i32.const 0
          local.set $l4
          i32.const 0
          local.set $l3
          br $B6
        end
        local.get $l6
        i32.const 2147483646
        i32.and
        local.set $l11
        i32.const 0
        local.set $l4
        local.get $l7
        local.set $p2
        local.get $l8
        local.set $p1
        local.get $l9
        local.set $p0
        i32.const 0
        local.set $l3
        loop $L8
          local.get $p2
          local.get $p0
          i32.load
          local.tee $l12
          local.get $l3
          i32.add
          local.tee $l3
          local.get $p1
          i32.load
          i32.add
          local.tee $l13
          i32.store
          local.get $p2
          i32.const 4
          i32.add
          local.get $p0
          i32.const 4
          i32.add
          i32.load
          local.tee $l14
          local.get $l13
          local.get $l3
          i32.lt_u
          local.get $l3
          local.get $l12
          i32.lt_u
          i32.add
          i32.add
          local.tee $l3
          local.get $p1
          i32.const 4
          i32.add
          i32.load
          i32.add
          local.tee $l12
          i32.store
          local.get $l12
          local.get $l3
          i32.lt_u
          local.get $l3
          local.get $l14
          i32.lt_u
          i32.add
          local.set $l3
          local.get $p2
          i32.const 8
          i32.add
          local.set $p2
          local.get $p1
          i32.const 8
          i32.add
          local.set $p1
          local.get $p0
          i32.const 8
          i32.add
          local.set $p0
          local.get $l11
          local.get $l4
          i32.const 2
          i32.add
          local.tee $l4
          i32.ne
          br_if $L8
        end
      end
      local.get $l10
      i32.eqz
      br_if $B4
      local.get $l7
      local.get $l4
      i32.const 2
      i32.shl
      local.tee $p2
      i32.add
      local.get $l9
      local.get $p2
      i32.add
      i32.load
      local.tee $p0
      local.get $l3
      i32.add
      local.tee $p1
      local.get $l8
      local.get $p2
      i32.add
      i32.load
      i32.add
      local.tee $p2
      i32.store
      local.get $p2
      local.get $p1
      i32.lt_u
      local.get $p1
      local.get $p0
      i32.lt_u
      i32.add
      local.set $l3
    end
    block $B9
      local.get $l5
      local.get $l6
      i32.le_u
      br_if $B9
      i32.const 1
      call $assert
      local.get $l5
      local.get $l6
      i32.sub
      local.tee $p1
      i32.const 1
      i32.and
      local.set $l14
      local.get $l9
      local.get $l6
      i32.const 2
      i32.shl
      local.tee $p2
      i32.add
      local.set $l11
      local.get $l7
      local.get $p2
      i32.add
      local.set $l9
      block $B10
        block $B11
          local.get $l5
          local.get $l6
          i32.const 1
          i32.add
          i32.ne
          br_if $B11
          i32.const 0
          local.set $p0
          br $B10
        end
        local.get $p1
        i32.const 2147483646
        i32.and
        local.set $l13
        i32.const 0
        local.set $p0
        local.get $l9
        local.set $p2
        local.get $l11
        local.set $p1
        loop $L12
          local.get $p2
          local.get $p1
          i32.load
          local.tee $l4
          local.get $l3
          i32.add
          local.tee $l3
          i32.store
          local.get $p2
          i32.const 4
          i32.add
          local.get $p1
          i32.const 4
          i32.add
          i32.load
          local.tee $l12
          local.get $l3
          local.get $l4
          i32.lt_u
          i32.add
          local.tee $l3
          i32.store
          local.get $l3
          local.get $l12
          i32.lt_u
          local.set $l3
          local.get $p2
          i32.const 8
          i32.add
          local.set $p2
          local.get $p1
          i32.const 8
          i32.add
          local.set $p1
          local.get $l13
          local.get $p0
          i32.const 2
          i32.add
          local.tee $p0
          i32.ne
          br_if $L12
        end
      end
      local.get $l14
      i32.eqz
      br_if $B9
      local.get $l9
      local.get $p0
      i32.const 2
      i32.shl
      local.tee $p2
      i32.add
      local.get $l11
      local.get $p2
      i32.add
      i32.load
      local.tee $p2
      local.get $l3
      i32.add
      local.tee $p1
      i32.store
      local.get $p1
      local.get $p2
      i32.lt_u
      local.set $l3
    end
    local.get $l7
    local.get $l5
    i32.const 2
    i32.shl
    i32.add
    local.get $l3
    i32.store
    local.get $l3
    local.get $l5
    i32.add)
  (func $mpz_abs_sub (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32)
    local.get $p2
    i32.load offset=8
    local.set $l3
    local.get $p1
    i32.load offset=8
    local.set $l4
    block $B0
      block $B1
        block $B2
          block $B3
            local.get $p1
            i32.load offset=4
            local.tee $l5
            local.get $l5
            i32.const 31
            i32.shr_s
            local.tee $l6
            i32.xor
            local.get $l6
            i32.sub
            local.tee $l7
            local.get $p2
            i32.load offset=4
            local.tee $l8
            local.get $l8
            i32.const 31
            i32.shr_s
            local.tee $l6
            i32.xor
            local.get $l6
            i32.sub
            local.tee $l9
            i32.ne
            br_if $B3
            local.get $l7
            i32.const 1
            i32.add
            local.set $l10
            local.get $l4
            local.get $l7
            i32.const 2
            i32.shl
            i32.const -4
            i32.add
            local.tee $l11
            i32.add
            local.set $l6
            local.get $l3
            local.get $l11
            i32.add
            local.set $l11
            loop $L4
              block $B5
                local.get $l10
                i32.const -1
                i32.add
                local.tee $l10
                i32.const 1
                i32.ge_s
                br_if $B5
                i32.const 0
                return
              end
              local.get $l11
              i32.load
              local.set $l12
              local.get $l6
              i32.load
              local.set $l13
              local.get $l6
              i32.const -4
              i32.add
              local.set $l6
              local.get $l11
              i32.const -4
              i32.add
              local.set $l11
              local.get $l13
              local.get $l12
              i32.eq
              br_if $L4
            end
            local.get $l13
            local.get $l12
            i32.le_u
            br_if $B2
            br $B1
          end
          local.get $l7
          local.get $l9
          i32.ge_u
          br_if $B1
        end
        block $B6
          block $B7
            local.get $l9
            local.get $p0
            i32.load
            i32.le_s
            br_if $B7
            local.get $p0
            local.get $l9
            call $mpz_realloc
            local.set $l14
            local.get $p1
            i32.load offset=8
            local.set $l4
            local.get $p2
            i32.load offset=8
            local.set $l3
            br $B6
          end
          local.get $p0
          i32.load offset=8
          local.set $l14
        end
        local.get $l9
        local.get $l7
        i32.ge_u
        call $assert
        block $B8
          block $B9
            local.get $l5
            br_if $B9
            i32.const 0
            local.set $l6
            br $B8
          end
          local.get $l7
          i32.const 1
          i32.and
          local.set $l5
          block $B10
            block $B11
              local.get $l7
              i32.const 1
              i32.ne
              br_if $B11
              i32.const 0
              local.set $l13
              i32.const 0
              local.set $l6
              br $B10
            end
            local.get $l7
            i32.const 2147483646
            i32.and
            local.set $l15
            i32.const 0
            local.set $l13
            local.get $l14
            local.set $l11
            local.get $l4
            local.set $l10
            local.get $l3
            local.set $l12
            i32.const 0
            local.set $l6
            loop $L12
              local.get $l11
              local.get $l12
              i32.load
              local.tee $p2
              local.get $l10
              i32.load
              local.tee $p1
              local.get $l6
              i32.add
              local.tee $l6
              i32.sub
              i32.store
              local.get $l11
              i32.const 4
              i32.add
              local.get $l12
              i32.const 4
              i32.add
              i32.load
              local.tee $p0
              local.get $l10
              i32.const 4
              i32.add
              i32.load
              local.tee $l16
              local.get $l6
              local.get $p1
              i32.lt_u
              local.get $p2
              local.get $l6
              i32.lt_u
              i32.add
              i32.add
              local.tee $l6
              i32.sub
              i32.store
              local.get $l6
              local.get $l16
              i32.lt_u
              local.get $p0
              local.get $l6
              i32.lt_u
              i32.add
              local.set $l6
              local.get $l11
              i32.const 8
              i32.add
              local.set $l11
              local.get $l10
              i32.const 8
              i32.add
              local.set $l10
              local.get $l12
              i32.const 8
              i32.add
              local.set $l12
              local.get $l15
              local.get $l13
              i32.const 2
              i32.add
              local.tee $l13
              i32.ne
              br_if $L12
            end
          end
          local.get $l5
          i32.eqz
          br_if $B8
          local.get $l14
          local.get $l13
          i32.const 2
          i32.shl
          local.tee $l11
          i32.add
          local.get $l3
          local.get $l11
          i32.add
          i32.load
          local.tee $l10
          local.get $l4
          local.get $l11
          i32.add
          i32.load
          local.tee $l11
          local.get $l6
          i32.add
          local.tee $l6
          i32.sub
          i32.store
          local.get $l6
          local.get $l11
          i32.lt_u
          local.get $l10
          local.get $l6
          i32.lt_u
          i32.add
          local.set $l6
        end
        block $B13
          local.get $l9
          local.get $l7
          i32.le_u
          br_if $B13
          i32.const 1
          call $assert
          local.get $l9
          local.get $l7
          i32.sub
          local.tee $l10
          i32.const 1
          i32.and
          local.set $p1
          local.get $l3
          local.get $l7
          i32.const 2
          i32.shl
          local.tee $l11
          i32.add
          local.set $p0
          local.get $l14
          local.get $l11
          i32.add
          local.set $l16
          block $B14
            block $B15
              local.get $l9
              local.get $l7
              i32.const 1
              i32.add
              i32.ne
              br_if $B15
              i32.const 0
              local.set $l10
              br $B14
            end
            local.get $l10
            i32.const 2147483646
            i32.and
            local.set $p2
            i32.const 0
            local.set $l10
            local.get $l16
            local.set $l11
            local.get $p0
            local.set $l7
            loop $L16
              local.get $l11
              local.get $l7
              i32.load
              local.tee $l12
              local.get $l6
              i32.sub
              i32.store
              local.get $l11
              i32.const 4
              i32.add
              local.get $l7
              i32.const 4
              i32.add
              i32.load
              local.tee $l13
              local.get $l12
              local.get $l6
              i32.lt_u
              local.tee $l6
              i32.sub
              i32.store
              local.get $l13
              local.get $l6
              i32.lt_u
              local.set $l6
              local.get $l11
              i32.const 8
              i32.add
              local.set $l11
              local.get $l7
              i32.const 8
              i32.add
              local.set $l7
              local.get $p2
              local.get $l10
              i32.const 2
              i32.add
              local.tee $l10
              i32.ne
              br_if $L16
            end
          end
          local.get $p1
          i32.eqz
          br_if $B13
          local.get $l16
          local.get $l10
          i32.const 2
          i32.shl
          local.tee $l11
          i32.add
          local.get $p0
          local.get $l11
          i32.add
          i32.load
          local.tee $l11
          local.get $l6
          i32.sub
          i32.store
          local.get $l11
          local.get $l6
          i32.lt_u
          local.set $l6
        end
        local.get $l6
        i32.eqz
        call $assert
        block $B17
          local.get $l8
          i32.eqz
          br_if $B17
          local.get $l9
          i32.const 2
          i32.shl
          local.get $l14
          i32.add
          i32.const -4
          i32.add
          local.set $l6
          loop $L18
            local.get $l6
            i32.load
            br_if $B17
            local.get $l6
            i32.const -4
            i32.add
            local.set $l6
            local.get $l9
            i32.const 1
            i32.gt_s
            local.set $l11
            local.get $l9
            i32.const -1
            i32.add
            local.set $l9
            local.get $l11
            br_if $L18
          end
          i32.const 0
          local.set $l9
        end
        i32.const 0
        local.get $l9
        i32.sub
        local.set $l7
        br $B0
      end
      block $B19
        block $B20
          local.get $l7
          local.get $p0
          i32.load
          i32.le_s
          br_if $B20
          local.get $p0
          local.get $l7
          call $mpz_realloc
          local.set $l14
          local.get $p2
          i32.load offset=8
          local.set $l3
          local.get $p1
          i32.load offset=8
          local.set $l4
          br $B19
        end
        local.get $p0
        i32.load offset=8
        local.set $l14
      end
      local.get $l7
      local.get $l9
      i32.ge_u
      call $assert
      block $B21
        block $B22
          local.get $l8
          br_if $B22
          i32.const 0
          local.set $l6
          br $B21
        end
        local.get $l9
        i32.const 1
        i32.and
        local.set $l8
        block $B23
          block $B24
            local.get $l9
            i32.const 1
            i32.ne
            br_if $B24
            i32.const 0
            local.set $l13
            i32.const 0
            local.set $l6
            br $B23
          end
          local.get $l9
          i32.const 2147483646
          i32.and
          local.set $l15
          i32.const 0
          local.set $l13
          local.get $l14
          local.set $l11
          local.get $l3
          local.set $l10
          local.get $l4
          local.set $l12
          i32.const 0
          local.set $l6
          loop $L25
            local.get $l11
            local.get $l12
            i32.load
            local.tee $p2
            local.get $l10
            i32.load
            local.tee $p1
            local.get $l6
            i32.add
            local.tee $l6
            i32.sub
            i32.store
            local.get $l11
            i32.const 4
            i32.add
            local.get $l12
            i32.const 4
            i32.add
            i32.load
            local.tee $p0
            local.get $l10
            i32.const 4
            i32.add
            i32.load
            local.tee $l16
            local.get $l6
            local.get $p1
            i32.lt_u
            local.get $p2
            local.get $l6
            i32.lt_u
            i32.add
            i32.add
            local.tee $l6
            i32.sub
            i32.store
            local.get $l6
            local.get $l16
            i32.lt_u
            local.get $p0
            local.get $l6
            i32.lt_u
            i32.add
            local.set $l6
            local.get $l11
            i32.const 8
            i32.add
            local.set $l11
            local.get $l10
            i32.const 8
            i32.add
            local.set $l10
            local.get $l12
            i32.const 8
            i32.add
            local.set $l12
            local.get $l15
            local.get $l13
            i32.const 2
            i32.add
            local.tee $l13
            i32.ne
            br_if $L25
          end
        end
        local.get $l8
        i32.eqz
        br_if $B21
        local.get $l14
        local.get $l13
        i32.const 2
        i32.shl
        local.tee $l11
        i32.add
        local.get $l4
        local.get $l11
        i32.add
        i32.load
        local.tee $l10
        local.get $l3
        local.get $l11
        i32.add
        i32.load
        local.tee $l11
        local.get $l6
        i32.add
        local.tee $l6
        i32.sub
        i32.store
        local.get $l6
        local.get $l11
        i32.lt_u
        local.get $l10
        local.get $l6
        i32.lt_u
        i32.add
        local.set $l6
      end
      block $B26
        local.get $l7
        local.get $l9
        i32.le_u
        br_if $B26
        i32.const 1
        call $assert
        local.get $l7
        local.get $l9
        i32.sub
        local.tee $l10
        i32.const 1
        i32.and
        local.set $p1
        local.get $l4
        local.get $l9
        i32.const 2
        i32.shl
        local.tee $l11
        i32.add
        local.set $p0
        local.get $l14
        local.get $l11
        i32.add
        local.set $l16
        block $B27
          block $B28
            local.get $l7
            local.get $l9
            i32.const 1
            i32.add
            i32.ne
            br_if $B28
            i32.const 0
            local.set $l10
            br $B27
          end
          local.get $l10
          i32.const 2147483646
          i32.and
          local.set $p2
          i32.const 0
          local.set $l10
          local.get $l16
          local.set $l11
          local.get $p0
          local.set $l9
          loop $L29
            local.get $l11
            local.get $l9
            i32.load
            local.tee $l12
            local.get $l6
            i32.sub
            i32.store
            local.get $l11
            i32.const 4
            i32.add
            local.get $l9
            i32.const 4
            i32.add
            i32.load
            local.tee $l13
            local.get $l12
            local.get $l6
            i32.lt_u
            local.tee $l6
            i32.sub
            i32.store
            local.get $l13
            local.get $l6
            i32.lt_u
            local.set $l6
            local.get $l11
            i32.const 8
            i32.add
            local.set $l11
            local.get $l9
            i32.const 8
            i32.add
            local.set $l9
            local.get $p2
            local.get $l10
            i32.const 2
            i32.add
            local.tee $l10
            i32.ne
            br_if $L29
          end
        end
        local.get $p1
        i32.eqz
        br_if $B26
        local.get $l16
        local.get $l10
        i32.const 2
        i32.shl
        local.tee $l11
        i32.add
        local.get $p0
        local.get $l11
        i32.add
        i32.load
        local.tee $l11
        local.get $l6
        i32.sub
        i32.store
        local.get $l11
        local.get $l6
        i32.lt_u
        local.set $l6
      end
      local.get $l6
      i32.eqz
      call $assert
      local.get $l5
      i32.eqz
      br_if $B0
      local.get $l7
      i32.const 2
      i32.shl
      local.get $l14
      i32.add
      i32.const -4
      i32.add
      local.set $l6
      loop $L30
        local.get $l6
        i32.load
        br_if $B0
        local.get $l6
        i32.const -4
        i32.add
        local.set $l6
        local.get $l7
        i32.const 1
        i32.gt_s
        local.set $l11
        local.get $l7
        i32.const -1
        i32.add
        local.set $l7
        local.get $l11
        br_if $L30
      end
      i32.const 0
      return
    end
    local.get $l7)
  (func $mpz_mul (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    block $B0
      block $B1
        local.get $p1
        i32.load offset=4
        local.tee $l3
        i32.eqz
        br_if $B1
        local.get $p2
        i32.load offset=4
        local.tee $l4
        br_if $B0
      end
      local.get $p0
      i32.const 0
      i32.store offset=4
      return
    end
    i32.const 1
    call $assert
    block $B2
      local.get $l4
      local.get $l4
      i32.const 31
      i32.shr_s
      local.tee $l5
      i32.xor
      local.get $l5
      i32.sub
      local.tee $l6
      local.get $l3
      local.get $l3
      i32.const 31
      i32.shr_s
      local.tee $l5
      i32.xor
      local.get $l5
      i32.sub
      local.tee $l7
      i32.add
      local.tee $l8
      i32.const 5
      i32.shl
      local.tee $l5
      local.get $l5
      i32.const 0
      i32.ne
      i32.sub
      i32.const 5
      i32.shr_u
      i32.const 1
      i32.add
      local.tee $l9
      i32.const 2
      i32.shl
      call $malloc
      local.tee $l5
      i32.eqz
      br_if $B2
      local.get $l4
      local.get $l3
      i32.xor
      local.set $l3
      block $B3
        block $B4
          local.get $l7
          local.get $l6
          i32.lt_u
          br_if $B4
          local.get $l5
          local.get $p1
          i32.load offset=8
          local.get $l7
          local.get $p2
          i32.load offset=8
          local.get $l6
          call $mpn_mul
          drop
          br $B3
        end
        local.get $l5
        local.get $p2
        i32.load offset=8
        local.get $l6
        local.get $p1
        i32.load offset=8
        local.get $l7
        call $mpn_mul
        drop
      end
      local.get $p0
      i32.load
      local.set $p1
      local.get $p0
      local.get $l9
      i32.store
      local.get $p0
      i32.load offset=8
      local.set $l4
      local.get $p0
      local.get $l5
      i32.store offset=8
      local.get $p0
      i32.const 0
      local.get $l8
      local.get $l5
      local.get $l8
      i32.const 2
      i32.shl
      i32.add
      i32.const -4
      i32.add
      i32.load
      i32.eqz
      i32.sub
      local.tee $p2
      i32.sub
      local.get $p2
      local.get $l3
      i32.const 0
      i32.lt_s
      select
      i32.store offset=4
      block $B5
        local.get $p1
        i32.eqz
        br_if $B5
        local.get $l4
        call $free
      end
      return
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $mpz_addmul_ui (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l3
    global.set $__stack_pointer
    local.get $l3
    i32.const 1156
    i32.store offset=12
    local.get $l3
    i32.const 0
    i32.store offset=4
    block $B0
      block $B1
        block $B2
          local.get $p2
          i32.eqz
          br_if $B2
          local.get $l3
          i32.const 1
          i32.store offset=8
          i32.const 1
          call $assert
          i32.const 4
          call $malloc
          local.tee $l4
          i32.eqz
          br_if $B0
          local.get $l4
          local.get $p2
          i32.store
          local.get $l3
          i32.const 1
          i32.store offset=4
          local.get $l3
          local.get $l4
          i32.store offset=12
          br $B1
        end
        local.get $l3
        i32.const 0
        i32.store offset=8
      end
      local.get $l3
      i32.const 4
      i32.add
      local.get $p1
      local.get $l3
      i32.const 4
      i32.add
      call $mpz_mul
      block $B3
        block $B4
          local.get $l3
          i32.load offset=8
          local.get $p0
          i32.load offset=4
          i32.xor
          i32.const 0
          i32.lt_s
          br_if $B4
          local.get $p0
          local.get $p0
          local.get $l3
          i32.const 4
          i32.add
          call $mpz_abs_add
          local.set $p2
          br $B3
        end
        local.get $p0
        local.get $p0
        local.get $l3
        i32.const 4
        i32.add
        call $mpz_abs_sub
        local.set $p2
      end
      local.get $p0
      i32.const 0
      local.get $p2
      i32.sub
      local.get $p2
      local.get $p0
      i32.load offset=4
      i32.const 0
      i32.lt_s
      select
      i32.store offset=4
      block $B5
        local.get $l3
        i32.load offset=4
        i32.eqz
        br_if $B5
        local.get $l3
        i32.load offset=12
        call $free
      end
      local.get $l3
      i32.const 16
      i32.add
      global.set $__stack_pointer
      return
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $mpz_submul_ui (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l3
    global.set $__stack_pointer
    local.get $l3
    i32.const 1156
    i32.store offset=12
    local.get $l3
    i32.const 0
    i32.store offset=4
    block $B0
      block $B1
        block $B2
          local.get $p2
          i32.eqz
          br_if $B2
          local.get $l3
          i32.const 1
          i32.store offset=8
          i32.const 1
          call $assert
          i32.const 4
          call $malloc
          local.tee $l4
          i32.eqz
          br_if $B0
          local.get $l4
          local.get $p2
          i32.store
          local.get $l3
          i32.const 1
          i32.store offset=4
          local.get $l3
          local.get $l4
          i32.store offset=12
          br $B1
        end
        local.get $l3
        i32.const 0
        i32.store offset=8
      end
      local.get $l3
      i32.const 4
      i32.add
      local.get $p1
      local.get $l3
      i32.const 4
      i32.add
      call $mpz_mul
      block $B3
        block $B4
          local.get $l3
          i32.load offset=8
          local.get $p0
          i32.load offset=4
          i32.xor
          i32.const 0
          i32.lt_s
          br_if $B4
          local.get $p0
          local.get $p0
          local.get $l3
          i32.const 4
          i32.add
          call $mpz_abs_sub
          local.set $p2
          br $B3
        end
        local.get $p0
        local.get $p0
        local.get $l3
        i32.const 4
        i32.add
        call $mpz_abs_add
        local.set $p2
      end
      local.get $p0
      i32.const 0
      local.get $p2
      i32.sub
      local.get $p2
      local.get $p0
      i32.load offset=4
      i32.const 0
      i32.lt_s
      select
      i32.store offset=4
      block $B5
        local.get $l3
        i32.load offset=4
        i32.eqz
        br_if $B5
        local.get $l3
        i32.load offset=12
        call $free
      end
      local.get $l3
      i32.const 16
      i32.add
      global.set $__stack_pointer
      return
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $mpz_div_qr (type $t6) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee $l5
    global.set $__stack_pointer
    block $B0
      block $B1
        block $B2
          local.get $p3
          i32.load offset=4
          local.tee $l6
          i32.eqz
          br_if $B2
          block $B3
            block $B4
              local.get $p2
              i32.load offset=4
              local.tee $l7
              br_if $B4
              block $B5
                local.get $p0
                i32.eqz
                br_if $B5
                local.get $p0
                i32.const 0
                i32.store offset=4
              end
              i32.const 0
              local.set $l6
              local.get $p1
              i32.eqz
              br_if $B3
              local.get $p1
              i32.const 0
              i32.store offset=4
              br $B3
            end
            local.get $l6
            local.get $l7
            i32.xor
            local.set $l8
            block $B6
              local.get $l7
              local.get $l7
              i32.const 31
              i32.shr_s
              local.tee $l9
              i32.xor
              local.get $l9
              i32.sub
              local.tee $l10
              local.get $l6
              local.get $l6
              i32.const 31
              i32.shr_s
              local.tee $l9
              i32.xor
              local.get $l9
              i32.sub
              local.tee $l11
              i32.ge_u
              br_if $B6
              block $B7
                local.get $p4
                i32.const 1
                i32.ne
                br_if $B7
                local.get $l8
                i32.const 0
                i32.lt_s
                br_if $B7
                block $B8
                  local.get $p1
                  i32.eqz
                  br_if $B8
                  local.get $p1
                  i32.const 0
                  local.get $p1
                  local.get $p2
                  local.get $p3
                  call $mpz_abs_sub
                  local.tee $l6
                  i32.sub
                  local.get $l6
                  local.get $p2
                  i32.load offset=4
                  i32.const 0
                  i32.lt_s
                  select
                  i32.store offset=4
                end
                block $B9
                  local.get $p0
                  br_if $B9
                  i32.const 1
                  local.set $l6
                  br $B3
                end
                local.get $p0
                i32.const 1
                i32.store offset=4
                block $B10
                  block $B11
                    local.get $p0
                    i32.load
                    i32.const 0
                    i32.gt_s
                    br_if $B11
                    local.get $p0
                    i32.const 1
                    call $mpz_realloc
                    local.set $p2
                    br $B10
                  end
                  local.get $p0
                  i32.load offset=8
                  local.set $p2
                end
                i32.const 1
                local.set $l6
                local.get $p2
                i32.const 1
                i32.store
                br $B3
              end
              block $B12
                local.get $p4
                br_if $B12
                local.get $l8
                i32.const -1
                i32.gt_s
                br_if $B12
                block $B13
                  local.get $p1
                  i32.eqz
                  br_if $B13
                  local.get $p1
                  i32.const 0
                  local.get $p1
                  local.get $p2
                  local.get $p3
                  call $mpz_abs_sub
                  local.tee $l6
                  i32.sub
                  local.get $l6
                  local.get $p2
                  i32.load offset=4
                  i32.const 0
                  i32.lt_s
                  select
                  i32.store offset=4
                end
                block $B14
                  local.get $p0
                  br_if $B14
                  i32.const 1
                  local.set $l6
                  br $B3
                end
                local.get $p0
                i32.const -1
                i32.store offset=4
                block $B15
                  block $B16
                    local.get $p0
                    i32.load
                    i32.const 0
                    i32.gt_s
                    br_if $B16
                    local.get $p0
                    i32.const 1
                    call $mpz_realloc
                    local.set $p2
                    br $B15
                  end
                  local.get $p0
                  i32.load offset=8
                  local.set $p2
                end
                i32.const 1
                local.set $l6
                local.get $p2
                i32.const 1
                i32.store
                br $B3
              end
              block $B17
                local.get $p1
                i32.eqz
                br_if $B17
                local.get $p1
                local.get $p2
                i32.eq
                br_if $B17
                block $B18
                  block $B19
                    local.get $l10
                    local.get $p1
                    i32.load
                    i32.le_s
                    br_if $B19
                    local.get $p1
                    local.get $l10
                    call $mpz_realloc
                    local.set $l12
                    br $B18
                  end
                  local.get $p1
                  i32.load offset=8
                  local.set $l12
                end
                local.get $l10
                i32.const 3
                i32.and
                local.set $l13
                local.get $p2
                i32.load offset=8
                local.set $l14
                i32.const 0
                local.set $l15
                block $B20
                  local.get $l10
                  i32.const 4
                  i32.lt_u
                  br_if $B20
                  local.get $l10
                  i32.const 2147483644
                  i32.and
                  local.set $l16
                  i32.const 0
                  local.set $l6
                  i32.const 0
                  local.set $l15
                  loop $L21
                    local.get $l12
                    local.get $l6
                    i32.add
                    local.tee $l9
                    local.get $l14
                    local.get $l6
                    i32.add
                    local.tee $l11
                    i32.load
                    i32.store
                    local.get $l9
                    i32.const 4
                    i32.add
                    local.get $l11
                    i32.const 4
                    i32.add
                    i32.load
                    i32.store
                    local.get $l9
                    i32.const 8
                    i32.add
                    local.get $l11
                    i32.const 8
                    i32.add
                    i32.load
                    i32.store
                    local.get $l9
                    i32.const 12
                    i32.add
                    local.get $l11
                    i32.const 12
                    i32.add
                    i32.load
                    i32.store
                    local.get $l6
                    i32.const 16
                    i32.add
                    local.set $l6
                    local.get $l16
                    local.get $l15
                    i32.const 4
                    i32.add
                    local.tee $l15
                    i32.ne
                    br_if $L21
                  end
                end
                block $B22
                  local.get $l13
                  i32.eqz
                  br_if $B22
                  local.get $l14
                  local.get $l15
                  i32.const 2
                  i32.shl
                  local.tee $l9
                  i32.add
                  local.set $l6
                  local.get $l12
                  local.get $l9
                  i32.add
                  local.set $l9
                  loop $L23
                    local.get $l9
                    local.get $l6
                    i32.load
                    i32.store
                    local.get $l6
                    i32.const 4
                    i32.add
                    local.set $l6
                    local.get $l9
                    i32.const 4
                    i32.add
                    local.set $l9
                    local.get $l13
                    i32.const -1
                    i32.add
                    local.tee $l13
                    br_if $L23
                  end
                end
                local.get $p1
                local.get $p2
                i32.load offset=4
                i32.store offset=4
              end
              i32.const 1
              local.set $l6
              local.get $p0
              i32.eqz
              br_if $B3
              local.get $p0
              i32.const 0
              i32.store offset=4
              br $B3
            end
            i32.const 0
            local.set $l15
            local.get $l5
            i32.const 0
            i32.store offset=12
            local.get $l10
            i32.const 2
            i32.shl
            local.tee $l6
            i32.const 0
            i32.ne
            call $assert
            local.get $l6
            call $malloc
            local.tee $l12
            i32.eqz
            br_if $B1
            local.get $l5
            local.get $l12
            i32.store offset=16
            local.get $l5
            local.get $l10
            i32.store offset=8
            local.get $l10
            i32.const 3
            i32.and
            local.set $l13
            local.get $p2
            i32.load offset=8
            local.set $l14
            block $B24
              local.get $l10
              i32.const 4
              i32.lt_u
              br_if $B24
              local.get $l10
              i32.const 2147483644
              i32.and
              local.set $l16
              i32.const 0
              local.set $l6
              i32.const 0
              local.set $l15
              loop $L25
                local.get $l12
                local.get $l6
                i32.add
                local.tee $p2
                local.get $l14
                local.get $l6
                i32.add
                local.tee $l9
                i32.load
                i32.store
                local.get $p2
                i32.const 4
                i32.add
                local.get $l9
                i32.const 4
                i32.add
                i32.load
                i32.store
                local.get $p2
                i32.const 8
                i32.add
                local.get $l9
                i32.const 8
                i32.add
                i32.load
                i32.store
                local.get $p2
                i32.const 12
                i32.add
                local.get $l9
                i32.const 12
                i32.add
                i32.load
                i32.store
                local.get $l6
                i32.const 16
                i32.add
                local.set $l6
                local.get $l16
                local.get $l15
                i32.const 4
                i32.add
                local.tee $l15
                i32.ne
                br_if $L25
              end
            end
            block $B26
              local.get $l13
              i32.eqz
              br_if $B26
              local.get $l14
              local.get $l15
              i32.const 2
              i32.shl
              local.tee $p2
              i32.add
              local.set $l6
              local.get $l12
              local.get $p2
              i32.add
              local.set $p2
              loop $L27
                local.get $p2
                local.get $l6
                i32.load
                i32.store
                local.get $l6
                i32.const 4
                i32.add
                local.set $l6
                local.get $p2
                i32.const 4
                i32.add
                local.set $p2
                local.get $l13
                i32.const -1
                i32.add
                local.tee $l13
                br_if $L27
              end
            end
            block $B28
              block $B29
                local.get $p0
                br_if $B29
                i32.const 0
                local.get $l12
                local.get $l10
                local.get $p3
                i32.load offset=8
                local.get $l11
                call $mpn_div_qr
                br $B28
              end
              local.get $l5
              local.get $l10
              local.get $l11
              i32.sub
              local.tee $p2
              i32.const 1
              i32.add
              local.tee $l9
              i32.const 5
              i32.shl
              local.tee $l6
              local.get $l6
              i32.const 0
              i32.ne
              i32.sub
              i32.const 5
              i32.shr_u
              i32.const 1
              i32.add
              local.tee $l6
              i32.store offset=20
              i32.const 1
              call $assert
              local.get $l6
              i32.const 2
              i32.shl
              call $malloc
              local.tee $l6
              i32.eqz
              br_if $B0
              local.get $l5
              local.get $l6
              i32.store offset=28
              local.get $l6
              local.get $l12
              local.get $l10
              local.get $p3
              i32.load offset=8
              local.get $l11
              call $mpn_div_qr
              local.get $l5
              i32.const 0
              local.get $l9
              local.get $l6
              local.get $p2
              i32.const 2
              i32.shl
              i32.add
              i32.load
              i32.eqz
              i32.sub
              local.tee $l6
              i32.sub
              local.get $l6
              local.get $l8
              i32.const 0
              i32.lt_s
              select
              local.tee $l9
              i32.store offset=24
            end
            local.get $l11
            i32.const 2
            i32.shl
            local.get $l12
            i32.add
            i32.const -4
            i32.add
            local.set $l6
            block $B30
              loop $L31
                local.get $l6
                i32.load
                br_if $B30
                local.get $l6
                i32.const -4
                i32.add
                local.set $l6
                local.get $l11
                i32.const 1
                i32.gt_s
                local.set $p2
                local.get $l11
                i32.const -1
                i32.add
                local.set $l11
                local.get $p2
                br_if $L31
              end
              i32.const 0
              local.set $l11
            end
            local.get $l5
            i32.const 0
            local.get $l11
            i32.sub
            local.get $l11
            local.get $l7
            i32.const 0
            i32.lt_s
            select
            local.tee $l6
            i32.store offset=12
            block $B32
              block $B33
                local.get $p4
                br_if $B33
                local.get $l8
                i32.const -1
                i32.gt_s
                br_if $B33
                local.get $l11
                i32.eqz
                br_if $B33
                block $B34
                  local.get $p0
                  i32.eqz
                  br_if $B34
                  local.get $l5
                  i32.const 0
                  local.get $l9
                  i32.sub
                  i32.store offset=24
                  local.get $l5
                  i32.const 20
                  i32.add
                  local.get $l5
                  i32.const 20
                  i32.add
                  i32.const 1
                  call $mpz_add_ui
                  local.get $l5
                  i32.const 0
                  local.get $l5
                  i32.load offset=24
                  i32.sub
                  i32.store offset=24
                end
                local.get $p1
                i32.eqz
                br_if $B32
                block $B35
                  block $B36
                    local.get $p3
                    i32.load offset=4
                    local.get $l6
                    i32.xor
                    i32.const 0
                    i32.lt_s
                    br_if $B36
                    local.get $l5
                    i32.const 8
                    i32.add
                    local.get $l5
                    i32.const 8
                    i32.add
                    local.get $p3
                    call $mpz_abs_add
                    local.set $l6
                    br $B35
                  end
                  local.get $l5
                  i32.const 8
                  i32.add
                  local.get $l5
                  i32.const 8
                  i32.add
                  local.get $p3
                  call $mpz_abs_sub
                  local.set $l6
                end
                i32.const 0
                local.get $l6
                i32.sub
                local.get $l6
                local.get $l5
                i32.load offset=12
                i32.const 0
                i32.lt_s
                select
                local.set $l6
                br $B32
              end
              local.get $p4
              i32.const 1
              i32.ne
              br_if $B32
              local.get $l8
              i32.const 0
              i32.lt_s
              br_if $B32
              local.get $l11
              i32.eqz
              br_if $B32
              block $B37
                local.get $p0
                i32.eqz
                br_if $B37
                local.get $l5
                i32.const 20
                i32.add
                local.get $l5
                i32.const 20
                i32.add
                i32.const 1
                call $mpz_add_ui
              end
              local.get $p1
              i32.eqz
              br_if $B32
              block $B38
                block $B39
                  local.get $p3
                  i32.load offset=4
                  local.get $l6
                  i32.xor
                  i32.const 0
                  i32.lt_s
                  br_if $B39
                  local.get $l5
                  i32.const 8
                  i32.add
                  local.get $l5
                  i32.const 8
                  i32.add
                  local.get $p3
                  call $mpz_abs_sub
                  local.set $l6
                  br $B38
                end
                local.get $l5
                i32.const 8
                i32.add
                local.get $l5
                i32.const 8
                i32.add
                local.get $p3
                call $mpz_abs_add
                local.set $l6
              end
              i32.const 0
              local.get $l6
              i32.sub
              local.get $l6
              local.get $l5
              i32.load offset=12
              i32.const 0
              i32.lt_s
              select
              local.set $l6
            end
            block $B40
              local.get $p0
              i32.eqz
              br_if $B40
              local.get $p0
              i32.load
              local.set $p2
              local.get $p0
              local.get $l5
              i32.load offset=20
              i32.store
              local.get $p0
              i32.load offset=8
              local.set $l9
              local.get $p0
              local.get $l5
              i64.load offset=24 align=4
              i64.store offset=4 align=4
              local.get $p2
              i32.eqz
              br_if $B40
              local.get $l9
              call $free
            end
            local.get $l5
            i32.load offset=8
            local.set $p2
            block $B41
              block $B42
                local.get $p1
                br_if $B42
                local.get $p2
                local.set $l6
                br $B41
              end
              local.get $p1
              local.get $l6
              i32.store offset=4
              local.get $p1
              i32.load
              local.set $l6
              local.get $p1
              local.get $p2
              i32.store
              local.get $p1
              i32.load offset=8
              local.set $p2
              local.get $p1
              local.get $l5
              i32.load offset=16
              i32.store offset=8
              local.get $l5
              local.get $p2
              i32.store offset=16
            end
            block $B43
              local.get $l6
              i32.eqz
              br_if $B43
              local.get $l5
              i32.load offset=16
              call $free
            end
            local.get $l11
            i32.const 0
            i32.ne
            local.set $l6
          end
          local.get $l5
          i32.const 32
          i32.add
          global.set $__stack_pointer
          local.get $l6
          return
        end
        i32.const 1024
        call $gmp_die
        unreachable
      end
      i32.const 1099
      call $gmp_die
      unreachable
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $extract_digit (type $t3) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    local.get $l1
    i32.const 1156
    i32.store offset=12
    local.get $l1
    i32.const 0
    i32.store offset=4
    block $B0
      block $B1
        block $B2
          local.get $p0
          i32.eqz
          br_if $B2
          local.get $l1
          i32.const 1
          i32.store offset=8
          i32.const 1
          call $assert
          i32.const 4
          call $malloc
          local.tee $l2
          br_if $B1
          i32.const 1099
          call $gmp_die
          unreachable
        end
        local.get $l1
        i32.const 0
        i32.store offset=8
        i32.const 1160
        i32.const 1172
        local.get $l1
        i32.const 4
        i32.add
        call $mpz_mul
        br $B0
      end
      local.get $l2
      local.get $p0
      i32.store
      local.get $l1
      i32.const 1
      i32.store offset=4
      local.get $l1
      local.get $l2
      i32.store offset=12
      i32.const 1160
      i32.const 1172
      local.get $l1
      i32.const 4
      i32.add
      call $mpz_mul
      local.get $l2
      call $free
    end
    block $B3
      block $B4
        i32.const 0
        i32.load offset=1200
        i32.const 0
        i32.load offset=1164
        i32.xor
        i32.const 0
        i32.lt_s
        br_if $B4
        i32.const 1184
        i32.const 1160
        i32.const 1196
        call $mpz_abs_add
        local.set $p0
        br $B3
      end
      i32.const 1184
      i32.const 1160
      i32.const 1196
      call $mpz_abs_sub
      local.set $p0
    end
    i32.const 0
    local.set $l2
    i32.const 0
    i32.const 0
    local.get $p0
    i32.sub
    local.get $p0
    i32.const 0
    i32.load offset=1164
    i32.const 0
    i32.lt_s
    select
    i32.store offset=1188
    i32.const 1160
    i32.const 0
    i32.const 1184
    i32.const 1208
    i32.const 2
    call $mpz_div_qr
    drop
    block $B5
      i32.const 0
      i32.load offset=1164
      i32.eqz
      br_if $B5
      i32.const 0
      i32.load offset=1168
      i32.load
      local.set $l2
    end
    local.get $l1
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get $l2)
  (func $eliminate_digit (type $t2) (param $p0 i32)
    (local $l1 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    i32.const 1196
    i32.const 1208
    local.get $p0
    call $mpz_submul_ui
    local.get $l1
    i32.const 1
    i32.store offset=8
    i32.const 1
    call $assert
    block $B0
      block $B1
        i32.const 4
        call $malloc
        local.tee $p0
        i32.eqz
        br_if $B1
        local.get $p0
        i32.const 10
        i32.store
        local.get $l1
        i32.const 1
        i32.store offset=4
        local.get $l1
        local.get $p0
        i32.store offset=12
        i32.const 1196
        i32.const 1196
        local.get $l1
        i32.const 4
        i32.add
        call $mpz_mul
        local.get $p0
        call $free
        local.get $l1
        i32.const 1
        i32.store offset=8
        i32.const 1
        call $assert
        i32.const 4
        call $malloc
        local.tee $p0
        i32.eqz
        br_if $B0
        local.get $p0
        i32.const 10
        i32.store
        local.get $l1
        i32.const 1
        i32.store offset=4
        local.get $l1
        local.get $p0
        i32.store offset=12
        i32.const 1172
        i32.const 1172
        local.get $l1
        i32.const 4
        i32.add
        call $mpz_mul
        local.get $p0
        call $free
        local.get $l1
        i32.const 16
        i32.add
        global.set $__stack_pointer
        return
      end
      i32.const 1099
      call $gmp_die
      unreachable
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $next_term (type $t2) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l1
    global.set $__stack_pointer
    i32.const 1196
    i32.const 1172
    i32.const 2
    call $mpz_addmul_ui
    local.get $l1
    i32.const 1
    i32.store offset=8
    i32.const 1
    call $assert
    block $B0
      block $B1
        i32.const 4
        call $malloc
        local.tee $l2
        i32.eqz
        br_if $B1
        local.get $l2
        local.get $p0
        i32.const 1
        i32.shl
        i32.const 1
        i32.or
        local.tee $l3
        i32.store
        local.get $l1
        i32.const 1
        i32.store offset=4
        local.get $l1
        local.get $l2
        i32.store offset=12
        i32.const 1196
        i32.const 1196
        local.get $l1
        i32.const 4
        i32.add
        call $mpz_mul
        local.get $l2
        call $free
        local.get $l1
        i32.const 1
        i32.store offset=8
        i32.const 1
        call $assert
        i32.const 4
        call $malloc
        local.tee $l2
        i32.eqz
        br_if $B0
        local.get $l2
        local.get $l3
        i32.store
        local.get $l1
        i32.const 1
        i32.store offset=4
        local.get $l1
        local.get $l2
        i32.store offset=12
        i32.const 1208
        i32.const 1208
        local.get $l1
        i32.const 4
        i32.add
        call $mpz_mul
        local.get $l2
        call $free
        local.get $l1
        i32.const 1156
        i32.store offset=12
        local.get $l1
        i32.const 0
        i32.store offset=4
        block $B2
          block $B3
            block $B4
              local.get $p0
              i32.eqz
              br_if $B4
              local.get $l1
              i32.const 1
              i32.store offset=8
              i32.const 1
              call $assert
              i32.const 4
              call $malloc
              local.tee $l2
              br_if $B3
              i32.const 1099
              call $gmp_die
              unreachable
            end
            local.get $l1
            i32.const 0
            i32.store offset=8
            i32.const 1172
            i32.const 1172
            local.get $l1
            i32.const 4
            i32.add
            call $mpz_mul
            br $B2
          end
          local.get $l2
          local.get $p0
          i32.store
          local.get $l1
          i32.const 1
          i32.store offset=4
          local.get $l1
          local.get $l2
          i32.store offset=12
          i32.const 1172
          i32.const 1172
          local.get $l1
          i32.const 4
          i32.add
          call $mpz_mul
          local.get $l2
          call $free
        end
        local.get $l1
        i32.const 16
        i32.add
        global.set $__stack_pointer
        return
      end
      i32.const 1099
      call $gmp_die
      unreachable
    end
    i32.const 1099
    call $gmp_die
    unreachable)
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    call $__VERIFIER_nondet_int
    local.set $l1
    i32.const 0
    i32.const 1156
    i32.store offset=1168
    i32.const 0
    i64.const 0
    i64.store offset=1160 align=4
    i32.const 0
    i32.const 1156
    i32.store offset=1192
    i32.const 0
    i64.const 0
    i64.store offset=1184 align=4
    i32.const 0
    i32.const 1156
    i32.store offset=1204
    i32.const 0
    i64.const 0
    i64.store offset=1196 align=4
    i32.const 0
    i32.const 1156
    i32.store offset=1216
    i32.const 0
    i64.const 4294967296
    i64.store offset=1208 align=4
    i32.const 1208
    i32.const 1
    call $mpz_realloc
    local.set $l2
    i32.const 0
    i32.const 1156
    i32.store offset=1180
    local.get $l2
    i32.const 1
    i32.store
    i32.const 0
    i64.const 4294967296
    i64.store offset=1172 align=4
    i32.const 1172
    i32.const 1
    call $mpz_realloc
    i32.const 1
    i32.store
    block $B0
      local.get $l1
      i32.eqz
      br_if $B0
      i32.const 0
      local.set $l3
      i32.const 0
      local.set $l4
      loop $L1
        local.get $l3
        i32.const 1
        i32.add
        local.tee $l3
        call $next_term
        block $B2
          block $B3
            i32.const 0
            i32.load offset=1176
            local.tee $l2
            i32.const 0
            i32.load offset=1200
            local.tee $l5
            i32.eq
            br_if $B3
            local.get $l2
            local.get $l5
            i32.ge_s
            br_if $L1
            br $B2
          end
          block $B4
            local.get $l2
            i32.const 0
            i32.lt_s
            br_if $B4
            local.get $l2
            i32.const 1
            i32.add
            local.set $l6
            i32.const 0
            i32.load offset=1180
            local.get $l2
            i32.const 2
            i32.shl
            local.tee $l5
            i32.add
            i32.const -4
            i32.add
            local.set $l2
            i32.const 0
            i32.load offset=1204
            local.get $l5
            i32.add
            i32.const -4
            i32.add
            local.set $l5
            loop $L5
              local.get $l6
              i32.const -1
              i32.add
              local.tee $l6
              i32.const 1
              i32.lt_s
              br_if $B2
              local.get $l5
              i32.load
              local.set $l7
              local.get $l2
              i32.load
              local.set $l8
              local.get $l2
              i32.const -4
              i32.add
              local.set $l2
              local.get $l5
              i32.const -4
              i32.add
              local.set $l5
              local.get $l8
              local.get $l7
              i32.eq
              br_if $L5
            end
            local.get $l8
            local.get $l7
            i32.gt_u
            br_if $L1
            br $B2
          end
          i32.const 1
          local.get $l2
          i32.sub
          local.set $l6
          i32.const 0
          i32.load offset=1204
          local.get $l2
          i32.const 2
          i32.shl
          local.tee $l5
          i32.sub
          i32.const -4
          i32.add
          local.set $l2
          i32.const 0
          i32.load offset=1180
          local.get $l5
          i32.sub
          i32.const -4
          i32.add
          local.set $l5
          loop $L6
            local.get $l6
            i32.const -1
            i32.add
            local.tee $l6
            i32.const 1
            i32.lt_s
            br_if $B2
            local.get $l5
            i32.load
            local.set $l7
            local.get $l2
            i32.load
            local.set $l8
            local.get $l2
            i32.const -4
            i32.add
            local.set $l2
            local.get $l5
            i32.const -4
            i32.add
            local.set $l5
            local.get $l8
            local.get $l7
            i32.eq
            br_if $L6
          end
          local.get $l8
          local.get $l7
          i32.gt_u
          br_if $L1
        end
        i32.const 3
        call $extract_digit
        local.tee $l2
        i32.const 4
        call $extract_digit
        i32.ne
        br_if $L1
        local.get $l2
        i32.const 48
        i32.add
        call $putchar
        drop
        block $B7
          local.get $l4
          i32.const 1
          i32.add
          local.tee $l4
          i32.const 10
          i32.rem_u
          br_if $B7
          local.get $l0
          local.get $l4
          i32.store
          i32.const 1144
          local.get $l0
          call $printf
          drop
        end
        local.get $l2
        call $eliminate_digit
        local.get $l4
        local.get $l1
        i32.ne
        br_if $L1
      end
    end
    local.get $l0
    i32.const 16
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66768))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "mpz_div_qr: Divide by zero.\00gmp_default_realloc: Virtual memory exhausted.\00gmp_default_alloc: Virtual memory exhausted.\00\09:%u\0a\00%s\0a\00\00\00\a0\c1\00\00")
  (@custom "name" "\00\0e\0dpidigits.wasm\01\95\03\1f\00\07fprintf\01\05abort\02\06assert\03\06malloc\04\07realloc\05\04free\06\15__VERIFIER_nondet_int\07\07putchar\08\06printf\09\07gmp_die\0a\07mpn_mul\0b\0fmpn_invert_3by2\0c\13mpn_div_qr_1_invert\0d\11mpn_div_qr_invert\0e\13mpn_div_qr_1_preinv\0f\13mpn_div_qr_2_preinv\10\0empn_div_qr_pi1\11\11mpn_div_qr_preinv\12\0ampn_div_qr\13\0bmpz_realloc\14\0ampz_add_ui\15\0bmpz_abs_add\16\0bmpz_abs_sub\17\07mpz_mul\18\0dmpz_addmul_ui\19\0dmpz_submul_ui\1a\0ampz_div_qr\1b\0dextract_digit\1c\0feliminate_digit\1d\09next_term\1e\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
