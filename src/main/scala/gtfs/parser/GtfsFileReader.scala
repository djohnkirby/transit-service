package gtfs.parser

import java.time.{LocalDate, LocalTime}
import gtfs.models._

import java.time.format.DateTimeFormatter

/**
  * Reads GTFS data from .txt files
  *
  * @param dir directory containing the files
  */
//DanielTODO: fix the missing stuff in this file
class GtfsFileReader(dir: String) extends GtfsReader {

  final val formatTwoDigits = "%02d"

  private def getHour(time_string: String): String = time_string.substring(0, 2)

  private def getNumberOfDaysFromNow(time_string: String): Int = getHour(time_string).toInt / 24

  private def normalizeAfterMidnight(time_string: String): String = {
    val hour = getHour(time_string)
    time_string.replaceFirst(hour, formatTwoDigits.format(hour.toInt % 24))
  }

  private def parseTimeAndCorrectForAfterMidnight(time_string: String): (Option[LocalTime], Int) =
    if (time_string.isEmpty) //untimed stop
      (None, 0)
    else //normal stop
      (Some(LocalTime.parse(normalizeAfterMidnight(time_string))), getNumberOfDaysFromNow(time_string))

  override def getStops =
    for (s <- CsvParser.fromPath(dir + "/stops.txt"))
      yield {
        Stop(
          stop_id = s("stop_id"),
          stop_name = s("stop_name"),
          stop_desc = s("stop_desc"),
          stop_lat = s("stop_lat").toDouble,
          stop_lon = s("stop_lon").toDouble
        )
      }

  /*  override def getRoutes = {
    for (r <- CsvParser.fromPath(dir + "/routes.txt"))
    yield {
      Route(
        route_id = r("route_id"),
        agency_id = r("agency_id"),
        route_short_name = r("route_short_name"),
        route_long_name = r("route_long_name"),
        route_desc = r("route_desc"),
        route_type = RouteType(r("route_type").toInt),
        route_url = r("route_url"),
        route_color = r("route_color"),
        route_text_color = r("route_text_color")
      )
    }
  }*/

  override def getStopTimes =
    for (s <- CsvParser.fromPath(dir + "/stop_times.txt"))
      yield {

        val (arrival_time, _)               = parseTimeAndCorrectForAfterMidnight(s("arrival_time"))
        val (departure_time, days_from_now) = parseTimeAndCorrectForAfterMidnight(s("departure_time"))
        val stop_sequence = s("stop_sequence") match {
          case ""     => None
          case string => Some(string.toInt)
        }

        StopTimeRec(
          stop_id = s("stop_id"),
          trip_id = s("trip_id"),
          stop_sequence = stop_sequence,
          arrival_time = arrival_time,
          departure_time = departure_time,
          shape_dist_traveled = s("shape_dist_traveled").toDouble,
          stop = null,
          days_from_now = days_from_now
        )
      }
  override def getTrips =
    for (t <- CsvParser.fromPath(dir + "/trips.txt"))
      yield {
        TripRec(
          trip_id = t("trip_id"),
          service_id = t("service_id"),
          route_id = t("route_id"),
          trip_headsign = t("trip_headsign"),
          stopTimes = Nil
        )
      }

  def getCalendar =
    for (c <- CsvParser.fromPath(dir + "/calendar.txt"))
      yield {
        CalendarRec(
          service_id = c("service_id"),
          start_date = LocalDate.parse(c("start_date"), DateTimeFormatter.BASIC_ISO_DATE),
          end_date = LocalDate.parse(c("end_date"), DateTimeFormatter.BASIC_ISO_DATE),
          week = Array(
            c("monday") == "1",
            c("tuesday") == "1",
            c("wednesday") == "1",
            c("thursday") == "1",
            c("friday") == "1",
            c("saturday") == "1",
            c("sunday") == "1"
          )
        )
      }

  def getCalendarDates =
    for (c <- CsvParser.fromPath(dir + "/calendar_dates.txt"))
      yield {
        CalendarDateRec(
          service_id = c("service_id"),
          date = LocalDate.parse(c("date"), DateTimeFormatter.BASIC_ISO_DATE),
          exception = if (c("exception_type") == "1") Symbol("Add") else Symbol("Remove")
        )
      }

  /*
  override def getFrequencies = {
    for (f <- CsvParser.fromPath(dir + "/frequencies.txt"))
    yield {
      Frequency(
        trip_id = f("trip_id"),
        start_time = f("start_time"),
        end_time = f("end_time"),
        headway = f("headway_secs").toInt.seconds
      )
    }
  }*/
}
