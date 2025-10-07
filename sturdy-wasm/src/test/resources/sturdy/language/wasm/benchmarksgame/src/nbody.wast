(module $nbody.wasm
  (type (;0;) (func (result i32)))
  (type (;1;) (func (param i32 i32) (result i32)))
  (type (;2;) (func))
  (type (;3;) (func (param i32 i32 f64 i32)))
  (type (;4;) (func (param i32 i32) (result f64)))
  (type (;5;) (func (param i32 i32)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type 0)))
  (import "env" "printf" (func $printf (type 1)))
  (func $__wasm_call_ctors (type 2))
  (func $advance (type 3) (param i32 i32 f64 i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 f64 f64 f64 f64)
    block  ;; label = @1
      local.get 3
      i32.eqz
      br_if 0 (;@1;)
      local.get 0
      i32.const -1
      i32.add
      local.set 4
      local.get 0
      i32.const 1
      i32.lt_s
      local.set 5
      local.get 3
      local.set 3
      loop  ;; label = @2
        local.get 3
        local.set 6
        block  ;; label = @3
          local.get 5
          br_if 0 (;@3;)
          local.get 4
          local.set 3
          i32.const 0
          local.set 7
          local.get 1
          local.set 8
          loop  ;; label = @4
            local.get 3
            local.set 9
            local.get 8
            local.tee 3
            i32.const 56
            i32.add
            local.set 10
            block  ;; label = @5
              local.get 7
              i32.const 1
              i32.add
              local.tee 11
              local.get 0
              i32.ge_s
              br_if 0 (;@5;)
              local.get 9
              local.set 12
              local.get 10
              local.set 7
              loop  ;; label = @6
                local.get 3
                local.get 3
                f64.load offset=24
                local.get 7
                local.tee 7
                i32.const 48
                i32.add
                local.tee 8
                f64.load
                local.get 3
                f64.load
                local.get 7
                f64.load
                f64.sub
                local.tee 13
                f64.mul
                local.get 2
                local.get 3
                f64.load offset=16
                local.get 7
                f64.load offset=16
                f64.sub
                local.tee 14
                local.get 14
                f64.mul
                local.get 13
                local.get 13
                f64.mul
                local.get 3
                f64.load offset=8
                local.get 7
                f64.load offset=8
                f64.sub
                local.tee 15
                local.get 15
                f64.mul
                f64.add
                f64.add
                local.tee 16
                local.get 16
                f64.sqrt
                f64.mul
                f64.div
                local.tee 16
                f64.mul
                f64.sub
                f64.store offset=24
                local.get 3
                local.get 3
                f64.load offset=32
                local.get 8
                f64.load
                local.get 15
                f64.mul
                local.get 16
                f64.mul
                f64.sub
                f64.store offset=32
                local.get 3
                local.get 3
                f64.load offset=40
                local.get 8
                f64.load
                local.get 14
                f64.mul
                local.get 16
                f64.mul
                f64.sub
                f64.store offset=40
                local.get 7
                local.get 13
                local.get 3
                f64.load offset=48
                f64.mul
                local.get 16
                f64.mul
                local.get 7
                f64.load offset=24
                f64.add
                f64.store offset=24
                local.get 7
                local.get 15
                local.get 3
                f64.load offset=48
                f64.mul
                local.get 16
                f64.mul
                local.get 7
                f64.load offset=32
                f64.add
                f64.store offset=32
                local.get 7
                local.get 14
                local.get 3
                f64.load offset=48
                f64.mul
                local.get 16
                f64.mul
                local.get 7
                f64.load offset=40
                f64.add
                f64.store offset=40
                local.get 12
                i32.const -1
                i32.add
                local.tee 8
                local.set 12
                local.get 7
                i32.const 56
                i32.add
                local.set 7
                local.get 8
                br_if 0 (;@6;)
              end
            end
            local.get 9
            i32.const -1
            i32.add
            local.set 3
            local.get 11
            local.set 7
            local.get 10
            local.set 8
            local.get 11
            local.get 0
            i32.ne
            br_if 0 (;@4;)
          end
        end
        local.get 0
        local.set 7
        local.get 1
        local.set 3
        block  ;; label = @3
          local.get 5
          br_if 0 (;@3;)
          loop  ;; label = @4
            local.get 3
            local.tee 3
            local.get 2
            local.get 3
            f64.load offset=24
            f64.mul
            local.get 3
            f64.load
            f64.add
            f64.store
            local.get 3
            local.get 2
            local.get 3
            f64.load offset=32
            f64.mul
            local.get 3
            f64.load offset=8
            f64.add
            f64.store offset=8
            local.get 3
            local.get 2
            local.get 3
            f64.load offset=40
            f64.mul
            local.get 3
            f64.load offset=16
            f64.add
            f64.store offset=16
            local.get 7
            i32.const -1
            i32.add
            local.tee 8
            local.set 7
            local.get 3
            i32.const 56
            i32.add
            local.set 3
            local.get 8
            br_if 0 (;@4;)
          end
        end
        local.get 6
        i32.const -1
        i32.add
        local.tee 7
        local.set 3
        local.get 7
        br_if 0 (;@2;)
      end
    end)
  (func $energy (type 4) (param i32 i32) (result f64)
    (local i32 f64 i32 f64 i32 i32 i32 f64 i32 i32 f64)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.set 2
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        i32.const 1
        i32.ge_s
        br_if 0 (;@2;)
        f64.const 0x0p+0 (;=0;)
        local.set 3
        br 1 (;@1;)
      end
      local.get 1
      local.set 1
      i32.const 0
      local.set 4
      f64.const 0x0p+0 (;=0;)
      local.set 5
      loop  ;; label = @2
        local.get 4
        local.set 6
        local.get 1
        local.tee 7
        f64.load offset=48
        local.set 3
        i32.const 24
        local.set 1
        local.get 5
        local.set 5
        loop  ;; label = @3
          local.get 1
          local.tee 4
          i32.const 8
          i32.add
          local.tee 8
          local.set 1
          local.get 5
          local.get 7
          local.get 4
          i32.add
          f64.load
          local.tee 5
          local.get 3
          local.get 5
          f64.mul
          f64.mul
          f64.const 0x1p-1 (;=0.5;)
          f64.mul
          f64.add
          local.tee 9
          local.set 5
          local.get 8
          i32.const 48
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 7
        i32.const 56
        i32.add
        local.set 10
        block  ;; label = @3
          block  ;; label = @4
            local.get 6
            i32.const 1
            i32.add
            local.tee 11
            local.get 0
            i32.lt_s
            br_if 0 (;@4;)
            local.get 9
            local.set 3
            br 1 (;@3;)
          end
          local.get 7
          f64.load offset=48
          local.set 12
          local.get 10
          local.set 1
          local.get 11
          local.set 4
          local.get 9
          local.set 5
          loop  ;; label = @4
            local.get 5
            local.set 5
            local.get 4
            local.set 6
            local.get 1
            local.set 8
            i32.const 0
            local.set 1
            loop  ;; label = @5
              local.get 2
              local.get 1
              local.tee 1
              i32.add
              local.get 7
              local.get 1
              i32.add
              f64.load
              local.get 8
              local.get 1
              i32.add
              f64.load
              f64.sub
              f64.store
              local.get 1
              i32.const 8
              i32.add
              local.tee 4
              local.set 1
              local.get 4
              i32.const 24
              i32.ne
              br_if 0 (;@5;)
            end
            local.get 5
            local.get 12
            local.get 8
            f64.load offset=48
            f64.mul
            local.get 2
            f64.load offset=16
            local.tee 3
            local.get 3
            f64.mul
            local.get 2
            f64.load
            local.tee 3
            local.get 3
            f64.mul
            local.get 2
            f64.load offset=8
            local.tee 3
            local.get 3
            f64.mul
            f64.add
            f64.add
            f64.sqrt
            f64.div
            f64.sub
            local.tee 5
            local.set 3
            local.get 8
            i32.const 56
            i32.add
            local.set 1
            local.get 6
            i32.const 1
            i32.add
            local.tee 8
            local.set 4
            local.get 5
            local.set 5
            local.get 8
            local.get 0
            i32.ne
            br_if 0 (;@4;)
          end
        end
        local.get 10
        local.set 1
        local.get 11
        local.set 4
        local.get 3
        local.tee 3
        local.set 5
        local.get 3
        local.set 3
        local.get 11
        local.get 0
        i32.ne
        br_if 0 (;@2;)
      end
    end
    local.get 3)
  (func $offset_momentum (type 5) (param i32 i32)
    (local i32 i32 i32 i32 i32 i32 i32)
    block  ;; label = @1
      local.get 0
      i32.const 1
      i32.lt_s
      br_if 0 (;@1;)
      local.get 1
      i32.const 24
      i32.add
      local.set 2
      i32.const 0
      local.set 3
      i32.const 0
      local.set 4
      loop  ;; label = @2
        local.get 2
        local.get 3
        local.tee 5
        i32.add
        local.set 6
        local.get 1
        local.get 4
        local.tee 7
        i32.const 56
        i32.mul
        i32.add
        local.set 8
        i32.const 0
        local.set 3
        loop  ;; label = @3
          local.get 2
          local.get 3
          local.tee 3
          i32.add
          local.tee 4
          local.get 4
          f64.load
          local.get 6
          local.get 3
          i32.add
          f64.load
          local.get 8
          f64.load offset=48
          f64.mul
          f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
          f64.div
          f64.add
          f64.store
          local.get 3
          i32.const 8
          i32.add
          local.tee 4
          local.set 3
          local.get 4
          i32.const 24
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 5
        i32.const 56
        i32.add
        local.set 3
        local.get 7
        i32.const 1
        i32.add
        local.tee 6
        local.set 4
        local.get 6
        local.get 0
        i32.ne
        br_if 0 (;@2;)
      end
    end)
  (func $_start (type 0) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32 f64 f64 f64 i32 i32)
    global.get $__stack_pointer
    i32.const 64
    i32.sub
    local.tee 0
    global.set $__stack_pointer
    call $__VERIFIER_nondet_int
    local.set 1
    i32.const 1040
    local.set 2
    i32.const 0
    local.set 3
    loop  ;; label = @1
      local.get 2
      local.set 4
      local.get 3
      local.tee 5
      i32.const 56
      i32.mul
      i32.const 1040
      i32.add
      local.set 6
      i32.const 24
      local.set 2
      loop  ;; label = @2
        local.get 2
        local.tee 2
        i32.const 1040
        i32.add
        local.tee 3
        local.get 3
        f64.load
        local.get 4
        local.get 2
        i32.add
        f64.load
        local.get 6
        f64.load offset=48
        f64.mul
        f64.const -0x1.3bd3cc9be45dep+5 (;=-39.4784;)
        f64.div
        f64.add
        f64.store
        local.get 2
        i32.const 8
        i32.add
        local.tee 3
        local.set 2
        local.get 3
        i32.const 48
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 4
      i32.const 56
      i32.add
      local.set 2
      local.get 5
      i32.const 1
      i32.add
      local.tee 4
      local.set 3
      local.get 4
      i32.const 5
      i32.ne
      br_if 0 (;@1;)
    end
    i32.const 1040
    local.set 2
    i32.const 0
    local.set 3
    f64.const 0x0p+0 (;=0;)
    local.set 7
    loop  ;; label = @1
      local.get 3
      local.set 5
      local.get 2
      local.tee 6
      f64.load offset=48
      local.set 8
      i32.const 24
      local.set 2
      local.get 7
      local.set 7
      loop  ;; label = @2
        local.get 2
        local.tee 3
        i32.const 8
        i32.add
        local.tee 4
        local.set 2
        local.get 7
        local.get 6
        local.get 3
        i32.add
        f64.load
        local.tee 7
        local.get 8
        local.get 7
        f64.mul
        f64.mul
        f64.const 0x1p-1 (;=0.5;)
        f64.mul
        f64.add
        local.tee 9
        local.set 7
        local.get 4
        i32.const 48
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 6
      i32.const 56
      i32.add
      local.set 10
      local.get 5
      i32.const 1
      i32.add
      local.set 11
      block  ;; label = @2
        block  ;; label = @3
          local.get 5
          i32.const 3
          i32.le_u
          br_if 0 (;@3;)
          local.get 9
          local.set 9
          br 1 (;@2;)
        end
        local.get 10
        local.set 2
        local.get 11
        local.set 3
        local.get 9
        local.set 7
        loop  ;; label = @3
          local.get 7
          local.set 7
          local.get 3
          local.set 5
          local.get 2
          local.set 4
          i32.const 0
          local.set 2
          loop  ;; label = @4
            local.get 0
            i32.const 32
            i32.add
            local.get 2
            local.tee 2
            i32.add
            local.get 6
            local.get 2
            i32.add
            f64.load
            local.get 4
            local.get 2
            i32.add
            f64.load
            f64.sub
            f64.store
            local.get 2
            i32.const 8
            i32.add
            local.tee 3
            local.set 2
            local.get 3
            i32.const 24
            i32.ne
            br_if 0 (;@4;)
          end
          local.get 7
          local.get 8
          local.get 4
          f64.load offset=48
          f64.mul
          local.get 0
          f64.load offset=48
          local.tee 9
          local.get 9
          f64.mul
          local.get 0
          f64.load offset=32
          local.tee 9
          local.get 9
          f64.mul
          local.get 0
          f64.load offset=40
          local.tee 9
          local.get 9
          f64.mul
          f64.add
          f64.add
          f64.sqrt
          f64.div
          f64.sub
          local.tee 7
          local.set 9
          local.get 4
          i32.const 56
          i32.add
          local.set 2
          local.get 5
          i32.const 1
          i32.add
          local.tee 4
          local.set 3
          local.get 7
          local.set 7
          local.get 4
          i32.const 5
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 10
      local.set 2
      local.get 11
      local.set 3
      local.get 9
      local.tee 8
      local.set 7
      local.get 11
      i32.const 5
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 0
    local.get 8
    f64.store offset=16
    i32.const 1024
    local.get 0
    i32.const 16
    i32.add
    call $printf
    drop
    i32.const 5
    i32.const 1040
    f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
    local.get 1
    call $advance
    i32.const 1040
    local.set 2
    i32.const 0
    local.set 3
    f64.const 0x0p+0 (;=0;)
    local.set 7
    loop  ;; label = @1
      local.get 3
      local.set 5
      local.get 2
      local.tee 6
      f64.load offset=48
      local.set 8
      i32.const 24
      local.set 2
      local.get 7
      local.set 7
      loop  ;; label = @2
        local.get 2
        local.tee 3
        i32.const 8
        i32.add
        local.tee 4
        local.set 2
        local.get 7
        local.get 6
        local.get 3
        i32.add
        f64.load
        local.tee 7
        local.get 8
        local.get 7
        f64.mul
        f64.mul
        f64.const 0x1p-1 (;=0.5;)
        f64.mul
        f64.add
        local.tee 9
        local.set 7
        local.get 4
        i32.const 48
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 6
      i32.const 56
      i32.add
      local.set 10
      local.get 5
      i32.const 1
      i32.add
      local.set 11
      block  ;; label = @2
        block  ;; label = @3
          local.get 5
          i32.const 3
          i32.le_u
          br_if 0 (;@3;)
          local.get 9
          local.set 9
          br 1 (;@2;)
        end
        local.get 10
        local.set 2
        local.get 11
        local.set 3
        local.get 9
        local.set 7
        loop  ;; label = @3
          local.get 7
          local.set 7
          local.get 3
          local.set 5
          local.get 2
          local.set 4
          i32.const 0
          local.set 2
          loop  ;; label = @4
            local.get 0
            i32.const 32
            i32.add
            local.get 2
            local.tee 2
            i32.add
            local.get 6
            local.get 2
            i32.add
            f64.load
            local.get 4
            local.get 2
            i32.add
            f64.load
            f64.sub
            f64.store
            local.get 2
            i32.const 8
            i32.add
            local.tee 3
            local.set 2
            local.get 3
            i32.const 24
            i32.ne
            br_if 0 (;@4;)
          end
          local.get 7
          local.get 8
          local.get 4
          f64.load offset=48
          f64.mul
          local.get 0
          f64.load offset=48
          local.tee 9
          local.get 9
          f64.mul
          local.get 0
          f64.load offset=32
          local.tee 9
          local.get 9
          f64.mul
          local.get 0
          f64.load offset=40
          local.tee 9
          local.get 9
          f64.mul
          f64.add
          f64.add
          f64.sqrt
          f64.div
          f64.sub
          local.tee 7
          local.set 9
          local.get 4
          i32.const 56
          i32.add
          local.set 2
          local.get 5
          i32.const 1
          i32.add
          local.tee 4
          local.set 3
          local.get 7
          local.set 7
          local.get 4
          i32.const 5
          i32.ne
          br_if 0 (;@3;)
        end
      end
      local.get 10
      local.set 2
      local.get 11
      local.set 3
      local.get 9
      local.tee 8
      local.set 7
      local.get 11
      i32.const 5
      i32.ne
      br_if 0 (;@1;)
    end
    local.get 0
    local.get 8
    f64.store
    i32.const 1024
    local.get 0
    call $printf
    drop
    local.get 0
    i32.const 64
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 66864))
  (global (;1;) i32 (i32.const 1040))
  (global (;2;) i32 (i32.const 1024))
  (global (;3;) i32 (i32.const 1320))
  (global (;4;) i32 (i32.const 1328))
  (global (;5;) i32 (i32.const 66864))
  (global (;6;) i32 (i32.const 1024))
  (global (;7;) i32 (i32.const 66864))
  (global (;8;) i32 (i32.const 131072))
  (global (;9;) i32 (i32.const 0))
  (global (;10;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "advance" (func $advance))
  (export "energy" (func $energy))
  (export "offset_momentum" (func $offset_momentum))
  (export "_start" (func $_start))
  (export "bodies" (global 1))
  (export "__dso_handle" (global 2))
  (export "__data_end" (global 3))
  (export "__stack_low" (global 4))
  (export "__stack_high" (global 5))
  (export "__global_base" (global 6))
  (export "__heap_base" (global 7))
  (export "__heap_end" (global 8))
  (export "__memory_base" (global 9))
  (export "__table_base" (global 10))
  (data $.rodata (i32.const 1024) "%.9f\0a\00")
  (data $.data (i32.const 1040) "\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\deE\be\c9<\bdC@,\d9<4\a0]\13@|\db\1f\c0\ab\90\f2\bf\f0\eb%l\f9\86\ba\bf\bc\cc\93\9b\06g\e3?\9b\94}\f5\f2~\06@\15\07Z\9a\d7\d2\99\bf\d83\ab\d9\95L\a3?g\ca2\c3\cd\af @\b0\01\de1\cb\7f\10@|F\eb\e1S\d3\d9\bfB\94\87\b8!,\f0\bf\13\8f\1f\bf\e95\fd?\b4#\11_H<\81?7\c6\07\0dI\1d\87?\cf\d9\a7\ce\ea\c9)@~f&\d6\e88.\c0\a0}%\beW\95\cc\bf\ef\1b\91\a9\1cS\f1?\c5\bbT>\7f\cc\eb?|>\f2\fak/\86\bf\b3\1e\f4\9c\d2=\5c?*W\05\a9g\c2.@ \a2\c83X\eb9\c0@\e5\ab\93\f3\f1\c6?J\bcY\16\b6T\ef?\a3\fb\c41\c6\07\e3?\f6evX\88\cb\a1\bf\ac\99\17S\f3\a8`?"))
