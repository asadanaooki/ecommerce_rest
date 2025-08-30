package com.example.enums.order;

import lombok.Getter;

@Getter
public enum RejectReason {
    SPAM("スパム・宣伝目的の投稿"),
    OFF_TOPIC("商品に関係のない内容"),
    INAPPROPRIATE("不適切な表現（誹謗中傷や攻撃的な内容）"),
    PERSONAL_INFO("個人情報が含まれている投稿"),
    OTHER("その他（管理者コメント参照）");
    
    private final String message;
    
    private RejectReason(String message) {
        this.message = message;
    }
}
