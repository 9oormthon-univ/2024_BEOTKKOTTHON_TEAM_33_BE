package com.goormthon.rememberspring.diary.excepion;

import com.goormthon.rememberspring.global.error.exception.NotFoundGroupException;

public class DiaryNotFoundException extends NotFoundGroupException {
    public DiaryNotFoundException(String message) {
        super(message);
    }

    public DiaryNotFoundException() {
        this("존재하지 않는 일기 입니다.");
    }
}
