package egovframework.issue.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import egovframework.issue.exception.UnsupportedAttachmentTypeException;
import egovframework.issue.service.AttachmentStorageService;
import egovframework.issue.service.StoredAttachment;

@Service
public class AttachmentStorageServiceImpl implements AttachmentStorageService {

    @Value("${attachment.storage.path}")
    private String storagePath;

    @Override
    public StoredAttachment store(Long issueId, MultipartFile file) throws IOException {
        DetectedImageType detected = detectImageType(file);

        String storageKey = UUID.randomUUID().toString() + detected.extension;
        Path targetDir = Paths.get(storagePath, String.valueOf(issueId));
        Files.createDirectories(targetDir);
        Path targetPath = targetDir.resolve(storageKey);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return new StoredAttachment(file.getOriginalFilename(), storageKey, detected.mimeType, file.getSize());
    }

    @Override
    public Path resolve(Long issueId, String storageKey) {
        return Paths.get(storagePath, String.valueOf(issueId), storageKey);
    }

    /**
     * 클라이언트가 보낸 Content-Type/파일명 확장자는 신뢰하지 않고, 파일 앞부분 매직 바이트로 실제
     * 형식을 판별한다. 허용 형식(png/jpg/gif/webp) 밖이면 저장하지 않는다.
     */
    private DetectedImageType detectImageType(MultipartFile file) throws IOException {
        byte[] header = new byte[12];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(header);
            if (read < 4) {
                throw new UnsupportedAttachmentTypeException("파일 형식을 확인할 수 없습니다: " + file.getOriginalFilename());
            }
        }

        if (matches(header, 0, 0x89, 0x50, 0x4E, 0x47)) {
            return new DetectedImageType(".png", "image/png");
        }
        if (matches(header, 0, 0xFF, 0xD8, 0xFF)) {
            return new DetectedImageType(".jpg", "image/jpeg");
        }
        if (matches(header, 0, 0x47, 0x49, 0x46, 0x38)) {
            return new DetectedImageType(".gif", "image/gif");
        }
        if (matches(header, 0, 'R', 'I', 'F', 'F') && matches(header, 8, 'W', 'E', 'B', 'P')) {
            return new DetectedImageType(".webp", "image/webp");
        }

        throw new UnsupportedAttachmentTypeException(
                "지원하지 않는 첨부파일 형식입니다(png/jpg/gif/webp만 가능): " + file.getOriginalFilename());
    }

    private boolean matches(byte[] header, int offset, int... expected) {
        if (header.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((header[offset + i] & 0xFF) != (expected[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    private static final class DetectedImageType {
        private final String extension;
        private final String mimeType;

        private DetectedImageType(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
    }
}
