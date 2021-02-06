package gtfs.models

import java.time.{DayOfWeek, LocalDate}
import scala.math.Ordered.orderingToOrdered

case class CalendarRec(
  service_id: String,
  start_date: LocalDate,
  end_date: LocalDate,
  week: Array[Boolean]
) {
  require(service_id != "", "Service ID is required")
  require(start_date <= end_date, "Time must flow forward")
  require(week.length == 7, "Week must contain 7 days")

  //TODO: this feels like it could use a refactor
  def activeOn(dt: LocalDate): Boolean = activeOn(dt.getDayOfWeek.getValue)
  def activeOn(weekDay: Int): Boolean  = week(weekDay - 1)
}
