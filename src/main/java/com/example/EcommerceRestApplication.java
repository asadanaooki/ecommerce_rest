package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceRestApplication {
    /*  TODO:
      postman毎回コピペしてる。もっと効率よく動作確認するには？
      テストとかじゃなく、自分で軽く確認するとき
      PC１端末での利用を一旦前提。別タブからの操作は考慮する
      ServiceでWeb層のStatusException呼んでおり、疎結合になっていない。規模拡大とともにリファクタ必要かも
      Mybatis plus導入する
      MapperはEntityを返すほうがよいかも
    */
	public static void main(String[] args) {
	    System.out.println("=== JVM 情報 ===");
	    System.out.println("java.home:       " + System.getProperty("java.home"));
	    System.out.println("java.vendor:     " + System.getProperty("java.vendor"));
	    System.out.println("java.version:    " + System.getProperty("java.version"));
	    System.out.println("java.class.path: " + System.getProperty("java.class.path"));
	    System.out.println("user.dir:        " + System.getProperty("user.dir"));
	    System.out.println("================");
		SpringApplication.run(EcommerceRestApplication.class, args);
	}

}
