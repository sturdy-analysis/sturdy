(module $test-arrays.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "blackhole_int" (func $blackhole_int (type $t0)))
  (func $__wasm_call_ctors (type $t1))
  (func $_start (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32)
    local.get $p0
    call $malloc
    local.set $l1
    block $B0
      block $B1
        local.get $p0
        i32.const 1
        i32.ge_s
        br_if $B1
        i32.const 0
        local.set $l2
        br $B0
      end
      i32.const 0
      local.set $l3
      i32.const 0
      local.set $l2
      loop $L2
        local.get $l1
        local.get $l3
        call $blackhole_int
        i32.const 2
        i32.shl
        i32.add
        i32.load
        call $blackhole_int
        local.get $l2
        i32.add
        local.set $l2
        local.get $p0
        local.get $l3
        i32.const 1
        i32.add
        local.tee $l3
        i32.ne
        br_if $L2
      end
    end
    local.get $l2)
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
  (@custom ".debug_loc" "\00\00\00\00'\00\00\00\03\00\11\00\9fE\00\00\00Q\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\00\00\00\00'\00\00\00\03\00\11\00\9fL\00\00\00N\00\00\00\04\00\ed\02\01\9fN\00\00\00Q\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\0d\00\00\00V\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01\12\06\00\00\02\0f\00I\13\00\00\03$\00\03\0e>\0b\0b\0b\00\00\04.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19I\13?\19\00\00\05\05\00\02\18\03\0e:\0b;\0bI\13\00\00\064\00\02\17\03\0e:\0b;\0bI\13\00\00\07\0b\01\11\01\12\06\00\00\08\89\82\01\001\13\11\01\00\00\09.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\0a\05\00I\13\00\00\0b\0f\00\00\00\0c\16\00I\13\03\0e:\0b;\0b\00\00\00")
  (@custom ".debug_info" "\e2\00\00\00\04\00\00\00\00\00\04\01\b8\00\00\00\1d\00\aa\00\00\00\00\00\00\009\00\00\00\05\00\00\00V\00\00\00\02+\00\00\00\03\13\00\00\00\05\04\04\05\00\00\00V\00\00\00\07\ed\03\00\00\00\00\9f\02\00\00\00\01\03+\00\00\00\05\04\ed\00\00\9f$\00\00\00\01\03+\00\00\00\06\00\00\00\00\1e\00\00\00\01\05+\00\00\00\06T\00\00\00\00\00\00\00\01\04&\00\00\00\07\1a\00\00\00<\00\00\00\06#\00\00\00\22\00\00\00\01\06+\00\00\00\00\08\b0\00\00\00\10\00\00\00\08\d4\00\00\008\00\00\00\08\d4\00\00\00E\00\00\00\00\09\a3\00\00\00\02\cb\c1\00\00\00\0a\c2\00\00\00\00\0b\0c\cd\00\00\00\17\00\00\00\02\9c\03+\00\00\00\07\04\09\09\00\00\00\02\de+\00\00\00\0a+\00\00\00\00\00")
  (@custom ".debug_str" "x\00_start\00blackhole_int\00size_t\00sum\00i\00length\00unsigned long\00/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00malloc\00test-arrays.c\00clang version 19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)\00")
  (@custom ".debug_line" "\82\00\00\00\04\009\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00test-arrays.c\00\00\00\00stdlib.h\00\01\00\00\00\05\15\0a\00\05\02\08\00\00\00\15\06\9a\05\16\06\88\05\05\06 \03z.\05 \06\eb\06\03y\ba\05\1e5\05\10X\05\0df\03yX\05!\06l\05\16\06 \05\05<\03z.\06\03\09.\02\03\00\01\01")
  (@custom "name" "\00\11\10test-arrays.wasm\013\04\00\06malloc\01\0dblackhole_int\02\11__wasm_call_ctors\03\06_start\07\12\01\00\0f__stack_pointer")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clangZ19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)")
  (@custom "target_features" "\04+\0amultivalue+\0fmutable-globals+\0freference-types+\08sign-ext"))
