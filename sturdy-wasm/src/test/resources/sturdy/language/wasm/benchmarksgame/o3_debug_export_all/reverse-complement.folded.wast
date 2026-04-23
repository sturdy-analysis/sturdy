(module
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32)))
  (type $t6 (func (result i32)))
  (import "env" "memmove" (func $env.memmove (type $t1)))
  (import "env" "toupper" (func $env.toupper (type $t0)))
  (import "env" "tolower" (func $env.tolower (type $t0)))
  (import "env" "malloc" (func $env.malloc (type $t0)))
  (import "env" "fileno" (func $env.fileno (type $t0)))
  (import "env" "read" (func $env.read (type $t1)))
  (import "env" "realloc" (func $env.realloc (type $t2)))
  (import "env" "write" (func $env.write (type $t1)))
  (import "env" "free" (func $env.free (type $t3)))
  (func $__wasm_call_ctors (export "__wasm_call_ctors") (type $t4))
  (func $process (export "process") (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l4
      (i32.sub
        (local.get $p1)
        (local.get $p0)))
    (loop $L0
      (local.set $l4
        (i32.sub
          (local.get $l4)
          (i32.const 1)))
      (i32.add
        (local.get $p0)
        (local.get $l2))
      (local.set $l2
        (local.tee $l5
          (i32.add
            (local.get $l2)
            (i32.const 1))))
      (br_if $L0
        (i32.ne
          (i32.load8_u)
          (i32.const 10))))
    (local.set $l3
      (i32.add
        (local.get $p0)
        (local.get $l5)))
    (block $B1
      (br_if $B1
        (i32.eq
          (i32.sub
            (local.tee $l2
              (i32.sub
                (i32.sub
                  (local.get $p1)
                  (local.get $p0))
                (local.tee $l4
                  (i32.mul
                    (i32.div_u
                      (local.get $l4)
                      (i32.const 61))
                    (i32.const 61)))))
            (i32.const 60))
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
      (local.set $p0
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
        (drop
          (call $env.memmove
            (i32.add
              (local.get $l2)
              (i32.const 1))
            (local.get $l2)
            (local.get $p0)))
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
    (if $I3
      (i32.ge_u
        (local.tee $l2
          (i32.sub
            (local.get $p1)
            (i32.const 1)))
        (local.get $l3))
      (then
        (loop $L4
          (local.set $p0
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
            (local.get $p0))
          (br_if $L4
            (i32.le_u
              (local.tee $l3
                (i32.add
                  (local.get $l3)
                  (i32.const 1)))
              (local.tee $l2
                (i32.sub
                  (local.get $l2)
                  (i32.const 1)))))))))
  (func $_start (export "_start") (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (if $I0
      (local.tee $l3
        (i32.load8_u
          (i32.const 1024)))
      (then
        (local.set $l0
          (i32.const 1025))
        (loop $L1
          (i32.store8
            (i32.add
              (call $env.toupper
                (local.tee $l1
                  (i32.extend8_s
                    (local.get $l3))))
              (i32.const 1072))
            (local.tee $l2
              (i32.load8_u
                (local.get $l0))))
          (i32.store8
            (i32.add
              (call $env.tolower
                (local.get $l1))
              (i32.const 1072))
            (local.get $l2))
          (i32.add
            (local.get $l0)
            (i32.const 1))
          (local.set $l0
            (i32.add
              (local.get $l0)
              (i32.const 2)))
          (br_if $L1
            (local.tee $l3
              (i32.load8_u))))))
    (local.set $l0
      (i32.const 8192))
    (local.set $l2
      (call $env.malloc
        (i32.const 8192)))
    (local.set $l3
      (i32.const 7936))
    (block $B2
      (if $I3
        (i32.eqz
          (local.tee $l5
            (call $env.read
              (local.tee $l6
                (call $env.fileno
                  (i32.load
                    (i32.const 0))))
              (local.get $l2)
              (i32.const 7936))))
        (then
          (local.set $l1
            (local.get $l2))
          (br $B2)))
      (loop $L4
        (if $I5
          (i32.le_u
            (local.get $l3)
            (local.tee $l4
              (i32.add
                (local.get $l4)
                (local.get $l5))))
          (then
            (local.set $l2
              (call $env.realloc
                (local.get $l2)
                (local.tee $l0
                  (select
                    (i32.sub
                      (local.get $l0)
                      (i32.const -1048576))
                    (i32.shl
                      (local.get $l0)
                      (i32.const 1))
                    (i32.gt_u
                      (local.get $l0)
                      (i32.const 1048575))))))))
        (br_if $L4
          (local.tee $l5
            (call $env.read
              (local.get $l6)
              (local.tee $l1
                (i32.add
                  (local.get $l2)
                  (local.get $l4)))
              (i32.sub
                (local.tee $l3
                  (i32.sub
                    (local.get $l0)
                    (i32.const 256)))
                (local.get $l4)))))))
    (i32.store8
      (local.get $l1)
      (i32.const 62))
    (local.set $l1
      (i32.sub
        (local.get $l1)
        (i32.const 1)))
    (loop $L6
      (local.set $l0
        (local.get $l1))
      (loop $L7
        (i32.load8_u
          (local.get $l0))
        (local.set $l0
          (i32.sub
            (local.get $l0)
            (i32.const 1)))
        (i32.const 62)
        (br_if $L7
          (i32.ne)))
      (call $process
        (i32.add
          (local.get $l0)
          (i32.const 1))
        (local.get $l1))
      (br_if $L6
        (i32.ge_u
          (local.tee $l1
            (local.get $l0))
          (local.get $l2))))
    (drop
      (call $env.write
        (call $env.fileno
          (i32.load
            (i32.const 0)))
        (local.get $l2)
        (local.get $l4)))
    (call $env.free
      (local.get $l2))
    (i32.const 0))
  (memory $memory (export "memory") 2)
  (global $tbl (export "tbl") i32 (i32.const 1072))
  (global $pairs (export "pairs") i32 (i32.const 1024))
  (global $__dso_handle (export "__dso_handle") i32 (i32.const 1024))
  (global $__data_end (export "__data_end") i32 (i32.const 1200))
  (global $__stack_low (export "__stack_low") i32 (i32.const 1200))
  (global $__stack_high (export "__stack_high") i32 (i32.const 66736))
  (global $__global_base (export "__global_base") i32 (i32.const 1024))
  (global $__heap_base (export "__heap_base") i32 (i32.const 66736))
  (global $__heap_end (export "__heap_end") i32 (i32.const 131072))
  (global $__memory_base (export "__memory_base") i32 (i32.const 0))
  (global $__table_base (export "__table_base") i32 (i32.const 1))
  (data $d0 (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a"))
