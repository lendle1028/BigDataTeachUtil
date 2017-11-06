/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka;

/**
 *
 * @author lendle
 */
public class Record {
   private String trialClass;
   private double testResult;
   public Record(String trialClass, double result) {
      this.trialClass = trialClass; testResult = result; }
   public String getTrialClass() { return trialClass; }
   public double getTestResult() { return testResult; }
}
            