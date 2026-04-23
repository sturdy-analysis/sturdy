(module $test-call-by-reference.wasm
  (type $t0 (func (param i32)))
  (type $t1 (func))
  (type $t2 (func (param i32) (result i32)))
  (type $t3 (func (result i32)))
  (import "env" "assert" (func $assert (type $t0)))
  (func $__wasm_call_ctors (type $t1))
  (func $fact (type $t2) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.set $l1
    local.get $l1
    global.set $__stack_pointer
    local.get $l1
    local.get $p0
    i32.store offset=8
    block $B0
      block $B1
        local.get $l1
        i32.load offset=8
        i32.const 1
        i32.eq
        i32.const 1
        i32.and
        i32.eqz
        br_if $B1
        local.get $l1
        local.get $l1
        i32.load offset=8
        i32.store offset=12
        br $B0
      end
      local.get $l1
      local.get $l1
      i32.load offset=8
      i32.const 1
      i32.sub
      call $fact
      local.get $l1
      i32.load offset=8
      i32.mul
      i32.store offset=12
    end
    local.get $l1
    i32.load offset=12
    local.set $l2
    local.get $l1
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get $l2
    return)
  (func $_start (type $t3) (result i32)
    i32.const 5
    call $fact
    i32.const 120
    i32.eq
    i32.const 1
    i32.and
    call $assert
    i32.const 0
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
  (export "fact" (func $fact))
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
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\02.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\03\05\00\02\18\03\0e:\0b;\0bI\13\00\00\04.\00\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\05$\00\03\0e>\0b\0b\0b\00\00\00")
  (@custom ".debug_info" "l\00\00\00\04\00\00\00\00\00\04\01\af\00\00\00\1d\00\8f\00\00\00\00\00\00\00\12\00\00\00\00\00\00\00\00\00\00\00\02\05\00\00\00l\00\00\00\04\ed\00\01\9f\0d\00\00\00\01\03h\00\00\00\03\02\91\08\00\00\00\00\01\03h\00\00\00\00\04r\00\00\00\1a\00\00\00\07\ed\03\00\00\00\00\9f\02\00\00\00\01\0bh\00\00\00\05\09\00\00\00\05\04\00")
  (@custom ".debug_ranges" "\05\00\00\00q\00\00\00r\00\00\00\8c\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "x\00_start\00int\00fact\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00../src/test-call-by-reference.c\00clang version 21.1.7\00")
  (@custom ".debug_line" "\94\00\00\00\04\007\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00\00test-call-by-reference.c\00\01\00\00\00\00\05\02\05\00\00\00\14\05\08\0a\08\bb\05\0a\06\90\05\10\06\91\05\09\06tS\05\15\06'\05\17\06t\05\10<\05\1ef\05\1cX\05\09 \03y<\05\01\06\03\09 \02\16\00\01\01\05\0c\0a\00\05\02s\00\00\00\03\0b\01\05\14\06\82\05\05t\06g\02\04\00\01\01")
  (@custom "name" "\00\1c\1btest-call-by-reference.wasm\01*\04\00\06assert\01\11__wasm_call_ctors\02\04fact\03\06_start\07\12\01\00\0f__stack_pointer")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
