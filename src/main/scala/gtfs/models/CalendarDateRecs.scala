package gtfs.models

import java.time.LocalDate

/**
  * @param service_id
  * @param date Date of service
  * @param exception 'Add or 'Remove for addition/removal of service on date
  */
case class CalendarDateRec(
  service_id: String,
  date: LocalDate,
  exception: Symbol
) {
  def addService: Boolean    = exception == Symbol("Add")
  def removeService: Boolean = exception == Symbol("Remove")
}
