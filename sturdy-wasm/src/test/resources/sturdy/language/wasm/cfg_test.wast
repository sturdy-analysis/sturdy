(module

  (func (export "test1") (param i32)
    local.get 0
    i32.const 0
    i32.eq
    (if
      (then
        i32.const 1
        drop
        br 0
        i32.const 1
        i32.const 2
        drop
        drop)
      (else
        br 0)
    )
    i32.const 1
    drop
  )

  (func $fac-rec (export "fac-rec") (param i64) (result i64)
    local.get 0
    i64.const 0
    i64.eq
    (if (result i64)
      (then
        i64.const 1)
      (else
        local.get 0
        local.get 0
        i64.const 1
        i64.sub
        call $fac-rec
        i64.mul)))

  (func (export "fac-iter") (param i64) (result i64) (local i64 i64)
    local.get 0
    local.set 1
    i64.const 1
    local.set 2
    (block
      (loop
        local.get 1
        i64.const 0
        i64.eq
        (if
          (then
            br 2)
          (else
            local.get 1
            local.get 2
            i64.mul
            local.set 2
            local.get 1
            i64.const 1
            i64.sub
            local.set 1
          )
        )
        br 0
      )
    )
    local.get 2
  )
)
