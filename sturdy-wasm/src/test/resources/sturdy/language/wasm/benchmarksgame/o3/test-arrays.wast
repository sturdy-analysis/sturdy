(module $test-arrays.wasm
  (type $t0 (func (param i32) (result i32)))
  (import "env" "blackhole_int" (func $blackhole_int (type $t0)))
  (func $_start (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    block $B0
      block $B1
        local.get $p0
        i32.const 1
        i32.ge_s
        br_if $B1
        i32.const 0
        local.set $l1
        br $B0
      end
      i32.const 0
      local.set $l2
      i32.const 0
      local.set $l1
      loop $L2
        local.get $l2
        call $blackhole_int
        drop
        local.get $l2
        call $blackhole_int
        local.get $l1
        i32.add
        local.set $l1
        local.get $p0
        local.get $l2
        i32.const 1
        i32.add
        local.tee $l2
        i32.ne
        br_if $L2
      end
    end
    local.get $l1)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66560))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (@custom "name" "\00\11\10test-arrays.wasm\01\18\02\00\0dblackhole_int\01\06_start\07\12\01\00\0f__stack_pointer")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
