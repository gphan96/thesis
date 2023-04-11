import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NBLSolver {
   private int numVariables;
   private int numClauses;
   private List<Clause> instanceSAT;
   private HashMap<Integer, List<BigDecimal>> noisesByClause;
   private HashMap<Integer, List<BigDecimal>> noisesByLiteral;

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

   //-- NOISE SOURCES --

   private void generateNoiseSource() {
      noisesByClause = new HashMap<>(numClauses + 1, 1.0f);
      noisesByLiteral = new HashMap<>(2 * numVariables + 1, 1.0f);
      for (int i = 1; i <= numClauses; i++) {
         for (int j = -numVariables; j <= numVariables; j++) {
            if (j == 0) {
               j = 1;
            }
            BigDecimal value = new BigDecimal(Math.random() - 0.5);
            // group the noise source by clause index
            List<BigDecimal> tempList1 = noisesByClause.get(i);
            if (tempList1 == null) {
               tempList1 = new ArrayList<>();
               noisesByClause.put(i, tempList1);
            }
            tempList1.add(value);
            // group the noise source by literal
            List<BigDecimal> tempList2 = noisesByLiteral.get(j);
            if (tempList2 == null) {
               tempList2 = new ArrayList<>();
               noisesByLiteral.put(j, tempList2);
            }
            tempList2.add(value);
         }
      }
   }

   //-- TAU --

   private BigDecimal constructHyperspace() {
      BigDecimal result = BigDecimal.ONE;
      for (int i = 1; i <= numVariables; i++) {
         BigDecimal product1 = BigDecimal.ONE;
         BigDecimal product2 = BigDecimal.ONE;
         List<BigDecimal> tempList = noisesByLiteral.get(i);
         for (BigDecimal value : tempList) {
            product1 = product1.multiply(value);
         }
         tempList = noisesByLiteral.get(-i);
         for (BigDecimal value : tempList) {
            product2 = product2.multiply(value);
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
      for (int i = 0; i < numVariables; i++) {
         if (literal < 0 && i == literal + numVariables) {
            result = result.multiply(tempList.get(i));
            continue;
         } else if (literal > 0 && i == -literal + numVariables) {
            result = result.multiply(tempList.get(literal - 1 + numVariables));
            continue;
         }
         BigDecimal noise1 = tempList.get(i);
         BigDecimal noise2 = tempList.get((2 * numVariables - 1) - i);
         result = result.multiply(noise1.add(noise2));
      }
      return result;
   }

   //-- CHECKER --

   public BigDecimal check() {
      generateNoiseSource();
      BigDecimal tau = constructHyperspace();
      BigDecimal sigma = constructInstanceNBL();      
      return tau.multiply(sigma);
   }
}
