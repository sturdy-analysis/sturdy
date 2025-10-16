(module
  (type $t0 (func (param i32 i32 i32) (result i32)))
  (type $t1 (func))
  (type $t2 (func (param i32) (result i32)))
  (type $t3 (func (param i32 i32 i32 i32) (result i32)))
  (type $t4 (func (param i32)))
  (type $t5 (func (param i32 i32)))
  (type $t6 (func (param i32 i32 i32)))
  (type $t7 (func (result i32)))
  (import "env" "strlen" (func $env.strlen (type $t2)))
  (import "env" "memcpy" (func $env.memcpy (type $t0)))
  (import "env" "fwrite" (func $env.fwrite (type $t3)))
  (import "env" "exit" (func $env.exit (type $t4)))
  (func $__wasm_call_ctors (type $t1))
  (func $repeat_And_Wrap_String (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    global.get $g0
    i32.const -64
    i32.add
    local.tee $l2
    local.set $l4
    local.get $l2
    global.set $g0
    local.get $l2
    local.get $p0
    call $env.strlen
    local.tee $l3
    i32.const 75
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee $l6
    global.set $g0
    block $B0
      local.get $l3
      i32.const -59
      i32.lt_s
      br_if $B0
      i32.const 1
      local.get $l3
      i32.const 60
      i32.add
      local.tee $l5
      local.get $l5
      i32.const 1
      i32.le_s
      select
      local.tee $l7
      i32.const 1
      i32.and
      i32.const 0
      local.set $l2
      local.get $l5
      i32.const 2
      i32.ge_s
      if $I1
        local.get $l7
        i32.const 2147483646
        i32.and
        local.set $l5
        loop $L2
          local.get $l2
          local.get $l6
          i32.add
          local.tee $l7
          local.get $p0
          local.get $l2
          local.get $l3
          i32.rem_s
          i32.add
          i32.load8_u
          i32.store8
          local.get $l7
          i32.const 1
          i32.add
          local.get $p0
          local.get $l2
          i32.const 1
          i32.add
          local.get $l3
          i32.rem_s
          i32.add
          i32.load8_u
          i32.store8
          local.get $l2
          i32.const 2
          i32.add
          local.tee $l2
          local.get $l5
          i32.ne
          br_if $L2
        end
      end
      i32.eqz
      br_if $B0
      local.get $l2
      local.get $l6
      i32.add
      local.get $p0
      local.get $l2
      local.get $l3
      i32.rem_s
      i32.add
      i32.load8_u
      i32.store8
    end
    local.get $l4
    i32.const 10
    i32.store8 offset=60
    local.get $p1
    i32.const 0
    i32.gt_s
    if $I3
      i32.const 0
      local.set $p0
      loop $L4
        i32.const 60
        local.set $l2
        local.get $p1
        i32.const 59
        i32.le_u
        if $I5
          local.get $p1
          local.get $l4
          i32.add
          i32.const 10
          i32.store8
          local.get $p1
          local.set $l2
        end
        local.get $l4
        local.get $p0
        local.get $l6
        i32.add
        local.get $l2
        call $env.memcpy
        local.get $l2
        i32.const 1
        i32.add
        i32.const 1
        i32.const 0
        i32.load
        call $env.fwrite
        drop
        local.get $p0
        local.get $l2
        i32.add
        local.tee $p0
        local.get $l3
        i32.const 0
        local.get $p0
        local.get $l3
        i32.gt_s
        select
        i32.sub
        local.set $p0
        local.get $p1
        local.get $l2
        i32.sub
        local.tee $p1
        i32.const 0
        i32.gt_s
        br_if $L4
      end
    end
    local.get $l4
    i32.const -64
    i32.sub
    global.set $g0)
  (func $rng_init (type $t1)
    i32.const 1568
    i32.const 0
    i32.store)
  (func $rng_gen_blk (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32)
    block $B0 (result i32)
      i32.const -1
      i32.const 1568
      i32.load
      local.get $p2
      i32.ne
      br_if $B0
      drop
      i32.const 1568
      local.get $p2
      i32.const 1
      i32.add
      local.tee $p2
      i32.const 0
      local.get $p2
      i32.const 1556
      i32.load
      i32.lt_s
      select
      i32.store
      i32.const 1564
      i32.const 1564
      i32.load
      local.tee $p2
      local.get $p2
      local.get $p1
      local.get $p1
      local.get $p2
      i32.gt_s
      select
      local.tee $p1
      i32.sub
      i32.store
      i32.const 0
      local.get $p1
      i32.eqz
      br_if $B0
      drop
      local.get $p1
      i32.const 1
      i32.and
      if $I1 (result i32)
        i32.const 1552
        i32.const 1552
        i32.load
        i32.const 3877
        i32.mul
        i32.const 29573
        i32.add
        i32.const 139968
        i32.rem_u
        local.tee $p2
        i32.store
        local.get $p0
        local.get $p2
        i32.store
        local.get $p0
        i32.const 4
        i32.add
        local.set $p0
        local.get $p1
        i32.const 1
        i32.sub
      else
        local.get $p1
      end
      local.set $l3
      local.get $p1
      i32.const 1
      i32.ne
      if $I2
        loop $L3
          i32.const 1552
          i32.const 1552
          i32.load
          i32.const 3877
          i32.mul
          i32.const 29573
          i32.add
          i32.const 139968
          i32.rem_u
          local.tee $p2
          i32.store
          local.get $p0
          local.get $p2
          i32.store
          i32.const 1552
          i32.const 1552
          i32.load
          i32.const 3877
          i32.mul
          i32.const 29573
          i32.add
          i32.const 139968
          i32.rem_u
          local.tee $p2
          i32.store
          local.get $p0
          i32.const 4
          i32.add
          local.get $p2
          i32.store
          local.get $p0
          i32.const 8
          i32.add
          local.set $p0
          local.get $l3
          i32.const 2
          i32.sub
          local.tee $l3
          br_if $L3
        end
      end
      local.get $p1
    end)
  (func $out_write (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32)
    i32.const -1
    local.set $l3
    local.get $p2
    i32.const 1572
    i32.load
    i32.eq
    if $I0 (result i32)
      i32.const 1572
      local.get $p2
      i32.const 1
      i32.add
      local.tee $p2
      i32.const 0
      local.get $p2
      i32.const 1560
      i32.load
      i32.lt_s
      select
      i32.store
      local.get $p0
      local.get $p1
      i32.const 1
      i32.const 0
      i32.load
      call $env.fwrite
    else
      i32.const -1
    end)
  (func $generate_And_Wrap_Pseudorandom_DNA_Sequence (type $t6) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 f32) (local $l11 i32) (local $l12 f32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    global.get $g0
    i32.const 308224
    i32.sub
    local.tee $l3
    local.set $l7
    local.get $l3
    global.set $g0
    local.get $l3
    local.get $p1
    i32.const 2
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee $l13
    global.set $g0
    block $B0
      local.get $p1
      i32.const 0
      i32.le_s
      local.tee $l16
      br_if $B0
      local.get $p1
      i32.const 1
      i32.and
      block $B1
        local.get $p1
        i32.const 1
        i32.eq
        if $I2
          br $B1
        end
        local.get $p0
        i32.const 12
        i32.add
        local.set $l3
        local.get $p1
        i32.const 2147483646
        i32.and
        local.set $l11
        local.get $l13
        local.set $l4
        loop $L3
          local.get $l4
          block $B4 (result i32)
            local.get $l10
            local.get $l3
            i32.const 8
            i32.sub
            f32.load
            f32.add
            local.tee $l12
            f32.const 0x1.116p+17 (;=139968;)
            f32.mul
            local.tee $l10
            f32.const 0x1p+32 (;=4.29497e+09;)
            f32.lt
            local.get $l10
            f32.const 0x0p+0 (;=0;)
            f32.ge
            i32.and
            if $I5
              local.get $l10
              i32.trunc_f32_u
              br $B4
            end
            i32.const 0
          end
          i32.const 1
          i32.add
          i32.store
          local.get $l4
          i32.const 4
          i32.add
          block $B6 (result i32)
            local.get $l12
            local.get $l3
            f32.load
            f32.add
            local.tee $l10
            f32.const 0x1.116p+17 (;=139968;)
            f32.mul
            local.tee $l12
            f32.const 0x1p+32 (;=4.29497e+09;)
            f32.lt
            local.get $l12
            f32.const 0x0p+0 (;=0;)
            f32.ge
            i32.and
            if $I7
              local.get $l12
              i32.trunc_f32_u
              br $B6
            end
            i32.const 0
          end
          i32.const 1
          i32.add
          i32.store
          local.get $l4
          i32.const 8
          i32.add
          local.set $l4
          local.get $l3
          i32.const 16
          i32.add
          local.set $l3
          local.get $l11
          local.get $l6
          i32.const 2
          i32.add
          local.tee $l6
          i32.ne
          br_if $L3
        end
      end
      i32.eqz
      br_if $B0
      local.get $l13
      local.get $l6
      i32.const 2
      i32.shl
      i32.add
      block $B8 (result i32)
        local.get $l10
        local.get $p0
        local.get $l6
        i32.const 3
        i32.shl
        i32.add
        f32.load offset=4
        f32.add
        f32.const 0x1.116p+17 (;=139968;)
        f32.mul
        local.tee $l12
        f32.const 0x1p+32 (;=4.29497e+09;)
        f32.lt
        local.get $l12
        f32.const 0x0p+0 (;=0;)
        f32.ge
        i32.and
        if $I9
          local.get $l12
          i32.trunc_f32_u
          br $B8
        end
        i32.const 0
      end
      i32.const 1
      i32.add
      i32.store
    end
    i32.const 1564
    local.get $p2
    i32.store
    i32.const 1572
    i32.const 0
    i32.store
    i32.const 1568
    i32.const 0
    i32.store
    local.get $p1
    i32.const 2147483644
    i32.and
    local.set $l17
    local.get $p1
    i32.const 3
    i32.and
    local.set $l15
    local.get $l7
    i32.const 4
    i32.or
    local.set $l14
    i32.const 0
    local.set $l3
    block $B10
      loop $L11
        i32.const 1556
        i32.load
        i32.const 1
        i32.gt_s
        local.set $l9
        i32.const 1552
        i32.load
        local.set $l4
        block $B12
          block $B13
            block $B14
              block $B15
                loop $L16
                  i32.const -1
                  local.set $l8
                  local.get $l3
                  i32.eqz
                  if $I17
                    i32.const 1568
                    local.get $l9
                    i32.store
                    i32.const 1564
                    local.get $p2
                    i32.const 61440
                    local.get $p2
                    local.get $p2
                    i32.const 61440
                    i32.ge_s
                    select
                    local.tee $l8
                    i32.sub
                    local.tee $l11
                    i32.store
                    local.get $p2
                    i32.eqz
                    br_if $B15
                    local.get $l7
                    local.set $l3
                    local.get $l8
                    i32.const 1
                    i32.and
                    if $I18 (result i32)
                      local.get $l7
                      local.get $l4
                      i32.const 3877
                      i32.mul
                      i32.const 29573
                      i32.add
                      i32.const 139968
                      i32.rem_u
                      local.tee $l4
                      i32.store
                      local.get $l14
                      local.set $l3
                      local.get $l8
                      i32.const 1
                      i32.sub
                    else
                      local.get $l8
                    end
                    local.set $l6
                    local.get $p2
                    i32.const 1
                    i32.ne
                    if $I19
                      loop $L20
                        local.get $l3
                        local.get $l4
                        i32.const 3877
                        i32.mul
                        i32.const 29573
                        i32.add
                        i32.const 139968
                        i32.rem_u
                        local.tee $p2
                        i32.store
                        local.get $l3
                        i32.const 4
                        i32.add
                        local.get $p2
                        i32.const 3877
                        i32.mul
                        i32.const 29573
                        i32.add
                        i32.const 139968
                        i32.rem_u
                        local.tee $l4
                        i32.store
                        local.get $l3
                        i32.const 8
                        i32.add
                        local.set $l3
                        local.get $l6
                        i32.const 2
                        i32.sub
                        local.tee $l6
                        br_if $L20
                      end
                    end
                    i32.const 1552
                    local.get $l4
                    i32.store
                    local.get $l9
                    local.set $l3
                    local.get $l11
                    local.set $p2
                  end
                  local.get $l8
                  i32.const -1
                  i32.eq
                  br_if $L16
                end
                local.get $l8
                i32.eqz
                br_if $B15
                local.get $l8
                i32.const 0
                i32.le_s
                if $I21
                  local.get $l7
                  i32.const 245760
                  i32.add
                  local.set $l5
                  br $B12
                end
                local.get $l16
                br_if $B14
                local.get $l7
                i32.const 245760
                i32.add
                local.set $l5
                i32.const 0
                local.set $l11
                i32.const 0
                local.set $l4
                loop $L22
                  local.get $l7
                  local.get $l11
                  i32.const 2
                  i32.shl
                  i32.add
                  i32.load
                  local.set $l9
                  i32.const 0
                  local.set $l6
                  i32.const 0
                  local.set $p2
                  local.get $p1
                  i32.const 4
                  i32.ge_u
                  if $I23
                    local.get $l13
                    local.set $l3
                    loop $L24
                      local.get $l6
                      local.get $l3
                      i32.load
                      local.get $l9
                      i32.le_u
                      i32.add
                      local.get $l3
                      i32.const 4
                      i32.add
                      i32.load
                      local.get $l9
                      i32.le_u
                      i32.add
                      local.get $l3
                      i32.const 8
                      i32.add
                      i32.load
                      local.get $l9
                      i32.le_u
                      i32.add
                      local.get $l3
                      i32.const 12
                      i32.add
                      i32.load
                      local.get $l9
                      i32.le_u
                      i32.add
                      local.set $l6
                      local.get $l3
                      i32.const 16
                      i32.add
                      local.set $l3
                      local.get $l17
                      local.get $p2
                      i32.const 4
                      i32.add
                      local.tee $p2
                      i32.ne
                      br_if $L24
                    end
                  end
                  local.get $l15
                  if $I25
                    local.get $l13
                    local.get $p2
                    i32.const 2
                    i32.shl
                    i32.add
                    local.set $l3
                    local.get $l15
                    local.set $p2
                    loop $L26
                      local.get $l6
                      local.get $l3
                      i32.load
                      local.get $l9
                      i32.le_u
                      i32.add
                      local.set $l6
                      local.get $l3
                      i32.const 4
                      i32.add
                      local.set $l3
                      local.get $p2
                      i32.const 1
                      i32.sub
                      local.tee $p2
                      br_if $L26
                    end
                  end
                  local.get $l5
                  local.get $p0
                  local.get $l6
                  i32.const 3
                  i32.shl
                  i32.add
                  i32.load8_u
                  i32.store8
                  block $B27 (result i32)
                    local.get $l4
                    i32.const 58
                    i32.le_s
                    if $I28
                      local.get $l5
                      i32.const 1
                      i32.add
                      local.set $l5
                      local.get $l4
                      i32.const 1
                      i32.add
                      br $B27
                    end
                    local.get $l5
                    i32.const 10
                    i32.store8 offset=1
                    local.get $l5
                    i32.const 2
                    i32.add
                    local.set $l5
                    i32.const 0
                  end
                  local.set $l4
                  local.get $l11
                  i32.const 1
                  i32.add
                  local.tee $l11
                  local.get $l8
                  i32.ne
                  br_if $L22
                end
                br $B13
              end
              local.get $l7
              i32.const 308224
              i32.add
              global.set $g0
              return
            end
            block $B29
              local.get $l8
              i32.const 1
              i32.eq
              if $I30
                local.get $l7
                i32.const 245760
                i32.add
                local.set $l5
                i32.const 0
                local.set $l4
                br $B29
              end
              local.get $l8
              i32.const 2147483646
              i32.and
              local.set $l6
              local.get $l7
              i32.const 245760
              i32.add
              local.set $l5
              i32.const 0
              local.set $l4
              loop $L31
                local.get $l5
                local.get $p0
                i32.load8_u
                i32.store8
                block $B32 (result i32)
                  local.get $l4
                  i32.const 59
                  i32.lt_s
                  if $I33
                    local.get $l4
                    i32.const 1
                    i32.add
                    local.set $l4
                    local.get $l5
                    i32.const 1
                    i32.add
                    br $B32
                  end
                  local.get $l5
                  i32.const 10
                  i32.store8 offset=1
                  i32.const 0
                  local.set $l4
                  local.get $l5
                  i32.const 2
                  i32.add
                end
                local.tee $l3
                local.get $p0
                i32.load8_u
                i32.store8
                block $B34 (result i32)
                  local.get $l4
                  i32.const 59
                  i32.lt_s
                  if $I35
                    local.get $l3
                    i32.const 1
                    i32.add
                    local.set $l5
                    local.get $l4
                    i32.const 1
                    i32.add
                    br $B34
                  end
                  local.get $l3
                  i32.const 10
                  i32.store8 offset=1
                  local.get $l3
                  i32.const 2
                  i32.add
                  local.set $l5
                  i32.const 0
                end
                local.set $l4
                local.get $l6
                i32.const 2
                i32.sub
                local.tee $l6
                br_if $L31
              end
            end
            local.get $l8
            i32.const 1
            i32.and
            i32.eqz
            br_if $B13
            local.get $l5
            local.get $p0
            i32.load8_u
            i32.store8
            local.get $l4
            i32.const 59
            i32.lt_s
            if $I36
              local.get $l4
              i32.const 1
              i32.add
              local.set $l4
              local.get $l5
              i32.const 1
              i32.add
              local.set $l5
              br $B13
            end
            local.get $l5
            i32.const 10
            i32.store8 offset=1
            local.get $l5
            i32.const 2
            i32.add
            local.set $l5
            i32.const 0
            local.set $l4
          end
          local.get $l4
          i32.eqz
          br_if $B12
          local.get $l5
          i32.const 10
          i32.store8
          local.get $l5
          i32.const 1
          i32.add
          local.set $l5
        end
        i32.const 1572
        i32.load
        br_if $B10
        local.get $l5
        local.get $l7
        i32.const 245760
        i32.add
        i32.sub
        local.set $p2
        i32.const 0
        local.set $l4
        loop $L37
          i32.const -1
          local.set $l3
          local.get $l4
          i32.eqz
          if $I38
            i32.const 1572
            i32.const 1560
            i32.load
            i32.const 1
            i32.gt_s
            i32.store
            local.get $l7
            i32.const 245760
            i32.add
            local.get $p2
            i32.const 1
            i32.const 0
            i32.load
            call $env.fwrite
            local.set $l3
            i32.const 1572
            i32.load
            local.set $l4
          end
          local.get $l3
          i32.const -1
          i32.eq
          br_if $L37
        end
        local.get $l3
        if $I39
          i32.const 1564
          i32.load
          local.set $p2
          i32.const 1568
          i32.load
          local.set $l3
          br $L11
        end
      end
      i32.const 1
      call $env.exit
      unreachable
    end
    loop $L40
      br $L40
    end
    unreachable)
  (func $_start (type $t7) (result i32)
    i32.const 1055
    i32.const 22
    i32.const 1
    i32.const 0
    i32.load
    call $env.fwrite
    drop
    i32.const 1104
    i32.const 5000
    call $repeat_And_Wrap_String
    i32.const 1568
    i32.const 0
    i32.store
    i32.const 1078
    i32.const 25
    i32.const 1
    i32.const 0
    i32.load
    call $env.fwrite
    drop
    i32.const 1392
    i32.const 15
    i32.const 7500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    i32.const 1024
    i32.const 30
    i32.const 1
    i32.const 0
    i32.load
    call $env.fwrite
    drop
    i32.const 1520
    i32.const 4
    i32.const 12500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    i32.const 0)
  (memory $memory 2)
  (global $g0 (mut i32) (i32.const 67120))
  (global $rng_tid i32 (i32.const 1568))
  (global $rng_tnum i32 (i32.const 1556))
  (global $rng_cnt i32 (i32.const 1564))
  (global $seed i32 (i32.const 1552))
  (global $out_tid i32 (i32.const 1572))
  (global $out_tnum i32 (i32.const 1560))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1576))
  (global $__stack_low i32 (i32.const 1584))
  (global $__stack_high i32 (i32.const 67120))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 67120))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "repeat_And_Wrap_String" (func $repeat_And_Wrap_String))
  (export "rng_init" (func $rng_init))
  (export "rng_tid" (global $rng_tid))
  (export "rng_gen_blk" (func $rng_gen_blk))
  (export "rng_tnum" (global $rng_tnum))
  (export "rng_cnt" (global $rng_cnt))
  (export "seed" (global $seed))
  (export "out_init" (func $rng_init))
  (export "out_write" (func $out_write))
  (export "out_tid" (global $out_tid))
  (export "out_tnum" (global $out_tnum))
  (export "generate_And_Wrap_Pseudorandom_DNA_Sequence" (func $generate_And_Wrap_Pseudorandom_DNA_Sequence))
  (export "_start" (func $_start))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (data $d0 (i32.const 1024) ">THREE Homo sapiens frequency\0a\00>ONE Homo sapiens alu\0a\00>TWO IUB ambiguity codes\0a\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00a\00\00\00q=\8a>c\00\00\00\8f\c2\f5=g\00\00\00\8f\c2\f5=t\00\00\00q=\8a>B\00\00\00\0a\d7\a3<D\00\00\00\0a\d7\a3<H\00\00\00\0a\d7\a3<K\00\00\00\0a\d7\a3<M\00\00\00\0a\d7\a3<N\00\00\00\0a\d7\a3<R\00\00\00\0a\d7\a3<S\00\00\00\0a\d7\a3<V\00\00\00\0a\d7\a3<W\00\00\00\0a\d7\a3<Y\00\00\00\0a\d7\a3<\00\00\00\00\00\00\00\00a\00\00\00\e9\1c\9b>c\00\00\00r\bdJ>g\00\00\00\d7IJ>t\00\00\00r_\9a>")
  (data $d1 (i32.const 1552) "*\00\00\00\01\00\00\00\01"))
