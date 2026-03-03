package com.pm.patientservice.rabbitmq;

import com.google.protobuf.util.JsonFormat;
import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import patient.messages.PatientMessage;

import java.util.Arrays;

@Service
public class RabbitMQProducer {
    private static final Logger log = LoggerFactory.getLogger(
            RabbitMQProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Patient patient) {
        PatientMessage patientMessage = PatientMessage.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setMessageType("PATIENT_MESSAGE_CREATED")
                .build();

        try {
            log.info("rabbitMQSend: {}", Arrays.toString(patientMessage.toByteArray()));

            String json = JsonFormat.printer().print(patientMessage);
            rabbitTemplate.convertAndSend("patientRabbitQueue", json);
        } catch (Exception e) {
            log.error("Error sending PatientCreated event: {}", patientMessage);
        }
    }
}
