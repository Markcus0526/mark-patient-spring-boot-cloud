package com.pm.analyticsservice.rabbitmq;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import patient.messages.PatientMessage;

@Service
public class RabbitConsumer {
    private static final Logger log = LoggerFactory.getLogger(
            RabbitConsumer.class);

    @RabbitListener(queues = "patientRabbitQueue")
    public void consumeMessage(String jsonMessage) { // 1. Change byte[] to String
        try {
            // 2. Create the Protobuf Builder
            PatientMessage.Builder builder = PatientMessage.newBuilder();

            // 3. Use Protobuf's JSON Parser to merge the String into the Builder
            JsonFormat.parser().ignoringUnknownFields().merge(jsonMessage, builder);

            // 4. Build the final object
            PatientMessage patientMessage = builder.build();

            log.info("Parsed JSON to Protobuf successfully: ID={}", patientMessage.getPatientId());

        } catch (Exception e) {
            log.error("Failed to parse JSON string: {}", e.getMessage());
        }
    }
}

