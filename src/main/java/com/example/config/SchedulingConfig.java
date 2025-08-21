package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
// TODO:
// 以下、本番環境も追加する
@Profile({"dev"})
public class SchedulingConfig {

}
