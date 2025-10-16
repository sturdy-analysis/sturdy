(module
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32 i32) (result i32)))
  (type $t2 (func))
  (type $t3 (func (param i32 i32 f64 i32)))
  (type $t4 (func (param i32 i32) (result f64)))
  (type $t5 (func (param i32 i32)))
  (import "env" "__VERIFIER_nondet_int" (func $env.__VERIFIER_nondet_int (type $t0)))
  (import "env" "printf" (func $env.printf (type $t1)))
  (func $__wasm_call_ctors (type $t2))
  (func $advance (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 f64) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 i32) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32)
    block $B0
      local.get $p3
      i32.eqz
      br_if $B0
      local.get $p0
      i32.const 0
      i32.le_s
      br_if $B0
      local.get $p0
      i32.const 1
      i32.sub
      local.set $l14
      loop $L1
        local.get $p3
        i32.const 1
        i32.sub
        local.set $p3
        i32.const 0
        local.set $l7
        local.get $l14
        local.set $l8
        local.get $p1
        local.set $l4
        loop $L2
          local.get $l8
          local.set $l12
          local.get $l4
          i32.const 56
          i32.add
          local.tee $l15
          local.set $l5
          local.get $p0
          local.get $l7
          i32.const 1
          i32.add
          local.tee $l7
          i32.gt_s
          if $I3
            loop $L4
              local.get $l4
              local.get $l4
              f64.load offset=24
              local.get $l5
              i32.const 48
              i32.add
              local.tee $l13
              f64.load
              local.get $l4
              f64.load
              local.get $l5
              f64.load
              f64.sub
              local.tee $l9
              f64.mul
              local.get $p2
              local.get $l4
              f64.load offset=16
              local.get $l5
              f64.load offset=16
              f64.sub
              local.tee $l10
              local.get $l10
              f64.mul
              local.get $l9
              local.get $l9
              f64.mul
              local.get $l4
              f64.load offset=8
              local.get $l5
              f64.load offset=8
              f64.sub
              local.tee $l11
              local.get $l11
              f64.mul
              f64.add
              f64.add
              local.tee $l6
              local.get $l6
              f64.sqrt
              f64.mul
              f64.div
              local.tee $l6
              f64.mul
              f64.sub
              f64.store offset=24
              local.get $l4
              local.get $l4
              f64.load offset=32
              local.get $l13
              f64.load
              local.get $l11
              f64.mul
              local.get $l6
              f64.mul
              f64.sub
              f64.store offset=32
              local.get $l4
              local.get $l4
              f64.load offset=40
              local.get $l13
              f64.load
              local.get $l10
              f64.mul
              local.get $l6
              f64.mul
              f64.sub
              f64.store offset=40
              local.get $l5
              local.get $l9
              local.get $l4
              f64.load offset=48
              f64.mul
              local.get $l6
              f64.mul
              local.get $l5
              f64.load offset=24
              f64.add
              f64.store offset=24
              local.get $l5
              local.get $l11
              local.get $l4
              f64.load offset=48
              f64.mul
              local.get $l6
              f64.mul
              local.get $l5
              f64.load offset=32
              f64.add
              f64.store offset=32
              local.get $l5
              local.get $l10
              local.get $l4
              f64.load offset=48
              f64.mul
              local.get $l6
              f64.mul
              local.get $l5
              f64.load offset=40
              f64.add
              f64.store offset=40
              local.get $l5
              i32.const 56
              i32.add
              local.set $l5
              local.get $l12
              i32.const 1
              i32.sub
              local.tee $l12
              br_if $L4
            end
          end
          local.get $l8
          i32.const 1
          i32.sub
          local.set $l8
          local.get $l15
          local.set $l4
          local.get $p0
          local.get $l7
          i32.ne
          br_if $L2
        end
        local.get $p0
        local.set $l5
        local.get $p1
        local.set $l4
        loop $L5
          local.get $l4
          local.get $p2
          local.get $l4
          f64.load offset=24
          f64.mul
          local.get $l4
          f64.load
          f64.add
          f64.store
          local.get $l4
          local.get $p2
          local.get $l4
          f64.load offset=32
          f64.mul
          local.get $l4
          f64.load offset=8
          f64.add
          f64.store offset=8
          local.get $l4
          local.get $p2
          local.get $l4
          f64.load offset=40
          f64.mul
          local.get $l4
          f64.load offset=16
          f64.add
          f64.store offset=16
          local.get $l4
          i32.const 56
          i32.add
          local.set $l4
          local.get $l5
          i32.const 1
          i32.sub
          local.tee $l5
          br_if $L5
        end
        local.get $p3
        br_if $L1
      end
    end)
  (func $energy (type $t4) (param $p0 i32) (param $p1 i32) (result f64)
    (local $l2 f64) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 f64) (local $l9 f64) (local $l10 f64)
    block $B0
      local.get $p0
      i32.const 0
      i32.le_s
      if $I1
        br $B0
      end
      local.get $p0
      i32.const 1
      i32.sub
      local.set $l4
      loop $L2
        local.get $l2
        local.get $p1
        local.tee $l3
        f64.load offset=24
        local.tee $l2
        local.get $l2
        local.get $l3
        f64.load offset=48
        local.tee $l6
        f64.mul
        f64.mul
        f64.const 0x1p-1 (;=0.5;)
        f64.mul
        f64.add
        local.get $l3
        f64.load offset=32
        local.tee $l2
        local.get $l6
        local.get $l2
        f64.mul
        f64.mul
        f64.const 0x1p-1 (;=0.5;)
        f64.mul
        f64.add
        local.get $l3
        f64.load offset=40
        local.tee $l2
        local.get $l6
        local.get $l2
        f64.mul
        f64.mul
        f64.const 0x1p-1 (;=0.5;)
        f64.mul
        f64.add
        local.set $l2
        local.get $l3
        i32.const 56
        i32.add
        local.set $p1
        local.get $p0
        local.get $l5
        i32.const 1
        i32.add
        local.tee $l5
        i32.gt_s
        if $I3
          local.get $l3
          f64.load offset=16
          local.set $l8
          local.get $l3
          f64.load offset=8
          local.set $l9
          local.get $l3
          f64.load
          local.set $l10
          local.get $l4
          local.set $l7
          local.get $p1
          local.set $l3
          loop $L4
            local.get $l2
            local.get $l6
            local.get $l3
            f64.load offset=48
            f64.mul
            local.get $l8
            local.get $l3
            f64.load offset=16
            f64.sub
            local.tee $l2
            local.get $l2
            f64.mul
            local.get $l10
            local.get $l3
            f64.load
            f64.sub
            local.tee $l2
            local.get $l2
            f64.mul
            local.get $l9
            local.get $l3
            f64.load offset=8
            f64.sub
            local.tee $l2
            local.get $l2
            f64.mul
            f64.add
            f64.add
            f64.sqrt
            f64.div
            f64.sub
            local.set $l2
            local.get $l3
            i32.const 56
            i32.add
            local.set $l3
            local.get $l7
            i32.const 1
            i32.sub
            local.tee $l7
            br_if $L4
          end
        end
        local.get $l4
        i32.const 1
        i32.sub
        local.set $l4
        local.get $p0
        local.get $l5
        i32.ne
        br_if $L2
      end
    end
    local.get $l2)
  (func $offset_momentum (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64)
    local.get $p0
    i32.const 0
    i32.gt_s
    if $I0
      local.get $p1
      i32.const 48
      i32.add
      local.set $l2
      local.get $p1
      f64.load offset=40
      local.set $l3
      local.get $p1
      f64.load offset=32
      local.set $l4
      local.get $p1
      f64.load offset=24
      local.set $l5
      loop $L1
        local.get $p1
        local.get $l5
        local.get $l2
        i32.const 24
        i32.sub
        f64.load
        local.get $l2
        f64.load
        local.tee $l6
        f64.mul
        f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
        f64.div
        f64.sub
        local.tee $l5
        f64.store offset=24
        local.get $p1
        local.get $l4
        local.get $l6
        local.get $l2
        i32.const 16
        i32.sub
        f64.load
        f64.mul
        f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
        f64.div
        f64.sub
        local.tee $l4
        f64.store offset=32
        local.get $p1
        local.get $l3
        local.get $l6
        local.get $l2
        i32.const 8
        i32.sub
        f64.load
        f64.mul
        f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
        f64.div
        f64.sub
        local.tee $l3
        f64.store offset=40
        local.get $l2
        i32.const 56
        i32.add
        local.set $l2
        local.get $p0
        i32.const 1
        i32.sub
        local.tee $p0
        br_if $L1
      end
    end)
  (func $_start (type $t0) (result i32)
    (local $l0 f64) (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 f64) (local $l17 i32) (local $l18 f64) (local $l19 f64) (local $l20 f64) (local $l21 f64) (local $l22 f64) (local $l23 f64) (local $l24 i32) (local $l25 f64) (local $l26 f64) (local $l27 f64) (local $l28 f64) (local $l29 f64) (local $l30 f64) (local $l31 f64) (local $l32 f64) (local $l33 f64) (local $l34 f64) (local $l35 f64) (local $l36 f64) (local $l37 f64) (local $l38 f64) (local $l39 f64) (local $l40 f64) (local $l41 f64) (local $l42 f64) (local $l43 f64)
    global.get $g0
    i32.const 32
    i32.sub
    local.tee $l17
    global.set $g0
    call $env.__VERIFIER_nondet_int
    local.set $l24
    i32.const 1248
    f64.load
    local.set $l18
    i32.const 1192
    f64.load
    local.set $l19
    i32.const 1080
    f64.load
    local.set $l4
    i32.const 1136
    f64.load
    local.set $l20
    i32.const 1240
    f64.load
    local.set $l21
    i32.const 1184
    f64.load
    local.set $l22
    i32.const 1072
    f64.load
    local.set $l8
    i32.const 1128
    f64.load
    local.set $l23
    i32.const 1064
    i32.const 1064
    f64.load
    local.tee $l1
    local.get $l1
    i32.const 1088
    f64.load
    local.tee $l1
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1120
    f64.load
    local.tee $l25
    i32.const 1144
    f64.load
    local.tee $l0
    f64.mul
    local.tee $l26
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1176
    f64.load
    local.tee $l27
    i32.const 1200
    f64.load
    local.tee $l5
    f64.mul
    local.tee $l28
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1232
    f64.load
    local.tee $l29
    i32.const 1256
    f64.load
    local.tee $l6
    f64.mul
    local.tee $l30
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1288
    f64.load
    local.tee $l31
    i32.const 1312
    f64.load
    local.tee $l9
    f64.mul
    local.tee $l32
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.tee $l7
    f64.store
    i32.const 1072
    local.get $l8
    local.get $l1
    local.get $l8
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l0
    local.get $l23
    f64.mul
    local.tee $l33
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l5
    local.get $l22
    f64.mul
    local.tee $l34
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l6
    local.get $l21
    f64.mul
    local.tee $l35
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l9
    i32.const 1296
    f64.load
    local.tee $l36
    f64.mul
    local.tee $l37
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.tee $l8
    f64.store
    i32.const 1080
    local.get $l4
    local.get $l1
    local.get $l4
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l0
    local.get $l20
    f64.mul
    local.tee $l38
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l5
    local.get $l19
    f64.mul
    local.tee $l39
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l6
    local.get $l18
    f64.mul
    local.tee $l40
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get $l9
    i32.const 1304
    f64.load
    local.tee $l41
    f64.mul
    local.tee $l42
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.tee $l4
    f64.store
    local.get $l17
    local.get $l7
    local.get $l1
    local.get $l7
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    local.get $l8
    local.get $l1
    local.get $l8
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l4
    local.get $l1
    local.get $l4
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l1
    local.get $l0
    f64.mul
    i32.const 1056
    f64.load
    local.tee $l4
    i32.const 1112
    f64.load
    local.tee $l8
    f64.sub
    local.tee $l7
    local.get $l7
    f64.mul
    i32.const 1040
    f64.load
    local.tee $l7
    i32.const 1096
    f64.load
    local.tee $l10
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    i32.const 1048
    f64.load
    local.tee $l3
    i32.const 1104
    f64.load
    local.tee $l16
    f64.sub
    local.tee $l13
    local.get $l13
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l1
    local.get $l5
    f64.mul
    local.get $l4
    i32.const 1168
    f64.load
    local.tee $l13
    f64.sub
    local.tee $l14
    local.get $l14
    f64.mul
    local.get $l7
    i32.const 1152
    f64.load
    local.tee $l14
    f64.sub
    local.tee $l15
    local.get $l15
    f64.mul
    local.get $l3
    i32.const 1160
    f64.load
    local.tee $l15
    f64.sub
    local.tee $l11
    local.get $l11
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l1
    local.get $l6
    f64.mul
    local.get $l4
    i32.const 1224
    f64.load
    local.tee $l11
    f64.sub
    local.tee $l12
    local.get $l12
    f64.mul
    local.get $l7
    i32.const 1208
    f64.load
    local.tee $l12
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l3
    i32.const 1216
    f64.load
    local.tee $l2
    f64.sub
    local.tee $l43
    local.get $l43
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l1
    local.get $l9
    f64.mul
    local.get $l4
    i32.const 1280
    f64.load
    local.tee $l1
    f64.sub
    local.tee $l4
    local.get $l4
    f64.mul
    local.get $l7
    i32.const 1264
    f64.load
    local.tee $l4
    f64.sub
    local.tee $l7
    local.get $l7
    f64.mul
    local.get $l3
    i32.const 1272
    f64.load
    local.tee $l7
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l25
    local.get $l26
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l23
    local.get $l33
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l20
    local.get $l38
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l0
    local.get $l5
    f64.mul
    local.get $l8
    local.get $l13
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l10
    local.get $l14
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l16
    local.get $l15
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l0
    local.get $l6
    f64.mul
    local.get $l8
    local.get $l11
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l10
    local.get $l12
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l16
    local.get $l2
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l0
    local.get $l9
    f64.mul
    local.get $l8
    local.get $l1
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l10
    local.get $l4
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l16
    local.get $l7
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l27
    local.get $l28
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l22
    local.get $l34
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l19
    local.get $l39
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l5
    local.get $l6
    f64.mul
    local.get $l13
    local.get $l11
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l14
    local.get $l12
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l15
    local.get $l2
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l5
    local.get $l9
    f64.mul
    local.get $l13
    local.get $l1
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l14
    local.get $l4
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l15
    local.get $l7
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l29
    local.get $l30
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l21
    local.get $l35
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l18
    local.get $l40
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l6
    local.get $l9
    f64.mul
    local.get $l11
    local.get $l1
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    local.get $l12
    local.get $l4
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    local.get $l2
    local.get $l7
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l31
    local.get $l32
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l36
    local.get $l37
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l41
    local.get $l42
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    f64.store offset=16
    i32.const 1024
    local.get $l17
    i32.const 16
    i32.add
    call $env.printf
    drop
    i32.const 5
    i32.const 1040
    f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
    local.get $l24
    call $advance
    local.get $l17
    i32.const 1064
    f64.load
    local.tee $l1
    local.get $l1
    i32.const 1088
    f64.load
    local.tee $l1
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    i32.const 1072
    f64.load
    local.tee $l0
    local.get $l1
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1080
    f64.load
    local.tee $l0
    local.get $l1
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l1
    i32.const 1144
    f64.load
    local.tee $l0
    f64.mul
    i32.const 1056
    f64.load
    local.tee $l9
    i32.const 1112
    f64.load
    local.tee $l4
    f64.sub
    local.tee $l5
    local.get $l5
    f64.mul
    i32.const 1040
    f64.load
    local.tee $l8
    i32.const 1096
    f64.load
    local.tee $l7
    f64.sub
    local.tee $l5
    local.get $l5
    f64.mul
    i32.const 1048
    f64.load
    local.tee $l10
    i32.const 1104
    f64.load
    local.tee $l3
    f64.sub
    local.tee $l5
    local.get $l5
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l1
    i32.const 1200
    f64.load
    local.tee $l5
    f64.mul
    local.get $l9
    i32.const 1168
    f64.load
    local.tee $l16
    f64.sub
    local.tee $l6
    local.get $l6
    f64.mul
    local.get $l8
    i32.const 1152
    f64.load
    local.tee $l13
    f64.sub
    local.tee $l6
    local.get $l6
    f64.mul
    local.get $l10
    i32.const 1160
    f64.load
    local.tee $l14
    f64.sub
    local.tee $l6
    local.get $l6
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l1
    i32.const 1256
    f64.load
    local.tee $l6
    f64.mul
    local.get $l9
    i32.const 1224
    f64.load
    local.tee $l15
    f64.sub
    local.tee $l11
    local.get $l11
    f64.mul
    local.get $l8
    i32.const 1208
    f64.load
    local.tee $l11
    f64.sub
    local.tee $l12
    local.get $l12
    f64.mul
    local.get $l10
    i32.const 1216
    f64.load
    local.tee $l12
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l1
    i32.const 1312
    f64.load
    local.tee $l1
    f64.mul
    local.get $l9
    i32.const 1280
    f64.load
    local.tee $l9
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l8
    i32.const 1264
    f64.load
    local.tee $l8
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l10
    i32.const 1272
    f64.load
    local.tee $l10
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    i32.const 1120
    f64.load
    local.tee $l2
    local.get $l0
    local.get $l2
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1128
    f64.load
    local.tee $l2
    local.get $l0
    local.get $l2
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1136
    f64.load
    local.tee $l2
    local.get $l0
    local.get $l2
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l0
    local.get $l5
    f64.mul
    local.get $l4
    local.get $l16
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l7
    local.get $l13
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l3
    local.get $l14
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l0
    local.get $l6
    f64.mul
    local.get $l4
    local.get $l15
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l7
    local.get $l11
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l3
    local.get $l12
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l0
    local.get $l1
    f64.mul
    local.get $l4
    local.get $l9
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l7
    local.get $l8
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l3
    local.get $l10
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    i32.const 1176
    f64.load
    local.tee $l0
    local.get $l5
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1184
    f64.load
    local.tee $l0
    local.get $l5
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1192
    f64.load
    local.tee $l0
    local.get $l5
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l5
    local.get $l6
    f64.mul
    local.get $l16
    local.get $l15
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l13
    local.get $l11
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l14
    local.get $l12
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l5
    local.get $l1
    f64.mul
    local.get $l16
    local.get $l9
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l13
    local.get $l8
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l14
    local.get $l10
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    i32.const 1232
    f64.load
    local.tee $l0
    local.get $l6
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1240
    f64.load
    local.tee $l0
    local.get $l6
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1248
    f64.load
    local.tee $l0
    local.get $l6
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    local.get $l6
    local.get $l1
    f64.mul
    local.get $l15
    local.get $l9
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l11
    local.get $l8
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l12
    local.get $l10
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    i32.const 1288
    f64.load
    local.tee $l0
    local.get $l1
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1296
    f64.load
    local.tee $l0
    local.get $l1
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    i32.const 1304
    f64.load
    local.tee $l0
    local.get $l1
    local.get $l0
    f64.mul
    f64.mul
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    f64.add
    f64.store
    i32.const 1024
    local.get $l17
    call $env.printf
    drop
    local.get $l17
    i32.const 32
    i32.add
    global.set $g0
    i32.const 0)
  (memory $memory 2)
  (global $g0 (mut i32) (i32.const 66864))
  (global $bodies i32 (i32.const 1040))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1320))
  (global $__stack_low i32 (i32.const 1328))
  (global $__stack_high i32 (i32.const 66864))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66864))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "advance" (func $advance))
  (export "energy" (func $energy))
  (export "offset_momentum" (func $offset_momentum))
  (export "_start" (func $_start))
  (export "bodies" (global $bodies))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (data $d0 (i32.const 1024) "%.9f\0a")
  (data $d1 (i32.const 1088) "\deE\be\c9<\bdC@,\d9<4\a0]\13@|\db\1f\c0\ab\90\f2\bf\f0\eb%l\f9\86\ba\bf\bc\cc\93\9b\06g\e3?\9b\94}\f5\f2~\06@\15\07Z\9a\d7\d2\99\bf\d83\ab\d9\95L\a3?g\ca2\c3\cd\af @\b0\01\de1\cb\7f\10@|F\eb\e1S\d3\d9\bfB\94\87\b8!,\f0\bf\13\8f\1f\bf\e95\fd?\b4#\11_H<\81?7\c6\07\0dI\1d\87?\cf\d9\a7\ce\ea\c9)@~f&\d6\e88.\c0\a0}%\beW\95\cc\bf\ef\1b\91\a9\1cS\f1?\c5\bbT>\7f\cc\eb?|>\f2\fak/\86\bf\b3\1e\f4\9c\d2=\5c?*W\05\a9g\c2.@ \a2\c83X\eb9\c0@\e5\ab\93\f3\f1\c6?J\bcY\16\b6T\ef?\a3\fb\c41\c6\07\e3?\f6evX\88\cb\a1\bf\ac\99\17S\f3\a8`?"))
