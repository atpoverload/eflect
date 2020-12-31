// package eflect.testing.data;
//
// import eflect.data.Accountant;
// import eflect.data.ActivityAccountant;
// import eflect.data.Sample;
// import eflect.data.ThreadActivity;
// import java.util.ArrayList;
// import java.util.Collection;
//
// /** Processor that merges samples into a footprint with task granularity. */
// public final class DummyEnergyAccountant implements ActivityAccountant {
//   private Collection<ThreadActivity> activity = new ArrayList<>();
//   private Accountant.Result result = Accountant.Result.ACCOUNTED;
//
//   public DummyEnergyAccountant() {}
//
//   /** Does nothing with the sample. */
//   @Override
//   public void add(Sample s) {}
//
//   /** Returns the stored data. */
//   @Override
//   public Collection<ThreadActivity> process() {
//     if (result == Accountant.Result.UNACCOUNTABLE || activity.isEmpty()) {
//       return null;
//     }
//     return activity;
//   }
//
//   /** Returns the stored result. */
//   @Override
//   public Accountant.Result isAccountable() {
//     return result;
//   }
//
//   /** Does nothing with the other accountant. */
//   @Override
//   public <T extends Accountant<Collection<ThreadActivity>>> void add(T other) {}
//
//   public void setResult(Accountant.Result result) {
//     this.result = result;
//   }
//
//   public void setActivity(Collection<ThreadActivity> activity) {
//     this.activity = activity;
//   }
// }
