(module
  (type (;0;) (func (param i32)))
  (type (;1;) (func))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param i32) (result i32)))
  (import "wasi_snapshot_preview1" "proc_exit" (func (;0;) (type 0)))
  (func (;1;) (type 1)
    nop)
  (func (;2;) (type 1)
    (local i32 i32 i32 i32 i32 i32 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64 f64)
    i32.const 1272
    f64.load
    local.set 16
    i32.const 1216
    f64.load
    local.set 17
    i32.const 1160
    f64.load
    local.set 18
    i32.const 1048
    f64.load
    local.set 10
    i32.const 1104
    f64.load
    local.set 19
    i32.const 1280
    f64.load
    local.set 20
    i32.const 1224
    f64.load
    local.set 7
    i32.const 1168
    f64.load
    local.set 8
    i32.const 1056
    f64.load
    local.set 11
    i32.const 1112
    f64.load
    local.set 9
    i32.const 1064
    i32.const 1064
    f64.load
    local.tee 6
    local.get 6
    i32.const 1072
    f64.load
    local.tee 12
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1120
    f64.load
    i32.const 1128
    f64.load
    local.tee 13
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1176
    f64.load
    i32.const 1184
    f64.load
    local.tee 14
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1232
    f64.load
    i32.const 1240
    f64.load
    local.tee 15
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    i32.const 1288
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
    local.get 11
    local.get 11
    local.get 12
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 9
    local.get 13
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 8
    local.get 14
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 7
    local.get 15
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 20
    local.get 6
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    f64.store
    i32.const 1048
    local.get 10
    local.get 10
    local.get 12
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 19
    local.get 13
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 18
    local.get 14
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 17
    local.get 15
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    local.get 16
    local.get 6
    f64.mul
    f64.const 0x1.3bd3cc9be45dep+5 (;=39.4784;)
    f64.div
    f64.sub
    f64.store
    i32.const 10
    local.set 3
    loop  ;; label = @1
      local.get 3
      i32.const 1
      i32.sub
      local.set 3
      i32.const 0
      local.set 2
      i32.const 1024
      local.set 0
      loop  ;; label = @2
        local.get 2
        i32.const 1
        i32.add
        local.tee 2
        local.set 4
        local.get 0
        i32.const 56
        i32.add
        local.tee 5
        local.set 1
        local.get 2
        i32.const 5
        i32.lt_s
        if  ;; label = @3
          loop  ;; label = @4
            local.get 0
            local.get 0
            f64.load offset=24
            local.get 0
            f64.load
            local.get 1
            f64.load
            f64.sub
            local.tee 7
            local.get 1
            f64.load offset=48
            f64.mul
            f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
            local.get 7
            local.get 7
            f64.mul
            local.get 0
            f64.load offset=8
            local.get 1
            f64.load offset=8
            f64.sub
            local.tee 8
            local.get 8
            f64.mul
            f64.add
            local.get 0
            f64.load offset=16
            local.get 1
            f64.load offset=16
            f64.sub
            local.tee 9
            local.get 9
            f64.mul
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
            local.get 0
            local.get 0
            f64.load offset=32
            local.get 8
            local.get 1
            f64.load offset=48
            f64.mul
            local.get 6
            f64.mul
            f64.sub
            f64.store offset=32
            local.get 0
            local.get 0
            f64.load offset=40
            local.get 6
            local.get 9
            local.get 1
            f64.load offset=48
            f64.mul
            f64.mul
            f64.sub
            f64.store offset=40
            local.get 1
            local.get 1
            f64.load offset=24
            local.get 6
            local.get 7
            local.get 0
            f64.load offset=48
            f64.mul
            f64.mul
            f64.add
            f64.store offset=24
            local.get 1
            local.get 1
            f64.load offset=32
            local.get 6
            local.get 8
            local.get 0
            f64.load offset=48
            f64.mul
            f64.mul
            f64.add
            f64.store offset=32
            local.get 1
            local.get 1
            f64.load offset=40
            local.get 6
            local.get 9
            local.get 0
            f64.load offset=48
            f64.mul
            f64.mul
            f64.add
            f64.store offset=40
            local.get 1
            i32.const 56
            i32.add
            local.set 1
            local.get 4
            i32.const 1
            i32.add
            local.tee 4
            i32.const 5
            i32.ne
            br_if 0 (;@4;)
          end
        end
        local.get 5
        local.set 0
        local.get 2
        i32.const 5
        i32.ne
        br_if 0 (;@2;)
      end
      i32.const 0
      local.set 1
      i32.const 1024
      local.set 0
      loop  ;; label = @2
        local.get 0
        local.get 0
        f64.load
        local.get 0
        f64.load offset=24
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        f64.add
        f64.store
        local.get 0
        local.get 0
        f64.load offset=8
        local.get 0
        f64.load offset=32
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        f64.add
        f64.store offset=8
        local.get 0
        local.get 0
        f64.load offset=16
        local.get 0
        f64.load offset=40
        f64.const 0x1.47ae147ae147bp-7 (;=0.01;)
        f64.mul
        f64.add
        f64.store offset=16
        local.get 0
        i32.const 56
        i32.add
        local.set 0
        local.get 1
        i32.const 1
        i32.add
        local.tee 1
        i32.const 5
        i32.ne
        br_if 0 (;@2;)
      end
      local.get 3
      br_if 0 (;@1;)
    end
    i32.const 0
    call 0
    unreachable)
  (func (;3;) (type 2) (result i32)
    global.get 0)
  (func (;4;) (type 0) (param i32)
    local.get 0
    global.set 0)
  (func (;5;) (type 3) (param i32) (result i32)
    global.get 0
    local.get 0
    i32.sub
    i32.const -16
    i32.and
    local.tee 0
    global.set 0
    local.get 0)
  (func (;6;) (type 2) (result i32)
    i32.const 1304)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 256 256)
  (global (;0;) (mut i32) (i32.const 5244192))
  (export "memory" (memory 0))
  (export "__indirect_function_table" (table 0))
  (export "_start" (func 2))
  (export "__errno_location" (func 6))
  (export "stackSave" (func 3))
  (export "stackRestore" (func 4))
  (export "stackAlloc" (func 5))
  (elem (;0;) (i32.const 1) func 1)
  (data (;0;) (i32.const 1072) "\deE\be\c9<\bdC@,\d9<4\a0]\13@|\db\1f\c0\ab\90\f2\bf\f0\eb%l\f9\86\ba\bf\bc\cc\93\9b\06g\e3?\9b\94}\f5\f2~\06@\15\07Z\9a\d7\d2\99\bf\d83\ab\d9\95L\a3?g\ca2\c3\cd\af @\b0\01\de1\cb\7f\10@|F\eb\e1S\d3\d9\bfB\94\87\b8!,\f0\bf\13\8f\1f\bf\e95\fd?\b4#\11_H<\81?7\c6\07\0dI\1d\87?\cf\d9\a7\ce\ea\c9)@~f&\d6\e88.\c0\a0}%\beW\95\cc\bf\ef\1b\91\a9\1cS\f1?\c5\bbT>\7f\cc\eb?|>\f2\fak/\86\bf\b3\1e\f4\9c\d2=\5c?*W\05\a9g\c2.@ \a2\c83X\eb9\c0@\e5\ab\93\f3\f1\c6?J\bcY\16\b6T\ef?\a3\fb\c41\c6\07\e3?\f6evX\88\cb\a1\bf\ac\99\17S\f3\a8`?"))
