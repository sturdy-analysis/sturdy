(module
  (type $t0 (func))
  (type $t1 (func (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func (result f64)))
  (import "env" "__VERIFIER_nondet_int" (func $env.__VERIFIER_nondet_int (type $t1)))
  (import "env" "printf" (func $env.printf (type $t2)))
  (func $__wasm_call_ctors (type $t0))
  (func $advance (type $t3) (param $p0 i32)
    (local $l1 i32) (local $l2 f64) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 f64) (local $l17 f64) (local $l18 f64) (local $l19 f64) (local $l20 f64) (local $l21 f64) (local $l22 f64) (local $l23 f64) (local $l24 f64) (local $l25 f64) (local $l26 f64) (local $l27 i32) (local $l28 i32) (local $l29 i32) (local $l30 i32) (local $l31 f64) (local $l32 f64) (local $l33 f64) (local $l34 f64)
    local.get $p0
    i32.const 0
    i32.gt_s
    if $I0
      i32.const 1168
      f64.load
      local.set $l12
      i32.const 1120
      f64.load
      local.set $l13
      i32.const 1072
      f64.load
      local.set $l14
      i32.const 1160
      f64.load
      local.set $l15
      i32.const 1112
      f64.load
      local.set $l16
      i32.const 1064
      f64.load
      local.set $l17
      i32.const 1152
      f64.load
      local.set $l18
      i32.const 1104
      f64.load
      local.set $l19
      i32.const 1056
      f64.load
      local.set $l20
      i32.const 1144
      f64.load
      local.set $l21
      i32.const 1096
      f64.load
      local.set $l22
      i32.const 1048
      f64.load
      local.set $l23
      i32.const 1136
      f64.load
      local.set $l24
      i32.const 1088
      f64.load
      local.set $l25
      i32.const 1040
      f64.load
      local.set $l26
      i32.const 1
      local.set $l5
      loop $L1
        i32.const 4
        local.set $l6
        i32.const 0
        local.set $l7
        i32.const 0
        local.set $l4
        loop $L2
          local.get $l4
          i32.const 3
          i32.le_u
          if $I3
            local.get $l4
            i32.const 3
            i32.shl
            local.tee $l1
            i32.const 1136
            i32.add
            f64.load
            local.set $l32
            local.get $l1
            i32.const 1088
            i32.add
            f64.load
            local.set $l33
            local.get $l1
            i32.const 1040
            i32.add
            f64.load
            local.set $l34
            local.get $l1
            i32.const 1328
            i32.add
            local.set $l27
            local.get $l1
            i32.const 1280
            i32.add
            local.set $l28
            local.get $l1
            i32.const 1232
            i32.add
            local.set $l29
            local.get $l1
            i32.const 1184
            i32.add
            f64.load
            local.set $l8
            local.get $l7
            local.set $l1
            local.get $l6
            local.set $l30
            loop $L4
              local.get $l29
              local.get $l29
              f64.load
              local.get $l1
              i32.const 1192
              i32.add
              f64.load
              local.tee $l31
              local.get $l34
              local.get $l1
              i32.const 1048
              i32.add
              f64.load
              f64.sub
              local.tee $l9
              f64.mul
              f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
              local.get $l32
              local.get $l1
              i32.const 1144
              i32.add
              f64.load
              f64.sub
              local.tee $l10
              local.get $l10
              f64.mul
              local.get $l33
              local.get $l1
              i32.const 1096
              i32.add
              f64.load
              f64.sub
              local.tee $l11
              local.get $l11
              f64.mul
              local.get $l9
              local.get $l9
              f64.mul
              f64.add
              f64.add
              f64.sqrt
              local.tee $l2
              local.get $l2
              local.get $l2
              f64.mul
              f64.mul
              f64.div
              local.tee $l2
              f64.mul
              f64.sub
              f64.store
              local.get $l28
              local.get $l28
              f64.load
              local.get $l31
              local.get $l11
              f64.mul
              local.get $l2
              f64.mul
              f64.sub
              f64.store
              local.get $l27
              local.get $l27
              f64.load
              local.get $l31
              local.get $l10
              f64.mul
              local.get $l2
              f64.mul
              f64.sub
              f64.store
              local.get $l1
              i32.const 1240
              i32.add
              local.tee $l3
              local.get $l9
              local.get $l8
              f64.mul
              local.get $l2
              f64.mul
              local.get $l3
              f64.load
              f64.add
              f64.store
              local.get $l1
              i32.const 1288
              i32.add
              local.tee $l3
              local.get $l11
              local.get $l8
              f64.mul
              local.get $l2
              f64.mul
              local.get $l3
              f64.load
              f64.add
              f64.store
              local.get $l1
              i32.const 1336
              i32.add
              local.tee $l3
              local.get $l10
              local.get $l8
              f64.mul
              local.get $l2
              f64.mul
              local.get $l3
              f64.load
              f64.add
              f64.store
              local.get $l1
              i32.const 8
              i32.add
              local.set $l1
              local.get $l30
              i32.const 1
              i32.sub
              local.tee $l30
              br_if $L4
            end
          end
          local.get $l7
          i32.const 8
          i32.add
          local.set $l7
          local.get $l6
          i32.const 1
          i32.sub
          local.set $l6
          local.get $l4
          i32.const 1
          i32.add
          local.tee $l4
          i32.const 5
          i32.ne
          br_if $L2
        end
        i32.const 1040
        i32.const 1232
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l26
        f64.add
        local.tee $l26
        f64.store
        i32.const 1088
        i32.const 1280
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l25
        f64.add
        local.tee $l25
        f64.store
        i32.const 1136
        i32.const 1328
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l24
        f64.add
        local.tee $l24
        f64.store
        i32.const 1048
        i32.const 1240
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l23
        f64.add
        local.tee $l23
        f64.store
        i32.const 1096
        i32.const 1288
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l22
        f64.add
        local.tee $l22
        f64.store
        i32.const 1144
        i32.const 1336
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l21
        f64.add
        local.tee $l21
        f64.store
        i32.const 1056
        i32.const 1248
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l20
        f64.add
        local.tee $l20
        f64.store
        i32.const 1104
        i32.const 1296
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l19
        f64.add
        local.tee $l19
        f64.store
        i32.const 1152
        i32.const 1344
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l18
        f64.add
        local.tee $l18
        f64.store
        i32.const 1064
        i32.const 1256
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l17
        f64.add
        local.tee $l17
        f64.store
        i32.const 1112
        i32.const 1304
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l16
        f64.add
        local.tee $l16
        f64.store
        i32.const 1160
        i32.const 1352
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l15
        f64.add
        local.tee $l15
        f64.store
        i32.const 1072
        i32.const 1264
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l14
        f64.add
        local.tee $l14
        f64.store
        i32.const 1120
        i32.const 1312
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l13
        f64.add
        local.tee $l13
        f64.store
        i32.const 1168
        i32.const 1360
        f64.load
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        local.get $l12
        f64.add
        local.tee $l12
        f64.store
        local.get $p0
        local.get $l5
        i32.eq
        local.get $l5
        i32.const 1
        i32.add
        local.set $l5
        i32.eqz
        br_if $L1
      end
    end)
  (func $energy (type $t4) (result f64)
    (local $l0 f64) (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 f64) (local $l17 f64)
    i32.const 1216
    f64.load
    local.tee $l6
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 1360
    f64.load
    local.tee $l7
    local.get $l7
    f64.mul
    i32.const 1264
    f64.load
    local.tee $l7
    local.get $l7
    f64.mul
    i32.const 1312
    f64.load
    local.tee $l7
    local.get $l7
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 1208
    f64.load
    local.tee $l7
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 1352
    f64.load
    local.tee $l2
    local.get $l2
    f64.mul
    i32.const 1256
    f64.load
    local.tee $l2
    local.get $l2
    f64.mul
    i32.const 1304
    f64.load
    local.tee $l2
    local.get $l2
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 1200
    f64.load
    local.tee $l2
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 1344
    f64.load
    local.tee $l0
    local.get $l0
    f64.mul
    i32.const 1248
    f64.load
    local.tee $l0
    local.get $l0
    f64.mul
    i32.const 1296
    f64.load
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 1192
    f64.load
    local.tee $l0
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 1336
    f64.load
    local.tee $l4
    local.get $l4
    f64.mul
    i32.const 1240
    f64.load
    local.tee $l4
    local.get $l4
    f64.mul
    i32.const 1288
    f64.load
    local.tee $l4
    local.get $l4
    f64.mul
    f64.add
    f64.add
    f64.mul
    i32.const 1184
    f64.load
    local.tee $l4
    f64.const 0x1p-1 (;=0.5;)
    f64.mul
    i32.const 1328
    f64.load
    local.tee $l3
    local.get $l3
    f64.mul
    i32.const 1232
    f64.load
    local.tee $l3
    local.get $l3
    f64.mul
    i32.const 1280
    f64.load
    local.tee $l3
    local.get $l3
    f64.mul
    f64.add
    f64.add
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    local.get $l4
    local.get $l0
    f64.mul
    i32.const 1136
    f64.load
    local.tee $l3
    i32.const 1144
    f64.load
    local.tee $l14
    f64.sub
    local.tee $l5
    local.get $l5
    f64.mul
    i32.const 1040
    f64.load
    local.tee $l5
    i32.const 1048
    f64.load
    local.tee $l15
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    i32.const 1088
    f64.load
    local.tee $l1
    i32.const 1096
    f64.load
    local.tee $l16
    f64.sub
    local.tee $l8
    local.get $l8
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l4
    local.get $l2
    f64.mul
    local.get $l3
    i32.const 1152
    f64.load
    local.tee $l8
    f64.sub
    local.tee $l9
    local.get $l9
    f64.mul
    local.get $l5
    i32.const 1056
    f64.load
    local.tee $l9
    f64.sub
    local.tee $l10
    local.get $l10
    f64.mul
    local.get $l1
    i32.const 1104
    f64.load
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
    local.get $l7
    f64.mul
    local.get $l3
    i32.const 1160
    f64.load
    local.tee $l11
    f64.sub
    local.tee $l12
    local.get $l12
    f64.mul
    local.get $l5
    i32.const 1064
    f64.load
    local.tee $l12
    f64.sub
    local.tee $l13
    local.get $l13
    f64.mul
    local.get $l1
    i32.const 1112
    f64.load
    local.tee $l13
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
    local.get $l6
    f64.mul
    local.get $l3
    i32.const 1168
    f64.load
    local.tee $l4
    f64.sub
    local.tee $l3
    local.get $l3
    f64.mul
    local.get $l5
    i32.const 1072
    f64.load
    local.tee $l3
    f64.sub
    local.tee $l5
    local.get $l5
    f64.mul
    local.get $l1
    i32.const 1120
    f64.load
    local.tee $l5
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    f64.add
    local.get $l0
    local.get $l2
    f64.mul
    local.get $l14
    local.get $l8
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    local.get $l15
    local.get $l9
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    local.get $l16
    local.get $l10
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l0
    local.get $l7
    f64.mul
    local.get $l14
    local.get $l11
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    local.get $l15
    local.get $l12
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    local.get $l16
    local.get $l13
    f64.sub
    local.tee $l1
    local.get $l1
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l0
    local.get $l6
    f64.mul
    local.get $l14
    local.get $l4
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l15
    local.get $l3
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l16
    local.get $l5
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    f64.add
    local.get $l2
    local.get $l7
    f64.mul
    local.get $l8
    local.get $l11
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l9
    local.get $l12
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    local.get $l10
    local.get $l13
    f64.sub
    local.tee $l0
    local.get $l0
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    local.get $l2
    local.get $l6
    f64.mul
    local.get $l8
    local.get $l4
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l9
    local.get $l3
    f64.sub
    local.tee $l2
    local.get $l2
    f64.mul
    local.get $l10
    local.get $l5
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
    local.get $l7
    local.get $l6
    f64.mul
    local.get $l11
    local.get $l4
    f64.sub
    local.tee $l6
    local.get $l6
    f64.mul
    local.get $l12
    local.get $l3
    f64.sub
    local.tee $l6
    local.get $l6
    f64.mul
    local.get $l13
    local.get $l5
    f64.sub
    local.tee $l6
    local.get $l6
    f64.mul
    f64.add
    f64.add
    f64.sqrt
    f64.div
    f64.sub
    f64.add)
  (func $offset_momentum (type $t0)
    (local $l0 f64) (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64)
    i32.const 1360
    f64.load
    local.set $l5
    i32.const 1352
    f64.load
    local.set $l6
    i32.const 1344
    f64.load
    local.set $l7
    i32.const 1328
    f64.load
    local.set $l8
    i32.const 1336
    f64.load
    local.set $l9
    i32.const 1264
    f64.load
    local.set $l10
    i32.const 1256
    f64.load
    local.set $l11
    i32.const 1248
    f64.load
    local.set $l12
    i32.const 1232
    f64.load
    local.set $l13
    i32.const 1240
    f64.load
    local.set $l14
    i32.const 1280
    i32.const 1312
    f64.load
    i32.const 1216
    f64.load
    local.tee $l0
    f64.mul
    i32.const 1304
    f64.load
    i32.const 1208
    f64.load
    local.tee $l1
    f64.mul
    i32.const 1296
    f64.load
    i32.const 1200
    f64.load
    local.tee $l2
    f64.mul
    i32.const 1288
    f64.load
    i32.const 1192
    f64.load
    local.tee $l3
    f64.mul
    i32.const 1280
    f64.load
    i32.const 1184
    f64.load
    local.tee $l4
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    f64.add
    f64.add
    f64.add
    f64.add
    f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
    f64.div
    f64.store
    i32.const 1232
    local.get $l10
    local.get $l0
    f64.mul
    local.get $l11
    local.get $l1
    f64.mul
    local.get $l12
    local.get $l2
    f64.mul
    local.get $l14
    local.get $l3
    f64.mul
    local.get $l13
    local.get $l4
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    f64.add
    f64.add
    f64.add
    f64.add
    f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
    f64.div
    f64.store
    i32.const 1328
    local.get $l5
    local.get $l0
    f64.mul
    local.get $l6
    local.get $l1
    f64.mul
    local.get $l7
    local.get $l2
    f64.mul
    local.get $l9
    local.get $l3
    f64.mul
    local.get $l8
    local.get $l4
    f64.mul
    f64.const 0x0p+0 (;=0;)
    f64.add
    f64.add
    f64.add
    f64.add
    f64.add
    f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
    f64.div
    f64.store)
  (func $init (type $t0)
    i32.const 1088
    i64.const 0
    i64.store
    i32.const 1040
    i64.const 0
    i64.store
    i32.const 1136
    i64.const 0
    i64.store
    i32.const 1232
    i64.const 0
    i64.store
    i32.const 1280
    i64.const 0
    i64.store
    i32.const 1328
    i64.const 0
    i64.store
    i32.const 1184
    i64.const 4630752910647379422
    i64.store
    i32.const 1048
    i64.const 4617136985637443884
    i64.store
    i32.const 1096
    i64.const -4615467600764216452
    i64.store
    i32.const 1144
    i64.const -4631240860977730576
    i64.store
    i32.const 1240
    i64.const 4603636522180398268
    i64.store
    i32.const 1288
    i64.const 4613514450253485211
    i64.store
    i32.const 1336
    i64.const -4640446117579192555
    i64.store
    i32.const 1192
    i64.const 4585593052079010776
    i64.store
    i32.const 1056
    i64.const 4620886515960171111
    i64.store
    i32.const 1104
    i64.const 4616330128746480048
    i64.store
    i32.const 1152
    i64.const -4622431185293064580
    i64.store
    i32.const 1248
    i64.const -4616141094713322430
    i64.store
    i32.const 1296
    i64.const 4610900871547424531
    i64.store
    i32.const 1200
    i64.const 4577659745833829943
    i64.store
    i32.const 1344
    i64.const 4576004977915405236
    i64.store
    i32.const 1064
    i64.const 4623448502799161807
    i64.store
    i32.const 1112
    i64.const -4598675596822288770
    i64.store
    i32.const 1160
    i64.const -4626158513131520608
    i64.store
    i32.const 1256
    i64.const 4607555276345777135
    i64.store
    i32.const 1304
    i64.const 4605999890795117509
    i64.store
    i32.const 1352
    i64.const -4645973824767902084
    i64.store
    i32.const 1208
    i64.const 4565592097032511155
    i64.store
    i32.const 1072
    i64.const 4624847617829197610
    i64.store
    i32.const 1120
    i64.const -4595383180696444384
    i64.store
    i32.const 1168
    i64.const 4595626498235032896
    i64.store
    i32.const 1264
    i64.const 4606994084859067466
    i64.store
    i32.const 1312
    i64.const 4603531791922690979
    i64.store
    i32.const 1360
    i64.const -4638202354754755082
    i64.store
    i32.const 1216
    i64.const 4566835785178257836
    i64.store)
  (func $_start (type $t1) (result i32)
    (local $l0 i32) (local $l1 i32)
    global.get $g0
    i32.const 32
    i32.sub
    local.tee $l0
    global.set $g0
    call $env.__VERIFIER_nondet_int
    call $init
    call $offset_momentum
    local.get $l0
    call $energy
    f64.store offset=16
    i32.const 1024
    local.get $l0
    i32.const 16
    i32.add
    call $env.printf
    drop
    call $advance
    local.get $l0
    call $energy
    f64.store
    i32.const 1024
    local.get $l0
    call $env.printf
    drop
    local.get $l0
    i32.const 32
    i32.add
    global.set $g0
    i32.const 0)
  (memory $memory 2)
  (global $g0 (mut i32) (i32.const 66912))
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
  (data $d0 (i32.const 1024) "%.9f\0a")
  (@custom ".debug_loc" "\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\01\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\13\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\13\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\15\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\16\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\17\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\03\9f\01\00\00\00\01\00\00\00\04\00\ed\00\1e\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\04\9f\01\00\00\00\01\00\00\00\04\00\ed\00!\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\05\9f\01\00\00\00\01\00\00\00\04\00\ed\00 \9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\04\9f\01\00\00\00\01\00\00\00\04\00\ed\00\1f\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\03\9f\01\00\00\00\01\00\00\00\04\00\ed\00!\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\03\00\11\01\9f\01\00\00\00\01\00\00\00\03\00\11\02\9f\01\00\00\00\01\00\00\00\03\00\11\03\9f\01\00\00\00\01\00\00\00\03\00\11\04\9f\01\00\00\00\01\00\00\00\03\00\11\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\05\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\04\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\03\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\03\00\11\04\9f\01\00\00\00\01\00\00\00\03\00\11\03\9f\01\00\00\00\01\00\00\00\03\00\11\02\9f\01\00\00\00\01\00\00\00\03\00\11\01\9f\01\00\00\00\01\00\00\00\03\00\11\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\0a\00\9e\08\00\00\00\00\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\02\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\0a\00\9e\08\00\00\00\00\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\074\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\08.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\09\05\00\02\18\03\0e:\0b;\0bI\13\00\00\0a4\00\02\17\03\0e:\0b;\0bI\13\00\00\0b\0b\01\11\01\12\06\00\00\0c.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\0d4\00\1c\0d\03\0e:\0b;\0bI\13\00\00\0e\0b\01U\17\00\00\0f.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b?\19\00\00\10.\00\11\01\12\06@\18\97B\19\03\0e:\0b;\0b?\19\00\00\11\89\82\01\001\13\11\01\00\00\12.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\13\05\00I\13\00\00\14\18\00\00\00\15\0f\00I\13\00\00\16&\00I\13\00\00\00")
  (@custom ".debug_info" "h\03\00\00\04\00\00\00\00\00\04\01\f4\00\00\00\1d\00\d6\00\00\00\00\00\00\00w\00\00\00\00\00\00\00\b0\01\00\00\023\00\00\00\01\90\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\06\00\057\00\00\00\06\01\06\de\00\00\00\08\07\07 \00\00\00^\00\00\00\01\0e\05\03\10\04\00\00\03j\00\00\00\04F\00\00\00\05\00\05_\00\00\00\04\08\07\17\00\00\00^\00\00\00\01\0e\05\03@\04\00\00\07\07\00\00\00^\00\00\00\01\0e\05\03p\04\00\00\07\19\00\00\00^\00\00\00\01\0f\05\03\d0\04\00\00\07\09\00\00\00^\00\00\00\01\0f\05\03\00\05\00\00\07\00\00\00\00^\00\00\00\01\0f\05\030\05\00\00\072\00\00\00^\00\00\00\01\10\05\03\a0\04\00\00\08\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9ff\00\00\00\01\13\09\04\ed\00\00\9f<\00\00\00\01\13Z\03\00\00\0a\83\00\00\00\09\01\00\00\01\18j\00\00\00\0a\a1\00\00\00\0c\01\00\00\01\17j\00\00\00\0a\bf\00\00\00\0f\01\00\00\01\16j\00\00\00\0a\dd\00\00\00\1f\00\00\00\01\15j\00\00\00\0a\09\01\00\00\f2\00\00\00\01\1bj\00\00\00\0a5\01\00\00\16\00\00\00\01\19j\00\00\00\0aa\01\00\00\06\00\00\00\01\1aj\00\00\00\0a\8d\01\00\00T\00\00\00\01\1cj\00\00\00\0b\00\00\00\00\00\00\00\00\0a\00\00\00\00N\00\00\00\01\1dZ\03\00\00\0b\00\00\00\00\00\00\00\00\0a+\00\00\00R\00\00\00\01\1fZ\03\00\00\0b\00\00\00\00\00\00\00\00\0aW\00\00\00P\00\00\00\01$Z\03\00\00\00\00\0b\00\00\00\00\00\00\00\00\0a\b9\01\00\00R\00\00\00\017Z\03\00\00\00\00\00\0c\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\0f\00\00\00\01Aj\00\00\00\0a\17\02\00\00u\00\00\00\01Cj\00\00\00\0b\00\00\00\00\00\00\00\00\0d\05R\00\00\00\01DZ\03\00\00\0e\00\00\00\00\0d\05P\00\00\00\01GZ\03\00\00\0e\c8\00\00\00\0a5\02\00\00\16\00\00\00\01Jj\00\00\00\0aa\02\00\00\1f\00\00\00\01Ij\00\00\00\0a\8d\02\00\00\06\00\00\00\01Kj\00\00\00\0a\b9\02\00\00n\00\00\00\01Lj\00\00\00\00\00\00\00\0f\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f>\00\00\00\01T\0a5\03\00\00\03\00\00\00\01Vj\00\00\00\0au\03\00\00\0c\00\00\00\01Vj\00\00\00\0a\93\03\00\00\1c\00\00\00\01Vj\00\00\00\0e\90\01\00\00\0a\d7\02\00\00R\00\00\00\01WZ\03\00\00\00\00\10\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f-\00\00\00\01c\0c\00\00\00\00\00\00\00\00\04\ed\00\00\9f\22\00\00\00\01\8bZ\03\00\00\0a\c5\03\00\00<\00\00\00\01\8dZ\03\00\00\11\ca\02\00\00\00\00\00\00\11p\02\00\00\00\00\00\00\11\db\01\00\00\00\00\00\00\11H\03\00\00\00\00\00\00\11\d7\00\00\00\00\00\00\00\11\db\01\00\00\00\00\00\00\11H\03\00\00\00\00\00\00\00\12X\00\00\00\02\f1Z\03\00\00\13a\03\00\00\14\00\05)\00\00\00\05\04\15f\03\00\00\16?\00\00\00\00")
  (@custom ".debug_ranges" "\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "vz\00pz\00dz\00vy\00py\00energy\00dy\00vx\00px\00dx\00_start\00int\00init\00mass\00char\00n\00offset_momentum\00k\00j\00i\00mag\00printf\00double\00advance\00distance\00/home/sven/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00nbody.c\00__ARRAY_SIZE_TYPE__\00R\00clang version 19.1.7\00z1\00y1\00x1\00")
  (@custom ".debug_line" "9\00\00\00\04\003\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00nbody.c\00\00\00\00stdlib.h\00\01\00\00\00")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0619.1.7")
  (@custom "target_features" "\04+\0fmutable-globals+\08sign-ext+\0freference-types+\0amultivalue"))
