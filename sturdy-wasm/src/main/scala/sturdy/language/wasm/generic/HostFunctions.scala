package sturdy.language.wasm.generic

import sturdy.values.Structural
import swam.{FuncType, ValType}
import swam.NumType.*

enum HostFunction(val name: String, val funcType: FuncType) extends Enumeration:
  case args_sizes_get extends HostFunction("args_sizes_get", FuncType(Vector(I32,I32), Vector(I32)))
  case args_get extends HostFunction("args_get", FuncType(Vector(I32,I32), Vector(I32)))
  case clock_time_get extends HostFunction("clock_time_get", FuncType(Vector(I32,I32,I32), Vector(I32)))
  case environ_sizes_get extends HostFunction("environ_sizes_get", FuncType(Vector(I32,I32), Vector(I32)))
  case environ_get extends HostFunction("environ_get", FuncType(Vector(I32,I32), Vector(I32)))
  case fd_allocate extends HostFunction("fd_allocate", FuncType(Vector(I32,I64,I64), Vector(I32)))
  case fd_close extends HostFunction("fd_close", FuncType(Vector(I32), Vector(I32)))
  case fd_fdstat_get extends HostFunction("fd_fdstat_get", FuncType(Vector(I32, I32), Vector(I32)))
  case fd_fdstat_set_flags extends HostFunction("fd_fdstat_set_flags", FuncType(Vector(I32, I32), Vector(I32)))
  case fd_filestat_get extends HostFunction("fd_filestat_get", FuncType(Vector(I32, I32), Vector(I32)))
  case fd_filestat_set_size extends HostFunction("fd_filestat_set_size", FuncType(Vector(I32, I64), Vector(I32)))
  case fd_filestat_set_times extends HostFunction("fd_filestat_set_times", FuncType(Vector(I32, I64,I64,I32), Vector(I32)))
  case fd_pread extends HostFunction("fd_pread", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32)))
  case fd_prestat_dir_name extends HostFunction("fd_prestat_dir_name", FuncType(Vector(I32,I32,I32), Vector(I32)))
  case fd_prestat_get extends HostFunction("fd_prestat_get", FuncType(Vector(I32,I32), Vector(I32)))
  case fd_pwrite extends HostFunction("fd_pwrite", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32)))
  case fd_read extends HostFunction("fd_read", FuncType(Vector(I32,I32,I32,I32), Vector(I32)))
  case fd_readdir extends HostFunction("fd_readdir", FuncType(Vector(I32,I32,I32,I64,I32), Vector(I32)))
  case fd_renumber extends HostFunction("fd_renumber", FuncType(Vector(I32,I32), Vector(I32)))
  case fd_seek extends HostFunction("fd_seek", FuncType(Vector(I32,I64,I32,I32), Vector(I32)))
  case fd_sync extends HostFunction("fd_close", FuncType(Vector(I32), Vector(I32)))
  case fd_write extends HostFunction("fd_write", FuncType(Vector(I32,I32,I32,I32), Vector(I32)))
  case path_create_directory extends HostFunction("path_create_directory", FuncType(Vector(I32,I32,I32), Vector(I32)))
  case path_filestat_get extends HostFunction("path_filestat_get", FuncType(Vector(I32,I32,I32,I32,I32), Vector(I32)))
  case path_filestat_set_times extends HostFunction("path_filestat_set_times", FuncType(Vector(I32,I32,I32,I32,I64,I64,I32), Vector(I32)))
  case path_open extends HostFunction("path_open", FuncType(Vector(I32,I32,I32,I32,I32,I64,I64,I32,I32), Vector(I32)))
  case path_link extends HostFunction("path_link", FuncType(Vector(I32,I32,I32,I32,I32,I32,I32), Vector(I32)))
  case path_readlink extends HostFunction("path_readlink", FuncType(Vector(I32,I32,I32,I32,I32,I32), Vector(I32)))
  case path_remove_directory extends HostFunction("path_remove_directory", FuncType(Vector(I32,I32,I32), Vector(I32)))
  case path_rename extends HostFunction("path_rename", FuncType(Vector(I32,I32,I32,I32,I32,I32), Vector(I32)))
  case path_symlink extends HostFunction("path_symlink", FuncType(Vector(I32,I32,I32,I32,I32), Vector(I32)))
  case path_unlink_file extends HostFunction("path_unlink_file", FuncType(Vector(I32,I32,I32), Vector(I32)))
  case poll_oneoff extends HostFunction("poll_oneoff", FuncType(Vector(I32,I32,I32,I32), Vector(I32)))
  case proc_exit extends HostFunction("proc_exit", FuncType(Vector(I32), Vector()))
  case random_get extends HostFunction("random_get", FuncType(Vector(I32,I32), Vector(I32)))
  case sched_yield extends HostFunction("sched_yield", FuncType(Vector(), Vector(I32)))

  override def toString(): String = name


given Structural[HostFunction] with {}

object HostFunction:
  val nameMap: Map[String, HostFunction] = HostFunction.values.map(f => f.name -> f).toMap
  
  def nameToHostFunction(name: String): HostFunction =
    nameMap.getOrElse(name, throw IllegalArgumentException(s"No host function with name $name."))