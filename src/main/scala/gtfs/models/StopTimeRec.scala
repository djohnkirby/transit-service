package gtfs.models

import java.time.{LocalDate, LocalDateTime, LocalTime}

case class StopTimeRec(
  stop_id: String,
  trip_id: String,
  stop_sequence: Option[Int],
  arrival_time: Option[LocalTime],
  departure_time: Option[LocalTime],
  shape_dist_traveled: Double = 0,
  stop: Stop = null,
  days_from_now: Int
) {

  /** Use given date to calucate arrival and departure time */
  def toStopTime(dt: LocalDate, offset: Long = 0): StopTime =
    new StopTime(
      this,
      arrival_time.map(LocalDateTime.of(dt.plusDays(days_from_now), _).plusMinutes(offset)),
      departure_time.map(LocalDateTime.of(dt.plusDays(days_from_now), _).plusMinutes(offset))
    )
}
