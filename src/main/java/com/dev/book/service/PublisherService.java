package com.dev.book.service;

import com.dev.book.dto.PublisherRequest;
import com.dev.book.dto.PublisherResponse;

import java.util.List;

public interface PublisherService {

    PublisherResponse create(PublisherRequest request);

    List<PublisherResponse> getAll();

    PublisherResponse getById(Long id);

    PublisherResponse update(Long id, PublisherRequest request);

    void delete(Long id);
}
