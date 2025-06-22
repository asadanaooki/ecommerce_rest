package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceRestApplication {
    // TODO:
    // postman毎回コピペしてる。もっと効率よく動作確認するには？
    // テストとかじゃなく、自分で軽く確認するとき

	public static void main(String[] args) {
		SpringApplication.run(EcommerceRestApplication.class, args);
	}

}
