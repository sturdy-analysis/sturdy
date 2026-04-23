(module $reverse-complement.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32)))
  (type $t6 (func (result i32)))
  (import "env" "toupper" (func $toupper (type $t0)))
  (import "env" "tolower" (func $tolower (type $t0)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "fileno" (func $fileno (type $t0)))
  (import "env" "read" (func $read (type $t1)))
  (import "env" "realloc" (func $realloc (type $t2)))
  (import "env" "write" (func $write (type $t1)))
  (import "env" "free" (func $free (type $t3)))
  (func $__wasm_call_ctors (type $t4))
  (func $process (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    global.get $__stack_pointer
    i32.const 32
    i32.sub
    local.set $l2
    local.get $l2
    local.get $p0
    i32.store offset=28
    local.get $l2
    local.get $p1
    i32.store offset=24
    block $B0
      loop $L1
        local.get $l2
        i32.load offset=28
        local.set $l3
        local.get $l2
        local.get $l3
        i32.const 1
        i32.add
        i32.store offset=28
        local.get $l3
        i32.load8_u
        local.set $l4
        i32.const 24
        local.set $l5
        local.get $l4
        local.get $l5
        i32.shl
        local.get $l5
        i32.shr_s
        i32.const 10
        i32.ne
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        br $L1
      end
    end
    local.get $l2
    local.get $l2
    i32.load offset=24
    local.get $l2
    i32.load offset=28
    i32.sub
    i32.store offset=20
    local.get $l2
    i32.load offset=20
    i32.const 61
    i32.rem_u
    local.set $l6
    local.get $l2
    i32.const 60
    local.get $l6
    i32.sub
    i32.store offset=16
    block $B2
      local.get $l2
      i32.load offset=16
      i32.eqz
      br_if $B2
      local.get $l2
      i32.load offset=28
      i32.const 60
      i32.add
      local.set $l7
      local.get $l2
      i32.load offset=16
      local.set $l8
      local.get $l2
      local.get $l7
      i32.const 0
      local.get $l8
      i32.sub
      i32.add
      i32.store offset=12
      block $B3
        loop $L4
          local.get $l2
          i32.load offset=12
          local.get $l2
          i32.load offset=24
          i32.lt_u
          i32.const 1
          i32.and
          i32.eqz
          br_if $B3
          local.get $l2
          i32.load offset=12
          i32.const 1
          i32.add
          local.set $l9
          local.get $l2
          i32.load offset=12
          local.set $l10
          local.get $l2
          i32.load offset=16
          local.set $l11
          block $B5
            local.get $l11
            i32.eqz
            br_if $B5
            local.get $l9
            local.get $l10
            local.get $l11
            memory.copy
          end
          local.get $l2
          i32.load offset=12
          i32.const 10
          i32.store8
          local.get $l2
          local.get $l2
          i32.load offset=12
          i32.const 61
          i32.add
          i32.store offset=12
          br $L4
        end
      end
    end
    local.get $l2
    local.get $l2
    i32.load offset=24
    i32.const -1
    i32.add
    i32.store offset=24
    block $B6
      loop $L7
        local.get $l2
        i32.load offset=28
        local.get $l2
        i32.load offset=24
        i32.le_u
        i32.const 1
        i32.and
        i32.eqz
        br_if $B6
        local.get $l2
        i32.load offset=28
        i32.load8_u
        local.set $l12
        i32.const 24
        local.set $l13
        local.get $l2
        local.get $l12
        local.get $l13
        i32.shl
        local.get $l13
        i32.shr_s
        i32.load8_u offset=1072
        i32.store8 offset=11
        local.get $l2
        i32.load offset=24
        i32.load8_u
        local.set $l14
        i32.const 24
        local.set $l15
        local.get $l14
        local.get $l15
        i32.shl
        local.get $l15
        i32.shr_s
        i32.load8_u offset=1072
        local.set $l16
        local.get $l2
        i32.load offset=28
        local.get $l16
        i32.store8
        local.get $l2
        i32.load8_u offset=11
        local.set $l17
        local.get $l2
        i32.load offset=24
        local.get $l17
        i32.store8
        local.get $l2
        local.get $l2
        i32.load offset=28
        i32.const 1
        i32.add
        i32.store offset=28
        local.get $l2
        local.get $l2
        i32.load offset=24
        i32.const -1
        i32.add
        i32.store offset=24
        br $L7
      end
    end
    return)
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    global.get $__stack_pointer
    i32.const 48
    i32.sub
    local.set $l0
    local.get $l0
    global.set $__stack_pointer
    local.get $l0
    i32.const 1024
    i32.store offset=44
    block $B0
      loop $L1
        local.get $l0
        i32.load offset=44
        i32.load8_u
        local.set $l1
        i32.const 0
        local.set $l2
        local.get $l1
        i32.const 255
        i32.and
        local.get $l2
        i32.const 255
        i32.and
        i32.ne
        i32.const 1
        i32.and
        i32.eqz
        br_if $B0
        local.get $l0
        i32.load offset=44
        i32.load8_u offset=1
        local.set $l3
        local.get $l0
        i32.load offset=44
        i32.load8_u
        local.set $l4
        i32.const 24
        local.set $l5
        local.get $l4
        local.get $l5
        i32.shl
        local.get $l5
        i32.shr_s
        call $toupper
        local.get $l3
        i32.store8 offset=1072
        local.get $l0
        i32.load offset=44
        i32.load8_u offset=1
        local.set $l6
        local.get $l0
        i32.load offset=44
        i32.load8_u
        local.set $l7
        i32.const 24
        local.set $l8
        local.get $l7
        local.get $l8
        i32.shl
        local.get $l8
        i32.shr_s
        call $tolower
        local.get $l6
        i32.store8 offset=1072
        local.get $l0
        local.get $l0
        i32.load offset=44
        i32.const 2
        i32.add
        i32.store offset=44
        br $L1
      end
    end
    local.get $l0
    i32.const 1048576
    i32.store offset=40
    local.get $l0
    i32.const 8192
    i32.store offset=36
    local.get $l0
    i32.const 0
    i32.store offset=28
    local.get $l0
    local.get $l0
    i32.load offset=36
    call $malloc
    i32.store offset=24
    local.get $l0
    i32.const 0
    i32.load
    call $fileno
    i32.store offset=20
    block $B2
      loop $L3
        local.get $l0
        i32.load offset=20
        local.get $l0
        i32.load offset=24
        local.get $l0
        i32.load offset=28
        i32.add
        local.get $l0
        i32.load offset=36
        i32.const 256
        i32.sub
        local.get $l0
        i32.load offset=28
        i32.sub
        call $read
        local.set $l9
        local.get $l0
        local.get $l9
        i32.store offset=32
        local.get $l9
        i32.eqz
        br_if $B2
        block $B4
          local.get $l0
          i32.load offset=32
          i32.const 0
          i32.lt_u
          i32.const 1
          i32.and
          i32.eqz
          br_if $B4
          br $B2
        end
        local.get $l0
        local.get $l0
        i32.load offset=32
        local.get $l0
        i32.load offset=28
        i32.add
        i32.store offset=28
        block $B5
          local.get $l0
          i32.load offset=28
          local.get $l0
          i32.load offset=36
          i32.const 256
          i32.sub
          i32.ge_u
          i32.const 1
          i32.and
          i32.eqz
          br_if $B5
          block $B6
            block $B7
              local.get $l0
              i32.load offset=36
              i32.const 1048576
              i32.ge_u
              i32.const 1
              i32.and
              i32.eqz
              br_if $B7
              local.get $l0
              i32.load offset=36
              i32.const 1048576
              i32.add
              local.set $l10
              br $B6
            end
            local.get $l0
            i32.load offset=36
            i32.const 1
            i32.shl
            local.set $l10
          end
          local.get $l0
          local.get $l10
          i32.store offset=36
          local.get $l0
          local.get $l0
          i32.load offset=24
          local.get $l0
          i32.load offset=36
          call $realloc
          i32.store offset=24
        end
        br $L3
      end
    end
    local.get $l0
    i32.load offset=24
    local.get $l0
    i32.load offset=28
    i32.add
    i32.const 62
    i32.store8
    local.get $l0
    local.get $l0
    i32.load offset=24
    local.get $l0
    i32.load offset=28
    i32.add
    i32.const -1
    i32.add
    i32.store offset=12
    loop $L8
      local.get $l0
      local.get $l0
      i32.load offset=12
      i32.store offset=16
      block $B9
        loop $L10
          local.get $l0
          i32.load offset=16
          i32.load8_u
          local.set $l11
          i32.const 24
          local.set $l12
          local.get $l11
          local.get $l12
          i32.shl
          local.get $l12
          i32.shr_s
          i32.const 62
          i32.ne
          i32.const 1
          i32.and
          i32.eqz
          br_if $B9
          local.get $l0
          local.get $l0
          i32.load offset=16
          i32.const -1
          i32.add
          i32.store offset=16
          br $L10
        end
      end
      local.get $l0
      i32.load offset=16
      local.get $l0
      i32.load offset=12
      call $process
      local.get $l0
      local.get $l0
      i32.load offset=16
      i32.const -1
      i32.add
      i32.store offset=12
      block $B11
        block $B12
          local.get $l0
          i32.load offset=12
          local.get $l0
          i32.load offset=24
          i32.lt_u
          i32.const 1
          i32.and
          i32.eqz
          br_if $B12
          br $B11
        end
        br $L8
      end
    end
    i32.const 0
    i32.load
    call $fileno
    local.get $l0
    i32.load offset=24
    local.get $l0
    i32.load offset=28
    call $write
    drop
    local.get $l0
    i32.load offset=24
    call $free
    i32.const 0
    local.set $l13
    local.get $l0
    i32.const 48
    i32.add
    global.set $__stack_pointer
    local.get $l13
    return)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66736))
  (global $tbl i32 (i32.const 1072))
  (global $pairs i32 (i32.const 1024))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1200))
  (global $__stack_low i32 (i32.const 1200))
  (global $__stack_high i32 (i32.const 66736))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66736))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "process" (func $process))
  (export "tbl" (global $tbl))
  (export "_start" (func $_start))
  (export "pairs" (global $pairs))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (export "__wasm_first_page_end" (global $__wasm_first_page_end))
  (data $.data (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\07.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\08\05\00\02\18\03\0e:\0b;\0bI\13\00\00\094\00\02\18\03\0e:\0b;\0bI\13\00\00\0a\0b\01\11\01\12\06\00\00\0b.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\0c\0f\00I\13\00\00\0d\16\00I\13\03\0e:\0b;\0b\00\00\0e&\00I\13\00\00\00")
  (@custom ".debug_info" "\98\01\00\00\04\00\00\00\00\00\04\01\06\01\00\00\1d\00\d2\00\00\00\00\00\00\007\00\00\00\00\00\00\00\00\00\00\00\02\1a\00\00\007\00\00\00\01\11\05\03\00\04\00\00\03C\00\00\00\04J\00\00\00#\00\05 \00\00\00\06\01\06\ee\00\00\00\08\07\02\b4\00\00\00b\00\00\00\01\12\05\030\04\00\00\03C\00\00\00\04J\00\00\00\80\00\05\07\00\00\00\05\04\07\06\00\00\00\96\01\00\00\04\ed\00\02\9f\12\00\00\00\01\15\08\02\91\1c2\00\00\00\01\15\7f\01\00\00\08\02\91\18%\00\00\00\01\15\7f\01\00\00\09\02\91\14.\00\00\00\01\18\84\01\00\00\09\02\91\10\ca\00\00\00\01\19\84\01\00\00\09\02\91\0b\ec\00\00\00\01#C\00\00\00\0a\85\00\00\00x\00\00\00\09\02\91\0c5\00\00\00\01\1c\7f\01\00\00\00\00\0b\9e\01\00\00\8f\02\00\00\04\ed\00\00\9f\00\00\00\00\01(n\00\00\00\09\02\91,\1e\00\00\00\01)\7f\01\00\00\09\02\91(\02\01\00\00\010\96\01\00\00\09\02\91$+\00\00\00\011\84\01\00\00\09\02\91 .\00\00\00\011\84\01\00\00\09\02\91\1c\ce\00\00\00\011\84\01\00\00\09\02\91\18\c6\00\00\00\012\7f\01\00\00\09\02\91\14(\00\00\00\014n\00\00\00\09\02\91\102\00\00\00\01?\7f\01\00\00\09\02\91\0c%\00\00\00\01?\7f\01\00\00\00\0cC\00\00\00\0d\8f\01\00\00\0b\00\00\00\02\9c\05\b8\00\00\00\07\04\0e\84\01\00\00\00")
  (@custom ".debug_ranges" "\06\00\00\00\9c\01\00\00\9e\01\00\00-\04\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "_start\00int\00size_t\00process\00pairs\00char\00to\00in\00buflen\00from\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00tbl\00unsigned long\00buf\00off\00end\00../src/reverse-complement.c\00__ARRAY_SIZE_TYPE__\00_1M\00clang version 21.1.7\00")
  (@custom ".debug_line" "\a8\02\00\00\04\00L\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00../src/../..\00\00reverse-complement.c\00\01\00\00stdlib.h\00\02\00\00\00\00\05\02\06\00\00\00\03\14\01\05\10\0a\08\ad\05\0b\06\08J\05\13\08.\05\04<\05\11\06\a0\05\16\06t\05\14X\05\0b \05\17\06=\05\1b\06X\05\14X\05\0bt\05\08\06>\05\10\a0\05\15\06X\05\1cX\05\1at\05\0e\9e\05!<\05%\90\05#X\05\07 \05\12\06g\05\14\06X\05\19X\05\1ct\05\0at\03b\08\12\05\0b\06\03\1f \05\0d\06X\05+\06V\05\07\06\c8.\03c.\05\0b\06\03$ \05\0f\06\c8\05\17\90\05\14X\05\04 \05\15\06g\05\14\06X\05\0fX\05\0b\d6\05\09t\05.<\05-X\05(X\05$\ba\05\1d\90\05\22X\059X\054t\057X\05\1f\06W\05%\06\c8\05\04\c8.\05\01\060\02\02\00\01\01\00\05\02\9e\01\00\00\03'\01\05\0b\0a\08Z\05\15\06\ac\05\14\90\05\04X\05\1c\06\08g\05\13\06\9e\05\0b\08X\05\1af\05\1c\06\91\05\13\06\9e\05\0b\08X\05\1af\06\8e\05\04\06\c8.\05\11\064\05\0b\9f\05#\06\90\05\17\06u\05\10\06t\05\0af\05\14\06>\05\0d\06\ac\05\08f\05\17\06=\05\1b\06\90\05!X\05\1fX\05& \05-X\055J\053X\05\12 \05\10\82\05\04t\05\0b\06Y\05\0f\06t\05\14\90\03J.\05\0e\06\037 \05\0b\06t\06\91\05\12\06t\05\19X\05\0fJ\05\14\06u\05\1b\06\90\05\13f\05%f\05,X\05\13\82\03G.\054\039 \05;X\03GX\05\13\039 \05\11J\05\18\06=\05\1d\06t\05\10X\05\0ef\03F<\05\04\06\035 \05\14/\05\045\05\08\06X\05\04X\05\0d \05\16\06Z\05\1c\06t\05\1aX\05  \05\11<\05\13\06>\05\11\06\90\05\18<\05\17\90\05\1d\08\12\05\07<\05)\06f\05\07\06\c8.\05\0f\060\05\15\06X\05\07X\05\0c\06h\05\11\06t\05\0a<\05\0b\06=\05\10\06\90\05\0eX\05\15t\03\ba\7f.\05\04\06\03\c0\00 \06\03@.\05\15\06\03\c6\00 \05\11#\05\0a\06\90\05\1af\05\1fX\05\04X\05\09\06u\05\04\06X\06h\02\13\00\01\01")
  (@custom "name" "\00\18\17reverse-complement.wasm\01c\0b\00\07toupper\01\07tolower\02\06malloc\03\06fileno\04\04read\05\07realloc\06\05write\07\04free\08\11__wasm_call_ctors\09\07process\0a\06_start\07\12\01\00\0f__stack_pointer\09\08\01\00\05.data")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
