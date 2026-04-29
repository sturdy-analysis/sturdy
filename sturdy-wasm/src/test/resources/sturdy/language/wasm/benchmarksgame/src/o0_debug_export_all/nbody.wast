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
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 f64) (local $l6 i32) (local $l7 f64) (local $l8 i32) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 i32) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 f64) (local $l17 i32) (local $l18 f64) (local $l19 f64) (local $l20 i32) (local $l21 i32) (local $l22 f64) (local $l23 i32) (local $l24 f64) (local $l25 f64) (local $l26 i32) (local $l27 i32) (local $l28 f64) (local $l29 i32) (local $l30 f64) (local $l31 f64) (local $l32 i32) (local $l33 i32) (local $l34 f64) (local $l35 i32) (local $l36 f64) (local $l37 f64) (local $l38 i32) (local $l39 i32) (local $l40 f64) (local $l41 i32) (local $l42 f64) (local $l43 f64) (local $l44 i32) (local $l45 i32) (local $l46 f64) (local $l47 i32) (local $l48 f64) (local $l49 f64) (local $l50 i32) (local $l51 i32) (local $l52 i32) (local $l53 f64) (local $l54 i32) (local $l55 i32) (local $l56 i32) (local $l57 f64) (local $l58 i32) (local $l59 i32) (local $l60 i32) (local $l61 f64) (local $l62 i32) (local $l63 i32)
    global.get $__stack_pointer
    i32.const 96
    i32.sub
    local.set $l1
    local.get $l1
    local.get $p0
    i32.store offset=92
    local.get $l1
    i32.const 1
    i32.store offset=20
    block $B0
      loop $L1
        local.get $l1
        i32.load offset=20
        local.get $l1
        i32.load offset=92
        i32.le_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l1
        i32.const 0
        i32.store offset=16
        block $B2
          loop $L3
            local.get $l1
            i32.load offset=16
            i32.const 5
            i32.lt_s
            i32.const 1
            i32.and
            i32.eqz
            br_if $B2
            local.get $l1
            i32.load offset=16
            local.set $l2
            local.get $l1
            i32.const 1040
            local.get $l2
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.store offset=72
            local.get $l1
            i32.load offset=16
            local.set $l3
            local.get $l1
            i32.const 1088
            local.get $l3
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.store offset=64
            local.get $l1
            i32.load offset=16
            local.set $l4
            local.get $l1
            i32.const 1136
            local.get $l4
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.store offset=56
            local.get $l1
            local.get $l1
            i32.load offset=16
            i32.const 1
            i32.add
            i32.store offset=12
            block $B4
              loop $L5
                local.get $l1
                i32.load offset=12
                i32.const 5
                i32.lt_s
                i32.const 1
                i32.and
                i32.eqz
                br_if $B4
                local.get $l1
                f64.load offset=72
                local.set $l5
                local.get $l1
                i32.load offset=12
                local.set $l6
                local.get $l1
                local.get $l5
                i32.const 1040
                local.get $l6
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.sub
                f64.store offset=80
                local.get $l1
                local.get $l1
                f64.load offset=80
                local.get $l1
                f64.load offset=80
                f64.mul
                f64.store offset=32
                local.get $l1
                f64.load offset=64
                local.set $l7
                local.get $l1
                i32.load offset=12
                local.set $l8
                local.get $l1
                local.get $l7
                i32.const 1088
                local.get $l8
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.sub
                f64.store offset=48
                local.get $l1
                f64.load offset=48
                local.set $l9
                local.get $l1
                f64.load offset=48
                local.set $l10
                local.get $l1
                local.get $l1
                f64.load offset=32
                local.get $l9
                local.get $l10
                f64.mul
                f64.add
                f64.store offset=32
                local.get $l1
                f64.load offset=56
                local.set $l11
                local.get $l1
                i32.load offset=12
                local.set $l12
                local.get $l1
                local.get $l11
                i32.const 1136
                local.get $l12
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.sub
                f64.store offset=40
                local.get $l1
                f64.load offset=40
                local.set $l13
                local.get $l1
                f64.load offset=40
                local.set $l14
                local.get $l1
                local.get $l1
                f64.load offset=32
                local.get $l13
                local.get $l14
                f64.mul
                f64.add
                f64.store offset=32
                local.get $l1
                local.get $l1
                f64.load offset=32
                f64.sqrt
                f64.store offset=32
                local.get $l1
                f64.load offset=32
                local.get $l1
                f64.load offset=32
                f64.mul
                local.get $l1
                f64.load offset=32
                f64.mul
                local.set $l15
                local.get $l1
                f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
                local.get $l15
                f64.div
                f64.store offset=24
                local.get $l1
                f64.load offset=80
                local.set $l16
                local.get $l1
                i32.load offset=12
                local.set $l17
                local.get $l16
                i32.const 1184
                local.get $l17
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.mul
                local.set $l18
                local.get $l1
                f64.load offset=24
                local.set $l19
                local.get $l1
                i32.load offset=16
                local.set $l20
                i32.const 1232
                local.get $l20
                i32.const 3
                i32.shl
                i32.add
                local.set $l21
                local.get $l21
                local.get $l21
                f64.load
                local.get $l19
                local.get $l18
                f64.neg
                f64.mul
                f64.add
                f64.store
                local.get $l1
                f64.load offset=48
                local.set $l22
                local.get $l1
                i32.load offset=12
                local.set $l23
                local.get $l22
                i32.const 1184
                local.get $l23
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.mul
                local.set $l24
                local.get $l1
                f64.load offset=24
                local.set $l25
                local.get $l1
                i32.load offset=16
                local.set $l26
                i32.const 1280
                local.get $l26
                i32.const 3
                i32.shl
                i32.add
                local.set $l27
                local.get $l27
                local.get $l27
                f64.load
                local.get $l25
                local.get $l24
                f64.neg
                f64.mul
                f64.add
                f64.store
                local.get $l1
                f64.load offset=40
                local.set $l28
                local.get $l1
                i32.load offset=12
                local.set $l29
                local.get $l28
                i32.const 1184
                local.get $l29
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.mul
                local.set $l30
                local.get $l1
                f64.load offset=24
                local.set $l31
                local.get $l1
                i32.load offset=16
                local.set $l32
                i32.const 1328
                local.get $l32
                i32.const 3
                i32.shl
                i32.add
                local.set $l33
                local.get $l33
                local.get $l33
                f64.load
                local.get $l31
                local.get $l30
                f64.neg
                f64.mul
                f64.add
                f64.store
                local.get $l1
                f64.load offset=80
                local.set $l34
                local.get $l1
                i32.load offset=16
                local.set $l35
                local.get $l34
                i32.const 1184
                local.get $l35
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.mul
                local.set $l36
                local.get $l1
                f64.load offset=24
                local.set $l37
                local.get $l1
                i32.load offset=12
                local.set $l38
                i32.const 1232
                local.get $l38
                i32.const 3
                i32.shl
                i32.add
                local.set $l39
                local.get $l39
                local.get $l39
                f64.load
                local.get $l36
                local.get $l37
                f64.mul
                f64.add
                f64.store
                local.get $l1
                f64.load offset=48
                local.set $l40
                local.get $l1
                i32.load offset=16
                local.set $l41
                local.get $l40
                i32.const 1184
                local.get $l41
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.mul
                local.set $l42
                local.get $l1
                f64.load offset=24
                local.set $l43
                local.get $l1
                i32.load offset=12
                local.set $l44
                i32.const 1280
                local.get $l44
                i32.const 3
                i32.shl
                i32.add
                local.set $l45
                local.get $l45
                local.get $l45
                f64.load
                local.get $l42
                local.get $l43
                f64.mul
                f64.add
                f64.store
                local.get $l1
                f64.load offset=40
                local.set $l46
                local.get $l1
                i32.load offset=16
                local.set $l47
                local.get $l46
                i32.const 1184
                local.get $l47
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.mul
                local.set $l48
                local.get $l1
                f64.load offset=24
                local.set $l49
                local.get $l1
                i32.load offset=12
                local.set $l50
                i32.const 1328
                local.get $l50
                i32.const 3
                i32.shl
                i32.add
                local.set $l51
                local.get $l51
                local.get $l51
                f64.load
                local.get $l48
                local.get $l49
                f64.mul
                f64.add
                f64.store
                local.get $l1
                local.get $l1
                i32.load offset=12
                i32.const 1
                i32.add
                i32.store offset=12
                br $L5
              end
            end
            local.get $l1
            local.get $l1
            i32.load offset=16
            i32.const 1
            i32.add
            i32.store offset=16
            br $L3
          end
        end
        local.get $l1
        i32.const 0
        i32.store offset=8
        block $B6
          loop $L7
            local.get $l1
            i32.load offset=8
            i32.const 5
            i32.lt_s
            i32.const 1
            i32.and
            i32.eqz
            br_if $B6
            local.get $l1
            i32.load offset=8
            local.set $l52
            i32.const 1232
            local.get $l52
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l53
            local.get $l1
            i32.load offset=8
            local.set $l54
            i32.const 1040
            local.get $l54
            i32.const 3
            i32.shl
            i32.add
            local.set $l55
            local.get $l55
            local.get $l55
            f64.load
            local.get $l53
            f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
            f64.mul
            f64.add
            f64.store
            local.get $l1
            i32.load offset=8
            local.set $l56
            i32.const 1280
            local.get $l56
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l57
            local.get $l1
            i32.load offset=8
            local.set $l58
            i32.const 1088
            local.get $l58
            i32.const 3
            i32.shl
            i32.add
            local.set $l59
            local.get $l59
            local.get $l59
            f64.load
            local.get $l57
            f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
            f64.mul
            f64.add
            f64.store
            local.get $l1
            i32.load offset=8
            local.set $l60
            i32.const 1328
            local.get $l60
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l61
            local.get $l1
            i32.load offset=8
            local.set $l62
            i32.const 1136
            local.get $l62
            i32.const 3
            i32.shl
            i32.add
            local.set $l63
            local.get $l63
            local.get $l63
            f64.load
            local.get $l61
            f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
            f64.mul
            f64.add
            f64.store
            local.get $l1
            local.get $l1
            i32.load offset=8
            i32.const 1
            i32.add
            i32.store offset=8
            br $L7
          end
        end
        local.get $l1
        local.get $l1
        i32.load offset=20
        i32.const 1
        i32.add
        i32.store offset=20
        br $L1
      end
    end
    return)
  (func $energy (type $t4) (result f64)
    (local $l0 i32) (local $l1 i32) (local $l2 f64) (local $l3 i32) (local $l4 f64) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 f64) (local $l9 i32) (local $l10 f64) (local $l11 i32) (local $l12 f64) (local $l13 i32) (local $l14 f64) (local $l15 i32) (local $l16 f64) (local $l17 i32) (local $l18 i32) (local $l19 f64) (local $l20 i32) (local $l21 i32) (local $l22 f64) (local $l23 i32) (local $l24 f64) (local $l25 f64) (local $l26 i32) (local $l27 f64) (local $l28 i32) (local $l29 f64)
    global.get $__stack_pointer
    i32.const 48
    i32.sub
    local.set $l0
    local.get $l0
    i32.const 0
    f64.convert_i32_s
    f64.store offset=40
    local.get $l0
    i32.const 0
    i32.store offset=36
    block $B0
      loop $L1
        local.get $l0
        i32.load offset=36
        i32.const 5
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l0
        i32.load offset=36
        local.set $l1
        i32.const 1184
        local.get $l1
        i32.const 3
        i32.shl
        i32.add
        f64.load
        f64.const 0x1p-1 (;=0.5;)
        f64.mul
        local.set $l2
        local.get $l0
        i32.load offset=36
        local.set $l3
        i32.const 1232
        local.get $l3
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l4
        local.get $l0
        i32.load offset=36
        local.set $l5
        i32.const 1232
        local.get $l5
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l6
        local.get $l0
        i32.load offset=36
        local.set $l7
        i32.const 1280
        local.get $l7
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l8
        local.get $l0
        i32.load offset=36
        local.set $l9
        local.get $l8
        i32.const 1280
        local.get $l9
        i32.const 3
        i32.shl
        i32.add
        f64.load
        f64.mul
        local.get $l4
        local.get $l6
        f64.mul
        f64.add
        local.set $l10
        local.get $l0
        i32.load offset=36
        local.set $l11
        i32.const 1328
        local.get $l11
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l12
        local.get $l0
        i32.load offset=36
        local.set $l13
        local.get $l10
        local.get $l12
        i32.const 1328
        local.get $l13
        i32.const 3
        i32.shl
        i32.add
        f64.load
        f64.mul
        f64.add
        local.set $l14
        local.get $l0
        local.get $l0
        f64.load offset=40
        local.get $l2
        local.get $l14
        f64.mul
        f64.add
        f64.store offset=40
        local.get $l0
        local.get $l0
        i32.load offset=36
        i32.const 1
        i32.add
        i32.store offset=32
        block $B2
          loop $L3
            local.get $l0
            i32.load offset=32
            i32.const 5
            i32.lt_s
            i32.const 1
            i32.and
            i32.eqz
            br_if $B2
            local.get $l0
            i32.load offset=36
            local.set $l15
            i32.const 1040
            local.get $l15
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l16
            local.get $l0
            i32.load offset=32
            local.set $l17
            local.get $l0
            local.get $l16
            i32.const 1040
            local.get $l17
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.sub
            f64.store offset=24
            local.get $l0
            i32.load offset=36
            local.set $l18
            i32.const 1088
            local.get $l18
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l19
            local.get $l0
            i32.load offset=32
            local.set $l20
            local.get $l0
            local.get $l19
            i32.const 1088
            local.get $l20
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.sub
            f64.store offset=16
            local.get $l0
            i32.load offset=36
            local.set $l21
            i32.const 1136
            local.get $l21
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l22
            local.get $l0
            i32.load offset=32
            local.set $l23
            local.get $l0
            local.get $l22
            i32.const 1136
            local.get $l23
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.sub
            f64.store offset=8
            local.get $l0
            f64.load offset=24
            local.set $l24
            local.get $l0
            f64.load offset=24
            local.set $l25
            local.get $l0
            local.get $l0
            f64.load offset=16
            local.get $l0
            f64.load offset=16
            f64.mul
            local.get $l24
            local.get $l25
            f64.mul
            f64.add
            local.get $l0
            f64.load offset=8
            local.get $l0
            f64.load offset=8
            f64.mul
            f64.add
            f64.sqrt
            f64.store
            local.get $l0
            i32.load offset=36
            local.set $l26
            i32.const 1184
            local.get $l26
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l27
            local.get $l0
            i32.load offset=32
            local.set $l28
            local.get $l27
            i32.const 1184
            local.get $l28
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.mul
            local.get $l0
            f64.load
            f64.div
            local.set $l29
            local.get $l0
            local.get $l0
            f64.load offset=40
            local.get $l29
            f64.sub
            f64.store offset=40
            local.get $l0
            local.get $l0
            i32.load offset=32
            i32.const 1
            i32.add
            i32.store offset=32
            br $L3
          end
        end
        local.get $l0
        local.get $l0
        i32.load offset=36
        i32.const 1
        i32.add
        i32.store offset=36
        br $L1
      end
    end
    local.get $l0
    f64.load offset=40
    return)
  (func $offset_momentum (type $t2)
    (local $l0 i32) (local $l1 i32) (local $l2 f64) (local $l3 i32) (local $l4 f64) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 f64) (local $l9 i32) (local $l10 f64) (local $l11 i32) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.set $l0
    local.get $l0
    i32.const 0
    f64.convert_i32_s
    f64.store offset=24
    local.get $l0
    i32.const 0
    f64.convert_i32_s
    f64.store offset=16
    local.get $l0
    i32.const 0
    f64.convert_i32_s
    f64.store offset=8
    local.get $l0
    i32.const 0
    i32.store offset=4
    block $B0
      loop $L1
        local.get $l0
        i32.load offset=4
        i32.const 5
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l0
        i32.load offset=4
        local.set $l1
        i32.const 1232
        local.get $l1
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l2
        local.get $l0
        i32.load offset=4
        local.set $l3
        i32.const 1184
        local.get $l3
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l4
        local.get $l0
        local.get $l0
        f64.load offset=24
        local.get $l2
        local.get $l4
        f64.mul
        f64.add
        f64.store offset=24
        local.get $l0
        i32.load offset=4
        local.set $l5
        i32.const 1280
        local.get $l5
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l6
        local.get $l0
        i32.load offset=4
        local.set $l7
        i32.const 1184
        local.get $l7
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l8
        local.get $l0
        local.get $l0
        f64.load offset=16
        local.get $l6
        local.get $l8
        f64.mul
        f64.add
        f64.store offset=16
        local.get $l0
        i32.load offset=4
        local.set $l9
        i32.const 1328
        local.get $l9
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l10
        local.get $l0
        i32.load offset=4
        local.set $l11
        i32.const 1184
        local.get $l11
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l12
        local.get $l0
        local.get $l0
        f64.load offset=8
        local.get $l10
        local.get $l12
        f64.mul
        f64.add
        f64.store offset=8
        local.get $l0
        local.get $l0
        i32.load offset=4
        i32.const 1
        i32.add
        i32.store offset=4
        br $L1
      end
    end
    local.get $l0
    f64.load offset=24
    f64.neg
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    local.set $l13
    i32.const 0
    local.get $l13
    f64.store offset=1232
    local.get $l0
    f64.load offset=16
    f64.neg
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    local.set $l14
    i32.const 0
    local.get $l14
    f64.store offset=1280
    local.get $l0
    f64.load offset=8
    f64.neg
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    local.set $l15
    i32.const 0
    local.get $l15
    f64.store offset=1328
    return)
  (func $init (type $t2)
    (local $l0 f64) (local $l1 f64) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 f64) (local $l6 f64) (local $l7 f64) (local $l8 f64) (local $l9 f64) (local $l10 f64) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 f64) (local $l17 f64) (local $l18 f64) (local $l19 f64) (local $l20 f64) (local $l21 f64) (local $l22 f64) (local $l23 f64) (local $l24 f64) (local $l25 f64) (local $l26 f64) (local $l27 f64) (local $l28 f64) (local $l29 f64) (local $l30 f64) (local $l31 f64) (local $l32 f64) (local $l33 f64) (local $l34 f64)
    i32.const 0
    f64.convert_i32_s
    local.set $l0
    i32.const 0
    local.get $l0
    f64.store offset=1040
    i32.const 0
    f64.convert_i32_s
    local.set $l1
    i32.const 0
    local.get $l1
    f64.store offset=1088
    i32.const 0
    f64.convert_i32_s
    local.set $l2
    i32.const 0
    local.get $l2
    f64.store offset=1136
    i32.const 0
    f64.convert_i32_s
    local.set $l3
    i32.const 0
    local.get $l3
    f64.store offset=1232
    i32.const 0
    f64.convert_i32_s
    local.set $l4
    i32.const 0
    local.get $l4
    f64.store offset=1280
    i32.const 0
    f64.convert_i32_s
    local.set $l5
    i32.const 0
    local.get $l5
    f64.store offset=1328
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    local.set $l6
    i32.const 0
    local.get $l6
    f64.store offset=1184
    f64.const 0x1.35da0343cd92cp+2 (;=4.84143;)
    local.set $l7
    i32.const 0
    local.get $l7
    f64.store offset=1048
    f64.const -0x1.290abc01fdb7cp+0 (;=-1.16032;)
    local.set $l8
    i32.const 0
    local.get $l8
    f64.store offset=1096
    f64.const -0x1.a86f96c25ebfp-4 (;=-0.103622;)
    local.set $l9
    i32.const 0
    local.get $l9
    f64.store offset=1144
    f64.const 0x1.367069b93ccbcp-1 (;=0.606326;)
    local.set $l10
    i32.const 0
    local.get $l10
    f64.store offset=1240
    f64.const 0x1.67ef2f57d949bp+1 (;=2.81199;)
    local.set $l11
    i32.const 0
    local.get $l11
    f64.store offset=1288
    f64.const -0x1.9d2d79a5a0715p-6 (;=-0.0252184;)
    local.set $l12
    i32.const 0
    local.get $l12
    f64.store offset=1336
    f64.const 0x1.34c95d9ab33d8p-5 (;=0.0376937;)
    local.set $l13
    i32.const 0
    local.get $l13
    f64.store offset=1192
    f64.const 0x1.0afcdc332ca67p+3 (;=8.34337;)
    local.set $l14
    i32.const 0
    local.get $l14
    f64.store offset=1056
    f64.const 0x1.07fcb31de01bp+2 (;=4.1248;)
    local.set $l15
    i32.const 0
    local.get $l15
    f64.store offset=1104
    f64.const -0x1.9d353e1eb467cp-2 (;=-0.403523;)
    local.set $l16
    i32.const 0
    local.get $l16
    f64.store offset=1152
    f64.const -0x1.02c21b8879442p+0 (;=-1.01077;)
    local.set $l17
    i32.const 0
    local.get $l17
    f64.store offset=1248
    f64.const 0x1.d35e9bf1f8f13p+0 (;=1.82566;)
    local.set $l18
    i32.const 0
    local.get $l18
    f64.store offset=1296
    f64.const 0x1.13c485f1123b4p-7 (;=0.00841576;)
    local.set $l19
    i32.const 0
    local.get $l19
    f64.store offset=1344
    f64.const 0x1.71d490d07c637p-7 (;=0.0112863;)
    local.set $l20
    i32.const 0
    local.get $l20
    f64.store offset=1200
    f64.const 0x1.9c9eacea7d9cfp+3 (;=12.8944;)
    local.set $l21
    i32.const 0
    local.get $l21
    f64.store offset=1064
    f64.const -0x1.e38e8d626667ep+3 (;=-15.1112;)
    local.set $l22
    i32.const 0
    local.get $l22
    f64.store offset=1112
    f64.const -0x1.c9557be257dap-3 (;=-0.223308;)
    local.set $l23
    i32.const 0
    local.get $l23
    f64.store offset=1160
    f64.const 0x1.1531ca9911befp+0 (;=1.08279;)
    local.set $l24
    i32.const 0
    local.get $l24
    f64.store offset=1256
    f64.const 0x1.bcc7f3e54bbc5p-1 (;=0.868713;)
    local.set $l25
    i32.const 0
    local.get $l25
    f64.store offset=1304
    f64.const -0x1.62f6bfaf23e7cp-7 (;=-0.0108326;)
    local.set $l26
    i32.const 0
    local.get $l26
    f64.store offset=1352
    f64.const 0x1.c3dd29cf41eb3p-10 (;=0.00172372;)
    local.set $l27
    i32.const 0
    local.get $l27
    f64.store offset=1208
    f64.const 0x1.ec267a905572ap+3 (;=15.3797;)
    local.set $l28
    i32.const 0
    local.get $l28
    f64.store offset=1072
    f64.const -0x1.9eb5833c8a22p+4 (;=-25.9193;)
    local.set $l29
    i32.const 0
    local.get $l29
    f64.store offset=1120
    f64.const 0x1.6f1f393abe54p-3 (;=0.179259;)
    local.set $l30
    i32.const 0
    local.get $l30
    f64.store offset=1168
    f64.const 0x1.f54b61659bc4ap-1 (;=0.979091;)
    local.set $l31
    i32.const 0
    local.get $l31
    f64.store offset=1264
    f64.const 0x1.307c631c4fba3p-1 (;=0.594699;)
    local.set $l32
    i32.const 0
    local.get $l32
    f64.store offset=1312
    f64.const -0x1.1cb88587665f6p-5 (;=-0.034756;)
    local.set $l33
    i32.const 0
    local.get $l33
    f64.store offset=1360
    f64.const 0x1.0a8f3531799acp-9 (;=0.00203369;)
    local.set $l34
    i32.const 0
    local.get $l34
    f64.store offset=1216
    return)
  (func $_start (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.set $l0
    local.get $l0
    global.set $__stack_pointer
    local.get $l0
    call $__VERIFIER_nondet_int
    i32.store offset=28
    call $init
    call $offset_momentum
    local.get $l0
    call $energy
    f64.store
    i32.const 1024
    local.get $l0
    call $printf
    drop
    local.get $l0
    i32.load offset=28
    call $advance
    local.get $l0
    call $energy
    f64.store offset=16
    i32.const 1024
    local.get $l0
    i32.const 16
    i32.add
    call $printf
    drop
    i32.const 0
    local.set $l1
    local.get $l0
    i32.const 32
    i32.add
    global.set $__stack_pointer
    local.get $l1
    return)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66912))
  (global $x i32 (i32.const 1040))
  (global $y i32 (i32.const 1088))
  (global $z i32 (i32.const 1136))
  (global $mass i32 (i32.const 1184))
  (global $vx i32 (i32.const 1232))
  (global $vy i32 (i32.const 1280))
  (global $vz i32 (i32.const 1328))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1368))
  (global $__stack_low i32 (i32.const 1376))
  (global $__stack_high i32 (i32.const 66912))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66912))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "advance" (func $advance))
  (export "x" (global $x))
  (export "y" (global $y))
  (export "z" (global $z))
  (export "mass" (global $mass))
  (export "vx" (global $vx))
  (export "vy" (global $vy))
  (export "vz" (global $vz))
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
  (export "__wasm_first_page_end" (global $__wasm_first_page_end))
  (data $.rodata (i32.const 1024) "%.9f\0a\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\074\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\08.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\09\05\00\02\18\03\0e:\0b;\0bI\13\00\00\0a4\00\02\18\03\0e:\0b;\0bI\13\00\00\0b\0b\01\11\01\12\06\00\00\0c.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\0d.\01\11\01\12\06@\18\03\0e:\0b;\0b?\19\00\00\0e.\00\11\01\12\06@\18\03\0e:\0b;\0b?\19\00\00\00")
  (@custom ".debug_info" "\00\03\00\00\04\00\00\00\00\00\04\01\12\01\00\00\1d\00\ed\00\00\00\00\00\00\00N\00\00\00\00\00\00\00\00\00\00\00\023\00\00\00\01\90\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\06\00\057\00\00\00\06\01\06\fc\00\00\00\08\07\07 \00\00\00^\00\00\00\01\0e\05\03\10\04\00\00\03j\00\00\00\04F\00\00\00\05\00\05\d5\00\00\00\04\08\07\17\00\00\00^\00\00\00\01\0e\05\03@\04\00\00\07\07\00\00\00^\00\00\00\01\0e\05\03p\04\00\00\07\19\00\00\00^\00\00\00\01\0f\05\03\d0\04\00\00\07\09\00\00\00^\00\00\00\01\0f\05\03\00\05\00\00\07\00\00\00\00^\00\00\00\01\0f\05\030\05\00\00\072\00\00\00^\00\00\00\01\10\05\03\a0\04\00\00\08\06\00\00\00\ef\04\00\00\04\ed\00\01\9f\dc\00\00\00\01\13\09\03\91\dc\00<\00\00\00\01\13\fc\02\00\00\0a\03\91\d0\00\1f\00\00\00\01\15j\00\00\00\0a\03\91\c8\00-\01\00\00\01\16j\00\00\00\0a\03\91\c0\00*\01\00\00\01\17j\00\00\00\0a\02\918'\01\00\00\01\18j\00\00\00\0a\02\910\16\00\00\00\01\19j\00\00\00\0a\02\91(\06\00\00\00\01\1aj\00\00\00\0a\02\91 \10\01\00\00\01\1bj\00\00\00\0a\02\91\18\d1\00\00\00\01\1cj\00\00\00\0bd\00\00\00\8f\04\00\00\0a\02\91\14\cb\00\00\00\01\1d\fc\02\00\00\0b\80\00\00\00l\03\00\00\0a\02\91\10\cf\00\00\00\01\1f\fc\02\00\00\0b\ea\00\00\00\f1\02\00\00\0a\02\91\0c\cd\00\00\00\01$\fc\02\00\00\00\00\0b\ec\03\00\00\f6\00\00\00\0a\02\91\08\cf\00\00\00\017\fc\02\00\00\00\00\00\0c\f7\04\00\00\93\02\00\00\04\ed\00\00\9f\0f\00\00\00\01Aj\00\00\00\0a\02\91(\eb\00\00\00\01Cj\00\00\00\0b?\05\00\00D\02\00\00\0a\02\91$\cf\00\00\00\01D\fc\02\00\00\0b)\06\00\00I\01\00\00\0a\02\91 \cd\00\00\00\01G\fc\02\00\00\0bH\06\00\00\19\01\00\00\0a\02\91\18\1f\00\00\00\01Ij\00\00\00\0a\02\91\10\16\00\00\00\01Jj\00\00\00\0a\02\91\08\06\00\00\00\01Kj\00\00\00\0a\02\91\00\e4\00\00\00\01Lj\00\00\00\00\00\00\00\0d\8c\07\00\00\7f\01\00\00\04\ed\00\00\9f>\00\00\00\01T\0a\02\91\18\1c\00\00\00\01Vj\00\00\00\0a\02\91\10\0c\00\00\00\01Vj\00\00\00\0a\02\91\08\03\00\00\00\01Vj\00\00\00\0b\c8\07\00\00\ea\00\00\00\0a\02\91\04\cf\00\00\00\01W\fc\02\00\00\00\00\0e\0d\09\00\00\e3\02\00\00\07\ed\03\00\00\00\00\9f-\00\00\00\01c\0c\f2\0b\00\00\82\00\00\00\04\ed\00\00\9f\22\00\00\00\01\8b\fc\02\00\00\0a\02\91\1c<\00\00\00\01\8d\fc\02\00\00\00\05)\00\00\00\05\04\00")
  (@custom ".debug_ranges" "\06\00\00\00\f5\04\00\00\f7\04\00\00\8a\07\00\00\8c\07\00\00\0b\09\00\00\0d\09\00\00\f0\0b\00\00\f2\0b\00\00t\0c\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "vz\00pz\00dz\00vy\00py\00energy\00dy\00vx\00px\00dx\00_start\00int\00init\00mass\00char\00n\00offset_momentum\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00k\00j\00i\00mag\00double\00advance\00distance\00../src/nbody.c\00__ARRAY_SIZE_TYPE__\00R\00clang version 21.1.7\00z1\00y1\00x1\00")
  (@custom ".debug_line" "\8a\04\00\00\04\00&\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00\00nbody.c\00\01\00\00\00\00\05\02\06\00\00\00\03\13\01\05\0e\0a\03\09\02^\01\05\15\06t\05\1a\90\05\17X\05\05 \05\12\06h\05\19\06t\05\1b\90\05\09<\05\14\06h\05\12\06t\05\10\08\12\05\14\06=\05\12\06t\05\10\08\12\05\14\06=\05\12\06t\05\10\08\12\05\1a\06=\05\1c\06t\05\16<\05!<\05#\90\05\0d<\05\16\06h\05\1d\06t\05\1bt\05\19\08.\05\14 \05\15\06=\05\1a\06t\05\18X\05\13 \05\16\06=\05\1d\06t\05\1bt\05\19\08.\05\14 \05\16\06=\05\1b\06t\05\13t\05\16\06\f3\05\1d\06t\05\1bt\05\19\08.\05\14 \05\16\06=\05\1b\06t\05\13t\05\1a\06\f3\05\15\06t\05\13 \05\1d\06=\05!\06X\05\1fX\05% \05#X\05\1a<\05\15\d6\05\1a\06=\05$\06t\05\1ft\05\1d\08\12\05)<\05\14t\05\11t\05\17\d6\05\1a\06\08\13\05$\06t\05\1ft\05\1d\08\12\05)<\05\14t\05\11t\05\17\d6\05\1a\06\08\13\05$\06t\05\1ft\05\1d\08\12\05)<\05\14t\05\11t\05\17\d6\05\1a\06\08\13\05$\06t\05\1ft\05\1d\08\12\05)<\05\14t\05\11t\05\17\d6\05\1a\06\f3\05$\06t\05\1ft\05\1d\08\12\05)<\05\14t\05\11t\05\17\d6\05\1a\06\f3\05$\06t\05\1ft\05\1d\08\12\05)<\05\14t\05\11t\05\17\d6\05.\06\03q\f2\05\0d\06\c8.\05&\06)\05\09\06\c8.\05\12\06\03\18.\05\19\06t\05\1b\90\05\09<\05\1d\06h\05\1a\06t\05\0f\08\12\05\0dt\05\12\d6\05\1d\06\08g\05\1a\06t\05\0f\08\12\05\0dt\05\12\d6\05\1d\06\08g\05\1a\06t\05\0f\08\12\05\0dt\05\12\d6\05&\06\08b\05\09\06\c8.\05\1d\06\03f.\05\05\06\c8.\05\01\06\03!.\02\02\00\01\01\00\05\02\f7\04\00\00\03\c1\00\01\05\0c\0a\02@\13\05\0e\83\05\15\06t\05\17\90\05\05<\05\18\06h\05\13\06t\05\12\e4\05!\ba\05\1et\05)\08\12\05&t\051\08\12\05.t\059\08\12\056t\054\08\12\05, \05A\82\05>t\05I\08\12\05Ft\05<\08.\05\0bJ\05\16\06\f3\05\18\06t\05\12<\05\1d<\05\1f\90\05\09<\05\1b\06h\05\19\06t\05\22\08\12\05 t\05\1e\08.\05\14 \05\1b\06=\05\19\06t\05\22\08\12\05 t\05\1e\08.\05\14 \05\1b\06=\05\19\06t\05\22\08\12\05 t\05\1e\08.\05\14 \05$\06=\05)\06t\05.t\053t\051X\05, \058f\05=X\056X\05\1f.\05\14 \05\18\06=\05\13\06t\05\22\08\12\05\1dt\05\1b\08\12\05( \05&X\05\0f<\05*\06\03z\c8\05\09\06\c8.\05\22\06+\05\05\06\c8.\05\0c\06\03\0c.\05\05\06X\02\02\00\01\01\00\05\02\8c\07\00\00\03\d4\00\01\05\0c\0a\02$\13\05\16\06\82\05 \82\05\0e\06\83\05\15\06t\05\17\90\05\05<\05\12\06h\05\0f\06t\05\1c\08\12\05\17t\05\0c\08\12\05\12\06\f3\05\0f\06t\05\1c\08\12\05\17t\05\0c\08\12\05\12\06\f3\05\0f\06t\05\1c\08\12\05\17t\05\0c\08\12\05\22\06\ee\05\05\06\c8.\05\0e\064\05\0d\06X\05\11 \05\0b\ba\05\0e\06\ad\05\0d\06X\05\11 \05\0b\ba\05\0e\06\ad\05\0d\06X\05\11 \05\0b\ba\05\01\06\ad\02\02\00\01\01\05\0a\0a\00\05\02\10\09\00\00\03\e4\00\01\f3\f3\05\0b\f3\f3\f3\05\0d\f3\05\0a\08Y\08Y\08Y\05\0b\08Y\08Y\08Y\05\0d\08Y\05\0a\08Y\08Y\08Y\05\0b\08Y\08Y\08Y\05\0d\08Y\05\0a\08Y\08Y\08Y\05\0b\08Y\08Y\08Y\05\0d\08Y\05\0a\08Y\08Y\08Y\05\0b\08Y\08Y\08Y\05\0d\08Y\05\01\08Y\02\02\00\01\01\00\05\02\f2\0b\00\00\03\8b\01\01\05\0d\0a\08Y\05\09\06\82\05\05\06=g\05\16g\05\05\06\82\05\0d\06\08!\05\05\06X\05\16\06g\05\05\06\82\06\08K\02\13\00\01\01")
  (@custom "name" "\00\0b\0anbody.wasm\01c\08\00\15__VERIFIER_nondet_int\01\06printf\02\11__wasm_call_ctors\03\07advance\04\06energy\05\0foffset_momentum\06\04init\07\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
