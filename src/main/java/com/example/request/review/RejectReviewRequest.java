package com.example.request.review;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.enums.order.RejectReason;
import com.example.json.annotation.TrimToNull;

import lombok.Data;

@Data
public class RejectReviewRequest {

    @NotNull
    private RejectReason reason;

    @TrimToNull
    @Size(max = 500)
    private String note;
    
    
    @AssertTrue(message = "REJECT_NOTE_REQUIRED_WHEN_OTHER")
    public boolean isRequiredWhenOther() {
        if (reason != RejectReason.OTHER) {
            return true;
        }
        return note != null && !note.isBlank();
    }
}
