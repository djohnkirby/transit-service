package gtfs

import gtfs.parser.GtfsFileReader
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar

import java.time.{DayOfWeek, LocalDate, LocalTime}

class ScheduleFinderSpec extends AnyFlatSpec with GtfsFixture with MockitoSugar {

  val gtfsFileReader: GtfsFileReader = mock[GtfsFileReader]

  when(gtfsFileReader.getStopTimes).thenReturn(generateStopTimeRecs(10).iterator)
  when(gtfsFileReader.getTrips).thenReturn(generateTrips.iterator)
  when(gtfsFileReader.getCalendar).thenReturn(generateCalendarRecs(includeWeekends = true).iterator)
  when(gtfsFileReader.getCalendarDates).thenReturn(generateCalendarExceptions.iterator)

  val stationIds = Set("stop1", "Stop1", "STOP1")

  val scheduleFinder = new ScheduleFinder(new ScheduleFinder.Dependencies {
    override val gtfsfileReader: GtfsFileReader = gtfsFileReader
  })

  behavior of "isThisStation"

  it should "return true when the stop time's station is one provided" in {
    scheduleFinder.isThisStation(
      generateStopTimeRecs(10).tail.head.toStopTime(LocalDate.now()),
      stationIds
    ) shouldBe true
  }

  it should "return false when provided an empty set" in {
    scheduleFinder
      .isThisStation(generateStopTimeRecs(10).head.toStopTime(LocalDate.now()), Set.empty[String]) shouldBe false
  }

  it should "return false when provided a set that doesn't contain this stop" in {
    scheduleFinder
      .isThisStation(generateStopTimeRecs(10).head.toStopTime(LocalDate.now()), Set("stop10000")) shouldBe false
  }

  behavior of "isUpcomingStopTime"

  it should "return true when the stopTime is in the next two hours" in {
    scheduleFinder.isUpcomingStopTime(generateStopTimeRecs(10).head.toStopTime(LocalDate.now())) shouldBe true
  }

  it should "return false when the stopTime is in the past" in {

    scheduleFinder.isUpcomingStopTime(
      generateStopTimeRecs(10, LocalTime.now().minusHours(1)).head.toStopTime(LocalDate.now())
    ) shouldBe false
  }

  it should "return false when the stopTime is at least 2 hours from now" in {
    scheduleFinder.isUpcomingStopTime(
      generateStopTimeRecs(10, LocalTime.now().plusHours(2)).tail.head.toStopTime(LocalDate.now())
    ) shouldBe false
  }

  behavior of "isRunningToday"

  it should "return true when it's a weekday and this service is running" in {
    scheduleFinder.isRunningToday(generateStopTimeRecs(10).head.toStopTime(nextWeekday)) shouldBe true
  }

  it should "return false when it's the weekend and this service is not running" in {
    when(gtfsFileReader.getCalendar).thenReturn(generateCalendarRecs(includeWeekends = false).iterator)
    val weekdayOnlyScheduleFinder = new ScheduleFinder(new ScheduleFinder.Dependencies {
      override val gtfsfileReader: GtfsFileReader = gtfsFileReader
    })

    weekdayOnlyScheduleFinder.isRunningToday(generateStopTimeRecs(10).head.toStopTime(nextWeekendDay)) shouldBe false
  }

  it should "return false when it's a holiday and this service is not running" in {
    scheduleFinder.isRunningToday(generateStopTimeRecs(10).head.toStopTime(christmas)) shouldBe false
  }

  behavior of "scheduleFinder"

  it should "find schedule" in {
    scheduleFinder.findSchedule(stationIds, 0) shouldBe "HOB: 4"
  }

}
