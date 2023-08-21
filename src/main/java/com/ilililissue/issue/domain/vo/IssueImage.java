package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotNull;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */

public class IssueImage extends SelfValidating<IssueImages> {

    @NotNull
    private final String fileName;

    @NotNull
    private final ImageExtension extension;

    private IssueImage(
            String fileName,
            ImageExtension extension
    ) {
        this.fileName = fileName;
        this.extension = extension;
        this.validate();
    }

    public static IssueImage of(String fileName, ImageExtension extension) {
        return new IssueImage(fileName, extension);
    }

    public static IssueImage from(String fullFileName) {
        return splitExtension(fullFileName);
    }

    // ???
    private static IssueImage splitExtension(String fullFileName) {
        int indexExtension = fullFileName.lastIndexOf("\\.");
        String fileName = fullFileName.substring(0, indexExtension);
        String extension = fileName.substring(indexExtension + 1);
        return new IssueImage(fileName, ImageExtension.from(extension));
    }

}
