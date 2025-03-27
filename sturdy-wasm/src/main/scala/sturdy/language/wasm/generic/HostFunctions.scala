package sturdy.language.wasm.generic

import sturdy.values.Structural
import swam.{FuncType, ValType}

import ValType.*

case class HostFunction(name: String, funcType: FuncType):
  override def toString: String = name

given Structural[HostFunction] with {}

class HostModules(initModules: (String, List[HostFunction])*):
  var hostModules: Map[String, (ModuleInstance, Map[String, (Int, HostFunction)])] = Map()
  for((name, functions) <- initModules) addModule(name, functions)

  def addModule(moduleName: String, functions: List[HostFunction]): Unit =
    val functionMap = functions.zipWithIndex.map((func,id) => func.name -> (id,func)).toMap[String, (Int, HostFunction)]
    hostModules += moduleName -> (ModuleInstance(Some(moduleName)), functionMap)

  def containsModule(hostModule: String): Boolean = hostModules.contains(hostModule)

  def getHostFunction(moduleName: String, functionName: String): Option[(ModuleInstance, Int, HostFunction)] =
    for {
      (moduleInst, functions) <- hostModules.get(moduleName);
      (ix, hostFunction) <- functions.get(functionName)
    } yield ((moduleInst, ix, hostFunction))


val wasi_snapshot_preview1: List[HostFunction] = List(
  HostFunction("args_sizes_get", FuncType(Vector(I32,I32), Vector(I32))),
  HostFunction("args_get", FuncType(Vector(I32,I32), Vector(I32))),
  HostFunction("clock_time_get", FuncType(Vector(I32,I64,I32), Vector(I32))),
  HostFunction("environ_sizes_get", FuncType(Vector(I32,I32), Vector(I32))),
  HostFunction("environ_get", FuncType(Vector(I32,I32), Vector(I32))),
  HostFunction("fd_allocate", FuncType(Vector(I32,I64,I64), Vector(I32))),
  HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fd_fdstat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_fdstat_set_flags", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_filestat_get", FuncType(Vector(I32, I32), Vector(I32))),
  HostFunction("fd_filestat_set_size", FuncType(Vector(I32, I64), Vector(I32))),
  HostFunction("fd_filestat_set_times", FuncType(Vector(I32, I64,I64,I32), Vector(I32))),
  HostFunction("fd_pread", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32))),
  HostFunction("fd_prestat_dir_name", FuncType(Vector(I32,I32,I32), Vector(I32))),
  HostFunction("fd_prestat_get", FuncType(Vector(I32,I32), Vector(I32))),
  HostFunction("fd_pwrite", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32))),
  HostFunction("fd_read", FuncType(Vector(I32,I32,I32,I32), Vector(I32))),
  HostFunction("fd_readdir", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32))),
  HostFunction("fd_renumber", FuncType(Vector(I32,I32), Vector(I32))),
  HostFunction("fd_seek", FuncType(Vector(I32,I64,I32,I32), Vector(I32))),
  HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
  HostFunction("fd_write", FuncType(Vector(I32,I32,I32,I32), Vector(I32))),
  HostFunction("path_create_directory", FuncType(Vector(I32,I32,I32), Vector(I32))),
  HostFunction("path_filestat_get", FuncType(Vector(I32,I32,I32,I32,I32), Vector(I32))),
  HostFunction("path_filestat_set_times", FuncType(Vector(I32,I32,I32,I32,I64,I64,I32), Vector(I32))),
  HostFunction("path_open", FuncType(Vector(I32,I32,I32,I32,I32,I64,I64,I32,I32), Vector(I32))),
  HostFunction("path_link", FuncType(Vector(I32,I32,I32,I32,I32,I32,I32), Vector(I32))),
  HostFunction("path_readlink", FuncType(Vector(I32,I32,I32,I32,I32,I32), Vector(I32))),
  HostFunction("path_remove_directory", FuncType(Vector(I32,I32,I32), Vector(I32))),
  HostFunction("path_rename", FuncType(Vector(I32,I32,I32,I32,I32,I32), Vector(I32))),
  HostFunction("path_symlink", FuncType(Vector(I32,I32,I32,I32,I32), Vector(I32))),
  HostFunction("path_unlink_file", FuncType(Vector(I32,I32,I32), Vector(I32))),
  HostFunction("poll_oneoff", FuncType(Vector(I32,I32,I32,I32), Vector(I32))),
  HostFunction("proc_exit", FuncType(Vector(I32), Vector())),
  HostFunction("random_get", FuncType(Vector(I32,I32), Vector(I32))),
  HostFunction("sched_yield", FuncType(Vector(), Vector(I32)))
)
