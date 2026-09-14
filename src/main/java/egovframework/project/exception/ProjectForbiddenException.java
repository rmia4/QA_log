package egovframework.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 프로젝트 담당자가 아닌 사용자가 편집 또는 보관을 시도했을 때 발생한다. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProjectForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProjectForbiddenException(Long projectId) {
        super("이 작업은 프로젝트 담당자만 할 수 있습니다(projectId=" + projectId + ").");
    }
}
