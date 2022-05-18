package sturdy.language.tip.analysis

import sturdy.effect.print.APrintPrefix
import sturdy.fix.OutCacheOwner
import sturdy.language.tip.GenericInterpreter
import sturdy.values.references.AllocationSiteAddr


type DaiTipOutCacheSign = OutCacheOwner[
  GenericInterpreter.FixIn,
  GenericInterpreter.FixOut[SignAnalysis.Value],
  Map[AllocationSiteAddr, SignAnalysis.Value],
  (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value]),
  (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value])]

type DaiTipOutCacheInterval = OutCacheOwner[
  GenericInterpreter.FixIn,
  GenericInterpreter.FixOut[IntervalAnalysis.Value],
  Map[AllocationSiteAddr, IntervalAnalysis.Value],
  (Map[AllocationSiteAddr, IntervalAnalysis.Value], APrintPrefix.PrintResult[IntervalAnalysis.Value]),
  (Map[AllocationSiteAddr, IntervalAnalysis.Value], APrintPrefix.PrintResult[IntervalAnalysis.Value])]
