(module $reverse-complement.wasm
  (type $t0 (func (param i32 i32 i32) (result i32)))
  (type $t1 (func (param i32) (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (param i32 i32)))
  (type $t6 (func (result i32)))
  (import "env" "memmove" (func $memmove (type $t0)))
  (import "env" "toupper" (func $toupper (type $t1)))
  (import "env" "tolower" (func $tolower (type $t1)))
  (import "env" "malloc" (func $malloc (type $t1)))
  (import "env" "fileno" (func $fileno (type $t1)))
  (import "env" "read" (func $read (type $t0)))
  (import "env" "realloc" (func $realloc (type $t2)))
  (import "env" "write" (func $write (type $t0)))
  (import "env" "free" (func $free (type $t3)))
  (func $__wasm_call_ctors (type $t4))
  (func $process (type $t5) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    local.get $p1
    local.get $p0
    i32.sub
    local.set $l2
    i32.const 0
    local.set $l3
    loop $L0
      local.get $l2
      i32.const -1
      i32.add
      local.set $l2
      local.get $p0
      local.get $l3
      i32.add
      local.set $l4
      local.get $l3
      i32.const 1
      i32.add
      local.tee $l5
      local.set $l3
      local.get $l4
      i32.load8_u
      i32.const 10
      i32.ne
      br_if $L0
    end
    local.get $p0
    local.get $l5
    i32.add
    local.set $l4
    block $B1
      local.get $p1
      local.get $p0
      i32.sub
      local.get $l2
      i32.const 61
      i32.div_u
      i32.const 61
      i32.mul
      local.tee $l2
      i32.sub
      local.tee $l3
      i32.const -60
      i32.add
      local.get $l5
      i32.eq
      br_if $B1
      local.get $l4
      local.get $l3
      local.get $l5
      i32.sub
      i32.add
      local.tee $l3
      local.get $p1
      i32.ge_u
      br_if $B1
      local.get $p0
      local.get $p1
      i32.sub
      local.get $l2
      i32.add
      local.get $l5
      i32.add
      i32.const 60
      i32.add
      local.set $l2
      loop $L2
        local.get $l3
        i32.const 1
        i32.add
        local.get $l3
        local.get $l2
        call $memmove
        drop
        local.get $l3
        i32.const 10
        i32.store8
        local.get $l3
        i32.const 61
        i32.add
        local.tee $l3
        local.get $p1
        i32.lt_u
        br_if $L2
      end
    end
    block $B3
      local.get $l4
      local.get $p1
      i32.const -1
      i32.add
      local.tee $l3
      i32.gt_u
      br_if $B3
      loop $L4
        local.get $l4
        i32.load8_s
        i32.const 1072
        i32.add
        i32.load8_u
        local.set $l2
        local.get $l4
        local.get $l3
        i32.load8_s
        i32.const 1072
        i32.add
        i32.load8_u
        i32.store8
        local.get $l3
        local.get $l2
        i32.store8
        local.get $l4
        i32.const 1
        i32.add
        local.tee $l4
        local.get $l3
        i32.const -1
        i32.add
        local.tee $l3
        i32.le_u
        br_if $L4
      end
    end)
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    i32.const 0
    local.set $l0
    block $B0
      i32.const 0
      i32.load8_u offset=1024
      local.tee $l1
      i32.eqz
      br_if $B0
      i32.const 1025
      local.set $l2
      loop $L1
        local.get $l1
        i32.extend8_s
        local.tee $l1
        call $toupper
        i32.const 1072
        i32.add
        local.get $l2
        i32.load8_u
        local.tee $l3
        i32.store8
        local.get $l1
        call $tolower
        i32.const 1072
        i32.add
        local.get $l3
        i32.store8
        local.get $l2
        i32.const 1
        i32.add
        local.set $l1
        local.get $l2
        i32.const 2
        i32.add
        local.set $l2
        local.get $l1
        i32.load8_u
        local.tee $l1
        br_if $L1
      end
    end
    i32.const 8192
    local.set $l2
    i32.const 8192
    call $malloc
    local.set $l4
    i32.const 7936
    local.set $l1
    block $B2
      block $B3
        i32.const 0
        i32.load
        call $fileno
        local.tee $l5
        local.get $l4
        i32.const 7936
        call $read
        local.tee $l3
        br_if $B3
        local.get $l4
        local.set $l6
        br $B2
      end
      i32.const 0
      local.set $l0
      loop $L4
        block $B5
          local.get $l3
          local.get $l0
          i32.add
          local.tee $l0
          local.get $l1
          i32.lt_u
          br_if $B5
          local.get $l4
          local.get $l2
          i32.const 1048576
          i32.add
          local.get $l2
          i32.const 1
          i32.shl
          local.get $l2
          i32.const 1048575
          i32.gt_u
          select
          local.tee $l2
          call $realloc
          local.set $l4
        end
        local.get $l5
        local.get $l4
        local.get $l0
        i32.add
        local.tee $l6
        local.get $l2
        i32.const -256
        i32.add
        local.tee $l1
        local.get $l0
        i32.sub
        call $read
        local.tee $l3
        br_if $L4
      end
    end
    local.get $l6
    i32.const 62
    i32.store8
    local.get $l6
    i32.const -1
    i32.add
    local.set $l6
    loop $L6
      local.get $l6
      local.set $l2
      loop $L7
        local.get $l2
        i32.load8_u
        local.set $l1
        local.get $l2
        i32.const -1
        i32.add
        local.tee $l3
        local.set $l2
        local.get $l1
        i32.const 62
        i32.ne
        br_if $L7
      end
      local.get $l3
      i32.const 1
      i32.add
      local.get $l6
      call $process
      local.get $l3
      local.set $l6
      local.get $l3
      local.get $l4
      i32.ge_u
      br_if $L6
    end
    i32.const 0
    i32.load
    call $fileno
    local.get $l4
    local.get $l0
    call $write
    drop
    local.get $l4
    call $free
    i32.const 0)
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
  (data $.data (i32.const 1024) "ATCGGCTAUAMKRYWWSSYRKMVBHDDHBVNN\0a\0a\00")
  (@custom ".debug_loc" "\ff\ff\ff\ff\06\00\00\00\00\00\00\00\0e\00\00\00\04\00\ed\00\00\9f\dc\00\00\00\de\00\00\00\04\00\ed\02\00\9f\de\00\00\00\e8\00\00\00\04\00\ed\00\04\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\00\00\00\00\9b\00\00\00\04\00\ed\00\01\9f\e3\00\00\00\e5\00\00\00\04\00\ed\02\01\9f\e5\00\00\00\e8\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\92\00\00\00\94\00\00\00\04\00\ed\02\00\9f\94\00\00\00\99\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\06\00\00\00\bc\00\00\00\e8\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f3\00\00\00j\00\00\00n\01\00\00\05\00\10\80\80@\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f3\00\00\00j\00\00\00\b0\00\00\00\04\00\10\80@\9f\d7\00\00\00\d9\00\00\00\04\00\ed\02\01\9f\d9\00\00\00\e1\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f3\00\00\00j\00\00\00\b0\00\00\00\02\000\9f\b9\00\00\00\bb\00\00\00\04\00\ed\02\00\9f\bb\00\00\00\e2\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f3\00\00\00|\00\00\00\b0\00\00\00\04\00\ed\00\04\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f3\00\00\00\94\00\00\00\96\00\00\00\04\00\ed\02\00\9f\96\00\00\00n\01\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f3\00\00\00\fc\00\00\00\fe\00\00\00\04\00\ed\02\00\9f\fe\00\00\00\00\01\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\f3\00\00\00$\01\00\00&\01\00\00\04\00\ed\02\00\9f&\01\00\00n\01\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\07.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\08\05\00\02\17\03\0e:\0b;\0bI\13\00\00\094\00\02\17\03\0e:\0b;\0bI\13\00\00\0a4\00\03\0e:\0b;\0bI\13\00\00\0b\0b\01\11\01\12\06\00\00\0c.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\0d\89\82\01\001\13\11\01\00\00\0e.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\0f\05\00I\13\00\00\10\0f\00\00\00\11\16\00I\13\03\0e:\0b;\0b\00\00\12\0f\00I\13\00\00\13\13\00\03\0e<\19\00\00\14&\00\00\00\15.\01\03\0e:\0b;\0b'\19<\19?\19\00\00\16&\00I\13\00\00\00")
  (@custom ".debug_info" "\c4\02\00\00\04\00\00\00\00\00\04\01,\01\00\00\1d\00\f6\00\00\00\00\00\00\00}\00\00\00\00\00\00\00\00\00\00\00\02\1b\00\00\007\00\00\00\01\11\05\03\00\04\00\00\03C\00\00\00\04J\00\00\00#\00\051\00\00\00\06\01\06\0b\01\00\00\08\07\02O\00\00\00b\00\00\00\01\12\05\030\04\00\00\03C\00\00\00\04J\00\00\00\80\00\05\07\00\00\00\05\04\07\06\00\00\00\eb\00\00\00\07\ed\03\00\00\00\00\9f\13\00\00\00\01\15\08\00\00\00\00J\00\00\00\01\15\bd\02\00\00\08:\00\00\006\00\00\00\01\15\bd\02\00\00\09\a0\00\00\00\09\01\00\00\01#C\00\00\00\0aF\00\00\00\01\18\1b\02\00\00\0ae\00\00\00\01\19\1b\02\00\00\0b\80\00\00\00)\00\00\00\09t\00\00\00M\00\00\00\01\1c\bd\02\00\00\00\00\0c\f3\00\00\00n\01\00\00\07\ed\03\00\00\00\00\9f\00\00\00\00\01(n\00\00\00\09\be\00\00\00\1f\01\00\00\010\c2\02\00\00\09\dd\00\00\00C\00\00\00\011\1b\02\00\00\09\17\01\00\00t\00\00\00\011\1b\02\00\00\09O\01\00\00a\00\00\00\012\bd\02\00\00\09m\01\00\00@\00\00\00\014n\00\00\00\09\99\01\00\00F\00\00\00\011\1b\02\00\00\0a6\00\00\00\01?\bd\02\00\00\09\c5\01\00\00J\00\00\00\01?\bd\02\00\00\0a\1f\00\00\00\01)\bd\02\00\00\0d\e7\01\00\00\1f\01\00\00\0d\f8\01\00\008\01\00\00\0d\09\02\00\00m\01\00\00\0d-\02\00\00\87\01\00\00\0dS\02\00\00\94\01\00\00\0dy\02\00\00\d2\01\00\00\0dS\02\00\00\ef\01\00\00\0du\00\00\000\02\00\00\0d-\02\00\00K\02\00\00\0d\8f\02\00\00U\02\00\00\0d\b0\02\00\00^\02\00\00\00\0e)\00\00\00\02\c8n\00\00\00\0fn\00\00\00\00\0e!\00\00\00\02\c9n\00\00\00\0fn\00\00\00\00\0e\e7\00\00\00\02\cb\1a\02\00\00\0f\1b\02\00\00\00\10\11&\02\00\00\0c\00\00\00\02\9c\05S\00\00\00\07\04\0e9\00\00\00\02\f1n\00\00\00\0f>\02\00\00\00\12C\02\00\00\11N\02\00\00'\01\00\00\02\ea\13#\01\00\00\0ex\00\00\00\02\ecn\02\00\00\0fn\00\00\00\0f\1a\02\00\00\0f\1b\02\00\00\00\11n\00\00\00\0b\00\00\00\02\e8\0e\ee\00\00\00\02\cd\1a\02\00\00\0f\1a\02\00\00\0f&\02\00\00\00\0ei\00\00\00\02\ebn\02\00\00\0fn\00\00\00\0f\aa\02\00\00\0f\1b\02\00\00\00\12\af\02\00\00\14\15o\00\00\00\02\ce\0f\1a\02\00\00\00\12C\00\00\00\16\1b\02\00\00\00")
  (@custom ".debug_ranges" "\06\00\00\00\f1\00\00\00\f3\00\00\00a\02\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "_start\00int\00ssize_t\00process\00pairs\00tolower\00toupper\00char\00to\00fileno\00in\00buflen\00from\00tbl\00unsigned long\00buf\00off\00write\00free\00end\00read\00/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00malloc\00realloc\00reverse-complement.c\00__ARRAY_SIZE_TYPE__\00_1M\00_IO_FILE\00clang version 19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)\00")
  (@custom ".debug_line" "\e5\01\00\00\04\00@\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00reverse-complement.c\00\00\00\00stdlib.h\00\01\00\00\00\05\04\0a\00\05\02\09\00\00\00\03\15\01\06\03j\ac\03\16f\05\10<\03jt\05\04\03\16J\05\0bX\05\13t\05\04 \03j<\05\14\06\03\19t\05\08\08h\06<\03e.\03\1b\82\03et\05\14\06\03\1e\08X\05\0a\06 \05\0d\06\e5\05+r\05#\06 \05\07X\03cJ\05\00\06\03$\82\05\14\06 \05\04<\05\14\06/\05\0b\06\c8\05-f\05$\c8\05\22J\057<\03[t\05\1f\06\03$J\06\03\5c \05\00\03$f\05\14 \05\04<\05\01\06L\02\01\00\01\01\05\14\0a\00\05\02\f6\00\00\00\03)\01\05\04\06\e4\03VX\05\13\06\03+\82\05\0b\06X\03U\82\05\07\03+f\05\1c \05\1aX\05\0b\06Y\06\03T\82\05\07\03,f\05\1a \03TX\05\14\06\03*J\05\04\06t\05\14<\05\04X\03Vf\05\10\06\032\9e\06\03N\82\05\14\06\034\ac\05\0d\06t\03Lf\05\12\06\035t\05\04\06f\03KJ\035J\03K.\05\0b\06\037X\05\0f\91\05\0b\06X\03H.\05\13\06\039\90\05\1b\06\ba\05\13 \05\10\06!\06\03F\9e\05\1f\06\035 \05-\06\d6\053 \05\12X\05\04f\05\0d\06\a6\05 v\06\03A<\05\17\06\03\c1\00f\05)\06\c8\03\bf\7f \05\1d\03\c1\00\82\05\07 \06v\06\03\bd\7f\90\05\0e\06\03\c6\00J\05\0b\06X\05\11\06?\05\0a\06\90\05\04f\06\ad\05\11\81\05\041\02\01\00\01\01")
  (@custom "name" "\00\18\17reverse-complement.wasm\01l\0c\00\07memmove\01\07toupper\02\07tolower\03\06malloc\04\06fileno\05\04read\06\07realloc\07\05write\08\04free\09\11__wasm_call_ctors\0a\07process\0b\06_start\07\12\01\00\0f__stack_pointer\09\08\01\00\05.data")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clangZ19.1.7 (https://github.com/llvm/llvm-project.git cd708029e0b2869e80abe31ddb175f7c35361f90)")
  (@custom "target_features" "\04+\0amultivalue+\0fmutable-globals+\0freference-types+\08sign-ext"))
