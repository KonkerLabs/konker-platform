package com.konkerlabs.platform.registry.data.services.publishers;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.*;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.*;

@Service(AmazonKinesis.URI_SCHEME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherAmazonKinesis implements EventPublisher {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";

    private AmazonKinesisClientBuilder clientBuilder = new AmazonKinesisClientBuilderSDK();

    @Override
    @Async
    public void send(Event outgoingEvent, URI destinationUri, Map<String, String> data, Tenant tenant, Application application) {
        Optional.ofNullable(outgoingEvent)
                .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(destinationUri)
                .filter(uri -> !uri.toString().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Destination URI cannot be null or empty"));
        Optional.ofNullable(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(application)
                .orElseThrow(() -> new IllegalArgumentException("Application cannot be null"));
        Optional.ofNullable(data)
                .orElseThrow(() -> new IllegalArgumentException("Data cannot be null"));

        AmazonKinesis kinesisProperties = AmazonKinesis.builder().build();
        kinesisProperties.setValues(data);

        Optional.ofNullable(kinesisProperties.getKey())
                .orElseThrow(() -> new IllegalArgumentException("Key cannot be null"));
        Optional.ofNullable(kinesisProperties.getSecret())
                .orElseThrow(() -> new IllegalArgumentException("Secret cannot be null or empty"));
        Optional.ofNullable(kinesisProperties.getStreamName())
                .orElseThrow(() -> new IllegalArgumentException("Stream name cannot be null"));
        Optional.ofNullable(kinesisProperties.getRegion())
                .orElseThrow(() -> new IllegalArgumentException("Region cannot be null"));

        try {

            com.amazonaws.services.kinesis.AmazonKinesis kinesisClient = clientBuilder.build(kinesisProperties);

            PutRecordRequest putRecordRequest = new PutRecordRequest();
            putRecordRequest.setData(ByteBuffer.wrap(outgoingEvent.getPayload().getBytes()));
            putRecordRequest.setPartitionKey(String.format("%s-%s", outgoingEvent.getIncoming().getChannel(), outgoingEvent.getTimestamp().toEpochMilli()));
            putRecordRequest.setStreamName(kinesisProperties.getStreamName());

            PutRecordResult result = kinesisClient.putRecord(putRecordRequest);
            LOGGER.info("Kinesis Result. Sequence Number: {}", result.getSequenceNumber());

        } catch (SdkClientException sdkEx) {
            LOGGER.info(
                    MessageFormat.format(EVENT_DROPPED, destinationUri, sdkEx.getMessage()),
                    tenant.toURI(),
                    tenant.getLogLevel());
        }

    }

    public interface AmazonKinesisClientBuilder {

        com.amazonaws.services.kinesis.AmazonKinesis build(AmazonKinesis kinesisProperties);

    }

    public class AmazonKinesisClientBuilderSDK implements AmazonKinesisClientBuilder {

        public com.amazonaws.services.kinesis.AmazonKinesis build(AmazonKinesis kinesisProperties) {

            com.amazonaws.services.kinesis.AmazonKinesisClientBuilder clientBuilder = com.amazonaws.services.kinesis.AmazonKinesisClientBuilder.standard();

            clientBuilder.setRegion(kinesisProperties.getRegion());
            clientBuilder.setCredentials(new AWSCredentialsProvider() {
                @Override
                public AWSCredentials getCredentials() {
                    return new BasicAWSCredentials(kinesisProperties.getKey(), kinesisProperties.getSecret());
                }
                @Override
                public void refresh() {
                }
            });
            clientBuilder.setClientConfiguration(new ClientConfiguration());

            return clientBuilder.build();
        }
    }

    public void setClientBuilder(AmazonKinesisClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

}