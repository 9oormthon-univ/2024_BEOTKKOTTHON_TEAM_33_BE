package com.goormthon.rememberspring.diary.excepion;

import com.goormthon.rememberspring.global.error.exception.AccessDeniedGroupException;

public class DiaryAccessDeniedException extends AccessDeniedGroupException {
    public DiaryAccessDeniedException(String message) {
        super(message);
    }

    public DiaryAccessDeniedException() {
        this("본인이 아니면 다이어리를 조작할 수 없습니다!");
    }
}
