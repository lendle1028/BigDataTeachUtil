/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imsofa.weka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.jagg.AggregateFunction;
import net.sf.jagg.Aggregation;
import net.sf.jagg.Aggregations;
import net.sf.jagg.Aggregator;
import net.sf.jagg.AvgAggregator;
import net.sf.jagg.CountAggregator;
import net.sf.jagg.StdDevAggregator;
import net.sf.jagg.model.AggregateValue;

/**
 *
 * @author lendle
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        // TODO code application logic here
        List<Record> rawData = new ArrayList<Record>();
        rawData.add(new Record("placebo", 60.5));
        rawData.add(new Record("placebo", 62.5));
        rawData.add(new Record("placebo", 58.5));
        rawData.add(new Record("newDrug", 80.5));
        rawData.add(new Record("newDrug", 83.5));
        rawData.add(new Record("newDrug", 77.5));

        List<String> properties = new ArrayList<String>();
        properties.add("trialClass");

        List<Aggregator> aggregators = new ArrayList<Aggregator>();
        aggregators.add(new CountAggregator("*"));
        aggregators.add(new AvgAggregator("testResult"));
        aggregators.add(new StdDevAggregator("testResult"));
        
        Aggregation agg = new Aggregation.Builder().setAggregators(Arrays.asList(
                    new AggregateFunction[]{
                        new AvgAggregator("testResult")
                    }
            )).setProperties(Arrays.asList(new String[]{
                "trialClass"
            })).build();

        List<AggregateValue<Record>> aggValues = agg.groupBy(rawData);

        for (AggregateValue<Record> aggValue : aggValues) {
            Record r = aggValue.getObject();
            StringBuffer buf = new StringBuffer();
            buf.append(r.getTrialClass());
            buf.append(":");
            for (Aggregator aggregator : aggregators) {
                buf.append(" ");
                buf.append(aggregator.toString());
                buf.append("=");
                buf.append(aggValue.getAggregateValue(aggregator));
            }
            System.out.println(buf.toString());
        }
    }

}
