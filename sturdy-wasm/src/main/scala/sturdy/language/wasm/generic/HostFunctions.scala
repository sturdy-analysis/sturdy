package sturdy.language.wasm.generic

import sturdy.values.Structural
import swam.{FuncType, NumType}

import NumType.*

case class HostFunction(name: String, funcType: FuncType):
  override def toString: String = name

given Structural[HostFunction] with {}

class HostModules(initModules: (String, List[HostFunction])*):
  var hostModules: Map[String, ModuleInstance] = Map()
  for((name, functions) <- initModules) addModule(name, functions)

  private def addModule(moduleName: String, functions: List[HostFunction]): Unit =
    val functionMap = functions.zipWithIndex.map((func,id) => func.name -> (id,func)).toMap[String, (Int, HostFunction)]
    val module = ModuleInstance(Some(moduleName))
    for((function,idx) <- functions.zipWithIndex) {
      module.exports :+= (function.name -> ExternalValue.Function(idx))
      module.addFunction(FunctionInstance.Host(module, idx, function))
    }
    hostModules += moduleName -> module

  def containsModule(hostModule: String): Boolean = hostModules.contains(hostModule)

  def getHostFunction(moduleName: String, functionName: String): Option[(ModuleInstance, Int, HostFunction)] =
    for {
      moduleInst <- hostModules.get(moduleName);
      case (_,ExternalValue.Function(idx)) <- moduleInst.exports.find((name,_) => name == functionName)
      case FunctionInstance.Host(_, _, hostFunction) <- moduleInst.functions.lift(idx)
    } yield ((moduleInst, idx, hostFunction))


