(module
  (memory 0)
  (func (export "size1") (result i32) (memory.size))
  (func (export "grow1") (param $sz i32) (drop (memory.grow (local.get $sz))))
)

(assert_return (invoke "size1") (i32.const 0))
(assert_return (invoke "grow1" (i32.const 1)))
(assert_return (invoke "size1") (i32.const 1))
(assert_return (invoke "grow1" (i32.const 4)))
(assert_return (invoke "size1") (i32.const 5))
(assert_return (invoke "grow1" (i32.const 0)))
(assert_return (invoke "size1") (i32.const 5))

(module
  (memory 1)
  (func (export "size2") (result i32) (memory.size))
  (func (export "grow2") (param $sz i32) (drop (memory.grow (local.get $sz))))
)

(assert_return (invoke "size2") (i32.const 1))
(assert_return (invoke "grow2" (i32.const 1)))
(assert_return (invoke "size2") (i32.const 2))
(assert_return (invoke "grow2" (i32.const 4)))
(assert_return (invoke "size2") (i32.const 6))
(assert_return (invoke "grow2" (i32.const 0)))
(assert_return (invoke "size2") (i32.const 6))

(module
  (memory 0 2)
  (func (export "size3") (result i32) (memory.size))
  (func (export "grow3") (param $sz i32) (drop (memory.grow (local.get $sz))))
)

(assert_return (invoke "size3") (i32.const 0))
(assert_return (invoke "grow3" (i32.const 3)))
(assert_return (invoke "size3") (i32.const 0))
(assert_return (invoke "grow3" (i32.const 1)))
(assert_return (invoke "size3") (i32.const 1))
(assert_return (invoke "grow3" (i32.const 0)))
(assert_return (invoke "size3") (i32.const 1))
(assert_return (invoke "grow3" (i32.const 4)))
(assert_return (invoke "size3") (i32.const 1))
(assert_return (invoke "grow3" (i32.const 1)))
(assert_return (invoke "size3") (i32.const 2))

(module
  (memory 3 8)
  (func (export "size4") (result i32) (memory.size))
  (func (export "grow4") (param $sz i32) (drop (memory.grow (local.get $sz))))
)

(assert_return (invoke "size4") (i32.const 3))
(assert_return (invoke "grow4" (i32.const 1)))
(assert_return (invoke "size4") (i32.const 4))
(assert_return (invoke "grow4" (i32.const 3)))
(assert_return (invoke "size4") (i32.const 7))
(assert_return (invoke "grow4" (i32.const 0)))
(assert_return (invoke "size4") (i32.const 7))
(assert_return (invoke "grow4" (i32.const 2)))
(assert_return (invoke "size4") (i32.const 7))
(assert_return (invoke "grow4" (i32.const 1)))
(assert_return (invoke "size4") (i32.const 8))


;; Type errors

(assert_invalid
  (module
    (memory 1)
    (func $type-result-i32-vs-empty
      (memory.size)
    )
  )
  "type mismatch"
)
(assert_invalid
  (module
    (memory 1)
    (func $type-result-i32-vs-f32 (result f32)
      (memory.size)
    )
  )
  "type mismatch"
)
