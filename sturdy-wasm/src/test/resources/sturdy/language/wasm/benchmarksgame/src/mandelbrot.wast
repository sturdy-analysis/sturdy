(module $mandelbrot.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32 i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (result i32)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "fprintf" (func $fprintf (type $t1)))
  (import "env" "fwrite" (func $fwrite (type $t2)))
  (import "env" "free" (func $free (type $t3)))
  (func $__wasm_call_ctors (type $t4)
    nop)
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i64)
    global.get $__stack_pointer
    i32.const 8432
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    local.get $l0
    i32.const 512
    i32.store offset=8428
    local.get $l0
    i32.const 32768
    call $malloc
    i32.store offset=8424
    local.get $l0
    local.get $l0
    i32.store offset=8420
    local.get $l0
    i32.const 0
    i32.store offset=8416
    loop $L0
      local.get $l0
      i32.load offset=8416
      i32.const 512
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l0
        i32.const 4112
        i32.add
        local.get $l0
        i32.load offset=8416
        i32.const 3
        i32.shl
        i32.add
        local.get $l0
        i32.load offset=8416
        f64.convert_i32_s
        f64.const 0x1p-8 (;=0.00390625;)
        f64.mul
        f64.const -0x1.8p+0 (;=-1.5;)
        f64.add
        f64.store
        local.get $l0
        i32.const 16
        i32.add
        local.get $l0
        i32.load offset=8416
        i32.const 3
        i32.shl
        i32.add
        local.get $l0
        i32.load offset=8416
        f64.convert_i32_s
        f64.const 0x1p-8 (;=0.00390625;)
        f64.mul
        f64.const -0x1p+0 (;=-1;)
        f64.add
        f64.store
        local.get $l0
        local.get $l0
        i32.load offset=8416
        i32.const 1
        i32.add
        i32.store offset=8416
        br $L0
      end
    end
    local.get $l0
    i32.const 0
    i32.store offset=8412
    loop $L2
      local.get $l0
      i32.load offset=8412
      i32.const 512
      i32.ge_s
      i32.eqz
      if $I3
        local.get $l0
        local.get $l0
        i32.const 16
        i32.add
        local.get $l0
        i32.load offset=8412
        i32.const 3
        i32.shl
        i32.add
        f64.load
        f64.store offset=8400
        local.get $l0
        i32.const 0
        i32.store offset=8396
        loop $L4
          local.get $l0
          i32.load offset=8396
          i32.const 512
          i32.ge_s
          i32.eqz
          if $I5
            local.get $l0
            i32.const 0
            i32.store offset=8252
            loop $L6
              local.get $l0
              i32.load offset=8252
              i32.const 8
              i32.ge_s
              i32.eqz
              if $I7
                local.get $l0
                i32.const 8320
                i32.add
                local.get $l0
                i32.load offset=8252
                i32.const 3
                i32.shl
                i32.add
                local.get $l0
                i32.const 4112
                i32.add
                local.get $l0
                i32.load offset=8396
                local.get $l0
                i32.load offset=8252
                i32.add
                i32.const 3
                i32.shl
                i32.add
                f64.load
                f64.store
                local.get $l0
                i32.const 8256
                i32.add
                local.get $l0
                i32.load offset=8252
                i32.const 3
                i32.shl
                i32.add
                local.get $l0
                f64.load offset=8400
                f64.store
                local.get $l0
                local.get $l0
                i32.load offset=8252
                i32.const 1
                i32.add
                i32.store offset=8252
                br $L6
              end
            end
            local.get $l0
            i32.const 255
            i32.store8 offset=8251
            local.get $l0
            i32.const 5
            i32.store offset=8244
            loop $L8
              local.get $l0
              i32.const 128
              i32.store8 offset=8243
              local.get $l0
              i32.const 0
              i32.store offset=8236
              loop $L9
                local.get $l0
                i32.load offset=8236
                i32.const 8
                i32.lt_s
                if $I10
                  local.get $l0
                  local.get $l0
                  i32.const 8320
                  i32.add
                  local.get $l0
                  i32.load offset=8236
                  i32.const 3
                  i32.shl
                  i32.add
                  f64.load
                  f64.store offset=8224
                  local.get $l0
                  local.get $l0
                  i32.const 8256
                  i32.add
                  local.get $l0
                  i32.load offset=8236
                  i32.const 3
                  i32.shl
                  i32.add
                  f64.load
                  f64.store offset=8216
                  local.get $l0
                  i32.const 8320
                  i32.add
                  local.get $l0
                  i32.load offset=8236
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get $l0
                  f64.load offset=8224
                  local.get $l0
                  f64.load offset=8224
                  f64.mul
                  local.get $l0
                  f64.load offset=8216
                  local.get $l0
                  f64.load offset=8216
                  f64.mul
                  f64.sub
                  local.get $l0
                  i32.const 4112
                  i32.add
                  local.get $l0
                  i32.load offset=8396
                  local.get $l0
                  i32.load offset=8236
                  i32.add
                  i32.const 3
                  i32.shl
                  i32.add
                  f64.load
                  f64.add
                  f64.store
                  local.get $l0
                  i32.const 8256
                  i32.add
                  local.get $l0
                  i32.load offset=8236
                  i32.const 3
                  i32.shl
                  i32.add
                  local.get $l0
                  f64.load offset=8224
                  f64.const 0x1p+1 (;=2;)
                  f64.mul
                  local.get $l0
                  f64.load offset=8216
                  f64.mul
                  local.get $l0
                  f64.load offset=8400
                  f64.add
                  f64.store
                  local.get $l0
                  f64.load offset=8224
                  local.get $l0
                  f64.load offset=8224
                  f64.mul
                  local.get $l0
                  f64.load offset=8216
                  local.get $l0
                  f64.load offset=8216
                  f64.mul
                  f64.add
                  f64.const 0x1p+2 (;=4;)
                  f64.gt
                  if $I11
                    local.get $l0
                    local.get $l0
                    i32.load8_u offset=8251
                    local.get $l0
                    i32.load8_u offset=8243
                    i32.const -1
                    i32.xor
                    i32.and
                    i32.store8 offset=8251
                  end
                  local.get $l0
                  local.get $l0
                  i32.load8_u offset=8243
                  i32.const 1
                  i32.shr_u
                  i32.store8 offset=8243
                  local.get $l0
                  local.get $l0
                  i32.load offset=8236
                  i32.const 1
                  i32.add
                  i32.store offset=8236
                  br $L9
                end
              end
              i32.const 0
              local.set $l1
              local.get $l0
              i32.load8_u offset=8251
              if $I12
                local.get $l0
                local.get $l0
                i32.load offset=8244
                i32.const 1
                i32.sub
                local.tee $l2
                i32.store offset=8244
                local.get $l2
                i32.const 0
                i32.ne
                local.set $l1
              end
              local.get $l1
              br_if $L8
            end
            local.get $l0
            i32.load offset=8424
            local.get $l0
            i32.load offset=8412
            i32.const 9
            i32.shl
            i32.const 8
            i32.div_s
            local.get $l0
            i32.load offset=8396
            i32.const 8
            i32.div_s
            i32.add
            i32.add
            local.get $l0
            i32.load8_u offset=8251
            i32.store8
            local.get $l0
            local.get $l0
            i32.load offset=8396
            i32.const 8
            i32.add
            i32.store offset=8396
            br $L4
          end
        end
        local.get $l0
        local.get $l0
        i32.load offset=8412
        i32.const 1
        i32.add
        i32.store offset=8412
        br $L2
      end
    end
    i32.const 0
    i32.load
    local.set $l3
    local.get $l0
    i64.const 512
    local.tee $l4
    i64.store offset=8
    local.get $l0
    local.get $l4
    i64.store
    local.get $l3
    i32.const 1024
    local.get $l0
    call $fprintf
    drop
    local.get $l0
    i32.load offset=8424
    i32.const 32768
    i32.const 1
    i32.const 0
    i32.load
    call $fwrite
    drop
    local.get $l0
    i32.load offset=8424
    call $free
    local.get $l0
    i32.load offset=8420
    local.tee $l0
    i32.const 8432
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1036))
  (global $__stack_low i32 (i32.const 1040))
  (global $__stack_high i32 (i32.const 66576))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66576))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "_start" (func $_start))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (data $.rodata (i32.const 1024) "P4\0a%jd %jd\0a"))
