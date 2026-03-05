(module
  (func)
  (func (param i32))
  (func (export "f") (param i32 i32)
	local.get 0
	local.get 1
	i32.add
	drop))

(register "funcexport")

(module
 (import "funcexport" "f" (func $f (param i32 i32)))
 (func)
 (func (param i32 i32 i32))
 (func (export "call_f")
       i32.const 40
       i32.const 2
       call $f))

(register "funcimport")
