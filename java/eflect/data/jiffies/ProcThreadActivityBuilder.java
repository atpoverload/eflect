package eflect.data.jiffies;

import eflect.data.ThreadActivity;

/** Builder for {@link ThreadActivity} from /proc/ data. */
final class ProcThreadActivityBuilder {
  long id;
  String name;
  int domain;
  long taskJiffies;
  long totalJiffies;

  ProcThreadActivityBuilder setId(long id) {
    this.id = id;
    return this;
  }

  ProcThreadActivityBuilder setName(String name) {
    this.name = name;
    return this;
  }

  ProcThreadActivityBuilder setDomain(int domain) {
    this.domain = domain;
    return this;
  }

  ProcThreadActivityBuilder setTaskJiffies(long taskJiffies) {
    this.taskJiffies = taskJiffies;
    return this;
  }

  ProcThreadActivityBuilder setTotalJiffies(long totalJiffies) {
    this.totalJiffies = totalJiffies;
    return this;
  }

  double getActivity() {
    return (double) taskJiffies / totalJiffies;
  }

  ThreadActivity build() {
    return new ThreadActivity(id, name, domain, getActivity());
  }
}
