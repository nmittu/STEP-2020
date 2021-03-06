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

package com.google.sps.maps;

import java.util.Objects;

public final class Marker {
  private final long id;
  private final double lat;
  private final double lng;
  private final String desc;
  private final boolean isOwner;

  public Marker(long id, double lat, double lng, String desc, boolean isOwner) {
    this.id = id;
    this.lat = lat;
    this.lng = lng;
    this.desc = Objects.requireNonNull(desc);
    this.isOwner = isOwner;
  }

  public long getId() {
    return id;
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public String getDesc() {
    return desc;
  }

  public boolean getIsOwner() {
    return isOwner;
  }
}