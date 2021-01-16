package pl.datart.arbitup.model

final case class Opportunity(cycle: List[Currency], multiplyer: Float) {
  def asString: String = {
    s"Found opportunity: ${cycle.mkString(" -> ")}, multiplyer: ${multiplyer.toString}."
  }
}
