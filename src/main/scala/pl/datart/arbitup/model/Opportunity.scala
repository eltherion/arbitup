package pl.datart.arbitup.model

final case class Opportunity(cycle: List[Currency], multiplier: Float) {
  def asString: String = {
    s"${cycle.mkString(" -> ")}, multiplier: ${multiplier.toString}"
  }
}
