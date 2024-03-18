package com.goormthon.rememberspring.diary.api.dto.response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public record DiaryContentResponseDto (
        String title,
        String date,
        List<String> hashTag,
        String contents

){
}
