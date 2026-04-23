(module $nbody.wasm
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32 i32) (result i32)))
  (type $t2 (func (param i32)))
  (type $t3 (func (result f64)))
  (type $t4 (func))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t0)))
  (import "env" "printf" (func $printf (type $t1)))
  (func $advance (type $t2) (param $p0 i32)
    (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i32) (local $l20 i32) (local $l21 f64) (local $l22 f64) (local $l23 f64) (local $l24 i32) (local $l25 i32) (local $l26 i32) (local $l27 f64) (local $l28 i32) (local $l29 f64) (local $l30 f64) (local $l31 f64) (local $l32 f64) (local $l33 f64) (local $l34 i32)
    block $B0
      local.get $p0
      i32.const 1
      i32.lt_s
      br_if $B0
      i32.const 0
      f64.load offset=1168
      local.set $l1
      i32.const 0
      f64.load offset=1120
      local.set $l2
      i32.const 0
      f64.load offset=1072
      local.set $l3
      i32.const 0
      f64.load offset=1160
      local.set $l4
      i32.const 0
      f64.load offset=1112
      local.set $l5
      i32.const 0
      f64.load offset=1064
      local.set $l6
      i32.const 0
      f64.load offset=1152
      local.set $l7
      i32.const 0
      f64.load offset=1104
      local.set $l8
      i32.const 0
      f64.load offset=1056
      local.set $l9
      i32.const 0
      f64.load offset=1144
      local.set $l10
      i32.const 0
      f64.load offset=1096
      local.set $l11
      i32.const 0
      f64.load offset=1048
      local.set $l12
      i32.const 0
      f64.load offset=1136
      local.set $l13
      i32.const 0
      f64.load offset=1088
      local.set $l14
      i32.const 0
      f64.load offset=1040
      local.set $l15
      i32.const 1
      local.set $l16
      loop $L1
        i32.const 4
        local.set $l17
        i32.const 0
        local.set $l18
        i32.const 0
        local.set $l19
        loop $L2
          block $B3
            local.get $l19
            i32.const 3
            i32.gt_u
            br_if $B3
            local.get $l19
            i32.const 3
            i32.shl
            local.tee $l20
            f64.load offset=1136
            local.set $l21
            local.get $l20
            f64.load offset=1088
            local.set $l22
            local.get $l20
            f64.load offset=1040
            local.set $l23
            local.get $l20
            i32.const 1328
            i32.add
            local.set $l24
            local.get $l20
            i32.const 1280
            i32.add
            local.set $l25
            local.get $l20
            i32.const 1232
            i32.add
            local.set $l26
            local.get $l20
            f64.load offset=1184
            local.set $l27
            local.get $l18
            local.set $l20
            local.get $l17
            local.set $l28
            loop $L4
              local.get $l26
              local.get $l26
              f64.load
              local.get $l23
              local.get $l20
              i32.const 1048
              i32.add
              f64.load
              f64.sub
              local.tee $l29
              local.get $l20
              i32.const 1192
              i32.add
              f64.load
              local.tee $l30
              f64.mul
              f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
              local.get $l21
              local.get $l20
              i32.const 1144
              i32.add
              f64.load
              f64.sub
              local.tee $l31
              local.get $l31
              f64.mul
              local.get $l22
              local.get $l20
              i32.const 1096
              i32.add
              f64.load
              f64.sub
              local.tee $l32
              local.get $l32
              f64.mul
              local.get $l29
              local.get $l29
              f64.mul
              f64.add
              f64.add
              f64.sqrt
              local.tee $l33
              local.get $l33
              local.get $l33
              f64.mul
              f64.mul
              f64.div
              local.tee $l33
              f64.mul
              f64.sub
              f64.store
              local.get $l25
              local.get $l25
              f64.load
              local.get $l32
              local.get $l30
              f64.mul
              local.get $l33
              f64.mul
              f64.sub
              f64.store
              local.get $l24
              local.get $l24
              f64.load
              local.get $l31
              local.get $l30
              f64.mul
              local.get $l33
              f64.mul
              f64.sub
              f64.store
              local.get $l20
              i32.const 1240
              i32.add
              local.tee $l34
              local.get $l29
              local.get $l27
              f64.mul
              local.get $l33
              f64.mul
              local.get $l34
              f64.load
              f64.add
              f64.store
              local.get $l20
              i32.const 1288
              i32.add
              local.tee $l34
              local.get $l32
              local.get $l27
              f64.mul
              local.get $l33
              f64.mul
              local.get $l34
              f64.load
              f64.add
              f64.store
              local.get $l20
              i32.const 1336
              i32.add
              local.tee $l34
              local.get $l31
              local.get $l27
              f64.mul
              local.get $l33
              f64.mul
              local.get $l34
              f64.load
              f64.add
              f64.store
              local.get $l20
              i32.const 8
              i32.add
              local.set $l20
              local.get $l28
              i32.const -1
              i32.add
              local.tee $l28
              br_if $L4
            end
          end
          local.get $l18
          i32.const 8
          i32.add
          local.set $l18
          local.get $l17
          i32.const -1
          i32.add
          local.set $l17
          local.get $l19
          i32.const 1
          i32.add
          local.tee $l19
          i32.const 5
          i32.ne
          br_if $L2
        end
        i32.const 0
        i32.const 0
        f64.load offset=1232
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l15
        f64.add
        local.tee $l15
        f64.store offset=1040
        i32.const 0
        i32.const 0
        f64.load offset=1280
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l14
        f64.add
        local.tee $l14
        f64.store offset=1088
        i32.const 0
        i32.const 0
        f64.load offset=1328
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l13
        f64.add
        local.tee $l13
        f64.store offset=1136
        i32.const 0
        i32.const 0
        f64.load offset=1240
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l12
        f64.add
        local.tee $l12
        f64.store offset=1048
        i32.const 0
        i32.const 0
        f64.load offset=1288
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l11
        f64.add
        local.tee $l11
        f64.store offset=1096
        i32.const 0
        i32.const 0
        f64.load offset=1336
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l10
        f64.add
        local.tee $l10
        f64.store offset=1144
        i32.const 0
        i32.const 0
        f64.load offset=1248
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l9
        f64.add
        local.tee $l9
        f64.store offset=1056
        i32.const 0
        i32.const 0
        f64.load offset=1296
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l8
        f64.add
        local.tee $l8
        f64.store offset=1104
        i32.const 0
        i32.const 0
        f64.load offset=1344
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l7
        f64.add
        local.tee $l7
        f64.store offset=1152
        i32.const 0
        i32.const 0
        f64.load offset=1256
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l6
        f64.add
        local.tee $l6
        f64.store offset=1064
        i32.const 0
        i32.const 0
        f64.load offset=1304
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l5
        f64.add
        local.tee $l5
        f64.store offset=1112
        i32.const 0
        i32.const 0
        f64.load offset=1352
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l4
        f64.add
        local.tee $l4
        f64.store offset=1160
        i32.const 0
        i32.const 0
        f64.load offset=1264
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l3
        f64.add
        local.tee $l3
        f64.store offset=1072
        i32.const 0
        i32.const 0
        f64.load offset=1312
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l2
        f64.add
        local.tee $l2
        f64.store offset=1120
        i32.const 0
        i32.const 0
        f64.load offset=1360
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l1
        f64.add
        local.tee $l1
        f64.store offset=1168
        local.get $l16
        local.get $p0
        i32.eq
        local.set $l20
        local.get $l16
        i32.const 1
        i32.add
        local.set $l16
        local.get $l20
        i32.eqz
        br_if $L1
      end
    end)
  (func $energy (type $t3) (result f64)
    (local $l0 f64) (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 f64) (local $l17 f64)
    i32.const 0
    f64.load offset=1216
    local.tee $l0
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 0
    f64.load offset=1360
    local.tee $l1
    local.get $l1
    f64.mul
    i32.const 0
    f64.load offset=1264
    local.tee $l1
    local.get $l1
    f64.mul
    i32.const 0
    f64.load offset=1312
    local.tee $l1
    local.get $l1
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 0
    f64.load offset=1208
    local.tee $l1
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 0
    f64.load offset=1352
    local.tee $l2
    local.get $l2
    f64.mul
    i32.const 0
    f64.load offset=1256
    local.tee $l2
    local.get $l2
    f64.mul
    i32.const 0
    f64.load offset=1304
    local.tee $l2
    local.get $l2
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 0
    f64.load offset=1200
    local.tee $l2
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 0
    f64.load offset=1344
    local.tee $l3
    local.get $l3
    f64.mul
    i32.const 0
    f64.load offset=1248
    local.tee $l3
    local.get $l3
    f64.mul
    i32.const 0
    f64.load offset=1296
    local.tee $l3
    local.get $l3
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 0
    f64.load offset=1192
    local.tee $l3
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 0
    f64.load offset=1336
    local.tee $l4
    local.get $l4
    f64.mul
    i32.const 0
    f64.load offset=1240
    local.tee $l4
    local.get $l4
    f64.mul
    i32.const 0
    f64.load offset=1288
    local.tee $l4
    local.get $l4
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 0
    f64.load offset=1184
    local.tee $l4
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 0
    f64.load offset=1328
    local.tee $l5
    local.get $l5
    f64.mul
    i32.const 0
    f64.load offset=1232
    local.tee $l5
    local.get $l5
    f64.mul
    i32.const 0
    f64.load offset=1280
    local.tee $l5
    local.get $l5
    f64.mul
    f64.add
    f64.add
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    local.get $l4
    local.get $l3
    f64.mul
    i32.const 0
    f64.load offset=1136
    local.tee $l5
    i32.const 0
    f64.load offset=1144
    local.tee $l6
    f64.sub
    local.tee $l7
    local.get $l7
    f64.mul
    i32.const 0
    f64.load offset=1040
    local.tee $l7
    i32.const 0
    f64.load offset=1048
    local.tee $l8
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    i32.const 0
    f64.load offset=1088
    local.tee $l9
    i32.const 0
    f64.load offset=1096
    local.tee $l10
    f64.sub
    local.tee $l11
    local.get $l11
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l4
    local.get $l2
    f64.mul
    local.get $l5
    i32.const 0
    f64.load offset=1152
    local.tee $l11
    f64.sub
    local.tee $l12
    local.get $l12
    f64.mul
    local.get $l7
    i32.const 0
    f64.load offset=1056
    local.tee $l12
    f64.sub
    local.tee $l13
    local.get $l13
    f64.mul
    local.get $l9
    i32.const 0
    f64.load offset=1104
    local.tee $l13
    f64.sub
    local.tee $l14
    local.get $l14
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l4
    local.get $l1
    f64.mul
    local.get $l5
    i32.const 0
    f64.load offset=1160
    local.tee $l14
    f64.sub
    local.tee $l15
    local.get $l15
    f64.mul
    local.get $l7
    i32.const 0
    f64.load offset=1064
    local.tee $l15
    f64.sub
    local.tee $l16
    local.get $l16
    f64.mul
    local.get $l9
    i32.const 0
    f64.load offset=1112
    local.tee $l16
    f64.sub
    local.tee $l17
    local.get $l17
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l4
    local.get $l0
    f64.mul
    local.get $l5
    i32.const 0
    f64.load offset=1168
    local.tee $l4
    f64.sub
    local.tee $l5
    local.get $l5
    f64.mul
    local.get $l7
    i32.const 0
    f64.load offset=1072
    local.tee $l5
    f64.sub
    local.tee $l7
    local.get $l7
    f64.mul
    local.get $l9
    i32.const 0
    f64.load offset=1120
    local.tee $l7
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    f64.add
    local.get $l3
    local.get $l2
    f64.mul
    local.get $l6
    local.get $l11
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    local.get $l8
    local.get $l12
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    local.get $l10
    local.get $l13
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l3
    local.get $l1
    f64.mul
    local.get $l6
    local.get $l14
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    local.get $l8
    local.get $l15
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    local.get $l10
    local.get $l16
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l3
    local.get $l0
    f64.mul
    local.get $l6
    local.get $l4
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l8
    local.get $l5
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l10
    local.get $l7
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    f64.add
    local.get $l2
    local.get $l1
    f64.mul
    local.get $l11
    local.get $l14
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l12
    local.get $l15
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l13
    local.get $l16
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l2
    local.get $l0
    f64.mul
    local.get $l11
    local.get $l4
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l12
    local.get $l5
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l13
    local.get $l7
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    f64.add
    local.get $l1
    local.get $l0
    f64.mul
    local.get $l14
    local.get $l4
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l15
    local.get $l5
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
    f64.add)
  (func $offset_momentum (type $t4)
    (local $l0 f64) (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64)
    i32.const 0
    f64.load offset=1360
    local.set $l0
    i32.const 0
    f64.load offset=1352
    local.set $l1
    i32.const 0
    f64.load offset=1344
    local.set $l2
    i32.const 0
    f64.load offset=1328
    local.set $l3
    i32.const 0
    f64.load offset=1336
    local.set $l4
    i32.const 0
    f64.load offset=1264
    local.set $l5
    i32.const 0
    f64.load offset=1256
    local.set $l6
    i32.const 0
    f64.load offset=1248
    local.set $l7
    i32.const 0
    f64.load offset=1232
    local.set $l8
    i32.const 0
    f64.load offset=1240
    local.set $l9
    i32.const 0
    i32.const 0
    f64.load offset=1312
    i32.const 0
    f64.load offset=1216
    local.tee $l10
    f64.mul
    i32.const 0
    f64.load offset=1304
    i32.const 0
    f64.load offset=1208
    local.tee $l11
    f64.mul
    i32.const 0
    f64.load offset=1296
    i32.const 0
    f64.load offset=1200
    local.tee $l12
    f64.mul
    i32.const 0
    f64.load offset=1288
    i32.const 0
    f64.load offset=1192
    local.tee $l13
    f64.mul
    i32.const 0
    f64.load offset=1280
    i32.const 0
    f64.load offset=1184
    local.tee $l14
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    f64.add
    f64.add
    f64.add
    f64.add
    f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
    f64.div
    f64.store offset=1280
    i32.const 0
    local.get $l5
    local.get $l10
    f64.mul
    local.get $l6
    local.get $l11
    f64.mul
    local.get $l7
    local.get $l12
    f64.mul
    local.get $l9
    local.get $l13
    f64.mul
    local.get $l8
    local.get $l14
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    f64.add
    f64.add
    f64.add
    f64.add
    f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
    f64.div
    f64.store offset=1232
    i32.const 0
    local.get $l0
    local.get $l10
    f64.mul
    local.get $l1
    local.get $l11
    f64.mul
    local.get $l2
    local.get $l12
    f64.mul
    local.get $l4
    local.get $l13
    f64.mul
    local.get $l3
    local.get $l14
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    f64.add
    f64.add
    f64.add
    f64.add
    f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
    f64.div
    f64.store offset=1328)
  (func $init (type $t4)
    i32.const 0
    i64.const 0
    i64.store offset=1088
    i32.const 0
    i64.const 0
    i64.store offset=1040
    i32.const 0
    i64.const 0
    i64.store offset=1136
    i32.const 0
    i64.const 0
    i64.store offset=1232
    i32.const 0
    i64.const 0
    i64.store offset=1280
    i32.const 0
    i64.const 0
    i64.store offset=1328
    i32.const 0
    i64.const 4630752910647379422
    i64.store offset=1184
    i32.const 0
    i64.const 4617136985637443884
    i64.store offset=1048
    i32.const 0
    i64.const -4615467600764216452
    i64.store offset=1096
    i32.const 0
    i64.const -4631240860977730576
    i64.store offset=1144
    i32.const 0
    i64.const 4603636522180398268
    i64.store offset=1240
    i32.const 0
    i64.const 4613514450253485211
    i64.store offset=1288
    i32.const 0
    i64.const -4640446117579192555
    i64.store offset=1336
    i32.const 0
    i64.const 4585593052079010776
    i64.store offset=1192
    i32.const 0
    i64.const 4620886515960171111
    i64.store offset=1056
    i32.const 0
    i64.const 4616330128746480048
    i64.store offset=1104
    i32.const 0
    i64.const -4622431185293064580
    i64.store offset=1152
    i32.const 0
    i64.const -4616141094713322430
    i64.store offset=1248
    i32.const 0
    i64.const 4610900871547424531
    i64.store offset=1296
    i32.const 0
    i64.const 4577659745833829943
    i64.store offset=1200
    i32.const 0
    i64.const 4576004977915405236
    i64.store offset=1344
    i32.const 0
    i64.const 4623448502799161807
    i64.store offset=1064
    i32.const 0
    i64.const -4598675596822288770
    i64.store offset=1112
    i32.const 0
    i64.const -4626158513131520608
    i64.store offset=1160
    i32.const 0
    i64.const 4607555276345777135
    i64.store offset=1256
    i32.const 0
    i64.const 4605999890795117509
    i64.store offset=1304
    i32.const 0
    i64.const -4645973824767902084
    i64.store offset=1352
    i32.const 0
    i64.const 4565592097032511155
    i64.store offset=1208
    i32.const 0
    i64.const 4624847617829197610
    i64.store offset=1072
    i32.const 0
    i64.const -4595383180696444384
    i64.store offset=1120
    i32.const 0
    i64.const 4595626498235032896
    i64.store offset=1168
    i32.const 0
    i64.const 4606994084859067466
    i64.store offset=1264
    i32.const 0
    i64.const 4603531791922690979
    i64.store offset=1312
    i32.const 0
    i64.const -4638202354754755082
    i64.store offset=1360
    i32.const 0
    i64.const 4566835785178257836
    i64.store offset=1216)
  (func $_start (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    call $__VERIFIER_nondet_int
    local.set $l1
    call $init
    call $offset_momentum
    local.get $l0
    call $energy
    f64.store offset=16
    i32.const 1024
    local.get $l0
    i32.const 16
    i32.add
    call $printf
    drop
    local.get $l1
    call $advance
    local.get $l0
    call $energy
    f64.store
    i32.const 1024
    local.get $l0
    call $printf
    drop
    local.get $l0
    i32.const 32
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66912))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "%.9f\0a\00")
  (@custom "name" "\00\0b\0anbody.wasm\01P\07\00\15__VERIFIER_nondet_int\01\06printf\02\07advance\03\06energy\04\0foffset_momentum\05\04init\06\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
