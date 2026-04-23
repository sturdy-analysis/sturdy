(module $binarytrees.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (result i32)))
  (type $t3 (func (param f64 f64) (result f64)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32) (result i32)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "free" (func $free (type $t1)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t2)))
  (import "env" "pow" (func $pow (type $t3)))
  (import "env" "assert" (func $assert (type $t1)))
  (func $__wasm_call_ctors (type $t4))
  (func $NewTreeNode (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l2)
      (call $malloc
        (i32.const 8)))
    (local.set $l3
      (i32.load offset=12
        (local.get $l2)))
    (i32.store
      (i32.load offset=4
        (local.get $l2))
      (local.get $l3))
    (local.set $l4
      (i32.load offset=8
        (local.get $l2)))
    (i32.store offset=4
      (i32.load offset=4
        (local.get $l2))
      (local.get $l4))
    (local.set $l5
      (i32.load offset=4
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return
      (local.get $l5)))
  (func $ItemCheck (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=8
      (local.get $l1)
      (local.get $p0))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.eq
                (i32.load
                  (i32.load offset=8
                    (local.get $l1)))
                (i32.const 0))
              (i32.const 1))))
        (i32.store offset=12
          (local.get $l1)
          (i32.const 1))
        (br $B0))
      (i32.store offset=12
        (local.get $l1)
        (i32.add
          (i32.add
            (call $ItemCheck
              (i32.load
                (i32.load offset=8
                  (local.get $l1))))
            (i32.const 1))
          (call $ItemCheck
            (i32.load offset=4
              (i32.load offset=8
                (local.get $l1)))))))
    (local.set $l2
      (i32.load offset=12
        (local.get $l1)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return
      (local.get $l2)))
  (func $BottomUpTree (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=8
      (local.get $l1)
      (local.get $p0))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.gt_u
                (i32.load offset=8
                  (local.get $l1))
                (i32.const 0))
              (i32.const 1))))
        (i32.store offset=12
          (local.get $l1)
          (call $NewTreeNode
            (call $BottomUpTree
              (i32.sub
                (i32.load offset=8
                  (local.get $l1))
                (i32.const 1)))
            (call $BottomUpTree
              (i32.sub
                (i32.load offset=8
                  (local.get $l1))
                (i32.const 1)))))
        (br $B0))
      (local.set $l2
        (i32.const 0))
      (i32.store offset=12
        (local.get $l1)
        (call $NewTreeNode
          (local.get $l2)
          (local.get $l2))))
    (local.set $l3
      (i32.load offset=12
        (local.get $l1)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return
      (local.get $l3)))
  (func $DeleteTree (type $t1) (param $p0 i32)
    (local $l1 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.ne
              (i32.load
                (i32.load offset=12
                  (local.get $l1)))
              (i32.const 0))
            (i32.const 1))))
      (call $DeleteTree
        (i32.load
          (i32.load offset=12
            (local.get $l1))))
      (call $DeleteTree
        (i32.load offset=4
          (i32.load offset=12
            (local.get $l1)))))
    (call $free
      (i32.load offset=12
        (local.get $l1)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return))
  (func $_start (type $t2) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 f64) (local $l3 f64) (local $l4 f64) (local $l5 i32) (local $l6 f64) (local $l7 i32)
    (local.set $l0
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l0))
    (i32.store offset=44
      (local.get $l0)
      (call $__VERIFIER_nondet_int))
    (i32.store offset=36
      (local.get $l0)
      (i32.const 4))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.gt_u
                (i32.add
                  (i32.load offset=36
                    (local.get $l0))
                  (i32.const 2))
                (i32.load offset=44
                  (local.get $l0)))
              (i32.const 1))))
        (i32.store offset=32
          (local.get $l0)
          (i32.add
            (i32.load offset=36
              (local.get $l0))
            (i32.const 2)))
        (br $B0))
      (i32.store offset=32
        (local.get $l0)
        (i32.load offset=44
          (local.get $l0))))
    (i32.store offset=28
      (local.get $l0)
      (i32.add
        (i32.load offset=32
          (local.get $l0))
        (i32.const 1)))
    (i32.store offset=24
      (local.get $l0)
      (call $BottomUpTree
        (i32.load offset=28
          (local.get $l0))))
    (local.set $l1
      (call $ItemCheck
        (i32.load offset=24
          (local.get $l0))))
    (local.set $l2
      (f64.convert_i32_u
        (i32.load offset=28
          (local.get $l0))))
    (call $assert
      (i32.and
        (i32.eq
          (local.get $l1)
          (i32.trunc_sat_f64_s
            (call $pow
              (f64.const 0x1p+1 (;=2;))
              (local.get $l2))))
        (i32.const 1)))
    (call $DeleteTree
      (i32.load offset=24
        (local.get $l0)))
    (i32.store offset=20
      (local.get $l0)
      (call $BottomUpTree
        (i32.load offset=32
          (local.get $l0))))
    (i32.store offset=40
      (local.get $l0)
      (i32.load offset=36
        (local.get $l0)))
    (block $B2
      (loop $L3
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.le_u
                (i32.load offset=40
                  (local.get $l0))
                (i32.load offset=32
                  (local.get $l0)))
              (i32.const 1))))
        (local.set $l3
          (f64.convert_i32_u
            (i32.add
              (i32.sub
                (i32.load offset=32
                  (local.get $l0))
                (i32.load offset=40
                  (local.get $l0)))
              (i32.load offset=36
                (local.get $l0)))))
        (i32.store offset=8
          (local.get $l0)
          (i32.trunc_sat_f64_s
            (call $pow
              (f64.const 0x1p+1 (;=2;))
              (local.get $l3))))
        (i32.store offset=4
          (local.get $l0)
          (i32.const 0))
        (i32.store offset=12
          (local.get $l0)
          (i32.const 1))
        (block $B4
          (loop $L5
            (br_if $B4
              (i32.eqz
                (i32.and
                  (i32.le_s
                    (i32.load offset=12
                      (local.get $l0))
                    (i32.load offset=8
                      (local.get $l0)))
                  (i32.const 1))))
            (i32.store offset=16
              (local.get $l0)
              (call $BottomUpTree
                (i32.load offset=40
                  (local.get $l0))))
            (i32.store offset=4
              (local.get $l0)
              (i32.add
                (call $ItemCheck
                  (i32.load offset=16
                    (local.get $l0)))
                (i32.load offset=4
                  (local.get $l0))))
            (call $DeleteTree
              (i32.load offset=16
                (local.get $l0)))
            (i32.store offset=12
              (local.get $l0)
              (i32.add
                (i32.load offset=12
                  (local.get $l0))
                (i32.const 1)))
            (br $L5)))
        (local.set $l4
          (f64.convert_i32_u
            (i32.load offset=40
              (local.get $l0))))
        (call $assert
          (i32.and
            (i32.eq
              (i32.mul
                (i32.trunc_sat_f64_s
                  (call $pow
                    (f64.const 0x1p+1 (;=2;))
                    (local.get $l4)))
                (i32.load offset=8
                  (local.get $l0)))
              (i32.load offset=4
                (local.get $l0)))
            (i32.const 1)))
        (i32.store offset=40
          (local.get $l0)
          (i32.add
            (i32.load offset=40
              (local.get $l0))
            (i32.const 2)))
        (br $L3)))
    (local.set $l5
      (call $ItemCheck
        (i32.load offset=20
          (local.get $l0))))
    (local.set $l6
      (f64.convert_i32_u
        (i32.load offset=32
          (local.get $l0))))
    (call $assert
      (i32.and
        (i32.eq
          (local.get $l5)
          (i32.trunc_sat_f64_s
            (call $pow
              (f64.const 0x1p+1 (;=2;))
              (local.get $l6))))
        (i32.const 1)))
    (local.set $l7
      (i32.const 0))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 48)))
    (return
      (local.get $l7)))
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
  (export "__wasm_first_page_end" (global $__wasm_first_page_end))
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\02\0f\00I\13\00\00\03\16\00I\13\03\0e:\0b;\0b\00\00\04\13\01\03\0e\0b\0b:\0b;\0b\00\00\05\0d\00\03\0eI\13:\0b;\0b8\0b\00\00\06$\00\03\0e>\0b\0b\0b\00\00\07.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\08\05\00\02\18\03\0e:\0b;\0bI\13\00\00\094\00\02\18\03\0e:\0b;\0bI\13\00\00\0a.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\0b.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\0c\0b\01\11\01\12\06\00\00\00")
  (@custom ".debug_info" "\df\01\00\00\04\00\00\00\00\00\04\01V\01\00\00\1d\00?\01\00\00\00\00\00\001\00\00\00\00\00\00\00\00\00\00\00\02+\00\00\00\036\00\00\00*\01\00\00\01\11\04.\00\00\00\08\01\0e\05\1e\00\00\00W\00\00\00\01\0f\00\05\18\00\00\00W\00\00\00\01\10\04\00\026\00\00\00\06\e5\00\00\00\05\04\07\05\00\00\00i\00\00\00\04\ed\00\02\9f3\01\00\00\01\14&\00\00\00\08\02\91\0c\1e\00\00\00\01\14&\00\00\00\08\02\91\08\18\00\00\00\01\14&\00\00\00\09\02\91\04\00\00\00\00\01\16&\00\00\00\00\07o\00\00\00x\00\00\00\04\ed\00\01\9f\b4\00\00\00\01!\5c\00\00\00\08\02\91\08\ea\00\00\00\01!&\00\00\00\00\07\e9\00\00\00\83\00\00\00\04\ed\00\01\9f\f8\00\00\00\01*&\00\00\00\08\02\91\08\c0\00\00\00\01*\db\01\00\00\00\0am\01\00\00e\00\00\00\04\ed\00\01\9f\11\01\00\00\017\08\02\91\0c\ea\00\00\00\017&\00\00\00\00\0b\d4\01\00\00\07\02\00\00\04\ed\00\00\9f\04\00\00\00\01C\d4\01\00\00\09\02\91,T\01\00\00\01E\db\01\00\00\09\02\91(\c0\00\00\00\01E\db\01\00\00\09\02\91$\cf\00\00\00\01E\db\01\00\00\09\02\91 \c6\00\00\00\01E\db\01\00\00\09\02\91\1c\d8\00\00\00\01E\db\01\00\00\09\02\91\18\05\01\00\00\01F&\00\00\00\09\02\91\14\1c\01\00\00\01F&\00\00\00\09\02\91\10\ef\00\00\00\01F&\00\00\00\0c\c2\02\00\00\c1\00\00\00\09\02\91\0c\be\00\00\00\01\5c\5c\00\00\00\09\02\91\08#\00\00\00\01\5c\5c\00\00\00\09\02\91\04\ae\00\00\00\01\5c\5c\00\00\00\00\00\06\14\00\00\00\05\04\06\0b\00\00\00\07\04\00")
  (@custom ".debug_ranges" "\05\00\00\00n\00\00\00o\00\00\00\e7\00\00\00\e9\00\00\00l\01\00\00m\01\00\00\d2\01\00\00\d4\01\00\00\db\03\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "new\00_start\00unsigned int\00right\00left\00iterations\00tn\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00check\00ItemCheck\00i\00depth\00maxDepth\00minDepth\00stretchDepth\00long\00tree\00tempTree\00BottomUpTree\00stretchTree\00DeleteTree\00longLivedTree\00treeNode\00NewTreeNode\00../src/binarytrees.c\00N\00clang version 21.1.7\00")
  (@custom ".debug_line" "z\02\00\00\04\00,\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00\00binarytrees.c\00\01\00\00\00\00\05\02\05\00\00\00\03\14\01\05\16\0a\02$\15\05\09\06\9e\05\11\06>\05\05\06t\05\0fX\05\12\06Y\05\05\06t\05\10X\05\0c\06Z\05\05\06t\02\0f\00\01\01\00\05\02o\00\00\00\03!\01\05\09\0a\08\bb\05\0f\06\90\05\14<\05\09\06\91\06\03\5c\90\05\1e\06\03& \05$\06t\05\14<\05\12f\056<\05<X\05,<\05*f\05\09 \03Z<\05\01\06\03' \02\16\00\01\01\00\05\02\e9\00\00\00\03*\01\05\09\0a\08\bb\05\0f\06\90\05\1a\06\93\05 \06t\05\0d<\05\1a\06g\05 \06X\05\0d<\05\10\06c\05\09\06f\03SX\05\10\06\033 \05\09\06\f2\03M<\05\01\06\034 \02\16\00\01\01\00\05\02m\01\00\00\037\01\05\09\0a\08\bb\05\0f\06t\05\14<\06\92\05\1a\06X\05\09<\05\14\06g\05\1a\06X\05\09<\03Df\05\0a\06\03? \05\05\06X\05\01\06g\02\0d\00\01\01\00\05\02\d4\01\00\00\03\c3\00\01\05\09\0a\08\cc\05\07\06\82\05\0e\06>\05\0av\05\13\06\90\05\1a<\05\18X\05\14\06u\05\1d\06t\05\12<\05\09<\03\b3\7f.\05\14\06\03\cf\00 \05\12\06t\03\b1\7f<\05\14\06\03\d1\00 \05\1d\06\90\05\12 \05 \06>\05\13\06t\05\11f\05\16\06=\05\0c\06X\054\82\05.\08.\05'\82\05#.\05\05J\05\10\06h\05\05\06X\05\22\06h\05\15\06t\05\13f\05\12\06>\05\10\06t\05\1c<\05%\90\05\22X\05\05 \05\1d\06j\05(\06X\05&X\050 \05.X\05\1d \05\16\d6\05\14\9e\05\0f\06>\05\10v\05\15\06t\05\1a\90\05\17X\05\09 \05%\06h\05\18\06t\05\16f\05 \06=\05\16\06t\05\13f\05\18\06\91\05\0d\06X\05'\06b\05\09\06\c8.\05\1c\066\05\16\06\08\12\05\10\82\05%.\05#X\053 \050X\05\09J\055\06\03pf\05\05\06\c8.\05\16\06\03\13.\05\0c\06X\054\82\05.\08.\05(\82\05%.\05\05J\06h\02\13\00\01\01")
  (@custom "name" "\00\11\10binarytrees.wasm\01\80\01\0b\00\06malloc\01\04free\02\15__VERIFIER_nondet_int\03\03pow\04\06assert\05\11__wasm_call_ctors\06\0bNewTreeNode\07\09ItemCheck\08\0cBottomUpTree\09\0aDeleteTree\0a\06_start\07\12\01\00\0f__stack_pointer")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
