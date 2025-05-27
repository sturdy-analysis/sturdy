(module
  (export "main" (func $main))
  (func $main (param i32) (result i32)
    i32.const 42
    local.get 0
    i32.const 1
    call $f
    br_if 0
    local.get 0
    i32.const 5
    call $f
    i32.add
  )
  (func $f (param i32 i32) (result i32)
    local.get 0
    if (result i32)
    local.get 1
    else
    i32.const 2
    end
  )

)
