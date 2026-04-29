(module $spectral-norm.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func))
  (type $t2 (func (param i32 i32) (result f64)))
  (type $t3 (func (param i32 i32 i32)))
  (type $t4 (func (result i32)))
  (import "env" "printf" (func $printf (type $t0)))
  (func $__wasm_call_ctors (type $t1))
  (func $eval_A (type $t2) (param $p0 i32) (param $p1 i32) (result f64)
    (local $l2 i32) (local $l3 f64)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.set $l2
    local.get $l2
    local.get $p0
    i32.store offset=12
    local.get $l2
    local.get $p1
    i32.store offset=8
    local.get $l2
    i32.load offset=12
    local.get $l2
    i32.load offset=8
    i32.add
    local.get $l2
    i32.load offset=12
    local.get $l2
    i32.load offset=8
    i32.add
    i32.const 1
    i32.add
    i32.mul
    i32.const 2
    i32.div_s
    local.get $l2
    i32.load offset=12
    i32.add
    i32.const 1
    i32.add
    f64.convert_i32_s
    local.set $l3
    f64.const 0x1p+0 (;=1;)
    local.get $l3
    f64.div
    return)
  (func $eval_A_times_u (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 f64) (local $l5 f64) (local $l6 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.set $l3
    local.get $l3
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=28
    local.get $l3
    local.get $p1
    i32.store offset=24
    local.get $l3
    local.get $p2
    i32.store offset=20
    local.get $l3
    i32.const 0
    i32.store offset=16
    block $B0
      loop $L1
        local.get $l3
        i32.load offset=16
        local.get $l3
        i32.load offset=28
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l3
        i32.load offset=20
        local.get $l3
        i32.load offset=16
        i32.const 3
        i32.shl
        i32.add
        i32.const 0
        f64.convert_i32_s
        f64.store
        local.get $l3
        i32.const 0
        i32.store offset=12
        block $B2
          loop $L3
            local.get $l3
            i32.load offset=12
            local.get $l3
            i32.load offset=28
            i32.lt_s
            i32.const 1
            i32.and
            i32.eqz
            br_if $B2
            local.get $l3
            i32.load offset=16
            local.get $l3
            i32.load offset=12
            call $eval_A
            local.set $l4
            local.get $l3
            i32.load offset=24
            local.get $l3
            i32.load offset=12
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l5
            local.get $l3
            i32.load offset=20
            local.get $l3
            i32.load offset=16
            i32.const 3
            i32.shl
            i32.add
            local.set $l6
            local.get $l6
            local.get $l6
            f64.load
            local.get $l4
            local.get $l5
            f64.mul
            f64.add
            f64.store
            local.get $l3
            local.get $l3
            i32.load offset=12
            i32.const 1
            i32.add
            i32.store offset=12
            br $L3
          end
        end
        local.get $l3
        local.get $l3
        i32.load offset=16
        i32.const 1
        i32.add
        i32.store offset=16
        br $L1
      end
    end
    local.get $l3
    i32.const 32
    i32.add
    global.set $__stack_pointer
    return)
  (func $eval_At_times_u (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 f64) (local $l5 f64) (local $l6 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.set $l3
    local.get $l3
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=28
    local.get $l3
    local.get $p1
    i32.store offset=24
    local.get $l3
    local.get $p2
    i32.store offset=20
    local.get $l3
    i32.const 0
    i32.store offset=16
    block $B0
      loop $L1
        local.get $l3
        i32.load offset=16
        local.get $l3
        i32.load offset=28
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l3
        i32.load offset=20
        local.get $l3
        i32.load offset=16
        i32.const 3
        i32.shl
        i32.add
        i32.const 0
        f64.convert_i32_s
        f64.store
        local.get $l3
        i32.const 0
        i32.store offset=12
        block $B2
          loop $L3
            local.get $l3
            i32.load offset=12
            local.get $l3
            i32.load offset=28
            i32.lt_s
            i32.const 1
            i32.and
            i32.eqz
            br_if $B2
            local.get $l3
            i32.load offset=12
            local.get $l3
            i32.load offset=16
            call $eval_A
            local.set $l4
            local.get $l3
            i32.load offset=24
            local.get $l3
            i32.load offset=12
            i32.const 3
            i32.shl
            i32.add
            f64.load
            local.set $l5
            local.get $l3
            i32.load offset=20
            local.get $l3
            i32.load offset=16
            i32.const 3
            i32.shl
            i32.add
            local.set $l6
            local.get $l6
            local.get $l6
            f64.load
            local.get $l4
            local.get $l5
            f64.mul
            f64.add
            f64.store
            local.get $l3
            local.get $l3
            i32.load offset=12
            i32.const 1
            i32.add
            i32.store offset=12
            br $L3
          end
        end
        local.get $l3
        local.get $l3
        i32.load offset=16
        i32.const 1
        i32.add
        i32.store offset=16
        br $L1
      end
    end
    local.get $l3
    i32.const 32
    i32.add
    global.set $__stack_pointer
    return)
  (func $eval_AtA_times_u (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.set $l3
    local.get $l3
    local.set $l4
    local.get $l3
    global.set $__stack_pointer
    local.get $l4
    local.get $p0
    i32.store offset=28
    local.get $l4
    local.get $p1
    i32.store offset=24
    local.get $l4
    local.get $p2
    i32.store offset=20
    local.get $l4
    i32.load offset=28
    local.set $l5
    local.get $l4
    local.get $l3
    i32.store offset=16
    local.get $l5
    i32.const 3
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    local.set $l6
    local.get $l3
    local.get $l6
    i32.sub
    local.set $l7
    local.get $l7
    local.set $l3
    local.get $l3
    global.set $__stack_pointer
    local.get $l4
    local.get $l5
    i32.store offset=12
    local.get $l4
    i32.load offset=28
    local.get $l4
    i32.load offset=24
    local.get $l7
    call $eval_A_times_u
    local.get $l4
    i32.load offset=28
    local.get $l7
    local.get $l4
    i32.load offset=20
    call $eval_At_times_u
    local.get $l4
    i32.load offset=16
    local.set $l3
    local.get $l4
    i32.const 32
    i32.add
    global.set $__stack_pointer
    return)
  (func $_start (type $t4) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 f64) (local $l4 i32) (local $l5 f64) (local $l6 i32) (local $l7 f64) (local $l8 i32) (local $l9 f64) (local $l10 i32)
    global.get $__stack_pointer
    i32.const 1648
    i32.sub
    local.set $l0
    local.get $l0
    global.set $__stack_pointer
    local.get $l0
    i32.const 100
    i32.store offset=1640
    local.get $l0
    local.get $l0
    i32.store offset=1636
    local.get $l0
    i32.const 0
    i32.store offset=1644
    block $B0
      loop $L1
        local.get $l0
        i32.load offset=1644
        i32.const 100
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l0
        i32.load offset=1644
        local.set $l1
        local.get $l0
        i32.const 816
        i32.add
        local.get $l1
        i32.const 3
        i32.shl
        i32.add
        f64.const 0x1p+0 (;=1;)
        f64.store
        local.get $l0
        local.get $l0
        i32.load offset=1644
        i32.const 1
        i32.add
        i32.store offset=1644
        br $L1
      end
    end
    local.get $l0
    i32.const 0
    i32.store offset=1644
    block $B2
      loop $L3
        local.get $l0
        i32.load offset=1644
        i32.const 10
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B2
        i32.const 100
        local.get $l0
        i32.const 816
        i32.add
        local.get $l0
        i32.const 16
        i32.add
        call $eval_AtA_times_u
        i32.const 100
        local.get $l0
        i32.const 16
        i32.add
        local.get $l0
        i32.const 816
        i32.add
        call $eval_AtA_times_u
        local.get $l0
        local.get $l0
        i32.load offset=1644
        i32.const 1
        i32.add
        i32.store offset=1644
        br $L3
      end
    end
    local.get $l0
    i32.const 0
    f64.convert_i32_s
    f64.store offset=1616
    local.get $l0
    i32.const 0
    f64.convert_i32_s
    f64.store offset=1624
    local.get $l0
    i32.const 0
    i32.store offset=1644
    block $B4
      loop $L5
        local.get $l0
        i32.load offset=1644
        i32.const 100
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B4
        local.get $l0
        i32.load offset=1644
        local.set $l2
        local.get $l0
        i32.const 816
        i32.add
        local.get $l2
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l3
        local.get $l0
        i32.load offset=1644
        local.set $l4
        local.get $l0
        i32.const 16
        i32.add
        local.get $l4
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l5
        local.get $l0
        local.get $l0
        f64.load offset=1624
        local.get $l3
        local.get $l5
        f64.mul
        f64.add
        f64.store offset=1624
        local.get $l0
        i32.load offset=1644
        local.set $l6
        local.get $l0
        i32.const 16
        i32.add
        local.get $l6
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l7
        local.get $l0
        i32.load offset=1644
        local.set $l8
        local.get $l0
        i32.const 16
        i32.add
        local.get $l8
        i32.const 3
        i32.shl
        i32.add
        f64.load
        local.set $l9
        local.get $l0
        local.get $l0
        f64.load offset=1616
        local.get $l7
        local.get $l9
        f64.mul
        f64.add
        f64.store offset=1616
        local.get $l0
        local.get $l0
        i32.load offset=1644
        i32.const 1
        i32.add
        i32.store offset=1644
        br $L5
      end
    end
    local.get $l0
    local.get $l0
    f64.load offset=1624
    local.get $l0
    f64.load offset=1616
    f64.div
    f64.sqrt
    f64.store
    i32.const 1024
    local.get $l0
    call $printf
    drop
    local.get $l0
    i32.load offset=1636
    local.set $l0
    i32.const 0
    local.set $l10
    local.get $l0
    i32.const 1648
    i32.add
    global.set $__stack_pointer
    local.get $l10
    return)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1031))
  (global $__stack_low i32 (i32.const 1040))
  (global $__stack_high i32 (i32.const 66576))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66576))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "eval_A" (func $eval_A))
  (export "eval_A_times_u" (func $eval_A_times_u))
  (export "eval_At_times_u" (func $eval_At_times_u))
  (export "eval_AtA_times_u" (func $eval_AtA_times_u))
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
  (data $.rodata (i32.const 1024) "%0.9f\0a\00")
  (@custom ".debug_loc" "\ff\ff\ff\ff>\02\00\00d\00\00\00\9c\00\00\00\03\00\ed\00\07\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\07.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\08\05\00\02\18\03\0e:\0b;\0bI\13\00\00\09.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\0a4\00\02\18\03\0e:\0b;\0bI\13\00\00\0b4\00\02\18\03\0eI\134\19\00\00\0c4\00\02\17\03\0e:\0b;\0bI\13\00\00\0d.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\0e\0f\00I\13\00\00\0f&\00I\13\00\00\10!\00I\137\13\00\00\00")
  (@custom ".debug_info" "E\02\00\00\04\00\00\00\00\00\04\01\11\01\00\00\1d\00\dd\00\00\00\00\00\00\00U\00\00\00\00\00\00\00\00\00\00\00\023\00\00\00\010\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\07\00\05P\00\00\00\06\01\06\f4\00\00\00\08\07\07\05\00\00\00U\00\00\00\04\ed\00\02\9f\0a\01\00\00\01\09\04\02\00\00\08\02\91\0c\d4\00\00\00\01\09\0b\02\00\00\08\02\91\08\d2\00\00\00\01\09\0b\02\00\00\00\09\5c\00\00\00\ef\00\00\00\04\ed\00\03\9f(\00\00\00\01\0b\08\02\91\1c\08\01\00\00\01\0b\0b\02\00\00\08\02\91\18:\00\00\00\01\0b\12\02\00\00\08\02\91\149\00\00\00\01\0b\1c\02\00\00\0a\02\91\10\d4\00\00\00\01\0d\0b\02\00\00\0a\02\91\0c\d2\00\00\00\01\0d\0b\02\00\00\00\09M\01\00\00\ef\00\00\00\04\ed\00\03\9f\07\00\00\00\01\15\08\02\91\1c\08\01\00\00\01\15\0b\02\00\00\08\02\91\18:\00\00\00\01\15\12\02\00\00\08\02\91\149\00\00\00\01\15\1c\02\00\00\0a\02\91\10\d4\00\00\00\01\17\0b\02\00\00\0a\02\91\0c\d2\00\00\00\01\17\0b\02\00\00\00\09>\02\00\00\9c\00\00\00\04\ed\00\04\9f\17\00\00\00\01\1f\08\02\91\1c\08\01\00\00\01\1f\0b\02\00\00\08\02\91\18:\00\00\00\01\1f\12\02\00\00\08\02\91\147\00\00\00\01\1f\1c\02\00\00\0b\02\91\0c&\01\00\00!\02\00\00\0c\00\00\00\00\05\00\00\00\01 (\02\00\00\00\0d\dc\02\00\00\e1\01\00\00\04\ed\00\00\9f<\00\00\00\01\22\0b\02\00\00\0a\03\91\ec\0c\d4\00\00\00\01$\0b\02\00\00\0a\03\91\e8\0c\08\01\00\00\01&7\02\00\00\0a\03\91\b0\06:\00\00\00\01'<\02\00\00\0a\02\91\10\05\00\00\00\01'<\02\00\00\0a\03\91\d8\0c\03\00\00\00\01'\04\02\00\00\0a\03\91\d0\0c\00\00\00\00\01'\04\02\00\00\00\05\d6\00\00\00\04\08\05L\00\00\00\05\04\0e\17\02\00\00\0f\04\02\00\00\0e\04\02\00\00\05C\00\00\00\07\04\03\04\02\00\00\10F\00\00\00v\01\00\00\00\0f\0b\02\00\00\03\04\02\00\00\04F\00\00\00d\00\00")
  (@custom ".debug_ranges" "\05\00\00\00Z\00\00\00\5c\00\00\00K\01\00\00M\01\00\00<\02\00\00>\02\00\00\da\02\00\00\dc\02\00\00\bd\04\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "vv\00vBv\00eval_At_times_u\00eval_AtA_times_u\00eval_A_times_u\00AtAu\00_start\00unsigned int\00char\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00j\00i\00double\00../src/spectral-norm.c\00__ARRAY_SIZE_TYPE__\00N\00eval_A\00clang version 21.1.7\00__vla_expr0\00")
  (@custom ".debug_line" ")\02\00\00\04\00.\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00\00spectral-norm.c\00\01\00\00\00\00\05\02\05\00\00\00\1a\05,\0a\08\c8\05.\06X\05-X\052 \054X\053X\055 \050<\058 \05;<\05:X\05< \05*<\05)<\05\1f\ba\02\02\00\01\01\00\05\02\5c\00\00\00\03\0b\01\05\08\0a\02/\14\05\0b\06t\05\0d\90\05\0cX\05\03 \05\07\06h\05\0a\06X\05\07X\05\0cJ\06g\05\0f\06t\05\11\90\05\10X\05\07 \05&f\05(X\05\1fX\05+\82\05-X\05+X\05\18\90\05\1bX\05\18X\05\1df\05\14\f2\05\07\c8.\05\10\06+\05\03\06\c8.\05\01\063\02\0d\00\01\01\00\05\02M\01\00\00\03\15\01\05\08\0a\02/\14\05\0b\06t\05\0d\90\05\0cX\05\03 \05\07\06h\05\0a\06X\05\07X\05\0cJ\06g\05\0f\06t\05\11\90\05\10X\05\07 \05&f\05(X\05\1fX\05+\82\05-X\05+X\05\18\90\05\1bX\05\18X\05\1df\05\14\f2\05\07\c8.\05\10\06+\05\03\06\c8.\05\01\063\02\0d\00\01\01\00\05\02>\02\00\00\03\1f\01\05\0c\0a\02/\12\05\03\06t\05\1f\02.\12\05!X\05\10X\057\82\05;X\05't\05Bf\02\14\00\01\01\00\05\02\dc\02\00\00\03\22\01\05\0d\0a\02'\15\05\03\91\05\08\83\05\0b\06\82\05\0c\9e\05\03J\05\16f\05\14\82\05\18\ba\05\10\ba\05\03\e4.\05\08\06/\05\0b\06\82\05\0c\9e\05\03<\05\07\06h\08=\05\11\089\05\03\06\e4.\05\09\063\05\06\06\90\05\08\06\91\05\0b\06\82\05\0c\9e\05\03J\05\1df\05\1b\82\05\22\08\12\05 \82\05\19\f2\05,\08 \05*\82\051\f2\05/\82\05(\f2\05\10\08 \05\03\e4.\05\19\06/\05\1d\06\82\05\1cf\05\14 \05\03 \05\01\06\08\22\02\1c\00\01\01")
  (@custom "name" "\00\13\12spectral-norm.wasm\01_\07\00\06printf\01\11__wasm_call_ctors\02\06eval_A\03\0eeval_A_times_u\04\0feval_At_times_u\05\10eval_AtA_times_u\06\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
