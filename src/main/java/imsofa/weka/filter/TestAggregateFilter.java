/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka.filter;

import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.jagg.AggregateFunction;
import net.sf.jagg.Aggregation;
import net.sf.jagg.AvgAggregator;
import org.apache.commons.beanutils.BeanUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.SimpleBatchFilter;

/**
 *
 * @author lendle
 */
public class TestAggregateFilter extends SimpleBatchFilter {

    protected String functions="";
    protected String aggregatedProperties="";
    protected String groupbyProperties = "";

    public String getFunctions() {
        return functions;
    }

    public void setFunctions(String functions) {
        this.functions = functions;
    }

    public String getAggregatedProperties() {
        return aggregatedProperties;
    }

    public void setAggregatedProperties(String aggregatedProperties) {
        this.aggregatedProperties = aggregatedProperties;
    }

    

    public String getGroupbyProperties() {
        return groupbyProperties;
    }

    public void setGroupbyProperties(String groupbyProperties) {
        this.groupbyProperties = groupbyProperties;
    }

    @Override
    public String globalInfo() {
        return "test";
    }

    @Override
    protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
        return this.process(inputFormat);
    }

    @Override
    protected Instances process(Instances instances) throws Exception {
        BeanGenerator beanGenerator = new BeanGenerator();
        beanGenerator.setNamingPolicy(new NamingPolicy() {
            @Override
            public String getClassName(String string, String string1, Object o, Predicate prdct) {
                return "imsofa.bean.temp"+System.currentTimeMillis();
            }
        });
        Map<String, Class> props = new HashMap<>();

        
        Enumeration en = instances.enumerateAttributes();
        while (en.hasMoreElements()) {
            Attribute attr = (Attribute) en.nextElement();
            switch (attr.type()) {
                case Attribute.NUMERIC:
                    props.put(attr.name(), double.class);
                    break;
                case Attribute.NOMINAL:
                    props.put(attr.name(), String.class);
                    break;
                case Attribute.DATE:
                    props.put(attr.name(), Date.class);
                    break;
                case Attribute.STRING:
                    props.put(attr.name(), String.class);
                    break;
                default:
                    props.put(attr.name(), String.class);
                    break;
            }
        }
        BeanGenerator.addProperties(beanGenerator, props);
        Class c = (Class) beanGenerator.createClass();
        
        List list = new ArrayList();
        en = instances.enumerateInstances();
        while (en.hasMoreElements()) {
            Instance instance = (Instance) en.nextElement();
            Object o = c.newInstance();
            for (int i = 0; i < instance.numAttributes(); i++) {
                //System.out.println(instance.value(i));
                if (instance.attribute(i).isNumeric()) {
                    BeanUtils.setProperty(o, instance.attribute(i).name(),
                            instance.value(i));
                } else {
                    BeanUtils.setProperty(o, instance.attribute(i).name(),
                            instance.stringValue(i));
                }
            }
            list.add(o);
        }

        Aggregation agg = new Aggregation.Builder().setAggregators(Arrays.asList(
                new AggregateFunction[]{
                    new AvgAggregator("underLipTop_x")
                }
        )).setProperties(Arrays.asList(new String[]{
            "time"
        })).build();
        return instances;
    }

    public static void main(String[] args) {
        try {
            BeanGenerator beanGenerator = new BeanGenerator();
            beanGenerator.setNamingPolicy(new NamingPolicy() {
                @Override
                public String getClassName(String string, String string1, Object o, Predicate prdct) {
                    return "test.bean.Test";
                }
            });
            Map<String, Class> props = new HashMap<>();

            CSVLoader loader = new CSVLoader();
            loader.setFile(new File("03face.csv"));
            Instances instances = loader.getDataSet();
            Enumeration en = instances.enumerateAttributes();
            while (en.hasMoreElements()) {
                Attribute attr = (Attribute) en.nextElement();
                System.out.println(attr.name());
                switch (attr.type()) {
                    case Attribute.NUMERIC:
                        props.put(attr.name(), double.class);
                        break;
                    case Attribute.NOMINAL:
                        props.put(attr.name(), String.class);
                        break;
                    case Attribute.DATE:
                        props.put(attr.name(), Date.class);
                        break;
                    case Attribute.STRING:
                        props.put(attr.name(), String.class);
                        break;
                    default:
                        props.put(attr.name(), String.class);
                        break;
                }
            }
            BeanGenerator.addProperties(beanGenerator, props);
            Class c = (Class) beanGenerator.createClass();
            System.out.println(Arrays.deepToString(c.getDeclaredMethods()));
            List list = new ArrayList();
            en = instances.enumerateInstances();
            while (en.hasMoreElements()) {
                Instance instance = (Instance) en.nextElement();
                Object o = c.newInstance();
                for (int i = 0; i < instance.numAttributes(); i++) {
                    //System.out.println(instance.value(i));
                    if (instance.attribute(i).isNumeric()) {
                        BeanUtils.setProperty(o, instance.attribute(i).name(),
                                instance.value(i));
                    } else {
                        BeanUtils.setProperty(o, instance.attribute(i).name(),
                                instance.stringValue(i));
                    }
                }
                list.add(o);
            }

            System.out.println(new Gson().toJson(list));
            Aggregation agg = new Aggregation.Builder().setAggregators(Arrays.asList(
                    new AggregateFunction[]{
                        new AvgAggregator("underLipTop_x")
                    }
            )).setProperties(Arrays.asList(new String[]{
                "time"
            })).build();
            System.out.println(agg.groupBy(list));
        } catch (Exception ex) {
            Logger.getLogger(TestAggregateFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
