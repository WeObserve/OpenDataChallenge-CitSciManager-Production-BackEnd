package com.sarjom.citisci.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class MongoConfig {
    private static Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${mongo.connection.url}")
    String mongoConnectionUrl;

    @Bean("mongoCustom")
    public MongoClient getMongoClient() {
        logger.info("Inside getMongoClient");

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoClientOptions.Builder options = MongoClientOptions.builder()
                .connectionsPerHost(4)
                .maxConnectionIdleTime(60000)
                .maxConnectionLifeTime(120000)
                .codecRegistry(codecRegistry);

        MongoClientURI uri = new MongoClientURI(mongoConnectionUrl, options);

        return new MongoClient(uri);
    }
}