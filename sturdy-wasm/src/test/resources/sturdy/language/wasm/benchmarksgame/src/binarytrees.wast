(module $binarytrees.wasm
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func (param i32)))
  (type (;2;) (func (result i32)))
  (type (;3;) (func (param f64) (result f64)))
  (type (;4;) (func))
  (type (;5;) (func (param i32 i32) (result i32)))
  (import "env" "malloc" (func $malloc (type 0)))
  (import "env" "free" (func $free (type 1)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type 2)))
  (import "env" "exp2" (func $exp2 (type 3)))
  (import "env" "assert" (func $assert (type 1)))
  (func $__wasm_call_ctors (type 4))
  (func $NewTreeNode (type 5) (param i32 i32) (result i32)
    (local i32)
    i32.const 8
    call $malloc
    local.tee 2
    local.get 1
    i32.store offset=4
    local.get 2
    local.get 0
    i32.store
    local.get 2)
  (func $ItemCheck (type 0) (param i32) (result i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      i32.load
      local.tee 1
      br_if 0 (;@1;)
      i32.const 1
      return
    end
    i32.const 0
    local.set 2
    loop  ;; label = @1
      local.get 2
      local.get 1
      call $ItemCheck
      i32.add
      i32.const 1
      i32.add
      local.set 2
      local.get 0
      i32.load offset=4
      local.tee 0
      i32.load
      local.tee 1
      br_if 0 (;@1;)
    end
    local.get 2
    i32.const 1
    i32.add)
  (func $BottomUpTree (type 0) (param i32) (result i32)
    (local i32 i32)
    block  ;; label = @1
      block  ;; label = @2
        local.get 0
        br_if 0 (;@2;)
        i32.const 0
        local.set 1
        i32.const 0
        local.set 2
        br 1 (;@1;)
      end
      local.get 0
      i32.const -1
      i32.add
      local.tee 0
      call $BottomUpTree
      local.set 1
      local.get 0
      call $BottomUpTree
      local.set 2
    end
    i32.const 8
    call $malloc
    local.tee 0
    local.get 2
    i32.store offset=4
    local.get 0
    local.get 1
    i32.store
    local.get 0)
  (func $DeleteTree (type 1) (param i32)
    (local i32)
    block  ;; label = @1
      local.get 0
      i32.load
      local.tee 1
      i32.eqz
      br_if 0 (;@1;)
      local.get 1
      call $DeleteTree
      local.get 0
      i32.load offset=4
      call $DeleteTree
    end
    local.get 0
    call $free)
  (func $_start (type 2) (result i32)
    (local i32 i32 i32 i32 f64 i32 i32 i32 i32 i32)
    call $__VERIFIER_nondet_int
    local.set 0
    block  ;; label = @1
      block  ;; label = @2
        call $__VERIFIER_nondet_int
        local.tee 1
        i32.const 2
        i32.add
        local.tee 2
        local.get 0
        local.get 2
        local.get 0
        i32.gt_u
        select
        local.tee 3
        i32.const 1
        i32.add
        local.tee 0
        f64.convert_i32_u
        call $exp2
        local.tee 4
        f64.abs
        f64.const 0x1p+31 (;=2.14748e+09;)
        f64.lt
        i32.eqz
        br_if 0 (;@2;)
        local.get 4
        i32.trunc_f64_s
        local.set 2
        br 1 (;@1;)
      end
      i32.const -2147483648
      local.set 2
    end
    local.get 0
    call $BottomUpTree
    local.tee 0
    call $ItemCheck
    local.get 2
    i32.eq
    call $assert
    local.get 0
    call $DeleteTree
    local.get 3
    call $BottomUpTree
    local.set 5
    block  ;; label = @1
      local.get 1
      local.get 3
      i32.gt_u
      br_if 0 (;@1;)
      local.get 3
      local.get 1
      i32.add
      local.set 6
      loop  ;; label = @2
        block  ;; label = @3
          block  ;; label = @4
            local.get 6
            local.get 1
            i32.sub
            f64.convert_i32_u
            call $exp2
            local.tee 4
            f64.abs
            f64.const 0x1p+31 (;=2.14748e+09;)
            f64.lt
            i32.eqz
            br_if 0 (;@4;)
            local.get 4
            i32.trunc_f64_s
            local.set 7
            br 1 (;@3;)
          end
          i32.const -2147483648
          local.set 7
        end
        i32.const 0
        local.set 0
        block  ;; label = @3
          local.get 7
          i32.const 1
          i32.lt_s
          br_if 0 (;@3;)
          local.get 7
          local.set 2
          loop  ;; label = @4
            local.get 1
            call $BottomUpTree
            local.tee 8
            call $ItemCheck
            local.set 9
            local.get 8
            call $DeleteTree
            local.get 9
            local.get 0
            i32.add
            local.set 0
            local.get 2
            i32.const -1
            i32.add
            local.tee 2
            br_if 0 (;@4;)
          end
        end
        block  ;; label = @3
          block  ;; label = @4
            local.get 1
            f64.convert_i32_u
            call $exp2
            local.tee 4
            f64.abs
            f64.const 0x1p+31 (;=2.14748e+09;)
            f64.lt
            i32.eqz
            br_if 0 (;@4;)
            local.get 4
            i32.trunc_f64_s
            local.set 2
            br 1 (;@3;)
          end
          i32.const -2147483648
          local.set 2
        end
        local.get 7
        local.get 2
        i32.mul
        local.get 0
        i32.eq
        call $assert
        local.get 1
        i32.const 2
        i32.add
        local.tee 1
        local.get 3
        i32.le_u
        br_if 0 (;@2;)
      end
    end
    block  ;; label = @1
      block  ;; label = @2
        local.get 3
        f64.convert_i32_u
        call $exp2
        local.tee 4
        f64.abs
        f64.const 0x1p+31 (;=2.14748e+09;)
        f64.lt
        i32.eqz
        br_if 0 (;@2;)
        local.get 4
        i32.trunc_f64_s
        local.set 0
        br 1 (;@1;)
      end
      i32.const -2147483648
      local.set 0
    end
    local.get 5
    call $ItemCheck
    local.get 0
    i32.eq
    call $assert
    i32.const 0)
  (memory (;0;) 2)
  (global $__stack_pointer (mut i32) (i32.const 66560))
  (global (;1;) i32 (i32.const 1024))
  (global (;2;) i32 (i32.const 1024))
  (global (;3;) i32 (i32.const 1024))
  (global (;4;) i32 (i32.const 66560))
  (global (;5;) i32 (i32.const 1024))
  (global (;6;) i32 (i32.const 66560))
  (global (;7;) i32 (i32.const 131072))
  (global (;8;) i32 (i32.const 0))
  (global (;9;) i32 (i32.const 1))
  (export "memory" (memory 0))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "NewTreeNode" (func $NewTreeNode))
  (export "ItemCheck" (func $ItemCheck))
  (export "BottomUpTree" (func $BottomUpTree))
  (export "DeleteTree" (func $DeleteTree))
  (export "_start" (func $_start))
  (export "__dso_handle" (global 1))
  (export "__data_end" (global 2))
  (export "__stack_low" (global 3))
  (export "__stack_high" (global 4))
  (export "__global_base" (global 5))
  (export "__heap_base" (global 6))
  (export "__heap_end" (global 7))
  (export "__memory_base" (global 8))
  (export "__table_base" (global 9)))
