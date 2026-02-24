package dev.jacaceresf.archie;

import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, PgVectorStoreAutoConfiguration.class})
public class ArchieApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchieApplication.class, args);
    }

}
