package dev.starryeye.initialization;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@TestConfiguration
@EnableR2dbcAuditing
public class R2dbcAuditingTestConfig {
}
