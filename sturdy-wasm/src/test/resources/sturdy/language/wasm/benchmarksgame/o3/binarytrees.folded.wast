(module $binarytrees.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (result i32)))
  (type $t3 (func (param f64) (result f64)))
  (type $t4 (func (param i32 i32) (result i32)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "free" (func $free (type $t1)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t2)))
  (import "env" "exp2" (func $exp2 (type $t3)))
  (import "env" "assert" (func $assert (type $t1)))
  (func $NewTreeNode (type $t4) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32)
    (i32.store offset=4
      (local.tee $l2
        (call $malloc
          (i32.const 8)))
      (local.get $p1))
    (i32.store
      (local.get $l2)
      (local.get $p0))
    (local.get $l2))
  (func $ItemCheck (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    (block $B0
      (br_if $B0
        (local.tee $l1
          (i32.load
            (local.get $p0))))
      (return
        (i32.const 1)))
    (local.set $l2
      (i32.const 0))
    (loop $L1
      (local.set $l2
        (i32.add
          (i32.add
            (local.get $l2)
            (call $ItemCheck
              (local.get $l1)))
          (i32.const 1)))
      (br_if $L1
        (local.tee $l1
          (i32.load
            (local.tee $p0
              (i32.load offset=4
                (local.get $p0)))))))
    (i32.add
      (local.get $l2)
      (i32.const 1)))
  (func $BottomUpTree (type $t0) (param $p0 i32) (result i32)
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.get $p0)))
      (return
        (call $NewTreeNode
          (call $BottomUpTree
            (local.tee $p0
              (i32.add
                (local.get $p0)
                (i32.const -1))))
          (call $BottomUpTree
            (local.get $p0)))))
    (call $NewTreeNode
      (i32.const 0)
      (i32.const 0)))
  (func $DeleteTree (type $t1) (param $p0 i32)
    (local $l1 i32)
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.tee $l1
            (i32.load
              (local.get $p0)))))
      (call $DeleteTree
        (local.get $l1))
      (call $DeleteTree
        (i32.load offset=4
          (local.get $p0))))
    (call $free
      (local.get $p0)))
  (func $_start (type $t2) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 f64) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l2
      (call $exp2
        (f64.convert_i32_u
          (local.tee $l0
            (i32.add
              (local.tee $l1
                (select
                  (local.tee $l0
                    (call $__VERIFIER_nondet_int))
                  (i32.const 6)
                  (i32.gt_u
                    (local.get $l0)
                    (i32.const 6))))
              (i32.const 1))))))
    (call $assert
      (i32.eq
        (call $ItemCheck
          (local.tee $l0
            (call $BottomUpTree
              (local.get $l0))))
        (i32.trunc_sat_f64_s
          (local.get $l2))))
    (call $DeleteTree
      (local.get $l0))
    (local.set $l3
      (i32.const 4))
    (local.set $l4
      (i32.add
        (local.get $l1)
        (i32.const 4)))
    (local.set $l5
      (call $BottomUpTree
        (local.get $l1)))
    (loop $L0
      (local.set $l0
        (i32.const 0))
      (block $B1
        (br_if $B1
          (i32.lt_s
            (local.tee $l6
              (i32.trunc_sat_f64_s
                (call $exp2
                  (f64.convert_i32_u
                    (i32.sub
                      (local.get $l4)
                      (local.get $l3))))))
            (i32.const 1)))
        (local.set $l7
          (local.get $l6))
        (loop $L2
          (local.set $l9
            (call $ItemCheck
              (local.tee $l8
                (call $BottomUpTree
                  (local.get $l3)))))
          (call $DeleteTree
            (local.get $l8))
          (local.set $l0
            (i32.add
              (local.get $l9)
              (local.get $l0)))
          (br_if $L2
            (local.tee $l7
              (i32.add
                (local.get $l7)
                (i32.const -1))))))
      (call $assert
        (i32.eq
          (i32.mul
            (local.get $l6)
            (i32.trunc_sat_f64_s
              (call $exp2
                (f64.convert_i32_u
                  (local.get $l3)))))
          (local.get $l0)))
      (br_if $L0
        (i32.le_u
          (local.tee $l3
            (i32.add
              (local.get $l3)
              (i32.const 2)))
          (local.get $l1))))
    (local.set $l2
      (call $exp2
        (f64.convert_i32_u
          (local.get $l1))))
    (call $assert
      (i32.eq
        (call $ItemCheck
          (local.get $l5))
        (i32.trunc_sat_f64_s
          (local.get $l2))))
    (i32.const 0))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66560))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (@custom "name" "\00\11\10binarytrees.wasm\01n\0a\00\06malloc\01\04free\02\15__VERIFIER_nondet_int\03\04exp2\04\06assert\05\0bNewTreeNode\06\09ItemCheck\07\0cBottomUpTree\08\0aDeleteTree\09\06_start\07\12\01\00\0f__stack_pointer")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
