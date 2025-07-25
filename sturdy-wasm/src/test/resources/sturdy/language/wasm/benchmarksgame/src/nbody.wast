(module $nbody.wasm
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param i32 i32 f64 i32)))
  (type (;4;) (func (param i32 i32) (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func $__wasi_proc_exit (type 0)))
  (func $__wasm_call_ctors (type 1)
    nop)
  (func $advance (type 3) (param i32 i32 f64 i32)
    (local i32 i32 f64 i32 f64 f64 f64 i32 i32)
    block  ;; label = @1
      local.get 3
      i32.eqz
      br_if 0 (;@1;)
      local.get 0
      i32.const 0
      i32.le_s
      br_if 0 (;@1;)
      loop  ;; label = @2
        local.get 3
        i32.const 1
        i32.sub
        local.set 3
        i32.const 0
        local.set 7
        local.get 1
        local.set 5
        loop  ;; label = @3
          local.get 7
          i32.const 1
          i32.add
          local.tee 7
          local.set 11
          local.get 5
          i32.const 56
          i32.add
          local.tee 12
          local.set 4
          local.get 0
          local.get 7
          i32.gt_s
          if  ;; label = @4
            loop  ;; label = @5
              local.get 5
              local.get 5
              f64.load offset=24
              local.get 5
              f64.load
              local.get 4
              f64.load
              f64.sub
              local.tee 8
              local.get 4
              f64.load offset=48
              f64.mul
              local.get 2
              local.get 5
              f64.load offset=16
              local.get 4
              f64.load offset=16
              f64.sub
              local.tee 9
              local.get 9
              f64.mul
              local.get 8
              local.get 8
              f64.mul
              local.get 5
              f64.load offset=8
              local.get 4
              f64.load offset=8
              f64.sub
              local.tee 10
              local.get 10
              f64.mul
              f64.add
              f64.add
              local.tee 6
              local.get 6
              f64.sqrt
              f64.mul
              f64.div
              local.tee 6
              f64.mul
              f64.sub
              f64.store offset=24
              local.get 5
              local.get 5
              f64.load offset=32
              local.get 10
              local.get 4
              f64.load offset=48
              f64.mul
              local.get 6
              f64.mul
              f64.sub
              f64.store offset=32
              local.get 5
              local.get 5
              f64.load offset=40
              local.get 9
              local.get 4
              f64.load offset=48
              f64.mul
              local.get 6
              f64.mul
              f64.sub
              f64.store offset=40
              local.get 4
              local.get 8
              local.get 5
              f64.load offset=48
              f64.mul
              local.get 6
              f64.mul
              local.get 4
              f64.load offset=24
              f64.add
              f64.store offset=24
              local.get 4
              local.get 10
              local.get 5
              f64.load offset=48
              f64.mul
              local.get 6
              f64.mul
              local.get 4
              f64.load offset=32
              f64.add
              f64.store offset=32
              local.get 4
              local.get 9
              local.get 5
              f64.load offset=48
              f64.mul
              local.get 6
              f64.mul
              local.get 4
              f64.load offset=40
              f64.add
              f64.store offset=40
              local.get 4
              i32.const 56
              i32.add
              local.set 4
              local.get 11
              i32.const 1
              i32.add
              local.tee 11
              local.get 0
              i32.ne
              br_if 0 (;@5;)
            end
          end
          local.get 12
          local.set 5
          local.get 0
          local.get 7
          i32.ne
          br_if 0 (;@3;)
        end
        i32.const 0
        local.set 5
        local.get 1
        local.set 4
        loop  ;; label = @3
          local.get 4
          local.get 2
          local.get 4
          f64.load offset=24
          f64.mul
          local.get 4
          f64.load
          f64.add
          f64.store
          local.get 4
          local.get 2
          local.get 4
          f64.load offset=32
          f64.mul
          local.get 4
          f64.load offset=8
          f64.add
          f64.store offset=8
          local.get 4
          local.get 2
          local.get 4
          f64.load offset=40
          f64.mul
          local.get 4
          f64.load offset=16
          f64.add
          f64.store offset=16
          local.get 4
          i32.const 56
          i32.add
          local.set 4
          local.get 5
          i32.const 1
          i32.add
          local.tee 5
          local.get 0
          i32.ne
          br_if 0 (;@3;)
        end
        local.get 3
        br_if 0 (;@2;)
      end
    end)
  (func $__original_main (type 2) (result i32)
    (local f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64)
    i32.const 1232
    f64.load
    local.set 7
    i32.const 1176
    f64.load
    local.set 8
    i32.const 1064
    f64.load
    local.set 1
    i32.const 1120
    f64.load
    local.set 9
    i32.const 1224
    f64.load
    local.set 10
    i32.const 1168
    f64.load
    local.set 11
    i32.const 1056
    f64.load
    local.set 2
    i32.const 1112
    f64.load
    local.set 12
    i32.const 1048
    i32.const 1048
    f64.load
    local.tee 0
    local.get 0
    i32.const 1072
    f64.load
    local.tee 3
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1104
    f64.load
    i32.const 1128
    f64.load
    local.tee 0
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1160
    f64.load
    i32.const 1184
    f64.load
    local.tee 4
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1216
    f64.load
    i32.const 1240
    f64.load
    local.tee 5
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1272
    f64.load
    i32.const 1296
    f64.load
    local.tee 6
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    f64.store
    i32.const 1056
    local.get 2
    local.get 3
    local.get 2
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 0
    local.get 12
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 4
    local.get 11
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 5
    local.get 10
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 6
    i32.const 1280
    f64.load
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    f64.store
    i32.const 1064
    local.get 1
    local.get 3
    local.get 1
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 0
    local.get 9
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 4
    local.get 8
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 5
    local.get 7
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 6
    i32.const 1288
    f64.load
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    f64.store
    i32.const 5
    i32.const 1024
    f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
    i32.const 10
    call $advance
    i32.const 0)
  (func $main (type 4) (param i32 i32) (result i32)
    call $__original_main)
  (func $_start (type 1)
    call $__wasm_call_ctors
    call $__original_main
    call $exit
    unreachable)
  (func $dummy (type 1)
    nop)
  (func $libc_exit_fini (type 1)
    call $dummy)
  (func $exit (type 0) (param i32)
    call $dummy
    call $libc_exit_fini
    call $dummy
    local.get 0
    call $_Exit
    unreachable)
  (func $_Exit (type 0) (param i32)
    local.get 0
    call $__wasi_proc_exit
    unreachable)
  (func $_emscripten_stack_restore (type 0) (param i32)
    local.get 0
    global.set $__stack_pointer)
  (func $emscripten_stack_get_current (type 2) (result i32)
    global.get $__stack_pointer)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 258 258)
  (global $__stack_pointer (mut i32) (i32.const 66848))
  (export "memory" (memory 0))
  (export "main" (func $main))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func $_start))
  (export "_emscripten_stack_restore" (func $_emscripten_stack_restore))
  (export "emscripten_stack_get_current" (func $emscripten_stack_get_current))
  (elem (;0;) (i32.const 1) func $__wasm_call_ctors)
  (data $.data (i32.const 1072) "\deE\be\c9<\bdC@,\d9<4\a0]\13@|\db\1f\c0\ab\90\f2\bf\f0\eb%l\f9\86\ba\bf\bc\cc\93\9b\06g\e3?\9b\94}\f5\f2~\06@\15\07Z\9a\d7\d2\99\bf\d83\ab\d9\95L\a3?g\ca2\c3\cd\af @\b0\01\de1\cb\7f\10@|F\eb\e1S\d3\d9\bfB\94\87\b8!,\f0\bf\13\8f\1f\bf\e95\fd?\b4#\11_H<\81?7\c6\07\0dI\1d\87?\cf\d9\a7\ce\ea\c9)@~f&\d6\e88.\c0\a0}%\beW\95\cc\bf\ef\1b\91\a9\1cS\f1?\c5\bbT>\7f\cc\eb?|>\f2\fak/\86\bf\b3\1e\f4\9c\d2=\5c?*W\05\a9g\c2.@ \a2\c83X\eb9\c0@\e5\ab\93\f3\f1\c6?J\bcY\16\b6T\ef?\a3\fb\c41\c6\07\e3?\f6evX\88\cb\a1\bf\ac\99\17S\f3\a8`?"))
