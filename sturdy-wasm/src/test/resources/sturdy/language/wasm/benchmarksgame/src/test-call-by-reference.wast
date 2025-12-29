(module $test-call-by-reference.wasm
  (type $t0 (func (param i32)))
  (type $t1 (func))
  (type $t2 (func (param i32) (result i32)))
  (type $t3 (func (result i32)))
  (import "env" "assert" (func $assert (type $t0)))
  (func $__wasm_call_ctors (type $t1))
  (func $fact (type $t2) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    i32.const 1
    local.set $l1
    block $B0
      local.get $p0
      i32.const 1
      i32.eq
      br_if $B0
      local.get $p0
      i32.const -1
      i32.add
      local.tee $l2
      i32.const 7
      i32.and
      local.set $l3
      block $B1
        block $B2
          local.get $p0
          i32.const -2
          i32.add
          i32.const 7
          i32.ge_u
          br_if $B2
          i32.const 1
          local.set $l1
          br $B1
        end
        i32.const 0
        local.set $l4
        i32.const 0
        local.get $l2
        i32.const -8
        i32.and
        i32.sub
        local.set $l5
        i32.const 1
        local.set $l1
        loop $L3
          local.get $p0
          local.get $l4
          i32.add
          local.tee $l2
          i32.const -7
          i32.add
          local.get $l2
          i32.const -6
          i32.add
          local.get $l2
          i32.const -5
          i32.add
          local.get $l2
          i32.const -4
          i32.add
          local.get $l2
          i32.const -3
          i32.add
          local.get $l2
          i32.const -2
          i32.add
          local.get $l2
          i32.const -1
          i32.add
          local.get $l2
          local.get $l1
          i32.mul
          i32.mul
          i32.mul
          i32.mul
          i32.mul
          i32.mul
          i32.mul
          i32.mul
          local.set $l1
          local.get $l5
          local.get $l4
          i32.const -8
          i32.add
          local.tee $l4
          i32.ne
          br_if $L3
        end
        local.get $p0
        local.get $l4
        i32.add
        local.set $p0
      end
      local.get $l3
      i32.eqz
      br_if $B0
      loop $L4
        local.get $p0
        local.get $l1
        i32.mul
        local.set $l1
        local.get $p0
        i32.const -1
        i32.add
        local.set $p0
        local.get $l3
        i32.const -1
        i32.add
        local.tee $l3
        br_if $L4
      end
    end
    local.get $l1)
  (func $_start (type $t3) (result i32)
    i32.const 1
    call $assert
    i32.const 0)
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
  (@custom ".debug_loc" "\ff\ff\ff\ff\06\00\00\00\00\00\00\00C\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\02.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19I\13?\19\00\00\03\05\00\02\17\03\0e:\0b;\0bI\13\00\00\04.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\05\89\82\01\001\13\11\01\00\00\06.\01\03\0e:\0b;\0b'\19<\19?\19\00\00\07\05\00I\13\00\00\08$\00\03\0e>\0b\0b\0b\00\00\00")
  (@custom ".debug_info" "\87\00\00\00\04\00\00\00\00\00\04\01\9c\00\00\00\1d\00\83\00\00\00\00\00\00\00\19\00\00\00\00\00\00\00\00\00\00\00\02\06\00\00\00\b3\00\00\00\07\ed\03\00\00\00\00\9f\14\00\00\00\01\03\83\00\00\00\03\00\00\00\00\00\00\00\00\01\03\83\00\00\00\00\04\ba\00\00\00\0c\00\00\00\07\ed\03\00\00\00\00\9f\09\00\00\00\01\0b\83\00\00\00\05v\00\00\00\c3\00\00\00\00\06\02\00\00\00\02\e0\07\83\00\00\00\00\08\10\00\00\00\05\04\00")
  (@custom ".debug_ranges" "\06\00\00\00\b9\00\00\00\ba\00\00\00\c6\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "x\00assert\00_start\00int\00fact\00/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00test-call-by-reference.c\00clang version 19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)\00")
  (@custom ".debug_line" "\a4\00\00\00\04\00D\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00test-call-by-reference.c\00\00\00\00stdlib.h\00\01\00\00\00\00\05\02\06\00\00\00\14\05\0a\0a\c9\05\08\06 *N\08TN*\b0\05\17\06\85\05\1c\06\ac\03y\02-\01\05\08\06j\06\d2$\05\1c\06[\05\17\06\c8\05\08\06q\05\01y\02\03\00\01\01\00\05\02\ba\00\00\00\03\0a\01\05\05\0a=\06\03tf\06\03\0d.\02\01\00\01\01")
  (@custom "name" "\00\1c\1btest-call-by-reference.wasm\01*\04\00\06assert\01\11__wasm_call_ctors\02\04fact\03\06_start\07\12\01\00\0f__stack_pointer")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clangZ19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)")
  (@custom "target_features" "\04+\0amultivalue+\0fmutable-globals+\0freference-types+\08sign-ext"))
