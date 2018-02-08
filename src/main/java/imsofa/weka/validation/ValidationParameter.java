/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.validation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lendle
 */
public class ValidationParameter<V> implements Serializable{
    private V validator=null;
    private Map<String, Serializable> parameters=new HashMap<>();
    private String validatorClassName=null;

    public String getValidatorClassName() {
        return validatorClassName;
    }

    public void setValidatorClassName(String validatorClassName) {
        this.validatorClassName = validatorClassName;
    }
    
    public V getValidator() {
        return validator;
    }

    public void setValidator(V validator) {
        this.validator = validator;
    }
    
    public Serializable getParameter(String name){
        return this.parameters.get(name);
    }
    
    public void setParameter(String name, Serializable value){
        this.parameters.put(name, value);
    }
}
