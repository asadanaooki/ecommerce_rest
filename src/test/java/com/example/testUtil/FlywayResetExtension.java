package com.example.testUtil;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class FlywayResetExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        DataSource dataSource = ctx.getBean(DataSource.class);
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .cleanDisabled(false)
                .validateOnMigrate(false)
                .locations("classpath:db/migration_test")
                .load();
        
        flyway.clean();
        flyway.migrate();
    }
}
