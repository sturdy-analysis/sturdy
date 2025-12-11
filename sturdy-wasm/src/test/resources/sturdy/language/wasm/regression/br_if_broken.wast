(module
    (func (export "main") (param $0 i32) (result i32)
      (local $1 i32)
      (block
        (local.set $1 (i32.const 1))
        (br_if 0 (local.get $0))
        (local.set $1 (i32.const 0))
      )
      (local.get $1)
    )
)