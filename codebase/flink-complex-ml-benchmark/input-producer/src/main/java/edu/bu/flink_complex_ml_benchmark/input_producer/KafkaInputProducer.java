package edu.bu.flink_complex_ml_benchmark.input_producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;

import edu.bu.flink_complex_ml_benchmark.connectors.SyntheticImageBatchesGenerator;

/**
 * Hello world!
 *
 */
public class KafkaInputProducer 
{
  SyntheticImageBatchesGenerator generator;

  public KafkaInputProducer() {
    generator = new SyntheticImageBatchesGenerator(224, 1, 300, 200);
  }

  public static void main(String[] args) {
    System.out.println( "Running Kafka Input Producer!" );

    new KafkaInputProducer().run();
  }

  public void run() {
    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("transactional.id", "input-producer-transaction");
    Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());

    producer.initTransactions();

    try {
        producer.beginTransaction();
        
        var event = generator.next();
        while (generator.hasNext()) {
          producer.send(new ProducerRecord<>("complex-ml-input", Long.toString(event.getId()), event.toString()));
        }
            
        producer.commitTransaction();
    } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
        // We can't recover from these exceptions, so our only option is to close the producer and exit.
        producer.close();
    } catch (KafkaException e) {
        // For all other exceptions, just abort the transaction and try again.
        producer.abortTransaction();
    }
    producer.close();
  }
}
