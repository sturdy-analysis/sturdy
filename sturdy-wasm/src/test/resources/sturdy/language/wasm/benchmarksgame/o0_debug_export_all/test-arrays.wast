(module $test-arrays.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "blackhole_int" (func $blackhole_int (type $t0)))
  (func $__wasm_call_ctors (type $t1))
  (func $_start (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.set $l1
    local.get $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store offset=12
    local.get $l1
    local.get $l1
    i32.load offset=12
    call $malloc
    i32.store offset=8
    local.get $l1
    i32.const 0
    i32.store offset=4
    local.get $l1
    i32.const 0
    i32.store
    block $B0
      loop $L1
        local.get $l1
        i32.load
        local.get $l1
        i32.load offset=12
        i32.lt_s
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l1
        local.get $l1
        i32.load offset=8
        local.get $l1
        i32.load
        call $blackhole_int
        i32.const 2
        i32.shl
        i32.add
        i32.load
        call $blackhole_int
        local.get $l1
        i32.load offset=4
        i32.add
        i32.store offset=4
        local.get $l1
        local.get $l1
        i32.load
        i32.const 1
        i32.add
        i32.store
        br $L1
      end
    end
    local.get $l1
    i32.load offset=4
    local.set $l2
    local.get $l1
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get $l2
    return)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66560))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1024))
  (global $__stack_low i32 (i32.const 1024))
  (global $__stack_high i32 (i32.const 66560))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66560))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
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
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01\12\06\00\00\02\0f\00I\13\00\00\03$\00\03\0e>\0b\0b\0b\00\00\04.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\05\05\00\02\18\03\0e:\0b;\0bI\13\00\00\064\00\02\18\03\0e:\0b;\0bI\13\00\00\07\0b\01\11\01\12\06\00\00\00")
  (@custom ".debug_info" "\8a\00\00\00\04\00\00\00\00\00\04\01\ac\00\00\00\1d\00\97\00\00\00\00\00\00\00\11\00\00\00\06\00\00\00\9f\00\00\00\02+\00\00\00\03\09\00\00\00\05\04\04\06\00\00\00\9f\00\00\00\04\ed\00\01\9f\02\00\00\00\01\03+\00\00\00\05\02\91\0c\90\00\00\00\01\03+\00\00\00\06\02\91\08\00\00\00\00\01\04&\00\00\00\06\02\91\04\0d\00\00\00\01\05+\00\00\00\07:\00\00\00U\00\00\00\06\02\91\00\8e\00\00\00\01\06+\00\00\00\00\00\00")
  (@custom ".debug_str" "x\00_start\00int\00sum\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00i\00length\00../src/test-arrays.c\00clang version 21.1.7\00")
  (@custom ".debug_line" "\84\00\00\00\04\00,\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00\00test-arrays.c\00\01\00\00\00\00\05\02\06\00\00\00\14\05\1c\0a\08\bb\05\15\06t\05\0af\05\09\06=\05\0du\05\14\06t\05\18\90\05\16X\05\05 \05\1e\06g\05.\06t\05 X\05\1ef\05\10t\05\0df\05!\06\8f\05\05\06\c8.\05\0c\061\05\05\06t\02\0f\00\01\01")
  (@custom "name" "\00\11\10test-arrays.wasm\013\04\00\06malloc\01\0dblackhole_int\02\11__wasm_call_ctors\03\06_start\07\12\01\00\0f__stack_pointer")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
