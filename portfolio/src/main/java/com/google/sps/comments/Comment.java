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

package com.google.sps.comments;

import java.util.Objects;

public final class Comment {
  private final long id;
  private final String displayName;
  private final String comment;
  private final String imageUrl;
  private final boolean isOwner;

  public Comment(long id, String displayName, String comment, String imageUrl, boolean isOwner) {
    this.id = id;
    this.displayName = Objects.requireNonNull(displayName);
    this.comment = Objects.requireNonNull(comment);
    this.imageUrl = imageUrl;
    this.isOwner = isOwner;
  }

  public long getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getComment() {
    return comment;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public boolean getIsOwner() {
    return isOwner;
  }
}