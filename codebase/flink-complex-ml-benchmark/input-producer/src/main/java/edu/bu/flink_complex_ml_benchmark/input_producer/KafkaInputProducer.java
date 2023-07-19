package edu.bu.flink_complex_ml_benchmark.input_producer;

import edu.bu.flink_complex_ml_benchmark.connectors.SyntheticImageBatchesGenerator;

/**
 * Hello world!
 *
 */
public class KafkaInputProducer 
{
    SyntheticImageBatchesGenerator generator;

    public KafkaInputProducer() {
        
    }

    public static void main( String[] args )
    {
        var k = new KafkaInputProducer();
        k.generator = new SyntheticImageBatchesGenerator(0, 0, 0, 0);
        System.out.println( "Hello World!" );
    }
}
