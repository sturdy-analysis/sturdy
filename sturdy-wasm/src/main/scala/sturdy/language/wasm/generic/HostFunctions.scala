package sturdy.language.wasm.generic

import sturdy.values.Structural
import swam.{FuncType, ValType}

import ValType.*

class HostFunction(val name: String, val funcType: FuncType):
  HostFunction.instances :+= this
  override def toString: String = name
object HostFunction:
  var instances: Vector[HostFunction] = Vector.empty
  
  
given Structural[HostFunction] with {}

object wasi:
  val module: ModuleInstance = new ModuleInstance(Some("wasi"))

  def get(name: String): (Int, HostFunction) =
    functions.getOrElse(name, throw IllegalArgumentException(s"No host function with name $name.")).swap

  val functions: Map[String, (HostFunction, Int)] = List(
    new HostFunction("args_sizes_get", FuncType(Vector(I32,I32), Vector(I32))),
    new HostFunction("args_get", FuncType(Vector(I32,I32), Vector(I32))),
    new HostFunction("clock_time_get", FuncType(Vector(I32,I32,I32), Vector(I32))),
    new HostFunction("environ_sizes_get", FuncType(Vector(I32,I32), Vector(I32))),
    new HostFunction("environ_get", FuncType(Vector(I32,I32), Vector(I32))),
    new HostFunction("fd_allocate", FuncType(Vector(I32,I64,I64), Vector(I32))),
    new HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
    new HostFunction("fd_fdstat_get", FuncType(Vector(I32, I32), Vector(I32))),
    new HostFunction("fd_fdstat_set_flags", FuncType(Vector(I32, I32), Vector(I32))),
    new HostFunction("fd_filestat_get", FuncType(Vector(I32, I32), Vector(I32))),
    new HostFunction("fd_filestat_set_size", FuncType(Vector(I32, I64), Vector(I32))),
    new HostFunction("fd_filestat_set_times", FuncType(Vector(I32, I64,I64,I32), Vector(I32))),
    new HostFunction("fd_pread", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32))),
    new HostFunction("fd_prestat_dir_name", FuncType(Vector(I32,I32,I32), Vector(I32))),
    new HostFunction("fd_prestat_get", FuncType(Vector(I32,I32), Vector(I32))),
    new HostFunction("fd_pwrite", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32))),
    new HostFunction("fd_read", FuncType(Vector(I32,I32,I32,I32), Vector(I32))),
    new HostFunction("fd_readdir", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32))),
    new HostFunction("fd_renumber", FuncType(Vector(I32,I32), Vector(I32))),
    new HostFunction("fd_seek", FuncType(Vector(I32,I64,I32,I32), Vector(I32))),
    new HostFunction("fd_close", FuncType(Vector(I32), Vector(I32))),
    new HostFunction("fd_write", FuncType(Vector(I32,I32,I32,I32), Vector(I32))),
    new HostFunction("path_create_directory", FuncType(Vector(I32,I32,I32), Vector(I32))),
    new HostFunction("path_filestat_get", FuncType(Vector(I32,I32,I32,I32,I32), Vector(I32))),
    new HostFunction("path_filestat_set_times", FuncType(Vector(I32,I32,I32,I32,I64,I64,I32), Vector(I32))),
    new HostFunction("path_open", FuncType(Vector(I32,I32,I32,I32,I32,I64,I64,I32,I32), Vector(I32))),
    new HostFunction("path_link", FuncType(Vector(I32,I32,I32,I32,I32,I32,I32), Vector(I32))),
    new HostFunction("path_readlink", FuncType(Vector(I32,I32,I32,I32,I32,I32), Vector(I32))),
    new HostFunction("path_remove_directory", FuncType(Vector(I32,I32,I32), Vector(I32))),
    new HostFunction("path_rename", FuncType(Vector(I32,I32,I32,I32,I32,I32), Vector(I32))),
    new HostFunction("path_symlink", FuncType(Vector(I32,I32,I32,I32,I32), Vector(I32))),
    new HostFunction("path_unlink_file", FuncType(Vector(I32,I32,I32), Vector(I32))),
    new HostFunction("poll_oneoff", FuncType(Vector(I32,I32,I32,I32), Vector(I32))),
    new HostFunction("proc_exit", FuncType(Vector(I32), Vector())),
    new HostFunction("random_get", FuncType(Vector(I32,I32), Vector(I32))),
    new HostFunction("sched_yield", FuncType(Vector(), Vector(I32)))
  ).zipWithIndex.map(f => f._1.name -> f).toMap
