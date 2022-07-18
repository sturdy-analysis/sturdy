
(module
    (func (export "rem_s") (param i32 i32) (result i32)
        local.get 0
        local.get 1
        i32.rem_s
    )

    (func (export "or") (param i32 i32) (result i32)
            local.get 0
            local.get 1
            i32.or
    )

    (func (export "and") (param i32 i32) (result i32)
                local.get 0
                local.get 1
                i32.and
    )

    (func (export "add") (param i32 i32) (result i32)
                    local.get 0
                    local.get 1
                    i32.add
    )

    (func (export "sub") (param i32 i32) (result i32)
                        local.get 0
                        local.get 1
                        i32.sub
    )

    (func (export "xor") (param i32 i32) (result i32)
                            local.get 0
                            local.get 1
                            i32.xor
    )

    (func (export "div_s") (param i32 i32) (result i32)
                                local.get 0
                                local.get 1
                                i32.div_s
    )

    (func (export "div_u") (param i32 i32) (result i32)
                                    local.get 0
                                    local.get 1
                                    i32.div_u
    )

    (func (export "clz") (param i32) (result i32)
        local.get 0
        i32.clz
    )

    (func (export "ctz") (param i32) (result i32)
            local.get 0
            i32.ctz
    )

    (func (export "popcnt") (param i32) (result i32)
                local.get 0
                i32.popcnt
    )

    (func (export "shr_s") (param i32 i32) (result i32)
        local.get 0
        local.get 1
        i32.shr_s
    )

    (func (export "shr_u") (param i32 i32) (result i32)
        local.get 0
        local.get 1
        i32.shr_u
    )

    (func (export "shl") (param i32 i32) (result i32)
        local.get 0
        local.get 1
        i32.shl
    )
)
