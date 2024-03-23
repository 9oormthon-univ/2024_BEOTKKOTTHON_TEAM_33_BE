package com.goormthon.rememberspring.diary.excepion;

import com.goormthon.rememberspring.global.error.exception.InvalidGroupException;

public class ExistsLikeDiaryException extends InvalidGroupException {
    public ExistsLikeDiaryException(String message) {
        super(message);
    }

}
