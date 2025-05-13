package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.entities.Enrollment;
import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.EventInfo;
import com.springboot.EventApp.model.entities.UserInfo;
import com.springboot.EventApp.repository.EnrollmentRepository;
import com.springboot.EventApp.repository.GroupRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/TimeToMeet")
public class TimeToMeetController {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    /**
     * @param start
     * @param end
     * @return minutes between two localDateTime objects
     */
    private int getMinutesBetween(LocalDateTime start, LocalDateTime end) {
        return ((end.getMinute() - start.getMinute()) + (60 * (end.getHour() - start.getHour())));
    }

    private boolean noOverlaps(ReturnPotentialEvent[] potentialEvents, LocalDateTime proposedStartTime, int meetingLength) {
        LocalDateTime proposedEndTime = proposedStartTime.plusMinutes(meetingLength);

        for (ReturnPotentialEvent event : potentialEvents) {
            if (event == null) continue;
            LocalDateTime existingStart = event.getEventStartTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(meetingLength);

            boolean overlaps = proposedStartTime.isBefore(existingEnd) && existingStart.isBefore(proposedEndTime);
            if (overlaps) return false;
        }

        return true;
    }


    private List<ReturnPotentialEvent> findBestMeetingTimes(int[] numAvailable, LocalDateTime startTime, int meetingLengthMinutes) {
        final int TIME_BLOCK_MINUTES = 5;
        int blockSize = meetingLengthMinutes / TIME_BLOCK_MINUTES;
        List<ReturnPotentialEvent> topEvents = new ArrayList<>();

        for (int i = 0; i <= numAvailable.length - blockSize; i++) {
            int sum = 0;
            for (int j = i; j < i + blockSize; j++) {
                sum += numAvailable[j];
            }
            double avg = (double) sum / blockSize;
            LocalDateTime proposedStart = startTime.plusMinutes(i * TIME_BLOCK_MINUTES);

            if (noOverlaps(topEvents.toArray(new ReturnPotentialEvent[0]), proposedStart, meetingLengthMinutes)) {
                topEvents.add(new ReturnPotentialEvent(proposedStart, avg));
                topEvents.sort((a, b) -> Double.compare(b.getAvgNumAvailable(), a.getAvgNumAvailable()));
                if (topEvents.size() > 3) topEvents.remove(3);  // Keep only top 3
            }
        }
        return topEvents;
    }

    @Operation(summary = "Find a time that users can meet in", description = "Finds a section of time that users in a group can meet, function prefers earlier times and non overlapping times.")
    @GetMapping("/findPotentialEvents/{lengthInMinutes}/{groupId}")
    public List<ReturnPotentialEvent> getPotentialEvents(@RequestParam("startTime") String startTimeStr, @RequestParam("endTime") String endTimeStr, @PathVariable int lengthInMinutes, @PathVariable Long groupId) {

//        FindEventTime findEventTime = new FindEventTime();
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Segments of time that are checked for availability
        final int TIME_BLOCK_MINUTES = 5;
        lengthInMinutes = ((lengthInMinutes + 4) / 5) * 5;  // round up to nearest 5 or 0

        int timespanProvided = getMinutesBetween(startTime, endTime);
        if (timespanProvided < lengthInMinutes) {
            throw new RuntimeException("provide more time for the meeting to take place");
        }

        if (startTime.getDayOfYear() != endTime.getDayOfYear() || startTime.getYear() != endTime.getYear()) {
            throw new RuntimeException("Requested finding time with differing days (illegal arguments)");
        }

        Optional<EventGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("group: " + groupId + " not found");
        }
        EventGroup group = groupOpt.get();

        List<Enrollment> enrollments = enrollmentRepository.findByGroup(group);
        List<UserInfo> users = new LinkedList<>();
        for (Enrollment e : enrollments) {
            users.add(e.getUser());
        }

        // contains a value for every 5 minutes
        int[] numAvailable = new int[timespanProvided / TIME_BLOCK_MINUTES];
        boolean[] timeUserIsBusy = new boolean[timespanProvided / TIME_BLOCK_MINUTES];
        int minuteEventStartFromRange = 0;
        int minuteEventEndFromRange = 0;
        Set<EventInfo> events;
        for (UserInfo user : users) {

            events = user.getEventIDsRSVPd();
            Arrays.fill(timeUserIsBusy, false);

            for (EventInfo event : events) {
                minuteEventStartFromRange = getMinutesBetween(startTime, event.getEventTime());
                minuteEventEndFromRange = minuteEventStartFromRange + event.getDurationInMinutes();

                int startIdx = Math.max(0, minuteEventStartFromRange / TIME_BLOCK_MINUTES);
                int endIdx = Math.min(timeUserIsBusy.length, minuteEventEndFromRange / TIME_BLOCK_MINUTES);

                for (int i = startIdx; i < endIdx; i++) {
                    timeUserIsBusy[i] = true;
                }
            }

            for (int i = 0; i < numAvailable.length; i++) {
                if (!timeUserIsBusy[i]) {
                    numAvailable[i]++;
                }
            }
        }

        return findBestMeetingTimes(numAvailable, startTime, lengthInMinutes);
    }


    /**
     * This class is used to store information regarding the events that may work
     */
    public static class ReturnPotentialEvent {
        LocalDateTime eventStartTime;
        double avgNumAvailable;

        public ReturnPotentialEvent() {
        }

        public ReturnPotentialEvent(LocalDateTime eventStartTime, double avgNumAvailable) {
            this.eventStartTime = eventStartTime;
            this.avgNumAvailable = avgNumAvailable;
        }

        public LocalDateTime getEventStartTime() {
            return eventStartTime;
        }

        public void setEventStartTime(LocalDateTime eventStartTime) {
            this.eventStartTime = eventStartTime;
        }

        public double getAvgNumAvailable() {
            return avgNumAvailable;
        }

        public void setAvgNumAvailable(double avgNumAvailable) {
            this.avgNumAvailable = avgNumAvailable;
        }
    }
}
