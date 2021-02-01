package gtfs.parser

import java.time.LocalTime

import gtfs.models._

/**
  * Reads GTFS data from .txt files
  *
  * @param dir directory containing the files
  */
//DanielTODO: fix the missing stuff in this file
class GtfsFileReader(dir: String) extends GtfsReader {

  val formatTwoDigits = "%02d"

  private def getHour(time_string: String): String = time_string.substring(0, 2)

  private def isAfterMidnight(time_string: String): Boolean = getHour(time_string).toInt >= 24

  private def normalizeAfterMidnight(time_string: String): String = {
    val hour = getHour(time_string)
    time_string.replaceFirst(hour, formatTwoDigits.format(hour.toInt % 24))
  }

  private def parseTimeAndCorrectForAfterMidnight(time_string: String): (Option[LocalTime], Boolean) =
    if (time_string.isEmpty) //untimed stop
      (None, false)
    else //normal stop
      (Some(LocalTime.parse(normalizeAfterMidnight(time_string))), isAfterMidnight(time_string))

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

        val arrival_time_string   = s("arrival_time")
        val departure_time_string = s("departure_time")

        val (arrival_time, is_tomorrow) = parseTimeAndCorrectForAfterMidnight(arrival_time_string)
        val (departure_time, _)         = parseTimeAndCorrectForAfterMidnight(s("departure_time"))

        StopTimeRec(
          stop_id = s("stop_id"),
          trip_id = s("trip_id"),
          stop_sequence = s("stop_sequence").toInt,
          arrival_time = arrival_time,
          departure_time = departure_time,
          shape_dist_traveled = s("shape_dist_traveled").toDouble,
          stop = null,
          is_tomorrow = is_tomorrow
        )
      }
  /* override def getTrips = {
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
  }


  def getCalendar = {
    for (c <- CsvParser.fromPath(dir + "/calendar.txt"))
    yield {
      CalendarRec(
        service_id = c("service_id"),
        start_date = c("start_date"),
        end_date = c("end_date"),
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
  }


  def getCalendarDates = {
    for (c <- CsvParser.fromPath(dir + "/calendar_dates.txt"))
    yield {
      CalendarDateRec(
        service_id = c("service_id"),
        date = c("date"),
        exception = if (c("exception_type") == "1") 'Add else 'Remove
      )
    }
  } //DanielTODO: gonna need this


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
