// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<Event> relevantEvents = new LinkedList<>(events);
    relevantEvents.removeIf(new Predicate<Event>() {
      @Override
      public boolean test(Event t) {
        Set<String> attendeesCopy = new HashSet<>(t.getAttendees());
        attendeesCopy.retainAll(request.getAttendees());
        return attendeesCopy.isEmpty();
      }
    });

    List<TimeRange> freeTime = getFreeTimes(relevantEvents);
    freeTime.removeIf(new Predicate<TimeRange>() {
      @Override
      public boolean test(TimeRange t) {
        return t.duration() < request.getDuration();
      }
    });

    return freeTime;
  }

  private List<TimeRange> getFreeTimes(List<Event> events) {
    Collections.sort(events, new Comparator<Event>() {
      @Override
      public int compare(Event o1, Event o2) {
        return TimeRange.ORDER_BY_START.compare(o1.getWhen(), o2.getWhen());
      }
    });
    
    LinkedList<TimeRange> freeTimes = new LinkedList<>();

    int i = 0;
    int start = TimeRange.START_OF_DAY;
    int end;

    while(i < events.size()) {
      end = events.get(i).getWhen().start();
      freeTimes.add(TimeRange.fromStartEnd(start, end, false));

      start = events.get(i).getWhen().end();
      while(i < events.size()-1 && events.get(i).getWhen().overlaps(events.get(i+1).getWhen())) {
        i++;
        start = Math.max(start, events.get(i).getWhen().end());
      }

      i++;
    }

    freeTimes.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));

    return freeTimes;
  }
}
