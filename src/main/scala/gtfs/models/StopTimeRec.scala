package gtfs.models

import java.time.{LocalDate, LocalDateTime, LocalTime}

case class StopTimeRec(
  stop_id: String,
  trip_id: String,
  stop_sequence: Int,
  arrival_time: LocalTime,
  departure_time: LocalTime,
  shape_dist_traveled: Double = 0,
  stop: Stop = null
) {
  /** Use given date to calucate arrival and departure time */
  def toStopTime(dt: LocalDate, offset: Long = 0): StopTime = {
    new StopTime(this,
      LocalDateTime.of(dt, arrival_time).plusMinutes(offset),
      LocalDateTime.of(dt, departure_time).plusMinutes(offset)
    )
  }
}
