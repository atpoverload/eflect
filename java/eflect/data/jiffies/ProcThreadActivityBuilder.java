package eflect.data.jiffies;

import eflect.data.ThreadActivity;

final class ProcThreadActivityBuilder {
  long id;
  String name;
  int domain;
  long taskJiffies;
  long totalJiffies;

  public ProcThreadActivityBuilder setId(long id) {
    this.id = id;
    return this;
  }

  public ProcThreadActivityBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public ProcThreadActivityBuilder setDomain(int domain) {
    this.domain = domain;
    return this;
  }

  public ProcThreadActivityBuilder setTaskJiffies(long taskJiffies) {
    this.taskJiffies = taskJiffies;
    return this;
  }

  public ProcThreadActivityBuilder setTotalJiffies(long totalJiffies) {
    this.totalJiffies = totalJiffies;
    return this;
  }

  public double getActivity() {
    return (double) taskJiffies / totalJiffies;
  }

  public ThreadActivity build() {
    return new ThreadActivity(id, name, domain, getActivity());
  }
}
