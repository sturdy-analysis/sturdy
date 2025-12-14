(module $nbody.wasm
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32 i32) (result i32)))
  (type $t2 (func))
  (type $t3 (func (param i32)))
  (type $t4 (func (result f64)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t0)))
  (import "env" "printf" (func $printf (type $t1)))
  (func $__wasm_call_ctors (type $t2))
  (func $advance (type $t3) (param $p0 i32)
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
            i32.const 1136
            i32.add
            f64.load
            local.set $l21
            local.get $l20
            i32.const 1088
            i32.add
            f64.load
            local.set $l22
            local.get $l20
            i32.const 1040
            i32.add
            f64.load
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
            i32.const 1184
            i32.add
            f64.load
            local.set $l27
            local.get $l18
            local.set $l20
            local.get $l17
            local.set $l28
            loop $L4
              local.get $l26
              local.get $l26
              f64.load
              local.get $l20
              i32.const 1192
              i32.add
              f64.load
              local.tee $l29
              local.get $l23
              local.get $l20
              i32.const 1048
              i32.add
              f64.load
              f64.sub
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
              local.get $l30
              local.get $l30
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
              local.get $l29
              local.get $l32
              f64.mul
              local.get $l33
              f64.mul
              f64.sub
              f64.store
              local.get $l24
              local.get $l24
              f64.load
              local.get $l29
              local.get $l31
              f64.mul
              local.get $l33
              f64.mul
              f64.sub
              f64.store
              local.get $l20
              i32.const 1240
              i32.add
              local.tee $l34
              local.get $l30
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
  (func $energy (type $t4) (result f64)
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
  (func $offset_momentum (type $t2)
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
  (func $init (type $t2)
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
  (global $z i32 (i32.const 1136))
  (global $y i32 (i32.const 1088))
  (global $x i32 (i32.const 1040))
  (global $vz i32 (i32.const 1328))
  (global $vy i32 (i32.const 1280))
  (global $vx i32 (i32.const 1232))
  (global $mass i32 (i32.const 1184))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1368))
  (global $__stack_low i32 (i32.const 1376))
  (global $__stack_high i32 (i32.const 66912))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66912))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "advance" (func $advance))
  (export "z" (global $z))
  (export "y" (global $y))
  (export "x" (global $x))
  (export "vz" (global $vz))
  (export "vy" (global $vy))
  (export "vx" (global $vx))
  (export "mass" (global $mass))
  (export "energy" (func $energy))
  (export "offset_momentum" (func $offset_momentum))
  (export "init" (func $init))
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
  (data $.rodata (i32.const 1024) "%.9f\0a\00")
  (@custom ".debug_loc" "\ff\ff\ff\ff\06\00\00\00\00\00\00\00\c3\00\00\00\03\00\11\01\9fV\04\00\00X\04\00\00\04\00\ed\02\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00S\02\00\00U\02\00\00\04\00\ed\02\00\9fU\02\00\00]\04\00\00\04\00\ed\00\13\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00S\02\00\00U\02\00\00\04\00\ed\02\00\9fU\02\00\00Z\02\00\00\04\00\ed\00\13\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\ef\00\00\00>\02\00\00\04\00\ed\00\15\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\fd\00\00\00>\02\00\00\04\00\ed\00\16\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\0b\01\00\00>\02\00\00\04\00\ed\00\17\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00h\01\00\00j\01\00\00\04\00\ed\02\03\9fj\01\00\00>\02\00\00\04\00\ed\00\1e\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\a4\01\00\00\a6\01\00\00\04\00\ed\02\04\9f\a6\01\00\00>\02\00\00\04\00\ed\00!\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\97\01\00\00\99\01\00\00\04\00\ed\02\05\9f\99\01\00\00>\02\00\00\04\00\ed\00 \9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\83\01\00\00\85\01\00\00\04\00\ed\02\04\9f\85\01\00\00>\02\00\00\04\00\ed\00\1f\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\ad\01\00\00\af\01\00\00\04\00\ed\02\03\9f\af\01\00\00>\02\00\00\04\00\ed\00!\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00[\02\00\00\be\02\00\00\03\00\11\00\9f\be\02\00\00!\03\00\00\03\00\11\01\9f!\03\00\00\84\03\00\00\03\00\11\02\9f\84\03\00\00\e7\03\00\00\03\00\11\03\9f\e7\03\00\00J\04\00\00\03\00\11\04\9fJ\04\00\00]\04\00\00\03\00\11\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffh\04\00\00r\03\00\00s\03\00\00\04\00\ed\02\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffh\04\00\00g\03\00\00i\03\00\00\04\00\ed\02\05\9fi\03\00\00s\03\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffh\04\00\00]\03\00\00_\03\00\00\04\00\ed\02\04\9f_\03\00\00s\03\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffh\04\00\00S\03\00\00U\03\00\00\04\00\ed\02\03\9fU\03\00\00s\03\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ffh\04\00\00o\03\00\00p\03\00\00\04\00\ed\02\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\dd\07\00\00\00\00\00\00:\00\00\00\03\00\11\00\9f:\00\00\00E\00\00\00\03\00\11\04\9fE\00\00\00P\00\00\00\03\00\11\03\9fP\00\00\00f\00\00\00\03\00\11\02\9ff\00\00\00\fb\00\00\00\03\00\11\01\9f\fb\00\00\00p\01\00\00\03\00\11\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\dd\07\00\00\00\00\00\005\01\00\00\0a\00\9e\08\00\00\00\00\00\00\00\00]\01\00\00^\01\00\00\04\00\ed\02\02\9f^\01\00\00h\01\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\dd\07\00\00\ea\00\00\00\f4\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\dd\07\00\00\00\00\00\00\fb\00\00\00\0a\00\9e\08\00\00\00\00\00\00\00\00$\01\00\00.\01\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\ca\0b\00\00\1c\00\00\00u\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\074\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\08.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\09\05\00\02\18\03\0e:\0b;\0bI\13\00\00\0a4\00\02\17\03\0e:\0b;\0bI\13\00\00\0b\0b\01\11\01\12\06\00\00\0c.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\0d4\00\1c\0d\03\0e:\0b;\0bI\13\00\00\0e\0b\01U\17\00\00\0f.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b?\19\00\00\10.\00\11\01\12\06@\18\97B\19\03\0e:\0b;\0b?\19\00\00\11\89\82\01\001\13\11\01\00\00\12.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\13\05\00I\13\00\00\14\18\00\00\00\15\0f\00I\13\00\00\16&\00I\13\00\00\00")
  (@custom ".debug_info" "h\03\00\00\04\00\00\00\00\00\04\01\08\01\00\00\1d\00\e1\00\00\00\00\00\00\00w\00\00\00\00\00\00\00\b0\01\00\00\023\00\00\00\01\90\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\06\00\057\00\00\00\06\01\06\e9\00\00\00\08\07\07 \00\00\00^\00\00\00\01\0e\05\03\10\04\00\00\03j\00\00\00\04F\00\00\00\05\00\05_\00\00\00\04\08\07\17\00\00\00^\00\00\00\01\0e\05\03@\04\00\00\07\07\00\00\00^\00\00\00\01\0e\05\03p\04\00\00\07\19\00\00\00^\00\00\00\01\0f\05\03\d0\04\00\00\07\09\00\00\00^\00\00\00\01\0f\05\03\00\05\00\00\07\00\00\00\00^\00\00\00\01\0f\05\030\05\00\00\072\00\00\00^\00\00\00\01\10\05\03\a0\04\00\00\08\06\00\00\00`\04\00\00\07\ed\03\00\00\00\00\9ff\00\00\00\01\13\09\04\ed\00\00\9f<\00\00\00\01\13Z\03\00\00\0a\83\00\00\00\ff\00\00\00\01\18j\00\00\00\0a\a1\00\00\00\02\01\00\00\01\17j\00\00\00\0a\bf\00\00\00\05\01\00\00\01\16j\00\00\00\0a\dd\00\00\00\1f\00\00\00\01\15j\00\00\00\0a\09\01\00\00\fd\00\00\00\01\1bj\00\00\00\0a5\01\00\00\16\00\00\00\01\19j\00\00\00\0aa\01\00\00\06\00\00\00\01\1aj\00\00\00\0a\8d\01\00\00T\00\00\00\01\1cj\00\00\00\0b\1d\00\00\00H\04\00\00\0a\00\00\00\00N\00\00\00\01\1dZ\03\00\00\0b\df\00\00\00\86\01\00\00\0a+\00\00\00R\00\00\00\01\1fZ\03\00\00\0b\df\00\00\00k\01\00\00\0aW\00\00\00P\00\00\00\01$Z\03\00\00\00\00\0be\02\00\00\eb\01\00\00\0a\b9\01\00\00R\00\00\00\017Z\03\00\00\00\00\00\0ch\04\00\00s\03\00\00\07\ed\03\00\00\00\00\9f\0f\00\00\00\01Aj\00\00\00\0a\17\02\00\00u\00\00\00\01Cj\00\00\00\0bh\04\00\00r\03\00\00\0d\05R\00\00\00\01DZ\03\00\00\0e\00\00\00\00\0d\05P\00\00\00\01GZ\03\00\00\0e\c8\00\00\00\0a5\02\00\00\16\00\00\00\01Jj\00\00\00\0aa\02\00\00\1f\00\00\00\01Ij\00\00\00\0a\8d\02\00\00\06\00\00\00\01Kj\00\00\00\0a\b9\02\00\00n\00\00\00\01Lj\00\00\00\00\00\00\00\0f\dd\07\00\00p\01\00\00\07\ed\03\00\00\00\00\9f>\00\00\00\01T\0a5\03\00\00\03\00\00\00\01Vj\00\00\00\0au\03\00\00\0c\00\00\00\01Vj\00\00\00\0a\93\03\00\00\1c\00\00\00\01Vj\00\00\00\0e\90\01\00\00\0a\d7\02\00\00R\00\00\00\01WZ\03\00\00\00\00\10O\09\00\00z\02\00\00\07\ed\03\00\00\00\00\9f-\00\00\00\01c\0c\ca\0b\00\00u\00\00\00\04\ed\00\00\9f\22\00\00\00\01\8bZ\03\00\00\0a\c5\03\00\00<\00\00\00\01\8dZ\03\00\00\11\ca\02\00\00\ec\0b\00\00\11p\02\00\00\f2\0b\00\00\11\db\01\00\00\fa\0b\00\00\11H\03\00\00\0e\0c\00\00\11\d7\00\00\00\17\0c\00\00\11\db\01\00\00\1f\0c\00\00\11H\03\00\000\0c\00\00\00\12X\00\00\00\02\f3Z\03\00\00\13a\03\00\00\14\00\05)\00\00\00\05\04\15f\03\00\00\16?\00\00\00\00")
  (@custom ".debug_ranges" "h\04\00\00\7f\04\00\00\80\04\00\00\82\04\00\00\8e\04\00\00\90\04\00\00\9c\04\00\00\9e\04\00\00\ad\04\00\00\c1\04\00\00\c2\04\00\00\c4\04\00\00\d0\04\00\00\d2\04\00\00\de\04\00\00\e0\04\00\00\ef\04\00\00\03\05\00\00\04\05\00\00\06\05\00\00\12\05\00\00\14\05\00\00 \05\00\00\22\05\00\001\05\00\00E\05\00\00F\05\00\00H\05\00\00T\05\00\00V\05\00\00b\05\00\00d\05\00\00s\05\00\00u\05\00\00\88\05\00\00\8a\05\00\00\96\05\00\00\98\05\00\00\a4\05\00\00\a6\05\00\00\bf\05\00\00\e6\06\00\00\e7\06\00\00_\07\00\00`\07\00\00\b0\07\00\00\b1\07\00\00\d9\07\00\00\00\00\00\00\00\00\00\00h\04\00\00\7f\04\00\00\80\04\00\00\82\04\00\00\8e\04\00\00\90\04\00\00\9c\04\00\00\9e\04\00\00\ad\04\00\00\c1\04\00\00\c2\04\00\00\c4\04\00\00\d0\04\00\00\d2\04\00\00\de\04\00\00\e0\04\00\00\ef\04\00\00\03\05\00\00\04\05\00\00\06\05\00\00\12\05\00\00\14\05\00\00 \05\00\00\22\05\00\001\05\00\00E\05\00\00F\05\00\00H\05\00\00T\05\00\00V\05\00\00b\05\00\00d\05\00\00s\05\00\00u\05\00\00\88\05\00\00\8a\05\00\00\96\05\00\00\98\05\00\00\a4\05\00\00\a6\05\00\00\bf\05\00\00\e6\06\00\00\e7\06\00\00_\07\00\00`\07\00\00\b0\07\00\00\b1\07\00\00\d9\07\00\00\00\00\00\00\00\00\00\00\dd\07\00\00\d0\08\00\00\d8\08\00\00\0a\09\00\00\12\09\00\00D\09\00\00\00\00\00\00\00\00\00\00\06\00\00\00f\04\00\00h\04\00\00\db\07\00\00\dd\07\00\00M\09\00\00O\09\00\00\c9\0b\00\00\ca\0b\00\00?\0c\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "vz\00pz\00dz\00vy\00py\00energy\00dy\00vx\00px\00dx\00_start\00int\00init\00mass\00char\00n\00offset_momentum\00k\00j\00i\00mag\00printf\00double\00advance\00distance\00/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00nbody.c\00__ARRAY_SIZE_TYPE__\00R\00z1\00y1\00x1\00clang version 19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)\00")
  (@custom ".debug_line" "$\06\00\00\04\003\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00nbody.c\00\00\00\00stdlib.h\00\01\00\00\00\00\05\02\06\00\00\00\03\13\01\05\17\0a\03\09\08f\05\05\06 \03c.\05#\06\03$\02\bf\01\01\06\03\5c<\05\17\06\03.\02f\01\05\1f\06\08\12\05\1b\06\03x\f2\05\19\06J\05\17\06(\05\1b\08T\05\19\06J\05\13\06!\05\1b\e1\05\19\06J\05\13\06!\05\18V\05\13Z\22\05\15!\05\1f!\05#\06t\05\1a \05\17\06!u\08/\08\9f\05\1d\06 \05\17t\06\08=\05\1d\06 \05\17t\06\08=\05\1d\06 \05\17t\05#\06\03q\f2\05\0d\06\82\03\5cf\05\09\06\03\1fJ\05\00\06\03a\d6\05\1b\03\1fX\05\09 \03a<\05\1a\06\039J\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\03E\c8\05\1a\06\039J\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\03E\c8\05\1a\06\039J\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\03E\c8\05\1a\06\039J\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\03E\c8\05\1a\06\039J\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\05\1a\06\08\13\05\12\06\f2\05\17\06\03b\c8\05\1d\06\ac\03c \05\05\03\1d.\05\01\06\03!t\02\01\00\01\01\05\1d\0a\00\05\02k\04\00\00\03\cc\00\01\05\12\03y\08<\05\1d'\05>\03y.\05<\06t\05\1d\06_\05\1e\03y.\05,\06t\05\1d\06_\05.\03y.\054\06t\05,X\05< \05\0b \05\1d\06'\05\12\03y\08<\05\1d'\05>\03y.\05<\06t\05\1d\06_\05\1e\03y.\05,\06t\05\1d\06_\05.\03y.\054\06t\05,X\05< \05\0b \05\1d\06'\05\12\03y\08<\05\1d'\05>\03y.\05<\06t\05\1d\06_\05\1e\03y.\05,\06t\05\1d\06_\05.\03y.\054\06t\05,X\05< \05\0b \05\1d\06'\05\12\03y\08<\05\1d'\05>\03y.\05<\06t\05\1d\06_\05\1e\03y.\05,\06t\05\1d\06_\05.\03y.\054\06t\05,X\05< \05\0b \05\1d\06'\05\13\03y.\05\12\06\08 \05\1d\06'\05>\03y.\05<\06t\05\1d\06_\05\1e\03y.\05,\06t\05\1d\06_\05.\03y.\054\06t\05,X\05< \05\0b \05\1b\06\b3\05\1d\06X\05 \06\c6\05\1e\06t\056\06=\05\1dY\05 \c4\05\1e\06t\05,\06?\05\1dY\05 \c5\05\1e\06t\051\06>\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\1b \05\1dX\05 \06H\05\1e\06t\056\06=\05\1dY\05 F\05\1e\06t\05,\06?\05\1dY\05 G\05\1e\06t\051\06>\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\1b \05\1dX\05 \06H\05\1e\06t\056\06=\05\1dY\05 F\05\1e\06t\05,\06?\05\1dY\05 G\05\1e\06t\051\06>\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\1b \05\1dX\05 \06H\05\1e\06t\056\06=\05\1dY\05 F\05\1e\06t\05,\06?\05\1dY\05 G\05\1e\06t\051\06>\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\0b\06\03y \05\1b'\05\1eV\056Y\05\1eU\05,[\05\1eV\051Z\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\1b \05\1e\06V\056Y\05\1eU\05,[\05\1eV\051Z\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\1b \05\1e\06V\056Y\05\1eU\05,[\05\1eV\051Z\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\0b\06\03y \05\1b'\05\1eV\056Y\05\1eU\05,[\05\1eV\051Z\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\1b \05\1e\06V\056Y\05\1eU\05,[\05\1eV\051Z\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\0b\06\03y \05\1b'\05\1eV\056Y\05\1eU\05,[\05\1eV\051Z\05,\06X\056 \05\1f \05&\06!\05\0f\06 \05\0b\06\03y \05\05\03\0a \02\01\00\01\01\05\0f\0a\00\05\02\e0\07\00\00\03\d8\00\010\8e0\8e0\8e0\8e0\8e\02;\13s\05\17\06.\05\0c\06u\05\0f;/s\05\17\06.\05\0c\06u\05\0f;/s\05\17\06.\05\0c\06u\05\0f;/s\05\17\06.\05\0c\06u\05\0f;/s\05\17\06.\05\0c\06u\06\03\a6\7f\08\12\05\11\06\03\de\00\90\05\0b\06 \05\0f\06o\05\0c\06.\03\a7\7f\02'\01\05\11\06\03\dd\00\90\05\0b\06 \05\0f\06p\05\0c0\06\03\a5\7f\02'\01\05\11\06\03\df\00\90\05\0b\06 \05\01\06u\02\01\00\01\01\00\05\02O\09\00\00\03\e3\00\01\05\0a\0aZ\ab\ae\05\0b\ad\ad\ad\05\0d\08=\05\0a\08=\08=\08=\05\0b\08/\08=\08=\05\0d\08/\05\0a\08=\08=\08=\05\0b\08=\08/\05\0d\080\05\0b\08-\05\0a\08>\08/\08=\05\0b\08/\08/\08=\05\0d\08/\05\0a\08=\08/\08/\05\0b\08/\08/\08=\05\0d\08/\05\01u\02\01\00\01\01\00\05\02\ca\0b\00\00\03\8b\01\01\05\0d\0a\08=\05\05\83g\05\16g\05\05\06\82\06\08K\05\16\83\05\05\06\82\06:h\91\02\0e\00\01\01")
  (@custom "name" "\00\0b\0anbody.wasm\01c\08\00\15__VERIFIER_nondet_int\01\06printf\02\11__wasm_call_ctors\03\07advance\04\06energy\05\0foffset_momentum\06\04init\07\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clangZ19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)")
  (@custom "target_features" "\04+\0amultivalue+\0fmutable-globals+\0freference-types+\08sign-ext"))
