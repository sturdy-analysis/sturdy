package sturdy.language.tip.analysis

import sturdy.effect.print.APrintPrefix
import sturdy.fix.OutCacheOwner
import sturdy.language.tip.GenericInterpreter
import sturdy.values.references.AllocationSiteAddr


type DaiTipOutCache = OutCacheOwner[
  GenericInterpreter.FixIn,
  GenericInterpreter.FixOut[SignAnalysis.Value],
  Map[AllocationSiteAddr, SignAnalysis.Value],
  (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value]),
  (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value])]

