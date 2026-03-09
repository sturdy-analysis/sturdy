(module

    (func $main (export "main") (param i32) (result i32)
        i32.const 0
        call $f ;; with 0
        drop
        local.get 0
        call $f ;; with Top


    )

    (func $f (param i32) (result i32)
        local.get 0
        (if
            (then
                i32.const -1 ;; not taken on the first call, but taken on the second
                br 1
            )
        )
        i32.const 5
        local.set 0
        (loop
            local.get 0
            i32.const 1
            i32.sub
            local.tee 0
            br_if 0
        )
        i32.const 1
    )
)
