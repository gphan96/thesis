package pkg;

import java.io.File;

public class main {
   
   public static void main(String[] args) {
      File dir = new File("SATInstances");
      File[] files = dir.listFiles();
      if (files != null) {
         for (File file : files) {            
            if (file.isFile() && file.getName().endsWith(".cnf")) {
               System.out.println(file.getName());
               //-- Execution --
               long startTime1 = System.currentTimeMillis();
               NBLSolver solver = new NBLSolver(file);
               long startTime2 = System.currentTimeMillis();
               for (int i = 0; i < 1; i++) {
                  if (solver.check()) {
                     System.out.println("satisfiable");
                  } else {
                     // System.out.println("unsatisfiable");
                  }
               }
               long endTime = System.currentTimeMillis();
               //-- Elapsed time --
               long lTime = startTime2 - startTime1;
               long pTime = endTime - startTime2;
               long tTime = lTime + pTime;
               System.out.println("------------Loading: " + lTime + " ms");
               System.out.println("------------Processing: " + pTime + " ms");
               System.out.println("------------Total: " + tTime + " ms");
            }
         }
      }
   }
}
