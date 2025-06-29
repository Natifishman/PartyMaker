package com.example.partymaker.utilities.groupBuilder;

import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.group.CreateGroupActivity;

/**
 * Helper implementing the builder pattern for constructing {@link Group} objects.
 */
public class GroupBuilder {
  private final Group group = new Group();

  public GroupBuilder setName(String name) {
    group.setGroupName(name);
    return this;
  }

  public GroupBuilder setAdmin(String adminKey) {
    group.setAdminKey(adminKey);
    return this;
  }

  public GroupBuilder setType(int type) {
    group.setGroupType(type);
    group.setCanAdd(type == CreateGroupActivity.GroupType.PUBLIC);
    return this;
  }

  public GroupBuilder setCreationTime(String timestamp) {
    group.setCreatedAt(timestamp);
    return this;
  }

  public GroupBuilder setPrice(String price) {
    group.setGroupPrice(price);
    return this;
  }

  public GroupBuilder setLocation(String location) {
    group.setGroupLocation(location);
    return this;
  }

  public GroupBuilder setDateTime(GroupDateTime dateTime) {
    group.setGroupDays(dateTime.getDay());
    group.setGroupMonths(dateTime.getMonth());
    group.setGroupYears(dateTime.getYear());
    group.setGroupHours(dateTime.getTime());
    return this;
  }

  public Group build() {
    return group;
  }
}