val stdlib: List[HostFunction] = List(
  HostFunction("malloc", FuncType(Vector(I32), Vector(I32))),
  HostFunction("realloc", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("calloc", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("free", FuncType(Vector(I32), Vector())),
  HostFunction("i32.interval", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("f32.interval", FuncType(Vector(F32, F32), Vector(F32))),
  HostFunction("f64.interval", FuncType(Vector(F64, F64), Vector(F64))),
  HostFunction("i32.phi", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("assert", FuncType(Vector(I32), Vector())),
  HostFunction("pow", FuncType(Vector(F64, F64), Vector(F64))),
  HostFunction("exp2", FuncType(Vector(F64), Vector(F64))),
  HostFunction("memmove", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("memcpy", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("memset", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("memcmp", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("strlen", FuncType(Vector(I32), Vector(I32))),
  HostFunction("toupper", FuncType(Vector(I32), Vector(I32))),
  HostFunction("tolower", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fileno", FuncType(Vector(I32), Vector(I32))),
  HostFunction("read", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("puts", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fgets", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("fputs", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("write", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("putchar", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fwrite", FuncType(Vector(I32, I32, I32, I32), Vector(I32))),
  HostFunction("printf", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fprintf", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("qsort", FuncType(Vector(I32, I32, I32, I32), Vector())),
  HostFunction("exit", FuncType(Vector(I32), Vector())),
  HostFunction("abort", FuncType(Vector(), Vector())),
)

val wasi_snapshot_preview1: List[HostFunction] = List(
  HostFunction("args_sizes_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("args_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("clock_time_get", FuncType(Vector(I32, I64, I32), Vector(I32))),
  HostFunction("environ_sizes_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("environ_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_allocate", FuncType(Vector(I32, I64, I64), Vector(I32))),
  HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fd_fdstat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_fdstat_set_flags", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_filestat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_filestat_set_size", FuncType(Vector(I32, I64), Vector(I32))),
  HostFunction("fd_filestat_set_times", FuncType(Vector(I32, I64, I64, I32), Vector(I32))),
  HostFunction("fd_pread", FuncType(Vector(I32, I32, I32, I64, I32), Vector(I32))),
  HostFunction("fd_prestat_dir_name", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("fd_prestat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_pwrite", FuncType(Vector(I32, I32, I32, I64, I32), Vector(I32))),
  HostFunction("fd_read", FuncType(Vector(I32, I32, I32, I32), Vector(I32))),
  HostFunction("fd_readdir", FuncType(Vector(I32, I32, I32, I64, I32), Vector(I32))),
  HostFunction("fd_renumber", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_seek", FuncType(Vector(I32, I64, I32, I32), Vector(I32))),
  HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fd_write", FuncType(Vector(I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_create_directory", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("path_filestat_get", FuncType(Vector(I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_filestat_set_times", FuncType(Vector(I32, I32, I32, I32, I64, I64, I32), Vector(I32))),
  HostFunction("path_open", FuncType(Vector(I32, I32, I32, I32, I32, I64, I64, I32, I32), Vector(I32))),
  HostFunction("path_link", FuncType(Vector(I32, I32, I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_readlink", FuncType(Vector(I32, I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_remove_directory", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("path_rename", FuncType(Vector(I32, I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_symlink", FuncType(Vector(I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_unlink_file", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("poll_oneoff", FuncType(Vector(I32, I32, I32, I32), Vector(I32))),
  HostFunction("proc_exit", FuncType(Vector(I32), Vector())),
  HostFunction("random_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("sched_yield", FuncType(Vector(), Vector(I32)))
)


val svbenchHostFunctions: List[HostFunction] = List(
  HostFunction("__VERIFIER_nondet_bool", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_char", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_short", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_int", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_long", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_longlong", FuncType(Vector(), Vector(I64))),
  HostFunction("__VERIFIER_nondet_uchar", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_ushort", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_uint", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_ulong", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_ulonglong", FuncType(Vector(), Vector(I64))),
  HostFunction("__VERIFIER_nondet_float", FuncType(Vector(), Vector(F32))),
  HostFunction("__VERIFIER_nondet_double", FuncType(Vector(), Vector(F64))),
  HostFunction("host_assert_fail", FuncType(Vector(), Vector())),
  HostFunction("__assert_fail", FuncType(Vector(I32, I32, I32, I32), Vector())),
  HostFunction("__blackhole_int", FuncType(Vector(I32), Vector(I32))),
  HostFunction("__blackhole_int_p", FuncType(Vector(I32), Vector(I32))),
  HostFunction("__blackhole_unsigned_int", FuncType(Vector(I32), Vector(I32))),
  HostFunction("__blackhole_unsigned_int_p", FuncType(Vector(I32), Vector(I32))),
  HostFunction("__blackhole_unsigned_int_p_p", FuncType(Vector(I32), Vector(I32))),
)

val wasi_unstable: List[HostFunction] = List(
  HostFunction("args_sizes_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("args_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("clock_time_get", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("environ_sizes_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("environ_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_allocate", FuncType(Vector(I32, I64, I64), Vector(I32))),
  HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fd_fdstat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_fdstat_set_flags", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_filestat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_filestat_set_size", FuncType(Vector(I32, I64), Vector(I32))),
  HostFunction("fd_filestat_set_times", FuncType(Vector(I32, I64, I64, I32), Vector(I32))),
  HostFunction("fd_pread", FuncType(Vector(I32, I32, I32, I64, I32), Vector(I32))),
  HostFunction("fd_prestat_dir_name", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("fd_prestat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_pwrite", FuncType(Vector(I32, I32, I32, I64, I32), Vector(I32))),
  HostFunction("fd_read", FuncType(Vector(I32, I32, I32, I32), Vector(I32))),
  HostFunction("fd_readdir", FuncType(Vector(I32, I32, I32, I64, I32), Vector(I32))),
  HostFunction("fd_renumber", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_seek", FuncType(Vector(I32, I64, I32, I32), Vector(I32))),
  HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fd_write", FuncType(Vector(I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_create_directory", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("path_filestat_get", FuncType(Vector(I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_filestat_set_times", FuncType(Vector(I32, I32, I32, I32, I64, I64, I32), Vector(I32))),
  HostFunction("path_open", FuncType(Vector(I32, I32, I32, I32, I32, I64, I64, I32, I32), Vector(I32))),
  HostFunction("path_link", FuncType(Vector(I32, I32, I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_readlink", FuncType(Vector(I32, I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_remove_directory", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("path_rename", FuncType(Vector(I32, I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_symlink", FuncType(Vector(I32, I32, I32, I32, I32), Vector(I32))),
  HostFunction("path_unlink_file", FuncType(Vector(I32, I32, I32), Vector(I32))),
  HostFunction("poll_oneoff", FuncType(Vector(I32, I32, I32, I32), Vector(I32))),
  HostFunction("proc_exit", FuncType(Vector(I32), Vector())),
  HostFunction("random_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("sched_yield", FuncType(Vector(), Vector(I32)))
)


val defaultHostModules = HostModules(
  "env" -> (stdlib ++ svbenchHostFunctions),
  "wasi_snapshot_preview1" -> wasi_snapshot_preview1,
  "wasi_unstable" -> wasi_unstable
)