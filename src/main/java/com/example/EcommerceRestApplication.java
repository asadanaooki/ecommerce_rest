package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceRestApplication {
    /*  TODO:
     * postman毎回コピペしてる。もっと効率よく動作確認するには？テストとかじゃなく、自分で軽く確認するとき
     * PC１端末での利用を一旦前提。別タブからの操作は考慮する
     * ServiceでWeb層のStatusException呼んでおり、疎結合になっていない。規模拡大とともにリファクタ必要かも
     * Mybatis plus導入？
     * Mapperに定義してるクエリを、ドメインごとに整理する
     * ログ実装
     * データベースに設定テーブル作る　動的に変更するため
     * DBのCheck制約をどれだけ付与するか
     * クラウドを見据えて、UTCに統一するか？
     * サニタイズ、エスケープ箇所検討
     * 設定値のDIは@Valueを使うものなのか？
     * Dtoの役割でないものもDtoクラスにしてる→Dtoの定義を明確に
     * 任意項目の入力欄は受け取る際、トリミング入れる→Review関連には入れてる
     * DuplicateKeyは409に変換すべきなのか？実装コストと頻度のトレードオフ
     * MybatisでDtoへのマップはSetterがない場合、リフレクションだがどれくらいパフォーマンス悪くなるか
     * FakeSMTP毎回クリック起動面倒→自動化
     * dev前提でhttp://localhost:8080を使用している
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
