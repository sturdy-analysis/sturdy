package sturdy.values.records

import sturdy.values.Indent
import sturdy.values.Tree

enum RecordTree[F] extends Tree:
  case MakeRecord(fields: Seq[(F, Tree)])
  case LookupRecordField(rec: Tree, field: F)
  case UpdateRecordField(rec: Tree, field: F, newval: Tree)

  override def prettyPrint(using Indent): String = this match
    case MakeRecord(fields) =>
      val fieldStrings = fields.map { case (f, t) => s"$f = ${t.prettyPrint}" }.mkString(", ")
      s"{ ${fieldStrings.mkString(", ")} }"
    case LookupRecordField(rec, field) =>
      s"${rec.prettyPrint}.$field"
    case UpdateRecordField(rec, field, newval) =>
      s"${rec.prettyPrint}.$field := ${newval.prettyPrint}"

given RecordTrees[F, R]: RecordOps[F, Tree, Tree] with
  def makeRecord(fields: Seq[(F, Tree)]): Tree = RecordTree.MakeRecord(fields)
  def lookupRecordField(rec: Tree, field: F): Tree = RecordTree.LookupRecordField(rec, field)
  def updateRecordField(rec: Tree, field: F, newval: Tree): Tree = RecordTree.UpdateRecordField(rec, field, newval)

