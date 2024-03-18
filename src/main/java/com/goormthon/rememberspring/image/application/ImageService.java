package com.goormthon.rememberspring.image.application;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.goormthon.rememberspring.image.domain.Image;
import com.goormthon.rememberspring.image.domain.repository.ImageRepository;
import com.goormthon.rememberspring.member.domain.Member;
import com.goormthon.rememberspring.member.domain.repository.MemberRepository;
import com.goormthon.rememberspring.member.exception.MemberNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ImageService {

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String keyFileName;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;

    public ImageService(MemberRepository memberRepository, ImageRepository imageRepository) {
        this.memberRepository = memberRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public List<String> upload(String email, MultipartFile[] multipartFiles) throws IOException {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        List<String> responseImages = new ArrayList<>();
        int imageSequence = 1;

        for (MultipartFile file : multipartFiles) {
            String uuid = getUuid();
            Storage storage = getStorage();

            String filePath = getFilePath(member, uuid);
            String imgUrl = getImgUrl(filePath);

            storageSave(file, filePath, storage);
            imageSave(imgUrl, imageSequence, member);
            imageSequence++;

            responseImages.add(imgUrl);
        }

        return responseImages;
    }

    private static String getUuid() {
        return UUID.randomUUID().toString();
    }

    private Storage getStorage() throws IOException {
        InputStream keyFile = ResourceUtils.getURL(keyFileName).openStream();

        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();
    }

    private static String getFilePath(Member member, String uuid) {
        return member.getMemberId() + "/" + uuid;
    }

    private String getImgUrl(String filePath) {
        return "https://storage.googleapis.com/" + bucketName + "/" + filePath;
    }

    private void storageSave(MultipartFile file, String filePath, Storage storage) throws IOException {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getInputStream());
    }

    private void imageSave(String imgUrl, int imageSequence, Member member) {
        Image image = Image.builder()
                .convertImageName(imgUrl)
                .imageSequence(imageSequence)
                .member(member)
                .build();

        imageRepository.save(image);
    }

}
