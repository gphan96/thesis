package pkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
   private String fileName;
   private Utilities utils;

   public NBLSolver(File file) {
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
         fileName = file.getName();
         String line;
         int clause_index = 1;
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
               List<Integer> clause = new ArrayList<>();
               for (int i = 0; i < tokens.length - 1; i++) {
                  if (!tokens[i].isEmpty()) {
                     int literal = Integer.parseInt(tokens[i]);
                     clause.add(literal);
                  }
               }
               if (clause.isEmpty()) {
                  break;
               }
               instanceSAT.add(new Clause(clause_index, clause));
               clause_index++;
            }
         }
      } catch (IOException e) {
         System.err.println("Failed to read file: " + file.getAbsolutePath());
         e.printStackTrace();
      }
   }

   //-- NOISE SOURCE --

   private BigDecimal noiseSource() {
      return BigDecimal.valueOf(random.nextDouble() * 2 - 1);
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
            BigDecimal positive = noiseSource();
            BigDecimal negative = noiseSource();
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
            BigDecimal noise = tempList.get(i * 2 + 1);
            result = result.multiply(noise);
            continue;
         } else if (literal > 0 && i + 1 == literal) {
            BigDecimal noise = tempList.get(i * 2);
            result = result.multiply(noise);
            continue;
         }
         BigDecimal positive = tempList.get(i * 2);
         BigDecimal negative = tempList.get(i * 2 + 1);
         result = result.multiply(positive.add(negative));
      }
      return result;
   }

   //-- CHECKER --

   public boolean check() {
      utils = new Utilities();
      boolean satifiability = true;
      BigDecimal S_N = BigDecimal.ZERO;
      List<BigDecimal> meanList = new ArrayList<>();

      int totalSample = 100000000;
      BigDecimal meanPre = BigDecimal.ZERO;

      for (int i = 1; i <= totalSample; i++) {
         random = new Random();
         BigDecimal tau = constructHyperspace();
         BigDecimal sigma = constructInstanceNBL();
         S_N = S_N.add(tau.multiply(sigma));
         BigDecimal meanCur = S_N.divide(BigDecimal.valueOf(i), RoundingMode.HALF_UP);
         meanList.add(meanCur);

         int threshold = 12;
         if (utils.checkStop(meanCur, meanPre, threshold)) {
             System.out.println(i);
             break;
         }
         meanPre = meanCur;
      }

      BigDecimal meanMax = utils.getMaxAbs(meanList);
      BigDecimal tolerance = meanMax.multiply(BigDecimal.valueOf(0.01));
      BigDecimal totalMean = utils.getMean(meanList);
      // System.out.println("Max:               " + meanMax);
      // System.out.println("Total Mean:        " + totalMean);

      if (totalMean.abs().compareTo(tolerance) <= 0) {
         satifiability = false;
      }

      int step = 1000;
      new Chart(meanList, totalMean, tolerance, fileName, step);
      return satifiability;
   }

}
