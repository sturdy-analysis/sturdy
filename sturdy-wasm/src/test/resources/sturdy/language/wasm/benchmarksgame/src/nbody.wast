(module $nbody.wasm
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32 i32) (result i32)))
  (type $t2 (func))
  (type $t3 (func (param i32 i32 f64 i32)))
  (type $t4 (func (param i32 i32) (result f64)))
  (type $t5 (func (param i32 i32)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t0)))
  (import "env" "printf" (func $printf (type $t1)))
  (func $__wasm_call_ctors (type $t2)
    nop)
  (func $advance (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 f64) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32)
    global.get $__stack_pointer
    i32.const 80
    i32.sub
    local.tee $l4
    local.get $p0
    i32.store offset=76
    local.get $l4
    local.get $p1
    i32.store offset=72
    local.get $l4
    local.get $p2
    f64.store offset=64
    local.get $l4
    local.get $p3
    i32.store offset=60
    loop $L0
      local.get $l4
      local.get $l4
      i32.load offset=60
      local.tee $l5
      i32.const 1
      i32.sub
      i32.store offset=60
      local.get $l5
      if $I1
        local.get $l4
        local.get $l4
        i32.load offset=72
        i32.store offset=48
        local.get $l4
        i32.const 0
        i32.store offset=56
        loop $L2
          local.get $l4
          i32.load offset=56
          local.get $l4
          i32.load offset=76
          i32.ge_s
          i32.eqz
          if $I3
            local.get $l4
            local.get $l4
            i32.load offset=48
            i32.const 56
            i32.add
            i32.store offset=44
            local.get $l4
            local.get $l4
            i32.load offset=56
            i32.const 1
            i32.add
            i32.store offset=52
            loop $L4
              local.get $l4
              i32.load offset=52
              local.get $l4
              i32.load offset=76
              i32.ge_s
              i32.eqz
              if $I5
                local.get $l4
                local.get $l4
                i32.load offset=48
                f64.load
                local.get $l4
                i32.load offset=44
                f64.load
                f64.sub
                f64.store offset=16
                local.get $l4
                local.get $l4
                i32.load offset=48
                f64.load offset=8
                local.get $l4
                i32.load offset=44
                f64.load offset=8
                f64.sub
                f64.store offset=24
                local.get $l4
                local.get $l4
                i32.load offset=48
                f64.load offset=16
                local.get $l4
                i32.load offset=44
                f64.load offset=16
                f64.sub
                f64.store offset=32
                local.get $l4
                local.get $l4
                f64.load offset=32
                local.get $l4
                f64.load offset=32
                f64.mul
                local.get $l4
                f64.load offset=16
                local.get $l4
                f64.load offset=16
                f64.mul
                local.get $l4
                f64.load offset=24
                local.get $l4
                f64.load offset=24
                f64.mul
                f64.add
                f64.add
                f64.store offset=8
                local.get $l4
                local.get $l4
                f64.load offset=64
                local.get $l4
                f64.load offset=8
                local.get $l4
                f64.load offset=8
                f64.sqrt
                f64.mul
                f64.div
                f64.store
                local.get $l4
                i32.load offset=48
                local.tee $l6
                local.get $l4
                f64.load offset=16
                local.get $l4
                i32.load offset=44
                f64.load offset=48
                f64.mul
                f64.neg
                local.get $l4
                f64.load
                f64.mul
                local.get $l6
                f64.load offset=24
                f64.add
                f64.store offset=24
                local.get $l4
                i32.load offset=48
                local.tee $l7
                local.get $l4
                f64.load offset=24
                local.get $l4
                i32.load offset=44
                f64.load offset=48
                f64.mul
                f64.neg
                local.get $l4
                f64.load
                f64.mul
                local.get $l7
                f64.load offset=32
                f64.add
                f64.store offset=32
                local.get $l4
                i32.load offset=48
                local.tee $l8
                local.get $l4
                f64.load offset=32
                local.get $l4
                i32.load offset=44
                f64.load offset=48
                f64.mul
                f64.neg
                local.get $l4
                f64.load
                f64.mul
                local.get $l8
                f64.load offset=40
                f64.add
                f64.store offset=40
                local.get $l4
                i32.load offset=44
                local.tee $l9
                local.get $l4
                f64.load offset=16
                local.get $l4
                i32.load offset=48
                f64.load offset=48
                f64.mul
                local.get $l4
                f64.load
                f64.mul
                local.get $l9
                f64.load offset=24
                f64.add
                f64.store offset=24
                local.get $l4
                i32.load offset=44
                local.tee $l10
                local.get $l4
                f64.load offset=24
                local.get $l4
                i32.load offset=48
                f64.load offset=48
                f64.mul
                local.get $l4
                f64.load
                f64.mul
                local.get $l10
                f64.load offset=32
                f64.add
                f64.store offset=32
                local.get $l4
                i32.load offset=44
                local.tee $l11
                local.get $l4
                f64.load offset=32
                local.get $l4
                i32.load offset=48
                f64.load offset=48
                f64.mul
                local.get $l4
                f64.load
                f64.mul
                local.get $l11
                f64.load offset=40
                f64.add
                f64.store offset=40
                local.get $l4
                local.get $l4
                i32.load offset=44
                i32.const 56
                i32.add
                i32.store offset=44
                local.get $l4
                local.get $l4
                i32.load offset=52
                i32.const 1
                i32.add
                i32.store offset=52
                br $L4
              end
            end
            local.get $l4
            local.get $l4
            i32.load offset=48
            i32.const 56
            i32.add
            i32.store offset=48
            local.get $l4
            local.get $l4
            i32.load offset=56
            i32.const 1
            i32.add
            i32.store offset=56
            br $L2
          end
        end
        local.get $l4
        local.get $l4
        i32.load offset=72
        i32.store offset=48
        local.get $l4
        i32.const 0
        i32.store offset=56
        loop $L6
          local.get $l4
          i32.load offset=56
          local.get $l4
          i32.load offset=76
          i32.ge_s
          i32.eqz
          if $I7
            local.get $l4
            i32.load offset=48
            local.tee $l12
            local.get $l4
            f64.load offset=64
            local.get $l4
            i32.load offset=48
            f64.load offset=24
            f64.mul
            local.get $l12
            f64.load
            f64.add
            f64.store
            local.get $l4
            i32.load offset=48
            local.tee $l13
            local.get $l4
            f64.load offset=64
            local.get $l4
            i32.load offset=48
            f64.load offset=32
            f64.mul
            local.get $l13
            f64.load offset=8
            f64.add
            f64.store offset=8
            local.get $l4
            i32.load offset=48
            local.tee $l14
            local.get $l4
            f64.load offset=64
            local.get $l4
            i32.load offset=48
            f64.load offset=40
            f64.mul
            local.get $l14
            f64.load offset=16
            f64.add
            f64.store offset=16
            local.get $l4
            local.get $l4
            i32.load offset=56
            i32.const 1
            i32.add
            i32.store offset=56
            local.get $l4
            local.get $l4
            i32.load offset=48
            i32.const 56
            i32.add
            i32.store offset=48
            br $L6
          end
        end
        br $L0
      end
    end)
  (func $energy (type $t4) (param $p0 i32) (param $p1 i32) (result f64)
    (local $l2 i32)
    global.get $__stack_pointer
    i32.const 80
    i32.sub
    local.tee $l2
    local.get $p0
    i32.store offset=76
    local.get $l2
    local.get $p1
    i32.store offset=72
    local.get $l2
    f64.const 0x0p+0 (;=0;)
    f64.store offset=64
    local.get $l2
    i32.const 0
    i32.store offset=28
    local.get $l2
    local.get $l2
    i32.load offset=72
    i32.store offset=16
    loop $L0
      local.get $l2
      i32.load offset=28
      local.get $l2
      i32.load offset=76
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l2
        i32.const 0
        i32.store offset=20
        loop $L2
          local.get $l2
          i32.load offset=20
          i32.const 3
          i32.ge_s
          i32.eqz
          if $I3
            local.get $l2
            local.get $l2
            f64.load offset=64
            local.get $l2
            i32.load offset=16
            f64.load offset=48
            local.get $l2
            i32.load offset=16
            i32.const 24
            i32.add
            local.get $l2
            i32.load offset=20
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.mul
            local.get $l2
            i32.load offset=16
            i32.const 24
            i32.add
            local.get $l2
            i32.load offset=20
            i32.const 3
            i32.shl
            i32.add
            f64.load
            f64.mul
            f64.const 0x1p-1 (;=0.5;)
            f64.mul
            f64.add
            f64.store offset=64
            local.get $l2
            local.get $l2
            i32.load offset=20
            i32.const 1
            i32.add
            i32.store offset=20
            br $L2
          end
        end
        local.get $l2
        local.get $l2
        i32.load offset=28
        i32.const 1
        i32.add
        i32.store offset=24
        local.get $l2
        local.get $l2
        i32.load offset=16
        i32.const 56
        i32.add
        i32.store offset=12
        loop $L4
          local.get $l2
          i32.load offset=24
          local.get $l2
          i32.load offset=76
          i32.ge_s
          i32.eqz
          if $I5
            local.get $l2
            i32.const 0
            i32.store offset=20
            loop $L6
              local.get $l2
              i32.load offset=20
              i32.const 3
              i32.ge_s
              i32.eqz
              if $I7
                local.get $l2
                i32.const 32
                i32.add
                local.get $l2
                i32.load offset=20
                i32.const 3
                i32.shl
                i32.add
                local.get $l2
                i32.load offset=16
                local.get $l2
                i32.load offset=20
                i32.const 3
                i32.shl
                i32.add
                f64.load
                local.get $l2
                i32.load offset=12
                local.get $l2
                i32.load offset=20
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.sub
                f64.store
                local.get $l2
                local.get $l2
                i32.load offset=20
                i32.const 1
                i32.add
                i32.store offset=20
                br $L6
              end
            end
            local.get $l2
            local.get $l2
            f64.load offset=64
            local.get $l2
            i32.load offset=16
            f64.load offset=48
            local.get $l2
            i32.load offset=12
            f64.load offset=48
            f64.mul
            local.get $l2
            f64.load offset=48
            local.get $l2
            f64.load offset=48
            f64.mul
            local.get $l2
            f64.load offset=32
            local.get $l2
            f64.load offset=32
            f64.mul
            local.get $l2
            f64.load offset=40
            local.get $l2
            f64.load offset=40
            f64.mul
            f64.add
            f64.add
            f64.sqrt
            f64.div
            f64.sub
            f64.store offset=64
            local.get $l2
            local.get $l2
            i32.load offset=12
            i32.const 56
            i32.add
            i32.store offset=12
            local.get $l2
            local.get $l2
            i32.load offset=24
            i32.const 1
            i32.add
            i32.store offset=24
            br $L4
          end
        end
        local.get $l2
        local.get $l2
        i32.load offset=16
        i32.const 56
        i32.add
        i32.store offset=16
        local.get $l2
        local.get $l2
        i32.load offset=28
        i32.const 1
        i32.add
        i32.store offset=28
        br $L0
      end
    end
    local.get $l2
    f64.load offset=64)
  (func $offset_momentum (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l2
    local.get $p0
    i32.store offset=12
    local.get $l2
    local.get $p1
    i32.store offset=8
    local.get $l2
    i32.const 0
    i32.store offset=4
    loop $L0
      local.get $l2
      i32.load offset=4
      local.get $l2
      i32.load offset=12
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l2
        i32.const 0
        i32.store
        loop $L2
          local.get $l2
          i32.load
          i32.const 3
          i32.ge_s
          i32.eqz
          if $I3
            local.get $l2
            i32.load offset=8
            i32.const 24
            i32.add
            local.get $l2
            i32.load
            i32.const 3
            i32.shl
            i32.add
            local.tee $l3
            local.get $l3
            f64.load
            local.get $l2
            i32.load offset=8
            local.get $l2
            i32.load offset=4
            i32.const 56
            i32.mul
            i32.add
            i32.const 24
            i32.add
            local.get $l2
            i32.load
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.get $l2
            i32.load offset=8
            local.get $l2
            i32.load offset=4
            i32.const 56
            i32.mul
            i32.add
            f64.load offset=48
            f64.mul
            f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
            f64.div
            f64.sub
            f64.store
            local.get $l2
            local.get $l2
            i32.load
            i32.const 1
            i32.add
            i32.store
            br $L2
          end
        end
        local.get $l2
        local.get $l2
        i32.load offset=4
        i32.const 1
        i32.add
        i32.store offset=4
        br $L0
      end
    end)
  (func $_start (type $t0) (result i32)
    (local $l0 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    local.get $l0
    call $__VERIFIER_nondet_int
    i32.store offset=28
    i32.const 5
    i32.const 1040
    call $offset_momentum
    local.get $l0
    i32.const 5
    i32.const 1040
    call $energy
    f64.store
    i32.const 1024
    local.get $l0
    call $printf
    drop
    i32.const 5
    i32.const 1040
    f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
    local.get $l0
    i32.load offset=28
    call $advance
    local.get $l0
    i32.const 5
    i32.const 1040
    call $energy
    f64.store offset=16
    i32.const 1024
    local.get $l0
    i32.const 16
    i32.add
    call $printf
    drop
    local.get $l0
    i32.const 32
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66864))
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
  (data $.rodata (i32.const 1024) "%.9f\0a")
  (data $.data (i32.const 1088) "\deE\be\c9<\bdC@,\d9<4\a0]\13@|\db\1f\c0\ab\90\f2\bf\f0\eb%l\f9\86\ba\bf\bc\cc\93\9b\06g\e3?\9b\94}\f5\f2~\06@\15\07Z\9a\d7\d2\99\bf\d83\ab\d9\95L\a3?g\ca2\c3\cd\af @\b0\01\de1\cb\7f\10@|F\eb\e1S\d3\d9\bfB\94\87\b8!,\f0\bf\13\8f\1f\bf\e95\fd?\b4#\11_H<\81?7\c6\07\0dI\1d\87?\cf\d9\a7\ce\ea\c9)@~f&\d6\e88.\c0\a0}%\beW\95\cc\bf\ef\1b\91\a9\1cS\f1?\c5\bbT>\7f\cc\eb?|>\f2\fak/\86\bf\b3\1e\f4\9c\d2=\5c?*W\05\a9g\c2.@ \a2\c83X\eb9\c0@\e5\ab\93\f3\f1\c6?J\bcY\16\b6T\ef?\a3\fb\c41\c6\07\e3?\f6evX\88\cb\a1\bf\ac\99\17S\f3\a8`?"))
