import java.util.List;

public class Clause {
   private final int index;
   private final List<Integer> literals;

   public Clause(int index, List<Integer> literals) {
      this.index = index;
      this.literals = literals;
   }

   public int getIndex() {
      return this.index;
   }

   public List<Integer> getLiterals() {
      return this.literals;
   }

}
