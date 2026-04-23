(module $reverse-complement.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func (param i32 i32)))
  (type $t5 (func (result i32)))
  (import "env" "toupper" (func $toupper (type $t0)))
  (import "env" "tolower" (func $tolower (type $t0)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "fileno" (func $fileno (type $t0)))
  (import "env" "read" (func $read (type $t1)))
  (import "env" "realloc" (func $realloc (type $t2)))
  (import "env" "write" (func $write (type $t1)))
  (import "env" "free" (func $free (type $t3)))
  (func $process (type $t4) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l2
      (i32.sub
        (local.get $p1)
        (local.get $p0)))
    (local.set $l3
      (i32.const 0))
    (loop $L0
      (local.set $l2
        (i32.add
          (local.get $l2)
          (i32.const -1)))
      (local.set $l4
        (i32.add
          (local.get $p0)
          (local.get $l3)))
      (local.set $l3
        (local.tee $l5
          (i32.add
            (local.get $l3)
            (i32.const 1))))
      (br_if $L0
        (i32.ne
          (i32.load8_u
            (local.get $l4))
          (i32.const 10))))
    (local.set $l3
      (i32.add
        (local.get $p0)
        (local.get $l5)))
    (block $B1
      (br_if $B1
        (i32.eq
          (i32.add
            (local.tee $l2
              (i32.sub
                (i32.sub
                  (local.get $p1)
                  (local.get $p0))
                (local.tee $l4
                  (i32.mul
                    (i32.div_u
                      (local.get $l2)
                      (i32.const 61))
                    (i32.const 61)))))
            (i32.const -60))
          (local.get $l5)))
      (br_if $B1
        (i32.ge_u
          (local.tee $l2
            (i32.add
              (local.get $l3)
              (i32.sub
                (local.get $l2)
                (local.get $l5))))
          (local.get $p1)))
      (local.set $l4
        (i32.add
          (i32.add
            (i32.add
              (i32.sub
                (local.get $p0)
                (local.get $p1))
              (local.get $l4))
            (local.get $l5))
          (i32.const 60)))
      (loop $L2
        (block $B3
          (br_if $B3
            (i32.eqz
              (local.get $l4)))
          (memory.copy
            (i32.add
              (local.get $l2)
              (i32.const 1))
            (local.get $l2)
            (local.get $l4)))
        (i32.store8
          (local.get $l2)
          (i32.const 10))
        (br_if $L2
          (i32.lt_u
            (local.tee $l2
              (i32.add
                (local.get $l2)
                (i32.const 61)))
            (local.get $p1)))))
    (block $B4
      (br_if $B4
        (i32.gt_u
          (local.get $l3)
          (local.tee $l2
            (i32.add
              (local.get $p1)
              (i32.const -1)))))
      (loop $L5
        (local.set $l4
          (i32.load8_u
            (i32.add
              (i32.load8_s
                (local.get $l3))
              (i32.const 1072))))
        (i32.store8
          (local.get $l3)
          (i32.load8_u
            (i32.add
              (i32.load8_s
                (local.get $l2))
              (i32.const 1072))))
        (i32.store8
          (local.get $l2)
          (local.get $l4))
        (br_if $L5
          (i32.le_u
            (local.tee $l3
              (i32.add
                (local.get $l3)
                (i32.const 1)))
            (local.tee $l2
              (i32.add
                (local.get $l2)
                (i32.const -1))))))))
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l0
      (i32.const 0))
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.tee $l1
            (i32.load8_u offset=1024
              (i32.const 0)))))
      (local.set $l2
        (i32.const 1025))
      (loop $L1
        (i32.store8
          (i32.add
            (call $toupper
              (local.tee $l1
                (i32.extend8_s
                  (local.get $l1))))
            (i32.const 1072))
          (local.tee $l3
            (i32.load8_u
              (local.get $l2))))
        (i32.store8
          (i32.add
            (call $tolower
              (local.get $l1))
            (i32.const 1072))
          (local.get $l3))
        (local.set $l1
          (i32.add
            (local.get $l2)
            (i32.const 1)))
        (local.set $l2
          (i32.add
            (local.get $l2)
            (i32.const 2)))
        (br_if $L1
          (local.tee $l1
            (i32.load8_u
              (local.get $l1))))))
    (local.set $l2
      (i32.const 8192))
    (local.set $l4
      (call $malloc
        (i32.const 8192)))
    (local.set $l1
      (i32.const 7936))
    (block $B2
      (block $B3
        (br_if $B3
          (local.tee $l3
            (call $read
              (local.tee $l5
                (call $fileno
                  (i32.load
                    (i32.const 0))))
              (local.get $l4)
              (i32.const 7936))))
        (local.set $l6
          (local.get $l4))
        (br $B2))
      (local.set $l0
        (i32.const 0))
      (loop $L4
        (block $B5
          (br_if $B5
            (i32.lt_u
              (local.tee $l0
                (i32.add
                  (local.get $l3)
                  (local.get $l0)))
              (local.get $l1)))
          (local.set $l4
            (call $realloc
              (local.get $l4)
              (local.tee $l2
                (select
                  (i32.add
                    (local.get $l2)
                    (i32.const 1048576))
                  (i32.shl
                    (local.get $l2)
                    (i32.const 1))
                  (i32.gt_u
                    (local.get $l2)
                    (i32.const 1048575)))))))
        (br_if $L4
          (local.tee $l3
            (call $read
              (local.get $l5)
              (local.tee $l6
                (i32.add
                  (local.get $l4)
                  (local.get $l0)))
              (i32.sub
                (local.tee $l1
                  (i32.add
                    (local.get $l2)
                    (i32.const -256)))
                (local.get $l0)))))))
    (i32.store8
      (local.get $l6)
      (i32.const 62))
    (local.set $l6
      (i32.add
        (local.get $l6)
        (i32.const -1)))
    (loop $L6
      (local.set $l2
        (local.get $l6))
      (loop $L7
        (local.set $l1
          (i32.load8_u
            (local.get $l2)))
        (local.set $l2
          (local.tee $l3
            (i32.add
              (local.get $l2)
              (i32.const -1))))
        (br_if $L7
          (i32.ne
            (local.get $l1)
            (i32.const 62))))
      (call $process
        (i32.add
          (local.get $l3)
          (i32.const 1))
        (local.get $l6))
      (local.set $l6
        (local.get $l3))
      (br_if $L6
        (i32.ge_u
          (local.get $l3)
          (local.get $l4))))
    (drop
      (call $write
        (call $fileno
          (i32.load
            (i32.const 0)))
        (local.get $l4)
        (local.get $l0)))
    (call $free
      (local.get $l4))
    (i32.const 0))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66736))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.data (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a\00")
  (@custom "name" "\00\18\17reverse-complement.wasm\01P\0a\00\07toupper\01\07tolower\02\06malloc\03\06fileno\04\04read\05\07realloc\06\05write\07\04free\08\07process\09\06_start\07\12\01\00\0f__stack_pointer\09\08\01\00\05.data")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
