package com.dev.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookStatisticsResponse {
    private Long bookId;
    private String title;
    private String isbn;
    private Long borrowCount;
}
