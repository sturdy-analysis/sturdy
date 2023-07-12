(module
  (memory 1)

  (table funcref
    (elem
      $noop $const
      $noop2 $const
    )
  )

  (global (mut i32) (i32.const 0))

  (type $out-i32 (func (result i32)))

  (func $const (export "const") (param i32) (result i32)
    (local.get 0)
  )

  (func $first (export "first") (param i32 i32) (result i32)
    (local.get 0)
  )

  (func $second (export "second") (param i32 i32) (result i32)
    (local.get 1)
  )

  (func $third (export "third") (param i32 i32 i32) (result i32 i32)
    (local.get 0)(local.get 1)
  )

  (func (export "call-first") (result i32)
    i32.const 0
    i32.const 1
    call $first
  )

  (func $noop (export "noop") (result i32)
    (i32.const 0)
  )

  (func $noop2 (export "noop2") (result i32 i32)
     (i32.const 0)(i32.const 1)
  )

  (func $test1 (export "test1") (result i32)
    (i32.const 1)
    (call $noop)
    (i32.add)
  )

  (func (export "test2") (result i32)
    (i32.const 2)
    (call $test1)
    (i32.add)
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

  (func (export "half-fac") (param i32) (result i32)
    (if (result i32) (i32.eq (local.get 0) (i32.const 0))
      (then (i32.const 1))
      (else (i32.const 0))))

  (func (export "half-fac-64") (param i64) (result i64)
    (if (result i64) (i64.eq (local.get 0) (i64.const 0))
      (then (i64.const 1))
      (else (i64.const 0))))

  (func (export "non-terminating") (result i32)
    (loop (br 0))
    i32.const 0
  )

  (func (export "maybe-non-terminating") (param i32) (result i32)
    (block
      (loop
        (br_if 1 (i32.eq (local.get 0) (i32.const 42)))
        (br 0)
      )
    )
    i32.const 0
  )

  (func (export "test-mem0") (param i32) (result i32)
    i32.const 0
    local.get 0
    i32.store
    i32.const 0
    i32.load
    )

  (func (export "test-mem") (param i32) (result i32)
    i32.const 0
    local.get 0
    i32.store
    i32.const 0
    i32.load
    i32.const 1
    i32.add
    )

  (func (export "test-mem2") (result i32)
    i32.const 0
    i32.load
  )

  (func (export "test-size") (result i32)
    memory.size
  )

  (func (export "test-memgrow") (result i32 i32)
    i32.const 1
    memory.grow
    memory.size
  )

  (func (export "test-br1") (result i32)
    (block (result i32)
           i32.const 42
           br 0)
  )

  (func (export "test-br2") (result i32)
    (block (result i32)
      (block (result i32)
        i32.const 42
        br 1))
    i32.const 1
    i32.add
  )

  (func (export "test-br3") (param i32) (result i32)
    (block (result i32)
      (block (result i32)
        (if (result i32) (i32.eq (local.get 0) (i32.const 0))
          (then
            i32.const 42
            br 0
          )
          (else
            i32.const 43
            br 1
          )
        )
      )
    )
  )

  (func (export "test-br-and-return") (param i32) (result i32)
    (block (result i32)
      (block (result i32)
        (if (result i32) (i32.eq (local.get 0) (i32.const 0))
          (then
            i32.const 42
            return
          )
          (else
            i32.const 43
            br 1
          )
        )
      )
    )
  )

  (func (export "test-br-and-return4") (param i32) (result i32)
      (block (result i32)
        (block (result i32)
          (if (result i32) (i32.eq (local.get 0) (i32.const 0))
            (then
              i32.const 42
              return
            )
            (else
              i32.const 42
              br 1
            )
          )
        )
      )
    )

  (func (export "test-unreachable") (result i32)
    i32.const 42
    return
    unreachable
  )

  (func (export "test-unreachable2") (result i32)
    (block (result i32)
      i32.const 42
      return
    )
    unreachable
  )

  (func (export "test-unreachable3") (result i32)
    (block (result i32)
      i32.const 42
      br 1
    )
    unreachable
  )

  (func (export "test-unreachable4") (result i32)
    (block (result i32)
      i32.const 42
      br 0
    )
    unreachable
  )

  (func (export "test-unreachable5") (param i32) (result i32)
    (if (result i32) (i32.eq (local.get 0) (i32.const 0))
      (then
        i32.const 42
        br 1
      )
      (else
        i32.const 43
        br 1
      )
    )
    unreachable
  )

  (func (export "test-br-and-return3") (param i32) (result i32)
      (block (result i32)
        (if (result i32) (i32.eq (local.get 0) (i32.const 0))
          (then
            i32.const 42
            br 1
          )
          (else
            i32.const 43
            br 1
          )
        )
        unreachable
      )
    )

  (func (export "test-br-and-return2") (param i32) (result i32)
    (block (result i32)
      (block (result i32)
        (if (result i32) (i32.eq (local.get 0) (i32.const 0))
          (then
            i32.const 42
            return
          )
          (else
            i32.const 43
            br 2
          )
        )
      )
      unreachable
    )
  )

  (func (export "test-call-indirect") (result i32)
    (call_indirect (type $out-i32) (i32.const 0))
  )

  (func (export "test-call-indirect-parametric") (param i32) (result i32)
    (call_indirect (type $out-i32) (local.get 0))
  )

  (func (export "break-multi-value") (result i32 i32 i64)
    (block (result i32 i32 i64)
      (br 0 (i32.const 18) (i32.const -18) (i64.const 18))
      (i32.const 19) (i32.const -19) (i64.const 19)
    )
  )

  (func (export "nesting") (param f32 f32) (result f32)
    (local f32 f32)
    (block
      (loop
        (br_if 1 (f32.eq (local.get 0) (f32.const 0)))
        (local.set 2 (local.get 1))
        (block
          (loop
            (br_if 1 (f32.eq (local.get 2) (f32.const 0)))
            (br_if 3 (f32.lt (local.get 2) (f32.const 0)))
            (local.set 3 (f32.add (local.get 3) (local.get 2)))
            (local.set 2 (f32.sub (local.get 2) (f32.const 2)))
            (br 0)
          )
        )
        (local.set 3 (f32.div (local.get 3) (local.get 0)))
        (local.set 0 (f32.sub (local.get 0) (f32.const 1)))
        (br 0)
      )
    )
    (local.get 3)
  )

  (func (export "as-br_table-index")
    (block (br_table 0 0 0 (br_table 0 (i32.const 1))))
  )
  (func (export "as-br_if-cond") (param i32)
    (block (br_if 0 (local.tee 0 (i32.const 1))))
  )

  ;;(func (f32.const nan:0x1) drop)
  (func (export "test-global") (param i32) (result i32)
    (if (i32.eq (local.get 0) (i32.const 0))
      (then
        i32.const 1
        global.set 0)
      (else
        i32.const 2
        global.set 0)
    )
    global.get 0
  )

  (func (export "division") (param i32 i32) (result i32)
    local.get 0
    local.get 1
    i32.div_s
  )

  (func (export "effects") (param i32) (result i32)
      (local i32)
      (if
        (block (result i32) (local.set 1 (i32.const 1)) (local.get 0))
        (then
          (local.set 1 (i32.mul (local.get 1) (i32.const 3)))
          (local.set 1 (i32.sub (local.get 1) (i32.const 5)))
          (local.set 1 (i32.mul (local.get 1) (i32.const 7)))
          (br 0)
          (local.set 1 (i32.mul (local.get 1) (i32.const 100)))
        )
        (else
          (local.set 1 (i32.mul (local.get 1) (i32.const 5)))
          (local.set 1 (i32.sub (local.get 1) (i32.const 7)))
          (local.set 1 (i32.mul (local.get 1) (i32.const 3)))
          (br 0)
          (local.set 1 (i32.mul (local.get 1) (i32.const 1000)))
        )
      )
      (local.get 1)
    )

)
