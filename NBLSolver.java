import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NBLSolver {
   private int numVariables;
   private int numClauses;
   private List<Clause> instanceSAT;
   private HashMap<Integer, List<BigDecimal>> noisesByClause;
   private Random random;

   public NBLSolver(File file) {
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
         String line;
         int index = 0;
         while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("c") || line.isEmpty()) {
               continue;
            } else if (line.startsWith("p cnf")) {
               String[] tokens = line.split("\\s+");
               numVariables = Integer.parseInt(tokens[2]);
               numClauses = Integer.parseInt(tokens[3]);
               instanceSAT = new ArrayList<>(numClauses);
            } else {
               String[] tokens = line.split("\\s+");
               List<Integer> literals = new ArrayList<>();
               for (int i = 0; i < tokens.length - 1; i++) {
                  if (!tokens[i].isEmpty()) {
                     int literal = Integer.parseInt(tokens[i]);
                     literals.add(literal);
                  }
               }
               if (literals.isEmpty()) {
                  break;
               }
               index++;
               instanceSAT.add(new Clause(index, literals));
            }
         }
      } catch (IOException e) {
         System.err.println("Failed to read file: " + file.getAbsolutePath());
         e.printStackTrace();
      }
   }

   //-- NOISE SOURCE --

   private double noiseSource() {
      return (random.nextDouble() * 2 - 1);
   }

   //-- TAU --

   private BigDecimal constructHyperspace() {
      noisesByClause = new HashMap<>(numClauses + 1, 1.0f);
      BigDecimal result = BigDecimal.ONE;
      for (int i = 1; i <= numVariables; i++) {
         BigDecimal product1 = BigDecimal.ONE;
         BigDecimal product2 = BigDecimal.ONE;
         for (int j = 1; j <= numClauses; j++) {
            // Generate noise source for 2 literals of variable x in clause c
            BigDecimal positive = BigDecimal.valueOf(noiseSource());
            BigDecimal negative = BigDecimal.valueOf(noiseSource());
            // Group the noise source by clause index
            List<BigDecimal> tempList1 = noisesByClause.get(j);
            if (tempList1 == null) {
               tempList1 = new ArrayList<>();
               noisesByClause.put(j, tempList1);
            }
            tempList1.add(positive);
            tempList1.add(negative);
            
            product1 = product1.multiply(positive);
            product2 = product2.multiply(negative);
         }
         result = result.multiply(product1.add(product2));
      }
      return result;
   }

   //-- SIGMA --

   private BigDecimal constructInstanceNBL() {
      BigDecimal result = BigDecimal.ONE;
      for (Clause clause : instanceSAT) {
         BigDecimal sum = BigDecimal.ZERO;
         for (int literal : clause.getLiterals()) {
            sum = sum.add(cube(clause.getIndex(), literal));
         }
         result = result.multiply(sum);
      }
      return result;
   }

   private BigDecimal cube(int clause, int literal) {
      List<BigDecimal> tempList = noisesByClause.get(clause);
      BigDecimal result = BigDecimal.ONE;
      // Noise sources of each clause are listed by the order:
      // [x1, -x1, x2, -x2, ...]
      for (int i = 0; i < numVariables; i++) {
         if (literal < 0 && i + 1 == Math.abs(literal)) {
            result = result.multiply(tempList.get(i * 2 + 1));
            continue;
         } else if (literal > 0 && i + 1 == literal) {
            result = result.multiply(tempList.get(i * 2));
            continue;
         }
         BigDecimal positive = tempList.get(i * 2);
         BigDecimal negative = tempList.get(i * 2 + 1);
         result = result.multiply(positive.add(negative));
      }
      return result;
   }

   //-- CHECKER --

   public BigDecimal check() {
      random = new Random();
      BigDecimal tau = constructHyperspace();
      BigDecimal sigma = constructInstanceNBL();
      return tau.multiply(sigma);
   }
}
