package gtfs.parser

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class GtfsFileReaderSpec extends AnyFlatSpec {
  val gtfsFileReader = new GtfsFileReader("./src/test/scala/gtfs/parser/resources")

  "GtfsFileReader" should "successfully parse stop-times.txt" in {
    val stopTimes = gtfsFileReader.getStopTimes

    stopTimes.size shouldBe 13678
  }

  "GtfsFileReader" should "successfully parse stops.txt" in {
    val stops = gtfsFileReader.getStops

    stops.size shouldBe 70
  }

  "GtfsFileReader" should "successfully parse trips.txt" in {
    val trips = gtfsFileReader.getTrips

    trips.size shouldBe 1990
  }

  "GtfsFileReader" should "successfully parse calendar.txt" in {
    val calendar = gtfsFileReader.getCalendar

    calendar.size shouldBe 4
  }

  "GtfsFileReader" should "successfully parse calendar_dates.txt" in {
    val calendarDates = gtfsFileReader.getCalendarDates

    calendarDates.size shouldBe 14
  }

}
