package pl.datart.arbitup.model

final case class Opportunity(cycle: List[Currency], rates: List[Float], multiplier: Float) {
  def asString: String = {
    s"""
       |Cycle: ${cycle.mkString(" -> ")},
       |rates: ${rates.mkString(" -> ")},
       |multiplier: ${multiplier.toString}.""".stripMargin
  }
}
