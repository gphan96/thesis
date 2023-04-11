import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class NBLSolver {
   private int numVariables;
   private int numClauses;
   private List<Clause> instanceSAT;
   private HashMap<Integer, List<Double>> noisesByClause;
   private HashMap<Integer, List<Double>> noisesByLiteral;

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
      Random random = new Random();
      for (int i = 1; i <= numClauses; i++) {
         for (int j = -numVariables; j <= numVariables; j++) {
            if (j == 0) {
               j = 1;
            }
            double value = random.nextDouble() - 0.5;
            // group the noise source by clause index
            List<Double> tempList1 = noisesByClause.get(i);
            if (tempList1 == null) {
               tempList1 = new ArrayList<>();
               noisesByClause.put(i, tempList1);
            }
            tempList1.add(value);
            // group the noise source by literal
            List<Double> tempList2 = noisesByLiteral.get(j);
            if (tempList2 == null) {
               tempList2 = new ArrayList<>();
               noisesByLiteral.put(j, tempList2);
            }
            tempList2.add(value);
         }
      }
   }

   //-- TAU --

   private double constructHyperspace() {
      double result = 1.0;
      for (int i = 1; i <= numVariables; i++) {
         double product1 = 1.0;
         double product2 = 1.0;
         List<Double> tempList = noisesByLiteral.get(i);
         for (Double value : tempList) {
            product1 *= value;
         }
         tempList = noisesByLiteral.get(-i);
         for (Double value : tempList) {
            product2 *= value;
         }
         result *= (product1 + product2);
      }
      return result;
   }

   //-- SIGMA --

   private double constructInstanceNBL() {
      double result = 1.0;
      for (Clause clause : instanceSAT) {
         double sum = 0.0;
         for (int literal : clause.getLiterals()) {
            sum += cube(clause.getIndex(), literal);
         }
         result *= sum;
      }
      return result;
   }

   private double cube(int clause, int literal) {
      List<Double> tempList = noisesByClause.get(clause);
      double result = 1.0;
      for (int i = 0; i < numVariables; i++) {
         if (literal < 0 && i == literal + numVariables) {
            result *= tempList.get(i);
            continue;
         } else if (literal > 0 && i == -literal + numVariables) {
            result *= tempList.get(literal - 1 + numVariables);
            continue;
         }
         double noise1 = tempList.get(i);
         double noise2 = tempList.get((2 * numVariables - 1) - i);
         result *= (noise1 + noise2);
      }
      return result;
   }

   //-- CHECKER --

   public double check() {
      generateNoiseSource();
      double tau = constructHyperspace();
      double sigma = constructInstanceNBL();      
      return tau * sigma;
   }
}
