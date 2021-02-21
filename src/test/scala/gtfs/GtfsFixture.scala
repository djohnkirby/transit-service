package gtfs

import gtfs.models.{CalendarDateRec, CalendarRec, StopTimeRec, TripRec}

import java.time.{LocalDate, LocalTime}

trait GtfsFixture {

  def generateStopTimeRec(startTime: LocalTime, n: Int) = StopTimeRec(
    stop_id = s"stop$n",
    trip_id = "trip1",
    stop_sequence = Some(n),
    arrival_time = Some(startTime.plusMinutes(5 * n)),
    departure_time = Some(startTime.plusMinutes(5 * n)),
    days_from_now = 0
  )

  def generateStopTimeRecs(
    n: Int = 1,
    startTime: LocalTime = LocalTime.now()
  ): Seq[StopTimeRec] = Seq.tabulate(n)(i => generateStopTimeRec(startTime, i))

  def generateTrips =
    Seq(
      TripRec(
        trip_id = "trip1",
        service_id = "service1",
        route_id = "route1",
        trip_headsign = "Hoboken",
        stopTimes = Nil
      )
    )

  def generateCalendarRecs(includeWeekends: Boolean = false): Seq[CalendarRec] =
    Seq(
      CalendarRec(
        "service1",
        LocalDate.now,
        LocalDate.now.plusMonths(6),
        includeWeekends match {
          case true  => everyDay
          case false => weekdaysOnly
        }
      )
    )

  val christmas = LocalDate.of(LocalDate.now().getYear, 12, 25)

  val weekdaysOnly = Array(true, true, true, true, true, false, false)

  val everyDay = Array.fill(7)(true)

  def generateCalendarDateRec(date: LocalDate) = CalendarDateRec(
    service_id = "service1",
    date = date,
    exception = Symbol("Remove")
  )

  def generateCalendarExceptions =
    Seq(generateCalendarDateRec(christmas))
}
