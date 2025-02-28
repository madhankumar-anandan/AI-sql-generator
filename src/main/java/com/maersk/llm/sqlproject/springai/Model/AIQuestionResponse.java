package com.maersk.llm.sqlproject.springai.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIQuestionResponse {
    private String message;
    private String generatedSql;
    private String dbResult;
}
