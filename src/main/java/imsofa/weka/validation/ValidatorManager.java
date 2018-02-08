/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.validation;

import weka.core.Instances;

/**
 *
 * @author lendle
 */
public class ValidatorManager {
    public static ValidatorManager getInstance(){
        return new ValidatorManager();
    }
    
    public Instances validate(Instances inst, ValidationParameter data){
        try {
            String validatorClassName=data.getValidatorClassName();
            Validator validator=(Validator) Class.forName(validatorClassName).newInstance();
            validator.init(data);
            return validator.validate(inst);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
