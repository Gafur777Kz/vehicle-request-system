package kz.narxoz.vrs.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

// app.downstream.* из yaml. Звонить будем в Lab 6, сейчас только читаем
@ConfigurationProperties(prefix = "app.downstream")
public record DownstreamProperties(URI baseUrl, Duration timeout) {
}
