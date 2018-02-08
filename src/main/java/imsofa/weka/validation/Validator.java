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
public interface Validator {
    public void init(ValidationParameter validationData);
    public Instances validate(Instances inst);
}
