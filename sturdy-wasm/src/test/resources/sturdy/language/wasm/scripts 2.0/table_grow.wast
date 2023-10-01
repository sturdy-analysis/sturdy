(module
  (table $t 0 externref)

  (func (export "get") (param $i i32) (result externref) (table.get $t (local.get $i)))
  (func (export "set") (param $i i32) (param $r externref) (table.set $t (local.get $i) (local.get $r)))

  (func (export "grow") (param $sz i32) (param $init externref) (result i32)
    (table.grow $t (local.get $init) (local.get $sz))
  )
  (func (export "grow-abbrev") (param $sz i32) (param $init externref) (result i32)
    (table.grow $t (local.get $init) (local.get $sz))
  )
  (func (export "size") (result i32) (table.size $t))
)

(assert_return (invoke "size") (i32.const 0))
(assert_trap (invoke "set" (i32.const 0) (ref.extern 2)) "out of bounds table access")
(assert_trap (invoke "get" (i32.const 0)) "out of bounds table access")

(assert_return (invoke "grow" (i32.const 1) (ref.null extern)) (i32.const 0))
(assert_return (invoke "size") (i32.const 1))
(assert_return (invoke "get" (i32.const 0)) (ref.null extern))
(assert_return (invoke "set" (i32.const 0) (ref.extern 2)))
(assert_return (invoke "get" (i32.const 0)) (ref.extern 2))
(assert_trap (invoke "set" (i32.const 1) (ref.extern 2)) "out of bounds table access")
(assert_trap (invoke "get" (i32.const 1)) "out of bounds table access")

(assert_return (invoke "grow-abbrev" (i32.const 4) (ref.extern 3)) (i32.const 1))
(assert_return (invoke "size") (i32.const 5))
(assert_return (invoke "get" (i32.const 0)) (ref.extern 2))
(assert_return (invoke "set" (i32.const 0) (ref.extern 2)))
(assert_return (invoke "get" (i32.const 0)) (ref.extern 2))
(assert_return (invoke "get" (i32.const 1)) (ref.extern 3))
(assert_return (invoke "get" (i32.const 4)) (ref.extern 3))
(assert_return (invoke "set" (i32.const 4) (ref.extern 4)))
(assert_return (invoke "get" (i32.const 4)) (ref.extern 4))
(assert_trap (invoke "set" (i32.const 5) (ref.extern 2)) "out of bounds table access")
(assert_trap (invoke "get" (i32.const 5)) "out of bounds table access")

;; Reject growing to size outside i32 value range
;;(module
  ;;(table $t 0x10 funcref)
  ;;(elem declare func $f)
  ;;(func $f (export "grow") (result i32)
    ;;(table.grow $t (ref.func $f) (i32.const 0xffff_fff0))
  ;;)
;;)

;;(assert_return (invoke "grow") (i32.const -1))

(module
  (table $t1 0 externref)
  (func (export "grow") (param i32) (result i32)
    (table.grow $t1 (ref.null extern) (local.get 0))
  )
)

(assert_return (invoke "grow" (i32.const 0)) (i32.const 5))