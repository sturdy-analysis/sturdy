;; Test tag section


;;; Foobar
(module
 (tag)
 (tag (param i32))
 (tag (param i32 i32))
 (tag (param i32 i32 i32))
 (tag (export "foobartag") (param i32 i32 i32 i32)))

(register "foobar")


;;; Test
(module
 (import "foobar" "foobartag" (tag (param i32 i32 i32 i32)))

 (func (param i32 i32 i32))
 (func (export "func-i32") (param i32))
 (tag)
 (tag (param i32))
 (tag (export "t2") (param i32))
 (tag $t3 (param i32 f32))
 (export "t3" (tag 3))
 )

(register "test")


;;; Whatever
(module
 (import "test" "func-i32" (func (param i32)))	
 (tag $t0 (import "test" "t2") (param i32))
 (import "test" "t3" (tag $t1 (param i32 f32)))
 )

(assert_invalid
  (module (tag (result i32)))
  "non-empty tag result type"
)
(assert_invalid
  (module (import "" "" (tag (result i32))))
  "non-empty tag result type"
)



