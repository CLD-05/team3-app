package com.foldy.domain.stats.Dto;

public record TagStatResponseDto(
	    String tagName,
	    Long count
	) {
	    // JPQL 생성자 조회(Projection)를 위해 선언
	    public TagStatResponseDto(String tagName, Long count) {
	        this.tagName = tagName;
	        this.count = count;
	    }
	}