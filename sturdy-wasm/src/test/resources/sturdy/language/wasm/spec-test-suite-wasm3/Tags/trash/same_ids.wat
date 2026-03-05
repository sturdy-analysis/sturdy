;; Test tag section

;;; Foobar
(module
 (func (export "foofunc") (param i32)))

(register "foobar")


;;; Test
(module
 (import "foobar" "foofunc" (func $importfunc (param i32)))
 (func (param i32 i32))
 (func (export "func-f64") (param f64))
 (export "fexport" (func $importfunc))
 )

(register "tagtest")



;;; When module "tagtest" is instantiated: foofunc and (i32 i32 func) have the same funcIx.
