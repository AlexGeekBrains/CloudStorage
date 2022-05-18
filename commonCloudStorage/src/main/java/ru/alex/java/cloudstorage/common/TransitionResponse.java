package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class TransitionResponse implements Serializable {
    private static final long serialVersionUID = 8471624556092334298L;
    private List<FileInfo> fileInfoList;
    private String serverPath;
}