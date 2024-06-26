package com.microservices.projectfinal.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@JsonRootName("course_video")
@Getter
@Builder
public class CourseVideoResponseDTO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String videoUrl;
    private String status;
    private long duration;
    private int numberOfOrder;
    private String thumbnailUrl;
    private Date createdAt;
    private Date modifiedAt;

}
