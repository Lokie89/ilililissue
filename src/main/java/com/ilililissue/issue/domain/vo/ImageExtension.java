package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.issue.domain.AccessDeniedExtensionException;

import java.util.Arrays;
import java.util.List;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
public class ImageExtension extends SelfValidating<ImageExtension> {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "svg", "HEIC");

    private final String extension;

    private ImageExtension(
            String extension
    ) {
        this.extension = extension;
        this.validate();
    }

    @Override
    protected void validate() {
        super.validate();
        this.validateExtension();
    }

    private void validateExtension() {
        if (!isAllowedExtension()) {
            throw new AccessDeniedExtensionException();
        }
    }

    private boolean isAllowedExtension() {
        return ALLOWED_EXTENSIONS.contains(this.extension);
    }

    public static ImageExtension from(String extension) {
        return new ImageExtension(extension);
    }
}
