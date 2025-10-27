(module
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (result i32)))
  (type $t3 (func (param f64) (result f64)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32) (result i32)))
  (import "env" "malloc" (func $env.malloc (type $t0)))
  (import "env" "free" (func $env.free (type $t1)))
  (import "env" "__VERIFIER_nondet_int" (func $env.__VERIFIER_nondet_int (type $t2)))
  (import "env" "exp2" (func $env.exp2 (type $t3)))
  (import "env" "assert" (func $env.assert (type $t1)))
  (func $__wasm_call_ctors (type $t4))
  (func $NewTreeNode (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32)
    i32.const 8
    call $env.malloc
    local.tee $l2
    local.get $p1
    i32.store offset=4
    local.get $l2
    local.get $p0
    i32.store
    local.get $l2)
  (func $ItemCheck (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    local.get $p0
    i32.load
    local.tee $l2
    i32.eqz
    if $I0
      i32.const 1
      return
    end
    loop $L1
      local.get $l2
      call $ItemCheck
      local.get $l1
      i32.add
      i32.const 1
      i32.add
      local.set $l1
      local.get $p0
      i32.load offset=4
      local.tee $p0
      i32.load
      local.tee $l2
      br_if $L1
    end
    local.get $l1
    i32.const 1
    i32.add)
  (func $BottomUpTree (type $t0) (param $p0 i32) (result i32)
    local.get $p0
    if $I0
      local.get $p0
      i32.const 1
      i32.sub
      local.tee $p0
      call $BottomUpTree
      local.get $p0
      call $BottomUpTree
      call $NewTreeNode
      return
    end
    i32.const 0
    i32.const 0
    call $NewTreeNode)
  (func $DeleteTree (type $t1) (param $p0 i32)
    (local $l1 i32)
    local.get $p0
    i32.load
    local.tee $l1
    if $I0
      local.get $l1
      call $DeleteTree
      local.get $p0
      i32.load offset=4
      call $DeleteTree
    end
    local.get $p0
    call $env.free)
  (func $_start (type $t2) (result i32)
    (local $l0 i32) (local $l1 f64) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    block $B0 (result i32)
      i32.const 6
      call $env.__VERIFIER_nondet_int
      local.tee $l0
      local.get $l0
      i32.const 6
      i32.le_u
      select
      local.tee $l4
      i32.const 1
      i32.add
      local.tee $l0
      f64.convert_i32_u
      call $env.exp2
      local.tee $l1
      f64.abs
      f64.const 0x1p+31 (;=2.14748e+09;)
      f64.lt
      if $I1
        local.get $l1
        i32.trunc_f64_s
        br $B0
      end
      i32.const -2147483648
    end
    local.set $l5
    local.get $l0
    call $BottomUpTree
    local.tee $l0
    call $ItemCheck
    local.get $l5
    i32.eq
    call $env.assert
    local.get $l0
    call $DeleteTree
    i32.const 4
    local.set $l2
    local.get $l4
    i32.const 4
    i32.add
    local.set $l6
    local.get $l4
    call $BottomUpTree
    loop $L2
      i32.const 0
      local.set $l3
      block $B3 (result i32)
        local.get $l6
        local.get $l2
        i32.sub
        f64.convert_i32_u
        call $env.exp2
        local.tee $l1
        f64.abs
        f64.const 0x1p+31 (;=2.14748e+09;)
        f64.lt
        if $I4
          local.get $l1
          i32.trunc_f64_s
          br $B3
        end
        i32.const -2147483648
      end
      local.tee $l0
      i32.const 0
      i32.gt_s
      if $I5
        local.get $l0
        local.set $l5
        loop $L6
          local.get $l2
          call $BottomUpTree
          local.tee $l8
          call $ItemCheck
          local.set $l9
          local.get $l8
          call $DeleteTree
          local.get $l3
          local.get $l9
          i32.add
          local.set $l3
          local.get $l5
          i32.const 1
          i32.sub
          local.tee $l5
          br_if $L6
        end
      end
      block $B7 (result i32)
        local.get $l2
        f64.convert_i32_u
        call $env.exp2
        local.tee $l1
        f64.abs
        f64.const 0x1p+31 (;=2.14748e+09;)
        f64.lt
        if $I8
          local.get $l1
          i32.trunc_f64_s
          br $B7
        end
        i32.const -2147483648
      end
      local.get $l0
      i32.mul
      local.get $l3
      i32.eq
      call $env.assert
      local.get $l2
      i32.const 2
      i32.add
      local.tee $l2
      local.get $l4
      i32.le_u
      br_if $L2
    end
    block $B9 (result i32)
      local.get $l4
      f64.convert_i32_u
      call $env.exp2
      local.tee $l1
      f64.abs
      f64.const 0x1p+31 (;=2.14748e+09;)
      f64.lt
      if $I10
        local.get $l1
        i32.trunc_f64_s
        br $B9
      end
      i32.const -2147483648
    end
    local.set $l3
    call $ItemCheck
    local.get $l3
    i32.eq
    call $env.assert
    i32.const 0)
  (memory $memory 2)
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
  (export "NewTreeNode" (func $NewTreeNode))
  (export "ItemCheck" (func $ItemCheck))
  (export "BottomUpTree" (func $BottomUpTree))
  (export "DeleteTree" (func $DeleteTree))
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
  (@custom ".debug_loc" "\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\02\004\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\04\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\01\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\08\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\02\0f\00I\13\00\00\03\16\00I\13\03\0e:\0b;\0b\00\00\04\13\01\03\0e\0b\0b:\0b;\0b\00\00\05\0d\00\03\0eI\13:\0b;\0b8\0b\00\00\06$\00\03\0e>\0b\0b\0b\00\00\07.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19I\13?\19\00\00\08\05\00\02\17\03\0e:\0b;\0bI\13\00\00\09\05\00\02\18\03\0e:\0b;\0bI\13\00\00\0a4\00\02\17\03\0e:\0b;\0bI\13\00\00\0b\89\82\01\001\13\11\01\00\00\0c.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\0d\05\00I\13\00\00\0e\0f\00\00\00\0f.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\10.\01\03\0e:\0b;\0b'\19<\19?\19\00\00\11.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\124\00\1c\0f\03\0e:\0b;\0bI\13\00\00\13\0b\01\11\01\12\06\00\00\144\00\03\0e:\0b;\0bI\13\00\00\00")
  (@custom ".debug_info" "\ed\02\00\00\04\00\00\00\00\00\04\01T\01\00\00\1d\00D\01\00\00\00\00\00\00\de\00\00\00\00\00\00\00\00\00\00\00\02+\00\00\00\036\00\00\00\c9\00\00\00\01\11\04<\00\00\00\08\01\0e\05%\00\00\00W\00\00\00\01\0f\00\05\1f\00\00\00W\00\00\00\01\10\04\00\026\00\00\00\06\7f\00\00\00\05\04\07\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\d2\00\00\00\01\14&\00\00\00\08,\00\00\00%\00\00\00\01\14&\00\00\00\09\04\ed\00\01\9f\1f\00\00\00\01\14&\00\00\00\0a\00\00\00\00\00\00\00\00\01\16&\00\00\00\0b\b6\00\00\00\00\00\00\00\00\0c=\01\00\00\02\cb\c7\00\00\00\0d\c8\00\00\00\00\0e\03\d3\00\00\00*\00\00\00\02\9c\06v\00\00\00\07\04\07\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9fE\00\00\00\01!\5c\00\00\00\08J\00\00\00\84\00\00\00\01!&\00\00\00\0b\da\00\00\00\00\00\00\00\00\07\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\97\00\00\00\01*&\00\00\00\09\04\ed\00\00\9fQ\00\00\00\01*\e9\02\00\00\0b\0e\01\00\00\00\00\00\00\0b\0e\01\00\00\00\00\00\00\0bc\00\00\00\00\00\00\00\0bc\00\00\00\00\00\00\00\00\0f\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\b0\00\00\00\017\09\04\ed\00\00\9f\84\00\00\00\017&\00\00\00\0b^\01\00\00\00\00\00\00\0b^\01\00\00\00\00\00\00\0b\a1\01\00\00\00\00\00\00\00\10\89\00\00\00\02\ce\0d\c7\00\00\00\00\11\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\0b\00\00\00\01C\e2\02\00\00\0a\84\00\00\00R\01\00\00\01E\e9\02\00\00\12\04`\00\00\00\01E\e9\02\00\00\0a\b0\00\00\00W\00\00\00\01E\e9\02\00\00\0a\dc\00\00\00i\00\00\00\01E\e9\02\00\00\0a\08\01\00\00\a4\00\00\00\01F&\00\00\00\0a4\01\00\00Q\00\00\00\01E\e9\02\00\00\0al\01\00\00\bb\00\00\00\01F&\00\00\00\0a\d2\01\00\00\8e\00\00\00\01F&\00\00\00\13\00\00\00\00\00\00\00\00\0a\8a\01\00\00?\00\00\00\01\5c\5c\00\00\00\0a\b5\01\00\00O\00\00\00\01\5c\5c\00\00\00\141\00\00\00\01\5c\5c\00\00\00\00\0b\0e\01\00\00\00\00\00\00\0b\da\00\00\00\00\00\00\00\0b\d5\02\00\00\00\00\00\00\0b^\01\00\00\00\00\00\00\0b\0e\01\00\00\00\00\00\00\0b\0e\01\00\00\00\00\00\00\0b\da\00\00\00\00\00\00\00\0b^\01\00\00\00\00\00\00\0b\d5\02\00\00\00\00\00\00\0b\da\00\00\00\00\00\00\00\0b\d5\02\00\00\00\00\00\00\00\10\04\00\00\00\02\de\0d\e2\02\00\00\00\06\1b\00\00\00\05\04\06\12\00\00\00\07\04\00")
  (@custom ".debug_ranges" "\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "new\00assert\00_start\00unsigned int\00right\00left\00size_t\00iterations\00tn\00check\00ItemCheck\00i\00depth\00maxDepth\00minDepth\00stretchDepth\00unsigned long\00tree\00free\00tempTree\00BottomUpTree\00stretchTree\00DeleteTree\00longLivedTree\00treeNode\00NewTreeNode\00/home/sven/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00malloc\00binarytrees.c\00N\00clang version 19.1.7\00")
  (@custom ".debug_line" "?\00\00\00\04\009\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00binarytrees.c\00\00\00\00stdlib.h\00\01\00\00\00")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0619.1.7")
  (@custom "target_features" "\04+\0fmutable-globals+\08sign-ext+\0freference-types+\0amultivalue"))
