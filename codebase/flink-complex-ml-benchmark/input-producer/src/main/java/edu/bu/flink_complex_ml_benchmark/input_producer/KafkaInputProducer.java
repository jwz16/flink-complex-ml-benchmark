package edu.bu.flink_complex_ml_benchmark.input_producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bu.flink_complex_ml_benchmark.connectors.SyntheticImageBatchesGenerator;

public class KafkaInputProducer {
  protected static Logger logger = LoggerFactory.getLogger(KafkaInputProducer.class);

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
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    Producer<String, String> producer = new KafkaProducer<>(props);

    try {
      
      var event = generator.next();
      while (generator.hasNext()) {
        producer.send(new ProducerRecord<>("complex-ml-input", Long.toString(event.getId()), event.serialize()));
      }

    } catch (KafkaException e) {
      logger.error(e.getMessage());
    }

    producer.close();
  }
}
