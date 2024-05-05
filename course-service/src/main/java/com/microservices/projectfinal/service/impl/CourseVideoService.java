package com.microservices.projectfinal.service.impl;

import com.microservices.projectfinal.dto.CourseVideoCreateDTO;
import com.microservices.projectfinal.dto.CourseVideoResponseDTO;
import com.microservices.projectfinal.dto.CourseVideoUpdateDTO;
import com.microservices.projectfinal.entity.CourseEntity;
import com.microservices.projectfinal.entity.CourseVideoEntity;
import com.microservices.projectfinal.exception.ResponseException;
import com.microservices.projectfinal.model.VideoInformationModel;
import com.microservices.projectfinal.repository.CourseRepository;
import com.microservices.projectfinal.repository.CourseVideoRepository;
import com.microservices.projectfinal.service.ICourseVideoService;
import com.microservices.projectfinal.util.MediaFileUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CourseVideoService implements ICourseVideoService {

    private final CourseVideoRepository courseVideoRepository;
    private final CourseRepository courseRepository;
    private final ResourceLoader resourceLoader;

    @Transactional
    @Override
    public CourseVideoResponseDTO createCourseVideo(CourseVideoCreateDTO courseVideoCreateDTO) {
        CourseEntity courseEntity = courseRepository.findById(courseVideoCreateDTO.getCourseId()).orElseThrow(
                () -> new ResponseException("Course not found", HttpStatus.BAD_REQUEST)
        );
        //chưa handle thứ tự video
        CourseVideoEntity courseVideoEntity = CourseVideoEntity.builder()
                .title(courseVideoCreateDTO.getTitle())
                .course(courseEntity)
                .description(courseVideoCreateDTO.getDescription())
                .build();

        CourseVideoEntity semiSave = courseVideoRepository.save(courseVideoEntity);

        String thumbnailPath = MediaFileUtils.saveImage(courseVideoCreateDTO.getThumbnail());
        VideoInformationModel videoInfor = MediaFileUtils.saveVideo(courseVideoCreateDTO.getVideo());

        semiSave.setThumbnailUrl(thumbnailPath);
        semiSave.setVideoUrl(videoInfor.getPath());
        semiSave.setDuration(videoInfor.getDuration());
         CourseVideoEntity finalSaved = courseVideoRepository.save(semiSave);
        return buildCourseVideoResponse(finalSaved);
    }

    @Override
    public List<CourseVideoResponseDTO> getCourseVideoByCourseId(Long courseId) {
        List<CourseVideoEntity> courseVideoEntities = courseVideoRepository.findByCourseId(courseId);
        if (CollectionUtils.isNotEmpty(courseVideoEntities)) {
            return courseVideoEntities.stream().map(this::buildCourseVideoResponse).toList();
        }
        return List.of();
    }

    @Override
    public CourseVideoResponseDTO getCourseVideoById(Long courseId, Long courseVideoId) {
        CourseVideoEntity courseVideoEntity = courseVideoRepository.findByCourseIdAndId(courseId, courseVideoId)
                .orElseThrow(() -> new ResponseException("Video not found", HttpStatus.BAD_REQUEST));

        return buildCourseVideoResponse(courseVideoEntity);
    }

    @Override
    public Mono<Resource> getVideoResource(Long courseId, Long courseVideoId) {
        CourseVideoEntity courseVideoEntity = courseVideoRepository.findByCourseIdAndId(courseId, courseVideoId).orElseThrow(
                () -> new ResponseException("Video not found", HttpStatus.BAD_REQUEST)
        );

        if (!courseVideoEntity.getActive()) {
            throw new ResponseException("Video is inactive", HttpStatus.BAD_REQUEST);
        }

        return Mono.fromSupplier(() -> resourceLoader.getResource("file:" + courseVideoEntity.getVideoUrl()));
    }

    @Override
    public CourseVideoResponseDTO updateCourseVideo(Long courseId, Long courseVideoId, CourseVideoUpdateDTO courseVideoUpdateDTO) {
        CourseVideoEntity courseVideoEntity = courseVideoRepository.findByCourseIdAndId(courseId, courseVideoId)
                .orElseThrow(() -> new ResponseException("Video not found", HttpStatus.BAD_REQUEST));
        courseVideoEntity.update(courseVideoUpdateDTO);

        CourseVideoEntity updated = courseVideoRepository.save(courseVideoEntity);
        return buildCourseVideoResponse(updated);
    }

    @Override
    public void deleteCourseVideo(Long courseId, Long courseVideoId) {
        CourseVideoEntity courseVideoEntity = courseVideoRepository.findByCourseIdAndId(courseId, courseVideoId)
                .orElseThrow(() -> new ResponseException("Video not found", HttpStatus.BAD_REQUEST));
        courseVideoEntity.inactive();
        courseVideoRepository.save(courseVideoEntity);
    }

    private CourseVideoResponseDTO buildCourseVideoResponse(CourseVideoEntity courseVideoEntity) {
        return CourseVideoResponseDTO.builder()
                .id(courseVideoEntity.getId())
                .courseId(courseVideoEntity.getCourse().getId())
                .title(courseVideoEntity.getTitle())
                .description(courseVideoEntity.getDescription())
                .videoUrl(courseVideoEntity.getVideoUrl())
                .status(courseVideoEntity.getActive() ? "active" : "inactive")
                .duration(courseVideoEntity.getDuration())
                .numberOfOrder(courseVideoEntity.getNumberOfOrder())
                .thumbnailUrl(courseVideoEntity.getThumbnailUrl())
                .createdAt(Date.from(courseVideoEntity.getCreatedAt()))
                .modifiedAt(Date.from(courseVideoEntity.getModifiedAt()))
                .build();
    }
}
